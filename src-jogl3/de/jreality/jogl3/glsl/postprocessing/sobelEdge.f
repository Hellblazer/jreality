#version 150

// WHAT IT DOES: edge detection, highlighting

out vec4 glFragColor;
smooth in vec4 texCoord;


uniform sampler2D image;
uniform vec3 R_inverseFilterTextureSize;
uniform int textureWidth;
uniform int textureHeight;
//uniform vec2i FilterTextureSize;
//uniform float R_fxaaSpanMax;
//uniform float R_fxaaReduceMin;
//uniform float R_fxaaReduceMul;

// needed somehow (maybe)
//layout(location = 0) out vec3 gl_FragColor;

void main(void)
{

	// shamelessly copied from: https://open.gl/framebuffers
	//vec2 offSet = R_inverseFilterTextureSize.xy;
	 vec2 texCoordOffset = vec2( 1.0/textureWidth , 1.0/textureHeight );
	//vec2 offSet = vec2( 1.0/float(textureHeight) , 1.0/float(textureWidth) );
	
	// directions
	//vec2 SW = vec2(texCoord.u - offSet.x , texCoord.v - offset.y);
	//vec2 SE = vec2(texCoord.u + offSet.x , texCoord.v - offset.y);
	//vec2 NW = vec2(texCoord.u - offSet.x , texCoord.v + offset.y);
	//vec2 NE = vec2(texCoord.u + offSet.x , texCoord.v + offset.y);
	vec2 SW = texCoord.xy + (vec2(-1.0, -1.0) * texCoordOffset);
	vec2 SE = texCoord.xy + (vec2(1.0, 1.0) * texCoordOffset);
	vec2 NW = texCoord.xy + (vec2(-1.0, 1.0) * texCoordOffset);
	vec2 NE = texCoord.xy + (vec2(1.0, -1.0) * texCoordOffset);
	
	//vec4 s1 = texture2D(image, texCoord.xy - offSet.x - offSet.y);
	//vec4 s2 = texture2D(image, texCoord.xy + offSet.x - offSet.y);
	//vec4 s3 = texture2D(image, texCoord.xy - offSet.x + offSet.y);
	//vec4 s4 = texture2D(image, texCoord.xy + offSet.x + offSet.y);
	vec4 s1 = texture2D(image, SW);
	vec4 s2 = texture2D(image, SE);
	vec4 s3 = texture2D(image, NW);
	vec4 s4 = texture2D(image, NE);
	
	vec4 sx = 4.0 * ((s4 + s3) - (s2 + s1));
	vec4 sy = 4.0 * ((s2 + s4) - (s1 + s3));
	vec4 sobel = sqrt(sx * sx + sy * sy);
	
		
	glFragColor = sobel;		
	//glFragColor = vec4(0.0);
	
}