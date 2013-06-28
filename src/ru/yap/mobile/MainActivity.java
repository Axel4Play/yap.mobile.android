package ru.yap.mobile;

import java.util.Locale;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.SearchManager;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class MainActivity extends FragmentActivity implements ForumFragment.Callbacks {

	SectionsPagerAdapter sectionsPagerAdapter;
	ViewPager viewPager;
	SharedPreferences sharedPreferences;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
			setTheme(android.R.style.Theme_Holo_Light);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main_activity);
		} else {
			setTheme(android.R.style.Theme_Holo);
			super.onCreate(savedInstanceState);
			setContentView(R.layout.main_activity);
		}
		
		sectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
		viewPager = (ViewPager) findViewById(R.id.pager);
		viewPager.setAdapter(sectionsPagerAdapter);
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
				if (position != 0) {
					Intent intent = new Intent(getBaseContext(), ForumActivity.class);
					intent.putExtra("menu_id", position);
					startActivity(intent);
				}
				return true;
			}
		};
		
		ActionBar actionBar = getActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		actionBar.setListNavigationCallbacks(adapter, navigationListener);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayHomeAsUpEnabled(false);
		actionBar.setSelectedNavigationItem(0);
	}

	@Override
	public void onItemSelected(int forum_id, int position, long id) {
		Intent intent = new Intent(this, TopicActivity.class);
		intent.putExtra("forum_id", forum_id);
		intent.putExtra("position", position);
		intent.putExtra("id", id);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
			getMenuInflater().inflate(R.menu.main_light, menu);	
		} else {
			getMenuInflater().inflate(R.menu.main_dark, menu);
		}
	    SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
	    SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
	    searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
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
	
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int index) {
			Fragment fragment = new ForumFragment();
	        Bundle args = new Bundle();
	        args.putInt("selected", 1);
	        args.putInt("forum_id", getResources().getIntArray(R.array.lenta_id)[index]);
	        fragment.setArguments(args);
			return fragment;
		}

		@Override
		public int getCount() {
			return getResources().getIntArray(R.array.lenta_id).length;
		}

		@Override
		public CharSequence getPageTitle(int index) {
			return getResources().getStringArray(R.array.lenta)[index];
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
	
}

//EOF