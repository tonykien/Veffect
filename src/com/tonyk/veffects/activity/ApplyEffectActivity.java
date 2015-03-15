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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
import com.tonyk.veffects.effects.Brick;
import com.tonyk.veffects.effects.Effects;
import com.tonyk.veffects.effects.Light;
import com.tonyk.veffects.effects.OffsetXY;
import com.tonyk.veffects.effects.Old;
import com.tonyk.veffects.effects.Pixelate;
import com.tonyk.veffects.effects.PointillizeFilter;
import com.tonyk.veffects.effects.RippleFilter;
import com.tonyk.veffects.effects.Television;
import com.tonyk.veffects.effects.TwirlFilter;
import com.tonyk.veffects.gl.GLToolbox;
import com.tonyk.veffects.gl.TextureRenderer;
import com.tonyk.veffects.utils.BitmapUtil;

public class ApplyEffectActivity extends ActionBarActivity implements OnTouchListener,
		OnClickListener, GLSurfaceView.Renderer {

	public static final float PREVIEW_RATIO = 4f / 3;

	public static final String TAG = "ApplyEffectActivity";

	private AdView mAdView;

	private ImageView mIvPreview;

	private ImageView mIvNone, mIvAutofix, mIvBrick, mIvBlackwhite, mIvBrightness, mIvCool,
			mIvContrast, mIvDocument, mIvDuotone, mIvFilllight, mIvFisheye, mIvGrain, mIvHot,
			mIvLomoish, mIvNoise, mIvPointillize, mIvPosterize, mIvRippleSin, mIvRippleNoise,
			mIvSaturate, mIvSnow, mIvTintColor, mIvTintAdjust, mIvTwirt, mIvVignette, mIvGrayscale,
			mIvInvert, mIvSepia, mIvEmboss, mIvSharpen, mIvGaussianBlur, mIvMeanRemoval,
			mIvEngrave, mIvReflection, mIvPixelate, mIvTelevision, mIvLight, mIvOld, mIvOffsetXY;

	private GLSurfaceView mEffectView;
	private int[] mTextures = new int[2];
	private EffectContext mEffectContext;
	private Effect mEffect;
	private TextureRenderer mTexRenderer = new TextureRenderer();
	private int mImageWidth;
	private int mImageHeight;
	private boolean mInitialized = false;

	private int mGlWidth, mGlHeight;

	private boolean mBtnApplyEffectPressed = false;

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
		setContentView(R.layout.activity_apply_effect);

		/* Load photo and show in preview */
		String path = getIntent().getStringExtra(HomeScreenActivity.PHOTO_PATH_KEY);
		mSrcBmp = BitmapUtil.resizeBitmap(path, 1000);
		mSrcBmp4Iv = BitmapUtil.resizeBitmap(path, 500);

		mAdView = (AdView) findViewById(R.id.adView);

		mIvPreview = (ImageView) findViewById(R.id.ivPreview);
		mIvPreview.setOnTouchListener(this);

		/**
		 * Initialise the renderer and tell it to only render when Explicit
		 * requested with the RENDERMODE_WHEN_DIRTY option
		 */
		mEffectView = (GLSurfaceView) findViewById(R.id.effectsview);
		mEffectView.setEGLContextClientVersion(2);
		mEffectView.setRenderer(ApplyEffectActivity.this);
		mEffectView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

		initImageViewBottom();
		setOnClickForEffects();
		
		mCurrentIvId = R.id.ivNone;
		((RelativeLayout) mIvNone.getParent()).setBackgroundColor(Color.parseColor("#66000000"));

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
		mIvNone = (ImageView) findViewById(R.id.ivNone);
		mIvAutofix = (ImageView) findViewById(R.id.ivAutoFix);
		mIvBlackwhite = (ImageView) findViewById(R.id.ivBlackWhite);
		mIvBrick = (ImageView) findViewById(R.id.ivBrick);
		mIvBrightness = (ImageView) findViewById(R.id.ivBrightness);
		mIvContrast = (ImageView) findViewById(R.id.ivContrast);
		mIvCool = (ImageView) findViewById(R.id.ivCool);
		mIvDocument = (ImageView) findViewById(R.id.ivDocument);
		mIvDuotone = (ImageView) findViewById(R.id.ivDuotone);
		mIvEmboss = (ImageView) findViewById(R.id.ivEmboss);
		mIvEngrave = (ImageView) findViewById(R.id.ivEngrave);
		mIvFilllight = (ImageView) findViewById(R.id.ivFilllight);
		mIvFisheye = (ImageView) findViewById(R.id.ivFisheye);
		mIvGaussianBlur = (ImageView) findViewById(R.id.ivGaussianBlur);
		mIvGrain = (ImageView) findViewById(R.id.ivGrain);
		mIvGrayscale = (ImageView) findViewById(R.id.ivGrayscale);
		mIvHot = (ImageView) findViewById(R.id.ivHot);
		mIvInvert = (ImageView) findViewById(R.id.ivInvert);
		mIvLight = (ImageView) findViewById(R.id.ivLight);
		mIvLomoish = (ImageView) findViewById(R.id.ivLomoish);
		mIvMeanRemoval = (ImageView) findViewById(R.id.ivMeanRemoval);
		mIvNoise = (ImageView) findViewById(R.id.ivNoise);
		mIvOffsetXY = (ImageView) findViewById(R.id.ivOffsetxy);
		mIvOld = (ImageView) findViewById(R.id.ivOld);
		mIvPixelate = (ImageView) findViewById(R.id.ivPixelate);
		mIvPointillize = (ImageView) findViewById(R.id.ivPointillize);
		mIvPosterize = (ImageView) findViewById(R.id.ivPosterize);
		mIvReflection = (ImageView) findViewById(R.id.ivReflection);
		mIvRippleNoise = (ImageView) findViewById(R.id.ivRippleNoise);
		mIvRippleSin = (ImageView) findViewById(R.id.ivRippleSin);
		mIvSaturate = (ImageView) findViewById(R.id.ivSaturate);
		mIvSepia = (ImageView) findViewById(R.id.ivSepia);
		mIvSharpen = (ImageView) findViewById(R.id.ivSharpen);
		mIvSnow = (ImageView) findViewById(R.id.ivSnow);
		mIvTelevision = (ImageView) findViewById(R.id.ivTelevision);
		mIvTintAdjust = (ImageView) findViewById(R.id.ivTintAdjust);
		mIvTintColor = (ImageView) findViewById(R.id.ivTintColor);
		mIvTwirt = (ImageView) findViewById(R.id.ivTwirl);
		mIvVignette = (ImageView) findViewById(R.id.ivVignette);
	}

	public void setOnClickForEffects() {
		mIvNone.setOnClickListener(mGlEffectOnClick);
		mIvAutofix.setOnClickListener(mGlEffectOnClick);
		mIvBlackwhite.setOnClickListener(mGlEffectOnClick);
		mIvBrick.setOnClickListener(this);
		mIvBrightness.setOnClickListener(mGlEffectOnClick);
		mIvContrast.setOnClickListener(mGlEffectOnClick);
		mIvCool.setOnClickListener(mGlEffectOnClick);
		mIvDocument.setOnClickListener(mGlEffectOnClick);
		mIvDuotone.setOnClickListener(mGlEffectOnClick);
		mIvEmboss.setOnClickListener(this);
		mIvEngrave.setOnClickListener(this);
		mIvFilllight.setOnClickListener(mGlEffectOnClick);
		mIvFisheye.setOnClickListener(mGlEffectOnClick);
		mIvGaussianBlur.setOnClickListener(this);
		mIvGrain.setOnClickListener(mGlEffectOnClick);
		mIvGrayscale.setOnClickListener(mGlEffectOnClick);
		mIvHot.setOnClickListener(mGlEffectOnClick);
		mIvInvert.setOnClickListener(mGlEffectOnClick);
		mIvLight.setOnClickListener(this);
		mIvLomoish.setOnClickListener(mGlEffectOnClick);
		mIvMeanRemoval.setOnClickListener(this);
		mIvNoise.setOnClickListener(this);
		mIvOffsetXY.setOnClickListener(this);
		mIvOld.setOnClickListener(this);
		mIvPixelate.setOnClickListener(this);
		mIvPointillize.setOnClickListener(this);
		mIvPosterize.setOnClickListener(mGlEffectOnClick);
		mIvReflection.setOnClickListener(this);
		mIvRippleNoise.setOnClickListener(this);
		mIvRippleSin.setOnClickListener(this);
		mIvSaturate.setOnClickListener(mGlEffectOnClick);
		mIvSepia.setOnClickListener(mGlEffectOnClick);
		mIvSharpen.setOnClickListener(mGlEffectOnClick);
		mIvSnow.setOnClickListener(this);
		mIvTelevision.setOnClickListener(this);
		mIvTintAdjust.setOnClickListener(this);
		mIvTintColor.setOnClickListener(mGlEffectOnClick);
		mIvTwirt.setOnClickListener(this);
		mIvVignette.setOnClickListener(mGlEffectOnClick);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.apply_effect, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onClick(View v) {
		ALog.i(TAG, "onClick");
		
		ImageView preIv = (ImageView) findViewById(mCurrentIvId);
		((RelativeLayout) preIv.getParent()).setBackgroundColor(Color.TRANSPARENT);
		mCurrentIvId = v.getId();
		((RelativeLayout) v.getParent()).setBackgroundColor(Color.parseColor("#66000000"));
		
		long timeClick = System.currentTimeMillis();
		// TODO Auto-generated method stub
		// mCurrentEffect = v.getId();
		// mEffectView.requestRender();

		// RelativeLayout rlSepia = (RelativeLayout)
		// findViewById(R.id.rlSepiaToning);
		// rlSepia.setVisibility(View.GONE);

		RelativeLayout rlTint = (RelativeLayout) findViewById(R.id.rlTint);
		rlTint.setVisibility(View.GONE);

		RelativeLayout rlOffsetXY = (RelativeLayout) findViewById(R.id.rlOffsetXY);
		rlOffsetXY.setVisibility(View.GONE);

		switch (v.getId()) {
		case R.id.ivNone:
			mIvPreview.setImageBitmap(mSrcBmp4Iv);
			break;

		case R.id.ivGrayscale:
			mIvPreview.setImageBitmap(Effects.doGreyscale(mSrcBmp4Iv));
			break;

		case R.id.ivInvert:
			mIvPreview.setImageBitmap(Effects.doInvert(mSrcBmp4Iv));
			break;

		// case R.id.ivSepiaToning:
		// rlSepia.setVisibility(View.VISIBLE);
		// doSepiaToning();
		// break;

		case R.id.ivEmboss:
			mIvPreview.setImageBitmap(Effects.emboss(mSrcBmp4Iv));
			break;

		case R.id.ivGaussianBlur:
			mIvPreview.setImageBitmap(Effects.applyGaussianBlur(mSrcBmp4Iv));
			break;

		case R.id.ivMeanRemoval:
			mIvPreview.setImageBitmap(Effects.applyMeanRemoval(mSrcBmp4Iv));
			break;

		case R.id.ivNoise:
			mIvPreview.setImageBitmap(Effects.applyNoiseEffect(mSrcBmp4Iv));
			break;

		case R.id.ivSnow:
			mIvPreview.setImageBitmap(Effects.applySnowEffect(mSrcBmp4Iv));
			break;

		case R.id.ivReflection:
			mIvPreview.setImageBitmap(Effects.applyReflection(mSrcBmp4Iv));
			break;

		case R.id.ivEngrave:
			mIvPreview.setImageBitmap(Effects.engrave(mSrcBmp4Iv));
			break;

		case R.id.ivTintAdjust:
			rlTint.setVisibility(View.VISIBLE);
			doTintImage();
			if (!mIvPreview.isShown()) {
				mIvPreview.setVisibility(View.VISIBLE);
				mIvPreview.bringToFront();
				mIvPreview.requestLayout();
			}
			rlTint.bringToFront();
			rlTint.requestLayout();
			break;

		case R.id.ivOffsetxy:
			rlOffsetXY.setVisibility(View.VISIBLE);
			doOffsetXY();
			if (!mIvPreview.isShown()) {
				mIvPreview.setVisibility(View.VISIBLE);
				mIvPreview.bringToFront();
				mIvPreview.requestLayout();
			}
			rlOffsetXY.bringToFront();
			rlOffsetXY.requestLayout();
			break;

		case R.id.ivTwirl:
			mCurrentEffectType = EffectType.EF_TWIRL;
			mIvPreview.setImageBitmap(TwirlFilter.doTwirlFilter(mSrcBmp4Iv,
					(float) mSrcBmp4Iv.getWidth() / 2, (float) mSrcBmp4Iv.getHeight() / 2));
			break;

		case R.id.ivRippleNoise:
			mIvPreview.setImageBitmap(RippleFilter.doRipple(mSrcBmp4Iv, 3));
			break;

		case R.id.ivRippleSin:
			mIvPreview.setImageBitmap(RippleFilter.doRipple(mSrcBmp4Iv, 0));
			break;

		case R.id.ivPointillize:
			// mChosenEf = EF_POINTILLIZE;
			mCurrentEffectType = EffectType.EF_POINTILLIZE;
			PointillizeFilter filter = new PointillizeFilter(8);

			// filter.setEdgeColor(Color.BLACK);
			// filter.setScale(mSizeValue);
			// filter.setRandomness(getAmout(mRandomnessValue));
			// filter.setAmount(0);
			// filter.setFuzziness(getAmout(mFuzzinessValue));
			// filter.setGridType(getSelectType());
			mIvPreview.setImageBitmap(filter.doPointillizeFilter(mSrcBmp4Iv));
			break;

		case R.id.ivPixelate:
			// Bitmap temp = mSrcBmp4Iv.copy(Config.ARGB_8888, true);
			// AndroidImage image = new AndroidImage(temp);
			// PixelateFilter pixelate = new PixelateFilter();
			// pixelate.setPixelSize(4);
			// pixelate.process(image);
			mIvPreview.setImageBitmap(Pixelate.doPixelate(mSrcBmp4Iv, 4));
			break;

		case R.id.ivTelevision:
			mIvPreview.setImageBitmap(Television.doTelevision(mSrcBmp4Iv));
			break;

		case R.id.ivLight:
			mIvPreview.setImageBitmap(Light.doLight(mSrcBmp4Iv));
			break;

		case R.id.ivOld:
			mIvPreview.setImageBitmap(Old.doOld(mSrcBmp4Iv));
			break;

		case R.id.ivBrick:
			mIvPreview.setImageBitmap(Brick.doBrick(mSrcBmp4Iv));
			break;

		// case R.id.ivRelief:
		// mIvPreview.setImageBitmap(Relief.doRelief(mSrcBmp4Iv));
		// break;

		default:
			break;
		}

		// hide gl surface view
		if (!mIvPreview.isShown()) {
			mIvPreview.setVisibility(View.VISIBLE);
			mIvPreview.bringToFront();
			mIvPreview.requestLayout();
			// mEffectView.onPause();
			// mEffectView.setVisibility(View.GONE);
		}
		ALog.i(TAG, "onClick: time:" + (System.currentTimeMillis() - timeClick));

	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		if (mCurrentEffectType == EffectType.EF_TWIRL) {
			float scale = (float) mSrcBmp4Iv.getWidth() / v.getWidth();
			if ((float) mSrcBmp4Iv.getWidth() / mSrcBmp4Iv.getHeight() < (float) v.getWidth() / v.getHeight()) {
				scale = (float) mSrcBmp4Iv.getHeight() / v.getHeight();
			}
			float centerX = event.getX() * scale - (float) (v.getWidth() * scale - mSrcBmp4Iv.getWidth()) / 2;
			float centerY = event.getY() * scale - (float) (v.getHeight() * scale - mSrcBmp4Iv.getHeight()) / 2;
			Log.i("onTouch", "event xy :" + event.getX() + "-" + event.getY() + " || " + centerX + " - " + centerY + " scale:" + scale);
			mIvPreview.setImageBitmap(TwirlFilter.doTwirlFilter(mSrcBmp4Iv,
					centerX, centerY));
		}
		return false;
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
		if (mCurrentIvId != R.id.ivNone) {
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

		if (mBtnApplyEffectPressed) {
			mBtnApplyEffectPressed = false;
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
				
				Intent i = new Intent(ApplyEffectActivity.this, ApplyTextureActivity.class);
				i.putExtra(ApplyTextureActivity.EFFECTED_BITMAP, file.getAbsolutePath());
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
		/**
		 * Initialize the correct effect based on the selected menu/action item
		 */
		switch (mCurrentIvId) {

		case R.id.ivNone:
			break;

		case R.id.ivAutoFix:
//			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_AUTOFIX);
//			mEffect.setParameter("scale", 0.5f);
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_BITMAPOVERLAY);
			mEffect.setParameter("bitmap", BitmapUtil.getBitmapFromAsset(this, "bubble30.png"));
			break;

		case R.id.ivBlackWhite:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_BLACKWHITE);
			mEffect.setParameter("black", .1f);
			mEffect.setParameter("white", .7f);
			break;

		case R.id.ivBrightness:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_BRIGHTNESS);
			mEffect.setParameter("brightness", 2.0f);
			break;

		case R.id.ivContrast:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_CONTRAST);
			mEffect.setParameter("contrast", 1.4f);
			break;

		case R.id.ivCool:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_CROSSPROCESS);
			break;

		case R.id.ivDocument:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_DOCUMENTARY);
			break;

		case R.id.ivDuotone:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_DUOTONE);
			mEffect.setParameter("first_color", Color.YELLOW);
			mEffect.setParameter("second_color", Color.DKGRAY);
			break;

		case R.id.ivFilllight:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FILLLIGHT);
			mEffect.setParameter("strength", .8f);
			break;

		case R.id.ivFisheye:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FISHEYE);
			mEffect.setParameter("scale", .5f);
			break;

		// case R.id.ivFlipvert:
		// mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FLIP);
		// mEffect.setParameter("vertical", true);
		// break;
		//
		// case R.id.ivFliphor:
		// mEffect = effectFactory.createEffect(EffectFactory.EFFECT_FLIP);
		// mEffect.setParameter("horizontal", true);
		// break;

		case R.id.ivGrain:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAIN);
			mEffect.setParameter("strength", 1.0f);
			break;

		case R.id.ivGrayscale:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_GRAYSCALE);
			break;

		case R.id.ivLomoish:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_LOMOISH);
			break;

		case R.id.ivInvert:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_NEGATIVE);
			break;

		case R.id.ivPosterize:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_POSTERIZE);
			break;

		// case R.id.ivRotate:
		// mEffect = effectFactory.createEffect(EffectFactory.EFFECT_ROTATE);
		// mEffect.setParameter("angle", 180);
		// break;

		case R.id.ivSaturate:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SATURATE);
			mEffect.setParameter("scale", .5f);
			break;

		case R.id.ivSepia:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SEPIA);
			break;

		case R.id.ivSharpen:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_SHARPEN);
			break;

		case R.id.ivHot:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_TEMPERATURE);
			mEffect.setParameter("scale", .9f);
			break;

		case R.id.ivTintColor:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_TINT);
			mEffect.setParameter("tint", Color.MAGENTA);
			break;

		case R.id.ivVignette:
			mEffect = effectFactory.createEffect(EffectFactory.EFFECT_VIGNETTE);
			mEffect.setParameter("scale", .5f);
			break;

		default:
			// mEffectView.setVisibility(View.INVISIBLE);
			break;

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
		if (mCurrentIvId != R.id.ivNone) {
			// if no effect is chosen, just render the original bitmap
			mTexRenderer.renderTexture(mTextures[1]);
		} else {
			// render the result of applyEffect()
			mTexRenderer.renderTexture(mTextures[0]);
		}
	}

	public void onBtnApplyEffectClick(View v) {
		mBtnApplyEffectPressed = true;
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

	/**
	 * Do effects
	 */
	private void doTintImage() {
		SeekBar sbDegree = (SeekBar) findViewById(R.id.sbDegree);
		mIvPreview.setImageBitmap(Effects.tintImage(mSrcBmp4Iv, sbDegree.getProgress()));
		
		sbDegree.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				mIvPreview.setImageBitmap(Effects.tintImage(mSrcBmp4Iv, progress));

			}
		});
	}

	private void doOffsetXY() {
		final SeekBar sbOffsetX = (SeekBar) findViewById(R.id.sbOffsetX);
		final SeekBar sbOffsetY = (SeekBar) findViewById(R.id.sbOffsetY);

		sbOffsetX.setMax(mSrcBmp4Iv.getWidth());
		sbOffsetY.setMax(mSrcBmp4Iv.getHeight());
		
		sbOffsetX.setProgress(mSrcBmp4Iv.getWidth() / 2);
		sbOffsetY.setProgress(mSrcBmp4Iv.getHeight() / 2);
		
		mIvPreview.setImageBitmap(OffsetXY.doOffsetXY(mSrcBmp4Iv, sbOffsetX.getProgress(),
				sbOffsetY.getProgress()));

		OnSeekBarChangeListener sbOffsetChange = new OnSeekBarChangeListener() {

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				mIvPreview.setImageBitmap(OffsetXY.doOffsetXY(mSrcBmp4Iv, sbOffsetX.getProgress(),
						sbOffsetY.getProgress()));
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {

			}

			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

			}
		};

		sbOffsetX.setOnSeekBarChangeListener(sbOffsetChange);
		sbOffsetY.setOnSeekBarChangeListener(sbOffsetChange);

	}
}
