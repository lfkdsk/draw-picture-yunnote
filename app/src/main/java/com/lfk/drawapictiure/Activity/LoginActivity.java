package com.lfk.drawapictiure.Activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import com.lfk.drawapictiure.Datebase.SQLHelper;
import com.lfk.drawapictiure.Info.UserInfo;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.Tools.HttpUtils;
import com.lfk.drawapictiure.Tools.MessageGZIP;
import com.lfk.drawapictiure.Tools.SPUtils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText loginName;
    private EditText loginPsw;
    private Snackbar snackbar;
    private android.support.v7.widget.Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        makeFilesAndDatabase();
        login();
        loginName = (EditText) findViewById(R.id.name);
        loginPsw = (EditText) findViewById(R.id.psw);
        findViewById(R.id.regist).setOnClickListener(this);
        findViewById(R.id.login).setOnClickListener(this);
        findViewById(R.id.login_back_button).setOnClickListener(this);

        toolbar = (Toolbar)findViewById(R.id.login_toolbar);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.darkblue));

    }

    private void makeFiles() {
        File dirJson = new File(UserInfo.PATH + "/json");
        File dirPicture = new File(UserInfo.PATH + "/picture");
        File dirTxt = new File(UserInfo.PATH + "/txt");
        File dirFile = new File(UserInfo.PATH + "/temp.lfk");
        if (!dirJson.exists() && dirPicture.exists()) {
            dirJson.mkdirs();
            dirPicture.mkdirs();
            dirTxt.mkdirs();
            dirFile.mkdirs();
        }
    }

    private void makeDatabase() {
        SQLHelper sqlHelper = new SQLHelper(this);
    }

    private void makeFilesAndDatabase() {
        makeFiles();
        makeDatabase();
    }

    private void login() {
        if (SPUtils.contains(this, "username") && SPUtils.contains(this, "token")) {
            Intent intentJump = new Intent(LoginActivity.this, MenuActivity.class);
            intentJump.putExtra("username", (String) SPUtils.get(LoginActivity.this, "username", "username"));
            UserInfo.TOKEN = (String) SPUtils.get(LoginActivity.this, "token", "token");
            UserInfo.UserName = (String) SPUtils.get(LoginActivity.this, "username", "username");
            if (SPUtils.contains(LoginActivity.this, "only_wifi")) {
                UserInfo.ONLY_WIFI = (boolean) SPUtils.get(LoginActivity.this, "only_wifi", false);
            }
            if (SPUtils.contains(LoginActivity.this, "open_code")) {
                UserInfo.OPEN_CODE = (boolean) SPUtils.get(LoginActivity.this, "open_code", false);
            }
            startActivity(intentJump);
            finish();
        }
    }


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UserInfo.SUCCESS_LOGIN:
                    Intent intent = new Intent(LoginActivity.this, MenuActivity.class);
                    intent.putExtra("username", loginName.getText().toString());
                    SPUtils.put(LoginActivity.this, "username", loginName.getText().toString());
                    SPUtils.put(LoginActivity.this, "password", loginPsw.getText().toString());
                    SPUtils.put(LoginActivity.this, "token", msg.obj.toString());
                    UserInfo.UserName = (String) SPUtils.get(LoginActivity.this, "username", "username");
                    UserInfo.TOKEN = (String) SPUtils.get(LoginActivity.this, "token", "token");
//                    Logger.e(UserInfo.TOKEN);
//                    makeFilesAndDatabase();
                    List<Integer> arrayList = new ArrayList<>();
                    SPUtils.put(LoginActivity.this, "delete_list", MessageGZIP.ListToJson(arrayList));
//                    Log.e("name:", (String) SPUtils.get(LoginActivity.this, "username", "username"));
//                    Log.e("token:", (String) SPUtils.get(LoginActivity.this, "token", "token"));
                    SPUtils.put(LoginActivity.this, "install", true);
                    startActivity(intent);
                    finish();
                    break;
                case UserInfo.CONTENT_ERROR:
                    snackMake(toolbar, "登陆失败");
//                    Toast.makeText(LoginActivity.this, "登陆失败", Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    private void snackMake(View view, String text) {
        snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.WHITE);
        Snackbar.SnackbarLayout ve = (Snackbar.SnackbarLayout) snackbar.getView();
        ve.setBackgroundColor(getResources().getColor(R.color.blue));
        snackbar.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.regist:
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
                break;
            case R.id.login:
                if (loginPsw.getText().toString().equals("") && loginName.getText().toString().equals("")) {
//                    Toast.makeText(LoginActivity.this, "请输入用户名／密码", Toast.LENGTH_SHORT).show();
                    snackMake(toolbar,"请输入用户名／密码");
                } else {
                    List<NameValuePair> list = new ArrayList<>();
                    list.add(new BasicNameValuePair("username", loginName.getText().toString()));
                    list.add(new BasicNameValuePair("password", loginPsw.getText().toString()));
                    HttpUtils.PostToHttp(this, UserInfo.url + "/user/login", list, handler, UserInfo.SUCCESS_LOGIN);
                }
                break;
            case R.id.login_back_button:
                finish();
                break;
        }
    }


}
