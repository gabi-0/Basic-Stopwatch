package com.example.basictimer;

interface ServiceCallback {
    public void onStart(long dif);
    public void onPause(long dif);
    public void onStop();
    public void onUpdate(long dif);
}
