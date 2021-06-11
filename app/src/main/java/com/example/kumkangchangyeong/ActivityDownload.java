package com.example.kumkangchangyeong;


import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Handler;
import androidx.appcompat.app.AppCompatActivity;

public class ActivityDownload extends AppCompatActivity {


    DownloadManager mDm;
    long mId = 0;
    Handler mHandler;
    ProgressDialog mProgressDialog;
    String serverVersion;
    String downloadUrl;


}
