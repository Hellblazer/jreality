#version 150
// FXAA shader, GLSL code adapted from:
// http://horde3d.org/wiki/index.php5?title=Shading_Technique_-_FXAA
// Whitepaper describing the technique:
// http://developer.download.nvidia.com/assets/gamedev/files/sdk/11/FXAA_WhitePaper.pdf

// april 2015, commented by padilla
// for jreality: this code was https://github.com/BennyQBD/3DEngineCpp/blob/054c2dcd7c52adcf8c9da335a2baee78850504b8/res/shaders/filter-fxaa.fs
// and explained on youtube: https://www.youtube.com/watch?v=Z9bYzpwVINA 

// WHAT IT DOES: FXAA is anti aliasing by post proccessing. It detects edge (by luminosity here) and blurs them if needed
// we apply this to a texture that has the final image of the rendereing process.

out vec4 glFragColor;
smooth in vec4 texCoord;


uniform sampler2D image;
uniform vec3 R_inverseFilterTextureSize;
//uniform float R_fxaaSpanMax;
//uniform float R_fxaaReduceMin;
//uniform float R_fxaaReduceMul;

// needed somehow (maybe)
//layout(location = 0) out vec3 gl_FragColor;

void main(void)
{

	//vec3 R_inverseFilterTextureSize= vec3(1.0/1024.0,1.0/780.0,0.0);

	// for testing, these are not uniforms
	float R_fxaaSpanMax = 8.0;
	float R_fxaaReduceMin = 1.0/128.0;
	float R_fxaaReduceMul = 1.0/8.0;

	// the inversceFilterTextureSize is needed to transform coordinate systems. Here we copy it without the 3rd
	// component because it is only a 2D texture
	vec2 texCoordOffset = R_inverseFilterTextureSize.xy;
	
	// luma is a non-arbritrary constant vector used to compute luminocity
	vec3 luma = vec3(0.299, 0.587, 0.114);	
	// compute the luminocity of the texture at the Top Left, Bottom Right etc.
	float lumaTL = dot(luma, texture2D(image, texCoord.xy + (vec2(-1.0, -1.0) * texCoordOffset)).xyz);
	float lumaTR = dot(luma, texture2D(image, texCoord.xy + (vec2(1.0, -1.0) * texCoordOffset)).xyz);
	float lumaBL = dot(luma, texture2D(image, texCoord.xy + (vec2(-1.0, 1.0) * texCoordOffset)).xyz);
	float lumaBR = dot(luma, texture2D(image, texCoord.xy + (vec2(1.0, 1.0) * texCoordOffset)).xyz);
	float lumaM  = dot(luma, texture2D(image, texCoord.xy).xyz);

	// compute the range of the lumas
	float lumaMin = min(lumaM, min(min(lumaTL, lumaTR), min(lumaBL, lumaBR)));
	float lumaMax = max(lumaM, max(max(lumaTL, lumaTR), max(lumaBL, lumaBR)));

	// create the direction vector that will show us the direction of the edge (if any)
	vec2 dir;
	dir.x = -((lumaTL + lumaTR) - (lumaBL + lumaBR));
	dir.y = ((lumaTL + lumaBL) - (lumaTR + lumaBR));
	
	// scale this edge vector, and try not to devide by zero or get too big. The smalles value (x or y) is scaled to 1
	float dirReduce = max((lumaTL + lumaTR + lumaBL + lumaBR) * (R_fxaaReduceMul * 0.25), R_fxaaReduceMin);
	float inverseDirAdjustment = 1.0/(min(abs(dir.x), abs(dir.y)) + dirReduce);
	
	// apply the scaling
	dir = min(vec2(R_fxaaSpanMax, R_fxaaSpanMax), 
		max(vec2(-R_fxaaSpanMax, -R_fxaaSpanMax), dir * inverseDirAdjustment)) * texCoordOffset;

	// average 2 pixel values in the direction of the edge
	vec3 result1 = (1.0/2.0) * (
		texture2D(image, texCoord.xy + (dir * vec2(1.0/3.0 - 0.5))).xyz +
		texture2D(image, texCoord.xy + (dir * vec2(2.0/3.0 - 0.5))).xyz);

	// and try to average 4
	vec3 result2 = result1 * (1.0/2.0) + (1.0/4.0) * (
		texture2D(image, texCoord.xy + (dir * vec2(0.0/3.0 - 0.5))).xyz +
		texture2D(image, texCoord.xy + (dir * vec2(3.0/3.0 - 0.5))).xyz);

	// compute the luminosity of the average with 4 samples. If it is too different, use the average of 2 only.	
	float lumaResult2 = dot(luma, result2);
	
	// reason: because the human eye is very sensitive to changes in luminosity
    if(lumaResult2 < lumaMin || lumaResult2 > lumaMax){
		glFragColor = vec4(result1, 1.0);
		}
	else{
		glFragColor = vec4(result2, 1.0);
		}
	//glFragColor = vec4( texture2D(image, texCoord.xy).xyz , 1.0 );	
	// just for testing if the shader works at all	
//	glFragColor = texture(image, texCoord.st);

	// black and white test
//  float lumaAvg =(lumaTL + lumaTR + lumaBL + lumaBR)*0.25;
//  glFragColor = vec4(lumaAvg);
	//glFragColor = vec4(lumaM);

	// avgerage color test
//	vec3 avgColor = (
//		texture2D(image, texCoord.xy + (vec2(-1.0, -1.0) * texCoordOffset)).xyz +
//		texture2D(image, texCoord.xy + (vec2(1.0, -1.0) * texCoordOffset)).xyz +
//		texture2D(image, texCoord.xy + (vec2(-1.0, 1.0) * texCoordOffset)).xyz +
//		texture2D(image, texCoord.xy + (vec2(1.0, 1.0) * texCoordOffset)).xyz
//		)*0.25;
//		glFragColor = vec4(avgColor,1.0);
		
}