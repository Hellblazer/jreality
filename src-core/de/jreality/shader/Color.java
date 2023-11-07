package de.jreality.shader;

import java.beans.ConstructorProperties;
import java.io.Serializable;

public class Color implements Serializable {

	private int r, g, b, a;

	public final static Color yellow = new Color(255, 255, 0, 255);
	public final static Color cyan = new Color(0, 255, 255, 255);
	public final static Color magenta = new Color(255, 0, 255, 255);
	public final static Color red = new Color(255, 0, 0, 255);
	public final static Color blue = new Color(0, 0, 255, 255);
	public final static Color green = new Color(0, 128, 0, 255);
	public final static Color white = new Color(255, 255, 255, 255);
	public final static Color black = new Color(0, 0, 0, 255);
	public final static Color gray = new Color(128, 128, 128, 255);
	public final static Color light_gray = new Color(211, 211, 211, 255);
	public final static Color pink = new Color(255, 192, 203, 255);
	public final static Color orange = new Color(255, 165, 0, 255);

	public final static Color ORANGE = orange;
	public final static Color PINK = pink;
	public final static Color LIGHT_GRAY = light_gray;
	public final static Color YELLOW = yellow;
	public final static Color CYAN = cyan;
	public final static Color MAGENTA = magenta;
	public final static Color RED = red;
	public final static Color BLUE = blue;
	public final static Color GREEN = green;
	public final static Color WHITE = white;
	public final static Color BLACK = black;
	public final static Color GRAY = gray;

	// public Color(){
	// r = 1;
	// b = 1;
	// g = 1;
	// a = 1;
	// }
	/**
	 * Creates an opaque sRGB color with the specified red, green, and blue
	 * values in the range (0 - 255). The actual color used in rendering depends
	 * on finding the best match given the color space available for a given
	 * output device. Alpha is defaulted to 255.
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>r</code>, <code>g</code> or <code>b</code> are
	 *             outside of the range 0 to 255, inclusive
    * @param r the red component
    * @param g the green component
    * @param b the blue component
    * @param a the alpha component
    * @see #getRed
    * @see #getGreen
    * @see #getBlue
    * @see #getAlpha
    * @see #getRGB
    */
	@ConstructorProperties({"red", "green", "blue", "alpha"})
	public Color(int r, int g, int b) {
		this(r, g, b, 255);
	}

	/**
	 * Creates an sRGB color with the specified red, green, blue, and alpha
	 * values in the range (0 - 255).
	 * 
	 * @throws IllegalArgumentException
	 *             if <code>r</code>, <code>g</code>, <code>b</code> or
	 *             <code>a</code> are outside of the range 0 to 255, inclusive
	 * @param r  the red component
	 * @param g  the green component
	 * @param b  the blue component
	 * @param a  the alpha component
	 * @see #getRed
	 * @see #getGreen
	 * @see #getBlue
	 * @see #getAlpha
	 * @see #getRGB
	 */
	@ConstructorProperties({"red", "green", "blue", "alpha"})
	public Color(int r, int g, int b, int a) {
		testColorValueRange(r, g, b, a);
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	@ConstructorProperties({"red", "green", "blue", "alpha"})
	public Color(float r, float g, float b) {
		this(r, g, b, 0.f);
	}
	
	@ConstructorProperties({"red", "green", "blue", "alpha"})
	public Color(float r, float g, float b, float a) {
		this((int) (r * 255 + 0.5), (int) (g * 255 + 0.5),
				(int) (b * 255 + 0.5), (int) (a * 255 + 0.5));
	}

	public Color(int rgb) {
		
	}

	public static Color toColor(Object o) {
		if (o.getClass().equals(Color.class)) {
			// System.out.println("Info: simply copying the jReality Color object");
			return (Color) o;
		} else {
			if (o.getClass().getName().equals("java.awt.Color")) {
				System.out
						.println("Info: found old java.awt.Color object. Converting to jReality Color object");
				Class parameterTypes[] = new Class[1];
				parameterTypes[0] = float[].class;
				try {
					java.lang.reflect.Method m = o.getClass().getMethod(
							"getRGBComponents", parameterTypes);
					float[] colors = new float[4];
					m.invoke(o, colors);
					return new Color(colors[0], colors[1], colors[2], colors[3]);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			} else {
				System.err
						.println("Trying to convert something that is not a Color to Color");
				return null;
			}
		}
	}

	public int getGreen() {
		return g;
	}

	public int getBlue() {
		return b;
	}

	public int getRed() {
		return r;
	}

	public int getAlpha() {
		return a;
	}

	public float[] getRGBComponents(float[] dest) {
		if (dest != null && dest.length != 4) 
			throw new IllegalArgumentException
			("wrong dimension of color array " + dest.length);
		
		if (dest != null) {
			dest[0] = r / 255f;
			dest[1] = g / 255f;
			dest[2] = b / 255f;
			dest[3] = a / 255f;
		}
		float[] ret = new float[4];
		ret[0] = r / 255f;
		ret[1] = g / 255f;
		ret[2] = b / 255f;
		ret[3] = a / 255f;
		return ret;
	}

	public float[] getComponents(float[] dest) {
		return getRGBComponents(dest);
	}

	public float[] getRGBColorComponents(float[] dest) {
		if (dest != null && dest.length != 3) 
			throw new IllegalArgumentException
			("wrong dimension of color array" + dest.length);
		if (dest != null) {
			dest[0] = r / 255f;
			dest[1] = g / 255f;
			dest[2] = b / 255f;
		}
		float[] ret = new float[3];
		ret[0] = r / 255f;
		ret[1] = g / 255f;
		ret[2] = b / 255f;
		return ret;
	}

	public float[] getColorComponents(float[] dest) {
		return getRGBComponents(dest);
	}

	public Color brighter() {
		r = ((int)(1.1f*r)>255)? 255 : (int)(1.1f*r);
		g = ((int)(1.1f*g)>255)? 255 : (int)(1.1f*g);
		b = ((int)(1.1f*b)>255)? 255 : (int)(1.1f*b);
		return new Color(r,g,b, a);
	}

	public Color darker() {
		return new Color((int)(0.9f * r), (int)(0.9f * g), (int)(0.9f * b), a);
	}

	public static Color getHSBColor(float h, float s, float b) {
		// java.awt.Color javadoc says that hueAngle is found as follows
		float hueAngle = 360 * (h - (float) Math.floor(h));
		// calculation according to English wikipedia article on HSL and HSV
		float hP = hueAngle / 60;
		float Chroma = s * b;
		float X = Chroma * (1 - Math.abs(hP % 2 - 1));
		float m = b - Chroma;
		if (hP < 1)
			return new Color(Chroma + m, X + m, 0 + m);
		if (hP < 2)
			return new Color(X + m, Chroma + m, 0 + m);
		if (hP < 2)
			return new Color(0 + m, Chroma + m, X + m);
		if (hP < 2)
			return new Color(0 + m, X + m, Chroma + m);
		if (hP < 2)
			return new Color(X + m, 0 + m, Chroma + m);
		return new Color(Chroma + m, 0 + m, X + m);
	}

	public int getRGB() {
		return b + g * 256 + r * 256 * 256 + a * 256 * 256 * 256;
	}

	/**
	 * Checks the color integer components supplied for validity. Throws an
	 * {@link IllegalArgumentException} if the value is out of range.
	 * 
	 * @param r
	 *            the Red component
	 * @param g
	 *            the Green component
	 * @param b
	 *            the Blue component
	 **/
	private static void testColorValueRange(int r, int g, int b, int a) {
		boolean rangeError = false;
		String badComponentString = "";

		if (a < 0 || a > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Alpha";
		}
		if (r < 0 || r > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Red";
		}
		if (g < 0 || g > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Green";
		}
		if (b < 0 || b > 255) {
			rangeError = true;
			badComponentString = badComponentString + " Blue";
		}
		if (rangeError == true) {
			throw new IllegalArgumentException(
					"Color parameter outside of expected range:"
							+ badComponentString);
		}
	}

	/**
	 * Returns a string representation of this <code>Color</code>. This method
	 * is intended to be used only for debugging purposes. The content and
	 * format of the returned string might vary between implementations. The
	 * returned string might be empty but cannot be <code>null</code>.
	 * 
	 * @return a string representation of this <code>Color</code>.
	 */
	public String toString() {
		return getClass().getName() + "[r=" + getRed() + ",g=" + getGreen()
				+ ",b=" + getBlue() + "]";
	}

}
