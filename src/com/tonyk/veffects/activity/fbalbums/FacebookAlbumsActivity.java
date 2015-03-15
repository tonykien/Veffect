package com.tonyk.veffects.activity.fbalbums;

import java.util.Arrays;

import android.app.Activity;
import android.os.Bundle;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.Session.OpenRequest;
import com.facebook.Session.StatusCallback;
import com.facebook.SessionState;
import com.tonyk.veffects.R;
import com.tonyk.veffects.custom.ALog;

public class FacebookAlbumsActivity extends Activity {
	
	private final static String TAG = "FacebookAlbumsActivity";

	private StatusCallback mStatusCallback = new SessionStatusCallback();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_facebook_albums);
		
		requestAlbums();
	}
	
	private void requestAlbums() {
		Session session = Session.getActiveSession();
		if (session != null && !session.isOpened() && !session.isClosed()) {
			session.openForRead(new OpenRequest(this).setPermissions(
					Arrays.asList("user_photos")).setCallback(
					mStatusCallback));
		} else {
			Session.openActiveSession(this, true, Arrays.asList("user_photos"), mStatusCallback);
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
			/* make the API call */
			new Request(
			    session,
			    "/me/albums",
			    null,
			    HttpMethod.GET,
			    new Request.Callback() {
			        public void onCompleted(Response response) {
			            /* handle the result */
			        	ALog.i(TAG, response.getGraphObject().getInnerJSONObject().toString());
			        }
			    }
			).executeAsync();
		} else if (state.isClosed()) {
			ALog.i(TAG, "Logged out...");
		}
	}
}
