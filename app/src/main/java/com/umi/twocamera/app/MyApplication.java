package com.umi.twocamera.app;

/**
 * @author 郑州优米科技有限公司
 * @version ${VERSION}
 * 修改密码activity
 * @date 2020/1/8.
 */

import android.app.Application;

import com.umi.greendao.gen.UserDao;
import com.umi.twocamera.bean.Record;
import com.umi.twocamera.bean.User;
import com.umi.twocamera.db.DaoManager;
import com.umi.twocamera.db.RecordDaoUtils;
import com.umi.twocamera.db.UserDaoUtils;
import com.umi.twocamera.utils.CarshHandler;
import com.umi.twocamera.utils.DatesUtil;

import java.io.File;

public class MyApplication extends Application
{
    private static MyApplication instance ;

    public static MyApplication getInstance(){
        return instance;
    }
    private UserDaoUtils mUserDaoUtils;
    private RecordDaoUtils mRecordDaoUtils;
    @Override
    public void onCreate()
    {
        super.onCreate();
        //处理初始化应用carsh
        CarshHandler crashHandler = CarshHandler.getIntance();
        crashHandler.init(getApplicationContext());
        if(!new File(Const.filePath).exists())new File(Const.filePath).mkdirs();
        initGreenDao();
        insertData();
        instance=this;
    }

    private void insertData() {
        mUserDaoUtils=new UserDaoUtils(this);
        mRecordDaoUtils=new RecordDaoUtils(this);
        if(mRecordDaoUtils.queryAllRecord().size()==0){
            Record record=new Record(null,"name","cardid","userid","picture", DatesUtil.getBeginDayOfLastMonth().getTime(),"department",0,0,0);
            Record record2=new Record(null,"name","cardid","userid","picture", DatesUtil.getBeginDayOfLastWeek().getTime(),"department",0,0,0);
            Record record3=new Record(null,"name","cardid","userid","picture", DatesUtil.getBeginDayOfYesterday().getTime(),"department",0,0,0);
            Record record4=new Record(null,"name","cardid","userid","picture", DatesUtil.getBeginDayOfMonth().getTime(),"department",0,0,0);
            Record record5=new Record(null,"name","cardid","userid","picture", DatesUtil.getBeginDayOfWeek().getTime(),"department",0,0,0);
            mRecordDaoUtils.insertRecord(record);
            mRecordDaoUtils.insertRecord(record2);
            mRecordDaoUtils.insertRecord(record3);
            mRecordDaoUtils.insertRecord(record4);
            mRecordDaoUtils.insertRecord(record5);
        }
        if(mUserDaoUtils.queryAllUser().size()==0){
            User user=new User(null,"name","123","userid","headphoto","time","department","face","","");
            mUserDaoUtils.insertUser(user);
        }
    }

    private void initGreenDao()
    {
        DaoManager mManager = DaoManager.getInstance();
        mManager.init(this);
    }
}
