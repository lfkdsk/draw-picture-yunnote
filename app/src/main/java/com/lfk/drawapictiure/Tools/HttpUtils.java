package com.lfk.drawapictiure.Tools;

/**
 * Created by liufengkai on 15/9/20.
 */

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.lfk.drawapictiure.Activity.MenuActivity;
import com.lfk.drawapictiure.Info.UserInfo;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
public class HttpUtils {

    public static void PostToHttp(Context context, String url, List<NameValuePair> params, Handler handler, int type) {
//        Logger.e("url", url);
        if ((!UserInfo.ONLY_WIFI && NetUtils.isConnected(context)) || (UserInfo.ONLY_WIFI && NetUtils.isWifi(context))) {
            new Thread(() -> {
                HttpPost httpPost = new HttpPost(url);
                try {
                    HttpEntity httpentity = new UrlEncodedFormEntity(params, "utf-8");
                    // 请求httpRequest
                    httpPost.setEntity(httpentity);
                    // 取得默认的HttpClient
                    DefaultHttpClient httpclient = new DefaultHttpClient();
                    // 取得HttpResponse
                    HttpResponse httpResponse = httpclient.execute(httpPost);
                    if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                        // 取得返回的字符串
                        String strResult = EntityUtils.toString(httpResponse.getEntity());
//                        Logger.e(strResult, "成功获取");
                        Message message = new Message();
                        switch (type) {
                            case UserInfo.SUCCESS_LOGIN:
                                try {
                                    JSONObject jsonObject = new JSONObject(strResult);
                                    if (jsonObject.getInt("code") == 1) {
                                        message.what = type;
                                        message.obj = strResult;
                                    } else {
                                        message.what = UserInfo.CONTENT_ERROR;
                                    }
                                    handler.sendMessage(message);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    message.what = type;
                                    message.obj = strResult;
                                    handler.sendMessage(message);
                                }
                                break;
                            case UserInfo.SUCCESS_REGISTER:
                            case UserInfo.FANKUI_SUCCESS:
                                try {
                                    JSONObject jsonObject = new JSONObject(strResult);
                                    if (jsonObject.getInt("code") == 0) {
                                        message.what = UserInfo.CONTENT_ERROR;
                                        handler.sendMessage(message);
                                    } else if (jsonObject.getInt("code") == 1) {
                                        message.what = type;
                                        message.obj = strResult;
                                        handler.sendMessage(message);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                break;

                            default:
                                message.what = type;
                                message.obj = strResult;
                                handler.sendMessage(message);
                                break;
                        }

                    } else {
//                        Logger.e("试图POST:" + type, "错误");
                        Message message = new Message();
                        message.what = UserInfo.CONTENT_ERROR;
                        handler.sendMessage(message);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        } else if (!NetUtils.isConnected(context)) {
            Message message = new Message();
            message.what = UserInfo.CONTENT_ERROR;
            handler.sendMessage(message);
        } else {
            Message message = new Message();
            message.what = UserInfo.ONLY_WIFI_NOT_CONNET;
            handler.sendMessage(message);
        }
    }

    public static void PostToHttps(String url, List<NameValuePair> params, Handler handler, int type) {
        new Thread(() -> {
            HttpPost httpPost = new HttpPost(url);
            try {
                HttpEntity httpentity = new UrlEncodedFormEntity(params, "utf-8");
                // 请求httpRequest
                httpPost.setEntity(httpentity);
                // 取得默认的HttpClient
                DefaultHttpClient httpclient = new DefaultHttpClient();
                // 取得HttpResponse
                HttpResponse httpResponse = httpclient.execute(httpPost);
                if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    // 取得返回的字符串
                    String strResult = EntityUtils.toString(httpResponse.getEntity());
////                    Log.e(strResult, "成功获取");
                } else {
////                    Log.e("试图POST:"+type, "错误");
                    Message message = new Message();
                    message.what = UserInfo.CONTENT_ERROR;
                    handler.sendMessage(message);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * 接受返回数据
     *
     * @param url
     * @param handler
     */
    public static void GetFromHttp(String url, Handler handler) {
//        Logger.e("url", url);
        new Thread(() -> {
            HttpClient httpCient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {
                HttpResponse httpResponse = httpCient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String response = EntityUtils.toString(entity, "utf-8");
                    Log.e("HttpGet_response", response);
                    Message message = handler.obtainMessage();
                    message.obj = response;
                    message.what = UserInfo.GET_BACK;
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }).start();
    }

    public static void GetFromHttp(String url, Handler handler, String name) {
        new Thread(() -> {
            HttpClient httpCient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {
                HttpResponse httpResponse = httpCient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String response = EntityUtils.toString(entity, "utf-8");
////                    Log.e("HttpGet_response",response);
                    Bundle bundle = new Bundle();
                    bundle.putString("response", response);
                    bundle.putString("name", name);
                    Message message = new Message();
                    message.what = UserInfo.FIND_NOTE_SUCCESS;
                    message.obj = bundle;
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }).start();
    }

    public static void GetFromHttp(String url, Handler handler, int type) {
        new Thread(() -> {
            HttpClient httpCient = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(url);
            try {
                HttpResponse httpResponse = httpCient.execute(httpGet);
                if (httpResponse.getStatusLine().getStatusCode() == 200) {
                    HttpEntity entity = httpResponse.getEntity();
                    String response = EntityUtils.toString(entity, "utf-8");
//                    Logger.e("HttpGet_response", response);
                    Message message = new Message();
                    message.what = type;
                    message.obj = response;
                    handler.sendMessage(message);
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }).start();
    }

    public static void GetFromHttps(Context context, String url, Handler handler, int type) {
//        Logger.d("save tent", "");
        if ((!UserInfo.ONLY_WIFI && NetUtils.isConnected(context)) || (UserInfo.ONLY_WIFI && NetUtils.isWifi(context))) {
            new Thread(() -> {
                HttpClient httpCient = new DefaultHttpClient();
                HttpGet httpGet = new HttpGet(url);
                try {
                    HttpResponse httpResponse = httpCient.execute(httpGet);
                    if (httpResponse.getStatusLine().getStatusCode() == 200) {
                        HttpEntity entity = httpResponse.getEntity();
                        String response = EntityUtils.toString(entity, "utf-8");
//                        Logger.e("HttpGet_response", response);
                        if (response.equals("[]")) {
                            Message message = new Message();
                            message.what = UserInfo.EMPTY;
                            handler.sendMessage(message);
                        } else {
                            try {
                                JSONArray jsonArray = new JSONArray(response);
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                                    String name = jsonObject.getString("name");
                                    int id = jsonObject.getInt("fileId");
                                    String cache = jsonObject.getString("cache");
                                    HttpClient httpCientget = new DefaultHttpClient();
                                    HttpGet httpGetget = new HttpGet(UserInfo.url + UserInfo.getNote + "/" + id
                                            + "?token=" + MenuActivity.token);
                                    try {
                                        HttpResponse httpResponseget = httpCientget.execute(httpGetget);
                                        if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                            HttpEntity entityget = httpResponseget.getEntity();
                                            String responseget = EntityUtils.toString(entityget, "utf-8");
                                            Bundle bundle = new Bundle();
                                            bundle.putString("response", responseget);
                                            bundle.putInt("fileid", id);
                                            bundle.putString("name", name);
                                            bundle.putString("pic", cache);
                                            Message message = new Message();
                                            message.what = type;
                                            message.obj = bundle;
                                            handler.sendMessage(message);
                                        }
                                    } catch (Exception e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                Message message = new Message();
                                message.what = UserInfo.EMPTY;
                                handler.sendMessage(message);
                            }
                        }
                    } else {
                        Message message = new Message();
                        message.what = UserInfo.CONTENT_ERROR;
                        handler.sendMessage(message);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

            }).start();
        } else if (!NetUtils.isConnected(context)) {
            Message message = new Message();
            message.what = UserInfo.CONTENT_ERROR;
            handler.sendMessage(message);
        } else {
            Message message = new Message();
            message.what = UserInfo.ONLY_WIFI_NOT_CONNET;
            handler.sendMessage(message);
        }
    }

}