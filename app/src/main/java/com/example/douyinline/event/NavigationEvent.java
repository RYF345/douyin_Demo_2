package com.example.douyinline.event;


public class NavigationEvent {
    private int position;

    public NavigationEvent(int position) {
        this.position = position;
    }

    public int getPosition() {
        return position;
    }
}
