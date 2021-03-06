package com.lfk.drawapictiure.Activity;

import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Message;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.lfk.drawapictiure.Datebase.SQLHelper;
import com.lfk.drawapictiure.Info.UserInfo;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.Tools.PdfMaker;
import com.lfk.drawapictiure.Tools.PicUtils;
import com.lfk.drawapictiure.Tools.SPUtils;
import com.lfk.drawapictiure.View.MarkDown.MDReader;
import com.lfk.drawapictiure.View.ZoomTextView;
import com.lowagie.text.DocumentException;
import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.DimEffect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.orhanobut.logger.Logger;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class NoteActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialEditText editText;
    private static boolean THEFIRSTTIME = false;
    private static SQLiteDatabase database;
    private static String oldstring;
    private ZoomTextView mMarkDownView;
    private MDReader markdown = null;
    private boolean MARKDOWN = false;
    private SweetSheet mSweetSheet;
    private ScrollView mRootView;
    private ProgressDialog progressDialog = null;
    private boolean firstInto = true;
    private Snackbar snackbar;
    private Toolbar toolbar;
    private SpannableStringBuilder sb = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.darkblue));

        toolbar = (Toolbar) findViewById(R.id.note_toolbar);
        toolbar.setTitle("");

        String build = getIntent().getStringExtra("build");
        if (build != null) {
            ((TextView) findViewById(R.id.note_cre)).setText(build);
            THEFIRSTTIME = true;
        } else {
            THEFIRSTTIME = false;
            String title = getIntent().getStringExtra("timeMin");
            String subtitle = getIntent().getStringExtra("time");
            if (title != null || subtitle != null) {
                ((TextView) findViewById(R.id.note_first)).setText(title);
                ((TextView) findViewById(R.id.note_second)).setText(subtitle);
            }
        }
        setSupportActionBar(toolbar);
        editText = (MaterialEditText) findViewById(R.id.note_edit);
        findViewById(R.id.note_markdown).setOnClickListener(this);
        findViewById(R.id.note_menu).setOnClickListener(this);
        mMarkDownView = (ZoomTextView) findViewById(R.id.markdown);
        mMarkDownView.setVisibility(View.INVISIBLE);
        mRootView = (ScrollView) findViewById(R.id.root_view);
        findViewById(R.id.note_back_button).setOnClickListener(this);
        mRootView.setVisibility(View.INVISIBLE);
        mMarkDownView.setVisibility(View.INVISIBLE);
        oldstring = getIntent().getStringExtra("content");
        if (oldstring != null) {
            editText.setText(oldstring);
        }
        initSweetSheet();
        database = SQLiteDatabase.openOrCreateDatabase(SQLHelper.NAME, null);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 第一次进入 首次编辑 字数小于1000
        if (firstInto && !THEFIRSTTIME && editText.getText().length() < 1000) {
            firstInto = false;
//            markDown();
            markdown = new MDReader(editText.getText().toString());
            new Thread(() -> {
                sb = markdown.getFormattedContent();
            }).start();
        }
    }

    /**
     * 菜单
     */
    private void initSweetSheet() {
        ArrayList<MenuEntity> menuEntities = new ArrayList<>();
        MenuEntity share_menuEntity = new MenuEntity("分享便签", R.drawable.iconfont_share, "share");
        MenuEntity text_menuEntity = new MenuEntity("保存为文本", R.drawable.iconfont_txt, "txt");
        MenuEntity jpg_menuEntity = new MenuEntity("保存为图片", R.drawable.iconfont_jpg, "pic");
        MenuEntity md_menuEntity = new MenuEntity("保存为MarkDown文件", R.drawable.iconfont_md, "md");
        MenuEntity pdf_menuEntity = new MenuEntity("导出为pdf文件", R.drawable.iconfont_pdf, "pdf");
        menuEntities.add(text_menuEntity);
        menuEntities.add(jpg_menuEntity);
        menuEntities.add(md_menuEntity);
        menuEntities.add(pdf_menuEntity);
        menuEntities.add(share_menuEntity);
        mSweetSheet = new SweetSheet((RelativeLayout) findViewById(R.id.note_relative));
        mSweetSheet.setMenuList(menuEntities);

        mSweetSheet.setBackgroundEffect(new DimEffect(8));
        mSweetSheet.setDelegate(new RecyclerViewDelegate(true));
        mSweetSheet.setOnMenuItemClickListener((position, menuEntity) -> {
            // 根据返回值, true 会关闭 SweetSheet ,false 则不会.
            switch (menuEntity.id) {
                case "share":
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_TEXT, editText.getText().toString());
                    sendIntent.setType("text/plain");
                    startActivity(Intent.createChooser(sendIntent, "发送便签"));
                    break;
                case "txt":
                    saveAsRawContent();
                    break;
                case "pic":
                    saveAsBitmap();
                    break;
                case "md":
                    saveAsMarkdown();
                    break;
                case "pdf":
                    saveAsPdf();
                    break;
            }
            return true;
        });

    }

    private void snackMake(View view, String text) {
        snackbar = Snackbar.make(view, text, Snackbar.LENGTH_LONG);
        snackbar.setActionTextColor(Color.WHITE);
        Snackbar.SnackbarLayout ve = (Snackbar.SnackbarLayout) snackbar.getView();
        ve.setBackgroundColor(getResources().getColor(R.color.blue));
        snackbar.show();
    }

    /**
     * 导出(不包含格式)
     */
    public void saveAsRawContent() {
        if (!UserInfo.TextPath.exists()) {
            UserInfo.TextPath.mkdirs();
        }
        if (markdown == null)
            markdown = new MDReader(editText.getText().toString());
        String filepath = UserInfo.TextPath.getPath() + "/" + System.currentTimeMillis() + ".txt";
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath), "UTF-8"));
            writer.write(markdown.getRawContent());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        snackMake(toolbar, "成功保存到:" + filepath);
//        Toast.makeText(this, "成功保存到:" + filepath, Toast.LENGTH_LONG).show();
    }

    /**
     * 导出(pdf)
     */
    public void saveAsPdf() {
        if (!UserInfo.TextPath.exists()) {
            UserInfo.TextPath.mkdirs();
        }
        String filepath = UserInfo.TextPath.getPath() + "/" + System.currentTimeMillis() + ".pdf";
        if (!MARKDOWN) {
            markDown();
        }
        new Thread(() -> {
            try {
                PdfMaker.makeIt(NoteActivity.this, filepath, PicUtils.createBitmap(this, mRootView));
            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }).start();
        snackMake(toolbar, "成功保存到:" + filepath);
//        Toast.makeText(this, "成功保存到:" + filepath, Toast.LENGTH_LONG).show();
    }

    /**
     * 导出(图片,大小为至少一个屏幕大小)
     */
    public void saveAsBitmap() {
        if (!UserInfo.TextPath.exists()) {
            UserInfo.TextPath.mkdirs();
        }
        if (!MARKDOWN) {
            markDown();
        }
        String filepath = UserInfo.TextPath.getPath() + "/" + System.currentTimeMillis() + ".jpg";
        if (MARKDOWN) {
            try {
                FileOutputStream stream = new FileOutputStream(filepath);
                Bitmap bitmap = PicUtils.createBitmap(this, mRootView);
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
//                    Toast.makeText(this, "成功保存到:" + filepath, Toast.LENGTH_LONG).show();
                    snackMake(toolbar, "成功保存到:" + filepath);
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 导出MarkDown文件
     */
    public void saveAsMarkdown() {
        if (!UserInfo.TextPath.exists()) {
            UserInfo.TextPath.mkdirs();
        }
        if (markdown == null)
            markdown = new MDReader(editText.getText().toString());
        String filepath = UserInfo.TextPath.getPath() + "/" + System.currentTimeMillis() + ".md";
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath), "UTF-8"));
            writer.write(markdown.getContent());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        snackMake(toolbar, "成功保存到:" + filepath);
//        Toast.makeText(this, "成功保存到:" + filepath, Toast.LENGTH_LONG).show();
    }

    /**
     * 结束Activity,包含处理数据库,不包含上传
     */
    private void finishTheActivity() {
        final ContentValues values = new ContentValues();
        final Date d = new Date();
        final java.text.DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        final java.text.DateFormat format_time = new SimpleDateFormat("HH:mm");
        if (!editText.getText().toString().equals("")) {
            if (THEFIRSTTIME) {
                long name_time = System.currentTimeMillis();
                values.put("_name", name_time + "");
                values.put("_time", format_time.format(d));
                values.put("_pic", format.format(d));
                values.put("_content", editText.getText().toString());
                values.put("_username", (String) SPUtils.get(NoteActivity.this, "username", UserInfo.PUBLIC_ID));
                values.put("_type", 1);
                database.insert("note", null, values);
                database.close();
//                Logger.d("save tent");
                getResultIntent(editText.getText().toString(), name_time + "", format.format(d));
            } else {
                if (oldstring.length() != editText.getText().toString().length()) {
                    database.execSQL("update note set _time = " + "\"" + format_time.format(d)
                            + "\"" + ", _content=" + "\""
                            + editText.getText().toString() + "\""
                            + ", _pic = " + "\"" + format.format(d)
                            + "\"" + " where _name =" + "\"" + getIntent().getStringExtra("name") + "\""
                            + " and _username=" + "\"" + SPUtils.get(NoteActivity.this, "username", UserInfo.PUBLIC_ID) + "\"");
//                    Log.e("update note", getIntent().getStringExtra("name"));
                    database.close();
                    getResultIntent(editText.getText().toString(), getIntent().getStringExtra("name"), format.format(d));
                }
            }
        }
        finish();
    }

    private void getResultIntent(String content, String name, String pic) {
        Intent intent = new Intent(NoteActivity.this, MenuActivity.class);
        intent.putExtra("content", content);
        intent.putExtra("cache", pic);
        intent.putExtra("name", name);
        intent.putExtra("type", 1);
        setResult(RESULT_OK, intent);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.note_back_button:
                if (MARKDOWN) {
                    markDown();
                    mMarkDownView.dismiss();
                } else {
                    finishTheActivity();
                }
                break;
            case R.id.note_menu:
                mMarkDownView.dismiss();
                if (mSweetSheet.isShow()) {
                    mSweetSheet.dismiss();
                }
                mSweetSheet.toggle();
                break;
            case R.id.note_markdown:
                markDown();
                break;
        }
    }

    /**
     * 切换MarkDown状态
     */
    private void markDown() {
        progressDialog = ProgressDialog.show(this, "请等待", "正在转码...", true);
        if (MARKDOWN) {
            markdown = new MDReader(editText.getText().toString());
            mMarkDownView.setVisibility(View.INVISIBLE);
            mRootView.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.VISIBLE);
            MARKDOWN = false;
            mMarkDownView.dismiss();
            ((ImageView) findViewById(R.id.note_markdown)).
                    setImageDrawable(getResources().getDrawable(R.drawable.icon_markdown));
            progressDialog.dismiss();
        } else {
            markdown = new MDReader(editText.getText().toString());
            if (sb != null) {
                mMarkDownView.setTextKeepState(sb, TextView.BufferType.SPANNABLE);
                sb = null;
            } else {
//                new Thread(() -> {
                sb = markdown.getFormattedContent();
                mMarkDownView.setTextKeepState(sb, TextView.BufferType.SPANNABLE);
//                });
            }
            progressDialog.dismiss();
            editText.setVisibility(View.INVISIBLE);
            mRootView.setVisibility(View.VISIBLE);
            mMarkDownView.setVisibility(View.VISIBLE);
            oldstring = editText.getText().toString();
            ((ImageView) findViewById(R.id.note_markdown)).
                    setImageDrawable(getResources().getDrawable(R.drawable.icon_markdown1));
            MARKDOWN = true;
        }
    }

    private android.os.Handler handler = new android.os.Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
//            switch (msg.what){


//            }
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mSweetSheet.isShow()) {
                    mSweetSheet.dismiss();
                } else {
                    if (MARKDOWN) {
                        markDown();
                        mMarkDownView.dismiss();
                    } else {
                        finishTheActivity();
                    }
                }
                break;

        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            mMarkDownView.dismiss();
            Logger.e("接受点击");
        }
        return true;
    }
}
