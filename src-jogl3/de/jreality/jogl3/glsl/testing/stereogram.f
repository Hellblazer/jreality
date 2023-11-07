// NOTE: Connection between de.jreality.plugin.stereogram.StereogramPlugin and 
//       de.jreality.jogl3.glsl.testing.stereogram.f via
//       de.jreality.jogl3.helper.TransparencyHelper and
//       de.jreality.shader.CommonAttributes

//author Benjamin Kutschan
//default polygon fragment shader
#version 150

out vec4 glFragColor;
smooth in vec4 texCoord;

uniform sampler2D image;
uniform sampler2D background;
uniform int slice;
uniform int numSlices;
uniform int seed;
uniform int seeed;
uniform float seeeed;
uniform int showEdgesInt;
uniform int randomDotInt;
uniform int depthFactorInt;
uniform int backgroundSpeedInt;
uniform float xZoom;
uniform float yZoom;
float depthFactor = (depthFactorInt/100.0);
float pi = 3.14159;
int numSlicesDoubled = numSlices * 2;

//uniform sampler2D image0;
//uniform sampler2D image1;
//uniform sampler2D image2;
//uniform sampler2D image3;

float hSF = 2.0/numSlices; // horizontal shift factor
float vSF = 1.0/4; // vertical shift factor
float thickness = 0.0010;
float transparency = 0.2;


vec4 filterEdges(int hS, int vS) {       			
	vec4 diff0 = texture(image, vec2(texCoord.s+hSF*hS, texCoord.t+vSF*vS)) - texture(image, vec2(texCoord.s+hSF*hS+thickness, texCoord.t+vSF*vS));
	vec4 diff1 = texture(image, vec2(texCoord.s+hSF*hS, texCoord.t+vSF*vS)) - texture(image, vec2(texCoord.s+hSF*hS-thickness, texCoord.t+vSF*vS));
	vec4 diff2 = texture(image, vec2(texCoord.s+hSF*hS, texCoord.t+vSF*vS)) - texture(image, vec2(texCoord.s+hSF*hS, texCoord.t+thickness+vSF*vS));
	vec4 diff3 = texture(image, vec2(texCoord.s+hSF*hS, texCoord.t+vSF*vS)) - texture(image, vec2(texCoord.s+hSF*hS, texCoord.t-thickness+vSF*vS));
				
	float d0 = diff0.x;
	float d1 = diff1.x;
	float d2 = diff2.x;
	float d3 = diff3.x;
				
	float total = d0+d1+d2+d3;
	float t = 1*total;
				        
	return vec4(t,t/2,t/4,transparency);
}



void main(void) {
	    
    bool showEdges = (showEdgesInt == 1);
    bool randomDot = (randomDotInt == 1);
           
	float displacement = 1 - texture(image, texCoord.st).r;
	displacement = depthFactor*(displacement+0.0)/numSlices;
	
	float leftBound = (slice + 1.0 ) / numSlices;
	float rightBound = (slice + 2.0) / numSlices;
	float scaleFactor = (numSlices+0.0)/2;
	
	vec4 edges = vec4(0,0,0,0);
	
	if(slice == 0) { 	
		if(texCoord.s < 2.0/numSlices) {
			// filter edges and copy several times into first slice for random lines
			if (showEdges == true) {
				for (int i=0; i<numSlices; i++) {
					for (int j=-2; j<4; j++) {        			
		    			edges = edges + filterEdges(i, j);
					}
				}
			}
			if (randomDot == true) {
				// paint random dots into first slice							   // speed				// chaos			// chaos		
				float randomness = fract(sin(dot(vec2(texCoord.s,texCoord.t+(seed/10000000.0)) ,vec2(102.9898,108.233))) * 1758.5453);	
				glFragColor = vec4(	randomness*sin(6*pi*texCoord.t*(seeed/100.0)),
									randomness*(0.3*(seeed/100.0)+0.4),
									randomness*cos(6*pi*texCoord.t*(1-seeed/100.0)), 
									1)
									+ edges;							
			} else {
				// scale background and paint first slice												 
				glFragColor=texture(background, vec2(texCoord.s*scaleFactor*xZoom+(0.5*sin(3*pi*(0.2*seeeed))+0.5)*(xZoom-1.0),
													 texCoord.t*yZoom*(-1)+(0.5*cos(5*pi*(0.2*seeeed))+0.5)*(yZoom*(-1)+1.0) )) // for some reason yZoom must be negativ, otherwise textures will be displayed upside down
				// add edges to first slice
									+ edges;
			}
		} else {
			// do nothing at right side to current slice
			discard;
		}			
	} else if (slice > 0 && slice < numSlices-1) {
		if (texCoord.s < leftBound) {
			// copy all from previous iteration
			glFragColor = texture(background, texCoord.st);
		} else if (texCoord.s >= leftBound && texCoord.s < rightBound) {
			// paint next slice from left neighboring slice with distortions
			glFragColor=texture(background,vec2(texCoord.s-hSF+displacement,texCoord.t));									
		} else {
			// do nothing at right side to current slice
			discard;
		}
	} else {
		// add edges of the 3D-objects
		if (showEdges == true) {
		    edges = filterEdges(0, 0); 
		}		        
		if (texCoord.s < leftBound) {
			// copy all from previous iteration
			glFragColor = texture(background, texCoord.st)
					// add edges
					+ edges;
		} else {
			// paint next slice from left neighboring slice with distortions
			glFragColor=texture(background,vec2(texCoord.s-hSF+displacement,texCoord.t))
					// add edges
					+ edges;
		}
	}
}
	