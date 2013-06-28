package ru.yap.mobile;

import java.util.Locale;

import android.app.ActionBar;
import android.app.SearchManager;
import android.app.ActionBar.OnNavigationListener;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SearchView;

public class ForumActivity extends FragmentActivity implements ForumFragment.Callbacks {

	SharedPreferences sharedPreferences;
	
	private int MENU_ID;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
			setTheme(android.R.style.Theme_Holo_Light);
		} else {
			setTheme(android.R.style.Theme_Holo);
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.forum_activity);		
		
		if (savedInstanceState == null) {
			MENU_ID = getIntent().getIntExtra("menu_id", 1);
			setFragment();
		} else {
			MENU_ID = savedInstanceState.getInt("MENU_ID");
		}
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
			getBaseContext(), 
			android.R.layout.simple_spinner_dropdown_item, 
			getResources().getStringArray(R.array.forum)
		);
		
		ActionBar.OnNavigationListener navigationListener = new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int position, long id) {
				if (MENU_ID != position) {
					if (position == 0) {
						startActivity(new Intent(getBaseContext(), MainActivity.class));
					} else {
			    		MENU_ID = position;
						setFragment();
					}
				}
				return true;
			}
		};
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(adapter, navigationListener);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setSelectedNavigationItem(MENU_ID);
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        outState.putInt("MENU_ID", MENU_ID);
    }
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
			getMenuInflater().inflate(R.menu.forum_light, menu);
		} else {
			getMenuInflater().inflate(R.menu.forum_dark, menu);
		}
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			startActivity(new Intent(getBaseContext(), MainActivity.class));
			return true;
		case R.id.menu_preferences:
			startActivity(new Intent(this, SettingsActivity.class));
			return true;
		case R.id.menu_create:
			startActivity(new Intent(this, CreateActivity.class));
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onItemSelected(int forum_id, int position, long id) {
		Intent intent = new Intent(this, TopicActivity.class);
		intent.putExtra("forum_id", forum_id);
		intent.putExtra("position", position);
		intent.putExtra("id", id);
		startActivity(intent);
	}
	
	public void setFragment() {
		ForumFragment fragment = new ForumFragment();
        Bundle args = new Bundle();
        args.putInt("selected", 1);
        args.putInt("forum_id", getResources().getIntArray(R.array.forum_id)[MENU_ID]);
        fragment.setArguments(args);
		getSupportFragmentManager().beginTransaction().replace(
			R.id.frame_layout, 
			fragment, 
			"forum_activity_tag_" + getResources().getIntArray(R.array.forum_id)[MENU_ID]
		).commit();
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
}

//EOF
