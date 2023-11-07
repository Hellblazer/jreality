//author Benjamin Kutschan
//default polygon fragment shader with multisample texture support
#version 330

uniform sampler2DMS image;
uniform int width;
uniform int height;

void main(void)
{

	// since we multisample, we need to filter our selfs. So we first average, then run the code on the average
	int nrOfSamples = 4; // hardcode

	// average the depth
	int Sint = int(round(gl_FragCoord.s));///_width;
	int Tint = int(round(gl_FragCoord.t));///_height;
	ivec2 iTexCoord = ivec2(Sint, Tint);
	float d = 0.0f;
	for(int sampleIndex = 0; sampleIndex < nrOfSamples; sampleIndex++) {
		 d = d + texelFetch(image, iTexCoord , sampleIndex ).x;//sampleIndex).x;
	}
	d = d / nrOfSamples;
	
	
	if(d - gl_FragCoord.z < 0.000000001)
		discard;
	
	gl_FragDepth = 1-gl_FragCoord.z;
}