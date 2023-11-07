package de.jreality.jogl3;

import static de.jreality.shader.CommonAttributes.SKY_BOX;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JPanel;

import com.jogamp.opengl.GL3;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLException;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import de.jreality.backends.viewer.PerformanceMeter;
import de.jreality.jogl.InstrumentedViewer;
import de.jreality.jogl3.JOGLSceneGraphComponentInstance.RenderableObject;
import de.jreality.jogl3.glsl.GLShader;
import de.jreality.jogl3.helper.BackgroundHelper;
import de.jreality.jogl3.helper.LightHelper;
import de.jreality.jogl3.helper.PostProcessingHelper;
import de.jreality.jogl3.helper.SkyboxHelper;
import de.jreality.jogl3.helper.SphereHelper;
import de.jreality.jogl3.helper.TransparencyHelper;
import de.jreality.jogl3.helper.TubeHelper;
import de.jreality.jogl3.light.JOGLLightCollection;
import de.jreality.jogl3.optimization.RenderableUnitCollection;
import de.jreality.jogl3.shader.LabelShader;
import de.jreality.jogl3.shader.PointShader;
import de.jreality.jogl3.shader.Texture2DLoader;
import de.jreality.math.P3;
import de.jreality.math.Pn;
import de.jreality.scene.Appearance;
import de.jreality.scene.Camera;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.SceneGraphPath;
import de.jreality.scene.StereoViewer;
import de.jreality.scene.data.AttributeEntityUtility;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.CubeMap;
import de.jreality.util.CameraUtility;
import de.jreality.util.SceneGraphUtility;


/**
 * the jogl3 viewer, the entry point that determines the rendering
 *
 */
public class JOGL3Viewer implements de.jreality.scene.Viewer, StereoViewer, InstrumentedViewer {

	//TODO there's a bug here. When supersample is 2 point sprites are displayed with the correct size,
	//if supersample is 1 here they are twice the size.
	private int supersample = 1;
	SceneGraphComponent auxiliaryRoot;
	protected JPanel component;
	protected GLCanvas canvas;

	/**
	 * the constructor is called in the in the View.java when created as a new instance by reading a string.
	 * It serves as the GLcanvas creator
	 * @throws Exception
	 */
	public JOGL3Viewer() throws Exception{
		System.out.println("JOGL3Viewer constuctor called");
		this.auxiliaryRoot = SceneGraphUtility.createFullSceneGraphComponent("AuxiliaryRoot");


		//create a profile and attempt to select the highest openGL version for this hardware
		GLProfile glp = null;

		try{
			glp = GLProfile.get("GL4");
			System.out.println("Check for GL4");
			String s = GLProfile.glAvailabilityToString();
			System.out.println(s);
			//			if(!s.contains("3.3") && !s.contains("GL4 true")){
			//				System.err.println("opengl 3.3 not available, thus no jogl3-backend");
			//				throw new Exception("opengl 3.3 not available, thus no jogl3-backend");
			//			}
		}catch(GLException e){
			System.out.println(e.getMessage());
		}
		if(glp == null){
			try{
				glp = GLProfile.get("GL4bc");
				System.out.println("Check for GL4bc");
			}catch(GLException e){
				System.out.println(e.getMessage());
			}
		}
		if(glp == null){
			try{
				glp = GLProfile.get("GL3");
				System.out.println("Check for GL3");
			}catch(GLException e){
				System.out.println(e.getMessage());
			}
		}
		if(glp == null){
			try{
				glp = GLProfile.get("GL3bc");
				System.out.println("Check for GL3bc");
			}catch(GLException e){
				System.out.println(e.getMessage());
				throw new Exception("no openGL profile available to support JOGL3");
			}
		}
		//		if(glp == null){
		//			try{
		//				glp = GLProfile.get("GL2");
		//				String s = GLProfile.glAvailabilityToString();
		//				System.out.println(s);
		//			}catch(GLException e){
		//				System.out.println(e.getMessage());
		//				throw new Exception("no openGL profile available to support JOGL3");
		//			}
		//		}

		//				String s = GLProfile.glAvailabilityToString();
		//				System.out.println(s);

		System.out.println("Using: " + glp.getName());

		// use the profile to create capabilities to pass to the canvas
		GLCapabilities caps = new GLCapabilities(glp);
		//TODO: check caps.setAccumAlphaBits(8);
		caps.setAlphaBits(8);
		caps.setStereo(false);
		caps.setDoubleBuffered(true);
		//a value of 1, 2, 4 and 8 has just the same effect on linux nvidia-310.14, i.e. no anti-aliasing
		//16 times does anti-aliasing
		// april15 padilla: false: this anti-aliasing does work but only if transparency is turned of
		caps.setSampleBuffers(true);
		caps.setNumSamples(4);

		canvas = new GLCanvas(caps);
		canvas.addGLEventListener(this);


	}

	public boolean canRenderAsync() {
//		System.out.println("can render async called");
		return false;
	}

	public SceneGraphComponent getAuxiliaryRoot() {
//		System.out.println("getAuxiliaryRoot");
		return auxiliaryRoot;
	}

	public SceneGraphPath getCameraPath() {
		//System.out.println("getCameraPath");
		return camPath;
	}

	public SceneGraphComponent getSceneRoot() {
		return sceneRoot;
	}

	KeyListener keyListener = new KeyListener() {
		public void keyPressed(KeyEvent e) {
			component.dispatchEvent(e);
		}
		public void keyReleased(KeyEvent e) {
			component.dispatchEvent(e);
		}
		public void keyTyped(KeyEvent e) {
			component.dispatchEvent(e);
		}
	};
	MouseListener mouseListener = new MouseListener() {
		public void mouseClicked(MouseEvent e) {
			component.dispatchEvent(e);
		}
		public void mouseEntered(MouseEvent e) {
			component.dispatchEvent(e);
		}
		public void mouseExited(MouseEvent e) {
			component.dispatchEvent(e);
		}
		public void mousePressed(MouseEvent e) {
			component.dispatchEvent(e);
			canvas.requestFocus();
		}
		public void mouseReleased(MouseEvent e) {
			component.dispatchEvent(e);
		}
	};
	MouseWheelListener mouseWheelListener = new MouseWheelListener() {
		public void mouseWheelMoved(MouseWheelEvent e) {
			component.dispatchEvent(e);
		}
	};

	MouseMotionListener mouseMotionListener = new MouseMotionListener() {
		public void mouseDragged(MouseEvent e) {
			component.dispatchEvent(e);
		}
		public void mouseMoved(MouseEvent e) {
			component.dispatchEvent(e);
		}
	};

	public Object getViewingComponent() {
		//System.out.println("getViewingComponent");
		// TODO Auto-generated method stub
		if (component == null) {
			component=new javax.swing.JPanel();
			component.setLayout(new java.awt.BorderLayout());
			component.setMaximumSize(new java.awt.Dimension(32768,32768));
			component.setMinimumSize(new java.awt.Dimension(10,10));
			if (canvas == null) return component;
			component.add("Center", canvas);
			canvas.addKeyListener(keyListener);
			canvas.addMouseListener(mouseListener);
			canvas.addMouseMotionListener(mouseMotionListener);
			canvas.addMouseWheelListener(mouseWheelListener);
		}
		return component;
	}

	public Dimension getViewingComponentSize() {
		return ((Component) getViewingComponent()).getSize();
	}

	public boolean hasViewingComponent() {
		return true;
	}

	public void render() {
		//this method is called by the jReality event mechanism,
		//it calls the GLCanvas.display() method. GLCanvas decides
		//whether the GL has been set up properly, tries to call init(),
		//and if all goes well it calls the GLEventListener.display(GLAutoDrawable arg0) method
		//implemented here.
		//System.out.println("called render");
		if(sceneRoot == null)
			System.out.println("scene root is null");
		else{
			//print(sceneRoot);
		}

		canvas.display();
	}

	public void renderAsync() {
//		System.out.println("renderAsync");
		//if (disposed) return;
		// does nothing at the moment???

	}

	public void setAuxiliaryRoot(SceneGraphComponent auxRoot) {
		//System.out.println("set Aux Root");
		this.auxiliaryRoot = auxRoot;
	}
	SceneGraphPath camPath = null;
	public void setCameraPath(SceneGraphPath cameraPath) {
		//System.out.println("setCameraPath");
		camPath = cameraPath;

	}

	SceneGraphComponent sceneRoot = null;

	JOGLSceneGraph proxyScene = null;

	public void setSceneRoot(SceneGraphComponent root) {
		//		if (proxyScene != null) proxyScene.dispose();
		//System.out.println("setSceneRoot");
		sceneRoot = root;
		//		proxyScene = new JOGLSceneGraph(root);
		//		proxyScene.createProxyTree();
	}
	public int getStereoType() {
		System.out.println("getStereoType");
		// TODO Auto-generated method stub
		return stereoType;
	}
	int stereoType = 0;
	public void setStereoType(int type) {
		System.out.println("setStereoType");
		stereoType = type;
	}

	public void display(GLAutoDrawable arg0){
		display(arg0, component.getWidth(), component.getHeight());
		//		display(arg0, width, height);
	}



	//	int width, height;

	CubeMap skyboxCubemap;
	PerformanceMeter perfmeter = new PerformanceMeter();

	//TODO transparancy
	// - write shaders
	// - extract transparent and nontransparent objects from scene graph into seperate lists
	// - do this in a way to allow deferred shading later
	// - do what is being done in the example DepthPeeling application
	// - think about sensible way of dealing with the occlusion query
	BufferedImage dst = null;
	boolean offscreen = false;
	int textureDeletionCounter = 0;
	RenderableUnitCollection RUC = null;
	public PostProcessingHelper postProcessor = null;
	public void display(GLAutoDrawable arg0, int width, int height) {

		perfmeter.beginFrame();

		if(offscreen){ // note padilla: at the end of this call we also call if(offscreen) with the same commands. Is this needed?
			TransparencyHelper.setSupersample((int)aa);
			TransparencyHelper.resizeFramebufferTextures(arg0.getGL().getGL3(), width, height);
		}
		//System.out.println("display called---------------------------------");
		if(arg0.getGL() != null && arg0.getGL().getGL3() != null){

			//			double[] aMatrix = new double[16];
			//			this.camPath.getMatrix(aMatrix);
			double[] mat = new double[16];
			Camera cam = (Camera)(this.camPath.getLastElement());
			double ar = ((double) arg0.getSurfaceWidth())/arg0.getSurfaceHeight();
			//ar = width/height;
			mat = CameraUtility.getCameraToNDC(cam, ar);
			//	        P3.makePerspectiveProjectionMatrix(mat, CameraUtility.getViewport(cam, 1), (float)cam.getNear(), (float)cam.getFar());
			double[] dmat = new double[16];
			this.camPath.getInverseMatrix(dmat);
			//	        this.camPath.getMatrix(dmat);
			//Rn.times(dmat, mat, dmat);


			GL3 gl = arg0.getGL().getGL3();

			//handle background and RenderableUnitsCollection state
			JOGLSceneGraphComponentInstance rootInstance = (JOGLSceneGraphComponentInstance) proxyScene.getTreeRoot();
			JOGLAppearanceInstance rootApInst = (JOGLAppearanceInstance)rootInstance.getAppearanceTreeNode();




			if(!((JOGLAppearanceEntity)rootApInst.getEntity()).dataUpToDate){
				Appearance rootAp = (Appearance) rootApInst.getEntity().getNode();
				backgroundHelper.updateBackground(gl, rootAp, width, height);

				Object bgo = null;
				if (rootAp != null)
					bgo = rootAp.getAttribute(CommonAttributes.SMALL_OBJ_OPTIMIZATION);
				if (bgo != null && bgo instanceof Boolean)
					RUC.setActive((Boolean) bgo);
				else{
					RUC.setActive(CommonAttributes.SMALL_OBJ_OPTIMIZATION_DEFAULT);
				}

				bgo = null;
				if (rootAp != null)
					bgo = rootAp.getAttribute(CommonAttributes.STEREOGRAM_RENDERING);
				if (bgo != null && bgo instanceof Boolean && (Boolean)bgo == true)
					TransparencyHelper.setUpStereogramTexture(gl, rootAp);
				else{
					TransparencyHelper.noStereogramRender();
				}


				// This is where the display function reads the ANTIALIASING attributes and prepares the framebuffer for it
				bgo = null;
				boolean enabled;
				if (rootAp != null)
					bgo = rootAp.getAttribute(CommonAttributes.ANTIALIASING_ENABLED);
				if (bgo != null && bgo instanceof Boolean)
					enabled = (Boolean) bgo;
				else{
					enabled = CommonAttributes.ANTIALIASING_ENABLED_DEFAULT;
				}
				if(enabled){
					int factor;
					if (rootAp != null)
						bgo = rootAp.getAttribute(CommonAttributes.ANTI_ALIASING_FACTOR);
					if (bgo != null && bgo instanceof Integer)
						factor = (Integer) bgo;
					else{
						factor = CommonAttributes.ANTI_ALIASING_FACTOR_DEFAULT;
					}
					if(factor != supersample){
						supersample = factor; // copy over to the hardcoded variable of this class Q: for what?
						TransparencyHelper.setSupersample(factor);
						TransparencyHelper.resizeFramebufferTextures(arg0.getGL().getGL3(), width, height);
					}
				}else{
					if(supersample != 1){ // if ==1, then we actually don't sample anything
						//reset
						supersample = 1;
						TransparencyHelper.setSupersample(supersample);
						TransparencyHelper.resizeFramebufferTextures(arg0.getGL().getGL3(), width, height);
					}
				}

				// Backface culling
				if (rootAp != null) 	bgo = rootAp.getAttribute(CommonAttributes.BACK_FACE_CULLING_ENABLED);
				if (bgo != null && bgo instanceof Boolean){TransparencyHelper.setBackFaceCulling((Boolean) bgo);}
				



			}// endif !dataUpToDate



			//update sky box
			rootApInst = (JOGLAppearanceInstance)rootInstance.getAppearanceTreeNode();
			if(!((JOGLAppearanceEntity)rootApInst.getEntity()).dataUpToDate){
				//System.out.println("cube map not upToDate");
				Appearance rootAp = (Appearance) rootApInst.getEntity().getNode();
				if (AttributeEntityUtility.hasAttributeEntity(CubeMap.class,SKY_BOX, rootAp)) {
					skyboxCubemap = (CubeMap) AttributeEntityUtility.createAttributeEntity(CubeMap.class, SKY_BOX, rootAp, true);
				}else{
					skyboxCubemap = null;
				}
				Texture2DLoader.deleteTextures(gl);
			}

			// april 2015 padilla: fixed the skybox motion by removing the translation of dmat
			double[] viewMat = P3.extractOrientationMatrix(null, dmat,P3.originP3, Pn.EUCLIDEAN);
			//prepare skybox for render
			SkyboxHelper.setup(viewMat, mat, skyboxCubemap, cam);
			//			before: SkyboxHelper.setup(dmat, mat, skyboxCubemap, cam);



			//collect lights from scene graph
			JOGLLightCollection globalLightCollection = new JOGLLightCollection(dmat);
			rootInstance.collectGlobalLights(dmat, globalLightCollection, proxyScene.lightsChanged);
			proxyScene.lightsChanged = false;
			//can load global lights texture here.
			lightHelper.loadGlobalLightTexture(globalLightCollection, gl);




			//calculate window dimensions and such needed for sprite size calculation
			Rectangle2D r = CameraUtility.getViewport(cam, ar);
			float x = (float)(r.getMaxX()-r.getMinX());
			float y = (float)(r.getMaxY()-r.getMinY());

			// create the render state helping object
			JOGLRenderState rootState = new JOGLRenderState(gl, dmat, mat, lightHelper, tubeHelper, sphereHelper, Math.min(component.getWidth(), component.getHeight()), Math.min(x, y));


			//extract nontransparent objects
			//List<RenderableObject> nonTranspObjects = new LinkedList<RenderableObject>();
			RUC.resetRestNonTranspObjects();
			List<RenderableObject> transpObjects = new LinkedList<RenderableObject>();
			rootInstance.collectTranspAndNonTransparent(rootState, RUC, transpObjects);

			//			System.out.println("Size of transparent object list = " + transpObjects.size()); // april15: basic vr demo: 2 objects. 1 if transparency of faces disabled

			infodata.clockrate = this.getClockRate();
			infodata.framerate = this.getFrameRate();
			infodata.polygoncount = this.getPolygonCount();

			
			//render scene graph
			if(offscreen == false){
				//				System.out.println("transparencyhelper.render call"); // april2k15: VR demo calls it
				TransparencyHelper.render(infodata, gl, RUC, transpObjects, width, height, backgroundHelper);
				//				ImprovedTransparencyHelper.render( gl, null, transpObjects, width, height, backgroundHelper);
			}else{
				this.dst = TransparencyHelper.renderOffscreen(aa, this.dst, gl, RUC, transpObjects, width, height, backgroundHelper);
				System.out.println("rendering offscreen");
			}



			rootInstance.setAppearanceEntitiesUpToDate();

			textureDeletionCounter++;
			if(textureDeletionCounter == 10){
				textureDeletionCounter = 0;
				Texture2DLoader.deleteTextures(gl);
			}

		} // endif GL3 not null


		if(offscreen){
			TransparencyHelper.setSupersample(supersample);
			TransparencyHelper.resizeFramebufferTextures(arg0.getGL().getGL3(), width, height);
		}

		// Post processing July2015
		if(postProcessor != null && postProcessor.gl==null){
			//complete it. The beginning was called in ViewMenuBar.java
			postProcessor.addGL(arg0.getGL().getGL3());
			postProcessor.readConfig2AddFxaa(proxyScene, postProcessor);
		}
		if(postProcessor != null && !postProcessor.initialized){
			//reactivate it (usually after jogl3.dispose() )
			postProcessor.reactivate(this, arg0.getGL().getGL3());
		}
		if(postProcessor != null && postProcessor.initialized){
			//use it
			postProcessor.render(width, height);
		}


		perfmeter.endFrame(); // performance meter
	}
	LightHelper lightHelper;
	BackgroundHelper backgroundHelper;
	TubeHelper tubeHelper;
	SphereHelper sphereHelper;
	public void dispose(GLAutoDrawable arg0) {
//		System.out.println("calling JOGL3Viewer.dispose");
		postProcessor.deactivate();
		//		postProcessor=null;
	}

	InfoOverlayData infodata = new InfoOverlayData();

	private int width, height = 0;
	public void init(GLAutoDrawable arg0) {


		if (proxyScene != null) proxyScene.dispose();
		proxyScene = new JOGLSceneGraph(sceneRoot);
		proxyScene.createProxyTree();

//		System.out.println("init!!!!!!!!!!!!");

		GL3 gl = arg0.getGL().getGL3();

		//initialization for depth peeling
		width = arg0.getSurfaceWidth();
		height = arg0.getSurfaceHeight();
		TransparencyHelper.initTransparency(gl, width, height);

		//		int[] arr = new int[1];
		//		gl.glGetIntegerv(gl.GL_SAMPLE_BUFFERS, arr, 0);
		//		System.out.println(arr[0]);
		//		gl.glEnable(gl.GL_MULTISAMPLE);
		//		gl.glGetIntegerv(gl.GL_SAMPLE_BUFFERS, arr, 0);
		//		System.out.println(arr[0]);


		int buf[] = new int[1];
		int sbuf[] = new int[1];

		//gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
		//gl.glEnable(gl.GL_BLEND);
		//gl.glBlendEquation(gl.GL_FUNC_ADD);
		//gl.glBlendFunc(gl.GL_SRC_ALPHA, gl.GL_ONE_MINUS_SRC_ALPHA);
		//initialize shaders once

		GLShader.initDefaultShaders(gl);
		PointShader.init(gl);
		//skybox = new JOGLSkybox();
		SkyboxHelper.init(gl);
		//initialize vbo once

		//create ligth texture
		lightHelper = new LightHelper();
		lightHelper.initGlobalLightTexture(gl);
		lightHelper.initLocalLightTexture(gl);
		LabelShader.init(gl);
		backgroundHelper = new BackgroundHelper();
		backgroundHelper.initializeBackground(gl);
		tubeHelper = new TubeHelper();
		sphereHelper = new SphereHelper();
		RUC = new RenderableUnitCollection();
	}

	public void reshape(GLAutoDrawable arg0, int arg1, int arg2, int arg3,
			int arg4) {
//		System.out.println("reshape");
		this.width = arg3;
		this.height = arg4;
		TransparencyHelper.setSupersample(supersample);
		TransparencyHelper.resizeFramebufferTextures(arg0.getGL().getGL3(), width, height);

	}

	@Override
	public double getClockRate() {
		return perfmeter.getClockrate();
	}

	@Override
	public double getFrameRate() {
		return perfmeter.getFramerate();
	}

	@Override
	public int getPolygonCount() {
		return 0;
	}

	@Override
	public void addGLEventListener(GLEventListener e) {
		canvas.addGLEventListener(e);
	}




	public BufferedImage renderOffscreen(int w, int h) {
		return renderOffscreen(null, w, h);
	}

	public BufferedImage renderOffscreen(BufferedImage dst, int w, int h) {
		return renderOffscreen(dst, w, h, 1.0);
	}

	public BufferedImage renderOffscreen(int w, int h, double aa) {
		return renderOffscreen(null, w, h, aa);
	}
	double aa = 1;
	//aa stands for Anti-Aliasing
	public BufferedImage renderOffscreen(BufferedImage dst, int w, int h,
			double aa) {
		this.width = w;
		this.height = h;
		this.aa = aa;
		this.dst = dst;
		offscreen = true; // render once offscreen.
		canvas.display();
		offscreen = false;
		return this.dst;
	}

	@Override
	public void installOverlay() {
		// TODO Auto-generated method stub
		infodata.activated = true;
	}

	@Override
	public void uninstallOverlay() {
		// TODO Auto-generated method stub
		infodata.activated = false;
	}

}
