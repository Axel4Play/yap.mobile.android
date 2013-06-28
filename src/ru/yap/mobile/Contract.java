package ru.yap.mobile;

import android.net.Uri;
import android.provider.BaseColumns;

public final class Contract {

	public static final String AUTHORITY = "ru.yap.mobile";

	public static final Uri AUTHORITY_URI = Uri.parse("content://" + AUTHORITY);

	public interface ForumColumns {
		public static final String ID             = "id";
		public static final String TITLE          = "title";
		public static final String DESCRIPTION    = "description";
		public static final String RATING         = "rating";
		public static final String ORIGINAL       = "original";
		public static final String PREVIEW        = "preview";
		public static final String PREVIEW_TYPE   = "preview_type";
		public static final String CREATED_AT     = "created_at";
		public static final String MESSAGES_COUNT = "messages_count";
		public static final String USER_NAME      = "user_name";
		public static final String MESSAGES_READ  = "messages_count_read";
	}

	public static final class Forum implements BaseColumns, ForumColumns {
		public static final String CONTENT_PATH = "forum";
		public static final Uri    CONTENT_URI  = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CONTENT_PATH;
	}

	
	
	public interface TopicColumns {
		public static final String CREATED  = "created_at";
		public static final String USERNAME = "user_name";
		public static final String AVATAR   = "avatar";
		public static final String RATING   = "rating";
		public static final String TEXT     = "text";
		public static final String HEIGHT   = "height";
		public static final String WIDTH    = "width";
		public static final String PREVIEW  = "preview";
		public static final String ORIGINAL = "original";
	}

	public static final class Topic implements BaseColumns, TopicColumns {
		public static final String CONTENT_PATH = "topic";
		public static final Uri    CONTENT_URI  = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CONTENT_PATH;
	}
	
	
	
	public interface ReadColumns {
		public static final String MESSAGES_COUNT = "messages_count";
	}
	
	public static final class Read implements BaseColumns, ReadColumns {
		public static final String CONTENT_PATH = "read";
		public static final Uri    CONTENT_URI  = Uri.withAppendedPath(AUTHORITY_URI, CONTENT_PATH);
		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd." + AUTHORITY + "." + CONTENT_PATH;
	}
	
}

//EOF