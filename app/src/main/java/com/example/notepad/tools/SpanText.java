package com.example.notepad.tools;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ImageSpan;
import android.util.AttributeSet;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 自定义控件，用于图文并排
 */
public class SpanText extends android.support.v7.widget.AppCompatEditText {

    public SpanText(Context context) {
        super(context);
    }

    public SpanText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * 让图片路径增加到日记字符串里，并且重新定位光标
     *
     * @param imgPath
     */
    public void addImgToText(String imgPath) {
        if (getText().length() == 0) {
            this.setText(" ");
        }
        SpannableString ss = new SpannableString(imgPath);

        this.append(ss);
        //插入图片空两行
        this.append("\n\n");
        setPicText(this.getText().toString());
        this.setSelection(this.getText().toString().length());  //光标位置
    }

    /**
     * 解析字符串文本，普通文字正常显示，路径显示为图片
     *
     * @param content
     */
    public void setPicText(String content) {
        //s1是路径字符串或者正常的文字
        //SpannableString可以通过使用其方法setSpan方法实现字符串各种形式风格的显示
        SpannableString s1 = new SpannableString(content);
        Pattern pattern = Pattern.compile(Constants.PicPatten);
        Matcher mc = pattern.matcher(content);//正则查找图片路径
        while (mc.find()) {
            Bitmap bitmap;
            BitMapUtil.Size size = BitMapUtil.getBitMapSize(mc.group());
            //图文混排里图片的大小边界，大于这个值则为大图，需要缩小，否则需要扩大
            if (size.getWidth() < Constants.SPANTEXT_BIG_PIC_LEAST_WIDTH && size.getHeight() < Constants.SPANTEXT_BIG_PIC_LEAST_HEIGHT) {
                bitmap = BitMapUtil.getSuitableBitmap(mc.group());//小图扩大
            } else { //得到了图片，并且大图缩小二倍
                bitmap = BitMapUtil.getBitmap(mc.group(), size.getWidth() / 2, size.getHeight() / 2);
            }
            //设置图片的样式的
            ImageSpan imgSpan = new ImageSpan(bitmap, ImageSpan.ALIGN_BASELINE);
            //start和end标记要替代的文字内容的范围
            s1.setSpan(imgSpan, mc.start(), mc.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        //插入了图片或文字
        this.setText(s1);
    }
}



