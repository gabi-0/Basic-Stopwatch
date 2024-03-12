package com.example.basictimer;

public class NotifyManager {
    public static final int NOTIFICATION_ID = 1001;
    public static final String CHANNEL_ID = "fg_1001";
    public static final String CHANNEL_NAME = "Stopwatch";

    public static final String ACTION_PLAY = "play";
    public static final String ACTION_PAUSE = "pause";
    public static final String ACTION_STOP = "stop";

    public static final int REQ_PLAY = 1;
    public static final int REQ_PAUSE = 2;
    public static final int REQ_STOP = 3;
}
