package com.umi.twocamera.ecport;

/**
 * @author 郑州优米科技有限公司
 * @version ${VERSION}
 * 修改密码activity
 * @date 2019/8/29.
 */
public class JavaBean3 {
   private String name;
   private String userId;
   private String cardId;
   private String department;
   private String time;
   private String isUp;
   private String upTime;
   private String failedNum;

    public JavaBean3(String name, String userId, String cardId, String department, String time, String isUp, String upTime, String failedNum) {
        this.name = name;
        this.userId = userId;
        this.cardId = cardId;
        this.department = department;
        this.time = time;
        this.isUp = isUp;
        this.upTime = upTime;
        this.failedNum = failedNum;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCardId() {
        return cardId;
    }

    public void setCardId(String cardId) {
        this.cardId = cardId;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIsUp() {
        return isUp;
    }

    public void setIsUp(String isUp) {
        this.isUp = isUp;
    }

    public String getUpTime() {
        return upTime;
    }

    public void setUpTime(String upTime) {
        this.upTime = upTime;
    }

    public String getFailedNum() {
        return failedNum;
    }

    public void setFailedNum(String failedNum) {
        this.failedNum = failedNum;
    }
}
