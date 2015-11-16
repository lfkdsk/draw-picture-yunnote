package com.lfk.drawapictiure.Activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.lfk.drawapictiure.Datebase.SQLHelper;
import com.lfk.drawapictiure.Fragment.CodeFragment;
import com.lfk.drawapictiure.Fragment.NoteFragment;
import com.lfk.drawapictiure.Fragment.PaintFragment;
import com.lfk.drawapictiure.Info.UserInfo;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.Tools.HttpUtils;
import com.lfk.drawapictiure.Tools.NetUtils;
import com.lfk.drawapictiure.Tools.SPUtils;
//import com.orhanobut.logger.Logger;
import com.lfk.drawapictiure.bluetoothc.BluetoothChat;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.rengwuxian.materialedittext.MaterialEditText;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import me.drakeet.materialdialog.MaterialDialog;


public class MenuActivity extends AppCompatActivity implements View.OnClickListener {
    private TabLayout mTabLayout;
    private ViewPager MainViewPager;
    private DrawerLayout mDrawerLayout;
    private int Position = 0;
    private static SQLiteDatabase database;
    private NavigationView navigationView;
    private String EditName;
    public static String token;
    private MaterialDialog materialDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drawer);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.darkblue));

        Toolbar toolbar = (Toolbar) findViewById(R.id.menu_toolbar);
        toolbar.setBackground(getResources().getDrawable(R.color.blue));
        toolbar.setTitle("云手帐");
        toolbar.setTitleTextColor(Color.WHITE);
        setSupportActionBar(toolbar);

        makeFiles();
        makeDatabase();
//        FindInInternetWithUrl();
        token = (String) SPUtils.get(this, "token", "token");

        final ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.main_drawer);
        navigationView = (NavigationView) findViewById(R.id.main_navigation);
        if (navigationView != null) {
            setupDrawerContent(navigationView);
        }

        setupViewPager();
        FloatingActionButton fabButton = (FloatingActionButton) findViewById(R.id.fab_button);
        fabButton.setOnClickListener(this);

        UserInfo.UserName = getIntent().getStringExtra("username");

        TextView textView = (TextView) navigationView.findViewById(R.id.user_name);
        textView.setText(UserInfo.UserName);

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("REFRESH");
        registerReceiver(receiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        makeFiles();
        makeDatabase();
//        setupViewPager();
    }

    private void setupDrawerContent(NavigationView navigationView) {
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    menuItem.setChecked(true);
                    mDrawerLayout.closeDrawers();
                    switch (menuItem.getItemId()) {
                        case R.id.open_code:
                            materialDialog = new MaterialDialog(MenuActivity.this)
                                    .setMessage("开启代码云同步？")
                                    .setNegativeButton("关闭", view -> {
                                        SPUtils.put(MenuActivity.this, "open_code", false);
                                        UserInfo.OPEN_CODE = false;
                                        setupViewPager();
                                        materialDialog.dismiss();
                                    })
                                    .setPositiveButton("开启", view -> {
                                        SPUtils.put(MenuActivity.this, "open_code", true);
                                        UserInfo.OPEN_CODE = true;
                                        setupViewPager();
                                        materialDialog.dismiss();
                                    });
                            materialDialog.show();
                            break;
                        case R.id.exchange_user:
                            materialDialog = new MaterialDialog(MenuActivity.this)
                                    .setMessage("确认切换用户？")
                                    .setNegativeButton("取消", view -> {
                                        materialDialog.dismiss();
                                    })
                                    .setPositiveButton("确认", view -> {
                                        Intent intent = new Intent(MenuActivity.this, ChooseActivity.class);
                                        SPUtils.clear(MenuActivity.this);
                                        startActivity(intent);
                                        finish();
                                    });
                            materialDialog.show();
                            break;
                        case R.id.menu_clear:
                            materialDialog = new MaterialDialog(MenuActivity.this)
                                    .setMessage("清理缓存？")
                                    .setNegativeButton("取消", view -> {
                                        materialDialog.dismiss();
                                    })
                                    .setPositiveButton("确认", view -> {
                                        showToast("清理成功");
                                        materialDialog.dismiss();
                                    });
                            materialDialog.show();
                            break;
                        case R.id.only_wifi:
                            materialDialog = new MaterialDialog(MenuActivity.this)
                                    .setMessage("仅WI-FI下使用？")
                                    .setNegativeButton("关闭", view -> {
                                        SPUtils.put(MenuActivity.this, "only_wifi", false);
                                        UserInfo.ONLY_WIFI = false;
                                        materialDialog.dismiss();
                                    })
                                    .setPositiveButton("开启", view -> {
                                        SPUtils.put(MenuActivity.this, "only_wifi", true);
                                        UserInfo.ONLY_WIFI = true;
                                        materialDialog.dismiss();
                                    });
                            materialDialog.show();
                            break;
                        case R.id.menu_fankui:
                            View edit = View.inflate(this, R.layout.menu_fankui, null);
                            MaterialEditText editText = (MaterialEditText) edit.findViewById(R.id.menu_fankui_edit);
                            materialDialog = new MaterialDialog(MenuActivity.this)
                                    .setTitle("发送反馈信息")
                                    .setContentView(edit)
                                    .setNegativeButton("取消", view -> {
                                        materialDialog.dismiss();
                                    })
                                    .setPositiveButton("发送", view -> {
                                        List<NameValuePair> list = new ArrayList<>();
                                        list.add(new BasicNameValuePair("content", editText.getText().toString()));
                                        HttpUtils.PostToHttp(this, UserInfo.url + "/feedback?token=" + token, list, handler, UserInfo.FANKUI_SUCCESS);
                                        materialDialog.dismiss();
                                    });
                            materialDialog.show();
                            break;
                        case R.id.menu_finish:
                            finish();
                            break;
                        case R.id.about_us:
                            materialDialog = new MaterialDialog(MenuActivity.this)
                                    .setTitle("关于我们")
                                    .setMessage("成员：刘丰恺、陈力经、苏梦曦、李润之")
                                    .setPositiveButton("确认", view -> {
                                        materialDialog.dismiss();
                                    });
                            materialDialog.show();
                            break;
                        case R.id.open_bluetooth:
                            Intent intent = new Intent(this, BluetoothChat.class);
                            startActivity(intent);
                    }
                    return true;
                });
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.fab_button:
                Intent intent = new Intent();
                switch (Position) {
                    case 0:
                        intent.setClass(this, MainActivity.class);
                        intent.putExtra("paint_name", "新建");
                        this.startActivityForResult(intent, UserInfo.SAVE_PAINT);
                        break;
                    case 1:
                        intent.setClass(this, NoteActivity.class);
                        intent.putExtra("build", "新建");
                        this.startActivityForResult(intent, UserInfo.SAVE_NOTE);
                        break;
                    case 2:
                        Intent code_intent = new Intent(Intent.ACTION_GET_CONTENT);
                        code_intent.addCategory(Intent.CATEGORY_OPENABLE);
                        code_intent.setType("file/*");
                        this.startActivityForResult(Intent.createChooser(code_intent, "选择代码文件"), UserInfo.SELECT_FILE);
                        break;
                }
                break;
        }
    }

    private void makeFiles() {
        File dir = new File(UserInfo.PATH);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    private void makeDatabase() {
        SQLHelper sqlHelper = new SQLHelper(this);
        database = sqlHelper.getWritableDatabase();
    }

    private void setupViewPager() {
        MainViewPager = (ViewPager) findViewById(R.id.main_viewpager);
        mTabLayout = (TabLayout) findViewById(R.id.tabs);
        mTabLayout.setBackgroundColor(getResources().getColor(R.color.blue));
        mTabLayout.setTabMode(TabLayout.MODE_FIXED);
        List<String> titles = new ArrayList<>();
        titles.add("手绘");
        titles.add("文字");
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(0)));
        mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(1)));
        if (UserInfo.OPEN_CODE) {
            titles.add("代码");
            mTabLayout.addTab(mTabLayout.newTab().setText(titles.get(2)));
        }
        MainAdapter mainAdapter = new MainAdapter(getSupportFragmentManager(), titles);
        MainViewPager.setAdapter(mainAdapter);
        mTabLayout.setupWithViewPager(MainViewPager);
        mTabLayout.setTabsFromPagerAdapter(mainAdapter);
        MainViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Position = position;
////                Log.e("" + Position, "position");
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
    }

    private void showToast(String toast) {
        Toast.makeText(MenuActivity.this, toast, Toast.LENGTH_SHORT);
    }


    private class MainAdapter extends FragmentPagerAdapter {
        private List<String> titles;

        public MainAdapter(FragmentManager fm, List<String> titles) {
            super(fm);
            this.titles = titles;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return titles.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            if (UserInfo.OPEN_CODE && position == 2) {
                fragment = CodeFragment.newInstance(database);
            }
            switch (position) {
                case 0:
                    fragment = PaintFragment.newInstance(database);
                    break;
                case 1:
                    fragment = NoteFragment.newInstance(database);
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            return titles.size();
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (mDrawerLayout != null)
                    mDrawerLayout.openDrawer(GravityCompat.START);
                break;

        }
        return super.onOptionsItemSelected(item);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UserInfo.UPDATE_SUCCESS:
                    try {
                        JSONObject jsonObject = new JSONObject(msg.obj.toString());
                        String id = jsonObject.getString("message");
                        int number_id = Integer.parseInt(id.substring(11, id.length() - 1));
////                        Log.e("file_id", number_id + "");
                        database.rawQuery("update note set _id=" + number_id +
                                " where _name=" + "\"" + EditName + "\"" + " and _username=" + "\""
                                + SPUtils.get(MenuActivity.this, "username", UserInfo.PUBLIC_ID) + "\"", null);
////                        Log.e(EditName+"editname","");
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(MenuActivity.this, "上传成功", Toast.LENGTH_SHORT).show();
                    break;
                case UserInfo.CONTENT_ERROR:
                    Toast.makeText(MenuActivity.this, "上传失败", Toast.LENGTH_SHORT).show();
                    break;
                case UserInfo.EDIT_SUCCESS:
                    try {
                        JSONObject jsonObject = new JSONObject(msg.obj.toString());
                        String id = jsonObject.getString("message");
                        if (id.equals("modify file ok")) {
                            Toast.makeText(MenuActivity.this, "更新成功", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case UserInfo.FANKUI_SUCCESS:
                    Toast.makeText(MenuActivity.this, "反馈成功", Toast.LENGTH_SHORT).show();
                    break;
                case UserInfo.CREATE_NOTE:
                    try {
                        JSONObject jsonObject = new JSONObject(msg.obj.toString());
                        String id = jsonObject.getString("message");
                        int number_id = Integer.parseInt(id.substring(11, id.length() - 1));
//                        Log.e("file_id", number_id + "");
                        database.rawQuery("update note set _id=" + number_id +
                                " where _name=" + "\"" + EditName + "\"" + " and _username=" + "\""
                                + SPUtils.get(MenuActivity.this, "username", UserInfo.PUBLIC_ID) + "\"", null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    break;
                case UserInfo.ONLY_WIFI_NOT_CONNET:
                    Toast.makeText(MenuActivity.this, "仅在Wi-Fi下更新", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
//        Logger.e("request on");
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case UserInfo.SAVE_PAINT:
                case UserInfo.SAVE_NOTE:
                case UserInfo.SAVE_CODE:
//                    Logger.d("save it");
                    UpdateAndCreate(data);
                    break;
                case UserInfo.SELECT_FILE:
                    Intent intent = new Intent(MenuActivity.this, CodeActivity.class);
                    intent.setData(data.getData());
                    startActivityForResult(intent, UserInfo.SAVE_CODE);
                    break;
            }
        }
    }

    private void UpdateUnSave() {
        if ((UserInfo.ONLY_WIFI && NetUtils.isWifi(this)) || (!UserInfo.ONLY_WIFI && NetUtils.isConnected(this))) {
            if (database != null) {
                Cursor cursor = database.rawQuery("Select * From note"
                        + " where _id is " + null + " and _username=" + "\""
                        + SPUtils.get(MenuActivity.this, "username", UserInfo.PUBLIC_ID) + "\"", null);
                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        do {
                            Intent intent = new Intent();
                            String name = cursor.getString(cursor.getColumnIndex("_name"));
                            int type = cursor.getInt(cursor.getColumnIndex("_type"));
                            try {
                                String cache = cursor.getString(cursor.getColumnIndex("_pic"));
                                intent.putExtra("cache", cache);
                            } catch (CursorIndexOutOfBoundsException e) {

                            }
                            String content = cursor.getString(cursor.getColumnIndex("_content"));
                            intent.putExtra("name", name);
                            intent.putExtra("content", content);
                            intent.putExtra("type", type);
                            UpdateAndCreate(intent);
                        } while (cursor.moveToNext());
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    // 明天修改为通过判断是否有id
    private void UpdateAndCreate(Intent data) {
//        Log.e("true", !SPUtils.get(this, "username", UserInfo.PUBLIC_ID).equals(UserInfo.PUBLIC_ID) + "");
        if (!SPUtils.get(this, "username", UserInfo.PUBLIC_ID).equals(UserInfo.PUBLIC_ID)) {
            EditName = data.getStringExtra("name");
//            Log.e("editname", EditName);
            List<NameValuePair> list = new ArrayList<>();
            list.add(new BasicNameValuePair("content", data.getStringExtra("content")));
            list.add(new BasicNameValuePair("name", EditName));
            list.add(new BasicNameValuePair("cache", data.getStringExtra("cache")));
            try {
                Cursor cursor = database.rawQuery("Select * From note"
                        + " where _name=" + "\"" + EditName + "\"" + " and _username=" + "\""
                        + SPUtils.get(this, "username", UserInfo.PUBLIC_ID) + "\"", null);
                cursor.moveToFirst();
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                cursor.close();
//                Log.e("update id", id + "");
                if (id != 0) {
                    String url = UserInfo.url + UserInfo.editNote + "/" + id + "?token=" + token;
                    HttpUtils.PostToHttp(this, url, list, handler, UserInfo.EDIT_SUCCESS);
                } else {
//                    Log.e("上传无id", "失败" + data.getIntExtra("type", 0));
                    String url = UserInfo.url + UserInfo.sendNote + "/" + data.getIntExtra("type", 0) + "/add?token=" + token;
                    HttpUtils.PostToHttp(this, url, list, handler, UserInfo.UPDATE_SUCCESS);
                }
            } catch (CursorIndexOutOfBoundsException e) {
//                Log.e("更新", "失败" + data.getIntExtra("type", 0));
                String url = UserInfo.url + UserInfo.sendNote + "/" + data.getIntExtra("type", 0) + "/add?token=" + token;
                HttpUtils.PostToHttp(this, url, list, handler, UserInfo.UPDATE_SUCCESS);
            }
        }
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "REFRESH":
                    UpdateUnSave();
                    break;
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.unregisterReceiver(receiver);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            moveTaskToBack(true);
        }
        return true;
    }
}


