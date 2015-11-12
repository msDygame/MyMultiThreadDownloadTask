package com.dygame.mymultithreaddownloadtask;

import android.util.Log;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Administrator on 2015/11/11.
 */
public class SimpleTimerTestTask
{
    protected Timer pTimer = null ;
    protected int iTimer = 0 ;//下載遊戲 , 更新進度的間隔時間  , 單位:秒
    protected long lProgress = 0 ;//下載遊戲 , 遊戲下載進度 : 多少%
    protected long lCount = 0;
    final protected long lMax = 100 ;
    protected String sPackageName = "" ;
    protected AsyncTaskStatus enumStatus = AsyncTaskStatus.PENDING ;
    protected boolean isCancelled = false ;
    protected String TAG = "MyCrashHandler" ;

    public SimpleTimerTestTask()
    {
        enumStatus = AsyncTaskStatus.RUNNING ;
    }

    protected void doInBackground(String sPackage , int iSetTimer)
    {
        sPackageName = sPackage ;
        iTimer = iSetTimer ;

        while (true)
        {
            if (isCancelled())
            {
                Log.e(TAG, "DownTvGameAsyncTask , task is Cancelled");
                onPostExecute((long) 0);
                return;
            }
            if (pTimer == null)
            {
                pTimer = new Timer();
                TimerTask pTimerTask = new TimerTask()
                {
                    public void run()
                    {
                        Random rand = new Random();
                        int i = rand.nextInt(10) + 1 ;//1~10
                        lCount += i ;
                        lProgress = (lCount * 100) / lMax;
                        onProgressUpdate((int) lProgress);
                        String sMsg = sPackageName + "=PROGRESS:" + lProgress + "%" ;
                        Log.i(TAG, sMsg);
                    }
                };
                //單位:millonSecond
                pTimer.schedule(pTimerTask, 0, iTimer * 1000);
            }
            if (lCount >= lMax)
            {
                onPostExecute((long) 1);
                return ;
            }
        }
    }

    protected void onPostExecute(Long result)
    {
        if (pTimer != null)
        {
            pTimer.purge();
            pTimer.cancel();
            pTimer = null;
        }
        setStatus(AsyncTaskStatus.FINISHED) ;
    }

    protected void onPreExecute()
    {
    }

    protected void onProgressUpdate(Integer values)
    {
        MyCustomListviewAdapter.getInstance().setProgressByPackageName(sPackageName , (int)lProgress) ;
    }
    //UI
    public String getPackageName() { return sPackageName ; }
    public void cancel(boolean is) { isCancelled = is ;	}
    public boolean isCancelled() { return isCancelled ; }
    public AsyncTaskStatus getStatus() { return enumStatus ; }
    public void setStatus(AsyncTaskStatus enumAs) { enumStatus = enumAs ; }
    public long getProgress() { return lProgress ; }
}
