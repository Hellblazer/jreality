package de.jreality.jogl3.helper;

import java.io.File;
import java.util.ArrayList;

import com.jogamp.opengl.GL3;

import de.jreality.jogl3.JOGL3Viewer;
import de.jreality.jogl3.JOGLAppearanceInstance;
import de.jreality.jogl3.JOGLSceneGraph;
import de.jreality.jogl3.JOGLSceneGraphComponentInstance;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.shader.GLVBOFloat;
import de.jreality.scene.Appearance;
import de.jreality.shader.CommonAttributes;


/**
 * This object allows to dinamicaly load and activate and deactivate post processing shaders that act on the final image.
 * 
 * we do not yet have method to edit the uniforms in the shaders. maybe create a postPro interface and one class for each shader
 * 
 * note: like the camera menu this could be later put in like a plugin.
 * @author padilla
 *
 */
/**
 * @author Marcel
 *
 */
public class PostProcessingHelper {

	//openGL
	public GL3 gl;

	// for startup
	public static boolean initialized = false;

	// make them global, to check when changes happend
	static int textureWidth =1;
	static int textureHeight =1;

	// for fast reference. true if at least one shader active
	public static boolean isEnabled = false; 

	// list to call each shader from
	static ArrayList<String> ppShaderNameList = new ArrayList<String>();
	static ArrayList<String> ppNameList = new ArrayList<String>(); // without "postprocessing/"
	public static ArrayList<GLShader> ppShaderList = new ArrayList<GLShader>();

	// always load the copy shader in case that no post processing was applied
	public static GLShader copyShader = new GLShader("testing/copy.v", "testing/copy.f");

	//let it work on its very own texture and framebuffer
	private static int[] ppTexture; // 0 = color, 1 = depth
	private static int[] ppTextureFrameBuffer;


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


	/**
	 * a gimmick constructor to use methods here before Jogl3 was initilized
	 */
	public PostProcessingHelper(){

	}

	/**
	 * initialises all shaders. Then later care about how much they are used
	 * @param shaderList
	 */
	public PostProcessingHelper(GL3 mygl ){//, ArrayList<GLShader> shaderList){
		addGL(mygl);
	}


	/**
	 * needed as the constructor. 
	 * @param mygl
	 */
	public void addGL(GL3 mygl){
		gl=mygl;
		// render init
		copyCoords = new GLVBOFloat(gl, testQuadCoords, "vertex_coordinates");
		copyTex = new GLVBOFloat(gl, testTexCoords, "texture_coordinates");		
		copyShader.init(mygl);

		ppTexture = new int[2];
		ppTextureFrameBuffer = new int[1];
		if(ppTexture[0]==0) gl.glGenTextures(ppTexture.length, ppTexture, 0);
		if(ppTextureFrameBuffer[0]==0) gl.glGenFramebuffers(ppTextureFrameBuffer.length, ppTextureFrameBuffer, 0);

		initialized=true;

		checkIfEnabled();

	}

	/**
	 * add a shader with this specific name
	 * @param shaderName
	 */
	public void addShader(String shaderName){
		addShader(shaderName, 1);
	}

	public void addShader(String shaderName, int much){
		for(int i = 0; i < much ; i++){
			GLShader shady = new GLShader("postprocessing/copy.v", shaderName);
			ppShaderList.add(shady);
		}
		checkIfEnabled();
	}

	public static void test(){
		System.out.println("nothign");
	}



	/**
	 * removes the shader on first sight. not all at once
	 * @param shaderName
	 */
	public void removeShader(String shaderName){
		int i = 0;
		for(GLShader shader : ppShaderList){

			if(shader.getFragFilename().equals(shaderName) ) {
				ppShaderList.remove(i);
				break;
			}
			i++;

		}
		checkIfEnabled();
	}

	public void removeAllShaders(){
		ppShaderList.clear();
		checkIfEnabled();
	}

	public void removeAllFxaaShaders(){

		ArrayList<Integer> toRemove = new ArrayList<Integer>();
		int index=0;
		for(GLShader shader : ppShaderList){
			if(shader.getFragFilename().equals("postprocessing/fxaa.f")){
				//remember to remove
				toRemove.add(0,index);				
			}
			index++;
		}
		// remove last one in the list first
		for(int i : toRemove){
			ppShaderList.remove(i);	
		}
		checkIfEnabled();

	}

	/**
	 * add times fxaa shaders to the very beginning of the list so it happens first if true.
	 * @param times
	 */
	public void addFxaaShader(int times, boolean beginning){
		removeAllFxaaShaders();
		GLShader fxaaShader = null;
		for(int i = 0 ; i < times ; i++){

			// init the shader onl yonce
			if(i==0) fxaaShader = new GLShader("postprocessing/copy.v", "postprocessing/fxaa.f");

			//put on the list
			if( beginning) ppShaderList.add(0, fxaaShader);
			else ppShaderList.add(fxaaShader);
		}
		checkIfEnabled();
	}

	/**
	 * // a number tracking how much fxaa is applied.  Used for keyboard action
	 * @return
	 */
	public int nrOfFxaaShaders(){
		int nr = 0;
		for(GLShader shader : ppShaderList){
			if(shader.getFragFilename().equals("postprocessing/fxaa.f")){
				nr++;				
			}
		}
		return nr;		
	}

	public void disable(){
		isEnabled=false;
	}
	public void enable(){
		isEnabled = true;
	}


	/**
	 * true if any shader is activated at all
	 */
	public void checkIfEnabled(){

		isEnabled = (ppShaderList.size()>0);

	}


	public void render(int width, int height){

		if(gl==null) System.err.println("GL IS NULL in post processor");

		int texInt = ppTexture[0];

		//		System.out.println("is Enabled = " + isEnabled);
		// make sure that if no post processing was enabled, that we don't end up with nothing.
		if(isEnabled){
			//			applyShader(copyShader, texInt, width, height);
			//		} else {

			// see if size did change
			checkSize(width, height);

			// copy final image to texture
			gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, 0);
			gl.glBindTexture(gl.GL_TEXTURE_2D, texInt);
			gl.glCopyTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_RGBA8, 0,0,width, height, 0);

			// draw on ppFBO to reuse the result on another post processing step
			gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, ppTextureFrameBuffer[0]);

			// do the work
			applyAll(texInt, width, height);

			//copy back to default
			copy2DefualtFB();
			//					gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, 0);
			//					applyShader(copyShader, texInt, width, height);
		}
	}

	/**
	 * calling this will cause the program to take the texture and rerender that image using each shader in the list.
	 * @param texInt
	 * @param width
	 * @param height
	 */
	public void applyAll(int texInt, int width, int height){

		//		System.out.println("ppShaderList.size() = " + ppShaderList.size());
		//		System.out.println("texturesize is = " + width + " x " + height);

		//for each shader
		for(GLShader shader  : ppShaderList){
			//apply as many times as it is activated
			//			System.out.println(shader.getFragFilename());
			//initialize if needed
			if(shader.shaderprogram == 0){
				shader.init(gl);
			}

			//then apply it
			applyShader(shader, texInt, width, height);
		}
	}

	/**
	 * Given the texture, this call will apply a single shader to the texture.
	 * @param shader
	 * @param texInt
	 * @param width
	 * @param height
	 */
	public void applyShader(GLShader shader, int texInt, int width, int height){

		gl.glDisable(gl.GL_BLEND);
		gl.glViewport(0, 0, width, height);
		gl.glEnable(gl.GL_TEXTURE_2D);
		gl.glActiveTexture(gl.GL_TEXTURE1); // !!!! TEXTURE1 works TEXTURE0 causes black screen. Hard coded danger!!!!
		gl.glBindTexture(gl.GL_TEXTURE_2D, texInt);

		gl.glDisable(gl.GL_DEPTH_TEST);

		shader.useShader(gl);

		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "image"), 0); // what does this do? why 0?

		// send basic uniforms, this one is only for fxaa
		gl.glUniform3f(gl.glGetUniformLocation(shader.shaderprogram, "R_inverseFilterTextureSize"),(float) (1.0/width),(float) (1.0/height),0f);
		gl.glUniform2i(gl.glGetUniformLocation(shader.shaderprogram, "FilterTextureSize"), width,    height);
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "textureWidth"), width);
		gl.glUniform1i(gl.glGetUniformLocation(shader.shaderprogram, "textureHeight"), height);

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
	 * will resize on time when sizes change
	 * @param width
	 * @param height
	 */
	public void checkSize(int width, int height){
		if( textureWidth != width || textureHeight!=height ){
			textureHeight = height;
			textureWidth = width;
			resizeFrameBufferTextures(textureWidth, textureHeight);
		}
	}

	public void resizeFrameBufferTextures(int width, int height){

		//		int filtermode = gl.GL_NEAREST;
		int filtermode = gl.GL_LINEAR;
		//		System.out.println("ppTexture[0] = " +ppTexture[0]);
		// texture preparation for color
		gl.glBindTexture(gl.GL_TEXTURE_2D, ppTexture[0]);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, filtermode);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, filtermode);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_RGBA8, width, height, 0, gl.GL_RGBA, gl.GL_UNSIGNED_BYTE, null);

		// texture preparation for depth
		gl.glBindTexture(gl.GL_TEXTURE_2D, ppTexture[1]);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MAG_FILTER, filtermode);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_MIN_FILTER, filtermode);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_S, gl.GL_CLAMP_TO_EDGE);
		gl.glTexParameteri(gl.GL_TEXTURE_2D, gl.GL_TEXTURE_WRAP_T, gl.GL_CLAMP_TO_EDGE);
		gl.glTexImage2D(gl.GL_TEXTURE_2D, 0, gl.GL_DEPTH_COMPONENT, width, height, 0, gl.GL_DEPTH_COMPONENT, gl.GL_FLOAT, null);

		// binding to framebuffer
		gl.glBindFramebuffer(gl.GL_FRAMEBUFFER, ppTextureFrameBuffer[0]);
		gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_COLOR_ATTACHMENT0, gl.GL_TEXTURE_2D, ppTexture[0], 0);
		gl.glFramebufferTexture2D(gl.GL_FRAMEBUFFER, gl.GL_DEPTH_ATTACHMENT, gl.GL_TEXTURE_2D, ppTexture[1], 0);

	}

	public void copy2DefualtFB(){

		gl.glBindFramebuffer(gl.GL_READ_FRAMEBUFFER, ppTextureFrameBuffer[0]); //FramebufferRenderName
		gl.glBindFramebuffer(gl.GL_DRAW_FRAMEBUFFER, 0); //FramebufferRenderName
		gl.glBlitFramebuffer(
				0, 0, textureWidth, textureHeight, 
				0, 0, textureWidth, textureHeight,//0, 0, width, height, //0, 0, supersample*width, supersample*height, 
				gl.GL_COLOR_BUFFER_BIT,
				gl.GL_NEAREST);
	}



	/**
	 * goes through the jogl3 glsl postprocessing directory and returns a List of all included shaders.
	 * important for drop and use shaders.
	 */
	public static ArrayList<String> getAvailableShaderList(){
		ppShaderNameList = new ArrayList<String>();
		ppNameList = new ArrayList<String>();
		String shaderfolder = GLShader.class.getResource("postprocessing/").toString();
		File folder = new File(shaderfolder);
		//		System.out.println(folder.getPath().substring(5, folder.getPath().length()));
		//cuts "file:" from string
		File[] listOfFiles = new File(folder.getPath().substring(5, folder.getPath().length())).listFiles();
		//		System.out.println(Arrays.toString(listOfFiles));

		for (int i = 0; i < listOfFiles.length; i++) {
			if (listOfFiles[i].isFile()) {
				ppNameList.add(listOfFiles[i].getName());
				ppShaderNameList.add("postprocessing/" + listOfFiles[i].getName());
			}
		}

		return ppShaderNameList;
	}

	/**
	 * for startup purposes
	 * @param proxyScene
	 * @param ppHelper
	 */
	public static void readConfig2AddFxaa(JOGLSceneGraph proxyScene, PostProcessingHelper ppHelper){
		JOGLSceneGraphComponentInstance rootInstance = (JOGLSceneGraphComponentInstance) proxyScene.getTreeRoot();
		JOGLAppearanceInstance rootApInst = (JOGLAppearanceInstance)rootInstance.getAppearanceTreeNode();
		Appearance rootAp = (Appearance) rootApInst.getEntity().getNode();
		Object bgo = null;
		boolean enabled;
		if (rootAp != null)
			bgo = rootAp.getAttribute(CommonAttributes.ANTIALIASING_FXAA_ENABLED);
		if (bgo != null && bgo instanceof Boolean)
			enabled = (Boolean) bgo;
		else{
			enabled = CommonAttributes.ANTIALIASING_FXAA_ENABLED_DEFAULT;
		}
		if(enabled){
			int factor;
			if (rootAp != null)
				bgo = rootAp.getAttribute(CommonAttributes.ANTI_ALIASING_FXAA_FACTOR);
			if (bgo != null && bgo instanceof Integer)
				factor = (Integer) bgo;
			else{
				factor = CommonAttributes.ANTI_ALIASING_FXAA_FACTOR_DEFAULT;
			}
			if(factor != 0){
				ppHelper.addFxaaShader(factor, false);
			}
		}
	}


	/**
	 * This method deactivates withouth destroying the post processor for switiching viewers.
	 * call at jogl3.dispose()
	 */
	public static void deactivate(){

		//ppShaderList.clear();
		// force later reboot of texturesize and textures
		initialized = false;
		textureHeight =-1;
		textureWidth = -1;

		for(GLShader shader  : ppShaderList){
			shader.shaderprogram=0;
		}

	}

	/**
	 * This method ractivates for switiching viewers.
	 * call at jogl3.renderasync() (this is called when switiching back)
	 */
	public static void reactivate(JOGL3Viewer viewer, GL3 mygl){
		//initialized = false;
		viewer.postProcessor.addGL(mygl);
	}



}
