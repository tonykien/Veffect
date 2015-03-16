package com.tonyk.veffects.activity;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.OnTouchListener;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.internal.bm;
import com.tonyk.veffects.R;
import com.tonyk.veffects.custom.ALog;
import com.tonyk.veffects.utils.BitmapUtil;

public class CropImageActivity extends Activity implements OnTouchListener {

	private Bitmap mSrcBmp, mMaskBmp;

	private Matrix mMatrix = new Matrix();
	private Matrix mSavedMatrix = new Matrix();

	private PointF mStart = new PointF();

	private ImageView mIvMask;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_crop_image);

		/* Load photo and show in preview */
		String path = getIntent().getStringExtra(HomeScreenActivity.PHOTO_PATH_KEY);
		mSrcBmp = BitmapUtil.resizeBitmap(path, 2000);

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
				
				Drawable drawable = ivPreview.getDrawable();
				//you should call after the bitmap drawn
				Rect bounds = drawable.getBounds();
				int bitmapWidth = drawable.getIntrinsicWidth(); //this is the bitmap's width
				int bitmapHeight = drawable.getIntrinsicHeight();
				
				ALog.i("Crop", "w-h:" + width + "-" + height);

				if (height > mSrcBmp.getHeight()) {
					mMaskBmp = Bitmap.createScaledBitmap(mMaskBmp, mSrcBmp.getWidth(),
							mSrcBmp.getWidth() * 4 / 3, true);
				} else {
					mMaskBmp = Bitmap.createScaledBitmap(mMaskBmp, bitmapWidth,
							bitmapWidth * 4 / 3, true);
				}
				
				mIvMask.setImageBitmap(mMaskBmp);

				mMatrix.setTranslate(0f, 0f);
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
			mMatrix.postTranslate(event.getX() - mStart.x, event.getY() - mStart.y);
			break;
		case MotionEvent.ACTION_UP:

			break;
		}
		mIvMask.setImageMatrix(mMatrix);
		return true;
	}
}
