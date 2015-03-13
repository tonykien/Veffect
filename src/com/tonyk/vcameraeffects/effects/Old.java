package com.tonyk.vcameraeffects.effects;

import android.graphics.Bitmap;
import android.graphics.Color;

public class Old {

	public static Bitmap doOld(Bitmap src) {
		// create new bitmap with the same settings as source bitmap
		Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

		int pixR = 0;
		int pixG = 0;
		int pixB = 0;
		int newR = 0;
		int newG = 0;
		int newB = 0;
		for (int x = 0; x < src.getWidth(); x++) {
			for (int y = 0; y < src.getHeight(); y++) {
				pixR = Color.red(src.getPixel(x, y));
				pixG = Color.green(src.getPixel(x, y));
				pixB = Color.blue(src.getPixel(x, y));
				newR = (int) (0.393 * pixR + 0.769 * pixG + 0.189 * pixB);
				newG = (int) (0.349 * pixR + 0.686 * pixG + 0.168 * pixB);
				newB = (int) (0.272 * pixR + 0.534 * pixG + 0.131 * pixB);
				int newColor = Color.argb(255, newR > 255 ? 255 : newR, newG > 255 ? 255 : newG,
						newB > 255 ? 255 : newB);
				bmOut.setPixel(x, y, newColor);
			}
		}

		return bmOut;
	}
}
