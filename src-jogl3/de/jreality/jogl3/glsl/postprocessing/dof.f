#version 150
// Depth Of Field shader, GLSL code adapted from:

// source http://artmartinsh.blogspot.de/2010/02/glsl-lens-blur-filter-with-bokeh.html
// april 2015, commented by padilla

// WHAT IT DOES: also reads a depth texture and blurs out far away objects.
// we apply this to a texture that has the final image of the rendereing process.

// result. play around with the hard coded const. The effect is really not that beneficial since the aliasing 

// new idea: in order to not blend objects at borders of greate depth difference,
// estimate the part of the image covered by the objects in the forground for bluring. 

//basics
out vec4 glFragColor;
smooth in vec4 texCoord;

// texture for color and depth
uniform sampler2D colorImage;
uniform sampler2D depthImage;

// screen dimensions
uniform vec3 R_inverseFilterTextureSize;
 
 //dof parameters
const float blurclamp = 2.0;  // max blur amount
const float bias = 0.6; //aperture - bigger values for shallower depth of field
// uniform float focus;  // this value comes from ReadDepth script.
const uniform float focus = 0.005; 
void main()
{
 		// preparation
        float aspectratio = R_inverseFilterTextureSize.y/R_inverseFilterTextureSize.x;
        vec2 aspectcorrect = vec2(1.0,aspectratio);
       
        vec4 depth1   = texture2D(depthImage,texCoord.xy );
 
 		// try to scale the depth
 		//float depth = sqrt(depth1.x);
 
		// compute the factor 
        float factor = ( depth1.x - focus );
 
 		// keep it between these two limits        
        vec2 dofblur = vec2 (clamp( factor * bias, -blurclamp, blurclamp ));
 
 
 		// compute the average color taken from a big sample. dofblur controlls the strength
        vec4 col = vec4(0.0);
       
        col += texture2D(colorImage, texCoord.xy);
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.0,0.4 )*aspectcorrect) * dofblur);
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.15,0.37 )*aspectcorrect) * dofblur);
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.29,0.29 )*aspectcorrect) * dofblur);
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.37,0.15 )*aspectcorrect) * dofblur);       
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.4,0.0 )*aspectcorrect) * dofblur);   
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.37,-0.15 )*aspectcorrect) * dofblur);       
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.29,-0.29 )*aspectcorrect) * dofblur);       
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.15,-0.37 )*aspectcorrect) * dofblur);
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.0,-0.4 )*aspectcorrect) * dofblur); 
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.15,0.37 )*aspectcorrect) * dofblur);
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.29,0.29 )*aspectcorrect) * dofblur);
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.37,0.15 )*aspectcorrect) * dofblur); 
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.4,0.0 )*aspectcorrect) * dofblur); 
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.37,-0.15 )*aspectcorrect) * dofblur);       
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.29,-0.29 )*aspectcorrect) * dofblur);       
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.15,-0.37 )*aspectcorrect) * dofblur);
       
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.15,0.37 )*aspectcorrect) * dofblur*0.9);
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.37,0.15 )*aspectcorrect) * dofblur*0.9);           
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.37,-0.15 )*aspectcorrect) * dofblur*0.9);           
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.15,-0.37 )*aspectcorrect) * dofblur*0.9);
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.15,0.37 )*aspectcorrect) * dofblur*0.9);
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.37,0.15 )*aspectcorrect) * dofblur*0.9);            
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.37,-0.15 )*aspectcorrect) * dofblur*0.9);   
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.15,-0.37 )*aspectcorrect) * dofblur*0.9);   
       
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.29,0.29 )*aspectcorrect) * dofblur*0.7);
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.4,0.0 )*aspectcorrect) * dofblur*0.7);       
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.29,-0.29 )*aspectcorrect) * dofblur*0.7);   
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.0,-0.4 )*aspectcorrect) * dofblur*0.7);     
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.29,0.29 )*aspectcorrect) * dofblur*0.7);
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.4,0.0 )*aspectcorrect) * dofblur*0.7);     
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.29,-0.29 )*aspectcorrect) * dofblur*0.7);   
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.0,0.4 )*aspectcorrect) * dofblur*0.7);
                         
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.29,0.29 )*aspectcorrect) * dofblur*0.4);
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.4,0.0 )*aspectcorrect) * dofblur*0.4);       
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.29,-0.29 )*aspectcorrect) * dofblur*0.4);   
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.0,-0.4 )*aspectcorrect) * dofblur*0.4);     
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.29,0.29 )*aspectcorrect) * dofblur*0.4);
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.4,0.0 )*aspectcorrect) * dofblur*0.4);     
        col += texture2D(colorImage, texCoord.xy + (vec2( -0.29,-0.29 )*aspectcorrect) * dofblur*0.4);   
        col += texture2D(colorImage, texCoord.xy + (vec2( 0.0,0.4 )*aspectcorrect) * dofblur*0.4);       
                       
        glFragColor = col/41.0;
        glFragColor.a = 1.0;
        
        //glFragColor =texture2D(colorImage,texCoord.xy);
        
}