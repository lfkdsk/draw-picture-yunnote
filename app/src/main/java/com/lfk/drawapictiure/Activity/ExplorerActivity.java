package com.lfk.drawapictiure.Activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.lfk.drawapictiure.Info.UserInfo;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.View.FileExplorer.FileExplorer;
import com.lfk.drawapictiure.View.FileExplorer.OnFileChosenListener;
import com.orhanobut.logger.Logger;
import com.readystatesoftware.systembartint.SystemBarTintManager;

public class ExplorerActivity extends AppCompatActivity implements View.OnClickListener {
    private FileExplorer fileExplorer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_explorer);


        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
//        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.darkblue));

        fileExplorer = (FileExplorer) findViewById(R.id.file_explorer);

        fileExplorer.setOnFileChosenListener(new OnFileChosenListener() {
            @Override
            public void onFileChosen(Uri fileUri) {
                Intent intent = new Intent(ExplorerActivity.this, MainActivity.class);
                intent.setData(fileUri);
                Logger.d("fileuri:" + fileUri);
                ExplorerActivity.this.setResult(RESULT_OK, intent);
                finish();
            }
        });

        fileExplorer.setCurrentDir(UserInfo.PATH + "/json");
        fileExplorer.setRootDir(UserInfo.PATH + "/json");

        findViewById(R.id.explorer_back_button).setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.explorer_back_button:
                finish();
                break;
        }
    }
}
