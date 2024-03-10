package com.example.basictimer;

interface ServiceCallback {
    public void onStart();
    public void onPause();
    public void onStop();
    public void onUpdate(long dif);
}
