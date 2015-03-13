package com.tonyk.vcameraeffects.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.PorterDuff;
import android.media.ExifInterface;

import com.tonyk.vcameraeffects.custom.ALog;

public class BitmapUtil {

	public static Bitmap resizeBitmap(String path, int requiredSize) {
		int orientation;
		try {
			// decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, o);

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;

			ALog.i("resizeBitmap", "width_tmp:" + width_tmp + " - " + height_tmp
					+ " // requiredSize:" + requiredSize);
			while (true) {
				if (width_tmp <= requiredSize) break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// decode with inSampleSize TODO
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			o2.inMutable = true;
			Bitmap bm = null;
			try {
				bm = BitmapFactory.decodeFile(path, o2);
			} catch (OutOfMemoryError e1) {
				ALog.e("resizebmp", "ERROR = " + e1.getMessage());
			}

			if (bm == null) {
				return bm;
			}
			ALog.i("resizeBitmap", "WH: " + bm.getWidth() + ", " + bm.getHeight());

			Bitmap bitmap = bm;

			// ============================================================
			ExifInterface exif = new ExifInterface(path);

			orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);

			Matrix m = new Matrix();

			if ((orientation == ExifInterface.ORIENTATION_ROTATE_180)) {
				m.postRotate(180);
				bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
				bm.recycle();
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
				m.postRotate(90);
				bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
				bm.recycle();
			} else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
				m.postRotate(270);
				bitmap = Bitmap.createBitmap(bm, 0, 0, bm.getWidth(), bm.getHeight(), m, true);
				bm.recycle();
			}

			return bitmap;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Bitmap getBitmapFromAsset(Context context, String filePath) {
	    AssetManager assetManager = context.getAssets();

	    InputStream istr;
	    Bitmap bitmap = null;
	    try {
	        istr = assetManager.open(filePath);
	        bitmap = BitmapFactory.decodeStream(istr);
	    } catch (IOException e) {
	        // handle exception
	    }

	    return bitmap;
	}
	
	/**
	 * @param bitmap The source bitmap.
	 * @param opacity a value between 0 (completely transparent) and 255 (completely
	 * opaque).
	 * @return The opacity-adjusted bitmap.  If the source bitmap is mutable it will be
	 * adjusted and returned, otherwise a new bitmap is created.
	 */
	public static Bitmap adjustOpacity(Bitmap bitmap, int opacity)	{
	    Bitmap mutableBitmap = bitmap.isMutable()
	                           ? bitmap
	                           : bitmap.copy(Bitmap.Config.ARGB_8888, true);
	    Canvas canvas = new Canvas(mutableBitmap);
	    int colour = (opacity & 0xFF) << 24;
	    canvas.drawColor(colour, PorterDuff.Mode.DST_IN);
	    return mutableBitmap;
	}
	
}
