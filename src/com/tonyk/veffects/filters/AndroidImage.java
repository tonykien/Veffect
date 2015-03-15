package com.tonyk.veffects.filters;

import android.graphics.Bitmap;
import android.graphics.Matrix;

/**
 * @author robert.hinds
 * 
 *         Wrapper class for the Android Bitmap - used by all filters
 * 
 */
public class AndroidImage {

	// original bitmap image
	private Bitmap mOriginalBmp;

	// format of image (jpg/png)
	private String mImageFormat;

	// dimensions of image
	private int mWidth, mHeight;

	// RGB Array Color
	protected int[] mColourArray;

	public AndroidImage(Bitmap img) {
		this.mOriginalBmp = img;
		mImageFormat = "jpg";
		mWidth = img.getWidth();
		mHeight = img.getHeight();
		updateColourArray();
	}

	/**
	 * Method to reset the image to a solid colour
	 * 
	 * @param color - colour to rest the entire image to
	 */
	public void clearImage(int color) {
		for (int y = 0; y < mHeight; y++) {
			for (int x = 0; x < mWidth; x++) {
				mOriginalBmp.setPixel(x, y, color);
			}
		}
	}

	/**
	 * Set colour array for image - called on initialisation by constructor
	 * 
	 * @param bitmap
	 */
	private void updateColourArray() {
		mColourArray = new int[mWidth * mHeight];
		mOriginalBmp.getPixels(mColourArray, 0, mWidth, 0, 0, mWidth, mHeight);
		int r, g, b;
		for (int y = 0; y < mHeight; y++) {
			for (int x = 0; x < mWidth; x++) {
				int index = y * mWidth + x;
				r = (mColourArray[index] >> 16) & 0xff;
				g = (mColourArray[index] >> 8) & 0xff;
				b = mColourArray[index] & 0xff;
				mColourArray[index] = 0xff000000 | (r << 16) | (g << 8) | b;
			}
		}
	}

	/**
	 * Method to set the colour of a specific pixel
	 * 
	 * @param x
	 * @param y
	 * @param colour
	 */
	public void setPixelColour(int x, int y, int colour) {
		mColourArray[((y * mOriginalBmp.getWidth() + x))] = colour;
		mOriginalBmp.setPixel(x, y, colour);
	}

	/**
	 * Get the colour for a specified pixel
	 * 
	 * @param x
	 * @param y
	 * @return colour
	 */
	public int getPixelColour(int x, int y) {
		return mColourArray[y * mWidth + x];
	}

	/**
	 * Set the colour of a specified pixel from an RGB combo
	 * 
	 * @param x
	 * @param y
	 * @param c0
	 * @param c1
	 * @param c2
	 */
	public void setPixelColour(int x, int y, int c0, int c1, int c2) {
		mColourArray[((y * mOriginalBmp.getWidth() + x))] = (255 << 24) + (c0 << 16) + (c1 << 8) + c2;
		mOriginalBmp.setPixel(x, y, mColourArray[((y * mOriginalBmp.getWidth() + x))]);
	}

	/**
	 * Method to get the RED colour for the specified pixel
	 * 
	 * @param x
	 * @param y
	 * @return colour of R
	 */
	public int getRComponent(int x, int y) {
//		Log.i("getRComponent", "ColourArray :" + ((getColourArray()[((y * mWidth + x))] & 0x00FF0000) >>> 16)
//				+ "\n Color.red :" + Color.red(mOriginalBmp.getPixel(x, y)));
		return (getColourArray()[((y * mWidth + x))] & 0x00FF0000) >>> 16;
	}

	/**
	 * Method to get the GREEN colour for the specified pixel
	 * 
	 * @param x
	 * @param y
	 * @return colour of G
	 */
	public int getGComponent(int x, int y) {
		return (getColourArray()[((y * mWidth + x))] & 0x0000FF00) >>> 8;
	}

	/**
	 * Method to get the BLUE colour for the specified pixel
	 * 
	 * @param x
	 * @param y
	 * @return colour of B
	 */
	public int getBComponent(int x, int y) {
		return (getColourArray()[((y * mWidth + x))] & 0x000000FF);
	}

	/**
	 * Method to rotate an image by the specified number of degrees
	 * 
	 * @param rotateDegrees
	 */
	public void rotate(int rotateDegrees) {
		Matrix mtx = new Matrix();
		mtx.postRotate(rotateDegrees);
		mOriginalBmp = Bitmap.createBitmap(mOriginalBmp, 0, 0, mWidth, mHeight, mtx, true);
		mWidth = mOriginalBmp.getWidth();
		mHeight = mOriginalBmp.getHeight();
		updateColourArray();
	}

	/**
	 * @return the image
	 */
	public Bitmap getBitmap() {
		return mOriginalBmp;
	}

//	/**
//	 * @param image the image to set
//	 */
//	public void setImage(Bitmap image) {
//		this.mOriginalBmp = image;
//	}

	/**
	 * @return the formatName
	 */
	public String getImageFormat() {
		return mImageFormat;
	}

	/**
	 * @param formatName the formatName to set
	 */
	public void setImageFormat(String formatName) {
		this.mImageFormat = formatName;
	}

	/**
	 * @return the width
	 */
	public int getWidth() {
		return mWidth;
	}

	/**
	 * @param width the width to set
	 */
	public void setWidth(int width) {
		this.mWidth = width;
	}

	/**
	 * @return the height
	 */
	public int getHeight() {
		return mHeight;
	}

	/**
	 * @param height the height to set
	 */
	public void setHeight(int height) {
		this.mHeight = height;
	}

	/**
	 * @return the colourArray
	 */
	public int[] getColourArray() {
		return mColourArray;
	}

	/**
	 * @param colourArray the colourArray to set
	 */
	public void setColourArray(int[] colourArray) {
		this.mColourArray = colourArray;
	}

}
