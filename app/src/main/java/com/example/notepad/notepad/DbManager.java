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

    //得到这个数据库的实例
    public static DbManager getInstance(Context context) {
        if (instance == null) {
            //synchronized是Java中的关键字，是一种同步锁,防止进程被阻塞
            synchronized (DbManager.class) {
                if (instance == null) {
                    instance = new DbManager();
                    helper = new DbHelper(context, Constants.DB_NAME, null, 1);
                    //得到可读可写的数据库
                    dbReadable = helper.getReadableDatabase();
                    dbWritable = helper.getWritableDatabase();
                }
            }
        }
        return instance;
    }

    //    空的构造方法
    //    private DbManager() {
    //    }

    /**
     * 新增日记
     *
     * @param icoPath 图标路径
     * @param content 正文
     * @param time    时间
     */
    public void addNote(String icoPath, String content, String time) {
        //ContentValues其实很像一个字典对象，可以用来存储键值对
        ContentValues values = new ContentValues();
        values.put(Constants.KEY_IMAGE, icoPath);
        values.put(Constants.KEY_CONTENT, content);
        values.put(Constants.KEY_TIME, time);
        //在数据库中插入这些信息，注意图片是插入的存储路径，这是常用的做法
        dbWritable.insert(Constants.TABLE_NAME, null, values);
        //清空values准备接受新的信息
        values.clear();
    }

    /**
     * 查数据库，并返回list结果
     */
    public List<ItemBean> getDataList() {
        List<ItemBean> list = new ArrayList<ItemBean>();
        //定义了一个光标，这个光标指向的是notepadTable
        Cursor cursor
                = dbReadable.rawQuery("SELECT * FROM " + Constants.TABLE_NAME, null);
        try {
            //当光标可以指向下一个信息时，给组建进行初始化
            while (cursor.moveToNext()) {
                //初始化itembeen并进行存储
                ItemBean note = new ItemBean();
                //通过ID，返回某列名对应的列索引值，如果不存在返回-1
                note.setId(cursor.getInt(cursor.getColumnIndex(Constants.KEY_ID)));
                //调用工具类的方法,显示图片
                Bitmap bitmap = BitMapUtil.getBitmap(cursor.getString(cursor.getColumnIndex(Constants.KEY_IMAGE)), Constants.ICO_PIC_WIDTH, Constants.ICO_PIC_HEIGHT);
                note.setImage(bitmap);
                //设置内容
                note.setContent(cursor.getString(cursor.getColumnIndex(Constants.KEY_CONTENT)));
                //设置时间
                note.setTime(cursor.getString(cursor.getColumnIndex(Constants.KEY_TIME)));
                //将初始好的信息添加到队列中
                list.add(note);
            }
        } catch (Exception e) {
        } finally {
            //关闭
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

    //从这个list中再找到相应id的笔记内容
    public ItemBean getData(int id) {
        Cursor cursor = dbReadable.rawQuery("SELECT * FROM "
                + Constants.TABLE_NAME + " WHERE "
                + Constants.KEY_ID + " = ?", new String[]{id + ""});
        if (!cursor.moveToFirst())
            return null;
        //初始化itembeen并进行显示
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

    //通过ID删除内容
    public void delNoteById(int id) {
        dbWritable.execSQL("DELETE FROM " + Constants.TABLE_NAME + " WHERE "
                + Constants.TABLE_ID + " = ?", new String[]{id + ""});
    }
}