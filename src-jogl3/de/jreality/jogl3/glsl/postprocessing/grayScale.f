#version 150
// grayscale shader, GLSL code adapted from: geek3d.com


// WHAT IT DOES: grayscale by lumination.

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
	
	// luma is a non-arbritrary constant vector used to compute luminocity
	vec3 luma = vec3(0.299, 0.587, 0.114);	
	// compute the luminocity of the texture at the Top Left, Bottom Right etc.
	float lumaM  = dot(luma, texture2D(image, texCoord.xy).xyz);
	glFragColor = vec4(lumaM);	
		
}