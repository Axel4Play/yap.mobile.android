package ru.yap.mobile;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;

public class CreateActivity extends Activity {

	private SharedPreferences sharedPreferences;

	public static final int TAKE_PHOTO   = 0;
	public static final int CHOOSE_PHOTO = 1;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
			setTheme(android.R.style.Theme_Holo_Light);
		} else {
			setTheme(android.R.style.Theme_Holo);
		}
	    
		super.onCreate(savedInstanceState);
		setContentView(R.layout.create_activity);
		
		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		actionBar.setTitle(R.string.menu_create);


		ImageView imageView = (ImageView) findViewById(R.id.imgView);

		imageView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				
				String[] items = new String[] {
					getString(R.string.take_photo),
					getString(R.string.choose_photo)
				};

				ListAdapter adapter = new ArrayAdapter<String>(
					getBaseContext(),
					R.layout.select_dialog_list,
					R.id.label,
					items) {
					public View getView(int position, View convertView, ViewGroup parent) {
						View v = super.getView(position, convertView, parent);
						ImageView icon = (ImageView) v.findViewById(R.id.icon);
						if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
							if (position == 0) {
								icon.setImageResource(R.drawable.device_access_camera_light);
							} else {
								icon.setImageResource(R.drawable.content_picture_light);
							}
						} else {
							if (position == 0) {
								icon.setImageResource(R.drawable.device_access_camera_dark);
							} else {
								icon.setImageResource(R.drawable.content_picture_dark);
							}
						}
						return v;
					}
				};
					
				new AlertDialog.Builder(CreateActivity.this).setAdapter(adapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						switch (item) {
						case TAKE_PHOTO:
						    startActivityForResult(new Intent(MediaStore.ACTION_IMAGE_CAPTURE), TAKE_PHOTO);
							break;
						case CHOOSE_PHOTO:
							startActivityForResult(new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI), CHOOSE_PHOTO);
							break;
						}
					}
				}).setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				}).setTitle(R.string.image).show();
			}
		});
		
		imageView.setOnLongClickListener(new OnLongClickListener() {
			@Override
			public boolean onLongClick(View v) {
				// TODO: добавить функцию очистки 
				return true;
			}
		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
			getMenuInflater().inflate(R.menu.create_light, menu);	
		} else {
			getMenuInflater().inflate(R.menu.create_dark, menu);
		}
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			super.onBackPressed();
			return true;
		case R.id.menu_send:
			//
			return true;
		case R.id.menu_preferences:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && null != data) {
        	if (requestCode == TAKE_PHOTO) {
        	    Bitmap original = (Bitmap) data.getExtras().get("data");
        	    ImageView imageView = (ImageView) findViewById(R.id.imgView);
        	    imageView.setImageBitmap(original);
        	}

        	if (requestCode == CHOOSE_PHOTO) {
            	Uri selectedImage = data.getData();
            	String[] filePathColumn = { MediaStore.Images.Media.DATA };
     
            	Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
            	cursor.moveToFirst();
            	String picturePath = cursor.getString(cursor.getColumnIndex(filePathColumn[0]));
            	cursor.close();

            	try {
            		Bitmap original = BitmapFactory.decodeFile(picturePath);
            		ImageView imageView = (ImageView) findViewById(R.id.imgView);
            		imageView.setImageBitmap(original);
            	} catch (Exception e) {}
        	}
        }
    }
}

//EOF