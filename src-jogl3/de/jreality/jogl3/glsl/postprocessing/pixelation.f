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
	
	float vx_offset = 10;
	float rt_w = 1.0/texOffset.x; // GeeXLab built-in
	float rt_h = 1.0/texOffset.y; // GeeXLab built-in
	float pixel_w = 10.0;
	float pixel_h = 10.0;
	
	vec2 uv = texCoord.xy;
  
  	vec3 tc = vec3(1.0, 0.0, 0.0);
  	if (uv.x < (vx_offset-0.005)){
    	float dx = pixel_w*(1./rt_w);
    	float dy = pixel_h*(1./rt_h);
    	vec2 coord = vec2(dx*floor(uv.x/dx), dy*floor(uv.y/dy));
    	tc = texture2D(image, coord).rgb;
  	}
  	else if (uv.x>=(vx_offset+0.005)){
    	tc = texture2D(image, uv).rgb;
  	}
	glFragColor = vec4(tc, 1.0);

	
}