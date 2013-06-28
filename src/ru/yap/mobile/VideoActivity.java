package ru.yap.mobile;

import android.app.Activity;
import android.os.Bundle;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.VideoView;

public class VideoActivity extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	        
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		getActionBar().hide();

		setContentView(R.layout.video_activity);
	        
		VideoView vv = (VideoView) findViewById(R.id.videoview);	        
		vv.setVideoPath("http://clips.vorwaerts-gmbh.de/big_buck_bunny.mp4");
		vv.setMediaController(new MediaController(this));
		vv.start();
		vv.requestFocus();
	}

}

//EOF
