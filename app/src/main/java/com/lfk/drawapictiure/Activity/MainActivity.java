package com.lfk.drawapictiure.Activity;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.annotation.TargetApi;
import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.lfk.drawapictiure.Datebase.SQLHelper;
import com.lfk.drawapictiure.Info.PathNode;
import com.lfk.drawapictiure.Info.UserInfo;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.Tools.PdfMaker;
import com.lfk.drawapictiure.Tools.SPUtils;
import com.lfk.drawapictiure.View.PaintView;
import com.lowagie.text.DocumentException;
import com.mingle.entity.MenuEntity;
import com.mingle.sweetpick.DimEffect;
import com.mingle.sweetpick.RecyclerViewDelegate;
import com.mingle.sweetpick.SweetSheet;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionButton;
import com.oguzdev.circularfloatingactionmenu.library.FloatingActionMenu;
import com.oguzdev.circularfloatingactionmenu.library.SubActionButton;
import com.readystatesoftware.systembartint.SystemBarTintManager;
import com.rengwuxian.materialedittext.MaterialEditText;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import afzkl.development.colorpickerview.dialog.ColorPickerDialogFragment;
import me.drakeet.materialdialog.MaterialDialog;

public class MainActivity extends AppCompatActivity implements ColorPickerDialogFragment.ColorPickerDialogListener, View.OnClickListener {
    private PaintView paintView;
    private TextView PenWidthView;
    private int PenWidth;
    private int EraserWidth;
    private boolean VISIBLE = false;
    //    private static boolean OPEN = false;
    private MaterialDialog mMaterialDialog;
    private FloatingActionButton rightLowerButton;
    private FloatingActionMenu rightLowerMenu;
    private SubActionButton.Builder rLSubBuilder;
    private static final int DIALOG_ID = 0;
    private static final int PREFERENCE_DIALOG_ID = 1;
    private static final int SELECT_PICTURE = 2;
    private static final int SELECT_FILE = 3;
    private static boolean THEFIRSTTIME = false;
    private static boolean EDITABLED = false;
    private PathNode pathNode;
    private static SQLiteDatabase database;
    // 保存的文件名
    private static String Paintname;
    private ImageButton imageButton;
    // menu
    private SweetSheet mSweetSheet;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setVisibility();
        initSweetSheet();

        SystemBarTintManager tintManager = new SystemBarTintManager(this);
        tintManager.setStatusBarTintEnabled(true);
//        tintManager.setNavigationBarTintEnabled(true);
        tintManager.setTintColor(getResources().getColor(R.color.darkblue));

        pathNode = (PathNode) getApplication();
        paintView = (PaintView) findViewById(R.id.paint);
        paintView.setIsRecordPath(true, pathNode);
        paintView.setOnPathListener((x, y, event, IsPaint) -> {
            EDITABLED = true;
            PathNode.Node temp_node = pathNode.new Node();
            temp_node.x = paintView.px2dip(x);
            temp_node.y = paintView.px2dip(y);
            if (IsPaint) {
                temp_node.PenColor = UserInfo.PaintColor;
                temp_node.PenWidth = UserInfo.PaintWidth;
            } else {
                temp_node.EraserWidth = UserInfo.EraserWidth;
            }
            temp_node.IsPaint = IsPaint;
            temp_node.TouchEvent = event;
            temp_node.time = System.currentTimeMillis();
            pathNode.addNode(temp_node);
////            Log.e("x:" + x + "y:" + y, "time:" + temp_node.time);
        });
        initView();
        database = SQLiteDatabase.openOrCreateDatabase(SQLHelper.NAME, null);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onResume() {
        super.onResume();
        setVisibility();
        Intent intent = getIntent();
        String paint_name = intent.getStringExtra("paint_name");
        String paint_content = intent.getStringExtra("paint_content");
        if (paint_name != null) {
            if (paint_name.equals("新建")) {
                THEFIRSTTIME = true;
                setMenu();
                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                Date now = new Date();
                Paintname = formatter.format(now);
            } else {
                THEFIRSTTIME = false;
                Paintname = paint_name;
            }
            ((TextView) findViewById(R.id.paint_name)).setText(paint_name);
            if (paint_content != null) {
                paintView.ContentToPathNodeToHandle(paint_content);
            }
        } else if (intent.getData() != null) {
            paintView.JsonToPathNodeToHandle(intent.getData());
        }
//        paintView.preview(pathNode.getPathList());
    }

    @Override
    protected void onPause() {
        super.onPause();
        setVisibility();
        paintView.save();
        paintView.setIsFirstOpen(true);
    }

    @Override
    protected void onStop() {
        super.onStop();
        pathNode.clearList();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private void setVisibility() {
        getWindow().getDecorView().setSystemUiVisibility
                (View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_IMMERSIVE);
    }

    private void initView() {
        findViewById(R.id.main_back_button).setOnClickListener(this);
        final ImageView fabIconNew = new ImageView(this);
        fabIconNew.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_new_light));
        rightLowerButton = new FloatingActionButton.Builder(this)
                .setContentView(fabIconNew)
                .build();
        rLSubBuilder = new SubActionButton.Builder(this);
        ImageView color = new ImageView(this);
        ImageView pen = new ImageView(this);
//        ImageView file = new ImageView(this);
        ImageView eraser = new ImageView(this);
        ImageView clean = new ImageView(this);
//        ImageView preview = new ImageView(this);

        color.setImageDrawable(getResources().getDrawable(R.drawable.iconfont_color));
        pen.setImageDrawable(getResources().getDrawable(R.drawable.iconfont_bi));
//        file.setImageDrawable(getResources().getDrawable(R.drawable.iconfont_daoru));
        eraser.setImageDrawable(getResources().getDrawable(R.drawable.iconfont_xpc));
        clean.setImageDrawable(getResources().getDrawable(R.drawable.iconfont_clear));
//        preview.setImageDrawable(getResources().getDrawable(R.drawable.icon_chonghui));

        rightLowerMenu = new FloatingActionMenu.Builder(this)
//                .addSubActionView(rLSubBuilder.setContentView(preview).build())
                .addSubActionView(rLSubBuilder.setContentView(color).build())
                .addSubActionView(rLSubBuilder.setContentView(pen).build())
//                .addSubActionView(rLSubBuilder.setContentView(file).build())
                .addSubActionView(rLSubBuilder.setContentView(eraser).build())
                .addSubActionView(rLSubBuilder.setContentView(clean).build())
                .attachTo(rightLowerButton)
                .build();

        rightLowerMenu.setStateChangeListener(new FloatingActionMenu.MenuStateChangeListener() {
            @Override
            public void onMenuOpened(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees clockwise
                fabIconNew.setRotation(0);
//                OPEN = true;
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 45);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR);
                animation.start();
            }

            @Override
            public void onMenuClosed(FloatingActionMenu menu) {
                // Rotate the icon of rightLowerButton 45 degrees counter-clockwise
                fabIconNew.setRotation(45);
//                OPEN = false;
                PropertyValuesHolder pvhR = PropertyValuesHolder.ofFloat(View.ROTATION, 0);
                ObjectAnimator animation = ObjectAnimator.ofPropertyValuesHolder(fabIconNew, pvhR);
                animation.start();
            }
        });

//        file.setOnLongClickListener(view -> {
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.setType("file/*");
//            startActivityForResult(intent, SELECT_FILE);
//            return true;
//        });

//        preview.setOnClickListener(view -> {
//            if (!paintView.isShowing()) {
//                paintView.preview(pathNode.getPathList());
//            }
//        });
//
//        preview.setOnLongClickListener(view -> {
//            paintView.PathNodeToJson(pathNode, new File(UserInfo.PATH+"/json"));
//            return false;
//        });
        clean.setOnClickListener(v -> {
            if (!paintView.isShowing()) {
                paintView.clean();
                pathNode.clearList();
                paintView.clearReUnList();
            }
        });
        eraser.setOnClickListener(view -> paintView.Eraser());
        pen.setOnClickListener(view -> paintView.Paint());
        initEraserView();
        pen.setOnLongClickListener(view -> {
            initFirstDialog();
            mMaterialDialog.show();
            return true;
        });
        color.setOnClickListener(view -> {
            ColorPickerDialogFragment f = ColorPickerDialogFragment
                    .newInstance(DIALOG_ID, null, null, Color.BLACK, true);
            f.setStyle(DialogFragment.STYLE_NORMAL, R.style.LightPickerDialogTheme);
            f.show(getFragmentManager(), "d");
        });
        eraser.setOnLongClickListener(view -> {
            initEraserView();
            mMaterialDialog.show();
            return true;
        });

//        file.setOnClickListener(view -> {
//            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
//            intent.addCategory(Intent.CATEGORY_OPENABLE);
//            intent.setType("image/*");
//            startActivityForResult(Intent.createChooser(intent, "选择图片"), SELECT_PICTURE);
//        });

//        rightLowerButton.setOnLongClickListener(view -> {
//            paintView.PathNodeToJson(pathNode, new File(UserInfo.PATH + "/json"));
//            return true;
//        });

        findViewById(R.id.main_back).setOnClickListener(this);
        findViewById(R.id.paint_player_it).setOnClickListener(this);
        findViewById(R.id.paint_paint_it).setOnClickListener(this);
        findViewById(R.id.paint_more).setOnClickListener(this);
        imageButton = (ImageButton) findViewById(R.id.paint_paint_it);
        initMenu();
    }


    private void initFirstDialog() {
        View view = View.inflate(this, R.layout.activity_paint_width, null);
        PenWidthView = (TextView) view.findViewById(R.id.width_text);
        PenWidthView.setText(UserInfo.PaintWidth + "");
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.width_seek);
        seekBar.setProgress(UserInfo.PaintWidth);
        PenWidth = UserInfo.PaintWidth;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                PenWidth = i;
                PenWidthView.setText("" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mMaterialDialog = new MaterialDialog(this)
                .setTitle("设定笔粗")
                .setContentView(view)
                .setPositiveButton("确定", view1 -> {
                    paintView.setPenWidth(PenWidth);
                    UserInfo.PaintWidth = PenWidth;
                    mMaterialDialog.dismiss();
                    setVisibility();
                    paintView.Paint();
                })
                .setNegativeButton("取消", view1 -> {
                    mMaterialDialog.dismiss();
                    setVisibility();
                });
    }


    private void initEraserView() {
        View view = View.inflate(this, R.layout.activity_paint_width, null);
        PenWidthView = (TextView) view.findViewById(R.id.width_text);
        PenWidthView.setText(UserInfo.EraserWidth + "");
        SeekBar seekBar = (SeekBar) view.findViewById(R.id.width_seek);
        seekBar.setProgress(UserInfo.EraserWidth);
        EraserWidth = UserInfo.EraserWidth;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                EraserWidth = i;
                PenWidthView.setText("" + i);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        mMaterialDialog = new MaterialDialog(this)
                .setTitle("设定橡皮粗")
                .setContentView(view)
                .setPositiveButton("确定", view1 -> {
                    paintView.setmEraserPaint(EraserWidth);
                    UserInfo.EraserWidth = EraserWidth;
                    paintView.Eraser();
                    mMaterialDialog.dismiss();
                    setVisibility();
                })
                .setNegativeButton("取消", view1 -> {
                    mMaterialDialog.dismiss();
                    setVisibility();
                });
    }


    @Override
    public void onColorSelected(int dialogId, int color) {
        switch (dialogId) {
            case PREFERENCE_DIALOG_ID:
                this.onColorSelected(dialogId, color);
                break;
            case DIALOG_ID:
                paintView.setColor(color);
                UserInfo.PaintColor = color;
                setVisibility();
                paintView.Paint();
                break;
        }

    }

    @Override
    public void onDialogDismissed(int dialogId) {

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case SELECT_PICTURE:
                    paintView.setmBitmap(data.getData());
                    break;
                case SELECT_FILE:
                    paintView.JsonToPathNodeToHandle(data.getData());
                    break;
            }
        }
    }

    private void finishTheActivity() {
        final ContentValues values = new ContentValues();
        final Date d = new Date();
        final java.text.DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
//        Logger.e(THEFIRSTTIME + "", "结束");
        if (THEFIRSTTIME) {
            View view = View.inflate(this, R.layout.activity_paint_name, null);
            final MaterialEditText editText = (MaterialEditText) view.findViewById(R.id.material_edit);
            mMaterialDialog = new MaterialDialog(this)
                    .setTitle("设定保存名称")
                    .setContentView(view)
                    .setPositiveButton("确定", view1 -> {
                        if (!editText.getText().toString().equals("")) {
                            mMaterialDialog.dismiss();
                            String content = paintView.PathNodeToJsonString(pathNode, "");
                            String pic = paintView.PathNodeToBitmapToString();
                            new Thread(() -> {
                                values.put("_name", editText.getText().toString());
                                values.put("_time", format.format(d));
                                values.put("_content", content);
                                values.put("_username", (String) SPUtils.get(MainActivity.this, "username", UserInfo.PUBLIC_ID));
                                values.put("_pic", pic);
                                values.put("_type", 0);
                                database.insert("note", null, values);
                                database.close();
                                UserInfo.Editabled = false;
                            }).start();
                            getResultIntent(content
                                    , editText.getText().toString()
                                    , pic, true);
                            finish();
                        } else {
                            Toast.makeText(this, "请输入名字", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("取消", view1 -> {
                        mMaterialDialog.dismiss();
                        setVisibility();
                    })
                    .setNatureButton("不保存", view1 -> finish());
            mMaterialDialog.show();
        } else {
            if (UserInfo.Editabled) {
                UserInfo.Editabled = false;
                String content = paintView.PathNodeToJsonString(pathNode, "");
                String pic = paintView.PathNodeToBitmapToString();
                database.execSQL("update note"
                        + " set _time = " + "\"" + format.format(d)
                        + "\"" + ", _content=" + "\""
                        + content + "\""
                        + ", _pic = " + "\"" + pic
                        + "\"" + " where _name =" + "\"" + Paintname + "\"");
                getResultIntent(content, Paintname, pic, false);
                database.close();
                finish();
            } else {
                finish();
            }
        }
    }

    private void getResultIntent(String content, String name, String pic, boolean type) {
        Intent intent = new Intent(MainActivity.this, MenuActivity.class);
        intent.putExtra("content", content);
        intent.putExtra("cache", pic);
        intent.putExtra("name", name);
        intent.putExtra("type", 0);
//        Logger.e("intent it");
        this.setResult(RESULT_OK, intent);
    }

    private void setMenu() {
        setVisibility();
        if (VISIBLE) {
            if (rightLowerMenu.isOpen())
                rightLowerMenu.close(true);
            rightLowerButton.setVisibility(View.INVISIBLE);
            paintView.setIsEditting(false);
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.iconfont_modify));
        } else {
            rightLowerButton.setVisibility(View.VISIBLE);
            if (!rightLowerMenu.isOpen())
                rightLowerMenu.open(true);
            paintView.setIsEditting(true);
            imageButton.setImageDrawable(getResources().getDrawable(R.drawable.iconfont_cancle));
        }
        paintView.isFocusable();
        VISIBLE = !VISIBLE;
    }

    private void initMenu() {
        rightLowerMenu.close(true);
        rightLowerButton.setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_HOME:
                moveTaskToBack(true);
                break;
            case KeyEvent.KEYCODE_MENU:
                setMenu();
                break;
            case KeyEvent.KEYCODE_VOLUME_UP:
                paintView.ReDoORUndo(true);
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                paintView.ReDoORUndo(false);
                break;
            case KeyEvent.KEYCODE_BACK:
                if (mSweetSheet.isShow()) {
                    mSweetSheet.dismiss();
                } else {
                    finishTheActivity();
                }
                break;
        }
        return true;
    }

    /**
     * 菜单
     */
    private void initSweetSheet() {
        ArrayList<MenuEntity> menuEntities = new ArrayList<>();
        MenuEntity share_menuEntity = new MenuEntity("分享", R.drawable.iconfont_share, "share");
        MenuEntity z_menuEntity = new MenuEntity("保存为帧动画", R.drawable.iconfont_txt, "lfk");
        MenuEntity jpg_menuEntity = new MenuEntity("保存为图片", R.drawable.iconfont_jpg, "pic");
        MenuEntity pdf_menuEntity = new MenuEntity("导出为pdf文件", R.drawable.iconfont_pdf, "pdf");
        MenuEntity import_menuEntity = new MenuEntity("导入帧动画", R.drawable.iconfont_pdf, "import");
        menuEntities.add(z_menuEntity);
        menuEntities.add(import_menuEntity);
        menuEntities.add(jpg_menuEntity);
        menuEntities.add(pdf_menuEntity);
        menuEntities.add(share_menuEntity);
        mSweetSheet = new SweetSheet((RelativeLayout) findViewById(R.id.paint_reative));
        mSweetSheet.setMenuList(menuEntities);
        mSweetSheet.setBackgroundEffect(new DimEffect(8));
        mSweetSheet.setDelegate(new RecyclerViewDelegate(true));
        mSweetSheet.setOnMenuItemClickListener((position, menuEntity) -> {
            // 根据返回值, true 会关闭 SweetSheet ,false 则不会.
            switch (menuEntity.id) {
                case "share":
                    Uri uri = paintView.BitmapToPicture(Paintname, new File(UserInfo.PATH + "/picture"));
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    sendIntent.setType("image/jpeg");
                    startActivity(Intent.createChooser(sendIntent, "发送图片"));
                    break;
                case "pic":
                    saveAsBitmap();
                    break;
                case "pdf":
                    saveAsPdf();
                    break;
                case "import":
                    importPic();
                    break;
                case "lfk":
                    saveAsZen();
                    break;
            }
            return true;
        });
    }

    /**
     * 保存帧动画
     */
    private void saveAsZen() {
//        new Thread(() -> {
        paintView.PathNodeToJson(Paintname, pathNode, new File(UserInfo.PATH + "/json"));
        Toast.makeText(this, "成功保存到:" + UserInfo.PATH + "/picture/" + Paintname + ".l", Toast.LENGTH_SHORT).show();
//        }).start();
    }

    /**
     * 引入帧动画
     */
    private void importPic() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("file/*");
        startActivityForResult(Intent.createChooser(intent, "选择帧动画"), SELECT_FILE);
    }

    /**
     * 保存图片
     */
    private void saveAsBitmap() {
        new Thread(() -> {
            paintView.BitmapToPicture(Paintname, new File(UserInfo.PATH + "/picture"));
        }).start();
        Toast.makeText(this, "成功保存到:" + UserInfo.PATH + "/picture/" + Paintname + ".jpg", Toast.LENGTH_SHORT).show();
    }

    /**
     * 保存PDF
     */
    private void saveAsPdf() {
        if (!UserInfo.TextPath.exists()) {
            UserInfo.TextPath.mkdirs();
        }
        String filepath = UserInfo.TextPath.getPath() + "/" + System.currentTimeMillis() + ".pdf";
        new Thread(() -> {
            try {
                PdfMaker.makeIt(MainActivity.this, filepath, paintView.saveAsBitmap());

            } catch (DocumentException | IOException e) {
                e.printStackTrace();
            }
        }).start();
        Toast.makeText(this, "成功保存到:" + filepath, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.main_back_button:
                finishTheActivity();
                break;
            case R.id.paint_player_it:
                if (!paintView.isShowing()) {
                    paintView.preview(pathNode.getPathList());
                }
                break;
            case R.id.paint_paint_it:
                setMenu();
                break;
            case R.id.paint_more:
                if (VISIBLE) {
                    setMenu();
                }
                if (mSweetSheet.isShow()) {
                    mSweetSheet.dismiss();
                }
                mSweetSheet.toggle();
                break;
        }
    }
}
