package com.tonyk.vcameraeffects.effects;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Brick {

	public static Bitmap doBrick(Bitmap src) {
		// create new bitmap with the same settings as source bitmap
		Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

		int iPixel = 0;
		for (int x = 0; x < src.getWidth(); x++) {
			for (int y = 0; y < src.getHeight(); y++) {
				int currColor = src.getPixel(x, y);
				int avg = (Color.red(currColor) + Color.green(currColor) + Color.blue(currColor)) / 3;
				if (avg >= 100) {
					iPixel = 255;
				} else {
					iPixel = 0;
				}
				bmOut.setPixel(x, y, Color.argb(255, iPixel, iPixel, iPixel));
			}
		}

		return bmOut;
	}
}
