
package com.umi.twocamera.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 */
public class NetworkUtils {

	/**
     * 网络连接
     * 
     * @date 2012-4-13
     * @tags @param con
     * @tags @return 是否有连接
     */

    public static boolean checkNet(Context context) {
        try {
            // 获取手机所有连接管理对象（wi_fi,net等连接的管理）
            ConnectivityManager manger = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            if (manger != null) {
                NetworkInfo info[] = manger.getAllNetworkInfo();
                if (info != null) {
                    for (int i = 0; i < info.length; i++) {
                        if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;

    }
}
