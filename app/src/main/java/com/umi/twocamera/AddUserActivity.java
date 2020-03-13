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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.umi.twocamera.bean.User;
import com.umi.twocamera.db.UserDaoUtils;
import com.umi.twocamera.receiver.PermissionInterface;
import com.umi.twocamera.utils.FileUtils;
import com.umi.twocamera.utils.HelpUtil;
import com.umi.twocamera.utils.PermissionHelper;

import java.net.NetworkInterface;
import java.util.Date;

public class AddUserActivity extends AppCompatActivity implements PermissionInterface {

    private TextView tv_title_name;
    private TextView tv_back;
    private TextView tv_ok;
    private TextView tv_yanzheng;
    private TextView tv_userid;
    private EditText et_user_name;
    private EditText et_cardid;
    private EditText et_department;
    private ImageView iv_headpic;
    private String headPicPath="";
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
                    String userid=tv_userid.getText().toString().trim();
                    String name=et_user_name.getText().toString().trim();
                    String cardId=et_cardid.getText().toString().trim();
                    String department=et_department.getText().toString().trim();
                    String yanzheng=tv_yanzheng.getText().toString().trim();
                    if(TextUtils.isEmpty(name)){
                        Toast.makeText(AddUserActivity.this,"请输入姓名",Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(user==null){
                        user=new User();
                    }
                    user.setCardId(cardId);
                    user.setCheckType(yanzheng);
                    user.setDepartment(department);
                    user.setFace(headPicPath);
                    user.setHeadPhoto(headPicPath);
                    user.setName(name);
                    user.setUserId(userid);
                    user.setUserType("");
                    boolean b=mUserDaoUtils.insertUser(user);
                    if(b){
                        Toast.makeText(AddUserActivity.this,"操作成功",Toast.LENGTH_SHORT).show();
                        finish();
                    }else{
                        Toast.makeText(AddUserActivity.this,"操作失败",Toast.LENGTH_SHORT).show();
                    }
                    break;
                case R.id.iv_headpic:
                    startActivityForResult(new Intent(AddUserActivity.this,CollectActivity.class),100);
                    break;
            }
        }
    };
    private AlertDialog alertDialog1;
    private User user=null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_add);
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
            user= (User) intent.getExtras().get("User");
            tv_userid.setText(user.getUserId());
            et_user_name.setText(user.getName());
            tv_yanzheng.setText(user.getCheckType());
            et_cardid.setText(user.getCardId());
            et_department.setText(user.getDepartment());
            Bitmap b=FileUtils.getLoacalBitmap(user.getHeadPhoto());
            if(b!=null)
            iv_headpic.setImageBitmap(b);
        }
        if(user!=null){
            tv_title_name.setText("修改用户");
        }else{
            tv_title_name.setText("添加用户");
        }
    }

    private void initView(){
        iv_headpic = (ImageView) findViewById(R.id.iv_headpic);
        tv_yanzheng = (TextView) findViewById(R.id.tv_yanzheng);
        tv_userid = (TextView) findViewById(R.id.tv_userid);
        tv_userid.setText(new Date().getTime()+"");
        et_user_name = (EditText) findViewById(R.id.et_user_name);
        et_cardid = (EditText) findViewById(R.id.et_cardid);
        et_department = (EditText) findViewById(R.id.et_department);
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
        iv_headpic.setOnClickListener(listener);
        tv_back.setOnClickListener(listener);
    }
    public void showList(final View view){
        final String[] items = {"人脸验证", "刷卡验证", "指纹验证"};
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(this);
        alertBuilder.setTitle("验证方式");
        alertBuilder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(AddUserActivity.this, items[i], Toast.LENGTH_SHORT).show();
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
            if (data != null && data.getExtras() != null){
                headPicPath = data.getExtras().getString("path");
            Bitmap b = FileUtils.getLoacalBitmap(headPicPath);
            if (b != null)
                iv_headpic.setImageBitmap(b);
        }
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