package com.umi.twocamera;

import android.Manifest;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.umi.twocamera.app.Const;
import com.umi.twocamera.bean.Record;
import com.umi.twocamera.bean.User;
import com.umi.twocamera.db.RecordDaoUtils;
import com.umi.twocamera.db.UserDaoUtils;
import com.umi.twocamera.receiver.PermissionInterface;
import com.umi.twocamera.utils.FileAccess;
import com.umi.twocamera.utils.PermissionHelper;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity2 extends AppCompatActivity implements PermissionInterface {
    ImageView iv_parent;
    SurfaceView surfaceview1, surfaceview2;
    SurfaceHolder surfaceholder1, surfaceholder2;
    String TAG = "MainActivity";
    private Camera camera1 = null, camera2;
    Camera.Parameters parameters;
    private NetworkInterface networkInterface;
    private PermissionHelper mPermissionHelper;
    private UserDaoUtils mUserDaoUtils;
    private RecordDaoUtils mRecordDaoUtils;
    private HashMap<String,User> user_hm=new HashMap<>();
    private User user=null;
    private View.OnClickListener listener=new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.iv_parent:
                    String cardno="123";
                    user=user_hm.get(cardno);
                    if(user!=null)
                    camera1.takePicture(null, null, picture);
                    break;
            }
        }
    };
    private Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    String path= Const.photoPath+ new Date().getTime()+".jpg";
                    if(!new File(Const.photoPath).exists())new File(Const.photoPath).mkdirs();
//                    FileUtils.getFileFromBytes(data,path);
                    if(data!=null)
                        FileAccess.writeFileSdcard(Const.photoPath,path, data);
                    Record record=new Record(null,user.getName(),user.getCardId(),
                            user.getUserId(),path,new Date().getTime(),user.getDepartment(),0,0,0);
                    boolean b=mRecordDaoUtils.insertRecord(record);
                    if(b){
                        Toast.makeText(MainActivity2.this,"刷卡成功",Toast.LENGTH_SHORT).show();
                    }else{
                        Toast.makeText(MainActivity2.this,"刷卡失败",Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    private byte[] data=null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        mUserDaoUtils=new UserDaoUtils(this);
        mRecordDaoUtils=new RecordDaoUtils(this);
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
    }
    private void initView(){
        iv_parent=findViewById(R.id.iv_parent);
        surfaceview1 = (SurfaceView) findViewById(R.id.surfaceview1);
        surfaceview2 = (SurfaceView) findViewById(R.id.surfaceview2);
        surfaceholder1 = surfaceview1.getHolder();
        surfaceholder1.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceholder1.addCallback(new surfaceholderCallbackBack());

        surfaceholder2 = surfaceview2.getHolder();
        surfaceholder2.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
//        surfaceholder2.addCallback(new surfaceholderCallbackFont());
        List<User> list=mUserDaoUtils.queryAllUser();
        for (int i=0;i<list.size();i++){
            User user=list.get(i);
            user_hm.put(user.getCardId(),user);
        }
        iv_parent.setOnClickListener(listener);
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
                Manifest.permission.CAMERA,
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
    /**
     * 后置摄像头回调
     */
    class surfaceholderCallbackBack implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // 获取camera对象
            int cameraCount = Camera.getNumberOfCameras();
            if (cameraCount > 0) {
                camera1 = Camera.open(0);
                try {
                    // 设置预览监听
                    camera1.setPreviewDisplay(holder);
                    Camera.Parameters parameters = camera1.getParameters();

                    if (MainActivity2.this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                        parameters.set("orientation", "portrait");
                        camera1.setDisplayOrientation(90);
                        parameters.setRotation(90);
                    } else {
                        parameters.set("orientation", "landscape");
                        camera1.setDisplayOrientation(0);
                        parameters.setRotation(0);
                    }
                    camera1.setParameters(parameters);
                    // 启动摄像头预览
                    camera1.startPreview();
                    camera1.setPreviewCallback(new setPreVrewCallBack());
                    System.out.println("camera.startpreview");

                } catch (IOException e) {
                    e.printStackTrace();
                    camera1.release();
                    System.out.println("camera.release");
                }
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera1.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        initCamera();// 实现相机的参数初始化
                        camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    }
                }
            });

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        // 相机参数的初始化设置
        private void initCamera() {
            parameters = camera1.getParameters();
            parameters.setPictureFormat(PixelFormat.JPEG);
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
            setDispaly(parameters, camera1);
            camera1.setParameters(parameters);
            camera1.startPreview();
            camera1.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
        }

        // 控制图像的正确显示方向
        private void setDispaly(Camera.Parameters parameters, Camera camera) {
            if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
                setDisplayOrientation(camera, 90);
            } else {
                parameters.setRotation(90);
            }

        }

        // 实现的图像的正确显示
        private void setDisplayOrientation(Camera camera, int i) {
            Method downPolymorphic;
            try {
                downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
                if (downPolymorphic != null) {
                    downPolymorphic.invoke(camera, new Object[]{i});
                }
            } catch (Exception e) {
                Log.e("Came_e", "图像出错");
            }
        }
    }

    class surfaceholderCallbackFont implements SurfaceHolder.Callback {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            // 获取camera对象
            int cameraCount = Camera.getNumberOfCameras();
            if (cameraCount >1) {
                camera2 = Camera.open(1);
            }
            try {
                // 设置预览监听
                camera2.setPreviewDisplay(holder);
                Camera.Parameters parameters = camera2.getParameters();

                if (MainActivity2.this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                    parameters.set("orientation", "portrait");
                    camera2.setDisplayOrientation(90);
                    parameters.setRotation(90);
                } else {
                    parameters.set("orientation", "landscape");
                    camera2.setDisplayOrientation(0);
                    parameters.setRotation(0);
                }
                camera2.setParameters(parameters);
                // 启动摄像头预览
                camera2.startPreview();
                camera2.setPreviewCallback(new setPreVrewCallBack2());
                System.out.println("camera.startpreview");

            } catch (IOException e) {
                e.printStackTrace();
                camera2.release();
                System.out.println("camera.release");
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            camera2.autoFocus(new Camera.AutoFocusCallback() {
                @Override
                public void onAutoFocus(boolean success, Camera camera) {
                    if (success) {
                        parameters = camera2.getParameters();
                        parameters.setPictureFormat(PixelFormat.JPEG);
                        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);// 1连续对焦
                        setDispaly(parameters, camera2);
                        camera2.setParameters(parameters);
                        camera2.startPreview();
                        camera2.cancelAutoFocus();// 2如果要实现连续的自动对焦，这一句必须加上
                        camera.cancelAutoFocus();// 只有加上了这一句，才会自动对焦。
                    }
                }
            });

        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {

        }

        // 控制图像的正确显示方向
        private void setDispaly(Camera.Parameters parameters, Camera camera) {
            if (Integer.parseInt(Build.VERSION.SDK) >= 8) {
                setDisplayOrientation(camera, 90);
            } else {
                parameters.setRotation(90);
            }

        }

        // 实现的图像的正确显示
        private void setDisplayOrientation(Camera camera, int i) {
            Method downPolymorphic;
            try {
                downPolymorphic = camera.getClass().getMethod("setDisplayOrientation", new Class[]{int.class});
                if (downPolymorphic != null) {
                    downPolymorphic.invoke(camera, new Object[]{i});
                }
            } catch (Exception e) {
                Log.e("Came_e", "图像出错");
            }
        }
    }
    class setPreVrewCallBack implements Camera.PreviewCallback{

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
//                LogUtils.i("TAG",data.length+"");
        }
    }
    class setPreVrewCallBack2 implements Camera.PreviewCallback{

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {

        }
    }
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == event.KEYCODE_BACK && event.getRepeatCount() == 0) {
            startActivity(new Intent(MainActivity2.this, MenuActivity.class));
            destroy();
            // 退出时，请求杀死进程
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onKeyDown(keyCode, event);
    }
    public void destroy(){
        if (camera1 != null) {
            camera1.setPreviewCallback(null) ;
            camera1.stopPreview();
            camera1.release();
            camera1 = null;
        }
    }
    /**
     * 创建png图片回调数据对象
     */

    Camera.PictureCallback picture = new Camera.PictureCallback() {
        @Override
        public void onPictureTaken(final byte[] data2, final Camera camera) {
            ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
            cachedThreadPool.execute(new Runnable() {
                public void run() {
                    android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);
                    camera.startPreview();
                    data=data2;
                    handler.sendEmptyMessage(0);
					/*String picture_base64 = "";
					try {
						if (data != null) {
							picture_base64 = HelpUtil
									.Bitmap2StrByBase64(HelpUtil.addDate(HelpUtil.byteToBitmap(data)));
						} else {
							Log.d(TAG,
									"Error creating media file, check storage permissions: ");
						}
						// TODO 刷卡数据状态待定
						saveKQ(picture_base64, data);
					}catch (Exception e) {
						Message msg = new Message();
						msg.what = 1;
						msg.obj = "拍照："+e.getMessage().toString();
						handler.sendMessage(msg);
						System.gc();
					}*/
                }
            });
        }
    };
}