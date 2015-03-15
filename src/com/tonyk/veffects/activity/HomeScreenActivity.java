package com.tonyk.veffects.activity;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
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

public class HomeScreenActivity extends ActionBarActivity {

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
		if (requestCode == REQUEST_CODE_LOAD_IMAGE && resultCode == RESULT_OK
				&& null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };
			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();
			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			cursor.close();

			Intent i = new Intent(this, ApplyEffectActivity.class);
			i.putExtra(PHOTO_PATH_KEY, picturePath);
			startActivity(i);

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
			Toast.makeText(HomeScreenActivity.this, "onAdLoaded",
					Toast.LENGTH_SHORT).show();
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
					String.format("onAdFailedToLoad(%s)", errorReason),
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onAdOpened() {
			Toast.makeText(HomeScreenActivity.this, "onAdOpened()",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onAdClosed() {
			Toast.makeText(HomeScreenActivity.this, "onAdClosed()",
					Toast.LENGTH_SHORT).show();
		}

		@Override
		public void onAdLeftApplication() {
			Toast.makeText(HomeScreenActivity.this, "onAdLeftApplication()",
					Toast.LENGTH_SHORT).show();
		}
	};
}
