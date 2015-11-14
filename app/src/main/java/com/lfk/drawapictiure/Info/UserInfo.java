package com.lfk.drawapictiure.Info;

import android.graphics.Color;
import android.os.Environment;

import java.io.File;

/**
 * Created by liufengkai on 15/8/23.
 */

public class UserInfo {
    // default info of user
    // Paint
    // paint width
    public static int PaintWidth = 10;
    // paint color
    public static int PaintColor = Color.rgb(80, 130, 250);
    // eraser width
    public static int EraserWidth = 50;

    public static boolean Editabled = false;

    public static final int FIRST_BUILD_PATINT = 20;

    // Internet
    public static final String url = "http://121.42.202.225:5000";

    public static final String sendNote = "/note/type";

    public static final String editNote = "/note/edit";

    public static final String getNote = "/note/get";

    public static final int SUCCESS_REGISTER = 0;

    public static final int SUCCESS_LOGIN = 1;

    public static final int CONTENT_ERROR = -1;

    public static final int UPDATE_SUCCESS = 2;

    public static final int EDIT_SUCCESS = 3;

    public static final int FIND_NOTE_SUCCESS = 4;

    public static final int FIND_NOTE_SEND_TO_HANDLER = 5;

    public static final int GET_NOTE_FROM_INTERNET = 6;

    public static final int FANKUI_SUCCESS = 7;

    public static final int CREATE_NOTE = 8;

    // User
    public static String UserName;

    public static final String PUBLIC_ID = "PUBLIC";

    public static final String PATH = Environment.getExternalStorageDirectory().getPath() + "/DrawAPicture";

    public static final File TextPath = new File(PATH + "/txt");

    public static final File PicPath = new File(PATH + "/picture");

    public static final File JsonPath = new File(PATH + "/json");

    public static String TOKEN;

    public static boolean ONLY_WIFI = false;

    public static boolean OPEN_CODE = false;

    // Intent result
    public static final int SAVE_PAINT = 10;

    public static final int SAVE_NOTE = 20;

    public static final int SAVE_CODE = 32;

    public static final int ONLY_WIFI_NOT_CONNET = -10;

    public static final int EMPTY = 0;

    // DataBase

    public static int CREATE_TABLE_SUCCESS;

    // Code File

    public static final int SELECT_FILE = 30;


}
