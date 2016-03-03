package com.ieeton.user.activity;

import com.ieeton.user.R;
import com.ieeton.user.utils.Constants;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoPlayerActivity extends TemplateActivity {
	private VideoView mVideoView;
	private MediaController controller;
	private String mUrl;
	
	@Override
	protected void handleTitleBarEvent(int eventId) {
		switch (eventId) {
		case RIGHT_BUTTON:
			break;
		case LEFT_BUTTON:
			finish();
			break;
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		setView(R.layout.activity_video_player);
		
		mUrl = getIntent().getStringExtra(Constants.EXTRA_URL);
		if (TextUtils.isEmpty(mUrl)){
			finish();
			return;
		}
		
		setTitleBar(null, null, null);
		mVideoView = (VideoView) findViewById(R.id.video);
	}

	private void play(){
		controller = new MediaController(this);
		controller.setMediaPlayer(mVideoView);			
		controller.setAnchorView(mVideoView);
		mVideoView.setMediaController(controller);
		mVideoView.setVideoPath(mUrl);
		mVideoView.start();
	}
	
	@Override
	protected void onResume() {
		play();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}

}
