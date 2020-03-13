package com.umi.twocamera;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.media.Image;
import android.media.ImageReader;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Surface;
import android.view.TextureView;
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
import java.net.NetworkInterface;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity_tmp extends AppCompatActivity implements PermissionInterface {
    ImageView iv_parent;
    String TAG = "MainActivity";
    private NetworkInterface networkInterface;
    private PermissionHelper mPermissionHelper;
    private UserDaoUtils mUserDaoUtils;
    private RecordDaoUtils mRecordDaoUtils;
    private HashMap<String, User> user_hm = new HashMap<>();
    private User user = null;
    private View.OnClickListener listener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.iv_parent:
                    String cardno = "123";
                    user = user_hm.get(cardno);
                    if (user != null)
//                    camera1.takePicture(null, null, picture);
                        break;
            }
        }
    };
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    String path = Const.photoPath + new Date().getTime() + ".jpg";
                    if (!new File(Const.photoPath).exists()) new File(Const.photoPath).mkdirs();
//                    FileUtils.getFileFromBytes(data,path);
                    if (data != null)
                        FileAccess.writeFileSdcard(Const.photoPath, path, data);
                    Record record = new Record(null, user.getName(), user.getCardId(),
                            user.getUserId(), path, new Date().getTime(), user.getDepartment(), 0, 0, 0);
                    boolean b = mRecordDaoUtils.insertRecord(record);
                    if (b) {
                        Toast.makeText(MainActivity_tmp.this, "刷卡成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity_tmp.this, "刷卡失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    private byte[] data = null;
    private TextureView mTextureView;
    private CameraCaptureSession mCameraCaptureSession;
    private CameraDevice mCameraDevice;
    private Surface mPreviewSurface;

    private TextureView mTextureView2;
    private CameraCaptureSession mCameraCaptureSession2;
    private CameraDevice mCameraDevice2;
    private Surface mPreviewSurface2;
    private HandlerThread mBackgroundThread_front;
    private Handler mHandler_front;
    private HandlerThread mBackgroundThread_back;
    private Handler mHandler_back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUserDaoUtils = new UserDaoUtils(this);
        mRecordDaoUtils = new RecordDaoUtils(this);
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
    }
    private void startBackgroundThread() {
        mBackgroundThread_front = new HandlerThread("Camera_front");
        mBackgroundThread_front.start();
        mHandler_front = new Handler(mBackgroundThread_front.getLooper());

        mBackgroundThread_back=new HandlerThread("Camera_back");
        mBackgroundThread_back.start();
        mHandler_back=new Handler(mBackgroundThread_back.getLooper());
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void initView() {
        iv_parent = findViewById(R.id.iv_parent);
        List<User> list = mUserDaoUtils.queryAllUser();
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            user_hm.put(user.getCardId(), user);
        }
        iv_parent.setOnClickListener(listener);

        startBackgroundThread();
        //预览用的surface
        mTextureView = (TextureView) this.findViewById(R.id.texture_preview);
        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1, int arg2) {
                // TODO 自动生成的方法存根
                mPreviewSurface = new Surface(arg0);
                CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                try {
                    if (ActivityCompat.checkSelfPermission(MainActivity_tmp.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    manager.openCamera("0", new CameraDevice.StateCallback() {
                        @Override
                        public void onOpened(CameraDevice arg0) {
                            // TODO 自动生成的方法存根s
                            mCameraDevice = arg0;
                            try {
                                mCameraDevice.createCaptureSession(Arrays.asList(mPreviewSurface), new CameraCaptureSession.StateCallback() {

                                    @Override
                                    public void onConfigured(CameraCaptureSession arg0) {
                                        // TODO 自动生成的方法存根
                                        mCameraCaptureSession = arg0;
                                        try {
                                            CaptureRequest.Builder builder;
                                            builder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                            builder.addTarget(mPreviewSurface);
                                            mCameraCaptureSession.setRepeatingRequest(builder.build(), null, null);
                                        } catch (CameraAccessException e1) {
                                            // TODO 自动生成的 catch 块
                                            e1.printStackTrace();
                                        }


                                    }

                                    @Override
                                    public void onConfigureFailed(CameraCaptureSession arg0) {
                                        // TODO 自动生成的方法存根

                                    }
                                }, null);
                            } catch (CameraAccessException e) {
                                // TODO 自动生成的 catch 块
                                e.printStackTrace();
                            }

                        }

                        @Override
                        public void onError(CameraDevice arg0, int arg1) {
                            // TODO 自动生成的方法存根

                        }

                        @Override
                        public void onDisconnected(CameraDevice arg0) {
                            // TODO 自动生成的方法存根

                        }
                    }, mHandler_front);
                } catch (CameraAccessException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
                // TODO 自动生成的方法存根
                return false;
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1, int arg2) {
                // TODO 自动生成的方法存根

            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
                // TODO 自动生成的方法存根

            }

        });

        //预览用的surface
        mTextureView2 = (TextureView) this.findViewById(R.id.texture_preview2);
        mTextureView2.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {

            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture arg0, int arg1, int arg2) {
                // TODO 自动生成的方法存根
                mPreviewSurface2 = new Surface(arg0);
                CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
                try {
                    if (ActivityCompat.checkSelfPermission(MainActivity_tmp.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    manager.openCamera("1", new CameraDevice.StateCallback() {

                        @Override
                        public void onOpened(CameraDevice arg0) {
                            // TODO 自动生成的方法存根s
                            mCameraDevice2 = arg0;
                            try {
                                mCameraDevice2.createCaptureSession(Arrays.asList(mPreviewSurface2), new CameraCaptureSession.StateCallback() {

                                    @Override
                                    public void onConfigured(CameraCaptureSession arg0) {
                                        // TODO 自动生成的方法存根
                                        mCameraCaptureSession2 = arg0;
                                        try {
                                            CaptureRequest.Builder builder;
                                            builder = mCameraDevice2.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                            builder.addTarget(mPreviewSurface2);
                                            mCameraCaptureSession2.setRepeatingRequest(builder.build(), null, null);
                                        } catch (CameraAccessException e1) {
                                            // TODO 自动生成的 catch 块
                                            e1.printStackTrace();
                                        }
                                    }
                                    @Override
                                    public void onConfigureFailed(CameraCaptureSession arg0) {
                                        // TODO 自动生成的方法存根
                                    }
                                }, null);
                            } catch (CameraAccessException e) {
                                // TODO 自动生成的 catch 块
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onError(CameraDevice arg0, int arg1) {
                            // TODO 自动生成的方法存根
                        }

                        @Override
                        public void onDisconnected(CameraDevice arg0) {
                            // TODO 自动生成的方法存根
                        }
                    }, mHandler_back);
                } catch (CameraAccessException e) {
                    // TODO 自动生成的 catch 块
                    e.printStackTrace();
                }
            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture arg0) {
                // TODO 自动生成的方法存根
                return false;
            }
            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture arg0, int arg1, int arg2) {
                // TODO 自动生成的方法存根

            }
            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture arg0) {
                // TODO 自动生成的方法存根

            }

        });
        /*ImageReader imageReader = ImageReader.newInstance(width, height, ImageFormat.YV12, 1);//预览数据流最好用非JPEG
        imageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Image image = reader.acquireLatestImage();//最后一帧
                //do something
                int len = image.getPlanes().length;
                byte[][] bytes = new byte[len][];
                int count = 0;
                for (int i = 0; i < len; i++) {
                    ByteBuffer buffer = image.getPlanes()[i].getBuffer();
                    int remaining = buffer.remaining();
                    byte[] data = new byte[remaining];
                    byte[] _data = new byte[remaining];
                    buffer.get(data);
                    System.arraycopy(data, 0, _data, 0, remaining);
                    bytes[i] = _data;
                    count += remaining;
                }
                //数据流都在 bytes[][] 中，关于有几个plane，可以看查看 ImageUtils.getNumPlanesForFormat(int format);
                // ...
                image.close();//一定要关闭
            }
        }, handler);*/
    }

    @Override
    protected void onDestroy() {
        Log.e(TAG, "onDestroy");
        destroy();
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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void requestPermissionsSuccess() {
        initView();
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == event.KEYCODE_BACK && event.getRepeatCount() == 0) {
            startActivity(new Intent(MainActivity_tmp.this, MenuActivity.class));
            destroy();
            // 退出时，请求杀死进程
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onKeyDown(keyCode, event);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void destroy(){
        if(mCameraDevice!=null){
            mCameraDevice.close();
            mCameraDevice=null;
        }
        if(mCameraDevice2!=null){
            mCameraDevice2.close();
            mCameraDevice2=null;
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
                }
            });
        }
    };
}