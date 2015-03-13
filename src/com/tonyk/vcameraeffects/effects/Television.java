package com.tonyk.vcameraeffects.effects;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Television {

	public static Bitmap doTelevision(Bitmap src) {
		// create new bitmap with the same settings as source bitmap
		Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());
		// Bitmap bmOut = src.copy(src.getConfig(), true);

		int r, g, b;
		for (int x = 0; x < src.getWidth(); x++) {
			for (int y = 0; y < src.getHeight(); y += 3) {
				r = 0;
				g = 0;
				b = 0;

				for (int w = 0; w < 3; w++) {
					if (y + w < src.getHeight()) {
						r += Color.red(src.getPixel(x, y + w)) / 2;
						g += Color.green(src.getPixel(x, y + w)) / 2;
						b += Color.blue(src.getPixel(x, y + w)) / 2;
					}
				}
				r = getValidInterval(r);
				g = getValidInterval(g);
				b = getValidInterval(b);

				for (int w = 0; w < 3; w++) {
					if (y + w < bmOut.getHeight()) {
						if (w == 0) {
							bmOut.setPixel(x, y + w, Color.rgb(r, 0, 0));
						} else if (w == 1) {
							bmOut.setPixel(x, y + w, Color.rgb(0, g, 0));
						} else if (w == 2) {
							bmOut.setPixel(x, y + w, Color.rgb(0, 0, b));
						}
					}
				}
			}
		}

		return bmOut;
	}

	public static int getValidInterval(int value) {
		if (value < 0) return 0;
		if (value > 255) return 255;
		return value;
	}
}
