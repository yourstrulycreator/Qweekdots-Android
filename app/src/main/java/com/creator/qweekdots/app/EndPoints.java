package com.creator.qweekdots.app;

public class EndPoints {

    // The EndPoint APIs for the QweekChat API
    // localhost url -
    private static final String BASE_URL = "https://qweek.fun/genjitsu/chat";
    public static final String USER = BASE_URL + "/user/_ID_";
    public static final String PLAYER = BASE_URL + "/player/_ID_";
    public static final String CHAT_ROOMS = BASE_URL + "/chat_rooms";
    public static final String HOT_CHAT_ROOMS = BASE_URL + "/hot_chat_rooms";
    public static final String MY_CHAT_ROOMS = BASE_URL + "/my_chat_rooms/_ID_";
    public static final String USER_CHAT_ROOMS = BASE_URL + "/user_chat_rooms/_ID_";
    public static final String CHAT_ROOM_EXISTS = BASE_URL + "/check_chat_room_exists";
    public static final String CHAT_THREAD = BASE_URL + "/chat_rooms/_ID_";
    public static final String CHAT_ROOM_MESSAGE = BASE_URL + "/chat_rooms/_ID_/message";
    public static final String CHAT_USER_MESSAGE = BASE_URL + "/users/_ID_/message";
}
