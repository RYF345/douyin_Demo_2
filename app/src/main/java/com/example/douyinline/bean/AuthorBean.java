package com.example.douyinline.bean;

import java.io.Serializable;

public class AuthorBean implements Serializable {
    private String authorName;
    private int authorAvatar;

    public AuthorBean(String authorName, int authorAvatar) {
        this.authorName = authorName;
        this.authorAvatar = authorAvatar;
    }

    public int getAuthorAvatar() {
        return authorAvatar;
    }

    public void setAuthorAvatar(int authorAvatar) {
        this.authorAvatar = authorAvatar;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
