package de.jreality.tutorial.tool;

import java.util.ArrayList;
import java.util.List;

import de.jreality.geometry.IndexedLineSetFactory;
import de.jreality.math.Matrix;
import de.jreality.math.Rn;
import de.jreality.plugin.JRViewer;
import de.jreality.scene.Appearance;
import de.jreality.scene.Geometry;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.scene.pick.PickResult;
import de.jreality.scene.tool.AbstractTool;
import de.jreality.scene.tool.InputSlot;
import de.jreality.scene.tool.ToolContext;
import de.jreality.shader.Color;
import de.jreality.shader.DefaultGeometryShader;
import de.jreality.shader.DefaultLineShader;
import de.jreality.shader.DefaultPointShader;
import de.jreality.shader.ShaderUtility;
import de.jreality.tools.DragEventTool;
import de.jreality.tools.PointDragEvent;
import de.jreality.tools.PointDragListener;
import de.jreality.toolsystem.ToolUtility;

public class AddPointsAndDragPointsExample extends AbstractTool {

   static List<double[]> points = new ArrayList<double[]>();
   static IndexedLineSetFactory lsf = new IndexedLineSetFactory();
   
   double offset = 5;
   
   public AddPointsAndDragPointsExample() {
      addCurrentSlot(InputSlot.SHIFT_LEFT_BUTTON, "add a new point");
      updateGeometry();
   }
   
   public static void pa(double[] ar, String msg)
   {
      System.out.println(msg + "--------------------");
      for(int i=0; i < ar.length; i++)
         System.out.print(ar[i]+" ");
      System.out.println();
   }

   @Override
   public void perform(ToolContext tc) {
      if (!tc.getAxisState(InputSlot.SHIFT_LEFT_BUTTON).isPressed()) return;
      
      int idx = -1;
      if(tc.getCurrentPick() != null && tc.getCurrentPick().getPickType() == PickResult.PICK_TYPE_LINE)
      {
         idx = tc.getCurrentPick().getIndex() + 1;
      }
      
      // determine the pointer transformation:
      // translation is the mouse pointer on the near clipping plane
      // z-axis is the direction of the mouse ray out of the screen
      // for a 6DOF input device, it is the position/orientation of the device
      Matrix m = new Matrix(tc.getTransformationMatrix(InputSlot.POINTER_TRANSFORMATION));
            
      // we compute the coordinates of the new point in world coordinates
      double[] foot = m.getColumn(3);
      double[] dir = m.getColumn(2);
      double[] offset = Rn.times(null, -5, dir);
      double[] newPoint = Rn.add(null, foot, offset);
      
      newPoint = ToolUtility.worldToLocal(tc, newPoint);
      
      if(idx == -1)
         points.add(newPoint);
      else
         points.add(idx, newPoint);
      updateGeometry();
   }

   private void updateGeometry() {
      int n = points.size();
      
      for(int i=0;i<n;i++){
    	  points.get(i)[3]=1.;
      }
      
      // set new vertices
      lsf.setVertexCount(n);
      if (n>0)
      {
         lsf.setVertexCoordinates(points.toArray(new double[0][]));
      }
      if (n>1) {
         // compute and set new edge indices:
         int[][]  idx = new int[n-1][2];
         for (int i=1; i<n; i++) {
            idx[i-1][0] = i-1;
            idx[i-1][1] = i;
         }
   
         lsf.setEdgeCount(n-1);
         lsf.setEdgeIndices(idx);
      }      
      lsf.update();
   }
   
   public Geometry getGeometry(){
	   return lsf.getGeometry();
   }
   
   private static void setupAppearance(Appearance ap) {
      DefaultGeometryShader dgs;
      DefaultLineShader dls;
      DefaultPointShader dpts;
      dgs = ShaderUtility.createDefaultGeometryShader(ap, true);
      dgs.setShowFaces(true);
      dgs.setShowLines(true);
      dgs.setShowPoints(true);
      dls = (DefaultLineShader) dgs.createLineShader("default");
      dls.setDiffuseColor(Color.yellow);
      dls.setTubeRadius(.03);
      dpts = (DefaultPointShader) dgs.createPointShader("default");
      dpts.setDiffuseColor(Color.red);
      dpts.setPointRadius(.05);
   }

public static void main(String[] args) {

      final AddPointsAndDragPointsExample example = new AddPointsAndDragPointsExample();
      
      SceneGraphComponent cmp = new SceneGraphComponent();
      cmp.setGeometry(example.getGeometry());
      cmp.addTool(example);
      
      Appearance ap = new Appearance();
      cmp.setAppearance(ap);
      setupAppearance(ap);
      
      DragEventTool t = new DragEventTool();
      t.addPointDragListener(new PointDragListener() {

         public void pointDragStart(PointDragEvent e) {
            System.out.println("drag start of vertex no "+e.getIndex());  
         }

		@Override
		public void pointDragEnd(PointDragEvent e) {
		}

		@Override
		public void pointDragged(PointDragEvent e) {
	        points.set(e.getIndex(), e.getPosition());  
	        example.updateGeometry();
		}
		
      });
      
      cmp.addTool(t);
      
      
      JRViewer.display(cmp);
   }
   
}