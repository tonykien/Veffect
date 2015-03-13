package com.tonyk.vcameraeffects.activity;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.effect.Effect;
import android.media.effect.EffectContext;
import android.media.effect.EffectFactory;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.opengl.GLUtils;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewTreeObserver;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.tonyk.vcameraeffects.R;
import com.tonyk.vcameraeffects.custom.ALog;
import com.tonyk.vcameraeffects.gl.GLToolbox;
import com.tonyk.vcameraeffects.gl.TextureRenderer;
import com.tonyk.vcameraeffects.utils.BitmapUtil;

public class ApplyBorderActivity extends ActionBarActivity implements OnClickListener,
		GLSurfaceView.Renderer {

	public static final float PREVIEW_RATIO = 4f / 3;

	public static final String TAG = "ApplyTextureActivity";

	private AdView mAdView;

	private ImageView mIvPreview;

	private ImageView mIvBorNone, mIvBorder1, mIvBorder2, mIvBorder3, mIvBorder4, mIvBorder5,
			mIvBorder6, mIvBorder7, mIvBorder8, mIvBorder9, mIvBorder10, mIvBorder11, mIvBorder12,
			mIvBorder13, mIvBorder14, mIvBorder15, mIvBorder16, mIvBorder17, mIvBorder18,
			mIvBorder19, mIvBorder20;

	private GLSurfaceView mEffectView;
	private int[] mTextures = new int[2];
	private EffectContext mEffectContext;
	private Effect mEffect;
	private TextureRenderer mTexRenderer = new TextureRenderer();
	private int mImageWidth;
	private int mImageHeight;
	private boolean mInitialized = false;

	private int mGlWidth, mGlHeight;

	private boolean mBtnSavePressed = false;

	/**
	 * Src bitmap for GL surfaceview.
	 */
	private Bitmap mSrcBmp;

	/**
	 * Src bitmap for imageview.
	 */
	private Bitmap mSrcBmp4Iv;

	/**
	 * Current bottom imageview id.
	 */
	private int mCurrentIvId;

	private EffectType mCurrentEffectType;

	private enum EffectType {
		EF_POINTILLIZE, EF_GRAYSCALE, EF_PIXELIZE, EF_TWIRL
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apply_border);

		/* Load photo and show in preview */
		String path = getIntent().getStringExtra(ApplyTextureActivity.EFFECTED_BITMAP);
		mSrcBmp = BitmapFactory.decodeFile(path);
		File tempPhoto = new File(path);
		tempPhoto.delete();

		mAdView = (AdView) findViewById(R.id.adView);

		mIvPreview = (ImageView) findViewById(R.id.ivPreview);

		/**
		 * Initialise the renderer and tell it to only render when Explicit
		 * requested with the RENDERMODE_WHEN_DIRTY option
		 */
		mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
		mEffectView.setEGLContextClientVersion(2);
		mEffectView.setRenderer(ApplyBorderActivity.this);
		mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		initImageViewBottom();
		setOnClickForEffects();

		mCurrentIvId = R.id.ivBorNone;
		((RelativeLayout) mIvBorNone.getParent()).setBackgroundColor(Color.parseColor("#66000000"));

		/* Set layout params for preview. size 3x4 */
		final RelativeLayout rlBottom = (RelativeLayout) findViewById(R.id.rlBottom);
		ViewTreeObserver vto = rlBottom.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				int screenWidth = getResources().getDisplayMetrics().widthPixels;
				int screenHeight = getResources().getDisplayMetrics().heightPixels;
				float density = getResources().getDisplayMetrics().density;
				ALog.i(TAG,
						"w-h:" + screenWidth + "-" + screenHeight + " ||" + rlBottom.getHeight());
				if ((float) (screenHeight - rlBottom.getHeight()) / screenWidth >= PREVIEW_RATIO) {
					LayoutParams params = new LayoutParams(screenWidth,
							(int) (screenWidth * PREVIEW_RATIO));
					params.addRule(RelativeLayout.ABOVE, R.id.rlBottom);
					mEffectView.setLayoutParams(params);
					ALog.i(TAG, "mEffectView:" + mEffectView.getHeight());
					mEffectView.requestRender();

					// if enough area for admob show.
					if (screenHeight - rlBottom.getHeight() - getStatusBarHeight() - screenWidth
							* PREVIEW_RATIO >= 50 * density) {
						mAdView.setVisibility(View.VISIBLE);
						// Request for Ads
						AdRequest adRequest = new AdRequest.Builder()
								// Add a test device to show Test Ads
								.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
								.addTestDevice("C6426F4AA22352ECD3F00DDFFFBA652C").build();
						mAdView.loadAd(adRequest);
					} else {
						mAdView.setVisibility(View.GONE);
					}
				} else {
					mAdView.setVisibility(View.GONE);
				}

				rlBottom.getViewTreeObserver().removeGlobalOnLayoutListener(this);
			}
		});

		mIvPreview.setImageBitmap(mSrcBmp4Iv);

	}

	@Override
	protected void onPause() {
		mAdView.pause();
		if (mEffectView != null) {
			mEffectView.onPause();
		}
		super.onPause();
	}

	@Override
	protected void onResume() {
		mInitialized = false;
		mAdView.resume();
		if (mEffectView != null) {
			mEffectView.onResume();
		}
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		mAdView.destroy();
		super.onDestroy();
	}

	public int getStatusBarHeight() {
		int result = 0;
		int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
		if (resourceId > 0) {
			result = getResources().getDimensionPixelSize(resourceId);
		}
		return result;
	}

	public void initImageViewBottom() {
		mIvBorNone = (ImageView) findViewById(R.id.ivBorNone);
		mIvBorder1 = (ImageView) findViewById(R.id.ivBorder1);
		mIvBorder2 = (ImageView) findViewById(R.id.ivBorder2);
		mIvBorder3 = (ImageView) findViewById(R.id.ivBorder3);
		mIvBorder4 = (ImageView) findViewById(R.id.ivBorder4);
		mIvBorder5 = (ImageView) findViewById(R.id.ivBorder5);
		mIvBorder6 = (ImageView) findViewById(R.id.ivBorder6);
		mIvBorder7 = (ImageView) findViewById(R.id.ivBorder7);
		mIvBorder8 = (ImageView) findViewById(R.id.ivBorder8);
		mIvBorder9 = (ImageView) findViewById(R.id.ivBorder9);
		mIvBorder10 = (ImageView) findViewById(R.id.ivBorder10);
		mIvBorder11 = (ImageView) findViewById(R.id.ivBorder11);
		mIvBorder12 = (ImageView) findViewById(R.id.ivBorder12);
		mIvBorder13 = (ImageView) findViewById(R.id.ivBorder13);
		mIvBorder14 = (ImageView) findViewById(R.id.ivBorder14);
		mIvBorder15 = (ImageView) findViewById(R.id.ivBorder15);
		mIvBorder16 = (ImageView) findViewById(R.id.ivBorder16);
		mIvBorder17 = (ImageView) findViewById(R.id.ivBorder17);
		mIvBorder18 = (ImageView) findViewById(R.id.ivBorder18);
		mIvBorder19 = (ImageView) findViewById(R.id.ivBorder19);
		mIvBorder20 = (ImageView) findViewById(R.id.ivBorder20);
	}

	public void setOnClickForEffects() {
		mIvBorNone.setOnClickListener(this);
		mIvBorder1.setOnClickListener(this);
		mIvBorder2.setOnClickListener(this);
		mIvBorder3.setOnClickListener(this);
		mIvBorder4.setOnClickListener(this);
		mIvBorder5.setOnClickListener(this);
		mIvBorder6.setOnClickListener(this);
		mIvBorder7.setOnClickListener(this);
		mIvBorder8.setOnClickListener(this);
		mIvBorder9.setOnClickListener(this);
		mIvBorder10.setOnClickListener(this);
		mIvBorder11.setOnClickListener(this);
		mIvBorder12.setOnClickListener(this);
		mIvBorder13.setOnClickListener(this);
		mIvBorder14.setOnClickListener(this);
		mIvBorder15.setOnClickListener(this);
		mIvBorder16.setOnClickListener(this);
		mIvBorder17.setOnClickListener(this);
		mIvBorder18.setOnClickListener(this);
		mIvBorder19.setOnClickListener(this);
		mIvBorder20.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		ALog.i(TAG, "onClick");

		ImageView preIv = (ImageView) findViewById(mCurrentIvId);
		((RelativeLayout) preIv.getParent()).setBackgroundColor(Color.TRANSPARENT);
		mCurrentIvId = v.getId();
		((RelativeLayout) v.getParent()).setBackgroundColor(Color.parseColor("#66000000"));

		mEffectView.requestRender();

	}

	@Override
	public void onDrawFrame(GL10 gl) {
		ALog.i(TAG, "onDrawFrame");
		if (!mInitialized) {
			// Only need to do this once
			mEffectContext = EffectContext.createWithCurrentGlContext();
			mTexRenderer.init();
			loadTextures();
			mInitialized = true;
		}
		if (mCurrentIvId != R.id.ivBorNone) {
			// if an effect is chosen initialize it and apply it to the texture
			initEffect();
			applyEffect();
		}
		renderResult();

		runOnUiThread(new Runnable() {

			@Override
			public void run() {
				if (mIvPreview.isShown()) {
					// mInitialized = false;
					mIvPreview.setVisibility(View.INVISIBLE);
					// mEffectView.onResume();
					// mEffectView.setVisibility(View.VISIBLE);
				}

			}
		});

		if (mBtnSavePressed) {
			mBtnSavePressed = false;
			String root = Environment.getExternalStorageDirectory().toString();
			File myDir = new File(root + "/ImageEffectFactory");
			myDir.mkdirs();
			Random generator = new Random();
			int n = 10000;
			n = generator.nextInt(n);

			String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
			String fname = "Image_" + timeStamp + ".jpg";

			File file = new File(myDir, fname);
			if (file.exists()) file.delete();
			try {
				Bitmap bmpOut = null;
				float scale;
				if ((float) mGlHeight / mGlWidth > (float) mImageHeight / mImageWidth) {
					scale = (float) mGlWidth / mImageWidth;
					bmpOut = createBitmapFromGLSurface(0,
							(int) ((mGlHeight - mImageHeight * scale) / 2), mGlWidth,
							(int) (mImageHeight * scale), gl);
				} else {
					scale = (float) mGlHeight / mImageHeight;
					bmpOut = createBitmapFromGLSurface(
							(int) ((mGlWidth - mImageWidth * scale) / 2), 0,
							(int) (mImageWidth * scale), mGlHeight, gl);
				}

				FileOutputStream out = new FileOutputStream(file);
				bmpOut.compress(Bitmap.CompressFormat.JPEG, 100, out);
				out.flush();
				out.close();
				
				Intent i = new Intent(ApplyBorderActivity.this, HomeScreenActivity.class);
				i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
				startActivity(i);
				
				// Toast.makeText(this, "save image",
				// Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		ALog.i("", "onSurfaceChanged: " + width + " - " + height);
		if (mTexRenderer != null) {
			mTexRenderer.updateViewSize(width, height);
			mGlWidth = width;
			mGlHeight = height;
		}

	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		// TODO Auto-generated method stub

	}

	private OnClickListener mGlEffectOnClick = new OnClickListener() {

		@Override
		public void onClick(View v) {
			ImageView preIv = (ImageView) findViewById(mCurrentIvId);
			((RelativeLayout) preIv.getParent()).setBackgroundColor(Color.TRANSPARENT);
			mCurrentIvId = v.getId();
			((RelativeLayout) v.getParent()).setBackgroundColor(Color.parseColor("#66000000"));
			// initEffect();
			// show gl surface view

			mEffectView.requestRender();

		}
	};

	private void initEffect() {
		EffectFactory effectFactory = mEffectContext.getFactory();
		if (mEffect != null) {
			mEffect.release();
		}
		mEffect = effectFactory.createEffect(EffectFactory.EFFECT_BITMAPOVERLAY);

		/**
		 * Initialize the correct effect based on the selected menu/action item
		 */
		Bitmap borderBmp = null;
		switch (mCurrentIvId) {
		case R.id.ivBorNone:
			borderBmp = null;
			break;
		case R.id.ivBorder1:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.border_white);
			break;
		case R.id.ivBorder2:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.border_gray);
			break;
		case R.id.ivBorder3:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.border_black);
			break;
		case R.id.ivBorder4:
			borderBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.border_white_rounded);
			break;
		case R.id.ivBorder5:
			borderBmp = BitmapFactory
					.decodeResource(getResources(), R.drawable.border_gray_rounded);
			break;
		case R.id.ivBorder6:
			borderBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.border_black_rounded);
			break;
		case R.id.ivBorder7:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.border1_w);
			break;
		case R.id.ivBorder8:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.border1_b);
			break;
		case R.id.ivBorder9:
			borderBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.border_spatter_white);
			break;
		case R.id.ivBorder10:
			borderBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.border_spatter_black);
			break;
		case R.id.ivBorder11:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.border_spray_white);
			break;
		case R.id.ivBorder12:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.border_spray_black);
			break;
		case R.id.ivBorder13:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.frame_diza);
			break;
		case R.id.ivBorder14:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.frame_diza2);
			break;
		case R.id.ivBorder15:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.frame_flower);
			break;
		case R.id.ivBorder16:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.frame_word);
			break;
		case R.id.ivBorder17:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.frame_3);
			break;
		case R.id.ivBorder18:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.frame_heart_line);
			break;
		case R.id.ivBorder19:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.frame_text_beautiful_girl);
			break;
		case R.id.ivBorder20:
			borderBmp = BitmapFactory.decodeResource(getResources(), R.drawable.frame_text_smile);
			break;
		default:
			borderBmp = null;
			break;
		}

		if (borderBmp != null) {
			mEffect.setParameter("bitmap", borderBmp);
		}
	}

	private void loadTextures() {
		// Generate textures
		GLES20.glGenTextures(2, mTextures, 0);

		// // Load input bitmap
		// Bitmap bitmap = BitmapFactory.decodeResource(getResources(),
		// R.drawable.puppy);
		mImageWidth = mSrcBmp.getWidth();
		mImageHeight = mSrcBmp.getHeight();
		mTexRenderer.updateTextureSize(mImageWidth, mImageHeight);

		// Upload to texture
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, mTextures[0]);
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, mSrcBmp, 0);

		// Set texture parameters
		GLToolbox.initTexParams();
	}

	private void applyEffect() {
		ALog.i(TAG, "applyEffect");
		if (mEffect != null) {
			/* Check supported effect */
			if (EffectFactory.isEffectSupported(mEffect.getName())) {
				mEffect.apply(mTextures[0], mImageWidth, mImageHeight, mTextures[1]);
			} else {
				Toast.makeText(this, "Effect not support", Toast.LENGTH_SHORT).show(); // TODO
			}
		}
	}

	private void renderResult() {
		if (mCurrentIvId != R.id.ivBorNone) {
			// if no effect is chosen, just render the original bitmap
			mTexRenderer.renderTexture(mTextures[1]);
		} else {
			// render the result of applyEffect()
			mTexRenderer.renderTexture(mTextures[0]);
		}
	}

	public void onBtnSaveClick(View v) {
		mBtnSavePressed = true;
		mEffectView.requestRender();
	}

	private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
			throws OutOfMemoryError {
		int bitmapBuffer[] = new int[w * h];
		int bitmapSource[] = new int[w * h];
		IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
		intBuffer.position(0);

		try {
			gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
			int offset1, offset2;
			for (int i = 0; i < h; i++) {
				offset1 = i * w;
				offset2 = (h - i - 1) * w;
				for (int j = 0; j < w; j++) {
					int texturePixel = bitmapBuffer[offset1 + j];
					int blue = (texturePixel >> 16) & 0xff;
					int red = (texturePixel << 16) & 0x00ff0000;
					int pixel = (texturePixel & 0xff00ff00) | red | blue;
					bitmapSource[offset2 + j] = pixel;
				}
			}
		} catch (GLException e) {
			return null;
		}

		// Bitmap outBm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
		// outBm.copyPixelsFromBuffer(intBuffer);

		return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
	}

}
