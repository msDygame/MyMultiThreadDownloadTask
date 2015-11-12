package com.dygame.mymultithreaddownloadtask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

enum AsyncTaskStatus
{
	FINISHED ,
	RUNNING ,
	PENDING
}
/**
 * 由 AsyncTask 改來的 @20151015 ,
 * 因為 Executors.newFixedThreadPool(3) , ExecutorCompletionService().submit() 似乎不能讓  DownloadTVGameAsyncTask 同時執行3個Thread...
 */
public class DownloadTVGameAsyncTask
{
	protected Timer pTimer = null ;
	protected int iTimer = 0 ;//下載遊戲 , 更新進度的間隔時間  , 單位:秒
	protected long lProgress = 0 ;//下載遊戲 , 遊戲下載進度 : 多少%
	protected String sPackageName = "" ;
	protected File file = null ;
	protected Context inContext = null ;
	protected boolean isCancelled = false ;
	protected AsyncTaskStatus enumStatus = AsyncTaskStatus.PENDING ;
	protected long lCount = 0;
	protected String TAG = "MyCrashHandler" ;
	
	public DownloadTVGameAsyncTask(Context context)
	{
		inContext = context ;
		enumStatus = AsyncTaskStatus.RUNNING ;
	}
	protected void doInBackground(String sPackage , String sTimer)
	{
		if (true == TVGameDownloadTask.stringIsNullOrEmpty(sPackage))
		{
			onPostExecute((long)-2 );// is null or empty
			return ;
		}
		sPackageName = sPackage ;
		//來源檔案
		String sURL = "http://download.aiwi-game.com.cn/dygameunity/dygame/android/029/" + sPackage + ".apk";
		//下載遊戲 , 更新進度的間隔時間  , 單位:秒
		Log.i(TAG, "GameDownload, sParams=" + sPackage + "," + sTimer);
		try
		{
			iTimer = Integer.parseInt(sTimer);
		}
		catch (NumberFormatException e)
		{
			Log.i(TAG, "MbDownloadApp, parseInt Exception");
		}

		//download apk
		HttpClient client = new DefaultHttpClient();
    	HttpGet get = new HttpGet(sURL);
		Log.i(TAG , "GameDownload, source file=" + sURL);
		//檔案路徑
		String sAPKSaveDir = "" ;
		if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			sAPKSaveDir = Environment.getExternalStorageDirectory().getAbsolutePath() ;
		}
		else
		{
			sAPKSaveDir = inContext.getFilesDir().getAbsolutePath();
		}
		//目的檔案
		String sAPKFileName = sPackageName + ".apk";
		file = new File(sAPKSaveDir,sAPKFileName);
		boolean bResumeDownload = false;//繼續下載
		Log.i(TAG,"GameDownload, target file=" + file.toString());
		long lTempFileLength = file.length();
		long lResumeStart = 0;
		Log.i(TAG , "GameDownload, target file lFileLength =" + lTempFileLength);
		if(lTempFileLength > 0)
		{
			//曾經下載過  繼續下載
			lResumeStart = lTempFileLength - 1;
			get.addHeader("Range", "bytes="+lResumeStart+"-");
			bResumeDownload = true;
		}

		Log.i(TAG , "GameDownload::response ");
		HttpResponse response = null;
		try
		{
			response = client.execute(get);
			Log.i(TAG , "GameDownload::response get.. ");
		}
		catch (ClientProtocolException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		int statusCode = response.getStatusLine().getStatusCode();
		Log.i(TAG , "GameDownload::statusCode " + statusCode);
		if(statusCode == 404)
		{
			onPostExecute((long) -3) ;
			return ;
		}
		//連線失敗
		if((statusCode!=200)&&(statusCode!=201)&&(statusCode!=202)&&(statusCode!=206))
		{
			onPostExecute((long) -4) ;
			return ;
    	}
		if(bResumeDownload == true)
		{
			if(response.getStatusLine().getStatusCode() == HttpStatus.SC_PARTIAL_CONTENT)//HttpStatus.SC_PARTIAL_CONTENT=206
			{
				bResumeDownload = true;
				lCount = lTempFileLength;
			}
			else
			{
				//不斷點續傳
				bResumeDownload = false;
				if(file.exists())
				{
					file.delete();
				}
				get.removeHeaders("Range");
				try
				{
					response = client.execute(get);
				}
				catch (ClientProtocolException e)
				{
					e.printStackTrace();
				}
				catch (IOException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		HttpEntity entity = response.getEntity();
		Long lFileLength = entity.getContentLength();
		Log.i(TAG , "DownloadGameZoneTask::FileSize = " + lFileLength);

		InputStream is = null;
		try
		{
			is = entity.getContent();
		}
		catch (IllegalStateException e1)
		{
			e1.printStackTrace();
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
		}
		if (is == null)
		{
			Log.e(TAG , "GameDownload , InputStream is null");
			onPostExecute((long) -5) ;
			return ;
		}		
/*
		Utility.CalcMemorySize(inContext);
		if( Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
		{
			if (lFileLength >= Utility.SIZE_FREE_SD_CARD)
			{
				onPostExecute((long) -6) ;
				return ;
			}
		}
		else
		{
			if (lFileLength >= Utility.SIZE_FREE_RAM)
			{
				onPostExecute((long) -7);
				return ;
			}
		}
*/
		RandomAccessFile fileOutputStream = null ;
		try
		{
			fileOutputStream = new RandomAccessFile(file, "rw");
		}
		catch (FileNotFoundException e2)
		{
			e2.printStackTrace();
			Log.i(TAG ,"FileNotFoundException: " + file.toString());
		}  
		if (bResumeDownload == true)
		{
			try
			{
				fileOutputStream.seek(lResumeStart);
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
			if (lCount > 0)
			{
				lFileLength = lFileLength + lCount;
				Log.i(TAG , "DownloadGameZoneTask::FileSize(ResumeDownload) = " + lCount + "/" + lFileLength);
				long lTemp = ((lCount) * 100) / lFileLength ;
				onProgressUpdate((int) lTemp);
			}
		}
		
		byte[] buf = new byte[1024];
		int ch = -1;
		try
		{
			Log.i(TAG , "fileOutputStream writing...");
			Log.i(TAG , "DownTvGameAsyncTask publishProgress Timer="+iTimer+"sec.");
			Log.i(TAG , "DownTvGameAsyncTask publishProgress Total="+lFileLength.intValue());
			while ((ch = is.read(buf)) != -1)
			{
				if (isCancelled())
				{
					if (fileOutputStream != null)
					{
						fileOutputStream.close();
					}
					Log.e(TAG , "DownTvGameAsyncTask , task is Cancelled");
					onPostExecute((long) 0);
					return ;
				}
				fileOutputStream.write(buf, 0, ch);
				lCount += (long)ch;
				lProgress = (lCount * 100) / lFileLength ;
				
				if (pTimer == null)
				{
					pTimer = new Timer();
					TimerTask pTimerTask = new TimerTask()
					{
						public void run()
						{
							onProgressUpdate((int)lProgress);
							Log.i(TAG , "DownTvGameAsyncTask publishProgress count="+lProgress+"%");
						}			
					};
					//單位:millonSecond
					pTimer.schedule(pTimerTask, 0 , iTimer*1000) ;
				}
			}
		}
		catch (IOException e1)
		{
			e1.printStackTrace();
			Log.i(TAG , "fileOutputStream.write IOException");
		}
		Log.i(TAG , "TvGame download Done");
		if (lCount >= lFileLength)
			onPostExecute((long) 1);
		else
			onPostExecute((long)-1);//IOException happen?
	}

	protected void onPostExecute(Long result)
	{
		if (pTimer != null)
		{
			pTimer.purge();
			pTimer.cancel();
			pTimer = null;
		}
		Log.i(TAG , "DownTvGameAsyncTask onPostExecute result="+result);
		//
		if (result > 0)//成功
		{
			//安裝
			installPackage(inContext , file) ;
/*			//Fix , 尚未安裝完成就已刪檔...
			//安裝後刪掉暫存檔
			if (file.exists()){
				Log.i("MyCrashHandler ",file.getAbsolutePath() + " file Exists");
				if(file.delete()){
					Log.i("MyCrashHandler ",file.getAbsolutePath() + " file delete");
				}
			}
*/					
		}
		else if (result == 0) //中止
		{
			//遊戲暫停下載 : 目前進度多少%
			String sMsg = sPackageName + " PROGRESS:" + lProgress + "%" ;
			Log.i(TAG ,sMsg);
		}
		else //失敗組
		{
    		String str = "" ;
    		     if (result == -1) str = "fileOutputStream.write IOException." ;
    		else if (result == -2) str = "PackageName or GameId has error." ;
    		else if (result == -3) str = "HttpResponse.getStatusCode = 404." ;
    		else if (result == -4) str = "HttpResponse statius != 200|201|202|206." ;
    		else if (result == -5) str = "HttpResponse.getEntity.getContent (InputStream) is null." ;
    		else if (result == -6) str = "DISK_NOT_ENOUGH." ;
    		else if (result == -7) str = "MEMORY_NOT_ENOUGH." ;
    		else                   str = "Unknown error." ;
    		//遊戲下載失敗 : 失敗原因
			String sMsg = "ERROR_CODE:" + result ;
			Log.i(TAG ,sMsg);
    	}
		//
		setStatus(AsyncTaskStatus.FINISHED) ;
	}

	protected void onPreExecute()
	{
	}

	protected void onProgressUpdate(Integer values)
	{
		String sMsg = sPackageName + " PROGRESS:" + values + "%" ;
		Log.i(TAG , sMsg);
	}	
	
	protected void onCancelled()
	{
		if (pTimer != null)
		{
			pTimer.purge();
			pTimer.cancel();
			pTimer = null;
		}
	}
	//UI
	public String getPackageName() { return sPackageName ; }
	public void cancel(boolean is) { isCancelled = is ;	}
	public boolean isCancelled() { return isCancelled ; }
	public AsyncTaskStatus getStatus() { return enumStatus ; }
	public void setStatus(AsyncTaskStatus enumAs) { enumStatus = enumAs ; }
	//安裝 App(InstallAPK)
	public void installPackage(Context inContext , File file)
	{
		Intent intent = new Intent();
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		intent.setAction(android.content.Intent.ACTION_VIEW);//安裝
		intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
		inContext.startActivity(intent);
	}
}
