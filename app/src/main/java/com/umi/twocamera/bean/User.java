package com.umi.twocamera.bean;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.NotNull;
import org.greenrobot.greendao.annotation.Generated;

import java.io.Serializable;

/**
 * @author 郑州优米科技有限公司
 * @version ${VERSION}
 * 课程表
 * @date 2020/1/8.
 */
@Entity
public class User implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Id(autoincrement = true)
    private Long _id;
    private String name;
    private String cardId;
    @NotNull
    private String userId;
    private String headPhoto;
    private String time;
    private String department;
    private String face;
    private String userType;
    private String checkType;
    @Generated(hash = 1128863091)
    public User(Long _id, String name, String cardId, @NotNull String userId,
            String headPhoto, String time, String department, String face,
            String userType, String checkType) {
        this._id = _id;
        this.name = name;
        this.cardId = cardId;
        this.userId = userId;
        this.headPhoto = headPhoto;
        this.time = time;
        this.department = department;
        this.face = face;
        this.userType = userType;
        this.checkType = checkType;
    }
    @Generated(hash = 586692638)
    public User() {
    }
    public Long get_id() {
        return this._id;
    }
    public void set_id(Long _id) {
        this._id = _id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getCardId() {
        return this.cardId;
    }
    public void setCardId(String cardId) {
        this.cardId = cardId;
    }
    public String getUserId() {
        return this.userId;
    }
    public void setUserId(String userId) {
        this.userId = userId;
    }
    public String getHeadPhoto() {
        return this.headPhoto;
    }
    public void setHeadPhoto(String headPhoto) {
        this.headPhoto = headPhoto;
    }
    public String getTime() {
        return this.time;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public String getDepartment() {
        return this.department;
    }
    public void setDepartment(String department) {
        this.department = department;
    }
    public String getFace() {
        return this.face;
    }
    public void setFace(String face) {
        this.face = face;
    }
    public String getUserType() {
        return this.userType;
    }
    public void setUserType(String userType) {
        this.userType = userType;
    }
    public String getCheckType() {
        return this.checkType;
    }
    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }

}