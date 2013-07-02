package ru.yap.mobile;

import java.util.HashMap;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import ru.yap.mobile.Contract.Forum;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;

import com.foxykeep.datadroid.exception.ConnectionException;
import com.foxykeep.datadroid.exception.CustomRequestException;
import com.foxykeep.datadroid.exception.DataException;
import com.foxykeep.datadroid.network.NetworkConnection;
import com.foxykeep.datadroid.network.NetworkConnection.ConnectionResult;
import com.foxykeep.datadroid.requestmanager.Request;
import com.foxykeep.datadroid.service.RequestService.Operation;

public final class ForumOperation implements Operation {
	
	@Override
	public Bundle execute(Context context, Request request) throws ConnectionException, DataException, CustomRequestException {
		
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		
		NetworkConnection connection = new NetworkConnection(context, sharedPreferences.getString("api_url", "http://api.m-yap.ru") + "/v1/forum/" + request.getInt("forum_id") + ".json");

		HashMap<String, String> params = new HashMap<String, String>();
		params.put("count", sharedPreferences.getString("topic_count", "100"));
		connection.setParameters(params);
		
		try {
		
			ConnectionResult result = connection.execute();
			ContentValues[] forumValues;
		
			try {
				JSONObject bodyJson = new JSONObject(result.body);
				JSONArray forumJson = bodyJson.getJSONArray("data");
			
				forumValues = new ContentValues[forumJson.length()];

				for (int i = 0; i < forumJson.length(); ++i) {
					ContentValues topic = new ContentValues();
					topic.put("id", forumJson.getJSONObject(i).getInt("id"));
					topic.put("title", forumJson.getJSONObject(i).getString("title"));
					topic.put("description", forumJson.getJSONObject(i).getString("description"));
					topic.put("rating", forumJson.getJSONObject(i).getInt("rating"));
					topic.put("preview_type", forumJson.getJSONObject(i).getString("preview_type"));
					topic.put("preview", forumJson.getJSONObject(i).getString("preview"));
					topic.put("original", forumJson.getJSONObject(i).getString("original"));
					topic.put("created_at", forumJson.getJSONObject(i).getInt("created_at"));
					topic.put("messages_count", forumJson.getJSONObject(i).getInt("messages_count"));
					topic.put("user_name", forumJson.getJSONObject(i).getString("user_name"));
					forumValues[i] = topic;
				}
			
			} catch (JSONException e) {
				throw new DataException(e.getMessage());
			}

			//context.getContentResolver().delete(Uri.withAppendedPath(Contract.Forum.CONTENT_URI, "" + request.getInt("forum_id")), null, null);
			//context.getContentResolver().notifyChange(Uri.withAppendedPath(Forum.CONTENT_URI, "" + request.getInt("forum_id")), null);
			context.getContentResolver().bulkInsert(Uri.withAppendedPath(Contract.Forum.CONTENT_URI, "" + request.getInt("forum_id")), forumValues);
			context.getContentResolver().notifyChange(Uri.withAppendedPath(Forum.CONTENT_URI, "" + request.getInt("forum_id")), null);
			
		} catch (ConnectionException e1) {
			throw new ConnectionException(e1.getMessage());
		}

		return null;
	}

}

//EOF