package com.tonyk.veffects.activity;

import java.util.ArrayList;
import java.util.Arrays;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;

import com.facebook.Session;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.tonyk.veffects.R;
import com.tonyk.veffects.config.Define;
import com.tonyk.veffects.custom.ALog;

public class SharePhotoActivity extends Activity {

	private final static String TAG = "SharePhotoActi";

	private StatusCallback mStatusCallback = new SessionStatusCallback();

	private UiLifecycleHelper mUiHelper;
	
	private ArrayList<Bitmap> mImages = new ArrayList<Bitmap>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_share_photo);

		mUiHelper = new UiLifecycleHelper(this, null);
		mUiHelper.onCreate(savedInstanceState);
		
		/* Load photo and show in preview */
		String path = getIntent().getStringExtra(Define.KEY_TEMP_BITMAP_PATH);
		mImages.add(BitmapFactory.decodeFile(path));
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO Auto-generated method stub
		super.onActivityResult(requestCode, resultCode, data);

		mUiHelper.onActivityResult(requestCode, resultCode, data,
				new FacebookDialog.Callback() {
					@Override
					public void onError(FacebookDialog.PendingCall pendingCall,
							Exception error, Bundle data) {
						ALog.e(TAG,
								String.format("Error: %s", error.toString()));
					}

					@Override
					public void onComplete(
							FacebookDialog.PendingCall pendingCall, Bundle data) {
						ALog.i(TAG, "Success!");
					}
				});
	}

	@Override
	protected void onResume() {
		super.onResume();
		mUiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mUiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
		super.onPause();
		mUiHelper.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mUiHelper.onDestroy();
	}

	public void onBtnFacebookClick(View v) {
//		Session session = Session.getActiveSession();
//		if (session != null && !session.isOpened() && !session.isClosed()) {
//			session.openForRead(new Session.OpenRequest(this).setPermissions(
//					Arrays.asList("public_profile")).setCallback(
//					mStatusCallback));
//		} else {
//			Session.openActiveSession(this, true, mStatusCallback);
//		}

		/* Fb share */
		if (FacebookDialog.canPresentShareDialog(getApplicationContext(),
				FacebookDialog.ShareDialogFeature.PHOTOS)) {
			// Publish the post using the Photo Share Dialog
			FacebookDialog shareDialog = new FacebookDialog.PhotoShareDialogBuilder(
					this).addPhotos(mImages).build();
			mUiHelper.trackPendingDialogCall(shareDialog.present());
		} else {
			// The user doesn't have the Facebook for Android app installed.
			// You may be able to use a fallback.
		}
	}

	private class SessionStatusCallback implements Session.StatusCallback {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (state.isOpened()) {
			ALog.i(TAG, "Logged in...");
		} else if (state.isClosed()) {
			ALog.i(TAG, "Logged out...");
		}
	}
}
