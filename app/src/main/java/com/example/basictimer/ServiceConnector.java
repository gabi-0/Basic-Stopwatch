package com.example.basictimer;

public class ServiceConnector {

    public static WatchService mService;

    public static void set(WatchService sv) {
        mService = sv;
    }

    public static WatchService getInstance() {
        return mService;
    }
}
