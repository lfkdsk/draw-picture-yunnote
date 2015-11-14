package com.lfk.drawapictiure.Datebase;

import android.content.Context;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

/**
 * Created by liufengkai on 15/9/16.
 */
public class SQLHelper extends SQLiteOpenHelper {
    public static String NAME = Environment.getExternalStorageDirectory().getPath() + "/DrawAPicture"+"/database/mine.db";

    public SQLHelper(Context context) {
        super(context, NAME, null, 1);
    }

    public SQLHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version, DatabaseErrorHandler errorHandler) {
        super(context, name, factory, version, errorHandler);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        String CREATE_TABLE = "create table if not exists note "+
                "(_name text primary key on conflict replace," +
                "_time text," +
                "_content text," +
                "_pic text,"+
                "_id integer,"+
                "_username text,"+
                "_type integer)";
        // 在paint中 name-> name time -> time content -> content pic->pic type->0
        // 在note中 name->毫秒的时间 time->time content ->content pic->timeMin type ->1
        sqLiteDatabase.execSQL(CREATE_TABLE);
//        Log.e("SQL","新建成功");
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
