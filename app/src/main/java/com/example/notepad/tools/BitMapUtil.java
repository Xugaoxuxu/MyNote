package com.example.notepad.tools;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Matrix;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

/**
 * 工具类，在原来的基础上加了getSuitableBitmap()
 */

public final class BitMapUtil {
    private static final Size ZERO_SIZE = new Size(0, 0);
    private static final Options OPTIONS_GET_SIZE = new Options();
    private static final Options OPTIONS_DECODE = new Options();
    private static final byte[] LOCKED = new byte[0];
    // 此对象用来保持Bitmap的回收顺序,保证最后使用的图片被回收
    private static final LinkedList CACHE_ENTRIES = new LinkedList();
    // 线程请求创建图片的队列
    private static final Queue TASK_QUEUE = new LinkedList();
    // 保存队列中正在处理的图片的key,有效防止重复添加到请求创建队列
    private static final Set TASK_QUEUE_INDEX = new HashSet();
    // 缓存Bitmap
    private static final Map IMG_CACHE_INDEX = new HashMap();                         // 通过图片路径,图片大小
    private static int CACHE_SIZE = 20; // 缓存图片数量

    static {
        OPTIONS_GET_SIZE.inJustDecodeBounds = true;
        // 初始化创建图片线程,并等待处理
        new Thread() {
            {
                //开启一个守护线程
                // 守护线程的优先级低，存活与否不影响JVM的退出的线程
                setDaemon(true);
            }

            public void run() {
                while (true) {
                    //线程请求创建图片的队列
                    synchronized (TASK_QUEUE) {
                        if (TASK_QUEUE.isEmpty()) {
                            try {
                                TASK_QUEUE.wait();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    //移除并返问队列头部的元素，如果队列为空，则返回nul
                    //缓存数组
                    QueueEntry entry = (QueueEntry) TASK_QUEUE.poll();
                    //path + "_" + width + "_" + height
                    String key = createKey(entry.path, entry.width,
                            entry.height);
                    TASK_QUEUE_INDEX.remove(key);
                    createBitmap(entry.path, entry.width, entry.height);
                }
            }
        }.start();
    }


    public static Bitmap getBitmap(String path, int width, int height) {
        if (path == null) {
            return null;
        }
        Bitmap bitMap = null;
        try {
            if (CACHE_ENTRIES.size() >= CACHE_SIZE) {
                destoryLast();
            }
            bitMap = useBitmap(path, width, height);
            if (bitMap != null && !bitMap.isRecycled()) {
                return bitMap;
            }
            bitMap = createBitmap(path, width, height);
            String key = createKey(path, width, height);
            synchronized (LOCKED) {
                IMG_CACHE_INDEX.put(key, bitMap);
                CACHE_ENTRIES.addFirst(key);
            }
        } catch (OutOfMemoryError err) {
            destoryLast();
            System.out.println(CACHE_SIZE);
            return createBitmap(path, width, height);
        }
        return bitMap;
    }


    public static Size getBitMapSize(String path) {
        File file = new File(path);
        if (file.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                BitmapFactory.decodeStream(in, null, OPTIONS_GET_SIZE);
                return new Size(OPTIONS_GET_SIZE.outWidth,
                        OPTIONS_GET_SIZE.outHeight);
            } catch (FileNotFoundException e) {
                return ZERO_SIZE;
            } finally {
                closeInputStream(in);
            }
        }
        return ZERO_SIZE;
    }

    // private Methods
    // 将图片加入队列头
    private static Bitmap useBitmap(String path, int width, int height) {
        Bitmap bitMap = null;
        String key = createKey(path, width, height);
        synchronized (LOCKED) {
            bitMap = (Bitmap) IMG_CACHE_INDEX.get(key);
            if (null != bitMap) {
                if (CACHE_ENTRIES.remove(key)) {
                    CACHE_ENTRIES.addFirst(key);
                }
            }
        }
        return bitMap;
    }

    // 回收最后一张图片
    private static void destoryLast() {
        synchronized (LOCKED) {
            String key = (String) CACHE_ENTRIES.removeLast();
            if (key.length() > 0) {
                Bitmap bitMap = (Bitmap) IMG_CACHE_INDEX.remove(key);
                if (bitMap != null && !bitMap.isRecycled()) {
                    bitMap.recycle();
                    bitMap = null;
                }
            }
        }
    }

    // 创建键
    private static String createKey(String path, int width, int height) {
        if (null == path || path.length() == 0) {
            return "";
        }
        return path + "_" + width + "_" + height;
    }

    /**
     * 根据路径的图片大小返回缩放后的图片Bitmap
     *
     * @param path
     * @return
     */
    public static Bitmap getSuitableBitmap(String path) {
        Size size = BitMapUtil.getBitMapSize(path);
        Bitmap bitmap = BitmapFactory.decodeFile(path);
        float wScale = 1, hScale = 1;
        if (size.getWidth() < 1000 && size.getHeight() < 1000) {
            wScale = hScale = 3;    //扩3倍
        } else if (size.getWidth() < 2000 || size.getHeight() < 2000) {
            wScale = hScale = 2;    //扩2倍
        }
        Matrix matrix = new Matrix();
        matrix.postScale(wScale, hScale); //长和宽放大缩小的比例
        Bitmap resizeBmp = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return resizeBmp;
    }

    // 通过图片路径,宽度高度创建一个Bitmap对象
    private static Bitmap createBitmap(String path, int width, int height) {
        File file = new File(path);
        if (file.exists()) {
            InputStream in = null;
            try {
                in = new FileInputStream(file);
                Size size = getBitMapSize(path);
                if (size.equals(ZERO_SIZE)) {
                    return null;
                }
                int scale = 1;
                int a = size.getWidth() / width;
                int b = size.getHeight() / height;
                scale = Math.max(a, b);
                if (scale == 0)
                    scale = 1;
                synchronized (OPTIONS_DECODE) {
                    OPTIONS_DECODE.inSampleSize = scale;

                    Bitmap bitMap = BitmapFactory.decodeStream(in, null,
                            OPTIONS_DECODE);
                    return bitMap;
                }
            } catch (FileNotFoundException e) {
                Log.v("BitMapUtil", "createBitmap==" + e.toString());
            } finally {
                closeInputStream(in);
            }
        }
        return null;
    }

    // 关闭输入流
    private static void closeInputStream(InputStream in) {
        if (null != in) {
            try {
                in.close();
            } catch (IOException e) {
                Log.v("BitMapUtil", "closeInputStream==" + e.toString());
            }
        }
    }

    // 图片大小
    static class Size {
        private int width, height;

        Size(int width, int height) {
            this.width = width;
            this.height = height;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }
    }

    // 队列缓存参数对象
    static class QueueEntry {
        public String path;
        public int width;
        public int height;
    }


}