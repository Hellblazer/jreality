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
	
	
	vec2 texOffset = R_inverseFilterTextureSize.xy;
	
	
	vec2 tc0 = texCoord.st + vec2(-texOffset.s, -texOffset.t);
  	vec2 tc1 = texCoord.st + vec2(         0.0, -texOffset.t);
  	vec2 tc2 = texCoord.st + vec2(+texOffset.s, -texOffset.t);
  	vec2 tc3 = texCoord.st + vec2(-texOffset.s,          0.0);
  	vec2 tc4 = texCoord.st + vec2(         0.0,          0.0);
  	vec2 tc5 = texCoord.st + vec2(+texOffset.s,          0.0);
  	vec2 tc6 = texCoord.st + vec2(-texOffset.s, +texOffset.t);
  	vec2 tc7 = texCoord.st + vec2(         0.0, +texOffset.t);
  	vec2 tc8 = texCoord.st + vec2(+texOffset.s, +texOffset.t);
  
  	vec4 col0 = texture2D(image, tc0);
  	vec4 col1 = texture2D(image, tc1);
  	vec4 col2 = texture2D(image, tc2);
  	vec4 col3 = texture2D(image, tc3);
  	vec4 col4 = texture2D(image, tc4);
  	vec4 col5 = texture2D(image, tc5);
  	vec4 col6 = texture2D(image, tc6);
  	vec4 col7 = texture2D(image, tc7);
  	vec4 col8 = texture2D(image, tc8);

  	vec4 sum = 8.0 * col4 - (col0 + col1 + col2 + col3 + col5 + col6 + col7 + col8); 
  	glFragColor = vec4(sum.rgb, 1.0);// * vertColor; 

	
}