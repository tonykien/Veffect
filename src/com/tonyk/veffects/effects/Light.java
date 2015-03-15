package com.tonyk.veffects.effects;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Light {

	public static Bitmap doLight(Bitmap src) {
		// create new bitmap with the same settings as source bitmap
		Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

		int width = src.getWidth();
		int height = src.getHeight();
		int pixR, pixG, pixB;
		int newR, newG, newB;
		int centerX = width / 2;
		int centerY = height / 2;
		int radius = Math.min(centerX, centerY);
		float strength = 150;
		for (int x = 1; x < width - 1; x++) {
			for (int y = 1; y < height - 1; y++) {
				pixR = Color.red(src.getPixel(x, y));
				pixG = Color.green(src.getPixel(x, y));
				pixB = Color.blue(src.getPixel(x, y));
				newR = pixR;
				newG = pixG;
				newB = pixB;
				int distance = (int) (Math.pow((centerX - x), 2) + Math.pow(centerY - y, 2));
				if (distance < radius * radius) {
					int result = (int) (strength * (1.0 - Math.sqrt(distance) / radius));
					newR = pixR + result;
					newG = pixG + result;
					newB = pixB + result;
				}
				newR = Math.min(255, Math.max(0, newR));
				newG = Math.min(255, Math.max(0, newG));
				newB = Math.min(255, Math.max(0, newB));
				bmOut.setPixel(x, y, Color.rgb(newR, newG, newB));
			}
		}

		return bmOut;
	}
}
