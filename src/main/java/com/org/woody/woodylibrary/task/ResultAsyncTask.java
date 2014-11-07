package com.org.woody.woodylibrary.task;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.widget.Toast;

import com.org.woody.woodylibrary.R;

/**方便使用的AsyncTask */
public abstract class ResultAsyncTask <T> extends AsyncTask<Void, Void, T>{
	private ProgressDialog pd;
	private Runnable onSucRun;
	private Runnable onFailRun;
	Context context;
	private boolean isSucToast=false;
	private boolean isFailToast=true;
	private String sucToast;
	private String failToast;
	private boolean isDismissPd=true;
    /**方便使用的AsyncTask */
	public ResultAsyncTask(Context context){
		this.context=context;
	}
	public ProgressDialog getProgressDialog(){
		return pd;
	}
	public ResultAsyncTask<T> setProgressDialog(ProgressDialog pd){
		this.pd=pd;
		return this;
	}
	public ResultAsyncTask<T> setProgressDialog(){
		return setProgressDialog(true);
	}
    public ResultAsyncTask<T> setProgressDialog(int waitMsgRes){
        return setProgressDialog(true, context.getString(waitMsgRes));
    }
    public ResultAsyncTask<T> setProgressDialog(String waitMsg){
        return setProgressDialog(true, waitMsg);
    }
    public ResultAsyncTask<T> setProgressDialog(boolean cancelAble){
        return setProgressDialog(cancelAble, context.getString(R.string.Task_PleaseWait));
    }
	public ResultAsyncTask<T> setProgressDialog(boolean cancelAble, String waitMsg){
		ProgressDialog pd=new ProgressDialog(context);
		pd.setMessage(waitMsg);
		pd.setCanceledOnTouchOutside(false);
		pd.setCancelable(cancelAble);
		pd.setOnCancelListener(new DialogInterface.OnCancelListener(){
			public void onCancel(DialogInterface dialog) {
				cancel(true);
			}
		});
		return setProgressDialog(pd);
	}
	public ResultAsyncTask<T> setOnSuccessRunnable(Runnable run){
		this.onSucRun=run;
		return this;
	}
	public ResultAsyncTask<T> setOnFailRunnable(Runnable run){
		this.onFailRun=run;
		return this;
	}
	/**设置是否显示操作成功或者失败的通知（没有设置OnFailRun,OnSucRun的时候） */
	public ResultAsyncTask<T> setToast(boolean isToast){
		this.isSucToast=isToast;
		this.isFailToast=isToast;
		return this;
	}
	/**设置是否显示操作成功或者失败的通知（没有设置OnFailRun,OnSucRun的时候） */
	public ResultAsyncTask<T> setToast(boolean isSucToast,boolean isFailToast){
		this.isSucToast=isSucToast;
		this.isFailToast=isFailToast;
		return this;
	}
	/**设置是否显示操作成功或者失败的通知（没有设置OnFailRun,OnSucRun的时候） */
	public ResultAsyncTask<T> setToast(String toast){
		return setToast(toast, toast);
	}
	/**
     * 设置是否显示操作成功或者失败的通知（没有设置OnFailRun,OnSucRun的时候）
     * @param sucToastRes 成功的提示
     * @param failToastRes 失败的提示
     */
	public ResultAsyncTask<T> setToast(int sucToastRes,int failToastRes){
		return setToast(context.getString(sucToastRes), context.getString(failToastRes));
	}
    /**
     * 设置是否显示操作成功或者失败的通知（没有设置OnFailRun,OnSucRun的时候）
     * @param sucToast 成功的提示，非空则强制开启提示，若为空提示字符为默认。
     * @param failToast 失败的提示，非空则强制开启提示，若为空提示字符为默认。
     */
	public ResultAsyncTask<T> setToast(String sucToast,String failToast){
		this.sucToast=sucToast;
		if(sucToast!=null) isSucToast=true;
		this.failToast=failToast;
		if(failToast!=null) isFailToast=true;
		return this;
	}
	public String getSucToast(){
		if(sucToast==null) return context.getString(R.string.Task_DoSuccess);
		else return sucToast;
	}
	public String getFailToast(){
		if(failToast==null) return context.getString(R.string.Task_LoadFail);
		else return failToast;
	}
	/**处理完后是否消失progressDialog，默认为true */
	public ResultAsyncTask<T> setDismissPd(boolean isDismissPd){
		this.isDismissPd=isDismissPd;
		return this;
	}
	@Override
	protected void onPostExecute(T result) {
		if(pd!=null && isDismissPd) pd.dismiss();
		if(result==null|| (result instanceof Boolean && !(Boolean)result) ){
			if(onFailRun==null&&isFailToast) Toast.makeText(context, getFailToast(), Toast.LENGTH_SHORT).show();
			if(onFailRun!=null) onFailRun.run();
			onPostExecuteFail(result);
		}else{
			if(onSucRun==null&&isSucToast) Toast.makeText(context, getSucToast(), Toast.LENGTH_SHORT).show();
			if(onSucRun!=null) onSucRun.run();
            onPostExecuteSuc(result);
		}
	}
    protected abstract void onPostExecuteSuc(T result);
    protected void onPostExecuteFail(T result){
    }
	
	@SuppressLint("NewApi")
	public void execute(){
		if(pd!=null&&!pd.isShowing()) pd.show();
		if(android.os.Build.VERSION.SDK_INT<11) super.execute();
		else executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
	}
}
