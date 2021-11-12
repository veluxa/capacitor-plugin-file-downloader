package com.veluxa.plugins;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import android.util.Log;

@NativePlugin
public class FileDownloader extends Plugin{

  private final int PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 10001;
  //下载器
  private DownloadManager downloadManager;
  private Context mContext;
  //下载的ID
  private long downloadId;
  private String pathstr;

  PluginCall _call;

  private final String outputDir = "Click";

  @PluginMethod
  public void download(PluginCall call) {
    _call = call;
    mContext = getContext();
    requestPermissions();

    call.save();
    downloadFile(call);
  }

  //获取权限
  private void requestPermissions() {

    if (!this.hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) && !this.hasPermission(Manifest.permission.READ_EXTERNAL_STORAGE)) {
      this.pluginRequestPermissions(
        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},
        PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE);
    }
  }

  //下载文件
  private void downloadFile(final PluginCall call) {
    String url = call.getString("url","");
    String filename = call.getString("filename","");
    OutputStream outputStream;

    HttpURLConnection connection = null;

    try {
      connection = (HttpURLConnection) new URL(url).openConnection();
      connection.setRequestMethod("GET");
      connection.setConnectTimeout(8000);
      connection.setReadTimeout(8000);

      InputStream inputStream = connection.getInputStream();
      byte[] buffer = new byte[1024];

      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // To Download File for Android 10 and above
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, filename);

        String desDirectory = Environment.DIRECTORY_DOWNLOADS;
        // If you want to create custom directory inside Download directory only
        desDirectory = desDirectory + File.separator + outputDir;
        File desFile = new File(desDirectory);
        if (!desFile.exists()) desFile.mkdir();
        // final output path
        pathstr = desDirectory + File.separator + filename; // pathstr is outputPath

        values.put(MediaStore.MediaColumns.RELATIVE_PATH, desDirectory);
        Uri uri = mContext.getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);
        if (uri != null) {
          outputStream = mContext.getContentResolver().openOutputStream(uri);
          if (outputStream != null) {
            BufferedOutputStream bos = new BufferedOutputStream(outputStream);
            int bytes;
            while ((bytes = bis.read(buffer)) != -1) {
              bos.write(buffer, 0, bytes);
              bos.flush();
            }
            bos.close();
          }
        }
        bis.close();

      } else {

        // first we create app name folder direct to the root directory
        String destURL = Environment.getExternalStorageDirectory().getPath() + File.separator + outputDir;
        File desFile = new File(destURL);
        if (!desFile.exists()) {
          desFile.mkdir();
        }

        // once the app name directory created we create download directory inside app directory
        destURL = destURL + File.separator + Environment.DIRECTORY_DOWNLOADS;
        desFile = new File(destURL);
        if (!desFile.exists()) {
          desFile.mkdir();
        }

        destURL = destURL + File.separator + filename;

        // final output path
        pathstr = destURL;

        outputStream = new FileOutputStream(destURL);
        BufferedOutputStream bos = new BufferedOutputStream(outputStream);
        int bytes;
        while ((bytes = inputStream.read(buffer)) != -1) {
          outputStream.write(buffer, 0, bytes);
        }

        outputStream.flush();
        outputStream.close();
        inputStream.close();
      }

      JSObject ret = new JSObject();
      ret.put("path", pathstr);
      _call.success(ret);
    } catch (Exception e) {
      _call.reject(e.getMessage());
    }

  }
}
