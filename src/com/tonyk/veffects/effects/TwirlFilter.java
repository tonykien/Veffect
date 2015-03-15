package com.tonyk.veffects.effects;

import android.graphics.Bitmap;

import com.tonyk.veffects.utils.ImageMath;

public class TwirlFilter {

	public static Bitmap doTwirlFilter(Bitmap src, float centerX, float centerY) {
		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());

		int radius = height > width ? width / 3 : height / 3;

		int[] colors = new int[width * height];
		int index;
		float angle = 0.7f;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				index = y * width + x;

				float dx = x - centerX;
				float dy = y - centerY;
				float distance = dx * dx + dy * dy;
				if (distance > radius * radius) {
					colors[index] = src.getPixel(x, y);
				} else {
					distance = (float) Math.sqrt(distance);
					float a = (float) Math.atan2(dy, dx) + angle * (radius - distance) / radius;
					int srcX = (int) (centerX + distance * (float) Math.cos(a));
					int srcY = (int) (centerY + distance * (float) Math.sin(a));
					srcX = ImageMath.clamp(srcX, 0, width - 1);
					srcY = ImageMath.clamp(srcY, 0, height - 1);
					colors[index] = src.getPixel(srcX, srcY);
				}
			}
		}

		bmOut.setPixels(colors, 0, width, 0, 0, width, height);
		return bmOut;
	}
}
