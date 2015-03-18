package com.tonyk.veffects.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.Toast;

import com.tonyk.veffects.R;
import com.tonyk.veffects.config.Define;
import com.tonyk.veffects.custom.ALog;
import com.tonyk.veffects.utils.BitmapUtil;

public class CropImageActivity extends Activity implements OnTouchListener {

	private Bitmap mSrcBmp, mMaskBmp;

	private Matrix mMatrix = new Matrix();
	private Matrix mSavedMatrix = new Matrix();

	private PointF mStart = new PointF();

	private ImageView mIvMask;
	/**
	 * Toa do mask trong ImageView ivPreview
	 */
	private float mMaskX = 0f, mMaskY = 0f;

	private float mMaskMaxX, mMaskMaxY;

	private enum MaskMoveType {
		VERTICAL, HORIZONTAL
	};

	private MaskMoveType mMoveType = MaskMoveType.VERTICAL;

	private float mBmpRatioVer, mBmpRatioHor;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crop_image);

		/* Load photo and show in preview */
		String path = getIntent().getStringExtra(HomeScreenActivity.PHOTO_PATH_KEY);
		mSrcBmp = BitmapUtil.resizeBitmap(path, 2000);

		if ((float) mSrcBmp.getHeight() / mSrcBmp.getWidth() > Define.IMAGE_RATIO) {
			mMoveType = MaskMoveType.VERTICAL;
		} else {
			mMoveType = MaskMoveType.HORIZONTAL;
		}

		mMaskBmp = BitmapFactory.decodeResource(getResources(), R.drawable.mask_3x4);

		final ImageView ivPreview = (ImageView) findViewById(R.id.ivPreview);
		mIvMask = (ImageView) findViewById(R.id.ivRectangleMask);

		ivPreview.setImageBitmap(mSrcBmp);

		ViewTreeObserver vto = ivPreview.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int width = ivPreview.getWidth();
				int height = ivPreview.getHeight();

				ALog.i("Crop", "w-h:" + width + "-" + height);
				if ((float) height / width < (float) mSrcBmp.getHeight() / mSrcBmp.getWidth()) {
					if ((float) mSrcBmp.getHeight() / mSrcBmp.getWidth() > Define.IMAGE_RATIO) {
						if (height > mSrcBmp.getHeight()) {
							mMaskBmp = Bitmap.createScaledBitmap(mMaskBmp, mSrcBmp.getWidth(),
									(int) (mSrcBmp.getWidth() * Define.IMAGE_RATIO), true);
							mBmpRatioVer = 1f;
						} else {
							int bitmapWidth = (int) (mSrcBmp.getWidth() * ((float) height / mSrcBmp
									.getHeight()));
							ALog.i("Crop", "bitmapWidth:" + bitmapWidth);
							mMaskBmp = Bitmap.createScaledBitmap(mMaskBmp, bitmapWidth,
									(int) (bitmapWidth * Define.IMAGE_RATIO), true);
							mBmpRatioVer = (float) mSrcBmp.getHeight() / height;
						}
					} else {
						if (height > mSrcBmp.getHeight()) {
							mMaskBmp = Bitmap.createScaledBitmap(mMaskBmp,
									(int) (mSrcBmp.getHeight() / Define.IMAGE_RATIO),
									mSrcBmp.getHeight(), true);
							mBmpRatioHor = 1f;
						} else {
							mMaskBmp = Bitmap.createScaledBitmap(mMaskBmp,
									(int) (height / Define.IMAGE_RATIO), height, true);
							mBmpRatioHor = (float) mSrcBmp.getHeight() / height;
						}
					}

				} else {
					if ((float) mSrcBmp.getHeight() / mSrcBmp.getWidth() < Define.IMAGE_RATIO) {
						if (width > mSrcBmp.getWidth()) {
							mMaskBmp = Bitmap.createScaledBitmap(mMaskBmp, mSrcBmp.getWidth(),
									(int) (mSrcBmp.getWidth() * Define.IMAGE_RATIO), true);
							mBmpRatioHor = 1f;
						} else {
							int bitmapHeight = (int) (mSrcBmp.getHeight() * ((float) width / mSrcBmp
									.getWidth()));
							mMaskBmp = Bitmap.createScaledBitmap(mMaskBmp,
									(int) (bitmapHeight / Define.IMAGE_RATIO), bitmapHeight, true);
							mBmpRatioHor = (float) mSrcBmp.getWidth() / width;
						}
					} else {
						if (width > mSrcBmp.getWidth()) {
							mMaskBmp = Bitmap.createScaledBitmap(mMaskBmp,
									(int) (mSrcBmp.getHeight() / Define.IMAGE_RATIO),
									mSrcBmp.getHeight(), true);
							mBmpRatioVer = 1f;
						} else {
							mMaskBmp = Bitmap.createScaledBitmap(mMaskBmp, width,
									(int) (width * Define.IMAGE_RATIO), true);
							mBmpRatioVer = (float) mSrcBmp.getWidth() / width;
						}
					}
				}

				mIvMask.setImageBitmap(mMaskBmp);

				mMaskX = (float) (width - mMaskBmp.getWidth()) / 2;
				mMaskY = (float) (height - mMaskBmp.getHeight()) / 2;
				mMaskMaxX = width - mMaskBmp.getWidth();
				mMaskMaxY = height - mMaskBmp.getHeight();
				mMatrix.setTranslate(mMaskX, mMaskY);
				mIvMask.setImageMatrix(mMatrix);
				mIvMask.setOnTouchListener(CropImageActivity.this);

				ivPreview.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		switch (event.getAction()) {
		case MotionEvent.ACTION_DOWN:
			mSavedMatrix.set(mMatrix);
			mStart.set(event.getX(), event.getY());
			break;
		case MotionEvent.ACTION_MOVE:
			mMatrix.set(mSavedMatrix);
			if (mMoveType == MaskMoveType.VERTICAL) {
				float offset = event.getY() - mStart.y;
				if (mMaskY + offset >= 0 && mMaskY + offset <= mMaskMaxY) {
					mMatrix.postTranslate(0, offset);
				} else if (mMaskY + offset < 0) {
					mMatrix.postTranslate(0, -mMaskY);
				} else {
					mMatrix.postTranslate(0, mMaskMaxY - mMaskY);
				}
			} else {
				float offset = event.getX() - mStart.x;
				if (mMaskX + offset >= 0 && mMaskX + offset <= mMaskMaxX) {
					mMatrix.postTranslate(offset, 0);
				} else if (mMaskX + offset < 0) {
					mMatrix.postTranslate(-mMaskX, 0);
				} else {
					mMatrix.postTranslate(mMaskMaxX - mMaskX, 0);
				}
			}

			break;
		case MotionEvent.ACTION_UP:
			if (mMoveType == MaskMoveType.VERTICAL) {
				mMaskY += event.getY() - mStart.y;
				mMaskY = (mMaskY < 0) ? 0 : (mMaskY > mMaskMaxY) ? mMaskMaxY : mMaskY;
			} else {
				mMaskX += event.getX() - mStart.x;
				mMaskX = (mMaskX < 0) ? 0 : (mMaskX > mMaskMaxX) ? mMaskMaxX : mMaskX;
			}
			break;
		}
		mIvMask.setImageMatrix(mMatrix);
		return true;
	}

	public void onBtnCropClick(View v) {
		new AsyncTask<Void, Void, String>() {
			ProgressDialog dialog;

			protected void onPreExecute() {
				dialog = new ProgressDialog(CropImageActivity.this);
				dialog.setMessage(getResources().getString(R.string.progressing));
				dialog.setCancelable(false);
				dialog.setCanceledOnTouchOutside(false);
				dialog.show();
			};

			@Override
			protected String doInBackground(Void... params) {
				Bitmap cropBmp = null;
				if (mMoveType == MaskMoveType.VERTICAL) {
					cropBmp = Bitmap.createBitmap(mSrcBmp, 0, (int) (mMaskY * mBmpRatioVer),
							mSrcBmp.getWidth(), (int) (mSrcBmp.getWidth() * Define.IMAGE_RATIO));
				} else {
					cropBmp = Bitmap.createBitmap(mSrcBmp, (int) (mMaskX * mBmpRatioHor), 0,
							(int) (mSrcBmp.getHeight() / Define.IMAGE_RATIO), mSrcBmp.getHeight());
				}

				String root = Environment.getExternalStorageDirectory().toString();
				File myDir = new File(root + Define.PATH_VEFFECT);
				myDir.mkdirs();
				Random generator = new Random();
				int n = 10000;
				n = generator.nextInt(n);

				String timeStamp = new SimpleDateFormat(Define.DATETIME_FORMAT).format(new Date());
				String fname = Define.FILENAME_PRE + timeStamp + Define.FILENAME_EXTENSION;

				File file = new File(myDir, fname);
				if (file.exists()) file.delete();
				try {
					FileOutputStream out = new FileOutputStream(file);
					cropBmp.compress(Bitmap.CompressFormat.PNG, 100, out);
					out.flush();
					out.close();
					return file.getAbsolutePath();
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

			protected void onPostExecute(String result) {
				dialog.dismiss();
				if (result != null) {
					Intent i = new Intent(CropImageActivity.this, ApplyEffectActivity.class);
					i.putExtra(Define.KEY_TEMP_BITMAP_PATH, result);
					startActivity(i);
				} else {
					Toast.makeText(CropImageActivity.this, getResources().getString(R.string.err_crop),
							Toast.LENGTH_SHORT).show();
				}
			};

		}.execute();

	}

	public void onBtnNotCropClick(View v) {
		String path = getIntent().getStringExtra(HomeScreenActivity.PHOTO_PATH_KEY);
		Intent i = new Intent(this, ApplyEffectActivity.class);
		i.putExtra(HomeScreenActivity.PHOTO_PATH_KEY, path);
		startActivity(i);
	}
}
