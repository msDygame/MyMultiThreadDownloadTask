package com.dygame.mymultithreaddownloadtask;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Administrator on 2015/11/11.
 */
public class MyCustomListviewAdapter extends BaseAdapter
{
    protected Activity mActivity ;
    protected LayoutInflater inflater;
    protected Context mContext ;
    protected static MyCustomListviewAdapter _instance = null;
    protected ArrayList<InnerTaskInfo> fileList = new ArrayList<InnerTaskInfo>();
    protected String TAG = "" ;
    ImageView iv = null ;
    TextView tv = null ;
    Button button = null ;
    ProgressBar pb = null ;

    public MyCustomListviewAdapter()
    {

    }

    public void create(Activity activity , Context context , String sTAG)
    {
        mActivity = activity ;
        mContext = context ;
        inflater = LayoutInflater.from(context);
        TAG = sTAG ;
        fileList.clear();
        InnerTaskInfo fileList_1 = new InnerTaskInfo() ;
        fileList_1.setsPackageName("A20150100");
        fileList_1.setiProgress(0);
        fileList.add(fileList_1) ;

        InnerTaskInfo fileList_2 = new InnerTaskInfo() ;
        fileList_2.setsPackageName("A20150123");
        fileList_2.setiProgress(0);
        fileList.add(fileList_2) ;

        InnerTaskInfo fileList_3 = new InnerTaskInfo() ;
        fileList_3.setsPackageName("A20150125");
        fileList_3.setiProgress(0);
        fileList.add(fileList_3) ;

        InnerTaskInfo fileList_4 = new InnerTaskInfo() ;
        fileList_4.setsPackageName("A20150148");
        fileList_4.setiProgress(0);
        fileList.add(fileList_4) ;

        InnerTaskInfo fileList_5 = new InnerTaskInfo() ;
        fileList_5.setsPackageName("A20150151");
        fileList_5.setiProgress(0);
        fileList.add(fileList_5) ;

        InnerTaskInfo fileList_6 = new InnerTaskInfo() ;
        fileList_6.setsPackageName("A20150155");
        fileList_6.setiProgress(0);
        fileList.add(fileList_6) ;

        InnerTaskInfo fileList_7 = new InnerTaskInfo() ;
        fileList_7.setsPackageName("A201500157");
        fileList_7.setiProgress(0);
        fileList.add(fileList_7) ;

        InnerTaskInfo fileList_8 = new InnerTaskInfo() ;
        fileList_8.setsPackageName("A20150159");
        fileList_8.setiProgress(0);
        fileList.add(fileList_8) ;

        InnerTaskInfo fileList_9 = new InnerTaskInfo() ;
        fileList_9.setsPackageName("A20150161");
        fileList_9.setiProgress(0);
        fileList.add(fileList_9) ;
        notifyDataSetChanged() ;
    }

    //get self
    public static MyCustomListviewAdapter getInstance()
    {
        if(_instance != null)
        {
            return _instance;
        }
        synchronized (MyCustomListviewAdapter.class)
        {
            if(_instance == null)
            {
                _instance = new MyCustomListviewAdapter();
            }
        }
        return _instance;
    }

    @Override
    public int getCount()
    {
        return fileList.size();
    }

    @Override
    public Object getItem(int position)
    {
        return fileList.get(position);
    }

    @Override
    public long getItemId(int position)
    {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        if (convertView == null)
        {
            convertView = inflater.inflate(R.layout.listview_adapter_layout , null);
        }
        final int iInnerPosition = position ;
        //find resources
        iv = (ImageView)convertView.findViewById(R.id.ivLogo);
       //iv.setImageDrawable(arrayAppinfo.get(position).getAppIcon());
        tv = (TextView)convertView.findViewById(R.id.tvMessage);
        String str = (String)fileList.get(position).getsPackageName() ;
        final String sInnerStr = str ;
        tv.setText(str);
        button = (Button)convertView.findViewById(R.id.button1);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                TVGameDownloadManager.getInstance().executeDownloadTask(sInnerStr, "1", mContext) ;
                String str = "Pressed ListView(" + iInnerPosition + ")Button="+sInnerStr;
                Log.i(TAG, str);
            }
        });
        pb = (ProgressBar)convertView.findViewById(R.id.progressBarDownloadTask);
        int iProgress = (int)fileList.get(position).getiProgress() ;
        pb.setProgress(iProgress);
        return convertView;
    }

    public void setProgressByPackageName(String sPackage , int iProgress)
    {
        for (int i = 0 ;  i  <fileList.size() ; i++)
        {
            if (sPackage.equalsIgnoreCase( (String)fileList.get(i).getsPackageName() ))
            {
                Log.i(TAG, "setProgressByPackageName=" + iProgress + "," + sPackage);
                fileList.get(i).setiProgress(iProgress) ;
                mActivity.runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        notifyDataSetChanged();
                    }
                });
                break ;
            }
        }
    }
}

class InnerTaskInfo
{
    protected String sPackageName = "" ;
    protected int iProgress = 0 ;
    //UI
    public InnerTaskInfo()
    {
        sPackageName = "" ;
        iProgress = 0 ;
    }

    public String getsPackageName()
    {
        return sPackageName;
    }

    public void setsPackageName(String sPackageName)
    {
        this.sPackageName = sPackageName;
    }

    public int getiProgress()
    {
        return iProgress;
    }

    public void setiProgress(int iProgress)
    {
        this.iProgress = iProgress;
    }
}