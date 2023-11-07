package de.jreality.jogl3.helper;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Random;

import com.jogamp.opengl.GL3;

import de.jreality.jogl.plugin.InfoOverlay;
import de.jreality.jogl3.InfoOverlayData;
import de.jreality.jogl3.JOGLSceneGraphComponentInstance.RenderableObject;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.optimization.RenderableUnitCollection;
import de.jreality.jogl3.shader.GLVBOFloat;
import de.jreality.jogl3.shader.LabelShader;
import de.jreality.jogl3.shader.Texture2DLoader;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.Texture2D;
import de.jreality.shader.TextureUtility;
import de.jreality.util.ImageUtility;
//import com.jogamp.opengl.util.awt.ImageUtil;

/**
 * Even though it is called "TransparencyHelper" all rendering goes through here.
 * We use multiple frabeBufferObjects and Textures to render the different layers of transparency.
 * These fbos are then copied to the screen in correct order.
 * 
 * 
 * EXPLAINING THE MAIN IDEA:::::
 * use two FBOs and bind them with textures. fbos[0] has tex[0] of depth. fbos[1] has tex[1 & 2] for depth and color.
 * peelDepth renders the depths of transp objects onto ... 
 * 
 * 
 * 
 * may2015 problem: anti aliasing is lost as soon as we render onto a TEXTURE_2D. Try Solution: render to TEXTURE_2D_MULTISAMPLE
 * Notes, http://stackoverflow.com/questions/14019910/how-does-glteximage2dmultisample-work:
 * By default, the multisampling set in the jogl3 constructor only applies to the default framebuffer 0. for multisampling on
 * our other buffers/textures we  need multisample textures:
 * glTexImage2DMultisample(GL_TEXTURE_2D_MULTISAMPLE, 4, GL_RGBA, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, GL_TRUE); and attach it to the fbo.
 * Every texture has to be multisample in order to carry all multisampling until the end. The very last texture only being copied to FB zero can be non MS.
 * Then comes the magic: the actual rendering process works like this:
 * You bind your FBO with MS texture attachment.Render your geometry.Then you RESOLVE the multisampled texture or into a regular texture or directly into the default backbuffer by blitting:
 * glBlitFramebuffer( //copy a block of pixels from the read framebuffer to the draw framebuffer
 *   0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, 
 *   0, 0, FRAMEBUFFER_SIZE.x, FRAMEBUFFER_SIZE.y, 
 *   GL_COLOR_BUFFER_BIT, GL_NEAREST);
 * 
 * june15: multisample texture work, but for some reason the transparency goes missing, similarly in non multisampling if the peeldepth is skipped.
 * 
 * comment: padilla
 *
 */
public class TransparencyHelper {

	/**
	 * contains: supersample. is the factor by which texture images are made much bigger to then be scaled down for Anti aliasing
	 * @param ss
	 */
	public static void setSupersample(int ss){
		TransparencyHelper.supersample = ss;
	}
	public static int getSupersample(){
		return TransparencyHelper.supersample;
	}
	private static int supersample = 2;

	//DONT FORGET TO INITIALIZE SHADERS WITH .init(GL3 gl)
	public static GLShader depth = new GLShader("transp/polygonDepth.v", "transp/depth.f"); // very basic, 
	public static GLShader transp = new GLShader("nontransp/polygon.v", "transp/polygonTransp.f");
	public static GLShader transpSphere = new GLShader("nontransp/sphere.v", "transp/sphereTransp.f");
	public static GLShader copy = new GLShader("testing/copy.v", "testing/copy.f");
	public static GLShader stereogramShader = new GLShader("testing/stereogram.v", "testing/stereogram.f");
	// april15 padilla: fxaa implementation
	public static GLShader fxaaShader = new GLShader("postprocessing/copy.v", "postprocessing/fxaa.f");
	// may15 padilla: depth of field implementation
	public static GLShader dofShader = new GLShader("postprocessing/copy.v", "postprocessing/dof.f");
	//july15 padilla: black and white filter
	public static GLShader bwShader = new GLShader("postprocessing/copy.v", "postprocessing/blackWhite.f");
	//june15 padilla: depth and transparency shaders for multisample texture support
	public static GLShader depthMS = new GLShader("transp/polygonDepth.v", "transp/depthMS.f"); // very basic, 
	public static GLShader transpMS = new GLShader("nontransp/polygon.v", "transp/polygonTranspMS.f");
	//	public static GLShader depthMS = new GLShader("transp/polygonDepth.v", "transp/depth.f"); // very basic, 
	//	public static GLShader transpMS = new GLShader("nontransp/polygon.v", "transp/polygonTransp.f");


	// these Coords are important for rendering onto a texture and coping that to the framebuffer
	static float testQuadCoords[] = {
		-1f, 1f, 0.1f, 1,
		1f, 1f, 0.1f, 1,
		1f, -1f, 0.1f, 1,
		1f, -1f, 0.1f, 1,
		-1f, -1f, 0.1f, 1,
		-1f, 1f, 0.1f, 1
	};

	static float testTexCoords[] = {
		0,1,0,0,
		1,1,0,0,
		1,0,0,0,
		1,0,0,0,
		0,0,0,0,
		0,1,0,0
	};
	public static GLVBOFloat copyCoords, copyTex;
	private static int[] queries = new int[1];

	//forth and fifth texture only for stereogram rendering, they are linked with the fbos
	private static int[] texs = new int[6];

	//third fbo only for stereogram rendering
	private static int[] fbos = new int[3];

	// may15 padilla: general post processing implementation==================
	static boolean postProcessingEnabled = true;
	//more general:
	static PostProcessingHelper postProcessor = null;
	// render everything to textures and then to more textures using a number of pp shaders
	// one more texture & buffer for screen rendering, use this array to call glGenTextures and not collide with other IDs
	private static int[] ppRenderTexture;
	private static int[] ppRenderTextureFrameBuffer;

	// ============== april15 padilla: fxaa post proccessing implementation =====
//	// IDEA: render everything to a texture, and then run fxaa on that texture.
//	static boolean fxaaEnabled = true; // hardcoded for testing
//	static int nrOfFxaaIterations=16;
	//=============
	// take a depth texture and blur out what is far away.
	static boolean depthOfFieldEnabled = false;

	// multisampling the textures attempt
	public static boolean enableMultisampleTextures =false;
	private static int glTextureMode = 0;
	static int multiSampleFactor=4;
	// need Render Frame Buffers, they actually multisample without multisample textures. But still no depth
	private static int[] renderFbos = new int[texs.length];
	// introduce more frame buffer to resolve the multisample transparencies depths to textures
	private static int[] fbosToResolve = new int[fbos.length];
	private static int[] fbosToResolveTex = new int[2*fbos.length];
	private static boolean additionalResolve = false;
	static boolean useRenderBuffers =false;
	//july15 when all trancparencies are disabled, only the default fbo is used. That one has multisampled anti aliasing
	/* REPORT:
	 * June15: atm the multi sample texture does not MSAA even if there are no transparent objects.
	 * And the normal textures do MSAA but looses it during the proccess.
	 */

	//Dec2015 add backfaceculling
	private static boolean enableBackFaceCulling=false;
	/**
	 * Called upon at JOGL3Viewer render to set the value here
	 * @param bool
	 */
	public static void setBackFaceCulling(boolean bool){TransparencyHelper.enableBackFaceCulling = bool;	}

	//================//================//================//================

	private static int[] queryresavail = new int[1];
	private static int[] queryres = new int[1];

	public static void resizeFramebufferTextures(GL3 gl, int width, int height){

		// control if normal TEXS or msaa TEXS here
		glTextureMode = gl.GL_TEXTURE_2D;
		if(enableMultisampleTextures){
			// switch to multisample Textures
			glTextureMode = gl.GL_TEXTURE_2D_MULTISAMPLE;
		}
		//		glTextureMode = gl.GL_TEXTURE_2D_MULTISAMPLE;

		// control the filtering mode
		int glFilteringMode = gl.GL_NEAREST;


		//CREATING TEXTURES

		// need three more textures to resolve the multisampling at each step of the transparency drawing
		// frame buffer to resolve color on fbosToResolve[2]
		gl.glBindTexture(glTextureMode, fbosToResolveTex[2]);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MAG_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MIN_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE) 
			gl.glTexImage2DMultisample(glTextureMode, multiSampleFactor, gl.GL_RGBA8, width, height, false);
		else gl.glTexImage2D(glTextureMode, 0, gl.GL_RGBA8, width, height, 0, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE, null);
		// frame buffer to resolve depth
		gl.glBindTexture(glTextureMode, fbosToResolveTex[1]);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MAG_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MIN_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE) 
			gl.glTexImage2DMultisample(gl.GL_TEXTURE_2D_MULTISAMPLE, multiSampleFactor, gl.GL_DEPTH_COMPONENT, supersample*width, supersample*height, false);
		else gl.glTexImage2D(glTextureMode, 0, gl.GL_DEPTH_COMPONENT32, supersample*width, supersample*height, 0, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, null);
		// frame buffer to resolve depth on fbosToResolve[0]
		gl.glBindTexture(glTextureMode, fbosToResolveTex[0]);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MAG_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MIN_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE) 
			gl.glTexImage2DMultisample(gl.GL_TEXTURE_2D_MULTISAMPLE, multiSampleFactor, gl.GL_DEPTH_COMPONENT, supersample*width, supersample*height, false);
		else gl.glTexImage2D(glTextureMode, 0, gl.GL_DEPTH_COMPONENT32, supersample*width, supersample*height, 0, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, null);


		gl.glBindTexture(glTextureMode, texs[5]);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MAG_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MIN_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE) 
			gl.glTexImage2DMultisample(glTextureMode, multiSampleFactor, gl.GL_RGBA8, width, height, false);
		else gl.glTexImage2D(glTextureMode, 0, gl.GL_RGBA8, width, height, 0, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE, null);
		//prepare color texture 4 to framebuffer object 2
		gl.glBindTexture(glTextureMode, texs[4]);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MAG_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MIN_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE) 
			gl.glTexImage2DMultisample(glTextureMode, multiSampleFactor, gl.GL_RGBA8, width, height, false);
		else gl.glTexImage2D(glTextureMode, 0, gl.GL_RGBA8, width, height, 0, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE, null);
		//		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE) 
		//			gl.glTexImage2DMultisample(gl.GL_TEXTURE_2D_MULTISAMPLE, multiSampleFactor, gl.GL_DEPTH_COMPONENT, supersample*width, supersample*height, false);
		//		else gl.glTexImage2D(glTextureMode, 0, gl.GL_DEPTH_COMPONENT32, supersample*width, supersample*height, 0, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, null);
		//prepare color texture 4 to framebuffer object 2
		gl.glBindTexture(glTextureMode, texs[3]);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MAG_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MIN_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE) 
			gl.glTexImage2DMultisample(glTextureMode, multiSampleFactor, gl.GL_RGBA8, width, height, false);
		else gl.glTexImage2D(glTextureMode, 0, gl.GL_RGBA8, width, height, 0, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE, null);
		//		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE) 
		//			gl.glTexImage2DMultisample(gl.GL_TEXTURE_2D_MULTISAMPLE, multiSampleFactor, gl.GL_DEPTH_COMPONENT, supersample*width, supersample*height, false);
		//		else gl.glTexImage2D(glTextureMode, 0, gl.GL_DEPTH_COMPONENT32, supersample*width, supersample*height, 0, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, null);
		//bind color texture to framebuffer object 1
		gl.glBindTexture(glTextureMode, texs[2]);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MAG_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MIN_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE)
			gl.glTexImage2DMultisample(glTextureMode, multiSampleFactor, gl.GL_RGBA8, supersample*width, supersample*height, false);
		else gl.glTexImage2D(glTextureMode, 0, gl.GL_RGBA8, supersample*width, supersample*height, 0, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE, null);
		//bind depth texture to framebuffer object 1
		gl.glBindTexture(glTextureMode, texs[1]);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MAG_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MIN_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE) 
			gl.glTexImage2DMultisample(gl.GL_TEXTURE_2D_MULTISAMPLE, multiSampleFactor, gl.GL_DEPTH_COMPONENT, supersample*width, supersample*height, false);
		else gl.glTexImage2D(glTextureMode, 0, gl.GL_DEPTH_COMPONENT32, supersample*width, supersample*height, 0, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, null);
		//bind depth texture to framebuffer object 0
		gl.glBindTexture(glTextureMode, texs[0]);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MAG_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_MIN_FILTER, glFilteringMode);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(glTextureMode, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		if(glTextureMode == gl.GL_TEXTURE_2D_MULTISAMPLE) 
			gl.glTexImage2DMultisample(gl.GL_TEXTURE_2D_MULTISAMPLE, multiSampleFactor, gl.GL_DEPTH_COMPONENT, supersample*width, supersample*height, false);
		else gl.glTexImage2D(glTextureMode, 0, gl.GL_DEPTH_COMPONENT32, supersample*width, supersample*height, 0, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, null);




		// ATTACHING

		//attach color to the framebuffer object 1
		gl.glBindTexture(glTextureMode, texs[2]);
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
		gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, glTextureMode, texs[2], 0);
		checkFramebuffer(gl);

		//attach depth to the framebuffer object 1
		gl.glBindTexture(glTextureMode, texs[1]);
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
		gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, glTextureMode, texs[1], 0);
		checkFramebuffer(gl);

		//attach depth texture to framebuffer object 0
		gl.glBindTexture(glTextureMode, texs[0]);
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[0]);
		gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, glTextureMode, texs[0], 0);
		checkFramebuffer(gl);

		if(additionalResolve){
			//attach color texture to fbosTo resolve
			gl.glBindTexture(glTextureMode, fbosToResolveTex[2]);
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbosToResolve[1]);
			gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, glTextureMode, fbosToResolveTex[2], 0);		//attach depth texture to framebuffer object 0
			checkFramebuffer(gl);
			gl.glBindTexture(glTextureMode, fbosToResolveTex[1]);
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbosToResolve[1]);
			gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, glTextureMode, fbosToResolveTex[1], 0);		//attach depth texture to framebuffer object 0
			checkFramebuffer(gl);
			gl.glBindTexture(glTextureMode, fbosToResolveTex[0]);
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbosToResolve[0]);
			gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, glTextureMode, fbosToResolveTex[0], 0);
			checkFramebuffer(gl);
		}

		if(useRenderBuffers){
			// NEW: ATTACH RENDER BUFFERS 
			//for some reason we got to attach a RENDERBUFFER to the fbos in order to have Depth and Color on MSAATextures.
			// Multi sample colorbuffer for texs[2]
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbosToResolve[1]);
			gl.glBindRenderbuffer(gl.GL_RENDERBUFFER, renderFbos[2]);
			gl.glRenderbufferStorageMultisample(gl.GL_RENDERBUFFER, multiSampleFactor, gl.GL_RGBA8, supersample*width, supersample*height);
			gl.glFramebufferRenderbuffer(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_RENDERBUFFER, renderFbos[2]);

			// Multi sample depth buffer for texs[1]
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbosToResolve[1]);
			gl.glBindRenderbuffer(gl.GL_RENDERBUFFER, renderFbos[1]);
			gl.glRenderbufferStorageMultisample(gl.GL_RENDERBUFFER, multiSampleFactor, gl.GL_DEPTH_COMPONENT32, supersample*width, supersample*height);
			gl.glFramebufferRenderbuffer(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, gl.GL_RENDERBUFFER, renderFbos[1]);

			// Multi sample depth buffer for texs[0]
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbosToResolve[0]);
			gl.glBindRenderbuffer(gl.GL_RENDERBUFFER, renderFbos[0]);
			gl.glRenderbufferStorageMultisample(gl.GL_RENDERBUFFER, multiSampleFactor, gl.GL_DEPTH_COMPONENT32, supersample*width, supersample*height);
			gl.glFramebufferRenderbuffer(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, gl.GL_RENDERBUFFER, renderFbos[0]);
		}



		// april15 padilla:
		// also prepare renderTexture and renderTextureFrameBuffer. turns out that even when multisampling, this rendertexture should be a normaltexture

		// texture preparation for color // since normally tex[2] is being copied to the buffer we try to mimic tex[2] in the construction
		gl.glBindTexture(gl.GL_TEXTURE_2D, ppRenderTexture[0]);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_RGBA8, width, height, 0, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE, null);

		// texture preparation for depth // do it like with tex[1]
		gl.glBindTexture(gl.GL_TEXTURE_2D, ppRenderTexture[1]);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, gl.GL_NEAREST);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, gl.GL_NEAREST);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_DEPTH_COMPONENT, width, height, 0, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, null);

		// binding to framebuffer
		//NEW TRY make post processing happen on the very last layer, by linking a texture to the defualt frame buffer|| RESULT: forbidden with fbo 0
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, ppRenderTextureFrameBuffer[0]);
		gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_TEXTURE_2D, ppRenderTexture[0], 0);
		gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, gl.GL_TEXTURE_2D, ppRenderTexture[1], 0);





		// DEBUG

		//		 checking these numbers solved a problem with the texture initialisations
		//		System.out.print("fbos[] = " );
		//		for(int i = 0 ; i<fbos.length ; i++) System.out.print( i + " ~ " + fbos[i] + " || " );
		//		System.out.println("");
		//		System.out.print("fbosToResolve= ");
		//		for(int i = 0 ; i<fbosToResolve.length ; i++) System.out.print( i + " ~ " + fbosToResolve[i] + " || " );
		//		System.out.println("");
		//		System.out.print("renderFBO[] = " );
		//		for(int i = 0 ; i< ppRenderTextureFrameBuffer.length ; i++) System.out.print( i + " ~ " + ppRenderTextureFrameBuffer[i] + " || " );
		//		System.out.println("");
		//		System.out.print("texs = " );
		//		for(int i = 0 ; i<texs.length ; i++) System.out.print( i + " ~ " + texs[i] + " || " );
		//		System.out.println("");
		//		System.out.print("rendertexs = ");
		//		for(int i = 0 ; i<ppRenderTexture.length ; i++) System.out.print( i + " ~ " + ppRenderTexture[i] + " || " );
		//		System.out.println("");
		//		System.out.println("check FBO complete:  " + (gl.GL_FRAMEBUFFER_COMPLETE == gl.glCheckFramebufferStatus(gl.GL_FRAMEBUFFER)));
		//		System.out.println("supersample size = " + supersample);
		// finish with fb 0 just to be sure.
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
		//debug test
		if(gl.glCheckFramebufferStatus(gl.GL_FRAMEBUFFER) != gl.GL_FRAMEBUFFER_COMPLETE){
			System.err.println("FRAMEBUFFER INCOMPLETE");
			//int[] crash = new int[1]; crash[1]=0;
		}


	}
	private static void initTextureFramebuffer(GL3 gl, int width, int height){
		// generate multiple frame buffers at once
		gl.glGenTextures(texs.length, texs, 0);
		gl.glGenFramebuffers(fbos.length, fbos, 0);

		// april15 padilla:
		// create another tex/frame to render on (color and depth) 
		ppRenderTexture = new int[2];
		ppRenderTextureFrameBuffer = new int[1];
		gl.glGenTextures(ppRenderTexture.length, ppRenderTexture, 0);
		gl.glGenFramebuffers(ppRenderTextureFrameBuffer.length, ppRenderTextureFrameBuffer, 0);
		// create render frame buffers to attach to the fbos[] to get depth and color multismapling. One for every texture
		gl.glGenRenderbuffers(renderFbos.length, renderFbos, 0);
		gl.glGenFramebuffers(fbosToResolve.length, fbosToResolve, 0);
		gl.glGenFramebuffers(fbosToResolveTex.length, fbosToResolveTex, 0);


		// resize also serve well for initialization
		resizeFramebufferTextures(gl, width, height);
	}

	public static void initTransparency(GL3 gl, int width, int height){
		depth.init(gl);
		transp.init(gl);
		transpSphere.init(gl);
		copy.init(gl);
		stereogramShader.init(gl);

		//		fxaaShader.init(gl); //april15 padilla
		//		dofShader.init(gl); //may15 padilla




		if(enableMultisampleTextures){ //june15 padilla
			depthMS.init(gl);
			transpMS.init(gl);
		}

		copyCoords = new GLVBOFloat(gl, testQuadCoords, "vertex_coordinates");
		copyTex = new GLVBOFloat(gl, testTexCoords, "texture_coordinates");
		gl.glGenQueries(1, queries, 0);
		gl.glEnable(gl.GL_DEPTH_TEST);
		initTextureFramebuffer(gl, width, height);
	}





	private static void startQuery(GL3 gl){
		gl.glBeginQuery(gl.GL_SAMPLES_PASSED, queries[0]);
	}
	private static int endQuery(GL3 gl){
		gl.glEndQuery(gl.GL_SAMPLES_PASSED);
		int counter = 0;
		do{
			counter++;
			gl.glGetQueryObjectuiv(queries[0],gl.GL_QUERY_RESULT_AVAILABLE, queryresavail, 0);
			//System.out.println("not true yet: " + counter);
			if(counter == 1000000000)
				System.out.println("reached max no of iterations in query loop");
		}while(queryresavail[0] != gl.GL_TRUE && counter < 1000000000);
		if(queryresavail[0] == gl.GL_TRUE){
			gl.glGetQueryObjectuiv(queries[0] ,gl.GL_QUERY_RESULT, queryres, 0);
			//    		System.out.println("query result after " + counter + " waits is " + queryres[0]);
			return queryres[0];
			//System.out.println("Query result is " + queryres[0]);
		}else{
			System.err.println("Oups, we waited for the query result for longer than a billion iterations! Returning 0, to make partial render possible");
			return 0;
		}
	}

	private static Texture2D stereogramTexture;
	private static boolean stereogram = false;
	private static int numSlices = 2;
	private static int showEdgesInt = 1;
	private static int randomDotInt = 1;
	private static int depthFactorInt = 200;
	private static int backgroundSpeedInt = 0;
	private static float xZoom = (float) 1.0;
	private static float yZoom = (float) 1.0;
	public static void setUpStereogramTexture(GL3 gl, Appearance rootAp){
		System.out.println("set up stereogram texture");

		Object obj = TextureUtility.getBackgroundTexture(rootAp);
		stereogramTexture = null;
		if (obj != null) {
			stereogramTexture = (Texture2D) obj;

			stereogram = true;
			Object bgo;
			bgo = rootAp.getAttribute(CommonAttributes.STEREOGRAM_NUM_SLICES);
			System.out.println("bgo type = " + bgo.getClass());
			if(bgo == null || !bgo.getClass().equals(Integer.class)){
				bgo = CommonAttributes.STEREOGRAM_NUM_SLICES_DEFAULT;
			}
			numSlices = (Integer) bgo;
			System.out.println("num slices = " + numSlices);



			// Andre
			Object bgo2;
			bgo2 = rootAp.getAttribute(CommonAttributes.STEREOGRAM_SHOWEDGES);
			if (bgo2 == null) {
				showEdgesInt = 0;
			} else if ((Boolean) rootAp.getAttribute(CommonAttributes.STEREOGRAM_SHOWEDGES)) {
				showEdgesInt = 1;
			} else {
				showEdgesInt = 0;
			}

			Object bgo3;
			bgo3 = rootAp.getAttribute(CommonAttributes.STEREOGRAM_RANDOMDOT);
			if (bgo3 == null) {
				randomDotInt = 0;
			} else if ((Boolean) rootAp.getAttribute(CommonAttributes.STEREOGRAM_RANDOMDOT)) {
				randomDotInt = 1;
			} else {
				randomDotInt = 0;
			}

			Object bgo4;
			bgo4 = rootAp.getAttribute(CommonAttributes.STEREOGRAM_DEPTHFACTOR);
//			System.out.println("bgo type = " + bgo4.getClass());
			if(bgo4 == null || !bgo4.getClass().equals(Integer.class)){
				bgo4 = CommonAttributes.STEREOGRAM_DEPTHFACTOR_DEFAULT;
			}
			depthFactorInt = (Integer) bgo4;
//			System.out.println("depthFactor = " + depthFactorInt);

			Object bgo5;
			bgo5 = rootAp.getAttribute(CommonAttributes.STEREOGRAM_BACKGROUNDSPEED);
//			System.out.println("bgo type = " + bgo5.getClass());
			if(bgo5 == null || !bgo5.getClass().equals(Integer.class)){
				bgo5 = CommonAttributes.STEREOGRAM_BACKGROUNDSPEED_DEFAULT;
			}
			backgroundSpeedInt = (Integer) bgo5;
//			System.out.println("backgroundSpeed = " + backgroundSpeedInt);

			Object bgo6;
			bgo6 = rootAp.getAttribute(CommonAttributes.STEREOGRAM_XZOOM);
//			System.out.println("bgo type = " + bgo6.getClass());
			if(bgo6 == null || !bgo6.getClass().equals(Float.class)){
				bgo6 = CommonAttributes.STEREOGRAM_XZOOM_DEFAULT;
			}
			xZoom = (Float) bgo6;
//			System.out.println("xZoom = " + xZoom);

			Object bgo7;
			bgo7 = rootAp.getAttribute(CommonAttributes.STEREOGRAM_YZOOM);
//			System.out.println("bgo type = " + bgo7.getClass());
			if(bgo7 == null || !bgo7.getClass().equals(Float.class)){
				bgo7 = CommonAttributes.STEREOGRAM_YZOOM_DEFAULT;
			}
			yZoom = (Float) bgo7;
//			System.out.println("yZoom = " + yZoom);



		}else{
			stereogram = false;
		}
	}
	public static void noStereogramRender(){
//		System.out.println("no stereogram render");
		stereogramTexture = null;
		stereogram = false;
		numSlices = 2;
	}

	private static void renderStereogram(InfoOverlayData infoData, GL3 gl, RenderableUnitCollection ruc, List<RenderableObject> transp, int width, int height, BackgroundHelper backgroundHelper){
		//		System.out.println("render stereogram");
		//TODO render scene to depth texture

		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
		gl.glViewport(0, 0, supersample*width, supersample*height);  

		gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		gl.glClear(gl.GL_COLOR_BUFFER_BIT);
		//draw background here
		backgroundHelper.draw(gl);
		SkyboxHelper.render(gl);
		//draw nontransparent objects into framebuffer
		gl.glEnable(gl.GL_DEPTH_TEST);
		gl.glClearDepth(1);
		gl.glClear(gl.GL_DEPTH_BUFFER_BIT);

		//TODO is this correct??
		gl.glDisable(gl.GL_BLEND);
		ruc.render(gl, width, height);


		if(transp.size() != 0){
			for(RenderableObject o : transp){
				o.render(width, height);
			}

			//TODO TODO TODO
			//if transp.size == 0 then don't do anything of this!
			//Except for drawing labels nicely on top of each other with correct AA.

			int quer = 1;
			//with this loop it draws as many layers as neccessary to complete the scene
			//you can experiment by drawing only one layer or two and then view the result (see the comment after the loop)

			//draw transparent objects into FBO with reverse depth values
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[0]);
			startQuery(gl);
			peelDepth(gl, transp, supersample*width, supersample*height);
			quer = endQuery(gl);

			int counter = 0;
			while(quer!=0 && counter < 20){
				counter++;
				//draw on the SCREEN
				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
				addOneLayer(gl, transp, supersample*width, supersample*height);
				//draw transparent objects into FBO with reverse depth values
				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[0]);
				startQuery(gl);
				peelDepth(gl, transp, supersample*width, supersample*height);
				quer = endQuery(gl);
			}
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
			gl.glDisable(gl.GL_DEPTH_TEST);
		}


		//TODO set up stereogram texture





		//TODO change copyFBO2FB to use stereogram shader from rootAp

		//you can change the number here:
		//0 means the current depth layer generated by peelDepth()
		//1 the depth layer of the final image generated by addOneLayer()
		//2 the color layer of the final image generated by addOneLayer()
		copySTEREOGRAM2FB(gl, 1, width, height);
	}


	private static Random rand = new Random();
	private static int seed = 0;
	private static int seeed = 0;
	private static float seeeed = 0;
	static long startTime = System.currentTimeMillis();	
	static long checkTime = System.currentTimeMillis();

	private static void copySTEREOGRAM2FB(GL3 gl, int tex, int width, int height){

		gl.glDisable(gl.GL_BLEND);
		gl.glViewport(0, 0, width, height);

		gl.glDisable(gl.GL_DEPTH_TEST);


		stereogramShader.useShader(gl);

		gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "image"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "background"), 1);


		gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "numSlices"), numSlices);

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyCoords.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(stereogramShader.shaderprogram, copyCoords.getName()), copyCoords.getElementSize(), copyCoords.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(stereogramShader.shaderprogram, copyCoords.getName()));

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyTex.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(stereogramShader.shaderprogram, copyTex.getName()), copyTex.getElementSize(), copyTex.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(stereogramShader.shaderprogram, copyTex.getName()));


		//bind depth texture
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE0);
		gl.glBindTexture(gl.GL_TEXTURE_2D, texs[tex]);



		stereogramShader.useShader(gl);

		gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "image"), 0);
		gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "background"), 1);
		gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "color"), 2);
		gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "numSlices"), numSlices);
		//    	gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "seed"), seed);    	



		// Andre
		gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "showEdgesInt"), showEdgesInt);
		gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "randomDotInt"), randomDotInt);
		gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "depthFactorInt"), depthFactorInt);
		gl.glUniform1f(gl.glGetUniformLocation(stereogramShader.shaderprogram, "xZoom"), xZoom);
		gl.glUniform1f(gl.glGetUniformLocation(stereogramShader.shaderprogram, "yZoom"), yZoom);


		if (System.currentTimeMillis() > checkTime + 50) {
			checkTime = System.currentTimeMillis();
			double seconds = (System.currentTimeMillis() - startTime)/1000.0;
			//    		double seconds = Math.PI/2.0;
			seed = (int) (seconds*100);
			//seed = rand.nextInt(100);
			gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "seed"), seed);
			seeed = (int) (50*Math.sin(seconds*(backgroundSpeedInt/100.0))+50);
			//seeed = (int) (50*Math.tan(seconds/1.0)+50); // is lustig :P
			//seeed = (int) (30*Math.sin(2*seconds/1.0)+40*Math.pow(Math.cos(2*seconds/Math.PI),2)+30);
			gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "seeed"), seeed);
			//seeeed = (int) (5000*Math.sin(seconds/3.0)+5000);
			//seeeed = (int) (seconds*500);
			//gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "seeeed"), seeeed);
			seeeed = (float) (seconds*(backgroundSpeedInt/100.0));
			gl.glUniform1f(gl.glGetUniformLocation(stereogramShader.shaderprogram, "seeeed"), (float) seeeed);
			//    		gl.glUniform1f(gl.glGetUniformLocation(stereogramShader.shaderprogram, "seconds"), 0);
			//System.out.println("Time in seconds: " + seconds);
		}	




		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyCoords.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(stereogramShader.shaderprogram, copyCoords.getName()), copyCoords.getElementSize(), copyCoords.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(stereogramShader.shaderprogram, copyCoords.getName()));

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyTex.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(stereogramShader.shaderprogram, copyTex.getName()), copyTex.getElementSize(), copyTex.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(stereogramShader.shaderprogram, copyTex.getName()));


		//bind depth texture
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE0);
		gl.glBindTexture(gl.GL_TEXTURE_2D, texs[tex]);

		//bind color texture
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE2);
		gl.glBindTexture(gl.GL_TEXTURE_2D, texs[2]);

		//		PROTOKOLL GUNN
		// 		P_2.pointFromLines
		//    	P_2.lineFromPoints
		//  	inneresProdukt ist negativ wenn schnittpunkt zwischen 

		//		Textur einladen - discretegroupCGG.jar, TriangleGroupDemo ...einladen getInspector()
		//		ShioNaviagationTool fuer avatar


		for(int slice = 0; slice < numSlices; slice++){
			//			System.out.println("slice = " + slice);
			if(slice == 0){
				Texture2DLoader.load(gl, stereogramTexture, gl.GL_TEXTURE1);
				//TODO bind tex[3] to fbo[2] and enable fbo[2]
				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[2]);
				gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_TEXTURE_2D, texs[3], 0);
			}else if(slice == numSlices-1){
				//TODO bind tex[4-slice%2] to TEXTURE1
				gl.glActiveTexture(gl.GL_TEXTURE1);
				gl.glBindTexture(gl.GL_TEXTURE_2D, texs[4-slice%2]);
				//TODO bind standard FB
				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
			}else{
				//TODO bind tex[4-slice%2] to TEXTURE1
				gl.glActiveTexture(gl.GL_TEXTURE1);
				gl.glBindTexture(gl.GL_TEXTURE_2D, texs[4-slice%2]);
				//TODO bind tex[4-(slice+1)%2] to fbo[2]
				//attach color texture with the framebuffer object
				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[2]);
				gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_TEXTURE_2D, texs[4-(slice+1)%2], 0);
			}


			gl.glUniform1i(gl.glGetUniformLocation(stereogramShader.shaderprogram, "slice"), slice);
			gl.glDrawArrays(gl.GL_TRIANGLES, 0, copyCoords.getLength()/4);

		}
		gl.glDisableVertexAttribArray(gl.glGetAttribLocation(stereogramShader.shaderprogram, copyCoords.getName()));
		gl.glDisableVertexAttribArray(gl.glGetAttribLocation(stereogramShader.shaderprogram, copyTex.getName()));

		stereogramShader.dontUseShader(gl);
	}






	/**
	 * The most important method of all.
	 * 
	 * @param infoData
	 * @param gl
	 * @param ruc
	 * @param transp
	 * @param width
	 * @param height
	 * @param backgroundHelper
	 */
	public static void render(InfoOverlayData infoData, GL3 gl, RenderableUnitCollection ruc, List<RenderableObject> transp, int width, int height, BackgroundHelper backgroundHelper){
		if(stereogram){
			renderStereogram(infoData, gl, ruc, transp, width, height, backgroundHelper);
			return;
		}
		
		//		System.out.println("gl version: " + gl);

		//	for(int i=0; i < fbos.length ; i++) System.out.println(fbos[i]); // names 1, 2 ,3. not 0
		// do normal render call

		// if transparent objects present, draw on fbos[1], else on 0
		if(transp.size() != 0){ // if there exist transparent things
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
			gl.glClearColor(0.55f, 0.95f, 0.15f, 1.0f);
			gl.glClear(gl.GL_COLOR_BUFFER_BIT);
			gl.glClearDepth(1);
			gl.glClear(gl.GL_DEPTH_BUFFER_BIT);			

			if(additionalResolve) {
				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbosToResolve[0]);
				gl.glClearColor(0.55f, 0.95f, 0.15f, 1.0f);
				gl.glClear(gl.GL_COLOR_BUFFER_BIT);
				gl.glClearDepth(1);
				gl.glClear(gl.GL_DEPTH_BUFFER_BIT);
				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbosToResolve[1]);
				gl.glClearColor(0.55f, 0.95f, 0.15f, 1.0f);
				gl.glClear(gl.GL_COLOR_BUFFER_BIT);
				gl.glClearDepth(1);
				gl.glClear(gl.GL_DEPTH_BUFFER_BIT);	
			}


			gl.glViewport(0, 0, supersample*width, supersample*height); // this is where the resolution is increased to fight aliasing

		}else{
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
			gl.glViewport(0, 0, width, height);
		}
		//		if(enableMultisampleTextures) gl.glBindTexture(gl.GL_TEXTURE_2D_MULTISAMPLE, texs[2]);
		gl.glClearColor(0.55f, 0.95f, 0.15f, 1.0f); // useful to see errors
		gl.glClear(gl.GL_COLOR_BUFFER_BIT);

		//		if(transp.size()!=0) testCurrentFrameBuffer(gl, fbos[1], ppRenderTextureFrameBuffer[0], width, height, true);

		// 0 performed an fbos[1] check here. non-multi always has MSAA while multitex NEVER has. !?!?!??!
		//		if(transp.size()!=0) testCurrentFrameBuffer(gl, fbos[1], ppRenderTextureFrameBuffer[0], width, height, true);

		// prepare for multisample case
		if(enableMultisampleTextures) {
			gl.glEnable(gl.GL_MULTISAMPLE);
			gl.glEnable(gl.GL_TEXTURE_2D_MULTISAMPLE);
		}


		//draw background here
		backgroundHelper.draw(gl);
		SkyboxHelper.render(gl); // this is where we finally render the skybox. Q: why is it not an object?
		//		if(enableMultisampleTextures) gl.glDisable(gl.GL_MULTISAMPLE);

		// 1 performed an fbos[1] check here. multi, not multi. result: non multi had MSAA while multi had not !?. Skybox was locked up side down
		//		if(transp.size()!=0) testCurrentFrameBuffer(gl, fbos[1], ppRenderTextureFrameBuffer[0], width, height, true); 

		//draw nontransparent objects into framebuffer
		gl.glEnable(gl.GL_DEPTH_TEST);
		gl.glClearDepth(1);
		gl.glClear(gl.GL_DEPTH_BUFFER_BIT);
		
		//CULL HERE
		if(enableBackFaceCulling){
		gl.glEnable(gl.GL_CULL_FACE);
		gl.glCullFace(gl.GL_BACK);
		}

		//TODO is this correct??
		gl.glDisable(gl.GL_BLEND);

		// draw everything that is not transparent
		ruc.render(gl, width, height); // april 2015 with basic VR demo it does nothing here, only if transparency deactivated

		// 2 performed an fbos[1] check here. multi, not multi. result: on both, extremly akward stripe patterns appear. crazy effects when toggeling transparencies.
		// further: only now  does non multi loose MSAA
		//		if(transp.size()!=0) testCurrentFrameBuffer(gl, fbos[1], ppRenderTextureFrameBuffer[0], width, height, true);

		// handle transparent things separately
		if(transp.size() != 0){

			//			if(enableMultisampleTextures) gl.glEnable(gl.GL_MULTISAMPLE);
			for(RenderableObject o : transp){
				//	if(o.geom instanceof JOGLFaceSetInstance) System.out.println("FaceSetInstance rendercall()"); JViewerVR
				//	transp objects in this list with transparenceEnabled=true will be called for rendering here but do nothing
				// If we have an object with transparent faces, the lines and points might still be rendered here
				// this is on fbos[1]
				o.render(width, height);

			}
			//			if(enableMultisampleTextures) gl.glDisable(gl.GL_MULTISAMPLE);

			//3  performed an fbos[1] check here. multi, not multi. result: no MSAA on any
			//			if(transp.size()!=0) testCurrentFrameBuffer(gl, fbos[1], ppRenderTextureFrameBuffer[0], width, height, true);

			//TODO TODO TODO
			//if transp.size == 0 then don't do anything of this!
			//Except for drawing labels nicely on top of each other with correct AA.

			int quer = 1;
			//with this loop it draws as many layers as neccessary to complete the scene
			//you can experiment by drawing only one layer or two and then view the result (see the comment after the loop)

			//peel depths on fbos[0], addOneLayer on fbos[1]

			if(additionalResolve){ // because tex[1] is used in peeldepth
				blitColorOrDepth(gl, fbosToResolve[1], fbos[1], width, height, width, height, gl.GL_COLOR_BUFFER_BIT);
				//	blitColorOrDepth(gl, fbosToResolve[1], fbos[1], width, height, width, height, gl.GL_DEPTH_BUFFER_BIT);
			}
			//			testCurrentFrameBuffer(gl, fbos[1], ppRenderTextureFrameBuffer[0], width, height,true);

			//draw transparent objects into FBO with reverse depth values
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[0]);
			startQuery(gl);
			peelDepth(gl, transp, supersample*width, supersample*height);

			quer = endQuery(gl);

			int counter = 0;
			while(quer!=0 && counter < 20){
				counter++;
				//draw on the SCREEN
				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
				addOneLayer(gl, transp, supersample*width, supersample*height);
				//draw transparent objects into FBO with reverse depth values
				gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[0]);
				startQuery(gl);
				peelDepth(gl, transp, supersample*width, supersample*height);
				quer = endQuery(gl);
				// 4  performed an fbos[1] check here. results like in 5.
				//				testCurrentFrameBuffer(gl, fbos[1], ppRenderTextureFrameBuffer[0], width, height,true);
			}
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
			gl.glDisable(gl.GL_DEPTH_TEST);

			//5  performed an fbos[1] check here. multi, not multi. result: no MSAA on any
			//			if(transp.size()!=0) testCurrentFrameBuffer(gl, fbos[1], ppRenderTextureFrameBuffer[0], width, height, true);

		}
		//    	
		if(infoData.activated)
			LabelShader.renderOverlay("Framerate = " + infoData.framerate + "\nClockrate = " + infoData.clockrate + "\nPolygonCount = " + infoData.polygoncount + "\n" + InfoOverlay.getMemoryUsage(), gl);

		// stop culling here, it destroys post processing and skyboxes
		if(enableBackFaceCulling)	gl.glDisable(gl.GL_CULL_FACE);

		if(transp.size() != 0){

			//			if(enableMultisampleTextures){
			//			// for resolving msaa texs
			//			copyFBO(gl, fbos[1], ppRenderTextureFrameBuffer[0], gl.GL_COLOR_BUFFER_BIT,
			//					supersample*width, supersample*height, supersample*width, supersample*height);
			//			copyFBO(gl, fbos[1], ppRenderTextureFrameBuffer[0], gl.GL_DEPTH_BUFFER_BIT,
			//					supersample*width, supersample*height, supersample*width, supersample*height);
			//			}

			//			copyFBO(gl, 0, ppRenderTextureFrameBuffer[0], gl.GL_COLOR_BUFFER_BIT,
			//					supersample*width, supersample*height, supersample*width, supersample*height);
			//			copyFBO(gl, 0, ppRenderTextureFrameBuffer[0], gl.GL_DEPTH_BUFFER_BIT,
			//					supersample*width, supersample*height, supersample*width, supersample*height);

			//			copyFBO(gl, fbos[1], ppRenderTextureFrameBuffer[0], gl.GL_COLOR_BUFFER_BIT,
			//					supersample*width, supersample*height, width, height);
			//			copyFBO(gl, fbos[1], ppRenderTextureFrameBuffer[0], gl.GL_DEPTH_BUFFER_BIT,
			//					supersample*width, supersample*height, width, height);
			//			
			//			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, ppRenderTextureFrameBuffer[0]);
			//			if(fxaaEnabled) for(int i=0;i<nrOfFxaaIterations;i++) copyFBO2FBwithFXAA(gl, ppRenderTexture[0],width,height);
			//			
			//			copyFBO(gl, ppRenderTextureFrameBuffer[0], 0, gl.GL_COLOR_BUFFER_BIT,
			//					width, height, width, height);

			//			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
			//			if(fxaaEnabled) for(int i=0;i<nrOfFxaaIterations;i++) copyFBO2FBwithFXAA(gl, texs[2],supersample*width,supersample*height);

			//			copyFBO(gl, fbos[1], 0, gl.GL_COLOR_BUFFER_BIT,
			//					supersample*width, supersample*height, width, height); 
			//			copyFBO(gl, fbos[1], 0, gl.GL_DEPTH_BUFFER_BIT,
			//					supersample*width, supersample*height, width, height);

			//			copyFBO(gl, ppRenderTextureFrameBuffer[0], 0, gl.GL_COLOR_BUFFER_BIT,
			//					width, height, width, height); 
			//			copyFBO(gl, ppRenderTextureFrameBuffer[0], 0, gl.GL_DEPTH_BUFFER_BIT,
			//					width, height, width, height);

			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
			//			//you can change the number here:
			//			//0 means the current depth layer generated by peelDepth()
			//			//1 the depth layer of the final image generated by addOneLayer()
			//			//2 the color layer of the final image generated by addOneLayer()
			copyFBO2FB(gl, 2, width, height);

		}


		// from here on, everything relevant is on the default FBO 0.
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);



	}

	/**
	 * renders on tex[0], made for depth. also blends
	 * Before calling it in the normal rendering we bind fbos[1]
	 * @param gl
	 * @param transp
	 * @param width
	 * @param height
	 */
	private static void addOneLayer(GL3 gl, List<RenderableObject> transp, int width, int height) {

		// resolve through another fbo
		if(additionalResolve) gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbosToResolve[1]);


		//		if(enableMultisampleTextures) 
		//						gl.glEnable(gl.GL_MULTISAMPLE);
		//						gl.glEnable(gl.GL_TEXTURE_2D_MULTISAMPLE);
		//		} else {
		gl.glEnable(gl.GL_TEXTURE_2D);
		//		}
		gl.glViewport(0, 0, width, height);

		gl.glEnable(gl.GL_BLEND);
		gl.glBlendEquation(gl.GL_FUNC_ADD);
		gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);

		gl.glActiveTexture(gl.GL_TEXTURE9);
		//		if(enableMultisampleTextures) gl.glBindTexture(gl.GL_TEXTURE_2D_MULTISAMPLE, texs[0]);
		//		else gl.glBindTexture(gl.GL_TEXTURE_2D, texs[0]);

		//		gl.glBindTexture(gl.GL_TEXTURE_2D, texs[0]);
		gl.glBindTexture(glTextureMode, texs[0]);

		for(RenderableObject o : transp){

			//			gl.glBindTexture(gl.GL_TEXTURE_2D, texs[0]);
			o.addOneLayer(width, height);
			//			gl.glDisable(gl.GL_MULTISAMPLE);
		}

		gl.glDisable(gl.GL_BLEND);

		//		if(enableMultisampleTextures) gl.glDisable(gl.GL_MULTISAMPLE);
		// resolve
		if(additionalResolve){
			blitColorOrDepth(gl, fbosToResolve[1], fbos[1], width, height, width, height, gl.GL_COLOR_BUFFER_BIT);
			blitColorOrDepth(gl, fbosToResolve[1], fbos[1], width, height, width, height, gl.GL_DEPTH_BUFFER_BIT);
		}
	}

	/**
	 * renders on tex[1], made for depth.
	 * Before calling it in the normal rendering we bind fbos[0]
	 * @param gl
	 * @param transp
	 * @param width
	 * @param height
	 */
	private static void peelDepth(GL3 gl, List<RenderableObject> transp, int width, int height) {

		// resolve through another fbo
		if(additionalResolve) gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbosToResolve[0]);

		//		if(enableMultisampleTextures) 
		//			gl.glEnable(gl.GL_MULTISAMPLE);
		//			gl.glDisable(gl.GL_MULTISAMPLE);
		//			gl.glEnable(gl.GL_TEXTURE_2D_MULTISAMPLE);
		//		} else {
		gl.glEnable(gl.GL_TEXTURE_2D);
		//		}

		gl.glViewport(0, 0, width, height);
		gl.glEnable(gl.GL_DEPTH_TEST);


		gl.glClearColor(0, 0, 0, 1);
		gl.glClear(gl.GL_COLOR_BUFFER_BIT);
		gl.glClearDepth(1);
		gl.glClear(gl.GL_DEPTH_BUFFER_BIT);

		gl.glActiveTexture(gl.GL_TEXTURE0);
		//		if(enableMultisampleTextures) gl.glBindTexture(gl.GL_TEXTURE_2D_MULTISAMPLE, texs[1]);
		//		else gl.glBindTexture(gl.GL_TEXTURE_2D, texs[1]);

		//		gl.glBindTexture(gl.GL_TEXTURE_2D, texs[1]);
		gl.glBindTexture(glTextureMode, texs[1]);

		for(RenderableObject o : transp){

			//			gl.glBindTexture(gl.GL_TEXTURE_2D, texs[1]);
			o.renderDepth(width, height);

		}

		//		if(enableMultisampleTextures) gl.glDisable(gl.GL_MULTISAMPLE);
		// resolve
		if(additionalResolve){
			//			blitColorOrDepth(gl, fbosToResolve[0], fbos[0], width, height, width, height, gl.GL_COLOR_BUFFER_BIT);
			blitColorOrDepth(gl, fbosToResolve[0], fbos[0], width, height, width, height, gl.GL_DEPTH_BUFFER_BIT);
		}
	}

	/**
	 * this method renders the input texture onto the current frame buffer.
	 * It does so by looking at a quad with our texture set.
	 * @param gl
	 * @param tex
	 * @param width
	 * @param height
	 */
	private static void copyFBO2FB(GL3 gl, int tex, int width, int height){
		gl.glDisable(gl.GL_BLEND);
		gl.glViewport(0, 0, width, height);

		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE0);
		gl.glBindTexture(gl.GL_TEXTURE_2D, texs[tex]);

		gl.glDisable(gl.GL_DEPTH_TEST);

		copy.useShader(gl);

		gl.glUniform1i(gl.glGetUniformLocation(copy.shaderprogram, "image"), 0);

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyCoords.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(copy.shaderprogram, copyCoords.getName()), copyCoords.getElementSize(), copyCoords.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(copy.shaderprogram, copyCoords.getName()));

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyTex.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(copy.shaderprogram, copyTex.getName()), copyTex.getElementSize(), copyTex.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(copy.shaderprogram, copyTex.getName()));

		gl.glDrawArrays(gl.GL_TRIANGLES, 0, copyCoords.getLength()/4);

		gl.glDisableVertexAttribArray(gl.glGetAttribLocation(copy.shaderprogram, copyCoords.getName()));
		gl.glDisableVertexAttribArray(gl.glGetAttribLocation(copy.shaderprogram, copyTex.getName()));

		copy.dontUseShader(gl);
	}

	/**
	 * this method renders the input texture onto the current frame buffer.
	 * It does so by looking at a quad with our texture set.
	 * @param gl
	 * @param FBOtexInt
	 * @param width
	 * @param height
	 */
	private static void copyAnyFBO2FB(GL3 gl, int FBOtexInt, int width, int height){
		gl.glDisable(gl.GL_BLEND);
		gl.glViewport(0, 0, width, height);

		//		if(enableMultisampleTextures) {
		//			//			gl.glEnable(gl.GL_TEXTURE_2D_MULTISAMPLE);
		//			gl.glActiveTexture(gl.GL_TEXTURE0);
		//			gl.glBindTexture(gl.GL_TEXTURE_2D_MULTISAMPLE, FBOtexInt);
		//		} else {
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glBindTexture(gl.GL_TEXTURE_2D, FBOtexInt);
		//		}		


		gl.glDisable(gl.GL_DEPTH_TEST);

		copy.useShader(gl);

		gl.glUniform1i(gl.glGetUniformLocation(copy.shaderprogram, "image"), 0);

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyCoords.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(copy.shaderprogram, copyCoords.getName()), copyCoords.getElementSize(), copyCoords.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(copy.shaderprogram, copyCoords.getName()));

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyTex.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(copy.shaderprogram, copyTex.getName()), copyTex.getElementSize(), copyTex.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(copy.shaderprogram, copyTex.getName()));

		gl.glDrawArrays(gl.GL_TRIANGLES, 0, copyCoords.getLength()/4);

		gl.glDisableVertexAttribArray(gl.glGetAttribLocation(copy.shaderprogram, copyCoords.getName()));
		gl.glDisableVertexAttribArray(gl.glGetAttribLocation(copy.shaderprogram, copyTex.getName()));

		copy.dontUseShader(gl);
	}


	/**
	 * this method renders the renderTexture, holding everything to the framebuffer.
	 * but uses FXAA!
	 * It does so by looking at a quad with our texture set.
	 * @param gl
	 * @param tex
	 * @param width
	 * @param height
	 */
	private static void copyFBO2FBwithShader(GL3 gl, GLShader shader ,  int texInt, int width, int height){


		gl.glDisable(gl.GL_BLEND);
		gl.glViewport(0, 0, width, height);

		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE1); // !!!! TEXTURE1 works TEXTURE0 causes black screen. Hard coded danger!!!!
		gl.glBindTexture(gl.GL_TEXTURE_2D, texInt);

		gl.glDisable(gl.GL_DEPTH_TEST);

		shader.useShader(gl);

		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "image"), 0); // what does this do? why 0?

		// send fxaa uniforms
		gl.glUniform3f(gl.glGetUniformLocation(shader.shaderprogram, "R_inverseFilterTextureSize"),(float) (1.0/width),(float) (1.0/height),0f);

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyCoords.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, copyCoords.getName()), copyCoords.getElementSize(), copyCoords.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, copyCoords.getName()));

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyTex.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(shader.shaderprogram, copyTex.getName()), copyTex.getElementSize(), copyTex.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, copyTex.getName()));

		gl.glDrawArrays(gl.GL_TRIANGLES, 0, copyCoords.getLength()/4);

		gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, copyCoords.getName()));
		gl.glDisableVertexAttribArray(gl.glGetAttribLocation(shader.shaderprogram, copyTex.getName()));

		shader.dontUseShader(gl);
	}

	/**
	 * this method renders the renderTexture, holding everything to the framebuffer.
	 * but uses Depth Of Field! Far away objects get blurry
	 * It does so by looking at a quad with our texture set.
	 * @param gl
	 * @param tex
	 * @param width
	 * @param height
	 */
	private static void copyFBO2FBwithDOF(GL3 gl, int width, int height){
//		System.out.println("dof apply call");

		gl.glDisable(gl.GL_BLEND);
		gl.glViewport(0, 0, width, height);

		// we have 2 textures. color and depth, ppRenderTexture[0] and texs[1] respectively
		gl.glEnable(gl.GL_TEXTURE_2D);


		gl.glDisable(gl.GL_DEPTH_TEST);

		gl.glClear(1);

		dofShader.useShader(gl);
//		System.out.println();
		gl.glActiveTexture(gl.GL_TEXTURE1); // !!!! TEXTURE1 works TEXTURE0 causes black screen. Hard coded danger!!!! TODO
		gl.glBindTexture(gl.GL_TEXTURE_2D, ppRenderTexture[0]);
		gl.glUniform1i(gl.glGetUniformLocation(dofShader.shaderprogram, "colorImage"), 0); // what does this do? why 0?
		gl.glActiveTexture(gl.GL_TEXTURE2); 
		gl.glBindTexture(gl.GL_TEXTURE_2D, texs[1]);
		gl.glUniform1i(gl.glGetUniformLocation(dofShader.shaderprogram, "depthImage"), 1); 




		// send fxaa uniforms
		gl.glUniform3f(gl.glGetUniformLocation(dofShader.shaderprogram, "R_inverseFilterTextureSize"),(float) (1.0/width),(float) (1.0/height),0f);

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyCoords.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(dofShader.shaderprogram, copyCoords.getName()), copyCoords.getElementSize(), copyCoords.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(dofShader.shaderprogram, copyCoords.getName()));

		gl.glBindBuffer(gl.GL_ARRAY_BUFFER, copyTex.getID());
		gl.glVertexAttribPointer(gl.glGetAttribLocation(dofShader.shaderprogram, copyTex.getName()), copyTex.getElementSize(), copyTex.getType(), false, 0, 0);
		gl.glEnableVertexAttribArray(gl.glGetAttribLocation(dofShader.shaderprogram, copyTex.getName()));

		gl.glDrawArrays(gl.GL_TRIANGLES, 0, copyCoords.getLength()/4);

		gl.glDisableVertexAttribArray(gl.glGetAttribLocation(dofShader.shaderprogram, copyCoords.getName()));
		gl.glDisableVertexAttribArray(gl.glGetAttribLocation(dofShader.shaderprogram, copyTex.getName()));

		dofShader.dontUseShader(gl);
	}

	/**
	 * This method copies the color or depth values of one FBO to another, optionally directly into the default one
	 * This method can be inserted into the code to view how the textures change
	 * 
	 * @param gl
	 * @param FBOint
	 * @param FBOtarget
	 * @param whatBit COLOR OR DEPTH
	 * @param startWidth
	 * @param startHeight
	 * @param targetWidth
	 * @param targetHeight
	 */
	private static void copyFBO(GL3 gl ,int FBOint, int FBOtarget, int whatBit, int startWidth, int startHeight, int targetWidth, int targetHeight){

		// we copy it twice since some tutorials did this and named it "resolveing multisample"
		gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, FBOint); //FramebufferRenderName
		gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, FBOtarget); //FramebufferRenderName
		gl.glBlitFramebuffer(
				0, 0, startWidth, startHeight, 
				0, 0, targetWidth, targetHeight,//0, 0, width, height, //0, 0, supersample*width, supersample*height, 
				whatBit,
				gl.GL_NEAREST);

	}

	private static void blitColorOrDepth(GL3 gl ,int FBOintRead, int FBOintDraw, int widthREAD, int heightREAD, int widthDRAW, int heightDRAW, int glBufferBit){

		// we copy it twice since some tutorials did this and named it "resolveing multisample"
		gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, FBOintRead); //FramebufferRenderName
		gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, FBOintDraw); //FramebufferRenderName
		gl.glBlitFramebuffer(
				0, 0, widthREAD, heightREAD, 
				0, 0, widthDRAW, heightDRAW,//0, 0, width, height, //0, 0, supersample*width, supersample*height, 
				glBufferBit, //| gl.GL_DEPTH_BUFFER_BIT,
				gl.GL_NEAREST);

	}

	/**
	 *  this method  runs a series of checks on the FBO
	 */
	private static void checkFramebuffer(GL3 gl){
		int glStatus =0;

		//		if(gl.glCheckFramebufferStatus(glStatus)!=0);{ //returns zero if an error occured
		switch (glStatus) {
		case 1: glStatus = gl.GL_FRAMEBUFFER_UNDEFINED;
		System.err.println("is returned if target is the default framebuffer, but the default framebuffer does not exist.");
		break;
		case 2: glStatus = gl.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT;
		System.err.println("is returned if target is the default framebuffer, but the default framebuffer does not exist.");
		break;
		case 3: glStatus = gl.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT;
		System.err.println("is returned if the framebuffer does not have at least one image attached to it.");
		break;
		case 4: glStatus = gl.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER;
		System.err.println("is returned if the value of GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE is GL_NONE for any color attachment point(s) named by GL_DRAW_BUFFERi.");
		break;
		case 5: glStatus = gl.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER;
		System.err.println("is returned if GL_READ_BUFFER is not GL_NONE and the value of GL_FRAMEBUFFER_ATTACHMENT_OBJECT_TYPE is GL_NONE for the color attachment point named by GL_READ_BUFFER.");
		break;
		case 6: glStatus = gl.GL_FRAMEBUFFER_UNSUPPORTED;
		System.err.println("is returned if the combination of internal formats of the attached images violates an implementation-dependent set of restrictions");
		break;
		case 7: glStatus = gl.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
		System.err.println("is returned if the value of GL_RENDERBUFFER_SAMPLES is not the same for all attached renderbuffers; if the value of GL_TEXTURE_SAMPLES is the not same for all attached textures; or, if the attached images are a mix of renderbuffers and textures, the value of GL_RENDERBUFFER_SAMPLES does not match the value of GL_TEXTURE_SAMPLES.");
		break;
		case 8: glStatus = gl.GL_FRAMEBUFFER_INCOMPLETE_MULTISAMPLE;
		System.err.println("is also returned if the value of GL_TEXTURE_FIXED_SAMPLE_LOCATIONS is not the same for all attached textures; or, if the attached images are a mix of renderbuffers and textures, the value of GL_TEXTURE_FIXED_SAMPLE_LOCATIONS is not GL_TRUE for all attached textures.");
		break;
		case 9: glStatus = gl.GL_FRAMEBUFFER_INCOMPLETE_LAYER_TARGETS;
		System.err.println("is returned if any framebuffer attachment is layered, and any populated attachment is not layered, or if all populated color attachments are not from textures of the same target.");
		break;
		default:
			break;
		}
		//		} end if
	}



	public static BufferedImage renderOffscreen(double AA, BufferedImage dst, GL3 gl, RenderableUnitCollection ruc, List<RenderableObject> transp, int width, int height, BackgroundHelper backgroundHelper){
//		System.out.println("supersample is " + supersample);
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
		gl.glViewport(0, 0, supersample*width, supersample*height);
		gl.glClearColor(0.5f, 0.5f, 0.5f, 1);
		gl.glClear(gl.GL_COLOR_BUFFER_BIT);
		//draw background here
		backgroundHelper.draw(gl);
		SkyboxHelper.render(gl);
		//draw nontransparent objects into framebuffer
		gl.glEnable(gl.GL_DEPTH_TEST);
		gl.glClearDepth(1);
		gl.glClear(gl.GL_DEPTH_BUFFER_BIT);

		gl.glDisable(gl.GL_BLEND);
		ruc.render(gl, width, height);
		for(RenderableObject o : transp){
			o.render(width, height);
		}
		int quer = 1;
		//with this loop it draws as many layers as neccessary to complete the scene
		//you can experiment by drawing only one layer or two and then view the result (see the comment after the loop)

		//draw transparent objects into FBO with reverse depth values
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[0]);
		startQuery(gl);
		peelDepth(gl, transp, supersample*width, supersample*height);
		quer = endQuery(gl);

		int counter = 0;
		while(quer!=0 && counter < 20){
			counter++;
			//draw on the SCREEN
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[1]);
			addOneLayer(gl, transp, supersample*width, supersample*height);
			//draw transparent objects into FBO with reverse depth values
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, fbos[0]);
			startQuery(gl);
			peelDepth(gl, transp, supersample*width, supersample*height);
			quer = endQuery(gl);
		}

		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
		//you can change the number here:
		//0 means the current depth layer generated by peelDepth()
		//1 the depth layer of the final image generated by addOneLayer()
		//2 the color layer of the final image generated by addOneLayer()
		copyFBO2FB(gl, 2, width, height);

//		System.out.println("before dst=new...");
		dst = new BufferedImage(width, height,
				BufferedImage.TYPE_4BYTE_ABGR); // TYPE_3BYTE_BGR); //

		Buffer buffer = ByteBuffer.wrap(((DataBufferByte) dst.getRaster()
				.getDataBuffer()).getData());

//		System.out.println("before ReadPixels");
		int err = gl.glGetError();
//		System.out.println(err);
		gl.glReadPixels(0, 0, width, height, GL3.GL_RGBA,
				GL3.GL_UNSIGNED_BYTE, buffer);
		err = gl.glGetError();
//		System.out.println("ss" + supersample);
//		System.out.println(err);
//		System.err.println("reading pixels");

		dst = ImageUtility.rearrangeChannels(null, dst);
		//TODO revert here
		//		ImageUtil.flipImageVertically(dst);
		return dst;
	}
}


