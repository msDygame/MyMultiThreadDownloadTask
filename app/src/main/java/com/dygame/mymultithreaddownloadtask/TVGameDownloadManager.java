package com.dygame.mymultithreaddownloadtask;

import java.util.ArrayList;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.content.Context;
import android.util.Log;

public class TVGameDownloadManager
{
	private static TVGameDownloadManager _instance;
	// 紀錄目前使用的MyAdapter
	private ExecutorService pool;
	private CompletionService mEcs;
	private ArrayList<TVGameDownloadTask> aryTask = new ArrayList<TVGameDownloadTask>();
	protected Context pContext = null ;
	protected String TAG = "MyCrashHandler" ;

	public static TVGameDownloadManager getInstance()
	{
		if(_instance != null)
		{
			return _instance;
		}
		
		synchronized (TVGameDownloadManager.class)
		{
			if(_instance == null)
			{
				_instance = new TVGameDownloadManager();
			}
		}
		return _instance;
	}
	
	private TVGameDownloadManager()
	{
		// 最多同時3條Thred執行,超過則等前面執行完會接下去執行
		pool = Executors.newFixedThreadPool(3);//創建一個有固定大小的線程池。 
		//A CompletionService that uses a supplied Executor to execute tasks. This class arranges that submitted tasks are, upon completion, placed on a queue accessible using take. The class is lightweight enough to be suitable for transient use when processing groups of tasks.
		mEcs = new ExecutorCompletionService(pool);//通過Executor提交一組並發執行的任務，並且希望在每一個任務完成後能立即得到結果
	}
	
	public void executeDownloadTask(String sPackage , String sTimer , Context pContext)
	{
		for(TVGameDownloadTask pTask : aryTask)
		{
			if (sPackage.equalsIgnoreCase(pTask.getPackageNameEx()))
			{
				//需要防呆,防止下載多次
				if (pTask.isTaskNull() == false) return ;//尚未FINISHED,超過3個Thread下載還未開始
				Log.i(TAG , "TVGameDownloadManager executeDownloadTask arrayList size= "+aryTask.size());
				aryTask.remove(pTask) ;//移除FINISHED
				break ;//同PackageName只做一次
			}
		}
		TVGameDownloadTask dTask = new TVGameDownloadTask(sPackage,sTimer,pContext) ;
		//Submits a value-returning task for execution and returns a Future representing the pending results of the task. Upon completion, this task may be taken or polled.
		mEcs.submit(dTask);//提交要執行的值返回任務，並返回表示掛起的任務結果的 Future。在完成時，可能會提取或輪詢此任務。
		aryTask.add(dTask);
		Log.i(TAG , "TVGameDownloadManager executeDownloadTask sumit = "+sPackage);
	}

	/**
	 * 停止正在執行中的AsyncTask
	 */
	public void cancelDownloadTask(String sPackageName)
	{
		Log.i(TAG , "TVGameDownloadManager , StopDownload PackageName arrayList size=" + aryTask.size());
		for(TVGameDownloadTask pTask : aryTask)
		{
			Log.i(TAG , "TVGameDownloadManager , cancel Download task , Package name :" + sPackageName + ",Task PackageName=" + pTask.getPackageName());
			if (sPackageName.equalsIgnoreCase(pTask.getPackageName()))
			{
				//停止正在執行中的AsyncTask
				pTask.cancelTask();
				Log.i(TAG , "TVGameDownloadManager , cancel Download task , Package name :" + sPackageName);
			}
			else
			{
				//正在下載的遊戲  跟  要停止下載的遊戲 不同
				Log.i(TAG , "TVGameDownloadManager , StopDownload PackageName fail");
			}
		}
	}
	
	/**
	 * 停止任務執行服務    
	 */
	public void shutdown(boolean isImmediately)
	{
		if (isImmediately == true)
			pool.shutdownNow();//該方法將取消尚未開始的所有任務，並立即終止正在運行的線程，自行滅亡。
		else
			pool.shutdown() ;//啟動線程池的關閉序列，該線程池將不會再接受任務請求，而且會在等待正在運行的所有線程運行結束後，殺死所有線程。		
	}

	public int getProgress(String sPackageName)
	{
		Log.i(TAG , "TVGameDownloadManager , StopDownload PackageName arrayList size=" + aryTask.size());
		for(TVGameDownloadTask pTask : aryTask)
		{
			Log.i(TAG , "TVGameDownloadManager , cancel Download task , Package name :" + sPackageName + ",Task PackageName=" + pTask.getPackageName());
			if (sPackageName.equalsIgnoreCase(pTask.getPackageName()))
			{
				return (pTask.getTaskProgress() );
			}
			else
			{
				Log.i(TAG , "here return 0=no packagename" );
				return 0 ;
			}
		}
		Log.i(TAG , "here return 0=no TVGameDownloadTask" );
		return 0 ;
	}
}