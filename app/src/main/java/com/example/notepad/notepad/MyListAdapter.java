package com.example.notepad.notepad;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.notepad.R;
import com.example.notepad.tools.Constants;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MyListAdapter extends BaseAdapter {
    private List<ItemBean> mList;
    private ViewHolder holder;
    private LayoutInflater mInflater;
    public boolean[] isSelected;
    public boolean showCheckbox = false;

    public MyListAdapter(Context context, List<ItemBean> list) {
        mList = list;
        mInflater = LayoutInflater.from(context);
        isSelected = new boolean[list.size()];
        for (int i = 0; i < list.size(); i++) {
            isSelected[i] = false;
        }
        showCheckbox = false;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_layout, null);
            holder.imageIco = (ImageView) convertView.findViewById(R.id.id_lv_img);
            holder.content = (TextView) convertView.findViewById(R.id.id_lv_content);
            holder.time = (TextView) convertView.findViewById(R.id.id_lv_time);
            holder.noteId = (TextView) convertView.findViewById(R.id.id_lv_id);
            holder.cb = (CheckBox) convertView.findViewById(R.id.id_lv_cb);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ItemBean itemBean = mList.get(position);
        holder.noteId.setText(itemBean.id + "");
        holder.imageIco.setImageBitmap(itemBean.image);
        holder.content.setText(delPathForContent(itemBean.content));
        holder.time.setText(itemBean.time);
        //反选状态
        holder.cb.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isSelected[position] = isChecked;
            }
        });
        holder.cb.setChecked(isSelected[position]);
        // 设置checkbox显示与否
        if (showCheckbox) {
            holder.cb.setVisibility(View.VISIBLE);
        } else {
            holder.cb.setVisibility(View.INVISIBLE);
        }
        return convertView;
    }

    class ViewHolder {
        ImageView imageIco;
        TextView noteId, content, time;
        CheckBox cb;
    }

    /**
     * 显示在列表里的文本，对文本进行处理：去除文本里的路径，长度过长则裁部分，再加上省略号
     *
     * @param content
     * @return 无图片标记且缩减处理的文本String
     */
    private String delPathForContent(String content) {
        String patternStr = Constants.PicPatten;
        Pattern pattern = Pattern.compile(patternStr);
        Matcher m = pattern.matcher(content);
        content = m.replaceAll("");

        patternStr = "\\n";
        pattern = Pattern.compile(patternStr);
        m = pattern.matcher(content);
        content = m.replaceAll("");

        if (content.length() > 12) {
            content = content.substring(0, 12) + "...";
        }
        return content;
    }
}
