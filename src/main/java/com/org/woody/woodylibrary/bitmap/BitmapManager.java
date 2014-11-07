package com.org.woody.woodylibrary.bitmap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
/**
 * 图片缓存管理类，有效防止图片引起的OOM
 * 设计了三重缓存：图片缓存（Bitmap），字节缓存（Byte），本地缓存(文件)
 * 提供bindView方法，方便的异步加载图片到View
 * @author linlinfaxin@163.com
 */
public class BitmapManager{
	public static final String NOMEDIA=".nomedia";
	static private Bitmap loadingFailBitmap;//默认的加载失败的图片
	public static Bitmap getLoadingFailBitmap(){return loadingFailBitmap;}
	static private File cacheImgDir;//图片缓存到本地的目录
	static final private String ImgFileNameExtension="";//本地缓存的图片的额外后缀名
	private static int MAX_BitmapList_Size=5*1024*1024;//最大的bitmap缓存大小
	private static int MAX_ImageByteList_Size=2*1024*1024;//最大的bytes缓存大小
	private static int MAX_Bitmap_Width=1280;//最大的可以缓存的BitmapList的宽（如果大于这个值会自动缩放）
	private static int MAX_Bitmap_Height=1280;//最大的可以缓存进BitmapList的高（如果大于这个值会自动缩放）
	private static final int DefaultLoadNetDelay=200;//从网络下载图片的等待延迟（有利于Listview的效率）
	/**内容图片缓存，用来缓存之前载入过的bitmap*/
	private static final LinkedHashMap<String,Bitmap> bitmapList=new LinkedHashMap<String,Bitmap>();
	/**内容字节缓存，用来缓存之前载入过的bitmap的字节*/
	private static final LinkedHashMap<String,byte[]> imagebytesList=new LinkedHashMap<String,byte[]>();
	public static void init(Context context){
		init(context, null, new File(context.getExternalCacheDir(), "imgCache"));
	}
	/**
	 * 初始化BitmapManager类的一些参数，必须在使用前调用
	 * @param context 上下文
	 * @param loadingFailBitmap 默认的加载失败的图片
	 * @param imgDir 默认本地图片缓存目录，为空则不缓存到本地文件
	 */
	public static void init(Context context,Bitmap loadingFailBitmap,File imgDir){
		final int memClass = ((ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass(); 
		initMaxCache(1024 * 1024 * memClass / 4, 1024 * 1024 * memClass / 4);
		if(loadingFailBitmap==null) loadingFailBitmap=Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_8888);
		BitmapManager.loadingFailBitmap=loadingFailBitmap;
		BitmapManager.cacheImgDir=imgDir;
		if (imgDir!=null) {
			try {
				new File(imgDir, NOMEDIA).createNewFile();
			} catch (Exception e) {
			}
			if (!BitmapManager.cacheImgDir.exists()) BitmapManager.cacheImgDir.mkdirs();
		}
	}
	static private void initMaxCache(int bitmapsize,int bytesize){
		MAX_BitmapList_Size=bitmapsize;
		MAX_ImageByteList_Size=bytesize;
	}
	/**bindView时，用于将view与url绑定，避免多次bandview造成的显示图片出错*/
	private static ConcurrentHashMap<View, String> bindedViewMap=new ConcurrentHashMap<View, String>();
	/**
	 * 异步绑定一个View，让它显示Bitmap（如果加载失败或者url为空，则显示BitmapManager.loadingFailBitmap）
	 * @param view 绑定的view，如果是ImageView则设置src，否则设置background
	 * @param url 图片路径
	 */
	public static void bindView(final View view,final String url){
		bindView(view, null, loadingFailBitmap, loadingFailBitmap, url, DefaultLoadNetDelay, null);
	}
	public static void bindView(final View view,final String url, int delay){
		bindView(view, null, loadingFailBitmap, loadingFailBitmap, url, delay, null);
	}
	/**
	 * 异步绑定一个View，让它显示Bitmap（如果加载失败或者url为空，则显示BitmapManager.loadingFailBitmap）
	 * @param view 绑定的view，如果是ImageView则设置src，否则设置background
	 * @param loadingBitmap 加载过程中view显示的bitmap,为空则显示透明
	 * @param url 图片路径
	 */
	public static void bindView(final View view,Bitmap loadingBitmap,final String url){
		bindView(view, loadingBitmap, loadingFailBitmap, loadingFailBitmap, url, DefaultLoadNetDelay, null);
	}

	/**
	 * 异步绑定一个View，让它显示Bitmap（如果加载失败，则显示BitmapManager.loadingFailBitmap）
	 * @param view 绑定的view，如果是ImageView则设置src，否则设置background
	 * @param loadingBitmap 加载过程中view显示的bitmap,为空则显示透明
	 * @param noImgBitmap 如果url为空显示的图片,为空则显示BitmapManager.loadingFailBitmap
	 * @param loadFailBitmap 如果加载图片失败显示的图片,为空则显示BitmapManager.loadingFailBitmap
	 * @param url 图片路径
	 */
	public static void bindView(final View view,Bitmap loadingBitmap,Bitmap noImgBitmap,Bitmap loadFailBitmap,final String url){
		bindView(view, loadingBitmap, noImgBitmap, loadFailBitmap, url, DefaultLoadNetDelay, null);
	}
	/**
	 * 异步绑定一个View，让它显示Bitmap（如果加载失败，则显示BitmapManager.loadingFailBitmap）
	 * @param view 绑定的view，如果是ImageView则设置src，否则设置background
	 * @param loadingBitmap 加载过程中view显示的bitmap,为空则显示透明
	 * @param noImgBitmap 如果url为空显示的图片,为空则显示BitmapManager.loadingFailBitmap
	 * @param loadFailBitmap 如果加载图片失败显示的图片,为空则显示BitmapManager.loadingFailBitmap
	 * @param url 图片路径
	 * @param delay 从网络载入的延迟等待
	 */
	public static void bindView(final View view,Bitmap loadingBitmap,Bitmap noImgBitmap,Bitmap loadFailBitmap,final String url,int delay){
		bindView(view, loadingBitmap, noImgBitmap, loadFailBitmap, url, delay, null);
	}
	/**
	 * 异步绑定一个View，让它显示Bitmap（如果加载失败，则显示BitmapManager.loadingFailBitmap）
	 * @param view 绑定的view，如果是ImageView则设置src，否则设置background
	 * @param loadingBitmap 加载过程中view显示的bitmap,为空则显示透明
	 * @param noImgBitmap 如果url为空显示的图片,为空则显示BitmapManager.loadingFailBitmap
	 * @param loadFailBitmap 如果加载图片失败显示的图片,为空则显示BitmapManager.loadingFailBitmap
	 * @param bitmapLoadingListener 图片进度监听接口，不需要在这个接口中setImageBitmap
	 * @param url 图片路径
	 */
	public static void bindView(final View view,Bitmap loadingBitmap,Bitmap noImgBitmap,Bitmap loadFailBitmap,final String url,final BitmapLoadingListener bitmapLoadingListener){
		bindView(view, loadingBitmap, noImgBitmap, loadFailBitmap, url, DefaultLoadNetDelay, bitmapLoadingListener);
	}
	/**
	 * 异步绑定一个View，让它显示Bitmap（如果加载失败，则显示BitmapManager.loadingFailBitmap）
	 * @param view 绑定的view，如果是ImageView则设置src，否则设置background
	 * @param loadingBitmap 加载过程中view显示的bitmap,为空则显示透明
	 * @param noImgBitmap 如果url为空显示的图片,为空则显示BitmapManager.loadingFailBitmap
	 * @param loadFailBitmap 如果加载图片失败显示的图片,为空则显示BitmapManager.loadingFailBitmap
	 * @param url 图片路径
	 * @param delay 从网络载入的延迟等待
	 * @param bitmapLoadingListener 图片进度监听接口，不需要在这个接口中setImageBitmap
	 */
	public static void bindView(final View view,Bitmap loadingBitmap,Bitmap noImgBitmap,final Bitmap loadFailBitmap,final String url,final int delay,final BitmapLoadingListener bitmapLoadingListener){
		if(view==null) return;
		if(TextUtils.isEmpty(url)){
			setImageToView(view,noImgBitmap!=null?noImgBitmap:loadingFailBitmap);
			return;
		}
		if(cacheImgDir==null) init(view.getContext());
		setImageToView(view, loadingBitmap );
		bindedViewMap.put(view, url);//将view和url绑定
		BitmapManager.getBitmapInBg(url,new BitmapLoadingListener() {
			@Override
			public void onBitmapLoadFinish(Bitmap bitmap, boolean isLoadSuccess) {
				if(url.equals(bindedViewMap.get(view))){//检查是否依然绑定
					bindedViewMap.remove(view);
					if(isLoadSuccess) setImageToView(view,bitmap);
					else setImageToView(view,loadFailBitmap!=null?loadFailBitmap:loadingFailBitmap);
					if(view instanceof RecycleableImageView){
						((RecycleableImageView) view).setUrl(url);
					}
					if(bitmapLoadingListener!=null) bitmapLoadingListener.onBitmapLoadFinish(bitmap, isLoadSuccess);
				}
			}
			@Override
			public void onBitmapLoading(int progress) {
//				Log.d("fax", "onBitmapLoading:"+progress);
				if(bitmapLoadingListener!=null) bitmapLoadingListener.onBitmapLoading(progress);
			}
		},delay,new LoadChecker() {
			public boolean canLoad() {
				return url.equals(bindedViewMap.get(view));
			}
		});
	}
	/**从view中获取bitmap，可能为null */
	public static Bitmap getBitmapFromView(View view){
		try {
			if (view instanceof ImageView) {
				return ((BitmapDrawable) ((ImageView) view).getDrawable()).getBitmap();
			} else {
				return ((BitmapDrawable) view.getBackground()).getBitmap();
			}
		} catch (Exception e) {
			return null;
		}
	}
	@SuppressWarnings("deprecation")
	private static void setImageToView(View view,Bitmap bitmap){
		if(view instanceof ImageView){
			if(bitmap==null) ((ImageView) view).setImageDrawable(new ColorDrawable(Color.TRANSPARENT));
			else ((ImageView) view).setImageBitmap(bitmap);
		}else{
			if(bitmap==null) view.setBackgroundColor(Color.TRANSPARENT);
			else view.setBackgroundDrawable(new BitmapDrawable(view.getContext().getResources(),bitmap));
		}
	}
	/**
	 * 异步获取图片，会立即返回。从listen中监听，listen中的方法会在当前线程执行
	 */
	@SuppressLint("NewApi")
	public static void getBitmapInBg(String url,BitmapLoadingListener listen){
		LoadBitmapTask task=new LoadBitmapTask(listen);
		if(android.os.Build.VERSION.SDK_INT<11) task.execute(url);
		else task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
	}
	/**
	 * 异步获取图片，会立即返回。从listen中监听，listen中的方法会在当前线程执行
	 * @param delay 延迟这个时间再载入
	 */
	@SuppressLint("NewApi")
	private static void getBitmapInBg(String url,BitmapLoadingListener listen, int delay, LoadChecker checker){
		LoadBitmapTask task=new LoadBitmapTask(listen,delay,checker);
		if(android.os.Build.VERSION.SDK_INT<11) task.execute(url);
		else task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, url);
	}
	/**
	 * 获取图片，会堵塞当前线程
	 * @param url 图片地址
	 * @return 请求的图片（如果失败会返回loadingFailBitmap）
	 */
	public static Bitmap getBitmap(String url){
		return getBitmap(url, null);
	}
	/**
	 * 获取图片
	 * @param url 图片地址
	 * @param task 读取bitmap的一个task，用作监听进度
	 * @return
	 */
	private static Bitmap getBitmap(String url,LoadBitmapTask task){
		if(url==null){
			return loadingFailBitmap;
		}
		try {
			Bitmap bitmap = getFromBitmapList(url, task);
			if (bitmap == null) return loadingFailBitmap;
			return bitmap;
		} catch (Exception e) {
			e.printStackTrace();
			return loadingFailBitmap;
		}
	}
	/**
	 * 从内存图片缓存中获取图片（如果获取失败则从内存字节缓存中获取）
	 * @param url url 图片的地址（在内存缓存中作为key）
	 * @param task 读取bitmap的一个task，用作监听进度
	 * @return 图片的字节数组
	 */
	private static Bitmap getFromBitmapList(String url,LoadBitmapTask task){
		Bitmap bitmap=bitmapList.get(url);
		if(bitmap==null||bitmap.isRecycled()){//从内存图片缓存获取失败，则尝试从内存字节缓存获取
			byte[] img_bytes=getFromImageBytesList(url,task);
			if(img_bytes==null) return null;
			bitmap = BitmapFactory.decodeByteArray(img_bytes, 0, img_bytes.length);
			bitmap = scaleToMiniBitmap(bitmap);
			putToBitmapList(url, bitmap);
		}
		return bitmap;
	}

	/**
	 * 从内存字节缓存中获取图片（如果获取失败则从本地缓存(网络)中获取）
	 * @param url 图片的地址（在内存缓存中作为key）
	 * @param task 读取bitmap的一个task，用作监听进度
	 * @return 图片的字节数组
	 */
	private static byte[] getFromImageBytesList(String url,LoadBitmapTask task){
		byte[] img_bytes=imagebytesList.get(url);//尝试从内存缓存imagebytesList中获取
		if(img_bytes==null){//从imagebytesList获取失败则尝试从本地(网络)获取
			img_bytes=getImgBytesInDisk(url,task);
			putToImageBytesList(url, img_bytes);
		}
		return img_bytes;
	}
	public static File getImgFile(String urlStr){
		if(cacheImgDir==null) return null;
		final String filename=convertToFileName(urlStr);
		final File imageFile = new File(cacheImgDir, filename);
		return imageFile;
	}
	/**
	 * 从本地读取图片(如果本地不存在，则从网络获取)
	 * @param urlStr 图片的地址，会被转换成本地路径储存在imgDir目录里
	 * @param task 读取bitmap的一个task，用作监听进度
	 * @return 图片的字节数组
	 */
	synchronized private static byte[] getImgBytesInDisk(String urlStr,LoadBitmapTask task){
		//尝试从本地文件获取图片
		final String filename=convertToFileName(urlStr);
		final File imageFile = new File(cacheImgDir, filename);
		if(cacheImgDir!=null){
			if (imageFile.exists() && imageFile.length() > 0) {
				try {
					byte[] bytes = getBytesFromInputStream(new BufferedInputStream(new FileInputStream(imageFile)),(int)imageFile.length(),task);
					if (bytes == null){
						imageFile.delete();
					}else return bytes;
				} catch (Exception e) {
					e.printStackTrace();
					imageFile.delete();//删除错误（写入一半）的文件
				}
			}else{
				imageFile.delete();//删除错误的文件
			}
		}
		
		//没有从本地文件获取到，那么尝试从网络获取(等待延迟的载入)
		if(task!=null && task.delay>0){
			try {
				Thread.sleep(task.delay);
				if(task.checker!=null && !task.checker.canLoad()) return null;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
		}
		
		byte[] bytes = getImgBytesInNet(urlStr,task);
		if(bytes==null) return null;
		
		//将从网络获取到的图片字节数组储存
		try {
			if (cacheImgDir!=null) {
				BufferedOutputStream fos = new BufferedOutputStream(new FileOutputStream(imageFile));
				ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
				byte[] temp = new byte[10 * 1024];
				int length;
				while ((length = bais.read(temp)) != -1) {
					fos.write(temp, 0, length);
				}
				fos.close();
				bais.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return bytes;
	}
	/**
	 * 从网络读取图片
	 * @param urlStr 图片路径
	 * @param task 读取bitmap的一个task，用作监听进度
	 * @return 图片的字节数组
	 */
	synchronized private static byte[] getImgBytesInNet(String urlStr,LoadBitmapTask task){
		try {
			URL url = new URL(urlStr);
			URLConnection conn=url.openConnection();
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(20000);
			int length=conn.getContentLength();
			byte[] imgbytes = getBytesFromInputStream(conn.getInputStream(), length, task);
			return imgbytes;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	/**
	 * 从输入流读取图像数据
	 * @param is 输入流，文件输入流或网络输入流
	 * @param length 输入流的长度，文件的大小或网络输入流的大小
	 * @param task 读取bitmap的一个task，用作监听进度
	 * @return
	 */
	private static byte[] getBytesFromInputStream(InputStream is,int length,LoadBitmapTask task){
		ByteArrayOutputStream baos=null;
		try {
			baos = new ByteArrayOutputStream();
			byte[] bytes = new byte[16 * 1024];
			int readedLength=0;
			int tempLength;
			while ((tempLength = is.read(bytes)) != -1) {
				if(Thread.currentThread().isInterrupted()) throw new InterruptedException();
				baos.write(bytes, 0, tempLength);
				if(task!=null&&length>0){
					readedLength+=tempLength;
					task.pushProgress(readedLength*100/length);
				}
			}
			byte[] imgbytes = baos.toByteArray();
			baos.close();
			is.close();
			return imgbytes;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				if (baos != null) baos.close();
			} catch (Exception e2) {
			}
			try {
				is.close();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			return null;
		}
	}
	/**
	 * 将url转化为文件名储存
	 */
	private static String convertToFileName(String urlStr){
		try {
			URL url=new URL(urlStr);
			String fileName=new File(url.getPath()).getName()+ImgFileNameExtension;
			return fileName;
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return urlStr.replace("\\", "-").replace("/", "-").replace(":", "-").replace("*", "-").replace("?", "-")
					  .replace("\"", "-").replace("<", "-").replace(">", "-").replace("|", "-");
	}
	private static synchronized void putToBitmapList(String url,Bitmap bitmap){
		if(!TextUtils.isEmpty(url) && bitmap!=null){
			bitmapList.put(url, bitmap);
			checkBitmapListSize();// 检查缓存大小是否达上限
		}
	}
	/**
	 * 检查缓存大小是否超出，如果超出则自动清除最早的缓存
	 */
	private static synchronized void checkBitmapListSize(){
		try {
			int allBitmapSize = 0;
			for (Bitmap bitmap : bitmapList.values()) {
				if (bitmap == null || bitmap.isRecycled())
					continue;
				allBitmapSize += (bitmap.getRowBytes() * bitmap.getHeight());
			}
			if (allBitmapSize > MAX_BitmapList_Size) {//缓存超出，开始清空
				int size = bitmapList.keySet().size();
				if (size > 0) {
					Bitmap bitmap = bitmapList.remove(bitmapList.keySet().iterator().next());
					bitmap.recycle();
					bitmap = null;
				}
				checkBitmapListSize();//重复检查
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	private static synchronized void putToImageBytesList(String url,byte[] bytes){
		if(!TextUtils.isEmpty(url) && bytes!=null &&bytes.length>0){
			imagebytesList.put(url, bytes);
			checkImageBytesSize();// 检查缓存大小是否达上限
		}
	}
	/**
	 * 检查缓存大小是否超出，如果超出则自动清除最早的缓存
	 */
	private static synchronized void checkImageBytesSize(){
		try {
			int allBytesSize = 0;
			for (byte[] bytes : imagebytesList.values()) {
				allBytesSize += (bytes.length);
			}
			if (allBytesSize > MAX_ImageByteList_Size) {//缓存超出，开始清空
				int size = imagebytesList.keySet().size();
				if (size > 0) {
					String key = imagebytesList.keySet().iterator().next();
					if (imagebytesList.remove(key) == null) {
					}
				} else {
				}
				checkImageBytesSize();//重复检查
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Bitmap scaleToMiniBitmap(Bitmap in){
		int widthLimit=MAX_Bitmap_Width;
		int heightLimit=MAX_Bitmap_Height;
		return scaleToMiniBitmap(in, widthLimit, heightLimit);
	}
	//将图片控制在限定高宽之内
	public static Bitmap scaleToMiniBitmap(Bitmap in,int widthLimit,int heightLimit){
		int inWidth=in.getWidth();
		int inHeight=in.getHeight();
//		Log.d("fax scaleBitmap", "in.width:"+inWidth+",in.height:"+inHeight);
		if(inWidth<=widthLimit&&inHeight<=heightLimit) return in;
		float scale=Math.min(widthLimit*1f/inWidth,heightLimit*1f/inHeight);
		Bitmap re=Bitmap.createScaledBitmap(in, (int)(inWidth*scale), (int) (inHeight*scale), true);
//		Log.d("fax scaleBitmap", "out.width:"+re.getWidth()+",out.height:"+re.getHeight());
		in.recycle();
		return re;
	}
	public static InputStream readBitmap(Bitmap bitmap){
		return new ByteArrayInputStream(compressBitmap(bitmap));
	}
	public static byte[] compressBitmap(Bitmap bitmap){
		return compressBitmap(bitmap, 80, false);
	}
	/**
	 * 压缩Bitmap，返回压缩后的字节数组
	 * @param bitmap 目标Bitmap
	 * @param quality 压缩质量
	 * @param isRecycle 压缩完成后是否回收Bitmap
	 * @return
	 */
	public static byte[] compressBitmap(Bitmap bitmap,int quality,boolean isRecycle){
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bitmap.compress(CompressFormat.JPEG, quality, baos);
		if(isRecycle) bitmap.recycle();
		try {
			baos.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return baos.toByteArray();
	}
	/**
	 * 图像载入进度的监听接口
	 * @author linfaxin
	 */
	public interface BitmapLoadingListener{
		/**
		 * 载入结束
		 * @param bitmap 载入的最终图片（有可能是loadingFailBitmap）
		 * @param isLoadSuccess 载入是否失败
		 */
		public void onBitmapLoadFinish(Bitmap bitmap, boolean isLoadSuccess);
		/**
		 * @param progress 载入的进度。（0-100）
		 */
		public void onBitmapLoading(int progress);
	}
	/**载入的检查器（判断是否可以载入） */
	public interface LoadChecker{
		public boolean canLoad();
	}
	/**
	 * 为了让listener的载入结果、进度在主线程上运行的类
	 * @author linfaxin
	 */
	private static class LoadBitmapTask extends AsyncTask<String, Integer, Bitmap>{
		BitmapLoadingListener bitmapLoadingListener;
		int delay;
		LoadChecker checker;
		public LoadBitmapTask(BitmapLoadingListener bitmapLoadingListener){
			this.bitmapLoadingListener=bitmapLoadingListener;
		}
		public LoadBitmapTask(BitmapLoadingListener bitmapLoadingListener,int delay,LoadChecker checker){
			this.bitmapLoadingListener=bitmapLoadingListener;
			this.delay=delay;
			this.checker=checker;
		}
		@Override
		protected Bitmap doInBackground(String... params) {
			try {
				Thread.sleep(200);//延迟载入，有效防止列表中快速滚动时的性能
				if(checker!=null && !checker.canLoad()) return null;
			} catch (InterruptedException e) {
				e.printStackTrace();
				return null;
			}
			return getBitmap((String) params[0],this);
		}
		private int lastProgress=0;
		public void pushProgress(int progress){
			if(lastProgress!=progress){
				lastProgress=progress;
				this.publishProgress(progress);
			}
		}
		@Override
		protected void onProgressUpdate(Integer... values) {
			bitmapLoadingListener.onBitmapLoading(values[0]);
		}
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if(checker!=null && !checker.canLoad()) bitmap=null;
			bitmapLoadingListener.onBitmapLoadFinish(bitmap, bitmap!=loadingFailBitmap);
		}
		
	}
}
