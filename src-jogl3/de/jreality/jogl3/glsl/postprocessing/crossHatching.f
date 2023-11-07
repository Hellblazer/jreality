#version 150
// crosshatch shader, GLSL code adapted from:
//geeks3d.com

// WHAT IT DOES: draw lines like a pencil

out vec4 glFragColor;
smooth in vec4 texCoord;


uniform sampler2D image;
uniform vec3 R_inverseFilterTextureSize;
uniform int textureWidth;
uniform int textureHeight;
//uniform float R_fxaaSpanMax;
//uniform float R_fxaaReduceMin;
//uniform float R_fxaaReduceMul;

// needed somehow (maybe)
//layout(location = 0) out vec3 gl_FragColor;

void main(void)
{
	float vx_offset = 2.0;
	float hatch_y_offset = 5.0;
	float lum_threshold_1= 1.0;
	float lum_threshold_2= 0.7;
	float lum_threshold_3= 0.5;
	float lum_threshold_4 = 0.3;
	
	
	 vec2 uv = texCoord.xy;
  
	vec3 tc = vec3(1.0, 0.0, 0.0);
	if (uv.x < (vx_offset-0.005)) {
    	float lum = length(texture2D(image, uv).rgb);
    	tc = vec3(1.0, 1.0, 1.0);
  
    if (lum < lum_threshold_1) 
    {
      if (mod(gl_FragCoord.x + gl_FragCoord.y, 10.0) == 0.0) 
        tc = vec3(0.0, 0.0, 0.0);
    }  
  
    if (lum < lum_threshold_2) 
    {
      if (mod(gl_FragCoord.x - gl_FragCoord.y, 10.0) == 0.0) 
        tc = vec3(0.0, 0.0, 0.0);
    }  
  
    if (lum < lum_threshold_3) 
    {
      if (mod(gl_FragCoord.x + gl_FragCoord.y - hatch_y_offset, 10.0) == 0.0) 
        tc = vec3(0.0, 0.0, 0.0);
    }  
  
    if (lum < lum_threshold_4) 
    {
      if (mod(gl_FragCoord.x - gl_FragCoord.y - hatch_y_offset, 10.0) == 0.0) 
        tc = vec3(0.0, 0.0, 0.0);
    }
  }
  else if (uv.x > (vx_offset+0.005))
  {
    tc = texture2D(image, uv).rgb;
  }
  
  glFragColor = vec4(tc, 1.0);
		
		
}