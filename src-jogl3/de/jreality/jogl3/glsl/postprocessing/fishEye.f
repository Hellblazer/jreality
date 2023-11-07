#version 150
// fishEye shader, GLSL code adapted from: geek3d.com


// WHAT IT DOES: distort spherically, has some numerical atan problems i think

out vec4 glFragColor;
smooth in vec4 texCoord;


uniform sampler2D image;
uniform vec3 R_inverseFilterTextureSize;
//uniform float R_fxaaSpanMax;
//uniform float R_fxaaReduceMin;
//uniform float R_fxaaReduceMul;

const float PI = 3.1415926535;
 
void main()
{
  float aperture = 178.0;
  float apertureHalf = 0.5 * aperture * (PI / 180.0);
  float maxFactor = sin(apertureHalf);
  
  vec2 uv;
  vec2 xy = 2.0 * texCoord.xy - 1.0;
  float d = length(xy);
  if (d <= (2.0-maxFactor))
  {
    d = length(xy * maxFactor);
    float z = sqrt(1.0 - d * d);
    float r = atan(d, z) / PI;
    float phi = atan(xy.y, xy.x);
    
    uv.x = r * cos(phi) + 0.5;
    uv.y = r * sin(phi) + 0.5;
  }
  else
  {
    uv = texCoord.xy;
  }
  vec4 c = texture2D(image, uv);
  glFragColor = c;
}