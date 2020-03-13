package com.umi.twocamera.app;

import android.os.Environment;

/**
 * @author 郑州优米科技有限公司
 * @version ${VERSION}
 * 修改密码activity
 * @date 2020/1/8.
 */
public class Const {
    public static String filePath= Environment.getExternalStorageDirectory() + "/twocamera/";
    public static String picPath= Environment.getExternalStorageDirectory() + "/twocamera/icon/";//头像
    public static String photoPath= Environment.getExternalStorageDirectory() + "/twocamera/photo/";//抓拍
    public static String fileExport= Environment.getExternalStorageDirectory() + "/twocamera/export/";//导出

    public static String yanzheng_type="yanzheng_type";//主机认证类型
    public static String fanqianhui="fanqianhui";//反潜回
    public static String chaoci="chaoci";//认证超次报警
    public static String chaoci_num="chaoci_num";//认证做多次数
}
