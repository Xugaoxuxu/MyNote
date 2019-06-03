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


/**
 * 自己的ListAdapter，主要是优化性能的
 */
public class MyListAdapter extends BaseAdapter {
    private List<ItemBean> mList;
    private ViewHolder holder;
    private LayoutInflater mInflater;
    public boolean[] isSelected;
    public boolean showCheckbox = false;

    public MyListAdapter(Context context, List<ItemBean> list) {
        mList = list;
        //LayoutInflater这个类还是非常有用的，它的作用类似于findViewById()
        // 不同点是LayoutInflater是用来找res/layout/下的xml布局文件，并且实例化
        // 而findViewById()是找xml布局文件下的具体widget控件(如Button、TextView等)
        mInflater = LayoutInflater.from(context);
        //为多选框设置ID
        isSelected = new boolean[list.size()];
        //默认是不选中
        for (int i = 0; i < list.size(); i++) {
            isSelected[i] = false;
        }
        //不显示
        showCheckbox = false;
    }

    //get set 用来方法调用私有的成员变量
    @Override
    public int getCount() {
        //list的个数
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        //get(position)是按照list中元素的索引进行取值
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        //item所在的位置，第几个这样的
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.item_layout, null);
            //初始化
            holder.imageIco = (ImageView) convertView.findViewById(R.id.id_lv_img);
            holder.content = (TextView) convertView.findViewById(R.id.id_lv_content);
            holder.time = (TextView) convertView.findViewById(R.id.id_lv_time);
            //左上角隐藏的那个就是id初始为-1
            holder.noteId = (TextView) convertView.findViewById(R.id.id_lv_id);
            holder.cb = (CheckBox) convertView.findViewById(R.id.id_lv_cb);

            //给View对象的一个标签，这个标签包含的是控件信息
            convertView.setTag(holder);
        } else {
            //如果已经初始化过了就获得这个tag
            holder = (ViewHolder) convertView.getTag();
        }
        //给itemBean赋上可显示的值
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

    /**
     * 在ViewHolder类中声明控件名称，然后在getView方法中使用初始化一次
     * View对象初始化一次，将控件引用赋值给ViewHolder变量
     * 这样就使用缓存从而大大进行优化速度
     */
    class ViewHolder {
        ImageView imageIco;
        TextView noteId, content, time;
        CheckBox cb;
    }

    /**
     * 显示在列表里的文本，对文本进行处理：
     *      去除文本里的路径，长度过长则裁部分，再加上省略号
     *
     * @param content
     * @return 无图片标记且缩减处理的文本String
     */
    private String delPathForContent(String content) {
        //从文本里匹配图片的正则
        String patternStr = Constants.PicPatten;
        //把规则编译成模式对象
        Pattern pattern = Pattern.compile(patternStr);
        // 通过模式对象得到匹配器对象
        Matcher m = pattern.matcher(content);
        //去除了路径的标记
        content = m.replaceAll("");

        patternStr = "\\n";
        //去除了回车换行的标记
        pattern = Pattern.compile(patternStr);
        m = pattern.matcher(content);
        content = m.replaceAll("");

        //当文本长于12时替换为...
        if (content.length() > 12) {
            content = content.substring(0, 12) + "...";
        }
        return content;
    }
}
