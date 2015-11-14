package com.lfk.drawapictiure.Activity;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.lfk.drawapictiure.Datebase.SQLHelper;
import com.lfk.drawapictiure.Info.UserInfo;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.Tools.PdfMaker;
import com.lfk.drawapictiure.Tools.SPUtils;
import com.lfk.drawapictiure.View.MarkDown.MDReader;
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

//import android.util.Log;
//import com.orhanobut.logger.Logger;

public class NoteActivity extends AppCompatActivity implements View.OnClickListener {
    private MaterialEditText editText;
    private static boolean THEFIRSTTIME = false;
    private static SQLiteDatabase database;
    private static String oldstring;
    private TextView mMarkDownView;
    private MDReader markdown = null;
    private boolean MARKDOWN = false;
    private SweetSheet mSweetSheet;
    private ScrollView mRootView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.darkblue));

        Toolbar toolbar = (Toolbar) findViewById(R.id.note_toolbar);
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
        mMarkDownView = (TextView) findViewById(R.id.markdown);
        mMarkDownView.setVisibility(View.INVISIBLE);
        mRootView = (ScrollView) findViewById(R.id.root_view);
        findViewById(R.id.note_back_button).setOnClickListener(this);
        mRootView.setVisibility(View.INVISIBLE);
        mMarkDownView.setVisibility(View.INVISIBLE);
        oldstring = getIntent().getStringExtra("content");
        if (oldstring != null) {
            editText.setText(oldstring);
            markDown();
        }
        initSweetSheet();
        database = SQLiteDatabase.openOrCreateDatabase(SQLHelper.NAME, null);
    }

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

    public void saveAsRawContent() {
        if (!UserInfo.TextPath.exists()) {
            UserInfo.TextPath.mkdirs();
        }
        if (markdown == null)
            markdown = new MDReader(editText.getText().toString());
        String filepath = UserInfo.PATH + "/txt/" + System.currentTimeMillis() + ".txt";
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath), "UTF-8"));
            writer.write(markdown.getRawContent());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "成功保存到:" + filepath, Toast.LENGTH_LONG).show();
    }

    public void saveAsPdf() {
        if (!UserInfo.TextPath.exists()) {
            UserInfo.TextPath.mkdirs();
        }
        String filepath = UserInfo.PATH + "/txt/" + System.currentTimeMillis() + ".pdf";
        if (!MARKDOWN) {
            markDown();
        }
        try {
            PdfMaker.makeIt(this, filepath, createBitmap(mRootView));
        } catch (DocumentException | IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "成功保存到:" + filepath, Toast.LENGTH_LONG).show();
    }

    public void saveAsBitmap() {
        if (!UserInfo.TextPath.exists()) {
            UserInfo.TextPath.mkdirs();
        }
        if (!MARKDOWN) {
            markDown();
        }
        String filepath = UserInfo.PATH + "/txt/" + System.currentTimeMillis() + ".jpg";
        if (MARKDOWN) {
            try {
                FileOutputStream stream = new FileOutputStream(filepath);
                Bitmap bitmap = createBitmap(mRootView);
                if (bitmap != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                    Toast.makeText(this, "成功保存到:" + filepath, Toast.LENGTH_LONG).show();
                }
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveAsMarkdown() {
        if (!UserInfo.TextPath.exists()) {
            UserInfo.TextPath.mkdirs();
        }
        if (markdown == null)
            markdown = new MDReader(editText.getText().toString());
        String filepath = UserInfo.PATH + "/txt/" + System.currentTimeMillis() + ".md";
        try {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filepath), "UTF-8"));
            writer.write(markdown.getContent());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Toast.makeText(this, "成功保存到:" + filepath, Toast.LENGTH_LONG).show();
    }

    public static Bitmap createBitmap(ScrollView v) {
        int width = 0, height = 0;
        for (int i = 0; i < v.getChildCount(); i++) {
            width += v.getChildAt(i).getWidth();
            height += v.getChildAt(i).getHeight();
        }
        Logger.e("检测到 " + "h: " + height + "w: " + width);
        if (width <= 0 || height <= 0) {
            Logger.e("未检测到 " + "h: " + height + "w: " + width);
            return null;
        }
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        v.draw(canvas);
        return bitmap;
    }

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
                } else {
                    finishTheActivity();
                }
                break;
            case R.id.note_menu:
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

    private void markDown() {
        if (MARKDOWN) {
            mMarkDownView.setVisibility(View.INVISIBLE);
            mRootView.setVisibility(View.INVISIBLE);
            editText.setVisibility(View.VISIBLE);
            MARKDOWN = false;
            ((ImageView) findViewById(R.id.note_markdown)).
                    setImageDrawable(getResources().getDrawable(R.drawable.icon_markdown));
        } else {
            markdown = new MDReader(editText.getText().toString());
            mMarkDownView.setTextKeepState(markdown.getFormattedContent(), TextView.BufferType.SPANNABLE);
            editText.setVisibility(View.INVISIBLE);
            mRootView.setVisibility(View.VISIBLE);
            mMarkDownView.setVisibility(View.VISIBLE);
            oldstring = editText.getText().toString();
            ((ImageView) findViewById(R.id.note_markdown)).
                    setImageDrawable(getResources().getDrawable(R.drawable.icon_markdown1));
            MARKDOWN = true;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                if (mSweetSheet.isShow()) {
                    mSweetSheet.dismiss();
                } else {
                    if (MARKDOWN) {
                        markDown();
                    } else {
                        finishTheActivity();
                    }
                }
                break;

        }
        return true;
    }
}
