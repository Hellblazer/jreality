package de.jreality.util;

public class ColorConverter {
	public static java.awt.Color toAwt(de.jreality.shader.Color source){
		return new java.awt.Color(source.getRGBComponents(null)[0], source.getRGBComponents(null)[1], source.getRGBComponents(null)[2], source.getRGBComponents(null)[3]);
	}
	
	public static java.awt.Color[] toAwt(de.jreality.shader.Color[] source){
		java.awt.Color[] awtcolor = new java.awt.Color[source.length];
		for(int i=0;i<source.length;i++){
			awtcolor[i] = ColorConverter.toAwt(source[i]);
		}
		return awtcolor;
	}
	
	public static de.jreality.shader.Color toJR(java.awt.Color source){
		return new de.jreality.shader.Color(source.getRGBComponents(null)[0], source.getRGBComponents(null)[1], source.getRGBComponents(null)[2], source.getRGBComponents(null)[3]);
	}
	
	public static de.jreality.shader.Color[] toJR(java.awt.Color[] source){
		de.jreality.shader.Color[] jrcolor = new de.jreality.shader.Color[source.length];
		for(int i=0;i<source.length;i++){
			jrcolor[i] = ColorConverter.toJR(source[i]);
		}
		return jrcolor;
	}
}
