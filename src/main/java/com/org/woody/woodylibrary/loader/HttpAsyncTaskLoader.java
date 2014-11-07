package com.org.woody.woodylibrary.loader;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.concurrent.CountDownLatch;

import org.apache.http.client.methods.HttpRequestBase;

import com.org.woody.woodylibrary.task.GsonAsyncTask;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.content.Loader;
import android.support.v4.util.TimeUtils;
import android.util.Log;

public class HttpAsyncTaskLoader<D> extends Loader<D> {

    static final boolean DEBUG = false;
    static final String TAG = "HttpAsyncTaskLoader";
    
    volatile LoadTask mTask;
    volatile LoadTask mCancellingTask;
    private HttpRequestBase request;
    private String url;

    long mUpdateThrottle;
    long mLastLoadCompleteTime = -10000;
    Handler mHandler;
	public HttpAsyncTaskLoader(Context context) {
		super(context);
		mTask = new LoadTask(context);
	}
	public HttpAsyncTaskLoader(Context context, HttpRequestBase request) {
		super(context);
		mTask = new LoadTask(context,request);
	}
	public HttpAsyncTaskLoader(Context context, String url) {
		super(context);
		mTask = new LoadTask(context,url);
	}

	class LoadTask extends GsonAsyncTask<D> implements Runnable{
		
		D result;
        boolean waiting;
        private CountDownLatch done = new CountDownLatch(1);
        
		public LoadTask(Context context) {
			super(context);
		}

		public LoadTask(Context context, HttpRequestBase request) {
			super(context, request);
		}

		public LoadTask(Context context, String url) {
			super(context, url);
		}
		
		@Override
        protected void onCancelled() {
            if (DEBUG) Log.v(TAG, this + " onCancelled");
            try {
            	HttpAsyncTaskLoader.this.dispatchOnCancelled(this, result);
            } finally {
                done.countDown();
            }
        }

		@Override
		protected void onPostExecuteSuc(D result) {
			if (DEBUG) Log.v(TAG, this + " onPostExecute");
            try {
            	HttpAsyncTaskLoader.this.dispatchOnLoadComplete(this, result);
            } finally {
                done.countDown();
            }
		}

		@Override
		public void run() {
			waiting = false;
			HttpAsyncTaskLoader.this.executePendingTask();
		}
	}
	
	
	
	/**
     * Set amount to throttle updates by.  This is the minimum time from
     * when the last {@link #onLoadInBackground()} call has completed until
     * a new load is scheduled.
     *
     * @param delayMS Amount of delay, in milliseconds.
     */
    public void setUpdateThrottle(long delayMS) {
        mUpdateThrottle = delayMS;
        if (delayMS != 0) {
            mHandler = new Handler();
        }
    }

    @Override
    protected void onForceLoad() {
        super.onForceLoad();
        cancelLoad();
        if (url != null) {
        	mTask = new LoadTask(getContext(),url);
		}else if (request != null) {
			mTask = new LoadTask(getContext(),request);
		}
        if (DEBUG) Log.v(TAG, "Preparing load: mTask=" + mTask);
        executePendingTask();
    }

    /**
     * Attempt to cancel the current load task. See {@link android.os.AsyncTask#cancel(boolean)}
     * for more info.  Must be called on the main thread of the process.
     *
     * <p>Cancelling is not an immediate operation, since the load is performed
     * in a background thread.  If there is currently a load in progress, this
     * method requests that the load be cancelled, and notes this is the case;
     * once the background thread has completed its work its remaining state
     * will be cleared.  If another load request comes in during this time,
     * it will be held until the cancelled load is complete.
     *
     * @return Returns <tt>false</tt> if the task could not be cancelled,
     *         typically because it has already completed normally, or
     *         because {@link #startLoading()} hasn't been called; returns
     *         <tt>true</tt> otherwise.
     */
    public boolean cancelLoad() {
        if (DEBUG) Log.v(TAG, "cancelLoad: mTask=" + mTask);
        if (mTask != null) {
            if (mCancellingTask != null) {
                // There was a pending task already waiting for a previous
                // one being canceled; just drop it.
                if (DEBUG) Log.v(TAG,
                        "cancelLoad: still waiting for cancelled task; dropping next");
                if (mTask.waiting) {
                    mTask.waiting = false;
                    mHandler.removeCallbacks(mTask);
                }
                mTask = null;
                return false;
            } else if (mTask.waiting) {
                // There is a task, but it is waiting for the time it should
                // execute.  We can just toss it.
                if (DEBUG) Log.v(TAG, "cancelLoad: task is waiting, dropping it");
                mTask.waiting = false;
                mHandler.removeCallbacks(mTask);
                mTask = null;
                return false;
            } else {
                boolean cancelled = mTask.cancel(false);
                if (DEBUG) Log.v(TAG, "cancelLoad: cancelled=" + cancelled);
                if (cancelled) {
                    mCancellingTask = mTask;
                }
                mTask = null;
                return cancelled;
            }
        }
        return false;
    }
	
	
	void executePendingTask() {
        if (mCancellingTask == null && mTask != null) {
            if (mTask.waiting) {
                mTask.waiting = false;
                mHandler.removeCallbacks(mTask);
            }
            if (mUpdateThrottle > 0) {
                long now = SystemClock.uptimeMillis();
                if (now < (mLastLoadCompleteTime+mUpdateThrottle)) {
                    // Not yet time to do another load.
                    if (DEBUG) Log.v(TAG, "Waiting until "
                            + (mLastLoadCompleteTime+mUpdateThrottle)
                            + " to execute: " + mTask);
                    mTask.waiting = true;
                    mHandler.postAtTime(mTask, mLastLoadCompleteTime+mUpdateThrottle);
                    return;
                }
            }
            if (DEBUG) Log.v(TAG, "Executing: " + mTask);
            mTask.setProgressDialog().execute();
        }
    }
	public void dispatchOnLoadComplete(LoadTask task, D data) {
		if (mTask != task) {
            if (DEBUG) Log.v(TAG, "Load complete of old task, trying to cancel");
            dispatchOnCancelled(task, data);
        } else {
            if (isAbandoned()) {
                // This cursor has been abandoned; just cancel the new data.
                onCanceled(data);
            } else {
                commitContentChanged();
                mLastLoadCompleteTime = SystemClock.uptimeMillis();
                mTask = null;
                if (DEBUG) Log.v(TAG, "Delivering result");
                deliverResult(data);
            }
        }
		
	}
	void dispatchOnCancelled(LoadTask task, D data) {
		 onCanceled(data);
        if (mCancellingTask == task) {
            if (DEBUG) Log.v(TAG, "Cancelled task is now canceled!");
            rollbackContentChanged();
            mLastLoadCompleteTime = SystemClock.uptimeMillis();
            mCancellingTask = null;
            executePendingTask();
        }
	}
	public void onCanceled(D data) {
	}
	
	/**
     * Locks the current thread until the loader completes the current load
     * operation. Returns immediately if there is no load operation running.
     * Should not be called from the UI thread: calling it from the UI
     * thread would cause a deadlock.
     * <p>
     * Use for testing only.  <b>Never</b> call this from a UI thread.
     *
     * @hide
     */
    public void waitForLoader() {
        LoadTask task = mTask;
        if (task != null) {
            try {
                task.done.await();
            } catch (InterruptedException e) {
                // Ignore
            }
        }
    }

    @Override
    public void dump(String prefix, FileDescriptor fd, PrintWriter writer, String[] args) {
        super.dump(prefix, fd, writer, args);
        if (mTask != null) {
            writer.print(prefix); writer.print("mTask="); writer.print(mTask);
                    writer.print(" waiting="); writer.println(mTask.waiting);
        }
        if (mCancellingTask != null) {
            writer.print(prefix); writer.print("mCancellingTask="); writer.print(mCancellingTask);
                    writer.print(" waiting="); writer.println(mCancellingTask.waiting);
        }
        if (mUpdateThrottle != 0) {
            writer.print(prefix); writer.print("mUpdateThrottle=");
                    TimeUtils.formatDuration(mUpdateThrottle, writer);
                    writer.print(" mLastLoadCompleteTime=");
                    TimeUtils.formatDuration(mLastLoadCompleteTime,
                            SystemClock.uptimeMillis(), writer);
                    writer.println();
        }
    }
}
