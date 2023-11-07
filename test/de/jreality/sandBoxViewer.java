
package de.jreality;

import de.jreality.geometry.Primitives;
import de.jreality.geometry.SphereUtility;
import de.jreality.plugin.JRViewer;
import de.jreality.plugin.JRViewer.ContentType;
import de.jreality.plugin.basic.Scene;
import de.jreality.plugin.content.ContentAppearance;
import de.jreality.plugin.content.ContentLoader;
import de.jreality.plugin.content.ContentTools;
import de.jreality.scene.Appearance;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.Color;
import de.jreality.shader.CommonAttributes;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPolygonShader;
import de.jreality.shader.ImplodePolygonShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.shader.TwoSidePolygonShader;
import de.jreality.util.SceneGraphUtility;


public class sandBoxViewer {

	
	public static void main(String[] args) {
		
//		System.out.println("=============================================");
//		System.out.println("===========Sand Box Viewer Startup ==========");
//		System.out.println("=============================================");
//		
//		// customize a JRViewer to have Virtual Reality support (skyboxes, terrain, etc)
		JRViewer v = new JRViewer();
		v.addBasicUI();
		v.addVRSupport();
		v.addContentSupport(ContentType.TerrainAligned);
		v.registerPlugin(new ContentAppearance());
		v.registerPlugin(new ContentLoader());
		v.registerPlugin(new ContentTools());
		
		
		Scene scene = v.getPlugin(Scene.class);	 
//	scene.getRootAppearance().setAttribute(CommonAttributes.ANTIALIASING_ENABLED, false);
//		scene.getRootAppearance().setAttribute(CommonAttributes.ANTI_ALIASING_FACTOR, 2);
		scene.getRootAppearance().setAttribute(CommonAttributes.ANTIALIASING_FXAA_ENABLED, true);
		scene.getRootAppearance().setAttribute(CommonAttributes.ANTI_ALIASING_FXAA_FACTOR, 1);		
		
		
		
//		//box with textures
//		final double [][] vertices  = new double[][] {
//				//The 8 vertices of the cube
//				{0,  0,  0}, {1,  0,  0}, {1,  1,  0}, {0,  1,  0}, //0-3
//				{0,  0,  1}, {1,  0,  1}, {1,  1,  1}, {0,  1,  1}, //4-7
//			};
//			//The 6 faces of the "wrapped" cube in space
//			final int [][] indices = new int [][] {
//				{ 0, 1, 2, 3 }, { 7, 6, 5, 4 }, { 0, 1, 5, 4 }, 
//				{ 1, 2, 6, 5 }, { 2, 3, 7, 6 }, { 3, 0, 4, 7 } 
//			};
//			//The 6 faces of the usual unwrapping of the cube 
//			final int [][] unwrapIndices = new int [][] {
//				//The first 3 faces are connected to each other as on the cube,
//				//so their indices stay the same
//				{ 0, 1, 2, 3 }, { 7, 6, 5, 4 }, { 0, 1, 5, 4 },
//				//The following 3 faces are not connected
//				//to some of the faces they are connected on the cube.
//				//This is done using vertex indices 8-13.
//				{ 1, 8, 9, 5 }, { 8, 10, 11, 9 }, { 12, 0, 4, 13 }
//			};
//			//The texture coordinates of the vertices. Make a sketch of them and
//			//you will see the unwrapped cube
//			final double [][] unwrapTextureCoordinates = new double[][] {
//				{ .25, .5},{ .5, .5},{ .5, .75},{ .25, .75}, //0-3
//				{ .25, .25 },{ .5, .25 },{ .5, .0 },{ .25, .0 },//4-7
//				{ .75, .5 },{ .75, .25 },{ 1., .5 },{ 1., .25 },{ 0., .5 },{ 0., .25 },//8-13
//			};
//		
//			final IndexedFaceSetFactory ifsf = new IndexedFaceSetFactory();
//			
//			// set all 14 vertices and their coordinates
//			ifsf.setVertexCount( 14 );
//			// We need to unwrap the vertex coordinates for the 14 vertices
//			// This is done by unwrapVertexAttributes, which determines the correspondence of vertices
//			// from correspondence in indices and unwrapIndices. For the cube:
//			// 8->2, 9->6, 10->3, 11->7, 12 -> 3, 13 -> 7	 
//	 
//			ifsf.setVertexCoordinates( IndexedFaceSetFactory.unwrapVertexAttributes(vertices, indices, unwrapIndices, 14));
//			ifsf.setVertexTextureCoordinates(unwrapTextureCoordinates);
//			
//			// The 6 faces of the cube, use the "wrapped" face indices
//			ifsf.setFaceCount( 6 );	
//			ifsf.setFaceIndices( indices );
//	 
//			// Generation of vertexNormals and edges is according to the wrapped indices rather than
//			// the unwrapped
//			ifsf.setGenerateVertexNormals( true );
//			ifsf.setGenerateEdgesFromFaces( true );
//			// The edge labels indicate that edges are doubled only in mode 2 below
//			ifsf.setGenerateEdgeLabels(true);
//			// Crude documentation 
//			ifsf.setVertexLabels(new String[]{"","","","","","    Klick with middle mouse","","","","","","","",""});
//			ifsf.update();
//			
//			SceneGraphComponent sgc = new SceneGraphComponent("scene");
//			
//			/* An action tool that cycles through 3 modes when the cube 
//			 * is clicked with the middle mouse button 
//			 * mode0: the texture cube with texture jumps, because no unwrapped indices are set
//			 * mode1: the nicely textured unwrapped cube
//			 * mode3: also nicely textured unwrapped cube, but now edges are doubled 
//			 * and broken vertex normals
//			 */
//			ActionTool tool = new ActionTool("PrimaryMenu");
//			tool.addActionListener(new ActionListener(){
//				int mode=0;
//				public void actionPerformed(ActionEvent e) {
//					mode = (mode +1) % 3;
//					if (mode==0) {
//						ifsf.setFaceIndices( indices ); 
//						ifsf.setUnwrapFaceIndices((int[][]) null); 
//						ifsf.setVertexLabels(new String[]{"","","","","","    NO unwrapped face indices","","","","","","","",""});
//					}
//					if (mode==1) {
//						ifsf.setFaceIndices( indices ); 
//						ifsf.setUnwrapFaceIndices( unwrapIndices ); 
//						ifsf.setVertexLabels(new String[]{"","","","","","    wrapped and UNWRAPPED face indices","","","","","","","",""});
//						}
//					if (mode==2) {
//						ifsf.setFaceIndices( unwrapIndices ); 
//						ifsf.setUnwrapFaceIndices( (int[][]) null ); 
//						ifsf.setVertexLabels(new String[]{"","","","","","    DOUBLED edges and BROKEN vertex normals, unwrapped indices","","","","","","","",""});
//						}
//					ifsf.update();
//				}
//			});
//			sgc.addTool(tool);
//			sgc.setGeometry(ifsf.getIndexedFaceSet());
//				
//			// Add the texture
//			sgc.setAppearance(new Appearance());
//			Texture2D tex;
//			try{
//				tex=TextureUtility.createTexture(
//					sgc.getAppearance(),       
//					"polygonShader", 
//					ImageData.load(Input.getInput("de/jreality/tutorial/geom/black_cross.png")),
//					false);
//				tex.setTextureMatrix(MatrixBuilder.euclidean().scale(250).getMatrix());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//			
//			// scale the cube in order to have relatively smaller labels
//			MatrixBuilder.euclidean().scale(4).assignTo(sgc);
	
//			v.registerPlugin(new JRViewer.ContentInjectionPlugin(sgc, true));
		
		// two side polygon example
		SceneGraphComponent world = SceneGraphUtility.createFullSceneGraphComponent("world");
		world.setGeometry(SphereUtility.sphericalPatch(0, 0.0, 180.0, 90.0, 40, 40, 1.0));
		Appearance ap = world.getAppearance();
		DefaultGeometryShader dgs = (DefaultGeometryShader) 
   			ShaderUtility.createDefaultGeometryShader(ap, true);
		dgs.setShowLines(true);
		dgs.setShowPoints(false);
		// A non-default shader has to be set using the following method call
		TwoSidePolygonShader tsps = (TwoSidePolygonShader) dgs.createPolygonShader("twoSide");
		// We use another non-default shader for the front surface of the two-sided shader
		ImplodePolygonShader dps = (ImplodePolygonShader) tsps.createFront("implode");
//		DefaultPolygonShader dps = (DefaultPolygonShader) tsps.createBack("default");
		DefaultPolygonShader dps2 = (DefaultPolygonShader) tsps.createBack("default");
		// but the attributes can be set directly using the Appearance.setAttribute() method
		ap.setAttribute("polygonShader.implodeFactor", .5);
		dps.setDiffuseColor(new Color(0,204,204));
		dps2.setDiffuseColor(new Color(204,204,0));
		DefaultLineShader dls = (DefaultLineShader) dgs.getLineShader();
		dls.setTubeDraw(true);
		dls.setTubeRadius(.05);
		dls.setDiffuseColor(Color.MAGENTA);
//		v.registerPlugin(new JRViewer.ContentInjectionPlugin(world, true));
//		JRViewer.display(world);
		
		
//			v.setContent(Primitives.icosahedron());
			v.setContent(Primitives.torus(15, 5, 50, 10));
			v.startup();
			
		
	}

}
