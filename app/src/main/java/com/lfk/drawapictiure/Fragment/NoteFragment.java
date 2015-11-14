package com.lfk.drawapictiure.Fragment;


import android.app.Fragment;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.CursorIndexOutOfBoundsException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Toast;

import com.lfk.drawapictiure.Activity.NoteActivity;
import com.lfk.drawapictiure.Adapter.NodeLayoutAdapter;
import com.lfk.drawapictiure.Info.NodeInfo;
import com.lfk.drawapictiure.Info.UserInfo;
import com.lfk.drawapictiure.R;
import com.lfk.drawapictiure.Tools.HttpUtils;
import com.lfk.drawapictiure.Tools.NetUtils;
import com.lfk.drawapictiure.Tools.SPUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import me.drakeet.materialdialog.MaterialDialog;

/**
 * A simple {@link Fragment} subclass.
 */
public class NoteFragment extends android.support.v4.app.Fragment {
    private RecyclerView mRecyclerView;
    private NodeLayoutAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private MaterialDialog mMaterialDialog;
    private static SQLiteDatabase database;
    private String token;
    private SwipeRefreshLayout swipeLayout;


    public static NoteFragment newInstance(SQLiteDatabase database) {
        return new NoteFragment(database);
    }

    public NoteFragment() {
        // Required empty public constructor
    }

    public NoteFragment(SQLiteDatabase database) {
        this.database = database;
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View wrapper = inflater.inflate(R.layout.fragment_note, container, false);
        mRecyclerView = (RecyclerView) wrapper.findViewById(R.id.note_recycle_view);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        token = (String) SPUtils.get(getActivity(), "token", "token");
        swipeLayout = (SwipeRefreshLayout) wrapper.findViewById(R.id.note_swipe_refresh);
        swipeLayout.setColorScheme(R.color.blue);
        swipeLayout.setOnRefreshListener(() -> new Handler().postDelayed(() -> {
            swipeLayout.setRefreshing(true);
            FindInInternetWithUrl();
            refreshTheAdapter();
            Intent intent = new Intent();
            intent.setAction("REFRESH");
            getActivity().sendBroadcast(intent);
        }, 500));
        return wrapper;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshTheAdapter();
    }

    private void refreshTheAdapter() {
        ArrayList<NodeInfo> arrayList = new ArrayList<>();
        NodeInfo nodeInfo;
        if (database != null) {
            Cursor cursor = database.rawQuery("Select * From note" +
                    " where _type = 1 and _username=" + "\""
                    + SPUtils.get(getActivity(), "username", UserInfo.PUBLIC_ID) + "\"", null);
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex("_name"));
                    String time = cursor.getString(cursor.getColumnIndex("_time"));
                    String content = cursor.getString(cursor.getColumnIndex("_content"));
                    String timeMin = cursor.getString(cursor.getColumnIndex("_pic"));
                    // name->root 主键 time->time pic->timeMin
//                    Log.e("name"+name+"time"+time,"content"+content+"timeMin"+timeMin);
                    nodeInfo = new NodeInfo(time, timeMin, content, name);
                    arrayList.add(0, nodeInfo);
                } while (cursor.moveToNext());
            }
        }
        mAdapter = new NodeLayoutAdapter(arrayList, getActivity());
        mAdapter.setItemClickListener((name, time, timeMin, content) -> {
            Intent intent = new Intent(getActivity(), NoteActivity.class);
            intent.putExtra("name", name);
            intent.putExtra("time", time);
            intent.putExtra("timeMin", timeMin);
            intent.putExtra("content", content);
            intent.putExtra("type", 1);
            getActivity().startActivityForResult(intent, UserInfo.SAVE_NOTE);
        });

        mAdapter.setItemLongClickListener((name, position) -> {
            View CheckView = View.inflate(getActivity(), R.layout.delete_item, null);
            CheckBox checkBox = (CheckBox) CheckView.findViewById(R.id.check_it);
            mMaterialDialog = new MaterialDialog(getActivity())
                    .setTitle("确定删除？")
                    .setContentView(CheckView)
                    .setPositiveButton("确定", view -> {
                        mAdapter.remove(position);
                        if (checkBox.isChecked())
                            deleteInInternet(name);
                        deleteFromDataBase(name);
                        mMaterialDialog.dismiss();
                    })
                    .setNegativeButton("取消", view -> {
                        mMaterialDialog.dismiss();
                    });
            mMaterialDialog.show();
        });
        mRecyclerView.setAdapter(mAdapter);
        swipeLayout.setRefreshing(false);
    }

    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UserInfo.GET_NOTE_FROM_INTERNET:
                    final ContentValues values = new ContentValues();
                    final Date d = new Date();
                    final java.text.DateFormat format = new SimpleDateFormat("dd/MM/yyyy");
                    final java.text.DateFormat format_time = new SimpleDateFormat("HH:mm");
                    Bundle bundle = (Bundle) msg.obj;
                    String name = bundle.getString("name");
//                    Log.e("note name", name);
                    int id = bundle.getInt("fileid");
                    try {
                        JSONObject jsonObject = new JSONObject(bundle.getString("response"));
                        database.execSQL("delete from note"
                                + " where _name=" + "\"" + name + "\"" + " and _username=" + "\"" +
                                SPUtils.get(getActivity(), "username", UserInfo.PUBLIC_ID) + "\"");
                        values.put("_name", name);
                        values.put("_time", format_time.format(d));
                        values.put("_pic", format.format(d));
                        values.put("_content", jsonObject.getString("message"));
                        values.put("_username", UserInfo.UserName);
                        values.put("_id", id);
                        values.put("_type", 1);
                        database.insert("note", null, values);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    refreshTheAdapter();
                    break;
                case UserInfo.EMPTY:
                    swipeLayout.setRefreshing(false);
                    break;
                case UserInfo.ONLY_WIFI_NOT_CONNET:
                    Toast.makeText(getActivity(), "仅在Wi-Fi下更新", Toast.LENGTH_SHORT).show();
                    swipeLayout.setRefreshing(false);
                    break;
                case UserInfo.CONTENT_ERROR:
                    Toast.makeText(getActivity(), "连接错误", Toast.LENGTH_SHORT).show();
                    swipeLayout.setRefreshing(false);
                    break;
            }
        }
    };


    private void deleteFromDataBase(String name) {
        database.execSQL("delete from note"
                + " where _name = " + "\"" + name + "\"");
    }

    private void deleteInInternet(String name) {
        if (!name.equals(UserInfo.PUBLIC_ID)) {
            try {
//                Log.e("name delete", name);
                Cursor cursor = database.rawQuery("Select * From note"
                        + " where _name=" + "\"" + name + "\"" + " and _username=" + "\""
                        + SPUtils.get(getActivity(), "username", UserInfo.PUBLIC_ID) + "\"", null);
                cursor.moveToFirst();
                int id = cursor.getInt(cursor.getColumnIndex("_id"));
                cursor.close();
                if (!NetUtils.isConnected(getActivity()) || (UserInfo.ONLY_WIFI && !NetUtils.isWifi(getActivity()))) {
//                    String json = (String)SPUtils.get(getActivity(),"delete_list","");
//                    List<Integer> list = MessageGZIP.JsonToList(json);
//                    list.add(id);
////                    Log.e("notefragment", "" + id);
//                    SPUtils.put(getActivity(),"delete_list",MessageGZIP.ListToJson(list));
                    Toast.makeText(getActivity(), "非联网状态下无法删除云端", Toast.LENGTH_SHORT).show();
                }
                HttpUtils.GetFromHttp(UserInfo.url + UserInfo.editNote + "/" + id + "/del?token=" + token);
//                Log.e("delete url", UserInfo.url + UserInfo.editNote + "/" + id + "/del?token="+token);
            } catch (CursorIndexOutOfBoundsException e) {
//                Log.e("internet","删除失败");
            }
        }
    }

    private void FindInInternetWithUrl() {
        if (!UserInfo.UserName.equals(UserInfo.PUBLIC_ID)) {
            String url = "http://121.42.202.225:5000/note/type/1/get?token=" + token;
            HttpUtils.GetFromHttps(getActivity(), url, handler, UserInfo.GET_NOTE_FROM_INTERNET);
        } else {
            String url = "http://121.42.202.225:5000/note/type/1/get?token=" + token;
            HttpUtils.GetFromHttps(getActivity(), url, handler, UserInfo.GET_NOTE_FROM_INTERNET);
        }
    }

}
