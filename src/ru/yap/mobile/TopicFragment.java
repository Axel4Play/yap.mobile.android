package ru.yap.mobile;

import ru.yap.mobile.Contract.Topic;

import com.handmark.pulltorefresh.extras.listfragment.PullToRefreshListFragment;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.Mode;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class TopicFragment extends PullToRefreshListFragment {
	
	private SharedPreferences sharedPreferences;
	private ImageLoader imageLoader;
	private DisplayImageOptions optionsAva;
	private DisplayImageOptions optionsImg;
	private float density;
	private int width;
	private int selected;
	
	private Callbacks mCallbacks = sCallbacks;

	public interface Callbacks {
		public void onPageUpdate(int mode, int count);
	}
	
	private static Callbacks sCallbacks = new Callbacks() {
		@Override
		public void onPageUpdate(int mode, int count) {
		}
	};
	
	private String[] selectionArgs = { null, null };
	
	private SimpleCursorAdapter mAdapter;
	
	private static final String[] PROJECTION = {
		Topic._ID,
		Topic.CREATED,
		Topic.USERNAME,
		Topic.AVATAR,
		Topic.RATING,
		Topic.TEXT,
		Topic.HEIGHT,
		Topic.WIDTH,
		Topic.PREVIEW,
		Topic.ORIGINAL
	};
	
	private LoaderCallbacks<Cursor> loaderCallbacks = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
			return new CursorLoader(
				getActivity(),
				Topic.CONTENT_URI,
				PROJECTION,
				null,
				selectionArgs,
				null
			);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
			mAdapter.swapCursor(cursor);
			setListShown(true);
		}

		@Override
		public void onLoaderReset(Loader<Cursor> arg0) {
			mAdapter.swapCursor(null);
		}
	};
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mCallbacks = (Callbacks) activity;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		selectionArgs[0] = "" + getArguments().getInt("topic_id");
		selectionArgs[1] = "" + getArguments().getInt("page");
		selected = getArguments().getInt("selected");
		
		imageLoader = ImageLoader.getInstance();

		if (!imageLoader.isInited()) {
			imageLoader.init(new ImageLoaderConfiguration.Builder(getActivity())
				.discCacheFileNameGenerator(new YapURLFileNameGenerator())
				//.enableLogging()
				.build()
			);			
		}

		optionsAva = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.navigation_refresh_light)
			.showImageForEmptyUri(R.drawable.social_person_light)
			.showImageOnFail(R.drawable.alerts_and_states_warning_light)
			//.resetViewBeforeLoading()
			//.cacheInMemory()
			//.cacheOnDisc()
			.build();
		
		optionsImg = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.navigation_refresh_light)
			.showImageOnFail(R.drawable.alerts_and_states_warning_light)
			//.resetViewBeforeLoading()
			//.cacheOnDisc()
			.build();
		
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		DisplayMetrics displaymetrics = new DisplayMetrics();
		getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

		density = getResources().getDisplayMetrics().density;
		width = displaymetrics.widthPixels;
		
        if ((int) Math.floor(width / density) > 600) {
        	width = width - (int) Math.ceil(360 * density);	
        }

        width = width - (int) Math.ceil(16 * density);
        
		mAdapter = new SimpleCursorAdapter(
			getActivity(), 
			R.layout.message, 
			null, 
			new String[] {
				Topic._ID,
				Topic.CREATED, 
				Topic.USERNAME, 
				Topic.AVATAR,
				Topic.RATING,
				Topic.TEXT,
				Topic.PREVIEW
			},
			new int[] {
				R.id.item_vote,
				R.id.item_created,
				R.id.item_username,
				R.id.item_avatar,
				R.id.item_rating,
				R.id.item_text,
				R.id.item_image
			},
			0
		);
		
		mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
	        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {

	        	if (view.getId() == R.id.item_vote) {
	        		((ImageView) view).setTag(cursor.getString(columnIndex));
	        		if(sharedPreferences.getString("theme", "Theme_Holo_Light").equals("Theme_Holo_Light")) {
	        			if (cursor.getInt(cursor.getColumnIndex(Topic.RATING)) >= 0) {
	        				((ImageView) view).setImageResource(R.drawable.rating_good_light);
	        			} else {
	        				((ImageView) view).setImageResource(R.drawable.rating_bad_light);
	        			}
	        		} else {
	        			if (cursor.getInt(cursor.getColumnIndex(Topic.RATING)) >= 0) {
	        				((ImageView) view).setImageResource(R.drawable.rating_good_dark);
	        			} else {
	        				((ImageView) view).setImageResource(R.drawable.rating_bad_dark);
	        			}
	        		}
	        		return true;
	        	}
	        	
	        	if (view.getId() == R.id.item_avatar) {
	        		try {
        				imageLoader.displayImage(cursor.getString(columnIndex), ((ImageView) view), optionsAva);
       				} catch (Exception e) { }
	        		return true;
	        	}
	        	
	        	if (view.getId() == R.id.item_text) {
	        		((TextView) view).setText(Html.fromHtml(cursor.getString(columnIndex)));
	        		((TextView) view).setMovementMethod(LinkMovementMethod.getInstance());
	        		return true;
	        	}

	        	if (view.getId() == R.id.item_image) {
	        		if (cursor.getString(columnIndex).length() == 0) {
	        			((ImageView) view).setVisibility(View.GONE);
	        		} else {
	        			int imgWidth = cursor.getInt(cursor.getColumnIndex(Topic.WIDTH));
	        			int imgHeight = cursor.getInt(cursor.getColumnIndex(Topic.HEIGHT));
	        			int newWidth = Math.min(width, ((imgWidth * 2) + (int) Math.ceil(16 * density)));
        				imgHeight = (int) Math.ceil(16 * density) + (int) Math.ceil( (newWidth - (int) Math.ceil(16 * density)) / ((double) imgWidth / (double) imgHeight) ); 
        				imgWidth = newWidth;

	        			LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(imgWidth, imgHeight);
	        			params.gravity = Gravity.CENTER_HORIZONTAL;
						((ImageView) view).setLayoutParams(params);
	        			((ImageView) view).setVisibility(View.VISIBLE);
	        			((ImageView) view).setTag(cursor.getString(cursor.getColumnIndex(Topic.ORIGINAL)));
	        			try {
	        				imageLoader.displayImage(cursor.getString(columnIndex), ((ImageView) view), optionsImg);
	        			} catch (Exception e) { }
	        		}
	        		return true;
	        	}
	        	return false;
	        }
		});
		
		getPullToRefreshListView().setOnRefreshListener(new OnRefreshListener<ListView>() {
			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				int mode = 0;
				if (getPullToRefreshListView().getCurrentMode() == Mode.PULL_FROM_START) {
					mode = 1;
				}
				mCallbacks.onPageUpdate(mode, mAdapter.getCursor().getCount());
			}
		});
		
		if (Integer.parseInt(selectionArgs[1]) == 0) {
			getPullToRefreshListView().setMode(Mode.PULL_FROM_END);
		} else {
			getPullToRefreshListView().setMode(Mode.BOTH);
		}

		getListView().setDivider(new ColorDrawable(this.getResources().getColor(R.color.gray)));
		getListView().setDividerHeight(1);
		
		getListView().setOnScrollListener(new PauseOnScrollListener(imageLoader, false, true));
		
		setEmptyText(getResources().getString(R.string.empty_text));
		setListAdapter(mAdapter);
		setSelection(selected);
		setListShown(false);
		
		getLoaderManager().initLoader(0, null, loaderCallbacks);
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sCallbacks;
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