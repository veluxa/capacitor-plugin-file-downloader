package com.veluxa.plugins;

import com.getcapacitor.JSObject;
import com.getcapacitor.NativePlugin;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;

import android.Manifest;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;

import java.io.File;

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

        //创建下载任务
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        //移动网络情况下是否允许漫游
        request.setAllowedOverRoaming(false);
        //在通知栏中显示，默认就是显示的
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE);
        request.setTitle("文件下载器");
        request.setDescription(filename + "下载中...");
        request.setVisibleInDownloadsUi(true);

        //设置下载的路径
        File file = new File(mContext.getExternalFilesDir(""), filename);
        request.setDestinationUri(Uri.fromFile(file));
        pathstr = file.getAbsolutePath();

        //获取DownloadManager
        if (downloadManager == null)
            downloadManager = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
        //将下载请求加入下载队列，加入下载队列后会给该任务返回一个long型的id，通过该id可以取消任务，重启任务、获取下载的文件等等
        if (downloadManager != null) {
            downloadId = downloadManager.enqueue(request);
        }

        //注册广播接收者，监听下载状态
        mContext.registerReceiver(receiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    //广播监听下载的各个状态
    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            checkStatus();
        }
    };

    //检查下载状态
    private void checkStatus() {
        DownloadManager.Query query = new DownloadManager.Query();
        //通过下载的id查找
        query.setFilterById(downloadId);
        Cursor cursor = downloadManager.query(query);
        if (cursor.moveToFirst()) {
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            switch (status) {
                //下载暂停
                case DownloadManager.STATUS_PAUSED:
                    break;
                //下载延迟
                case DownloadManager.STATUS_PENDING:
                    break;
                //正在下载
                case DownloadManager.STATUS_RUNNING:
                    break;
                //下载完成
                case DownloadManager.STATUS_SUCCESSFUL:
                    //下载完成
                    cursor.close();
                    JSObject ret = new JSObject();
                    ret.put("path", pathstr);
                    _call.success(ret);
                    break;
                //下载失败
                case DownloadManager.STATUS_FAILED:
                    cursor.close();
                    mContext.unregisterReceiver(receiver);
                    _call.reject("下载失败");
                    break;
            }
        }
    }
}
