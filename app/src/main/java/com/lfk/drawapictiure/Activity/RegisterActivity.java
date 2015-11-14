package com.lfk.drawapictiure.Activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.lfk.drawapictiure.Info.UserInfo;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.Tools.HttpUtils;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.util.ArrayList;
import java.util.List;

public class RegisterActivity extends AppCompatActivity implements View.OnClickListener{
    private EditText regName;
    private EditText regPsw;
    private EditText regPswRe;
    private EditText regEmail;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_regist_avtivity);
        regEmail = (EditText)findViewById(R.id.regemail);
        regName = (EditText)findViewById(R.id.regname);
        regPsw = (EditText)findViewById(R.id.regpsw);
        regPswRe = (EditText)findViewById(R.id.reregpsw);
        findViewById(R.id.regbutton).setOnClickListener(this);
        findViewById(R.id.register_back_button).setOnClickListener(this);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.darkblue));

    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case UserInfo.SUCCESS_REGISTER:
                    Toast.makeText(RegisterActivity.this,"注册成功",Toast.LENGTH_SHORT).show();
                    finish();
                    break;
                case UserInfo.CONTENT_ERROR:
                    Toast.makeText(RegisterActivity.this,"注册失败",Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_regist_avtivity, menu);
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

    private String getTextString(EditText editText){
        return editText.getText().toString();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.regbutton:
                if(getTextString(regEmail).equals("")||getTextString(regName).equals("")
                        ||getTextString(regPsw).equals("")||getTextString(regPswRe).equals("")){
                    Toast.makeText(RegisterActivity.this,"请确认输入",Toast.LENGTH_SHORT).show();
                }
                else {
                    if(!(getTextString(regPsw)).equals(getTextString(regPswRe))){
                        Toast.makeText(RegisterActivity.this,"确认密码错误",Toast.LENGTH_SHORT).show();
                    }else {
                        List<NameValuePair> list = new ArrayList<>();
                        list.add(new BasicNameValuePair("username", regName.getText().toString()));
                        list.add(new BasicNameValuePair("password", regPsw.getText().toString()));
                        list.add(new BasicNameValuePair("email", regEmail.getText().toString()));
                        HttpUtils.PostToHttp(this,UserInfo.url + "/user/register", list, handler, UserInfo.SUCCESS_REGISTER);
                    }
                }
                break;
            case R.id.register_back_button:
                finish();
                break;
        }
    }
}
