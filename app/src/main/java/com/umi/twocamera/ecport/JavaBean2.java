package com.umi.twocamera.ecport;

/**
 * @author 郑州优米科技有限公司
 * @version ${VERSION}
 * 修改密码activity
 * @date 2019/8/29.
 */
public class JavaBean2 {
   private String name;
   private String userId;
   private String cardId;
   private String department;
   private String time;
   private String type;
   private String checkType;

    public JavaBean2(String name, String userId, String cardId, String department, String time, String type, String checkType) {
        this.name = name;
        this.userId = userId;
        this.cardId = cardId;
        this.department = department;
        this.time = time;
        this.type = type;
        this.checkType = checkType;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCheckType() {
        return checkType;
    }

    public void setCheckType(String checkType) {
        this.checkType = checkType;
    }
}
