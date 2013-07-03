package ru.yap.mobile;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import it.sephiroth.android.library.imagezoom.ImageViewTouch;
import it.sephiroth.android.library.imagezoom.ImageViewTouch.OnImageViewTouchSingleTapListener;
import it.sephiroth.android.library.imagezoom.ImageViewTouchBase.DisplayType;

import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import android.annotation.TargetApi;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

public class ImageActivity extends FragmentActivity {
	
	private boolean showBar = false;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		
		if (android.os.Build.VERSION.SDK_INT >= 14) {
			setIcsFullscreen();
		} else {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		}
		
		getActionBar().hide();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setDisplayShowTitleEnabled(true);
		getActionBar().setBackgroundDrawable(getResources().getDrawable(R.drawable.actionbar_bg));
		
		setContentView(R.layout.image_activity);
		
		ImageViewTouch imageView = (ImageViewTouch) findViewById(R.id.image);

		imageView.setDisplayType(DisplayType.FIT_IF_BIGGER);
		
		imageView.setSingleTapListener( new OnImageViewTouchSingleTapListener() {
			@Override
			public void onSingleTapConfirmed() {
				if (showBar) {
			          getActionBar().hide();
			          if (android.os.Build.VERSION.SDK_INT >= 14) {
			        	  setLowProfile();
			          }
				} else {
			          getActionBar().show();
				}
				showBar = !showBar;
			}
		} );
		
		ImageLoader imageLoader = ImageLoader.getInstance();
		if (!imageLoader.isInited()) {
			imageLoader.init(new ImageLoaderConfiguration.Builder(ImageActivity.this)
				.discCacheFileNameGenerator(new YapURLFileNameGenerator())
				//.enableLogging()
				.build()
			);			
		}
		DisplayImageOptions options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.navigation_refresh_dark)
			.showImageOnFail(R.drawable.alerts_and_states_warning_dark)
			//.resetViewBeforeLoading()
			.cacheOnDisc(true)
			.build();
		
		imageLoader.displayImage(getIntent().getStringExtra("url"), imageView, options);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.image, menu);
        return true;
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			super.onBackPressed();
			return true;
		case R.id.share:
			doShare();
			return true;
		case R.id.download:
			doCopy();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	private void doCopy() {
		ImageLoader imageLoader = ImageLoader.getInstance();
		File src = imageLoader.getDiscCache().get(getIntent().getStringExtra("url"));
		File dst = new File(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES), (new YapURLFileNameGenerator().generate(getIntent().getStringExtra("url"))));
		try {
			copy(src, dst);
			MediaScannerConnection.scanFile(this,
				new String[] { dst.toString() }, null,
				new MediaScannerConnection.OnScanCompletedListener() {
					public void onScanCompleted(String path, Uri uri) {}
				}
			);
			Toast.makeText(getApplicationContext(), getString(R.string.save_ok), Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			e.printStackTrace();
			Toast.makeText(getApplicationContext(), getString(R.string.save_error), Toast.LENGTH_SHORT).show();
		}
	}

	private void doShare() {
    	ImageLoader imageLoader = ImageLoader.getInstance();
    	Uri uri = Uri.fromFile(imageLoader.getDiscCache().get(getIntent().getStringExtra("url")));
		Intent shareIntent = new Intent(Intent.ACTION_SEND);
		shareIntent.setType("image/*");
	    shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
	    startActivity(Intent.createChooser(shareIntent, getResources().getText(R.string.menu_share)));
	}

	public void copy(File src, File dst) throws IOException {
		InputStream in = new FileInputStream(src);
		OutputStream out = new FileOutputStream(dst);
		byte[] buf = new byte[1024];
		int len;
		while ((len = in.read(buf)) > 0) {
			out.write(buf, 0, len);
		}
		in.close();
		out.close();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setIcsFullscreen() {
		if (ViewConfiguration.get(this).hasPermanentMenuKey()) {
			getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
		} else {
			setLowProfile();
		}
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void setLowProfile() {
		if (!ViewConfiguration.get(this).hasPermanentMenuKey()) {
			getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
		}
	}

	public class YapURLFileNameGenerator implements FileNameGenerator {
		@Override
		public String generate(String imageUri) {
			if (imageUri.lastIndexOf(".") > 0) {
				return "yap" + String.valueOf(imageUri.hashCode()) + imageUri.substring(imageUri.lastIndexOf("."));
			} else {
				return "yap" + String.valueOf(imageUri.hashCode());
			}
		}
	}

}

//EOF