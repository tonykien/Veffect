package com.tonyk.vcameraeffects.effects;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;

public class Relief {

	public static Bitmap doRelief(Bitmap src) {
		// create new bitmap with the same settings as source bitmap
		Bitmap bmOut = Bitmap.createBitmap(src.getWidth(), src.getHeight(), src.getConfig());

		int preColor = 0;
		preColor = src.getPixel(0, 0);
		for (int x = 0; x < src.getWidth(); x++) {
			for (int y = 0; y < src.getHeight(); y++) {
				int currColor = src.getPixel(x, y);
				int r = Color.red(currColor) - Color.red(preColor) + 128;
				int g = Color.green(currColor) - Color.red(preColor) + 128;
				int b = Color.green(currColor) - Color.blue(preColor) + 128;
				int a = Color.alpha(currColor);
				int modifyColor = Color.argb(a, r, g, b);
				bmOut.setPixel(x, y, modifyColor);
				preColor = currColor;
			}
		}
		Canvas c = new Canvas(bmOut);
		Paint paint = new Paint();
		ColorMatrix cm = new ColorMatrix();
		cm.setSaturation(0);
		ColorMatrixColorFilter f = new ColorMatrixColorFilter(cm);
		paint.setColorFilter(f);
		c.drawBitmap(bmOut, 0, 0, paint);

		return bmOut;
	}
}
