package com.umi.twocamera;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.umi.twocamera.app.Const;
import com.umi.twocamera.bean.User;
import com.umi.twocamera.db.UserDaoUtils;
import com.umi.twocamera.receiver.PermissionInterface;
import com.umi.twocamera.utils.FileUtils;
import com.umi.twocamera.utils.PermissionHelper;
import com.umi.twocamera.utils.PrefsUtils;

import java.net.NetworkInterface;
import java.util.Date;

public class SetActivity extends AppCompatActivity implements PermissionInterface {

    private TextView tv_title_name;
    private TextView tv_back;
    private TextView tv_ok;
    private TextView tv_yanzheng;
    String TAG = "MainActivity";
    private NetworkInterface networkInterface;
    private PermissionHelper mPermissionHelper;
    private UserDaoUtils mUserDaoUtils;
    private View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_yanzheng:
                    showList(tv_yanzheng);
                    break;
                case R.id.tv_back:
                        finish();
                    break;
                case R.id.tv_ok:
                    String yanzheng=tv_yanzheng.getText().toString().trim();
                    PrefsUtils.writePrefs(SetActivity.this, Const.yanzheng_type,yanzheng);
                    PrefsUtils.writeBooleanPrefs(SetActivity.this, Const.fanqianhui,switch1.isChecked());
                    PrefsUtils.writeBooleanPrefs(SetActivity.this, Const.chaoci,switch2.isChecked());
                    PrefsUtils.writePrefs(SetActivity.this, Const.chaoci_num,et_num.getText().toString());
                    Toast.makeText(SetActivity.this,"设置成功",Toast.LENGTH_SHORT).show();
                    finish();
                   /* if(TextUtils.isEmpty(name)){
                        Toast.makeText(SetActivity.this,"请输入姓名",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(b){
                        Toast.makeText(SetActivity.this,"操作成功",Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(SetActivity.this,"操作失败",Toast.LENGTH_SHORT).show();
                    }*/
                    break;
                case R.id.iv_headpic:
                    startActivityForResult(new Intent(SetActivity.this,CollectActivity.class),100);
                    break;
            }
        }
    };
    private AlertDialog alertDialog1;
    private Switch switch1;
    private Switch switch2;
    private EditText et_num;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set);
//        mPermissionHelper = new PermissionHelper(this, this);
//        mPermissionHelper.requestPermissions();
        initView();
        initData();
        initListener();
        mUserDaoUtils=new UserDaoUtils(this);
    }
    private void initData() {
        Intent intent=getIntent();
        if(intent.getExtras()!=null){
        }
        tv_title_name.setText("门禁设置");
        tv_yanzheng.setText(PrefsUtils.readPrefs(SetActivity.this, Const.yanzheng_type));
        switch1.setChecked(PrefsUtils.readBooleanPrefs(SetActivity.this, Const.fanqianhui));
        switch2.setChecked(PrefsUtils.readBooleanPrefs(SetActivity.this, Const.chaoci));
        String num=PrefsUtils.readPrefs(SetActivity.this, Const.chaoci_num,et_num.getText().toString());
        if(!TextUtils.isEmpty(num))et_num.setText(num);
    }

    private void initView(){
        et_num = (EditText) findViewById(R.id.et_num);
        switch1 = (Switch) findViewById(R.id.switch1);
        switch2 = (Switch) findViewById(R.id.switch2);
        tv_yanzheng = (TextView) findViewById(R.id.tv_yanzheng);
        tv_ok = (TextView) findViewById(R.id.tv_ok);
        tv_ok.setVisibility(View.VISIBLE);
        tv_back = (TextView) findViewById(R.id.tv_back);
        tv_back.setVisibility(View.VISIBLE);
        tv_title_name = (TextView) findViewById(R.id.tv_title_name);
        tv_title_name.setVisibility(View.VISIBLE);
    }
    private void initListener(){
        tv_ok.setOnClickListener(listener);
        tv_yanzheng.setOnClickListener(listener);
        tv_back.setOnClickListener(listener);
    }
    public void onToggleClicked(View view) {
        /*
         * 强转为Switch类型的
         */
        boolean isChecked = ((Switch) view).isChecked();
        if (isChecked == true) {
            Toast.makeText(SetActivity.this, "打开", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(SetActivity.this, "关闭", Toast.LENGTH_SHORT).show();
        }
    }
    public void showList(final View view){
        final String[] items = {"人脸验证", "刷卡验证", "指纹验证"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("验证方式");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(SetActivity.this, items[i], Toast.LENGTH_SHORT).show();
                ((TextView)view).setText(items[i]);
                alertDialog1.dismiss();
            }
        });
        alertDialog1 = alertBuilder.create();
        alertDialog1.show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100) {
        }
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