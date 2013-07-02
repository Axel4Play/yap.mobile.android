package ru.yap.mobile;

import ru.yap.mobile.RestRequestManager;
import ru.yap.mobile.Contract.Forum;
import ru.yap.mobile.RequestFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.requestmanager.RequestManager.RequestListener;

import com.handmark.pulltorefresh.extras.listfragment.PullToRefreshListFragment;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.nostra13.universalimageloader.cache.disc.naming.FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.PauseOnScrollListener;

public class ForumFragment extends PullToRefreshListFragment {

	private Callbacks mCallbacks = sCallbacks;

	public interface Callbacks {
		public void onItemSelected(int forum_id, int position, long id);
	}
	
	private static Callbacks sCallbacks = new Callbacks() {
		@Override
		public void onItemSelected(int forum_id, int position, long id) {
		}
	};
	
	private RestRequestManager requestManager;
	private SimpleCursorAdapter mAdapter;
	private ImageLoader imageLoader;
	private DisplayImageOptions options;
	
	private boolean LOADING = false;
	private int FORUM_ID;
	
	private static final String[] PROJECTION = {
		Forum._ID,
		Forum.ID,
		Forum.TITLE,
		Forum.DESCRIPTION,
		Forum.RATING,
		Forum.ORIGINAL,
		Forum.PREVIEW,
		Forum.PREVIEW_TYPE,
		Forum.MESSAGES_COUNT,
		Forum.USER_NAME,
		Forum.MESSAGES_READ
	};

	RequestListener requestListener = new RequestListener() {

		@Override
		public void onRequestFinished(Request request, Bundle resultData) {
			getPullToRefreshListView().onRefreshComplete();
		}

		void showError(String text) {
			getPullToRefreshListView().onRefreshComplete();
			new AlertDialog.Builder(getActivity())
				.setTitle(android.R.string.dialog_alert_title)
				.setMessage(text)
				.setCancelable(false)
				.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
						setListShown(true);
					}
				})
				.create()
				.show();
		}

		@Override
		public void onRequestDataError(Request request) {
			showError(getString(R.string.error_data));
		}

		@Override
		public void onRequestConnectionError(Request request, int statusCode) {
			showError(getString(R.string.error_connection));
		}
		
		@Override
		public void onRequestCustomError(Request request, Bundle resultData) {
			showError(getString(R.string.error_custom));
		}
	};
	
	private LoaderCallbacks<Cursor> loaderCallbacks = new LoaderCallbacks<Cursor>() {
		@Override
		public Loader<Cursor> onCreateLoader(int loaderId, Bundle arg1) {
			return new CursorLoader(
				getActivity(),
				Uri.withAppendedPath(Forum.CONTENT_URI, "" + FORUM_ID),
				PROJECTION,
				null,
				null,
				null
			);
		}

		@Override
		public void onLoadFinished(Loader<Cursor> arg0, Cursor cursor) {
			mAdapter.swapCursor(cursor);
			if (cursor.getCount() == 0) {
				setListShown(false);
				if (!LOADING) {
					update();
				}
			} else {
				LOADING = false;
				setListShown(true);
			}
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
		
		if (savedInstanceState == null) {
			FORUM_ID = getArguments().getInt("forum_id");
		} else {
			FORUM_ID = savedInstanceState.getInt("FORUM_ID");
			LOADING  = savedInstanceState.getBoolean("LOADING");
		}

		imageLoader = ImageLoader.getInstance();

		if (!imageLoader.isInited()) {
			imageLoader.init(new ImageLoaderConfiguration.Builder(getActivity())
				.discCacheFileNameGenerator(new YapURLFileNameGenerator())
				//.enableLogging()
				.build()
			);			
		}

		options = new DisplayImageOptions.Builder()
			.showStubImage(R.drawable.navigation_refresh_light)
			.showImageOnFail(R.drawable.alerts_and_states_warning_light)
			//.resetViewBeforeLoading()
			//.cacheInMemory()
			//.cacheOnDisc()
			.build();
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		requestManager = RestRequestManager.from(getActivity());
		
		mAdapter = new SimpleCursorAdapter(
			getActivity(), 
			R.layout.topic, 
			null, 
			new String[] {
				Forum._ID,
				Forum.TITLE,
				Forum.DESCRIPTION,
				Forum.RATING,
				Forum.PREVIEW
			},
			new int[] {
				R.id.item_ext,
				R.id.item_title,
				R.id.item_description,
				R.id.item_rating,
				R.id.item_thumbnail
			}, 
			0
		);

		mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
	        public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
	        	boolean newTopic = cursor.isNull(cursor.getColumnIndex(Forum.MESSAGES_READ));
	        	
	        	if (newTopic) {
	        		view.setAlpha(1.0f);	        		
	        	} else {
	        		view.setAlpha(0.5f);
	        	}
	        	
	        	if (view.getId() == R.id.item_ext) {
	        		int newCount = cursor.getInt(cursor.getColumnIndex(Forum.MESSAGES_COUNT)) - cursor.getInt(cursor.getColumnIndex(Forum.MESSAGES_READ));
	        		if (newTopic || newCount == 0) {
	        			((TextView) view).setText(" • " + cursor.getString(cursor.getColumnIndex(Forum.USER_NAME)) + " • " + cursor.getString(cursor.getColumnIndex(Forum.MESSAGES_COUNT)) );
	        		} else {
	        			((TextView) view).setText(" • " + cursor.getString(cursor.getColumnIndex(Forum.USER_NAME)) + " • " + cursor.getString(cursor.getColumnIndex(Forum.MESSAGES_COUNT)) + " (+" + newCount + ")");
	        		}
	        		return true;
	        	}
	        	
	        	if (view.getId() == R.id.item_description) {
	        		if (cursor.getString(columnIndex).length() == 0) {
	        			((TextView) view).setVisibility(View.GONE);
	        		} else {
	        			((TextView) view).setVisibility(View.VISIBLE);
	        		}
	        	}
	        	
	        	if (view.getId() == R.id.item_thumbnail) {
	        		if (cursor.getString(columnIndex).length() == 0 || cursor.isNull(columnIndex) || cursor.getString(columnIndex).equals("null")) {
	        			((ImageView) view).setVisibility(View.GONE);
	        		} else {
	        			((ImageView) view).setVisibility(View.VISIBLE);
	        			((ImageView) view).setTag(cursor.getString(cursor.getColumnIndex(Forum.PREVIEW_TYPE)) + "|" + cursor.getString(cursor.getColumnIndex(Forum.ORIGINAL)));
	        			try {
	        				imageLoader.displayImage(cursor.getString(columnIndex), ((ImageView) view), options);
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
				update();
			}
		});

		getListView().setOnScrollListener(new PauseOnScrollListener(imageLoader, false, true));

		setEmptyText(getResources().getString(R.string.empty_text));
		setListAdapter(mAdapter);
		setListShown(false);

		getLoaderManager().initLoader(FORUM_ID, null, loaderCallbacks);

		if (savedInstanceState == null) {
			setSelection(getArguments().getInt("position"));
		}
	}
	
	@Override
    public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
        outState.putBoolean("LOADING", LOADING);
        outState.putInt("FORUM_ID", FORUM_ID);
    }
	
	@Override
	public void onDetach() {
		super.onDetach();
		mCallbacks = sCallbacks;
	}

	public void onListItemClick(ListView l, View v, int position, long id) {
		if (!LOADING) {
			mCallbacks.onItemSelected(FORUM_ID, position, id);
		}
	}

	void update() {
		LOADING = true;
		getPullToRefreshListView().setRefreshing();
		
		getActivity().getContentResolver().delete(Uri.withAppendedPath(Contract.Forum.CONTENT_URI, "" + FORUM_ID), null, null);
		getActivity().getContentResolver().notifyChange(Uri.withAppendedPath(Forum.CONTENT_URI, "" + FORUM_ID), null);
		
		Request updateRequest = new Request(RequestFactory.REQUEST_FORUM);
		updateRequest.put("forum_id", FORUM_ID);
		requestManager.execute(updateRequest, requestListener);
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