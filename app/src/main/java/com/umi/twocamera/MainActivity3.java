package com.umi.twocamera;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
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
import android.util.Size;
import android.util.SparseIntArray;
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
import com.umi.twocamera.utils.LogUtils;
import com.umi.twocamera.utils.PermissionHelper;
import com.umi.twocamera.view.AutoFitTextureView;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class MainActivity3 extends AppCompatActivity implements PermissionInterface {
    ImageView iv_parent;
    String TAG = "MainActivity3";
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
                        Toast.makeText(MainActivity3.this, "刷卡成功", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(MainActivity3.this, "刷卡失败", Toast.LENGTH_SHORT).show();
                    }
                    break;
            }
        }
    };
    private byte[] data = null;
    private AutoFitTextureView texture_preview;
    private AutoFitTextureView texture_preview2;
    private HandlerThread mBackgroundThread_front;
    private HandlerThread mBackgroundThread_back;
    private Integer mSensorOrientation;
    private Handler mHandler_front;
    private Handler mHandler_back;
    private CameraDevice mCameraDevice;
    private Size mPreviewSize;
    private CameraCaptureSession mPreviewSession;
    private CaptureRequest.Builder mPreviewBuilder;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);//使用信号量 Semaphore 进行多线程任务调度
    private Semaphore mCameraOpenCloseLock_front = new Semaphore(1);//使用信号量 Semaphore 进行多线程任务调度

    private static final SparseIntArray ORIENTATIONS = new SparseIntArray();
    static
    {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main3);
        mUserDaoUtils = new UserDaoUtils(this);
        mRecordDaoUtils = new RecordDaoUtils(this);
        mPermissionHelper = new PermissionHelper(this, this);
        mPermissionHelper.requestPermissions();
    }

    private void initView() {
        iv_parent = findViewById(R.id.iv_parent);
        List<User> list = mUserDaoUtils.queryAllUser();
        for (int i = 0; i < list.size(); i++) {
            User user = list.get(i);
            user_hm.put(user.getCardId(), user);
        }
        iv_parent.setOnClickListener(listener);
        texture_preview=findViewById(R.id.texture_preview);
        texture_preview2=findViewById(R.id.texture_preview2);
    }
    @Override
    public void onResume() {
        super.onResume();

        if (texture_preview.isAvailable()) {
            openCamera(false,texture_preview.getWidth(), texture_preview.getHeight());
        } else {
            texture_preview.setSurfaceTextureListener(mFrontTextureListener);
        }
        if (texture_preview2.isAvailable()) {
            openCamera(true,texture_preview2.getWidth(), texture_preview2.getHeight());
        } else {
            texture_preview2.setSurfaceTextureListener(mBackTextureListener);
        }
        startBackgroundThread();
    }
    private void startBackgroundThread() {
        mBackgroundThread_front = new HandlerThread("Camera_front");
        mBackgroundThread_front.start();
        mHandler_front = new Handler(mBackgroundThread_front.getLooper());

        mBackgroundThread_back=new HandlerThread("Camera_back");
        mBackgroundThread_back.start();
        mHandler_back=new Handler(mBackgroundThread_back.getLooper());
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void openCamera(boolean isBack, int width, int height) {
        if (ActivityCompat.checkSelfPermission(MainActivity3.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
//            return;
        }
        final Activity activity = MainActivity3.this;
        if (null == activity || activity.isFinishing()) {
            return;
        }
        CameraManager manager = (CameraManager) activity.getSystemService(Context.CAMERA_SERVICE);
        try {
//            L.d("tryAcquire");
            if (!getSemaphore(isBack).tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                throw new RuntimeException("Time out waiting to lock camera opening.");
            }
            String cameraId;
            if (isBack){
                cameraId = manager.getCameraIdList()[0];      //0后   1前
                // Choose the sizes for camera preview and video recording
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                if (map == null) {
                    throw new RuntimeException("Cannot get available preview/video sizes");
                }
//                mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        width, height);

                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    texture_preview2.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    texture_preview2.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
                configureTransform(true,width, height);
//                mMediaRecorder = new MediaRecorder();
                manager.openCamera(cameraId, mStateCallback_back, null);
            }else {
                cameraId = manager.getCameraIdList()[1];     //0后   1前
                // Choose the sizes for camera preview and video recording
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                StreamConfigurationMap map = characteristics
                        .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                mSensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
                if (map == null) {
                    throw new RuntimeException("Cannot get available preview/video sizes");
                }
//                mVideoSize = chooseVideoSize(map.getOutputSizes(MediaRecorder.class));
                mPreviewSize = getOptimalSize(map.getOutputSizes(SurfaceTexture.class),
                        width, height);
                int orientation = getResources().getConfiguration().orientation;
                if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    texture_preview.setAspectRatio(mPreviewSize.getWidth(), mPreviewSize.getHeight());
                } else {
                    texture_preview.setAspectRatio(mPreviewSize.getHeight(), mPreviewSize.getWidth());
                }
                configureTransform(false,width, height);
//                mMediaRecorder = new MediaRecorder();
                manager.openCamera(cameraId, mStateCallback_front, null);
            }

        } catch (CameraAccessException | InterruptedException e) {
//            showTip("Cannot access the camera.");
            activity.finish();
        } catch (NullPointerException e) {
            // Currently an NPE is thrown when the Camera2API is used but not supported on the
            // device this code runs.
        }
    }

    private Semaphore getSemaphore(boolean isBack) {
        if(isBack){
            return mCameraOpenCloseLock;
        }else{
            return mCameraOpenCloseLock_front;
        }
    }

    private TextureView.SurfaceTextureListener mFrontTextureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(false,texture_preview.getWidth(), texture_preview.getHeight());
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private TextureView.SurfaceTextureListener mBackTextureListener=new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera(true,texture_preview2.getWidth(), texture_preview2.getHeight());
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {

        }
    };
    private CameraDevice.StateCallback mStateCallback_back = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview(true);
            mCameraOpenCloseLock.release();
            if (null != texture_preview2) {
                configureTransform(true,texture_preview2.getWidth(), texture_preview2.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = MainActivity3.this;
            if (null != activity) {
                activity.finish();
            }
        }

    };
    private CameraDevice.StateCallback mStateCallback_front = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            startPreview(false);
            mCameraOpenCloseLock_front.release();
            if (null != texture_preview) {
                configureTransform(false,texture_preview.getWidth(), texture_preview.getHeight());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            mCameraOpenCloseLock_front.release();
            cameraDevice.close();
            mCameraDevice = null;
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int error) {
            mCameraOpenCloseLock_front.release();
            cameraDevice.close();
            mCameraDevice = null;
            Activity activity = MainActivity3.this;
            if (null != activity) {
                activity.finish();
            }
        }

    };
    private void startPreview(final Boolean isBack) {
        if (null == mCameraDevice || !getTexture(isBack).isAvailable() || null == mPreviewSize) {
            return;
        }
        final Handler mHandler;
        final String threadName;
        if (isBack){
            mHandler=mHandler_back;
            threadName="Camera_back";
        }else {
            mHandler=mHandler_front;
            threadName="Camera_front";
        }

        try {
//            closePreviewSession();
            SurfaceTexture texture = getTexture(isBack).getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
            mPreviewBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            Surface previewSurface = new Surface(texture);
            mPreviewBuilder.addTarget(previewSurface);
            mCameraDevice.createCaptureSession(Collections.singletonList(previewSurface),
                    new CameraCaptureSession.StateCallback() {

                        @Override
                        public void onConfigured(@NonNull CameraCaptureSession session) {
                            mPreviewSession = session;
                            //updatePreview
                            if (null == mCameraDevice) {
                                return;
                            }
                            try {
                                setUpCaptureRequestBuilder(mPreviewBuilder);
                                HandlerThread thread = new HandlerThread(threadName);
                                thread.start();
                                session.setRepeatingRequest(mPreviewBuilder.build(), null, mHandler);
                            } catch (CameraAccessException e) {
                                e.printStackTrace();
                            }
                        }

                        @Override
                        public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                            Activity activity = MainActivity3.this;
                            if (null != activity) {
                                Toast.makeText(activity, "Failed", Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, mHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private AutoFitTextureView getTexture(boolean isBack){
        if (isBack){
            return texture_preview2;
        }else {
            return texture_preview;
        }
    }
    // 选择sizeMap中大于并且最接近width和height的size
    private Size getOptimalSize(Size[] sizeMap, int width, int height) {
        List<Size> sizeList = new ArrayList<>();
        for (Size option : sizeMap) {
            if (width > height) {
                if (option.getWidth() > width && option.getHeight() > height) {
                    sizeList.add(option);
                }
            } else {
                if (option.getWidth() > height && option.getHeight() > width) {
                    sizeList.add(option);
                }
            }
        }
        if (sizeList.size() > 0) {
            return Collections.min(sizeList, new Comparator<Size>() {
                @Override
                public int compare(Size lhs, Size rhs) {
                    return Long.signum(lhs.getWidth() * lhs.getHeight() - rhs.getWidth() * rhs.getHeight());
                }
            });
        }
        return sizeMap[0];
    }
    private void configureTransform(boolean isBack,int viewWidth, int viewHeight){} /*{
        TextureView textureView;
        if(isBack){
            textureView=texture_preview2;
        }else{
            textureView=texture_preview;
        }
        if (null == textureView || null == mPreviewSize) {
            return;
        }
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        Matrix matrix = new Matrix();
        RectF viewRect = new RectF(0, 0, viewWidth, viewHeight);
        RectF bufferRect = new RectF(0, 0, mPreviewSize.getHeight(), mPreviewSize.getWidth());
        float centerX = viewRect.centerX();
        float centerY = viewRect.centerY();
        if (Surface.ROTATION_90 == rotation || Surface.ROTATION_270 == rotation) {
            bufferRect.offset(centerX - bufferRect.centerX(), centerY - bufferRect.centerY());
            matrix.setRectToRect(viewRect, bufferRect, Matrix.ScaleToFit.FILL);
            float scale = Math.max(
                    (float) viewHeight / mPreviewSize.getHeight(),
                    (float) viewWidth / mPreviewSize.getWidth());
            matrix.postScale(scale, scale, centerX, centerY);
            matrix.postRotate(90 * (rotation - 2), centerX, centerY);
        } else if (Surface.ROTATION_180 == rotation) {
            matrix.postRotate(180, centerX, centerY);
        }
        textureView.setTransform(matrix);
    }*/
    private void setUpCaptureRequestBuilder(CaptureRequest.Builder builder) {
        builder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        // 获取设备方向
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
//        rotation=1;
        // 根据设备方向计算设置照片的方向
        builder.set(CaptureRequest.JPEG_ORIENTATION
                , ORIENTATIONS.get(rotation));
    }
    private void closePreviewSession() {
        if (mPreviewSession != null) {
            try {
                mPreviewSession.stopRepeating();
                mPreviewSession.abortCaptures();
                mPreviewSession.close();
            } catch (CameraAccessException e) {
                e.printStackTrace();
            }
        }
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
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == event.KEYCODE_BACK && event.getRepeatCount() == 0) {
            startActivity(new Intent(MainActivity3.this, MenuActivity.class));
            destroy();
            // 退出时，请求杀死进程
            android.os.Process.killProcess(android.os.Process.myPid());
        }
        return super.onKeyDown(keyCode, event);
    }
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void destroy(){
        closePreviewSession();
        if(mCameraDevice!=null){
            mCameraDevice.close();
            mCameraDevice=null;
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
