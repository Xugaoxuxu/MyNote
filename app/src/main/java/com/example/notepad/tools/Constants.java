package com.example.notepad.tools;

/**
 * 常量表
 */

public class Constants {
    public static final String TABLE_NAME = "notepadTable";

    public static final String TABLE_ID = "_id";
    public static final String DB_NAME = "notepad.db";
    public static final String KEY_CONTENT = "content";
    public static final String KEY_IMAGE = "image";
    public static final String KEY_TIME = "time";
    public static final String KEY_ID = "_id";
    public static final String IMG_DIR = "NotePad_IMG";

    public static final String INTENT_NOTEID = "noteId";
    /**
     * 按两次才离开的最大允许间隔时间
     **/
    public static final long EXIT_TIME = 3000;

    /**
     * 图文混排里图片的大小边界，大于这个值则为大图，需要缩小，否则需要扩大
     **/
    public static final int SPANTEXT_BIG_PIC_LEAST_WIDTH = 2000;
    public static final int SPANTEXT_BIG_PIC_LEAST_HEIGHT = 2000;

    /**
     * 笔记的图标大小
     **/
    public static final int ICO_PIC_WIDTH = 170;
    public static final int ICO_PIC_HEIGHT = 150;

    /**
     * 从文本里匹配图片的正则
     **/
    public static final String PicPatten = "/storage/.*?\\.\\.*(jpg|png|gif|jpeg|WebP)";
}
