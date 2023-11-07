#version 150

// WHAT IT DOES: edge detection, highlighting

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

	// shamelessly copied from: https://open.gl/framebuffers
	
	vec4 lumcoeff = vec4(0.299, 0.587, 0.114, 0);
	vec2 texOffset = R_inverseFilterTextureSize.xy;
	
	
	vec2 tc0 = texCoord.xy + vec2(-texOffset.x, -texOffset.y);
  	vec2 tc1 = texCoord.xy + vec2(         0.0, -texOffset.y);
  	vec2 tc2 = texCoord.xy + vec2(-texOffset.x,          0.0);
  	vec2 tc3 = texCoord.xy + vec2(+texOffset.x,          0.0);
  	vec2 tc4 = texCoord.xy + vec2(         0.0, +texOffset.y);
  	vec2 tc5 = texCoord.xy + vec2(+texOffset.x, +texOffset.y);
  
  	vec4 col0 = texture2D(image, tc0);
  	vec4 col1 = texture2D(image, tc1);
  	vec4 col2 = texture2D(image, tc2);
  	vec4 col3 = texture2D(image, tc3);
  	vec4 col4 = texture2D(image, tc4);
  	vec4 col5 = texture2D(image, tc5);

  	vec4 sum = vec4(0.5) + (col0 + col1 + col2) - (col3 + col4 + col5);
  	float lum = dot(sum, lumcoeff);
  	glFragColor = vec4(lum, lum, lum, 1.0) ;//* vertColor;
  	glFragColor = vec4(lum);
  	
  	// luma is a non-arbritrary constant vector used to compute luminocity
	vec3 luma = vec3(0.299, 0.587, 0.114);	
	// compute the luminocity of the texture at the Top Left, Bottom Right etc.
	float lumaM  = dot(luma, texture2D(image, texCoord.xy).xyz);
//	glFragColor = vec4(lumaM);	  

	
}