package com.tonyk.veffects.filters;

/**
 * @author robert.hinds
 * 
 *         Based on Marvin plugin
 *         http://marvinproject.sourceforge.net/en/plugins/pixelize.html
 *         Originally authored by Gabriel Ambr�sio Archanjo
 * 
 */
public class PixelateFilter implements IAndroidFilter {

	/**
	 * Size of the blurred pixel - the bigger this is, the more coarsely the
	 * image will be pixelated. The pixelation appearence will always be
	 * different for different size images
	 */
	private int mPixelSize = 16;

	@Override
	public AndroidImage process(AndroidImage imageIn) {
		int l_rgb;

		for (int x = 0; x < imageIn.getWidth(); x += mPixelSize) {
			for (int y = 0; y < imageIn.getHeight(); y += mPixelSize) {
				l_rgb = getPredominantRGB(imageIn, x, y, mPixelSize);
				fillRect(imageIn, x, y, mPixelSize, l_rgb);
			}
		}

		return imageIn;
	}

	/**
	 * @return the pixelSize
	 */
	public int getPixelSize() {
		return mPixelSize;
	}

	/**
	 * @param pixelSize the pixelSize to set
	 */
	public void setPixelSize(int pixelSize) {
		this.mPixelSize = pixelSize;
	}

	/**
	 * Method gets the predominant colour pixels to extrapolate the pixelation
	 * from
	 * 
	 * @param imageIn
	 * @param a_x
	 * @param a_y
	 * @param squareSize
	 * @return
	 */
	private int getPredominantRGB(AndroidImage imageIn, int a_x, int a_y, int squareSize) {
		int red = -1;
		int green = -1;
		int blue = -1;

		for (int x = a_x; x < a_x + squareSize; x++) {
			for (int y = a_y; y < a_y + squareSize; y++) {
				if (x < imageIn.getWidth() && y < imageIn.getHeight()) {

					if (red == -1) {
						red = imageIn.getRComponent(x, y);
					} else {
						red = (red + imageIn.getRComponent(x, y)) / 2;
					}
					if (green == -1) {
						green = imageIn.getGComponent(x, y);
					} else {
						green = (green + imageIn.getGComponent(x, y)) / 2;
					}
					if (blue == -1) {
						blue = imageIn.getBComponent(x, y);
					} else {
						blue = (blue + imageIn.getBComponent(x, y)) / 2;
					}
				}
			}
		}
		return (255 << 24) + (red << 16) + (green << 8) + blue;
	}

	/**
	 * Method to extrapolate out
	 * 
	 * @param imageIn
	 * @param a_x
	 * @param a_y
	 * @param squareSize
	 * @param a_rgb
	 */
	private void fillRect(AndroidImage imageIn, int a_x, int a_y, int squareSize, int a_rgb) {
		for (int x = a_x; x < a_x + squareSize; x++) {
			for (int y = a_y; y < a_y + squareSize; y++) {
				if (x < imageIn.getWidth() && y < imageIn.getHeight()) {
					imageIn.setPixelColour(x, y, a_rgb);
				}
			}
		}
	}

}
