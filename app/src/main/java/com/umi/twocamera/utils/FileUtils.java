package com.umi.twocamera.utils;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
* @author 郑州优米科技有限公司
* @date 2016-10-21
* @version V1.0  
*/
@SuppressLint({ "NewApi", "DefaultLocale" })
public class FileUtils {
	// 获取当前目录下所有的mp4文件
	public static Vector<String> GetVideoFileName(String fileAbsolutePath) {
		Vector<String> vecFile = new Vector<String>();
		File file = new File(fileAbsolutePath);
		File[] subFile = file.listFiles();
		if(subFile!=null){
		for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
			// 判断是否为文件夹
			if (!subFile[iFileLength].isDirectory()) {
				String filename = subFile[iFileLength].getAbsolutePath();
				// 判断是否为MP4结尾
				if (filename.trim().toLowerCase().endsWith(".mp4")
						|| filename.trim().toLowerCase().endsWith(".mkv")
						|| filename.trim().toLowerCase().endsWith(".avi")) {
					vecFile.add(filename);
				}
			}
		}
		}
		return vecFile;
	}

	// 获取当前目录下所有的圖片文件
	public static Vector<String> GetImageFileName(String fileAbsolutePath) {
		Vector<String> vecFile = new Vector<String>();
		File file = new File(fileAbsolutePath);
		File[] subFile = file.listFiles();
		if(subFile!=null){
			for (int iFileLength = 0; iFileLength < subFile.length; iFileLength++) {
				// 判断是否为文件夹
				if (!subFile[iFileLength].isDirectory()) {
					String filename = subFile[iFileLength].getAbsolutePath();
					// 判断是否为MP4结尾
					if (filename.trim().toLowerCase().endsWith(".jpg")) {
						vecFile.add(filename);
					}
				}
			}
		}
		return vecFile;
	}

	/**
	 * 保存文件
	 * 
	 * @throws IOException
	 */
//	static int count=0;
	public static int getBitmapSize(Bitmap bitmap){
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){    //API 19
	        return bitmap.getAllocationByteCount();
	    }
	    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1){//API 12
	        return bitmap.getByteCount();
	    }
	    return bitmap.getRowBytes() * bitmap.getHeight();                //earlier version
	}
	public static byte[] getBytes(InputStream is) throws IOException {
        ByteArrayOutputStream outstream = new ByteArrayOutputStream();
        //把所有的变量收集到一起，然后一次性把数据发送出去
        byte[] buffer = new byte[1024]; // 用数据装
        int len = 0;
        while ((len = is.read(buffer)) != -1) {
            outstream.write(buffer, 0, len);
        }
        outstream.close();

        return outstream.toByteArray();
    }
	/**
	 * Get image from newwork
	 * 
	 * @param path
	 *            The path of image
	 * @return InputStream
	 * @throws Exception
	 */
	public static InputStream getImageStream(String path) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(5 * 1000);
		conn.setRequestMethod("GET");
		int num=conn.getContentLength()/1024;
		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			return conn.getInputStream();
		}else{
			return null;
		}
	}
	
	public static Bitmap getImageBitmap(String path,boolean isAd) throws Exception {
		URL url = new URL(path);
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(20 * 1000);
		conn.setRequestMethod("GET");
		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		InputStream is = conn.getInputStream();
	     BitmapFactory.Options options=new BitmapFactory.Options();
			int num=1;
			try{
				if(conn.getContentLength()>500000) {
//				num = conn.getContentLength() / 200000;
					num=Chufa(conn.getContentLength(),500000);
					options.inSampleSize =num;
				}
			}catch (Exception e){
				e.printStackTrace();
			}
	     options.inJustDecodeBounds = false;
          /*  int sign=1;
	     if(num>100)  sign = num / 30;
             if (!isAd && sign > 0)
                 options.inSampleSize = sign;   //width，hight设为原来的2分一*/
	     Bitmap bm =BitmapFactory.decodeStream(is,null,options);
//	     LogUtils.i("TAG","原始："+num+"压缩："+sign+"压缩后："+bm.getByteCount()/1024.);
		 return bm;
		}else{
			return null;
		}
	}
	//定义方法
	public static int Chufa(int C,int D) {
		BigDecimal a = new BigDecimal(C);
		BigDecimal b = new BigDecimal(D);
		return Integer.parseInt(a.divide(b,0,BigDecimal.ROUND_HALF_UP)+"");
	}
	public static Bitmap getImageBitmap2(String path,boolean isAd) throws Exception {
		byte[] b=null;
		URL url = new URL(path);
//		URL url = new URL("http://login.zzumi.com/txy/upload//headPic//190308133616//15916497472.jpg");
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setConnectTimeout(20 * 1000);
		conn.setRequestMethod("GET");
		int num=conn.getContentLength()/1024;
		if (conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
			InputStream is = conn.getInputStream();
			b=readInputStream(is);
			Bitmap bm =decodeSampledBitmapFromStream(b,300,300);
			return bm;
		}else{
			return null;
		}
	}
	/**
	 * 加载本地图片
	 * 
	 * @param url
	 * @return
	 */
	public static Bitmap getLoacalBitmap(String url) {
		try {
			if(TextUtils.isEmpty(url))return null;
			FileInputStream fis = new FileInputStream(url);
			return BitmapFactory.decodeStream(fis); // /把流转化为Bitmap图片
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	static Bitmap comp(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);
		if (baos.toByteArray().length / 1024 > 1024) {// 判断如果图片大于1M,进行压缩避免在生成图片（BitmapFactory.decodeStream）时溢出   
			baos.reset();// 重置baos即清空baos
			image.compress(Bitmap.CompressFormat.JPEG, 50, baos);// 这里压缩50%，把压缩后的数据存放到baos中
		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());
		BitmapFactory.Options newOpts = new BitmapFactory.Options();
		// 开始读入图片，此时把options.inJustDecodeBounds 设回true了
		newOpts.inJustDecodeBounds = true;
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		newOpts.inJustDecodeBounds = false;
		int w = newOpts.outWidth;
		int h = newOpts.outHeight;
		// 现在主流手机比较多是800*480分辨率，所以高和宽我们设置为
		float hh = 800f;// 这里设置高度为800f
		float ww = 480f;// 这里设置宽度为480f
		// 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
		int be = 1;// be=1表示不缩放
		if (w > h && w > ww) {// 如果宽度大的话根据宽度固定大小缩放
			be = (int) (newOpts.outWidth / ww);
		} else if (w < h && h > hh) {// 如果高度高的话根据宽度固定大小缩放
			be = (int) (newOpts.outHeight / hh);
		}
		if (be <= 0)
			be = 1;
		newOpts.inSampleSize = be;// 设置缩放比例
		newOpts.inPreferredConfig = Config.RGB_565;// 降低图片从ARGB888到RGB565
		// 重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
		isBm = new ByteArrayInputStream(baos.toByteArray());
		bitmap = BitmapFactory.decodeStream(isBm, null, newOpts);
		return compressImage(bitmap);// 压缩好比例大小后再进行质量压缩
	}

	private static Bitmap compressImage(Bitmap image) {

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.JPEG, 100, baos);// 质量压缩方法，这里100表示不压缩，把压缩后的数据存放到baos中
		int options = 100;
		while (baos.toByteArray().length / 1024 > 100) { // 循环判断如果压缩后图片是否大于100kb,大于继续压缩
			baos.reset();// 重置baos即清空baos
			options -= 10;// 每次都减少10
			image.compress(Bitmap.CompressFormat.JPEG, options, baos);// 这里压缩options%，把压缩后的数据存放到baos中

		}
		ByteArrayInputStream isBm = new ByteArrayInputStream(baos.toByteArray());// 把压缩后的数据baos存放到ByteArrayInputStream中
		Bitmap bitmap = BitmapFactory.decodeStream(isBm, null, null);// 把ByteArrayInputStream数据生成图片
		return bitmap;
	}
	/**
     * 递归删除文件和文件夹
     * @param file    要删除的根目录
     */
    public static void RecursionDeleteFile(File file){
        if(file.isFile()){
            file.delete();
            return;
        }
        if(file.isDirectory()){
            File[] childFile = file.listFiles();
            if(childFile == null || childFile.length == 0){
                file.delete();
                return;
            }
            for(File f : childFile){
                RecursionDeleteFile(f);
            }
            file.delete();
        }
    }

	//root自动安装应用
		public static int execRootCmdSilent(String cmd) {  
		       int result = -1;  
		       DataOutputStream dos = null;  
		  
		       try {  
		           Process p = Runtime.getRuntime().exec("su");  
		           dos = new DataOutputStream(p.getOutputStream());  
		  
		           Log.i("TAG", cmd);  
		           dos.writeBytes(cmd + "\n");  
		           dos.flush();  
		           dos.writeBytes("exit\n");  
		           dos.flush();  
		           p.waitFor();  
		           result = p.exitValue();  
		       } catch (Exception e) {  
		           e.printStackTrace();  
		       } finally {  
		           if (dos != null) {  
		               try {  
		                   dos.close();  
		               } catch (Exception e) {  
		                   e.printStackTrace();  
		               }  
		           }  
		       }  
		       return result;  
		   }
	private static byte[] readInputStream(InputStream in) throws Exception{
		int len=0;
		byte buf[]=new byte[1024];
		ByteArrayOutputStream out=new ByteArrayOutputStream();
		while((len=in.read(buf))!=-1){
			out.write(buf,0,len);  //把数据写入内存
		}
		out.close();  //关闭内存输出流
		return out.toByteArray(); //把内存输出流转换成byte数组
	}
	private static Bitmap decodeSampledBitmapFromStream(byte[] b,int reqWidth, int reqHeight) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeByteArray(b, 0, b.length, options);
		// 调用上面定义的方法计算inSampleSize值
		options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
		// 使用获取到的inSampleSize值再次解析图片
		options.inJustDecodeBounds = false;
		Log.v("decode","返回bitmap");
		return BitmapFactory.decodeByteArray(b, 0, b.length, options);
	}
	/*private static int calculateInSampleSize(BitmapFactory.Options options,
									  int reqWidth, int reqHeight) {
		// 源图片的高度和宽度
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			// 计算出实际宽高和目标宽高的比率
			final int heightRatio = Math.round((float) height / (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			// 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
			// 一定都会大于等于目标的宽和高。
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		Log.v("calculate"," "+inSampleSize);
		return inSampleSize;
	}*/
	public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {

		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}
	/**
	 * 把字节数组保存为一个文件
	 *
	 * @param b
	 * @param outputFile
	 * @return
	 */
	public static File getFileFromBytes(byte[] b, String outputFile) {
		File ret = null;
		BufferedOutputStream stream = null;
		try {
			ret = new File(outputFile);
			FileOutputStream fstream = new FileOutputStream(ret);
			stream = new BufferedOutputStream(fstream);
			stream.write(b);
		} catch (Exception e) {
			// log.error("helper:get file from byte process error!");
			e.printStackTrace();
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException e) {
					// log.error("helper:get file from byte process error!");
					e.printStackTrace();
				}
			}
		}
		return ret;
	}
}
