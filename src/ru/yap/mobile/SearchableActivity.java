package ru.yap.mobile;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.widget.Toast;

public class SearchableActivity extends Activity {

	SharedPreferences sharedPreferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
		
		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
			setTheme(android.R.style.Theme_Holo_Light);
		} else {
			setTheme(android.R.style.Theme_Holo);
		}
		
		super.onCreate(savedInstanceState);
	    setContentView(R.layout.search_activity);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		
	    handleIntent(getIntent());
	}
	
    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }
	
    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            String query = intent.getStringExtra(SearchManager.QUERY);
            Toast.makeText(this, query, Toast.LENGTH_SHORT).show();
        }
    }
    
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			super.onBackPressed();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
    
}

//EOF