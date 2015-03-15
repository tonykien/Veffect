package com.tonyk.veffects.activity;

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
import com.tonyk.veffects.R;
import com.tonyk.veffects.custom.ALog;
import com.tonyk.veffects.gl.GLToolbox;
import com.tonyk.veffects.gl.TextureRenderer;
import com.tonyk.veffects.utils.BitmapUtil;

public class ApplyTextureActivity extends ActionBarActivity implements OnClickListener,
		GLSurfaceView.Renderer {

	public static final float PREVIEW_RATIO = 4f / 3;

	public static final String TAG = "ApplyTextureActivity";
	public static final String EFFECTED_BITMAP = "effected_bitmap";

	private AdView mAdView;

	private ImageView mIvPreview;

	private ImageView mIvTexNone, mIvTexBrick, mIvTexBubble, mIvTexFire, mIvTexGlass, mIvTexIce,
			mIvTexLight, mIvTexMoneyCoin, mIvTexMoneyDola, mIvTexMoon, mIvTexOrange, mIvTexRainbow,
			mIvTexSky, mIvTexSmoke, mIvTexSnow, mIvTexStar, mIvTexSunset, mIvTexThunder, mIvTexWater, mIvTexWaterDrop, mIvTexWord;

	private GLSurfaceView mEffectView;
	private int[] mTextures = new int[2];
	private EffectContext mEffectContext;
	private Effect mEffect;
	private TextureRenderer mTexRenderer = new TextureRenderer();
	private int mImageWidth;
	private int mImageHeight;
	private boolean mInitialized = false;

	private int mGlWidth, mGlHeight;

	private boolean mBtnApplyTexturePressed = false;

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

	private int mAlphaProgress = 255;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_apply_texture);

		/* Load photo and show in preview */
		String path = getIntent().getStringExtra(EFFECTED_BITMAP);
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
		mEffectView.setRenderer(ApplyTextureActivity.this);
		mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		initImageViewBottom();
		setOnClickForEffects();

		mCurrentIvId = R.id.ivTexNone;
		((RelativeLayout) mIvTexNone.getParent()).setBackgroundColor(Color.parseColor("#66000000"));

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

		SeekBar sbAlpha = (SeekBar) findViewById(R.id.sbAlpha);
		sbAlpha.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				// TODO Auto-generated method stub

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mAlphaProgress = progress;
				mEffectView.requestRender();
			}
		});

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
		mIvTexNone = (ImageView) findViewById(R.id.ivTexNone);
		mIvTexBrick = (ImageView) findViewById(R.id.ivTexBrick);
		mIvTexBubble = (ImageView) findViewById(R.id.ivTexBubble);
		mIvTexFire = (ImageView) findViewById(R.id.ivTexFire);
		mIvTexGlass = (ImageView) findViewById(R.id.ivTexGlass);
		mIvTexIce = (ImageView) findViewById(R.id.ivTexIce);
		mIvTexLight = (ImageView) findViewById(R.id.ivTexLight);
		mIvTexMoneyCoin = (ImageView) findViewById(R.id.ivTexMoneyCoin);
		mIvTexMoneyDola = (ImageView) findViewById(R.id.ivTexMoneyDola);
		mIvTexMoon = (ImageView) findViewById(R.id.ivTexMoon);
		mIvTexOrange = (ImageView) findViewById(R.id.ivTexOringe);
		mIvTexRainbow = (ImageView) findViewById(R.id.ivTexRainbow);
		mIvTexSky = (ImageView) findViewById(R.id.ivTexSky);
		mIvTexSmoke = (ImageView) findViewById(R.id.ivTexSmoke);
		mIvTexSnow = (ImageView) findViewById(R.id.ivTexSnow);
		mIvTexStar = (ImageView) findViewById(R.id.ivTexStar);
		mIvTexSunset = (ImageView) findViewById(R.id.ivTexSunset);
		mIvTexThunder = (ImageView) findViewById(R.id.ivTexThunder);
		mIvTexWater = (ImageView) findViewById(R.id.ivTexWater);
		mIvTexWaterDrop = (ImageView) findViewById(R.id.ivTexWaterDrop);
		mIvTexWord = (ImageView) findViewById(R.id.ivTexWord);
	}

	public void setOnClickForEffects() {
		mIvTexNone.setOnClickListener(this);
		mIvTexBrick.setOnClickListener(this);
		mIvTexBubble.setOnClickListener(this);
		mIvTexFire.setOnClickListener(this);
		mIvTexGlass.setOnClickListener(this);
		mIvTexIce.setOnClickListener(this);
		mIvTexLight.setOnClickListener(this);
		mIvTexMoneyCoin.setOnClickListener(this);
		mIvTexMoneyDola.setOnClickListener(this);
		mIvTexMoon.setOnClickListener(this);
		mIvTexOrange.setOnClickListener(this);
		mIvTexRainbow.setOnClickListener(this);
		mIvTexSky.setOnClickListener(this);
		mIvTexSmoke.setOnClickListener(this);
		mIvTexSnow.setOnClickListener(this);
		mIvTexStar.setOnClickListener(this);
		mIvTexSunset.setOnClickListener(this);
		mIvTexThunder.setOnClickListener(this);
		mIvTexWater.setOnClickListener(this);
		mIvTexWaterDrop.setOnClickListener(this);
		mIvTexWord.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		ALog.i(TAG, "onClick");

		ImageView preIv = (ImageView) findViewById(mCurrentIvId);
		((RelativeLayout) preIv.getParent()).setBackgroundColor(Color.TRANSPARENT);
		mCurrentIvId = v.getId();
		((RelativeLayout) v.getParent()).setBackgroundColor(Color.parseColor("#66000000"));

		mEffectView.requestRender();

		RelativeLayout rlAjustAlpha = (RelativeLayout) findViewById(R.id.rlAjustAlpha);
		if (mCurrentIvId != R.id.ivTexNone) {
			rlAjustAlpha.setVisibility(View.VISIBLE);
			rlAjustAlpha.bringToFront();
		} else {
			rlAjustAlpha.setVisibility(View.GONE);
		}

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
		if (mCurrentIvId != R.id.ivTexNone) {
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

		if (mBtnApplyTexturePressed) {
			mBtnApplyTexturePressed = false;
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
				bmpOut.compress(Bitmap.CompressFormat.PNG, 100, out);
				out.flush();
				out.close();
				
				Intent i = new Intent(ApplyTextureActivity.this, ApplyBorderActivity.class);
				i.putExtra(EFFECTED_BITMAP, file.getAbsolutePath());
				startActivity(i);
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
		Bitmap textureBmp = null;
		switch (mCurrentIvId) {
		case R.id.ivTexNone:
			textureBmp = null;
			break;
		case R.id.ivTexBrick:
			// textureBmp = BitmapUtil.getBitmapFromAsset(this,
			// "tex_brick_30.png");
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_brick_70);
			break;
		case R.id.ivTexBubble:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_bubble_ver_70);
			break;
		case R.id.ivTexFire:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_fire_ver_70);
			break;
		case R.id.ivTexGlass:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_glass_ver_70);
			break;
		case R.id.ivTexIce:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_ice_ver_70);
			break;
		case R.id.ivTexLight:
			textureBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.tex_light_streaks_ver_70);
			break;
		case R.id.ivTexMoneyCoin:
			textureBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.tex_money_coin_ver_70);
			break;
		case R.id.ivTexMoneyDola:
			textureBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.tex_money_dola_ver_70);
			break;
		case R.id.ivTexMoon:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_moon_ver_70);
			break;
		case R.id.ivTexOringe:
			textureBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.tex_orange_and_pink_ver_70);
			break;
		case R.id.ivTexRainbow:
			textureBmp = BitmapFactory
					.decodeResource(getResources(), R.drawable.tex_rainbow_ver_70);
			break;
		case R.id.ivTexSky:
			textureBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.tex_sky_blue_ver_70);
			break;
		case R.id.ivTexSmoke:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_smoke_ver_70);
			break;
		case R.id.ivTexSnow:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_snow_ver_70);
			break;
		case R.id.ivTexStar:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_star_ver_70);
			break;
		case R.id.ivTexSunset:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_sunset_ver_70);
			break;
		case R.id.ivTexThunder:
			textureBmp = BitmapFactory
					.decodeResource(getResources(), R.drawable.tex_thunder_ver_70);
			break;
		case R.id.ivTexWater:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_water_ver_70);
			break;
		case R.id.ivTexWaterDrop:
			textureBmp = BitmapFactory.decodeResource(getResources(),
					R.drawable.tex_water_drop_ver_70);
			break;
		case R.id.ivTexWord:
			textureBmp = BitmapFactory.decodeResource(getResources(), R.drawable.tex_word_ver_70);
			break;
		default:
			textureBmp = null;
			break;
		}

		if (textureBmp != null) {
			textureBmp = BitmapUtil.adjustOpacity(textureBmp, mAlphaProgress);
			mEffect.setParameter("bitmap", textureBmp);
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
		if (mCurrentIvId != R.id.ivTexNone) {
			// if no effect is chosen, just render the original bitmap
			mTexRenderer.renderTexture(mTextures[1]);
		} else {
			// render the result of applyEffect()
			mTexRenderer.renderTexture(mTextures[0]);
		}
	}

	public void onBtnApplyTextureClick(View v) {
		mBtnApplyTexturePressed = true;
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
