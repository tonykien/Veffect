package com.tonyk.veffects.activity;

import java.io.IOException;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.tonyk.veffects.R;
import com.tonyk.veffects.activity.fbalbums.FacebookAlbumsActivity;
import com.tonyk.veffects.config.Define;
import com.tonyk.veffects.custom.ALog;

public class HomeScreenActivity extends Activity {

	private static final int REQUEST_CODE_LOAD_IMAGE = 1111;
	public static final String PHOTO_PATH_KEY = "photo_path_key";
	private AdView mAdView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_home_screen);

		mAdView = (AdView) findViewById(R.id.adView);
		mAdView.setAdListener(mAdListener);

		// Request for Ads
		AdRequest adRequest = new AdRequest.Builder()

				// Add a test device to show Test Ads
				.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
				.addTestDevice("C6426F4AA22352ECD3F00DDFFFBA652C").build();

		mAdView.loadAd(adRequest);
	}

	@Override
	protected void onPause() {
		mAdView.pause();
		super.onPause();

		// Logs 'app deactivate' App Event. (fb)
		AppEventsLogger.deactivateApp(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		mAdView.resume();

		// Logs 'install' and 'app activate' App Events.(fb)
		AppEventsLogger.activateApp(this);

	}

	@Override
	protected void onDestroy() {
		mAdView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			long time = System.currentTimeMillis();
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null,
					null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			Options o = new Options();
			BitmapFactory.decodeFile(picturePath, o);
			float picRatio = (float) o.outHeight / o.outWidth;
			
			ExifInterface exif;
			try {
				exif = new ExifInterface(picturePath);
				int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
				if (orientation == ExifInterface.ORIENTATION_ROTATE_90
						|| orientation == ExifInterface.ORIENTATION_ROTATE_270) {
					if (picRatio > 1) {
						// TODO landscape
					} else {
						// TODO portrait
					}
				} else {
					if (picRatio >= 1) {
						// TODO portrait
					} else {
						// TODO landscape
					}
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			if (picRatio < 1) {
				picRatio = 1f / picRatio;
			}
			if (Math.abs(picRatio - Define.IMAGE_RATIO) < 0.1f) {
				Intent i = new Intent(this, ApplyEffectActivity.class);
				i.putExtra(PHOTO_PATH_KEY, picturePath);
				startActivity(i);
			} else {
				Intent i = new Intent(this, CropImageActivity.class);
				i.putExtra(PHOTO_PATH_KEY, picturePath);
				startActivity(i);
			}
			ALog.e("home", "time:" + (System.currentTimeMillis() - time));

			// mPath = picturePath;
			// mBmpResized = resizeBitmap(picturePath, 1000);
			// // getResources().getDisplayMetrics().widthPixels / 2);
			// mIvPreview.setImageBitmap(mBmpResized);
			//
			// initImagePreviewEffects(resizeBitmap(picturePath, 120));
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.home_screen, menu);
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

	public void onBtnGalleryClick(View v) {
		Intent i = new Intent(Intent.ACTION_PICK,
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
		startActivityForResult(i, REQUEST_CODE_LOAD_IMAGE);
	}

	public void onBtnFacebookPhotoClick(View v) {
		Intent i = new Intent(this, FacebookAlbumsActivity.class);
		startActivity(i);
	}

	private AdListener mAdListener = new AdListener() {

		@Override
		public void onAdLoaded() {
			Toast.makeText(HomeScreenActivity.this, "onAdLoaded", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onAdFailedToLoad(int errorCode) {
			String errorReason = "";
			switch (errorCode) {
			case AdRequest.ERROR_CODE_INTERNAL_ERROR:
				errorReason = "Internal error";
				break;
			case AdRequest.ERROR_CODE_INVALID_REQUEST:
				errorReason = "Invalid request";
				break;
			case AdRequest.ERROR_CODE_NETWORK_ERROR:
				errorReason = "Network Error";
				break;
			case AdRequest.ERROR_CODE_NO_FILL:
				errorReason = "No fill";
				break;
			}
			Toast.makeText(HomeScreenActivity.this,
					String.format("onAdFailedToLoad(%s)", errorReason), Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onAdOpened() {
			Toast.makeText(HomeScreenActivity.this, "onAdOpened()", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onAdClosed() {
			Toast.makeText(HomeScreenActivity.this, "onAdClosed()", Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onAdLeftApplication() {
			Toast.makeText(HomeScreenActivity.this, "onAdLeftApplication()", Toast.LENGTH_SHORT)
					.show();
		}
	};
}
