package com.tonyk.veffects.effects;

import java.util.Random;

import android.graphics.Bitmap;

import com.tonyk.veffects.utils.ImageMath;
import com.tonyk.veffects.utils.Noise;

public class PointillizeFilter {

	public final static int RANDOM = 0;
	public final static int SQUARE = 1;
	public final static int HEXAGONAL = 2;
	public final static int OCTAGONAL = 3;
	public final static int TRIANGULAR = 4;

	private float edgeThickness = 0.6f;
	private boolean fadeEdges = false;
	private int edgeColor = 0x66000000;
	private float fuzziness = 0.6f;

	// Cellular
	protected float m00 = 1.0f;
	protected float m01 = 0.0f;
	protected float m10 = 0.0f;
	protected float m11 = 1.0f;

	protected float scale = 32;
	protected float stretch = 1.0f;
	protected float angle = 0.0f;

	protected float[] coefficients = { 1, 0, 0, 0 };
	protected float angleCoefficient;
	private float gradientCoefficient;
	protected int gridType = HEXAGONAL;

	protected Random random = new Random();
	private static byte[] probabilities;
	public float distancePower = 2;

	public class Point {
		public int index;
		public float x, y;
		public float dx, dy;
		public float cubeX, cubeY;
		public float distance;
	}

	protected Point[] results = null;
	protected float randomness = 0;

	// protected int gridType = HEXAGONAL;

	public PointillizeFilter(float scale) {
		this.scale = scale;
		randomness = 0.0f;
		results = new Point[3];
		for (int j = 0; j < results.length; j++)
			results[j] = new Point();
	}

	public Bitmap doPointillizeFilter(Bitmap src) {
		int width = src.getWidth();
		int height = src.getHeight();

		Bitmap outBm = Bitmap.createBitmap(width, height, src.getConfig());
		int[] colors = new int[width * height];
		src.getPixels(colors, 0, width, 0, 0, width, height);
		outBm.setPixels(filterPixels(width, height, colors), 0, width, 0, 0, width, height);
		return outBm;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public void setEdgeThickness(float edgeThickness) {
		this.edgeThickness = edgeThickness;
	}

	public float getEdgeThickness() {
		return edgeThickness;
	}

	public void setFadeEdges(boolean fadeEdges) {
		this.fadeEdges = fadeEdges;
	}

	public boolean getFadeEdges() {
		return fadeEdges;
	}

	public void setEdgeColor(int edgeColor) {
		this.edgeColor = edgeColor;
	}

	public int getEdgeColor() {
		return edgeColor;
	}

	public void setFuzziness(float fuzziness) {
		this.fuzziness = fuzziness;
	}

	public float getFuzziness() {
		return fuzziness;
	}

	public int getPixel(int x, int y, int[] inPixels, int width, int height) {
		float nx = m00 * x + m01 * y;
		float ny = m10 * x + m11 * y;
		nx /= scale;
		ny /= scale * stretch;
		nx += 1000;
		ny += 1000; // Reduce artifacts around 0,0
		float f = evaluate(nx, ny);

		float f1 = results[0].distance;
		int srcx = ImageMath.clamp((int) ((results[0].x - 1000) * scale), 0, width - 1);
		int srcy = ImageMath.clamp((int) ((results[0].y - 1000) * scale), 0, height - 1);
		int v = inPixels[srcy * width + srcx];

		if (fadeEdges) {
			float f2 = results[1].distance;
			srcx = ImageMath.clamp((int) ((results[1].x - 1000) * scale), 0, width - 1);
			srcy = ImageMath.clamp((int) ((results[1].y - 1000) * scale), 0, height - 1);
			int v2 = inPixels[srcy * width + srcx];
			v = ImageMath.mixColors(0.5f * f1 / f2, v, v2);
		} else {
			f = 1 - ImageMath.smoothStep(edgeThickness, edgeThickness + fuzziness, f1);
			v = ImageMath.mixColors(f, edgeColor, v);
		}
		return v;
	}

	public String toString() {
		return "Pixellate/Pointillize...";
	}

	/******************/
	public float evaluate(float x, float y) {
		for (int j = 0; j < results.length; j++)
			results[j].distance = Float.POSITIVE_INFINITY;

		int ix = (int) x;
		int iy = (int) y;
		float fx = x - ix;
		float fy = y - iy;

		float d = checkCube(fx, fy, ix, iy, results);
		if (d > fy) d = checkCube(fx, fy + 1, ix, iy - 1, results);
		if (d > 1 - fy) d = checkCube(fx, fy - 1, ix, iy + 1, results);
		if (d > fx) {
			checkCube(fx + 1, fy, ix - 1, iy, results);
			if (d > fy) d = checkCube(fx + 1, fy + 1, ix - 1, iy - 1, results);
			if (d > 1 - fy) d = checkCube(fx + 1, fy - 1, ix - 1, iy + 1, results);
		}
		if (d > 1 - fx) {
			d = checkCube(fx - 1, fy, ix + 1, iy, results);
			if (d > fy) d = checkCube(fx - 1, fy + 1, ix + 1, iy - 1, results);
			if (d > 1 - fy) d = checkCube(fx - 1, fy - 1, ix + 1, iy + 1, results);
		}

		float t = 0;
		for (int i = 0; i < 3; i++)
			t += coefficients[i] * results[i].distance;
		if (angleCoefficient != 0) {
			float angle = (float) Math.atan2(y - results[0].y, x - results[0].x);
			if (angle < 0) angle += 2 * (float) Math.PI;
			angle /= 4 * (float) Math.PI;
			t += angleCoefficient * angle;
		}
		if (gradientCoefficient != 0) {
			float a = 1 / (results[0].dy + results[0].dx);
			t += gradientCoefficient * a;
		}
		return t;
	}

	private float checkCube(float x, float y, int cubeX, int cubeY, Point[] results) {
		int numPoints;
		random.setSeed(571 * cubeX + 23 * cubeY);
		switch (gridType) {
		case RANDOM:
		default:
			numPoints = probabilities[random.nextInt() & 0x1fff];
			break;
		case SQUARE:
			numPoints = 1;
			break;
		case HEXAGONAL:
			numPoints = 1;
			break;
		case OCTAGONAL:
			numPoints = 2;
			break;
		case TRIANGULAR:
			numPoints = 2;
			break;
		}
		for (int i = 0; i < numPoints; i++) {
			float px = 0, py = 0;
			float weight = 1.0f;
			switch (gridType) {
			case RANDOM:
				px = random.nextFloat();
				py = random.nextFloat();
				break;
			case SQUARE:
				px = py = 0.5f;
				if (randomness != 0) {
					px += randomness * (random.nextFloat() - 0.5);
					py += randomness * (random.nextFloat() - 0.5);
				}
				break;
			case HEXAGONAL:
				if ((cubeX & 1) == 0) {
					px = 0.75f;
					py = 0;
				} else {
					px = 0.75f;
					py = 0.5f;
				}
				if (randomness != 0) {
					px += randomness * Noise.noise2(271 * (cubeX + px), 271 * (cubeY + py));
					py += randomness
							* Noise.noise2(271 * (cubeX + px) + 89, 271 * (cubeY + py) + 137);
				}
				break;
			case OCTAGONAL:
				switch (i) {
				case 0:
					px = 0.207f;
					py = 0.207f;
					break;
				case 1:
					px = 0.707f;
					py = 0.707f;
					weight = 1.6f;
					break;
				}
				if (randomness != 0) {
					px += randomness * Noise.noise2(271 * (cubeX + px), 271 * (cubeY + py));
					py += randomness
							* Noise.noise2(271 * (cubeX + px) + 89, 271 * (cubeY + py) + 137);
				}
				break;
			case TRIANGULAR:
				if ((cubeY & 1) == 0) {
					if (i == 0) {
						px = 0.25f;
						py = 0.35f;
					} else {
						px = 0.75f;
						py = 0.65f;
					}
				} else {
					if (i == 0) {
						px = 0.75f;
						py = 0.35f;
					} else {
						px = 0.25f;
						py = 0.65f;
					}
				}
				if (randomness != 0) {
					px += randomness * Noise.noise2(271 * (cubeX + px), 271 * (cubeY + py));
					py += randomness
							* Noise.noise2(271 * (cubeX + px) + 89, 271 * (cubeY + py) + 137);
				}
				break;
			}
			float dx = (float) Math.abs(x - px);
			float dy = (float) Math.abs(y - py);
			float d;
			dx *= weight;
			dy *= weight;
			if (distancePower == 1.0f)
				d = dx + dy;
			else if (distancePower == 2.0f)
				d = (float) Math.sqrt(dx * dx + dy * dy);
			else
				d = (float) Math.pow(
						(float) Math.pow(dx, distancePower) + (float) Math.pow(dy, distancePower),
						1 / distancePower);

			// Insertion sort the long way round to speed it up a bit
			if (d < results[0].distance) {
				Point p = results[2];
				results[2] = results[1];
				results[1] = results[0];
				results[0] = p;
				p.distance = d;
				p.dx = dx;
				p.dy = dy;
				p.x = cubeX + px;
				p.y = cubeY + py;
			} else if (d < results[1].distance) {
				Point p = results[2];
				results[2] = results[1];
				results[1] = p;
				p.distance = d;
				p.dx = dx;
				p.dy = dy;
				p.x = cubeX + px;
				p.y = cubeY + py;
			} else if (d < results[2].distance) {
				Point p = results[2];
				p.distance = d;
				p.dx = dx;
				p.dy = dy;
				p.x = cubeX + px;
				p.y = cubeY + py;
			}
		}
		return results[2].distance;
	}

	protected int[] filterPixels(int width, int height, int[] inPixels) {
		int index = 0;
		int[] outPixels = new int[width * height];

		for (int y = 0; y < height; y++) {
			for (int x = 0; x < width; x++) {
				outPixels[index++] = getPixel(x, y, inPixels, width, height);
			}
		}
		return outPixels;
	}

}
