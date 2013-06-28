package ru.yap.mobile;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;

import ru.yap.mobile.Contract.Forum;
import ru.yap.mobile.Contract.Read;
import ru.yap.mobile.Contract.Topic;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.util.Log;

public class RestProvider extends ContentProvider {

	private static final int[] forum_id = {
		 1,  2,  3,  4,  5,  6,  7,  8,  9, 11, 13, 14, 16, 17, 18, 23, 24, 
		25, 26, 27, 28, 29, 30, 31, 32, 33, 35, 36, 37, 38, 40, 41, 42, 43,
		44, 45, 1001, 1002, 1003, 1004, 1005, 1006, 1007, 1008, 1009, 1010
	};

	private static final String DB_NAME = "yap.db";
	private static final int DB_VERSION = 1;

	private static final UriMatcher sUriMatcher;

	private static final int PATH_FORUM = 1;
	private static final int PATH_TOPIC = 2;
	private static final int PATH_READ  = 3;
	

	static {
		sUriMatcher = new UriMatcher(0);
		sUriMatcher.addURI(Contract.AUTHORITY, "forum/#", PATH_FORUM);
		sUriMatcher.addURI(Contract.AUTHORITY, "topic", PATH_TOPIC);
		sUriMatcher.addURI(Contract.AUTHORITY, "read",  PATH_READ);
	}

	private DatabaseHeloper mDatabaseHelper;

	class DatabaseHeloper extends SQLiteOpenHelper {

		public DatabaseHeloper(Context context, String name, CursorFactory factory, int version) {
			super(context, name, factory, version);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL("create table read (" + 
				Read._ID             + " integer primary key, " +
				Read.MESSAGES_COUNT  + " integer " +
			")");
			for (int i = 0; i < forum_id.length; i++) {
				String sql = 
				"create table forum" + forum_id[i] + " (" + 
					Forum._ID            + " integer primary key autoincrement, " +
					Forum.ID             + " integer, " +
					Forum.TITLE          + " text, " +
					Forum.DESCRIPTION    + " text, " +
					Forum.RATING         + " integer, " +
					Forum.ORIGINAL       + " text, " +
					Forum.PREVIEW        + " text, " +
					Forum.PREVIEW_TYPE   + " text, " +
					Forum.CREATED_AT     + " integer, " +
					Forum.MESSAGES_COUNT + " integer, " +
					Forum.USER_NAME      + " text, " +
					Forum.MESSAGES_READ  + " integer " +
				")";
				db.execSQL(sql);
				db.execSQL("CREATE INDEX forum" + forum_id[i] + "_idx ON forum" + forum_id[i] + "(id)");
			}
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		}

	}

	@Override
	public boolean onCreate() {
		mDatabaseHelper = new DatabaseHeloper(getContext(), DB_NAME, null, DB_VERSION);
		return true;
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
		switch (sUriMatcher.match(uri)) {
		case PATH_FORUM: {
			Cursor cursor = mDatabaseHelper.getReadableDatabase().query("forum" + uri.getLastPathSegment(), projection, selection, selectionArgs, null, null, sortOrder);
			cursor.setNotificationUri(getContext().getContentResolver(), Uri.withAppendedPath(Forum.CONTENT_URI, uri.getLastPathSegment()));
			return cursor;
		}
		case PATH_TOPIC: {
			
			String[] columns = {
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
	        MatrixCursor cursor = new MatrixCursor(columns);
	        
			NetworkConnection connection = new NetworkConnection(getContext(), "http://api.m-yap.ru/v1/topic/" + selectionArgs[0] + ".json");
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
			
			HashMap<String, String> params = new HashMap<String, String>();
			params.put("page",  selectionArgs[1]);
			params.put("count", sharedPreferences.getString("messages_count", "25"));
			connection.setParameters(params);
			
			try {
				
				ConnectionResult result = connection.execute();
				
				try {
					JSONObject bodyJson = new JSONObject(result.body);
					JSONArray forumJson = bodyJson.getJSONArray("data");
					
					for (int i = 0; i < forumJson.length(); ++i) {
						cursor.addRow(new String[] {
							"" + forumJson.getJSONObject(i).getInt("id"),
							forumJson.getJSONObject(i).getString(Topic.CREATED),
							forumJson.getJSONObject(i).getString(Topic.USERNAME),
							forumJson.getJSONObject(i).getString(Topic.AVATAR),
							"" + forumJson.getJSONObject(i).getInt(Topic.RATING), 
							forumJson.getJSONObject(i).getString(Topic.TEXT),
							"" + forumJson.getJSONObject(i).getInt(Topic.HEIGHT),
							"" + forumJson.getJSONObject(i).getInt(Topic.WIDTH),
							forumJson.getJSONObject(i).getString(Topic.PREVIEW),
							forumJson.getJSONObject(i).getString(Topic.ORIGINAL)
						});
					}

				} catch (JSONException e) {
					Log.d("test", "JSONException");
				}

			} catch (ConnectionException e1) {
				Log.d("test", "ConnectionException");
			}

			return cursor;
		}
		default:
			return null;
		}
	}

	@Override
	public String getType(Uri uri) {
		switch (sUriMatcher.match(uri)) {
		case PATH_FORUM:
			return Forum.CONTENT_TYPE;
		case PATH_READ:
			return Read.CONTENT_TYPE;
		default:
			return null;
		}
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		switch (sUriMatcher.match(uri)) {
		case PATH_FORUM: {
			mDatabaseHelper.getWritableDatabase().insert("forum" + uri.getLastPathSegment(), null, values);
			mDatabaseHelper.getWritableDatabase().execSQL("update forum" + uri.getLastPathSegment() + " set " + Forum.MESSAGES_READ + " = (select " + Read.MESSAGES_COUNT + " from read WHERE " + Read._ID + " = " + values.getAsString(Forum.ID) + ") WHERE " + Forum.ID + " = " + values.getAsString(Forum.ID));
			//getContext().getContentResolver().notifyChange(Uri.withAppendedPath(Forum.CONTENT_URI, uri.getLastPathSegment()), null);
			return null;
		}
		case PATH_READ: {
			mDatabaseHelper.getWritableDatabase().delete("read", "_id = " + values.getAsString(Read._ID), null);
			mDatabaseHelper.getWritableDatabase().insert("read", null, values);
			for (int i = 0; i < forum_id.length; i++) {
			    ContentValues args = new ContentValues();
			    args.put(Forum.MESSAGES_READ, values.getAsString(Read.MESSAGES_COUNT));
			    mDatabaseHelper.getWritableDatabase().update("forum" + forum_id[i], args, "id =" + values.getAsString(Read._ID), null);
			    //getContext().getContentResolver().notifyChange(Uri.withAppendedPath(Forum.CONTENT_URI, "" + forum_id[i]), null);
			}
			return null;
		}
		default:
			return null;
		}
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case PATH_FORUM: {
			return mDatabaseHelper.getWritableDatabase().delete("forum" + uri.getLastPathSegment(), selection, selectionArgs);
		}
		default:
			return 0;
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		switch (sUriMatcher.match(uri)) {
		case PATH_FORUM:
			return mDatabaseHelper.getWritableDatabase().update("forum" + uri.getLastPathSegment(), values, selection, selectionArgs);
		default:
			return 0;
		}
	}

}

//EOF