package de.jreality.jogl3.optimization;

import java.util.LinkedList;
import java.util.List;
import java.util.WeakHashMap;

import com.jogamp.opengl.GL3;

import de.jreality.jogl3.GlTexture;
import de.jreality.jogl3.JOGLSceneGraphComponentInstance.RenderableObject;
import de.jreality.jogl3.geom.GlReflectionMap;
import de.jreality.jogl3.geom.JOGLFaceSetEntity;
import de.jreality.jogl3.geom.JOGLFaceSetInstance;
import de.jreality.jogl3.glsl.GLShader;

/**
 * is activated or deactivated by: CommonAttributes.SMALL_OBJ_OPTIMIZATION.
 * it uses  new OptimizedGLShader("../glsl/" + shader.getVertFilename(), "../glsl/" + shader.getFragFilename()
 * Why? Because GL3 is too slow at rendering many tiny objects separately. So we combine many renderable objects to one.
 * All these hashmaps here allow us to "sort" everything to switch shaders etc. less often. 
 * comment: padilla
 *
 */
public class RenderableUnitCollection{
	
	public final int MAX_NUM_FLOATS = 100000;
	
	private boolean active = true;
	
	public void setActive(boolean val){
//		System.out.println("SETTING ACTIVE TO " + val);
		active = val;
	}
	
	public void resetRestNonTranspObjects(){
		restNonTranspObjects = new LinkedList<RenderableObject>();
	}
	
	public List<RenderableObject> restNonTranspObjects = new LinkedList<RenderableObject>();
	
	// combine Shader, Texture, ReflectionMap, RenderableUnit
	WeakHashMap<GLShader, WeakHashMap<GlTexture, WeakHashMap<GlReflectionMap, RenderableUnit>>> units = new WeakHashMap<GLShader,WeakHashMap<GlTexture,WeakHashMap<GlReflectionMap,RenderableUnit>>>();
	
	private GlTexture nullTex = new GlTexture();
	private GlReflectionMap nullReflMap = new GlReflectionMap();
	
	public void add(RenderableObject o){
//		System.out.println(o.geom.getClass());
		
		if(o.geom instanceof JOGLFaceSetInstance){ // if the GeometryInstance of the object was a FaceSetInstance
			JOGLFaceSetInstance f = (JOGLFaceSetInstance)o.geom;
			JOGLFaceSetEntity fse = (JOGLFaceSetEntity) f.getEntity();
//			System.out.println("Length = " + fse.getAllVBOs()[0].getLength());
//			System.out.println(f.ifd.drawLabels);
			if((!f.ifd.drawLabels || f.labelData.tex == null) && fse.getAllVBOs()[0].getLength() <= MAX_NUM_FLOATS && active && !f.getEdgeDraw() && !f.getVertexDraw()){
//				System.out.println("adding to renderableUnit");
				
				GlTexture tex = f.faceTexture;
				if(!tex.hasTexture())
					tex = nullTex;
				
				GLShader shader = f.getPolygonShader();
				GlReflectionMap reflMap = f.reflMap;
				if(!reflMap.hasReflMap())
					reflMap = nullReflMap;
				
				WeakHashMap<GlTexture, WeakHashMap<GlReflectionMap, RenderableUnit>> hm1 = units.get(shader);
				if(hm1 == null){
//					System.out.println("new shader forces new RenderableUnit");
					hm1 = new WeakHashMap<GlTexture, WeakHashMap<GlReflectionMap,RenderableUnit>>();
					units.put(shader, hm1);
				}
				//hm1 now usable
//				System.err.println("tex is " + tex);
				WeakHashMap<GlReflectionMap, RenderableUnit> hm2 = hm1.get(tex);
				if(hm2 == null){
//					System.out.println("new texture forces new RenderableUnit");
					hm2 = new WeakHashMap<GlReflectionMap,RenderableUnit>();
					hm1.put(tex, hm2);
				}
				//hm2 now usable
				RenderableUnit ru = hm2.get(reflMap);
				if(ru == null){
//					System.out.println("new reflection map forces new RenderableUnit");
					ru = new RenderableUnit(tex, new OptimizedGLShader("../glsl/" + shader.getVertFilename(), "../glsl/" + shader.getFragFilename()), reflMap);
//					ru = new RenderableUnit(tex, new GLShader("nontransp/Cpolygon.v", "nontransp/Cpolygon.f"), reflMap);
					hm2.put(reflMap, ru);
				}
				ru.register(o);
			}else{
//				System.out.println("adding to rest, because big enough");
				restNonTranspObjects.add(o);
			}
		}else{
//			System.out.println("adding to rest, because no face set");
			restNonTranspObjects.add(o);
		}
	}
	
	public void render(GL3 gl, int width, int height){
//		int i=0,j=0; // for syso
		for(GLShader t : units.keySet()){
			for(GlTexture tex : units.get(t).keySet()){
				for(GlReflectionMap r : units.get(t).get(tex).keySet()){
					// use all the hashmaps
					units.get(t).get(tex).get(r).update(gl);
					units.get(t).get(tex).get(r).render();
					//					i++;

				}
			}
		}
		for(RenderableObject o : restNonTranspObjects){
			o.render(width, height);
//			j++;
		}
		
//		System.out.println("nr of renderunit.render() calls = " + i);
//		System.out.println("nr of renderobject.render() calls = " + j);
//		  april 2015: in basic VR support with transparent faces, only 0 was printed, but when the 
	}
	
}
