package com.dygame.mymultithreaddownloadtask;

import java.util.concurrent.Callable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.util.Log;

public class TVGameDownloadTask implements Callable<String>
{
   //protected DownloadTVGameAsyncTask downloadTVGameAsyncTask = null ;//下載遊戲, 並會定時回傳 遊戲下載進度 : 多少% 給手柄 //20151111@android 6.0似乎不能用HttpClient.execute(get);
	protected SimpleTimerTestTask downloadTVGameAsyncTask = null ;
	protected String sPackageName = "" ;//default
	protected String sTimer = "1" ;//default , update progress per 1 second
	protected Context mContext = null ;
	protected String TAG = "MyCrashHandler" ;

	public TVGameDownloadTask(String sParamPackage , String sParamTimer , Context context)
	{
		sPackageName = sParamPackage ;
		sTimer = sParamTimer ;
		mContext = context ;
	}
	
	@Override
	public String call() throws Exception
	{
		Log.i(TAG , "TVGameDownloadTask Callable call().");
		//防呆
		if (true == stringIsNullOrEmpty(sPackageName))
		{
			Log.i("DYCommand_TCP_DEBUG", "TVGameDownloadTask sPackageName is null or empty.");
			return "0" ;
		}
		//需要防呆,防止下載多次
		if (isTaskFinish() == false) return "0";	
	   //downloadTVGameAsyncTask = new DownloadTVGameAsyncTask(mContext);//20151111@android 6.0似乎不能用HttpClient.execute(get);
		downloadTVGameAsyncTask = new SimpleTimerTestTask() ;
		downloadTVGameAsyncTask.doInBackground(sPackageName , 1) ;//downloadTVGameAsyncTask.execute(sPackageName , sTimer);
		Log.i(TAG , "TVGameDownloadTask execute = "+sPackageName);
		return null;
	}
	
	public void cancelTask()
	{
		if (downloadTVGameAsyncTask == null)
		{
			Log.i(TAG , "TVGameDownloadTask , cancel Download task fail , Package name :" + sPackageName + " Task is null");
			return ;
		}
		else
		{
			if (downloadTVGameAsyncTask.getStatus() != AsyncTaskStatus.FINISHED)
			{
				downloadTVGameAsyncTask.cancel(true) ;
			}
			else
			{
				Log.i(TAG , "TVGameDownloadTask , cancel Download task fail , Package name :" + sPackageName + " is Task.FINISH");
			}
		}
	}
	
	public String getPackageName()
	{
		//沒有任何遊戲在下載
		if (downloadTVGameAsyncTask == null)
		{
			return "" ;
		}
		else
		{
			//正在下載的遊戲
			return (downloadTVGameAsyncTask.getPackageName()) ;
		}
	}

	public String getPackageNameEx()
	{
		return sPackageName ;//20151111@fix
	}
	
	public boolean isTaskFinish()
	{
		if (downloadTVGameAsyncTask == null)
		{
			return true ;//將建構,null可
		}
		else
		{
			if (downloadTVGameAsyncTask.getStatus() != AsyncTaskStatus.FINISHED)
			{
				Log.i(TAG , "TVGameDownloadTask downloadTVGameAsyncTask is not finished.");
				return false ;//PENDING,RUNNING
			}
			else
			{
				return true ;//FINISHED
			}
		}
	}
	
	public boolean isTaskNull()
	{
		if (downloadTVGameAsyncTask == null)
		{
			return false ;//下載還未開始,防止下載多次
		}
		else
		{
			if (downloadTVGameAsyncTask.getStatus() != AsyncTaskStatus.FINISHED)
			{
				Log.i(TAG , "TVGameDownloadTask downloadTVGameAsyncTask is not finished.");
				return false ;//PENDING,RUNNING
			}
			else
			{
				return true ;//FINISHED
			}
		}
	}

	public int getTaskProgress()
	{
		if (downloadTVGameAsyncTask == null)
		{
			Log.i(TAG , "here return 0=downloadTVGameAsyncTask is null" );
			return 0 ;
		}
		else
		{
			return ((int)downloadTVGameAsyncTask.getProgress()) ;
		}
	}

	/**
	 * 仿C# 的 String.IsNullOrEmpty()
	 */
	@SuppressLint("NewApi")
	static public boolean stringIsNullOrEmpty(String sTarget)
	{
		if (Build.VERSION.SDK_INT >= 9)
		{
			if(sTarget != null && !sTarget.isEmpty())//isEmpty() is ApiLevel 9
			{
				return false ;
			}
			else
			{
				return true;
			}
		}
		else
		{
			if(sTarget != null && !sTarget.equals(""))
			{
				return false ;
			}
			else
			{
				return true;
			}
		}
	}
}
