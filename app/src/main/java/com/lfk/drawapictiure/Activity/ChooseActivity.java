package com.lfk.drawapictiure.Activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.lfk.drawapictiure.Info.UserInfo;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.Tools.SPUtils;

public class ChooseActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose);
        findViewById(R.id.choose_desktop).setOnClickListener(this);
        findViewById(R.id.choose_login).setOnClickListener(this);
        if (SPUtils.contains(this, "install") && (boolean) SPUtils.get(this, "install", true)) {
            Intent intent_login = new Intent(ChooseActivity.this, LoginActivity.class);
            startActivity(intent_login);
            finish();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_choose, menu);
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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.choose_login:
                Intent intent_login = new Intent(ChooseActivity.this, LoginActivity.class);
                startActivity(intent_login);
                finish();
                break;
            case R.id.choose_desktop:
                Intent intent_desktop = new Intent(ChooseActivity.this, MenuActivity.class);
                SPUtils.clear(this);
                SPUtils.put(this, "username", UserInfo.PUBLIC_ID);
                intent_desktop.putExtra("username", UserInfo.PUBLIC_ID);
                startActivity(intent_desktop);
//                finish();
                break;
        }
    }
}
