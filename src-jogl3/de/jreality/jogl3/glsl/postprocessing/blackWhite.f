#version 150
// FXAA shader, GLSL code adapted from:


// WHAT IT DOES: discrete black and white by lumination.

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
	vec4 lumcoeff = vec4(0.299, 0.587, 0.114, 0);
	vec4 col = texture2D(image, texCoord.st);
  	float lum = dot(col, lumcoeff);
  	if (0.5 < lum) {
    	glFragColor = vec4(1, 1, 1, 1);//vertColor;
  	} else {
    	glFragColor = vec4(0, 0, 0, 1);
  	} 
		
}