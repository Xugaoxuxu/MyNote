package com.example.notepad.notepad;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

import com.example.notepad.tools.BitMapUtil;
import com.example.notepad.tools.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * 用于连接数据库的工具类
 */
public class DbManager {
    private static DbManager instance = null;
    private static DbHelper helper = null;
    private static SQLiteDatabase dbReadable = null;
    private static SQLiteDatabase dbWritable = null;

    public static DbManager getInstance(Context context) {
        if (instance == null) {
            synchronized (DbManager.class) {
                if (instance == null) {
                    instance = new DbManager();
                    helper = new DbHelper(context, Constants.DB_NAME, null, 1);
                    dbReadable = helper.getReadableDatabase();
                    dbWritable = helper.getWritableDatabase();
                }
            }
        }
        return instance;
    }

    private DbManager() {
    }

    /**
     * 新增日记
     *
     * @param icoPath 图标路径
     * @param content 正文
     * @param time    时间
     */
    public void addNote(String icoPath, String content, String time) {
        ContentValues values = new ContentValues();
        values.put(Constants.KEY_IMAGE, icoPath);
        values.put(Constants.KEY_CONTENT, content);
        values.put(Constants.KEY_TIME, time);
        dbWritable.insert(Constants.TABLE_NAME, null, values);
        values.clear();
    }

    /**
     * 查数据库，并返回list结果
     */
    public List<ItemBean> getDataList() {
        List<ItemBean> list = new ArrayList<ItemBean>();
        Cursor cursor
                = dbReadable.rawQuery("SELECT * FROM " + Constants.TABLE_NAME, null);
        try {
            while (cursor.moveToNext()) {
                ItemBean note = new ItemBean();
                note.setId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_ID)));
                Bitmap bitmap = BitMapUtil.getBitmap(cursor.getString(cursor.getColumnIndex(Constants.KEY_IMAGE)), Constants.ICO_PIC_WIDTH, Constants.ICO_PIC_HEIGHT);
                note.setImage(bitmap);
                note.setContent(cursor.getString(cursor.getColumnIndex(Constants.KEY_CONTENT)));
                note.setTime(cursor.getString(cursor.getColumnIndex(Constants.KEY_TIME)));
                list.add(note);
            }
        } catch (Exception e) {
        } finally {
            cursor.close();
        }
        return list;
    }

    /**
     * 数据库中获得指定ID的笔记内容
     *
     * @param id
     * @return 笔记对象
     */
    public ItemBean getData(int id) {
        Cursor cursor = dbReadable.rawQuery("SELECT * FROM "
                + Constants.TABLE_NAME + " WHERE "
                + Constants.KEY_ID + " = ?", new String[]{id + ""});
        if (!cursor.moveToFirst())
            return null;
        ItemBean note = new ItemBean();
        note.setId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_ID)));
        Bitmap bitmap = BitMapUtil.getBitmap(cursor.getString(cursor.getColumnIndex(Constants.KEY_IMAGE)), Constants.ICO_PIC_WIDTH, Constants.ICO_PIC_HEIGHT);
        note.setImage(bitmap);
        note.setContent(cursor.getString(cursor.getColumnIndex(Constants.KEY_CONTENT)));
        note.setTime(cursor.getString(cursor.getColumnIndex(Constants.KEY_TIME)));
        cursor.close();
        return note;
    }

    /**
     * 修改内容也修改图标的更新 Update
     *
     * @param id
     * @param path
     * @param content
     * @param time
     */
    public void updateData(int id, String path, String content, String time) {
        ContentValues cv = new ContentValues();
        cv.put(Constants.KEY_IMAGE, path);
        cv.put(Constants.KEY_CONTENT, content);
        cv.put(Constants.KEY_TIME, time);
        dbWritable.update(Constants.TABLE_NAME, cv, Constants.KEY_ID + "=?", new String[]{id + ""});
    }

    /**
     * 不修改图标的Update
     *
     * @param id
     * @param content
     * @param time
     */
    public void updateData(int id, String content, String time) {
        ContentValues cv = new ContentValues();
        cv.put(Constants.KEY_CONTENT, content);
        cv.put(Constants.KEY_TIME, time);
        dbWritable.update(Constants.TABLE_NAME, cv, Constants.KEY_ID + "=?", new String[]{id + ""});
    }

    public void delNoteById(int id) {
        dbWritable.execSQL("DELETE FROM " + Constants.TABLE_NAME + " WHERE "
                + Constants.TABLE_ID + " = ?", new String[]{id + ""});
    }
}