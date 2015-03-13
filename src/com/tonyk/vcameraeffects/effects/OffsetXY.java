package com.tonyk.vcameraeffects.effects;

import android.graphics.Bitmap;

public class OffsetXY {

	public static Bitmap doOffsetXY(Bitmap src, int offX, int offY) {
		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

		int[] colors = new int[width * height];
		int index;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				index = y * width + x;
				colors[index] = src.getPixel((width + x - offX) % width, (height + y - offY)
						% height);
				// if ((x < offX) && (y < offY)) {
				// colors[index] = src.getPixel(width + x - offX, height + y -
				// offY);
				// } else if ((x < offX) && (y >= offY)) {
				// colors[index] = src.getPixel(width + x - offX, y - offY);
				// } else if ((x >= offX) && (y < offY)) {
				// colors[index] = src.getPixel(x - offX, height + y - offY);
				// } else if ((x >= offX) && (y >= offY)) {
				// colors[index] = src.getPixel(x - offX, y - offY);
				// }
			}
		}

		bmOut.setPixels(colors, 0, width, 0, 0, width, height);

		return bmOut;
	}
}
