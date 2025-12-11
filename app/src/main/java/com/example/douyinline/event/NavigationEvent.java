package com.example.douyinline.event;


public class NavigationEvent {
    private int position;
    private int coverResId;  // 封面资源 ID，用于转场动画

    public NavigationEvent(int position) {
        this.position = position;
        this.coverResId = 0;
    }

    public NavigationEvent(int position, int coverResId) {
        this.position = position;
        this.coverResId = coverResId;
    }

    public int getPosition() {
        return position;
    }

    public int getCoverResId() {
        return coverResId;
    }
}
