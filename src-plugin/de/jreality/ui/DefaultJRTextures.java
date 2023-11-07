package de.jreality.ui;

import java.util.HashMap;
import java.util.Map;


public class DefaultJRTextures {
	
	private static final String DEFAULT_TEXTURE = "1 Grid";
	
	public static Map<String,String> getDefaultTextures(){
		Map<String,String> textures = new HashMap<String,String>();
		textures.put(DEFAULT_TEXTURE, "textures/grid.jpeg");
		textures.put("2 Black Grid", "textures/gridBlack.jpg");
		textures.put("3 Metal", "textures/metal_basic88.png");
		textures.put("4 Tiles", "textures/recycfloor1_clean2.png");
		textures.put("5 Wallpaper", "textures/kf_techfloor10c.png");
		textures.put("6 Fence","textures/chainlinkfence.png");
		textures.put("7 Rust","textures/outfactory3.png");
		textures.put("8 Cross", "textures/boysurface.png");
		return textures;
	}

	public static String getDefaultTexture() {
		return DEFAULT_TEXTURE;
	}
}
