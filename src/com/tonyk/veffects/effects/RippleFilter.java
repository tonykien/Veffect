package com.tonyk.veffects.effects;

import android.graphics.Bitmap;

import com.tonyk.veffects.utils.ImageMath;
import com.tonyk.veffects.utils.Noise;

public class RippleFilter {
	
	/**
     * Sine wave ripples.
     */
	public final static int SINE = 0;

    /**
     * Sawtooth wave ripples.
     */
	public final static int SAWTOOTH = 1;

    /**
     * Triangle wave ripples.
     */
	public final static int TRIANGLE = 2;

    /**
     * Noise ripples.
     */
	public final static int NOISE = 3;

	public static Bitmap doRipple(Bitmap src, int waveType) {
		int width = src.getWidth();
		int height = src.getHeight();
		Bitmap bmOut = Bitmap.createBitmap(width, height, src.getConfig());
		
		float waveLength = 5f;
		float amplitudeX = 6f;
		float amplitudeY = 2f;
		
		int[] colors = new int[width * height];
		int index;
		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				index = y * width + x;
				
				float nx = (float)y / waveLength;
				float ny = (float)x / waveLength;
				
				float fx, fy;
				switch (waveType) {
				case SINE:
				default:
					fx = (float)Math.sin(nx);
					fy = (float)Math.sin(ny);
					break;
				case SAWTOOTH:
					fx = ImageMath.mod(nx, 1);
					fy = ImageMath.mod(ny, 1);
					break;
				case TRIANGLE:
					fx = ImageMath.triangle(nx);
					fy = ImageMath.triangle(ny);
					break;
				case NOISE:
					fx = Noise.noise1(nx);
					fy = Noise.noise1(ny);
					break;
				}
				
				fx = x + amplitudeX * fx;
				fy = y + amplitudeY * fy;
				fx = ImageMath.clamp(fx, 0, width - 1);
				fy = ImageMath.clamp(fy, 0, height - 1);
				colors[index] = src.getPixel((int) fx, (int) fy);
			}
		}
		
		bmOut.setPixels(colors, 0, width, 0, 0, width, height);
		return bmOut;
	}
}
