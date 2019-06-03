package com.example.notepad.notepad;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * 记事本bean
 * 即可重用 Java 组件，用于存储记事本的图片，内容，事件，id的组建信息
 */
public class ItemBean {
    Bitmap image;
    String content;
    String time;
    int id;

    public ItemBean() {
    }

    public ItemBean(int id, Bitmap bitmap, String content, String time) {
        this.id = id;
        this.image = bitmap;
        this.content = content;
        this.time = time;
    }

    public ItemBean(Bitmap bitmap, String content, String time) {
        this.image = bitmap;
        this.content = content;
        this.time = time;
    }

    public ItemBean(Context context, int resId, String content, String time) {
        Resources resources = context.getResources();
        this.image = BitmapFactory.decodeResource(resources, resId);
        this.content = content;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public String getContent() {
        return content;
    }

    public Bitmap getImage() {
        return image;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setImage(Bitmap image) {
        this.image = image;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
