package com.umi.twocamera;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.umi.twocamera.receiver.PermissionInterface;
import com.umi.twocamera.utils.PermissionHelper;

import java.io.IOException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;

public class MenuActivity extends AppCompatActivity implements PermissionInterface {

    private LinearLayout ll_user;
    private LinearLayout ll_data;
    private LinearLayout ll_search;
    private LinearLayout ll_set;
    private LinearLayout ll_export;
    private LinearLayout ll_system;
    private LinearLayout ll_face;
    private TextView tv_title_name;
    private TextView tv_back;
    String TAG = "MainActivity";
    private NetworkInterface networkInterface;
    private PermissionHelper mPermissionHelper;
    private View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.ll_user:
                    MenuActivity.this.startActivity(new Intent(MenuActivity.this,UserManagerActivity.class));
                    break;
                case R.id.ll_search:
                    MenuActivity.this.startActivity(new Intent(MenuActivity.this,RecoSeachActivity.class));
                    break;
                case R.id.ll_data:
                    MenuActivity.this.startActivity(new Intent(MenuActivity.this,DataManagerActivity.class));
                    break;
                case R.id.ll_set:
                    MenuActivity.this.startActivity(new Intent(MenuActivity.this,SetActivity.class));
                    break;
                case R.id.ll_system:
                    MenuActivity.this.startActivity(new Intent(MenuActivity.this,DeviceInfoActivity.class));
                    break;
                case R.id.ll_export:
                    MenuActivity.this.startActivity(new Intent(MenuActivity.this,ExportActivity.class));
                    break;
                case R.id.ll_face:
                    MenuActivity.this.startActivity(new Intent(MenuActivity.this,MainActivity.class));
                    break;
                case R.id.tv_back:
                    finish();
                    break;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);
//        mPermissionHelper = new PermissionHelper(this, this);
//        mPermissionHelper.requestPermissions();
        initView();
        initListener();
    }
    private void initView(){
        ll_user = (LinearLayout) findViewById(R.id.ll_user);
        ll_search = (LinearLayout) findViewById(R.id.ll_search);
        ll_data = (LinearLayout) findViewById(R.id.ll_data);
        ll_export = (LinearLayout) findViewById(R.id.ll_export);
        ll_system = (LinearLayout) findViewById(R.id.ll_system);
        ll_set = (LinearLayout) findViewById(R.id.ll_set);
        ll_face = (LinearLayout) findViewById(R.id.ll_face);
        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_back.setVisibility(View.VISIBLE);
        tv_title_name = (TextView) findViewById(R.id.tv_title_name);
        tv_title_name.setText("菜单");
        tv_title_name.setVisibility(View.VISIBLE);
    }
    private void initListener(){
        ll_user.setOnClickListener(listener);
        ll_search.setOnClickListener(listener);
        ll_data.setOnClickListener(listener);
        ll_export.setOnClickListener(listener);
        ll_system.setOnClickListener(listener);
        ll_set.setOnClickListener(listener);
        tv_back.setOnClickListener(listener);
        ll_face.setOnClickListener(listener);
    }
    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        super.onDestroy();
    }

    @Override
    public int getPermissionsRequestCode() {
        //设置权限请求requestCode，只有不跟onRequestPermissionsResult方法中的其他请求码冲突即可。
        return 10000;
    }

    @Override
    public String[] getPermissions() {
        //设置该界面所需的全部权限
        return new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_PHONE_STATE
        };
    }

    @Override
    public void requestPermissionsSuccess() {
        initView();
    }

    @Override
    public void requestPermissionsFail() {
        initView();
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(mPermissionHelper.requestPermissionsResult(requestCode, permissions, grantResults)){
            //权限请求结果，并已经处理了该回调
            return;
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}