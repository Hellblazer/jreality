/*******************************************************
* Fixed.frag Fixed Function Equivalent Fragment Shader *
*   Automatically Generated by 3Dlabs GLSL ShaderGen   *
*             http://developer.3dlabs.com              *
*******************************************************/
uniform sampler2D texUnit0;

void main (void) 
{
    vec4 color;

    color = gl_Color;

    color *= texture2D(texUnit0, gl_TexCoord[0].xy);


//   float fog;

//    fog = (gl_Fog.end - gl_FogFragCoord) * gl_Fog.scale;

//    fog = clamp(fog, 0.0, 1.0);

//    color = vec4(mix( vec3(gl_Fog.color), vec3(color), fog), color.a);

    gl_FragColor = color;
}
