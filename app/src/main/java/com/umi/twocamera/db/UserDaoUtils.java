package com.umi.twocamera.db;

import android.content.Context;
import android.util.Log;

import com.umi.greendao.gen.UserDao;
import com.umi.greendao.gen.DaoMaster;
import com.umi.twocamera.bean.User;

import org.greenrobot.greendao.query.QueryBuilder;

import java.util.List;

/**
 * @author 郑州优米科技有限公司
 * @version ${VERSION}
 * 修改密码activity
 * @date 2020/1/8.
 */
public class UserDaoUtils {
    private static final String TAG = UserDaoUtils.class.getSimpleName();
    private DaoManager mManager;

    public UserDaoUtils(Context context){
        mManager = DaoManager.getInstance();
        mManager.init(context);
    }

    /**
     * 完成user记录的插入，如果表未创建，先创建User表
     * @param user
     * @return
     */
    public boolean insertUser(User user){
        boolean flag = false;
        flag = mManager.getDaoSession().getUserDao().insertOrReplace(user) == -1 ? false : true;
        Log.i(TAG, "insert User :" + flag + "-->" + user.toString());
        return flag;
    }

    /**
     * 插入多条数据，在子线程操作
     * @param userList
     * @return
     */
    public boolean insertMultUser(final List<User> userList) {
        boolean flag = false;
        try {
            mManager.getDaoSession().runInTx(new Runnable() {
                @Override
                public void run() {
                    for (User user : userList) {
                        mManager.getDaoSession().insertOrReplace(user);
                    }
                }
            });
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 修改一条数据
     * @param user
     * @return
     */
    public boolean updateUser(User user){
        boolean flag = false;
        try {
            mManager.getDaoSession().update(user);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除单条记录
     * @param user
     * @return
     */
    public boolean deleteUser(User user){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().delete(user);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 删除所有记录
     * @return
     */
    public boolean deleteAll(){
        boolean flag = false;
        try {
            //按照id删除
            mManager.getDaoSession().deleteAll(User.class);
            flag = true;
        }catch (Exception e){
            e.printStackTrace();
        }
        return flag;
    }

    /**
     * 查询所有记录
     * @return
     */
    public List<User> queryAllUser(){
        return mManager.getDaoSession().loadAll(User.class);
    }

    /**
     * 根据主键id查询记录
     * @param key
     * @return
     */
    public User queryUserById(long key){
        return mManager.getDaoSession().load(User.class, key);
    }

    /**
     * 使用native sql进行查询操作
     */
    public List<User> queryUserByNativeSql(String sql, String[] conditions){
        return mManager.getDaoSession().queryRaw(User.class, sql, conditions);
    }

    /**
     * 使用queryBuilder进行查询
     * @return
     */
    public List<User> queryUserByQueryBuilder(long id){
        QueryBuilder<User> queryBuilder = mManager.getDaoSession().queryBuilder(User.class);
        return queryBuilder.where(UserDao.Properties._id.eq(id)).list();
    }
    public List<User> queryUser(int offset){
        QueryBuilder<User> queryBuilder = mManager.getDaoSession().queryBuilder(User.class);
        return queryBuilder.offset(offset * 20).limit(20).list();
    }
    public List<User> queryUserId(String userId,int offset){
        QueryBuilder<User> queryBuilder = mManager.getDaoSession().queryBuilder(User.class);
        return queryBuilder.where(UserDao.Properties.UserId.eq(userId)).offset(offset * 20).limit(20).list();
    }
    public List<User> queryCardId(String cardId,int offset){
        QueryBuilder<User> queryBuilder = mManager.getDaoSession().queryBuilder(User.class);
        return queryBuilder.where(UserDao.Properties.CardId.eq(cardId)).offset(offset * 20).limit(20).list();
    }
    public List<User> queryName(String name,int offset){
        QueryBuilder<User> queryBuilder = mManager.getDaoSession().queryBuilder(User.class);
        return queryBuilder.where(UserDao.Properties.Name.eq(name)).offset(offset * 20).limit(20).list();
    }
}