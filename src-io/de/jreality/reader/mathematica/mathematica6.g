//**************************************************
// * Mathematica6 Parser
// */


header {
/**
* this code is generated by ANTLR from the 'mathematica6.g'-file
* @author Bernd Gonska
* @version 1.0
*/
package de.jreality.reader.mathematica;

import java.util.*;
import java.util.List;
import java.util.ArrayList;
import de.jreality.geometry.*;
import de.jreality.scene.data.*;
import de.jreality.scene.*;
import de.jreality.scene.SceneGraphComponent;
import de.jreality.shader.Color;
import de.jreality.util.LoggingSystem;
import java.util.logging.Level;
import java.util.logging.Logger;
}

class Mathematica6Parser extends Parser;
options {
	k = 3;							// three token lookahead
}

{
/**
* wandelt Graphic3D- Objekte die aus Mathematica mittels 
* 			<Graphics3D -Object> >> <Pfad>\ <fileName.m>
* herausgeschrieben wurden in einen SceneGraphen von JReality um.
*
* Die Daten haben im file folgende Form(sind uebrigens direkter Mathematica-Text):
* Graphics3D[ {} , {} ]
* in der ersten Klammer steht ein "{..}"-Klammer Baum von gemischten Listen aus 
* graphischen Objekten und graphischen Directiven( wie Farben ect.)
* Bsp: Graphics3D[ 		{o1,o2,d1,{d2,o3,{o4},d3,o5}} 			,{} ]   
*					(d1-d3 Directiven , o1-o5 graphische Objekte)
* in der 2. Klammer steht eine Liste von Optionen die die ganze Scene beinflussen sollen.
* Bsp: Graphics3D[ 		{o1,o2,d1,{d2,o3,{o4},d3,o5}} 			,{Op1,Opt2,..} ]
*
* Vorgehen: 
* erste Klammer:
*		Klammern werden zu SceneGraphComponents die den Inhalt als Unterbaeume tragen.
* 		die graphischen Objekte werden je zu einer SceneGr.C. mit entsprechender Geometrie
*		die graphischen Directiven werden in der jeweils betroffenen Geometrie gesetzt(Farben)
*				manche Directiven werden ueberlesen!!!
* zweite Klammer:
*		alle Optionen werden ueberlesen!!!
*
* Problem: manchmal werden Coordinaten so ausgerechnet das sie noch einen 
*	 Imaginaerteil haben ( meist = 0) der noch dahinter steht
*	Loesung :ein + ...*I ignorieren!
*			   Doubles werden ja immer durch "," getrennt! Also geht das!
*/

	// this is what is returned from the parsing process
	private SceneGraphComponent root = new SceneGraphComponent();	
	private SceneGraphComponent current = root;		// aktuell zu erweiternder Knoten 
	private MState initialState=new MState();
	private Logger log = LoggingSystem.getLogger(Mathematica6Parser.class);
	private Appearance startApp =new Appearance();

// -------------------------------- default Lights ----------------------------
	public static SceneGraphComponent getDefaultLightNode (){
		return	MathematicaHelper.getDefaultLightNode();
	} 

/**
* konstructs a parser who can translate a
* mathematica-file to the corresponding SceneGraph
* @param    see superclass
* example: Mathematica6Parser p=
*	    new Mathematica6Parser(new Mathematica6Lexer(
*	     new FileReader(new File("file.m"))));
*/
}


// ------------------------------------------------------------------------------
// -------------------------------- Parser --------------------------------------
// ------------------------------------------------------------------------------
/**
* starts the parsing Process
* @param none sourcefile set by creating the object
* @returns SceneGraphComponent root of generated scene
*/
start returns [SceneGraphComponent r=new SceneGraphComponent()]
{
	root.setName("Mathematica6");
	log.setLevel(Level.FINE);	
}
	:
	( graphics3D
	 |OPEN_BRACE listOfGraphics3D CLOSE_BRACE
	)
	{r=root;}
	;

private
listOfGraphics3D
{System.out.println("listOfGraphics3D");}
	:(	
		( graphics3D
		 |OPEN_BRACE listOfGraphics3D CLOSE_BRACE
		)
	 	(COLON listOfGraphics3D)?
	 )?
	;

private
graphics3D
{System.out.println("graphics3D");}
	:"Graphics3D"
	OPEN_BRACKET  
		{					
			// neuen Knoten erstellen der die Listenelemente enthaelt, 
			SceneGraphComponent newPart = new SceneGraphComponent();
			newPart.setName("Graphic");
			SceneGraphComponent oldPart = current;
			current.addChild(newPart);
			current=newPart;
			MState state2=initialState.copy();
		}		 
	(
		OPEN_BRACE objectListG3D[state2] CLOSE_BRACE (COLON waste)?
		| objectListG3D[state2]
	)?
	CLOSE_BRACKET 
		{current=oldPart;
  		System.out.println("done");
  		}		 
	; 

private
objectListG3D [MState state]
{ MState state2=state.copy(); 
  System.out.println("objectList");
}
// abarbeiten einer Abfolge von 3d-Objecten(und Directiven)
	:(
		  listG3D[state2]		// Listen koennen Listen enthalten
		| objectG3D[state2]
	 )
	 ( COLON 
	 	(
		   listG3D[state2]
		 | objectG3D[state2]
		)
	 )*
	;	
private
objectListGC [MState state]
{ MState state2=state.copy(); 
  System.out.println("objectList");
}
// abarbeiten einer Abfolge von 3d-Objecten(und Directiven)
	:(
		  listGC[state2]		// Listen koennen Listen enthalten
		| objectGC[state2]
	 )
	 ( COLON 
	 	(
		   listGC[state2]
		 | objectGC[state2]
		)
	 )*
	;	

private
listG3D[MState state]
{
 System.out.println("list");
 MState state2=state.copy(); 
}
	// eine Klammer mit mglw. einer Abfolge von 3d-Objekten
	:	OPEN_BRACE						
			{						
			// neuen Knoten erstellen der die Listenelemente haelt, 
			SceneGraphComponent newPart = new SceneGraphComponent();
			newPart.setName("Object");
			SceneGraphComponent oldPart = current;
			current.addChild(newPart);
			current=newPart;
			}
	        (objectListG3D[state2])?	// das innere der Klammer einhaengen
	    CLOSE_BRACE
			{
				current=oldPart;
			}
	;
private
listGC[MState state]
{
 System.out.println("list");
 MState state2=state.copy(); 
}
	// eine Klammer mit mglw. einer Abfolge von 3d-Objekten
	:	OPEN_BRACE						
			{						
			// neuen Knoten erstellen der die Listenelemente haelt, 
			SceneGraphComponent newPart = new SceneGraphComponent();
			newPart.setName("Object");
			SceneGraphComponent oldPart = current;
			current.addChild(newPart);
			current=newPart;
			}
	        (objectListGC[state2])?	// das innere der Klammer einhaengen
	    CLOSE_BRACE
			{
				current=oldPart;
			}
	;

	
// ---------------------------------- 3D Objects ---------------------------------------------
private
graphGroupG3D [MState state]
{
 MState state2=state.copy();
 System.out.println("graphGroup");
}
	:	"GraphicsGroup" 
		OPEN_BRACKET 
		(objectListG3D[state2])?
		CLOSE_BRACKET 
	;
private
graphGroupGC [MState state]
{
 MState state2=state.copy();
 System.out.println("graphGroup");
}
	:	"GraphicsGroup" 
		OPEN_BRACKET 
		(objectListGC[state2])?
		CLOSE_BRACKET 
	;
	
private
objectG3D [MState state]
{
// System.out.println("object");
}
	:	graphGroupG3D[state]
	|	cuboid [state]					// Wuerfel 
	|	cylinder[state]				// Zylinder
	|	sphere[state]					// Sphaere
	|	text[state]					// Text an einem Punkt im Raum (einelementiges labeld PointSet)
	|	inset[state]
	|	graphComp[state]
	|	pointBlock [state]				// Punkte
	|	lineBlock [state] 				// Linien
	|	polygonBlock[state]	 		    // Polygonen
	|	directive[state] 				// graphische Direktiven (aendert eine Appearance)
	|   strange
	;
private
objectGC [MState state]
{
// System.out.println("object");
}
	:	graphGroupGC[state]
	|	cuboid [state]					// Wuerfel 
	|	cylinder[state]					// Zylinder
	|	sphere[state]					// Sphaere
	|	text[state]						// Text an einem Punkt im Raum (einelementiges labeld PointSet)
	|	inset[state]
	|	graphComp[state]
	|	indexedPointSet [state]			// Abfolge von Punkten (PointSet)
	|	indexedLineSet [state] 			// Abfolge von Linien (IndexedLineSet)
	|	indexedFaceSet [state]	 		// Abfolge von Polygonen (IndexedFaceSet)
	|	directive[state] 				// graphische Direktiven (aendert eine Appearance)
	|	gcOpt[state]
	|	strange
	;
	
// ----------------------------- Graphic Primitives ------------------------------------------------------------
private 
cuboid [MState state]
// ein achsenparalleler Wuerfel, gegeben durch Zentrum(Kantenlaenge 1) oder zusaetzlich durch Laengen
{
 System.out.println("cuboid");
 double[] v1 = new double []{0,0,0}; 
 double[] v2 = new double []{1,1,1}; 
}
	:"Cuboid"
	 OPEN_BRACKET 
	 (v1=vektor {v2=new double[]{v1[0]+1,v1[1]+1,v1[2]+1};}
	 (COLON v2=vektor )?)?
	 CLOSE_BRACKET 
	 	{ current.addChild(state.makeCuboid(v1,v2));}
 	;

private 
sphere[MState state]
{
 System.out.println("sphere");
 double[] center=new double []{0,0,0}; 
 double radius=1;
 int n=0;
}
	:"Sphere"
	 OPEN_BRACKET 
			(
			 center=vektor	
			  ( COLON	radius=doublething )?
			 |n=indexVektor 
			  { center=state.getCoords(n); } 
			  ( COLON	radius=doublething )?
			)?
	 CLOSE_BRACKET 
			{ current.addChild(state.makeSphere(center,radius));	}
 	;

private 
cylinder [MState state]
// ein Zylinder gegeben durch eine Strecke(Anfang und Ende) und ggf. einen Raduis(default=1).
{
 System.out.println("cylinder");
 double[] anfg=new double []{0,0,-1}; 
 double[] ende=new double []{0,0,1}; 
 double radius=1;
 int n=0;
}
	:"Cylinder"
	 OPEN_BRACKET 
			(OPEN_BRACE
			 (anfg=vektor
			  |n=indexVektor 
			  { anfg=state.getCoords(n); } 
			 )
			 COLON 
			 (ende=vektor
			  |n=indexVektor 
			  { ende=state.getCoords(n); } 
			 )
			 CLOSE_BRACE
			 ( COLON radius=doublething )?
			)?
	 CLOSE_BRACKET 
			{// realisiert durch gestreckten Zylinder
			 current.addChild(state.makeCylinder(anfg,ende,radius));
			}
 	;
 	
private
text [MState state]
{
 System.out.println("text");
String expr="";
double[] coords=new double[]{0,0,0};
double[] offset=new double[]{0,0};
double[] dir=new double[]{1,0};
int n=0;
}
// ein Stueck Text im Raum 
	:"Text"		OPEN_BRACKET 
					s:STRING 
					(
					 COLON 
					   ( coords=vektor
					   |n=indexVektor { coords=state.getCoords(n); } 
					   )
					(COLON offset=vektordata2D
					(COLON dir=vektordata2D
					)?)?)?
				CLOSE_BRACKET 
					{
					 expr=s.getText();
					 // realisiert durch ein einelementiges Pointset mit einem LabelPunkt 
					 // mit verschwindend kleinem Radius!
					 current.addChild(state.makeLabel(expr,coords,offset,dir));
					}
	;

private
inset [MState state]
{System.out.println("inset");}
// ein beliebiges eingefuegtes Objekt (nicht unbedingt ein 3D Objekt!)
// wird ignoriert
	:"Inset"
	OPEN_BRACKET 
		waste
	CLOSE_BRACKET 
	;

private
graphComp [MState state]
{
 System.out.println("graphComp"); 
MState state2=state.copy();
double[][] coords;
}
	:	"GraphicsComplex" 
		OPEN_BRACKET 
		coords=vertexList
			{ state2.coords=coords;}
		COLON
		(objectListGC[state2])?
		CLOSE_BRACKET 
	;


private
pointBlock [MState state]
{
 System.out.println("pointBlock");
 List<double[]> points= new LinkedList<double[]>(); 
 double[] v = new double[3];
 }
	:"Point"
	   OPEN_BRACKET
	   		(v=vektor {points.add(v);}
	   		|OPEN_BRACE v=vektor {points.add(v);}
	   		 (COLON v=vektor {points.add(v);})*
	   		 CLOSE_BRACE
	   		)
	   (COLON waste)?
	   CLOSE_BRACKET 
	{
		PointSetFactory psf = new PointSetFactory();
		double [][] coords = new double [points.size()][];
		int i=0;
		for(double[] d : points){
			coords[i]=d;
			i++;
		}
		psf.setVertexCount(coords.length);
		psf.setVertexCoordinates(coords);
		psf.update();
		SceneGraphComponent geo=new SceneGraphComponent();
		geo.setAppearance(state.getPointSetApp());
		PointSet p=psf.getPointSet();
		geo.setGeometry(p);
		geo.setName("Points");
		current.addChild(geo);
	}
	;  

private
indexedPointSet [MState state]
{
 System.out.println("indexedPointSet");
// eine Abfolge von Punkten wird zu einer PointSet
 int[] points= null; 
 Integer n;
 ArrayList<Color> colList= new ArrayList<Color>();
 ArrayList<double[]> normList= new ArrayList<double[]>();
 }
	:"Point" OPEN_BRACKET
			( n=indexVektor {points= new int[]{n};}
			 |points=vertexIndexList )
	   (COLON gcOptInside[colList,normList])*	   		
	   CLOSE_BRACKET
	 {
		PointSetFactory psf = new PointSetFactory();
		double [][] coords = state.getIndexCoords(points);
		psf.setVertexCount(points.length);
		psf.setVertexCoordinates(coords);
		psf.update();
		SceneGraphComponent geo=new SceneGraphComponent();
		geo.setAppearance(state.getPointSetApp());
		PointSet p=psf.getPointSet();
		state.assignColorList(p,colList);	
		state.assignNormalList(p,normList);		
		geo.setGeometry(p);
		geo.setName("indexedPoints");
		current.addChild(geo);
	 }
	;  

private
lineBlock [MState state]
{
 System.out.println("lineBlock");
 List<double[][]> lines=new LinkedList<double[][]>();
 double[][] line=null;
 int numVerts=0;
}
	 :("Line"|"Tube")
	  OPEN_BRACKET
		 ( line=vertexList 				{lines.add(line);numVerts+=line.length;}
		  |	OPEN_BRACE line=vertexList	{lines.add(line);numVerts+=line.length;}
		    (COLON line=vertexList    	{lines.add(line);numVerts+=line.length;})*
		    CLOSE_BRACE
		 )
	   (COLON waste)?	   		
	 CLOSE_BRACKET 
	{
		IndexedLineSetFactory lineset=new IndexedLineSetFactory();
		double [][] coords=new double[numVerts][];
		int[][] indices= new int[lines.size()][];
		int laufNum=0;
		for(int i=0;i<lines.size();i++){
			line=lines.get(i);
			indices[i]=new int[line.length];
			for(int j=0; j<line.length;j++){
				indices[i][j]=laufNum;
				coords[laufNum]=line[j];
				laufNum++;
			}
		}
		lineset.setVertexCount(coords.length);
		lineset.setVertexCoordinates(coords);
		lineset.setEdgeCount(indices.length);
		lineset.setEdgeIndices(indices);
		lineset.update();
		SceneGraphComponent geo=new SceneGraphComponent();
		geo.setAppearance(state.getLineSetApp());
		IndexedLineSet ils=lineset.getIndexedLineSet();
		geo.setGeometry(ils);
		geo.setName("Lines");
		current.addChild(geo);
	}
	;  
private
indexedLineSet [MState state]
{
 System.out.println("indexedLineSet");
 List<int[]> indis=new LinkedList<int[]>();
 int[] line= null;
 ArrayList<Color> colList= new ArrayList<Color>();
 ArrayList<double[]> normList= new ArrayList<double[]>();
}
	: ("Line"|"Tube")
	OPEN_BRACKET
		( line=vertexIndexList { indis.add(line); }
		 |OPEN_BRACE 
		  line=vertexIndexList { indis.add(line); }
		  ( COLON line=vertexIndexList { indis.add(line); } )*
		  CLOSE_BRACE
		)
	   (COLON gcOptInside[colList,normList])*	   		
	CLOSE_BRACKET
	{
		IndexedLineSetFactory lineset=new IndexedLineSetFactory();
		int[][] indices= new int[indis.size()][];
		for(int i=0;i<indis.size();i++)
			indices[i]=indis.get(i);
		double[][] coords= state.coords;
		lineset.setVertexCount(coords.length);
		lineset.setVertexCoordinates(coords);
		lineset.setEdgeCount(indices.length);
		lineset.setEdgeIndices(indices);
		lineset.update();
		SceneGraphComponent geo=new SceneGraphComponent();
		geo.setAppearance(state.getLineSetApp());
		IndexedLineSet ils=lineset.getIndexedLineSet();
		state.assignColorList(ils,colList);	
		state.assignNormalList(ils,normList);		
		geo.setGeometry(ils);
		geo.setName("indexedLines");
		current.addChild(geo); 
	}
	;  

private 
polygonBlock [MState state]
{
 System.out.println("polygonBlock");
 MState state2=state.copy(); 
 List<double[][]> faceList= new LinkedList<double[][]>();
 double[][] face;
 int numVerts=0;
}
	:"Polygon"
	 OPEN_BRACKET
   		(face=vertexList 				{ faceList.add(face);numVerts+=face.length;}
		 | 	OPEN_BRACE	face=vertexList { faceList.add(face);numVerts+=face.length;}
		 	( COLON face=vertexList 	{ faceList.add(face);numVerts+=face.length;} )*
		 	CLOSE_BRACE
		)
	   (COLON waste)?	   		
	 CLOSE_BRACKET 
	{
		IndexedFaceSetFactory faceSet = new IndexedFaceSetFactory();
		double [][] coords=coords= new double[numVerts][];
		int[][] indices= new int[faceList.size()][];
		int laufNum=0;
		for(int i=0;i<faceList.size();i++){
			face=faceList.get(i);
			indices[i]=new int[face.length];
			for(int j=0; j<face.length;j++){
				indices[i][j]=laufNum;
				coords[laufNum]=face[j];
				laufNum++;
			}
		}
		faceSet.setVertexCount(coords.length);
		faceSet.setVertexCoordinates(coords);
		faceSet.setFaceCount(indices.length);
		faceSet.setFaceIndices(indices);
		faceSet.setGenerateFaceNormals(true);
		faceSet.update();		
		SceneGraphComponent geo=new SceneGraphComponent();	// Komponenten erstellen und einhaengen
		geo.setAppearance(state2.getFaceApp());
		IndexedFaceSet ifs= faceSet.getIndexedFaceSet();
		current.addChild(geo);
		geo.setName("Faces");
		geo.setGeometry(ifs);
		state.faces.add(ifs);
	}
	;
private 
indexedFaceSet [MState state]
{
 System.out.println("indexedFaceSet");
 List<int[]> indis=new LinkedList<int[]>();
 int[] face=null;
 ArrayList<Color> colList= new ArrayList<Color>();
 ArrayList<double[]> normList= new ArrayList<double[]>();
}
	:"Polygon"
	 OPEN_BRACKET
		( face=vertexIndexList { indis.add(face); }
		 | 	OPEN_BRACE	
		 	face=vertexIndexList { indis.add(face); }
		 	( COLON face=vertexIndexList { indis.add(face); } )*
		 	CLOSE_BRACE
		)
	   (COLON gcOptInside[colList,normList])*	   		
	 CLOSE_BRACKET
	{
		IndexedFaceSetFactory faceSet = new IndexedFaceSetFactory();
		int[][] indices= new int[indis.size()][];
		for(int i=0;i<indis.size();i++)
			indices[i]=indis.get(i);
		double[][] coords= state.coords;
		faceSet.setVertexCount(coords.length);
		faceSet.setVertexCoordinates(coords);
		faceSet.setFaceCount(indices.length);
		faceSet.setFaceIndices(indices);
		faceSet.setGenerateFaceNormals(true);
		faceSet.update();
		IndexedFaceSet ifs= faceSet.getIndexedFaceSet();
		SceneGraphComponent geo=new SceneGraphComponent();	// Komponenten erstellen und einhaengen
		geo.setAppearance(state.getFaceApp());
		current.addChild(geo);
		geo.setName("indexedFaces");
		state.assignColorList(ifs,colList);	
		state.assignNormalList(ifs,normList);		
		geo.setGeometry(ifs);
		state.faces.add(ifs);
	}
	;
	
// -------------------------------------------------- Farben --------------------------------------------
private// TODO: extended
faceColor [MState state] returns[Color fC]
{
 System.out.println("faceColor");
// Farben fuer Flaechen sind in 'SurfaceColor[]' gekapselt
 Color specular; double d; fC= new Color(255,0,0);
}
	: "SurfaceColor" OPEN_BRACKET
			fC=color[state]
				{state.faceColor=fC;}
			( waste )?
		CLOSE_BRACKET 
	;

private
color [MState state] returns[Color c]
{
 System.out.println("color");
// liest eine Farbe 
// Farben haben verschiedene Darstellungen
c= new Color(0,255,0);}
		: "Red"				{c=MHelper.rgbaToRgba(1,0,0,1);}
		| "Green"			{c=MHelper.rgbaToRgba(0,1,0,1);}
		| "Blue" 			{c=MHelper.rgbaToRgba(0,0,1,1);}
		| "Black" 			{c=MHelper.rgbaToRgba(0,0,0,1);}
		| "White" 			{c=MHelper.rgbaToRgba(1,1,1,1);}
		| "Gray" 			{c=MHelper.rgbaToRgba(.5,.5,.5,1);}
		| "Cyan" 			{c=MHelper.rgbaToRgba(0,1,1,1);}
		| "Magenta" 		{c=MHelper.rgbaToRgba(1,0,1,1);}
		| "Yellow" 			{c=MHelper.rgbaToRgba(1,1,0,1);}
		| "Brown" 			{c=MHelper.rgbaToRgba(0.6,0.4,0.2,1);}
		| "Orange" 			{c=MHelper.rgbaToRgba(1,.5,0,1);}
		| "Pink" 			{c=MHelper.rgbaToRgba(1,.5,.5,1);}
		| "Purple" 			{c=MHelper.rgbaToRgba(.5,0,.5,1);}
		| "LightRed" 		{c=MHelper.lighter(MHelper.rgbaToRgba(1,0,0,1),.85);}
		| "LightGreen" 		{c=MHelper.lighter(MHelper.rgbaToRgba(0,1,0,1),.88);}
		| "LightBlue" 		{c=MHelper.rgbaToRgba(0.87,0.94,1,1);}
		| "LightGray" 		{c=MHelper.lighter(MHelper.rgbaToRgba(.5,.5,.5,1),.70);}
		| "LightCyan" 		{c=MHelper.lighter(MHelper.rgbaToRgba(0,1,1,1),.90);}
		| "LightMagenta" 	{c=MHelper.lighter(MHelper.rgbaToRgba(1,0,1,1),.90);}
		| "LightYellow" 	{c=MHelper.lighter(MHelper.rgbaToRgba(.85,.85,0,1),.85);}
		| "LightBrown" 		{c=MHelper.lighter(MHelper.rgbaToRgba(0.6,0.4,0.2,1),.85);}
		| "LightOrange" 	{c=MHelper.rgbaToRgba(1,0.9,0.75,1);}
		| "LightPink" 		{c=MHelper.lighter(MHelper.rgbaToRgba(1,.5,.5,1),.85);}
		| "LightPurple" 	{c=MHelper.lighter(MHelper.rgbaToRgba(.5,0,.5,1),.88);}
		| "Lighter" OPEN_BRACKET
					{double frac=1./3;}
					c=color[state]
					(COLON frac=doublething)?
				CLOSE_BRACKET 
					{
					c=MHelper.lighter(c,frac);
					}
		| "Darker" OPEN_BRACKET
					{double frac=1./3;}
					c=color[state] 
					(COLON frac=doublething)?
				CLOSE_BRACKET 
					{
					c=MHelper.darker(c,frac);
					}
		|"RGBColor" OPEN_BRACKET  // Red-Green-Blue
					{double r,g,b,a; r=b=g=0; a=1;}
					( OPEN_BRACE
					  r=doublething 
					  COLON g=doublething 
					  COLON b=doublething
					  CLOSE_BRACE
					|
					  r=doublething 
					  COLON g=doublething 
					  COLON b=doublething
					)
					CLOSE_BRACKET 
					{
					 c=MHelper.rgbaToRgba(r,g,b,a);
					 state.setColor(c);
				 	}
		| "Hue" 	OPEN_BRACKET // Hue-Saturation-Brightness
					{double h=.5; double s=1;	double b=1;	double a=0;}
					(OPEN_BRACE)?
					h= doublething 
					(COLON s=doublething COLON b=doublething 
					 (COLON a=doublething 
					 )?
					)?
					(CLOSE_BRACE)?
				CLOSE_BRACKET 
					{
					 c = MHelper.hsbaToRgba(h,s,b,a);
 					 state.setColor(c);
					}
		| "GrayLevel" OPEN_BRACKET // Schwarz-Weiss
					{double gr=0; double a=1;} 
					gr=doublething 
					(COLON a=doublething )?
				CLOSE_BRACKET 
					{
						c=MHelper.greyLevelToRgba(gr,a);
						state.setColor(c);
					}
		| "CMYKColor" OPEN_BRACKET // Cyan-Magenta-Yellow-black(Alpha)
					{
					 double cy,ma,ye,k; 
					 cy=ma=ye=k=0; 
					 double a=1;
					}
					(OPEN_BRACE)?
					cy=doublething COLON 
					ma=doublething COLON 
					ye=doublething COLON 
					k=doublething 
					(COLON a=doublething )?
					(CLOSE_BRACE)?
				CLOSE_BRACKET 
					{
					c=MHelper.cmykaToRgba(cy,ma,ye,k,a);
					state.setColor(c);
					}
		| "Opacity" OPEN_BRACKET
					{c=state.color; double o=0;}
					o=doublething
					(COLON c=color[state])?
				CLOSE_BRACKET 
					{
					c=MHelper.opacity(c,o);
					state.setColor(c);
					}
		| c=directiveComplex[state]
		| c=faceColor[state]	
		;
		
private 
directiveComplex [MState state] returns[Color c]
{
 System.out.println("color");
 c= new Color(0,255,0);
 }
	:"Directive"
	 OPEN_BRACKET
		dirList[state]
	 CLOSE_BRACKET 
	  {
	   c=state.getColor();
	  }
	;
private
dirList [MState state]
	: (directive[state]	| OPEN_BRACE dirList[state] CLOSE_BRACE)
	  (COLON (directive[state]	| OPEN_BRACE dirList[state] CLOSE_BRACE)  )*	
	;


// -------------------------------------------- Daten ------------------------------------
private
vertexIndexList returns[int[] v]
{//System.out.println("vertexindexList");
// Koordinaten in einer Liste zu Vector(double[3])
Integer point=null;
LinkedList<Integer> verts=new LinkedList<Integer>();
v=null;
}
		: OPEN_BRACE
		    point=indexVektor
		    {  	verts.add(point);   }
		  (COLON 
			point=indexVektor
			{  	verts.add(point);   }
		  )*
		  CLOSE_BRACE
		 {
		 	v= new int[verts.size()];
		 	int i=0;
		 	for(int vert:verts){
		 		v[i]=vert;
		 		i++;
		 	}
		 }
		;

private
vertexList returns[double[][] v]
{//System.out.println("vertexList");
// Koordinaten in einer Liste zu Vector(double[3])
double [] point =new double[3];
LinkedList<double[]> verts=new LinkedList<double[]>();
v=null;
}
		:OPEN_BRACE
			point=vektor
		    {
		    	verts.add(point); 
		    	point =new double[3];
		    }
		  (COLON point=vektor
			{
		    	verts.add(point);
		    	point =new double[3];
		    }
		  )*
		 CLOSE_BRACE
		 {
		 	v= new double[verts.size()][];
		 	int i=0;
		 	for(double[] vert:verts){
		 		v[i]=vert;
		 		i++;
		 	}
		 }
		;

private
vektor returns[double[] res]
{//System.out.println("vektor");
// ein KoordinatenTripel(Punkt) zu double[3]
res =new double [3];
double res1,res2,res3;
}
	:OPEN_BRACE
 	    res1=doublething 
		COLON res2=doublething 
		COLON res3=doublething 
			{   res[0]=res1;   res[1]=res2;    res[2]=res3;	}
	 CLOSE_BRACE  
	;
private
indexVektor returns[Integer in]
{//System.out.println("indexVektor");
int n;
in=null;
}
	:n=integerthing {in= new Integer(n-1);}
	;

private	
vektordata2D returns[double[] res]
{//System.out.println("vektordata2D");
// das gleiche wie vektor, beeinflusst aber nicht die Borderberechnung(Scenengroese)
res =new double [2];
double res1,res2;}
	: 	OPEN_BRACE 
			res1=doublething COLON res2=doublething
		CLOSE_BRACE
			{
			 res[0]=res1;
			 res[1]=res2;
			}
	;


// --------------------------------graphische Directiven ---------------------------------------- 

private 
directive[MState state]
{System.out.println("directive");
// Direktiven die die Appearance beeinflussen(keine Farben)
// Bemerkung: Der Aufruf 'waste' ignoriert alles in der Klammer.
Color col;}
	: col= color[state]{ // Farbe fuer folgende Punkte, Linien und Texte
				  state.setColor(col);
				}
	| edgeForm[state]
	| faceForm[state]
	| strange
	;

private 
edgeForm [MState state]
{state.edgeDraw=false;}
	:"EdgeForm"
		OPEN_BRACKET
		( edgeFormContent[state]
	   		( COLON edgeFormContent[state] )*
	   		{state.edgeDraw=true;}
	 	)?
		CLOSE_BRACKET
	;
	
private 
edgeFormContent [MState state]
{Color col;}	
	:col=color[state]{state.edgeColor=col;}
	|OPEN_BRACE
	 ( edgeFormContent[state]
	   ( COLON edgeFormContent[state] )*
	 )?
	CLOSE_BRACE
	|strange
	;

private 
faceForm [MState state]
{state.faceDraw=false;}
	:"FaceForm"
		OPEN_BRACKET
		( faceFormContent[state]
	   		( COLON faceFormContent[state] )*
	   		{state.faceDraw=true;}
		 )?
		CLOSE_BRACKET
	;
	
private 
faceFormContent [MState state]
{Color col;}	
	:col=color[state]{state.faceColor=col;}
	|OPEN_BRACE
	 ( faceFormContent[state]
	   ( COLON faceFormContent[state] )*
	 )?
	CLOSE_BRACE
	|strange
	;
	
// ----------------------------------------------- Optionen GraphicsComplex ------------------------------------------
private
gcOpt [MState state]
{
 double[] n=null;
 Color c=null;
 ArrayList<Color> colList= new ArrayList<Color>();
 ArrayList<double[]> normList= new ArrayList<double[]>();
 System.out.println("gcOpt");
}
	: "ContentSelectable" MINUS LARGER	egal
	| "VertexColors" 
			MINUS LARGER
			( 
			 "None"
			|
			  OPEN_BRACE
			  c=vertexColor	{colList.add(c);} 
			  ( COLON c=vertexColor	{colList.add(c);} ) *
			  CLOSE_BRACE
				{	  state.assignColorList(colList);	}
			)
	| "VertexNormals"
			MINUS LARGER
			( 
			 "None"
			|
			 OPEN_BRACE
			 n=vektor {normList.add(n);} 
			 ( COLON n=vektor	{normList.add(n);} ) *
			 CLOSE_BRACE
				{  state.assignNormalList(normList);	}
			)
	;

private
gcOptInside [ArrayList<Color> colList,ArrayList<double[]> normList]
{
 double[] n=null;
 Color c=null;
 System.out.println("gcOptInside");
}
	: "ContentSelectable" MINUS LARGER	egal
	| "VertexColors" 
			MINUS LARGER
			( 
			 "None"
			|
			  OPEN_BRACE
			  c=vertexColor	{colList.add(c);} 
			  ( COLON c=vertexColor	{colList.add(c);} ) *
			  CLOSE_BRACE
			)
	| "VertexNormals"
			MINUS LARGER
			( 
			 "None"
			|
			 OPEN_BRACE
			 n=vektor {normList.add(n);} 
			 ( COLON n=vektor	{normList.add(n);} ) *
			 CLOSE_BRACE
			)
	;

private 
vertexColor returns[Color c]
{
 MState s= new MState();
 double[] n=null;
 c=null;
}
	:c=color[s]
	|n=vektor { c=MHelper.colorToRgba(n);}
	;

// -------------------------------------------------- Kleinkram -------------------------------------------


private
integerthing returns[int i]
// liest ein Integer aus
{i=0;String sig="";}
	: (PLUS | MINUS {sig="-";} )?
	  s:INTEGER_THING {i=Integer.parseInt(sig + s.getText());}
	;


private
doublething returns[double d=0]
{
 double a=0;
 double b=1;
}
	: a=doubleHelp
	  (SLASH b=doubleHelp)?
	  (doubleHelp STAR "I") ?
	  {d=a/b;}
	;

	
private
doubleHelp  returns[double d=0]
// liest ein double aus
	{double e=0; String sig="";}
    : (PLUS | MINUS {sig="-";} )?
    ( s:INTEGER_THING 
    		{d=Double.parseDouble(sig + s.getText());}
      (DOT
      	(s2:INTEGER_THING 
    		{ d=Double.parseDouble(sig + s.getText()+ "." + s2.getText());}
         )?
      )?
	| DOT s3:INTEGER_THING 
			{d=Double.parseDouble(sig + "0." + s3.getText());}
    )
    (e=exponent_thing {d=d*Math.pow(10,e);})?
    ;

private 
exponent_thing returns[int e]
// liest den exponenten fuer double_thing
{e=0; String sig="";}
    : STAR HAT 
    (PLUS | MINUS {sig="-";} )?
     s:INTEGER_THING
     	{e=Integer.parseInt(sig + s.getText() );}
	;
	
private 
waste // ueberliset alles bis zum Klammerende auch mit Unterklammern
	:   (~(	LPAREN | RPAREN | OPEN_BRACE | OPEN_BRACKET | CLOSE_BRACE | CLOSE_BRACKET))*
		(   OPEN_BRACE	 	waste		CLOSE_BRACE		waste
		 |	OPEN_BRACKET 	waste		CLOSE_BRACKET	waste
		 |  LPAREN			waste		RPAREN			waste
		)?
  ;

private
egal
// ueberliest den Rest bis zur naechsten Option. Laest das Komma stehen!
// endet auch beim Klammerende
	: (~(  COLON | OPEN_BRACE | OPEN_BRACKET | CLOSE_BRACKET | CLOSE_BRACE | LPAREN | RPAREN ))*
		(   OPEN_BRACE	 	waste		CLOSE_BRACE   		egal
		 |	OPEN_BRACKET 	waste		CLOSE_BRACKET   	egal
		 |  LPAREN			waste		RPAREN				egal  	)?
  ;
  
private 
strange // ueberliest alle bekannten objekte die nicht implementiert sind
	: ("Arrow"|"AbsoluteDashing"|"AbsolutePointSize"|"AbsoluteThickness"|
	   "Arrowheads"|"Annotation"|"AlignmentPoint"|"AspectRatio"|"Axes"|
	   "AxesEdge"|"AxesLabel"|"AxesOrigin"|"AxesStyle"|"AmbientLight"|
	   "BezierCurve"|"BSplineCurve"|"BSplineSurface"|"Button"|"Background"|
	   "BaselinePosition"|"BaseStyle"|"Boxed"|"BoxRatios"|"BoxStyle"|
	   "CapForm"|"ContentSelectable"|"ControllerLinking"|"ControllerMethod"|
	   "ControllerPath"|"ColorOutput"|"Dashing"|"Dynamic"|"DisplayFunction"|
	   "DefaultColor"|"DefaultFont"|"DisplayFunction"|"Epilog"|
	   "EventHandler"|"FaceGrids"|"FaceGridsStyle"|"FormatType"|"Hyperlink"|
	   "ImageMargins"|"ImagePadding"|"ImageSize"|"JoinForm"|"Lighting"|
	   "LightSources"|"LabelStyle"|"Mouseover"|"Method"|"Prolog"|"PlotRange"|
	   "PlotRangePadding"|"PlotLabel"|"PlotRegion"|"PointSize"|
	   "PolygonIntersections"|"Plot3Matrix"|"PopupWindow"|"PreserveImageOptions"|
	   "RenderAll"|"RotationAction"|"SphericalRegion"|"Shading"|"Spec"|
	   "Specularity"|"StatusArea"|"Style"|"Ticks"|"TicksStyle"|"Tooltip"|
	   "TextStyle"|"Thickness"|"ViewPoint"|"ViewAngle"|"ViewCenter"|
	   "ViewVertical"|"ViewMatrix"|"ViewRange"|"ViewVector"|"ViewVertical"|"None")
	 egal
	;
	
	
/** ************************************************************************
*   ******************* The Mathematica Lexer ******************************
*   ************************************************************************
* this class is only for class MathematicaParser
*/

class Mathematica6Lexer extends Lexer;
options {
	charVocabulary = '\3'..'\377';
	k=2;
	testLiterals=false;
}
	/** Terminal Symbols */
OPEN_BRACE:		'{';
CLOSE_BRACE:	'}';
OPEN_BRACKET:	'[';
CLOSE_BRACKET:	']';
LPAREN:			'(';
RPAREN:			')';
BACKS:			'\\';
SLASH:			'/';
COLON:			',';
DOLLAR: 		'$';
MINUS:			'-';
PLUS:			'+';
LARGER:			'>';
SMALER:			'<';
DOT:			'.';
HAT:			'^';
STAR:			'*';
DDOT: 			':';

T1: '!';
T2: '@';
T3: '#';
T4: '%';
T5: '&';
T6: '=';
T7: ';';
T8: '"';
T9: '?';

ID
options {
	paraphrase = "an identifier";
	testLiterals=true;
}
	:	('a'..'z'|'A'..'Z'|'_') (ID_LETTER)*
	;

private 
ID_LETTER:
	('a'..'z'|'A'..'Z'|'_'|'0'..'9')
	;
	
INTEGER_THING
	: (DIGIT)+
	;
		
private
DIGIT:
	('0'..'9')
	;
	
STRING:
		'"'! (ESC | ~('"'|'\\'))* '"'!
	;
private
ESC:
		'\\'! ('\\' | '"')
	;

WS_:
		( ' '
		| '\t'
		| '\f'
		// handle newlines
		|	(options {
					generateAmbigWarnings=false;
				}
		: "\r\n"	// Evil DOS
			| '\r'		// MacINTosh
			| '\n'		// Unix (the right way)
			{newline(); } )	
		)+ { $setType(Token.SKIP); }
;
