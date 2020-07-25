package com.creator.qweekdots.app;

public class AppConfig {
	// Server user login url
	public static String URL_LOGIN = "https://qweek.fun/genjitsu/signin.php";

	// Server user register url
	public static String URL_REGISTER = "https://qweek.fun/genjitsu/signup.php";

	//Server post text url
	public static String URL_DROP_TEXT = "https://qweek.fun/genjitsu/parse/droptext.php";

	//Server post gif url
	public static String URL_DROP_GIF = "https://qweek.fun/genjitsu/parse/dropgif.php";

	//Server post qweeksnap/qweekpic url
	public static String URL_DROP_QWEEKPIC = "https://qweek.fun/genjitsu/parse/dropqp.php";

	//Server post qweeksnap/qweekvid url
	public static String URL_DROP_QWEEKVID = "https://qweek.fun/genjitsu/parse/dropqv.php";

	//Server post audio url
	public static String URL_DROP_AUDIO = "https://qweek.fun/genjitsu/parse/dropaudio.php";

	//Server post comment url
	public static String URL_COMMENT_TEXT = "https://qweek.fun/genjitsu/parse/comment.php";

	// Server like url
	public static String URL_LIKE = "https://qweek.fun/genjitsu/parse/droplike.php";

	// Server upvote url
	public static String URL_UPVOTE = "https://qweek.fun/genjitsu/parse/drop_upvote.php";

	// Server downvote url
	public static String URL_DOWNVOTE = "https://qweek.fun/genjitsu/parse/drop_downvote.php";

	// Server follow url
	public static String URL_FOLLOW = "https://qweek.fun/genjitsu/parse/follow.php";

	// Server profile settings url
	public static String URL_PROFILE_SETTINGS = "https://qweek.fun/genjitsu/parse/profile_settings.php";

	// Server delete url
	public static String URL_DELETE = "https://qweek.fun/genjitsu/parse/delete.php";

	// flag to identify whether to show single line
	// or multi line text in push notification tray
	public static boolean appendNotificationMessages = true;

	// global topic to receive app wide push notifications
	public static final String TOPIC_GLOBAL = "global";

	// broadcast receiver intent filters
	public static final String SENT_TOKEN_TO_SERVER = "sentTokenToServer";
	public static final String REGISTRATION_COMPLETE = "registrationComplete";
	public static final String PUSH_NOTIFICATION = "pushNotification";

	// type of push messages
	public static final int PUSH_TYPE_CHATROOM = 1;
	public static final int PUSH_TYPE_USER = 2;

	// id to handle the notification in the notification try
	public static final int NOTIFICATION_ID = 100;
	public static final int NOTIFICATION_ID_BIG_IMAGE = 101;

}
