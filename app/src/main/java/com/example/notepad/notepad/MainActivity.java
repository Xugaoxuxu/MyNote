package com.example.notepad.notepad;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.notepad.R;
import com.example.notepad.tools.Constants;
import com.example.notepad.tools.ToastUtils;

import java.util.List;

/**
 * 主界面，显示已经写好的列表
 */
public class MainActivity extends BaseActivity {
    private static final int ADD_NOTE = 1;
    private ListView listview;
    private List<ItemBean> dataList;
    public static MyListAdapter mAdapter;
    private DbManager dbManager;
    private ImageButton addBtn;
    private long oldTime = 0;
    public boolean deleteMode;  //是否是长按item => 删除note
    private boolean isALLselect;

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        dbManager = DbManager.getInstance(this);
        initView();
        initEvent();
        initDataFromDb();
        deleteMode = false;
        isALLselect = false;

        //申请权限
        performCodeWithPermission("将获取你的相机以及内存权限", new PermissionCallback() {
            @Override
            public void hasPermission() {
            }

            @Override
            public void noPermission() {
            }
        }, Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.MOUNT_UNMOUNT_FILESYSTEMS);

        // android 7.0系统解决拍照的问题
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
    }

    public static void refreshUI() {
        mAdapter.notifyDataSetChanged();
    }

    private void initView() {
        listview = (ListView) findViewById(R.id.lv_listView);
        addBtn = (ImageButton) findViewById(R.id.id_addNote);
    }

    /**
     * 从数据库获取所有笔记，传给dataList
     */
    private void initDataFromDb() {
        //        dataList = new ArrayList<ItemBean>();
        dataList = dbManager.getDataList();
        mAdapter = new MyListAdapter(MainActivity.this, dataList);
        listview.setAdapter(mAdapter);
        refreshUI();
    }

    private void initEvent() {
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                startActivityForResult(intent, ADD_NOTE);
                MainActivity.this.finish();
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (!deleteMode) {
                    MyListAdapter.ViewHolder viewHolder
                            = (MyListAdapter.ViewHolder) view.getTag();
                    String noteId = viewHolder.noteId.getText().toString().trim();
                    Intent intent = new Intent(MainActivity.this, NoteActivity.class);
                    intent.putExtra(Constants.INTENT_NOTEID, Integer.parseInt(noteId));
                    startActivityForResult(intent, ADD_NOTE);
                    MainActivity.this.finish();
                    // 传递id
                } else {
                    MyListAdapter.ViewHolder holder = (MyListAdapter.ViewHolder) view.getTag();
                    holder.cb.toggle();
                }
            }
        });
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                deleteMode = true;
                mAdapter.showCheckbox = true;
                beginDelMode();
                mAdapter.isSelected[position] = true;
                refreshUI();
                return true;
            }
        });
    }

    /**
     * 常规情况下的视图
     */
    private void beginNormalMode() {
        RelativeLayout layoutDelTop = (RelativeLayout) findViewById(R.id.id_del_mode_top);
        layoutDelTop.setVisibility(View.GONE);
        RelativeLayout layoutNormalTop = (RelativeLayout) findViewById(R.id.id_normal_top);
        layoutNormalTop.setVisibility(View.VISIBLE);
        RelativeLayout layoutDelBottom = (RelativeLayout) findViewById(R.id.id_del_mode_bottom);
        layoutDelBottom.setVisibility(View.GONE);
        ImageButton imgBtnDel = (ImageButton) findViewById(R.id.id_btn_del_mode_bottom);
        imgBtnDel.setVisibility(View.GONE);
        selectAllToState(false);
        mAdapter.showCheckbox = false;
        deleteMode = false;
    }

    /**
     * 长按item后的视图，显示全选/取消/删除按钮
     */
    private void beginDelMode() {
        deleteMode = true;
        mAdapter.showCheckbox = true;
        RelativeLayout layoutDelTop = (RelativeLayout) findViewById(R.id.id_del_mode_top);
        layoutDelTop.setVisibility(View.VISIBLE);
        RelativeLayout layoutNormalTop = (RelativeLayout) findViewById(R.id.id_normal_top);
        layoutNormalTop.setVisibility(View.GONE);
        RelativeLayout layoutDelBottom = (RelativeLayout) findViewById(R.id.id_del_mode_bottom);
        layoutDelBottom.setVisibility(View.VISIBLE);
        TextView tvCancel = (TextView) findViewById(R.id.top_tv_cancel);
        tvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                beginNormalMode();
                mAdapter.showCheckbox = false;
                selectAllToState(false);
            }
        });
        TextView tvSelectAll = (TextView) findViewById(R.id.top_tv_selectAll);
        tvSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isALLselect) {
                    TextView tv = (TextView) v;
                    tv.setText("全选");
                } else {
                    TextView tv = (TextView) v;
                    tv.setText("全不选");
                }
                selectAllToState(!isALLselect);
                isALLselect = !isALLselect;
                refreshUI();
            }
        });
        ImageButton delBtn = (ImageButton) findViewById(R.id.id_btn_del_mode_bottom);
        delBtn.setVisibility(View.VISIBLE);
        delBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                delSelectedItem();
                beginNormalMode();
                initDataFromDb();
            }
        });
    }

    /**
     * 将全部item变成一个状态（选中/不选中）
     *
     * @param state
     */
    public void selectAllToState(boolean state) {
        for (int i = 0; i < dataList.size(); i++) {
            mAdapter.isSelected[i] = state;
        }
    }

    public void delSelectedItem() {
        for (int i = 0; i < dataList.size(); i++) {
            if (mAdapter.isSelected[i] == true) {
                dbManager.delNoteById(dataList.get(i).getId());
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (deleteMode) {
            beginNormalMode();
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            long currentTime = System.currentTimeMillis();
            if (currentTime - oldTime <= Constants.EXIT_TIME) {
                this.finish();
            } else {
                ToastUtils.showShort(this, "再按一次退出");
                oldTime = currentTime;
            }
        }
        return true;
    }
}