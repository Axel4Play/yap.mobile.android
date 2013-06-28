package ru.yap.mobile;

import java.util.Locale;

import ru.yap.mobile.Contract.Forum;
import ru.yap.mobile.Contract.Read;
import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnClickListener;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.FragmentActivity;
import android.support.v4.widget.SlidingPaneLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.SearchView;

public class TopicActivity extends FragmentActivity implements ForumFragment.Callbacks, TopicFragment.Callbacks {
	
	private int FORUM_ID;
	private int TOPIC_ID;
	private int PAGE;
	private int MESSAGES;
	private String TITLE;
	private String DESCRIPTION;
	
	public static final int TAKE_PHOTO   = 0;
	public static final int CHOOSE_PHOTO = 1;
	
	private Menu menu;
	private SharedPreferences sharedPreferences;
	private SlidingPaneLayout mSlidingPaneLayout;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this); 
		
		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
			setTheme(android.R.style.Theme_Holo_Light);
		} else {
			setTheme(android.R.style.Theme_Holo);
		}
		
		super.onCreate(savedInstanceState);
		
		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
			setContentView(R.layout.topic_activity_light);	
		} else {
			setContentView(R.layout.topic_activity_dark);
		}
		
		mSlidingPaneLayout = (SlidingPaneLayout) findViewById(R.id.sliding_pane_layout);

		ActionBar actionBar = getActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(true);
		
		if (savedInstanceState == null) {
			Intent intent = getIntent();
			
			ForumFragment fragmentLeft = new ForumFragment();
	        Bundle argsL = new Bundle();
	        argsL.putInt("forum_id", intent.getIntExtra("forum_id", 1));
	        argsL.putInt("position", intent.getIntExtra("position", 1));
	        fragmentLeft.setArguments(argsL);
			getSupportFragmentManager().beginTransaction().add(
				R.id.frame_layout_left, 
				fragmentLeft, 
				"topic_activity_tag_" + intent.getIntExtra("forum_id", 1)
			).commit();
			
			setFragment(intent.getIntExtra("forum_id", 1), intent.getLongExtra("id", 1));
		} else {
			FORUM_ID = savedInstanceState.getInt("FORUM_ID");
			TOPIC_ID = savedInstanceState.getInt("TOPIC_ID");
			PAGE     = savedInstanceState.getInt("PAGE");
			MESSAGES = savedInstanceState.getInt("MESSAGES");
			
			TITLE = savedInstanceState.getString("TITLE");
			DESCRIPTION = savedInstanceState.getString("DESCRIPTION");
			getActionBar().setTitle(TITLE);
			if (DESCRIPTION.length() > 0) {
				getActionBar().setSubtitle(DESCRIPTION);
			} else {
				getActionBar().setSubtitle(null);	
			}
		}
	}
	
	@Override
	public void onItemSelected(int forum_id, int position, long id) {
		setFragment(forum_id, id);
		
		MenuItem mm = menu.findItem(R.id.menu_page);
		mm.setTitle("" + (PAGE + 1));
	}
	
	@Override
	public void onPageUpdate(int mode, int count) {
		int selected = 0;
		if (mode == 0) {
			if (count >= Integer.parseInt(sharedPreferences.getString("messages_count", "25"))) {
				PAGE = PAGE + 1;
			} else {
				selected = count + 1;
			}
		} else {
			PAGE = PAGE - 1;
		}

		MenuItem mm = menu.findItem(R.id.menu_page);
		mm.setTitle("" + (PAGE + 1));
		
		TopicFragment fragmentRight = new TopicFragment();
		Bundle args = new Bundle();
		args.putInt("topic_id", TOPIC_ID);
		args.putInt("page",     PAGE);
		args.putInt("selected", selected);
		fragmentRight.setArguments(args);
		getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_right, fragmentRight).commit();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
			getMenuInflater().inflate(R.menu.topic_light, menu);
		} else {
			getMenuInflater().inflate(R.menu.topic_dark, menu);
		}
		
		MenuItem mm = menu.findItem(R.id.menu_page);
		mm.setTitle("" + (PAGE + 1));
		
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		
	    this.menu = menu;
	    
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			super.onBackPressed();
			return true;
		case R.id.menu_preferences:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_create:
			startActivity(new Intent(this, CreateActivity.class));
			return true;
		case R.id.menu_page:
			int pageCount = 1;
			if (MESSAGES != 0) {
				pageCount = (int) Math.ceil(MESSAGES / Double.parseDouble(sharedPreferences.getString("messages_count", "25")));
			} 
			String[] pages = new String[pageCount];
			for(int i = 0; i < pageCount; i++) {
			    pages[i] = "" + (i + 1);
			}

			new AlertDialog.Builder(this).setSingleChoiceItems(pages, PAGE, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int index) {
					dialog.dismiss();
					PAGE = index;

					MenuItem mm = menu.findItem(R.id.menu_page);
					mm.setTitle("" + (PAGE + 1));
					
					TopicFragment fragmentRight = new TopicFragment();
					Bundle args = new Bundle();
					args.putInt("topic_id", TOPIC_ID);
					args.putInt("page",     PAGE);
					fragmentRight.setArguments(args);
					getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_right, fragmentRight).commit();
				}
			}).setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int id) {
					dialog.cancel();
				}
			}).setTitle(R.string.pages).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        outState.putString("TITLE",       TITLE);
        outState.putString("DESCRIPTION", DESCRIPTION);
        
        outState.putInt("FORUM_ID", FORUM_ID);
        outState.putInt("TOPIC_ID", TOPIC_ID);
        outState.putInt("PAGE",     PAGE);
        outState.putInt("MESSAGES", MESSAGES);
    }

	public void onBackPressed() {
		if (mSlidingPaneLayout.isOpen()) {
			super.onBackPressed();
		} else {
			mSlidingPaneLayout.openPane();
		}
	}

	
	public void onImageClick(View v) {
		String url = v.getTag().toString();
		if (url.substring(url.lastIndexOf(".")).toLowerCase(Locale.ENGLISH).equals(".gif")) {
			Intent intent = new Intent(this, GifActivity.class);
			intent.putExtra("url", url);
			startActivity(intent);
		} else {
			Intent intent = new Intent(this, ImageActivity.class);
			intent.putExtra("url", url);
			startActivity(intent);
		}
	}

	public void onPreviewClick(View v) {
		String[] url = v.getTag().toString().split("\\|");		
		if (url[0].equals("img")) {
			if (url[1].substring(url[1].lastIndexOf(".")).toLowerCase(Locale.ENGLISH).equals(".gif")) {
				Intent intent = new Intent(this, GifActivity.class);
				intent.putExtra("url", url[1]);
				startActivity(intent);
			} else {
				Intent intent = new Intent(this, ImageActivity.class);
				intent.putExtra("url", url[1]);
				startActivity(intent);
			}
		}
		if (url[0].equals("video")) {
			Intent intent = new Intent(this, VideoActivity.class);
			intent.putExtra("url", url[1]);
			startActivity(intent);			
		}
	}
	
	public void onRatingClick(View v) {

		String[] items = new String[] { "+ ", "-" };

		ListAdapter adapter = new ArrayAdapter<String>(
			this,
			R.layout.select_rating,
			R.id.label,
			items) {
			public View getView(int position, View convertView, ViewGroup parent) {
				View v = super.getView(position, convertView, parent);
				ImageView icon = (ImageView) v.findViewById(R.id.icon);
				if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
					if (position == 0) {
						icon.setImageResource(R.drawable.rating_good_light);
					} else {
						icon.setImageResource(R.drawable.rating_bad_light);
					}
				} else {
					if (position == 0) {
						icon.setImageResource(R.drawable.rating_good_dark);
					} else {
						icon.setImageResource(R.drawable.rating_bad_dark);
					}
				}
				return v;
			}
		};

		new AlertDialog.Builder(this).setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int item) {
				//...
			}
		}).setNegativeButton(R.string.discard, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int id) {
				dialog.cancel();
			}
		}).setTitle(R.string.rank).show();

	}
	
	public void onPhotoClick(View v) {

		String[] items = new String[] {
			getString(R.string.take_photo),
			getString(R.string.choose_photo)
		};

		ListAdapter adapter = new ArrayAdapter<String>(
			this,
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
		
		new AlertDialog.Builder(this).setAdapter(adapter, new DialogInterface.OnClickListener() {
			@Override
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

	public void onSendClick(View v) {
		//
	}

	public void onAvatarClick(View v) {
		//
	}

	public void setFragment(int forum_id, long id) {

		String[] projection = { 
			Forum.ID,
			Forum.MESSAGES_COUNT,
			Forum.TITLE, 
			Forum.DESCRIPTION
		};
		
		Cursor mCursor     = getContentResolver().query(Uri.withAppendedPath(Forum.CONTENT_URI, "" + forum_id), projection, "_id = " + id, null, null);
		mCursor.moveToFirst();
		int topic_id       = mCursor.getInt(0);
		int messages_count = mCursor.getInt(1);
		TITLE              = mCursor.getString(2);
		DESCRIPTION        = mCursor.getString(3);
		mCursor.close();
	
		FORUM_ID = forum_id;
		TOPIC_ID = topic_id;
		MESSAGES = messages_count;
		PAGE     = 0;
		
		getActionBar().setTitle(TITLE);
			
		if (DESCRIPTION.length() > 0) {
			getActionBar().setSubtitle(DESCRIPTION);
		} else {
			getActionBar().setSubtitle(null);
		}

		TopicFragment fragmentRight = new TopicFragment();
		Bundle args = new Bundle();
		args.putInt("topic_id", TOPIC_ID);
		args.putInt("page",     PAGE);
		args.putInt("selected", 0);
		fragmentRight.setArguments(args);
		getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout_right, fragmentRight).commit();

		if (mSlidingPaneLayout.isOpen()) {
			mSlidingPaneLayout.closePane();
		}
		
		new UpdateRead().execute(forum_id, topic_id, messages_count);
	}
	
    private class UpdateRead extends AsyncTask<Integer, Void, Void>{
        @Override
        protected Void doInBackground(Integer... params) {
    		ContentValues values = new ContentValues();
    		values.put(Read._ID, params[1]);
    		values.put(Read.MESSAGES_COUNT, params[2]);
    		getContentResolver().insert(Read.CONTENT_URI, values);
    		getContentResolver().notifyChange(Uri.withAppendedPath(Forum.CONTENT_URI, "" + params[0]), null);
        	return null;
        }
        
        @Override
        protected void onPostExecute(Void result) {
        	super.onPostExecute(result);
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