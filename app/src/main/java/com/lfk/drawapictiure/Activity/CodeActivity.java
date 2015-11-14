package com.lfk.drawapictiure.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.lfk.drawapictiure.Datebase.SQLHelper;
import com.lfk.drawapictiure.Info.UserInfo;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.Tools.SPUtils;
import com.lfk.drawapictiure.View.CodeView.CodeView;
import com.readystatesoftware.systembartint.SystemBarTintManager;

import java.io.File;

import me.drakeet.materialdialog.MaterialDialog;

public class CodeActivity extends AppCompatActivity implements View.OnClickListener{
    private CodeView codeView;
    private MaterialDialog materialDialog;
    private SQLiteDatabase database;
    private boolean FIRST_OPEN_FILE = true;
    private String filename;
    private String Mycontent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_code);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.black));

        Toolbar toolbar = (Toolbar)findViewById(R.id.code_toolbar);
        toolbar.setBackgroundColor(getResources().getColor(R.color.code_gray));
        toolbar.setTitle("");
        setSupportActionBar(toolbar);

        codeView = (CodeView)findViewById(R.id.mcodeview);
        findViewById(R.id.code_back).setOnClickListener(this);
        database = SQLiteDatabase.openOrCreateDatabase(SQLHelper.NAME,null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        File dir = null;
        String stringUri = getIntent().getStringExtra("code");
        if(stringUri != null && getSupportActionBar() != null){
            FIRST_OPEN_FILE = false;
            codeView.setStringSource(stringUri);
            ((TextView)findViewById(R.id.code_first)).setText(getIntent().getStringExtra("title"));
        }else {
            Uri fileUri = getIntent().getData();
            if (fileUri != null) {
                dir = new File(fileUri.getPath());
            }

            if (dir != null && getSupportActionBar()  != null) {
                FIRST_OPEN_FILE = true;
                codeView.setDirSource(dir);
                filename = dir.getName();
                ((TextView)findViewById(R.id.code_first)).setText(filename);
            } else
                finish();
        }
        final TextView textView = (TextView)findViewById(R.id.code_edit);
        textView.setOnClickListener(view -> {
            if (!codeView.isEditable()) {
                textView.setText("完成");
                codeView.setContentEditable(true);
            } else {
                textView.setText("编辑");
                codeView.setContentEditable(false);
            }
        });
    }

    private void finishTheActivity(){
//        Log.e("first_open_file", "" + FIRST_OPEN_FILE);
        if(FIRST_OPEN_FILE) {
            final ContentValues values = new ContentValues();
            materialDialog = new MaterialDialog(this)
                    .setMessage("是否保存？")
                    .setPositiveButton("确定", view -> {
                        values.put("_name", filename);
                        values.put("_content", codeView.getContent());
                        values.put("_username", UserInfo.UserName);
                        values.put("_type", 2);
                        database.insert("note", null, values);
                        database.close();
                        materialDialog.dismiss();
                        setToIntent(filename , codeView.getContent(), "");
                        finish();
                    })
                    .setNegativeButton("取消", view -> {
                        materialDialog.dismiss();
                    });
            materialDialog.show();
        }else {
            if(getIntent().getStringExtra("code").length() != codeView.getContent().length()) {
                database.execSQL("update note set _content=" + "\""
                        + codeView.getContent() + "\"" + " where _name ="
                        + "\"" + getIntent().getStringExtra("title") + "\""
                        + " and _username=" + "\""
                        + SPUtils.get(CodeActivity.this, "username", UserInfo.PUBLIC_ID) + "\"");

                database.close();
                setToIntent(getIntent().getStringExtra("title"), codeView.getContent(), "");
            }else {
                finish();
            }
        }
    }

    private void setToIntent(String name ,String content,String cache){
        Intent intent = new Intent(CodeActivity.this,MenuActivity.class);
        intent.putExtra("name",name);
        intent.putExtra("content",content);
        intent.putExtra("cache",cache);
        intent.putExtra("type",2);
        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()){
            case R.id.code_back:
                finishTheActivity();
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode){
            case KeyEvent.KEYCODE_BACK:
                finishTheActivity();
                break;
        }
        return true;
    }
}
