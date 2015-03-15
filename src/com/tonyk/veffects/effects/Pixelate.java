package com.tonyk.veffects.effects;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Pixelate {

	public static Bitmap doPixelate(Bitmap src, int squareSize) {
		// create new bitmap with the same settings as source bitmap
		Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

		int color;

		for (int x = 0; x < src.getWidth(); x += squareSize) {
			for (int y = 0; y < src.getHeight(); y += squareSize) {
				color = getPredominantRGB(src, x, y, squareSize);
				fillRect(bmOut, x, y, squareSize, color);
			}
		}
		
		return bmOut;
	}

	private static int getPredominantRGB(Bitmap bmp, int row, int col, int squareSize) {
		int red = -1;
		int green = -1;
		int blue = -1;

		for (int x = row; x < row + squareSize; x++) {
			for (int y = col; y < col + squareSize; y++) {
				if (x < bmp.getWidth() && y < bmp.getHeight()) {

					if (red == -1) {
						red = Color.red(bmp.getPixel(x, y));
					} else {
						red = (red + Color.red(bmp.getPixel(x, y))) / 2;
					}
					if (green == -1) {
						green = Color.green(bmp.getPixel(x, y));
					} else {
						green = (green + Color.green(bmp.getPixel(x, y))) / 2;
					}
					if (blue == -1) {
						blue = Color.blue(bmp.getPixel(x, y));
					} else {
						blue = (blue + Color.blue(bmp.getPixel(x, y))) / 2;
					}
				}
			}
		}
		// return (255 << 24) + (red << 16) + (green << 8) + blue;
//		Log.i("getPredominantRGB", "old << :" + ((255 << 24) + (red << 16) + (green << 8) + blue)
//				+ "\n Color.rgb :" + Color.rgb(red, green, blue));
		return Color.rgb(red, green, blue);
	}

	private static void fillRect(Bitmap bmp, int row, int col, int squareSize, int rgb) {
		for (int x = row; x < row + squareSize; x++) {
			for (int y = col; y < col + squareSize; y++) {
				if (x < bmp.getWidth() && y < bmp.getHeight()) {
					bmp.setPixel(x, y, rgb);
				}
			}
		}
	}
}
