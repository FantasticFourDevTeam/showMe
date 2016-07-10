package com.example.FundigoApp.Chat;

import java.util.Date;

public class MessageChat {
    public final static int MSG_TYPE_TEXT = 0;
    public final static int MSG_STATE_SUCCESS = 1;

    private Long id;
    private Integer type;
    private Integer state;
    private String fromUserName;
    private String content;

    private Boolean isSend;
    private Boolean sendSucces;
    private Date time;

    public MessageChat(Integer type, Integer state, String fromUserName,
                       String content, Boolean isSend, Boolean sendSucces, Date time) {
        super ();
        this.type = type;
        this.state = state;
        this.fromUserName = fromUserName;
        this.content = content;
        this.isSend = isSend;
        this.sendSucces = sendSucces;
        this.time = time;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getFromUserName() {
        return fromUserName;
    }

    public void setFromUserName(String fromUserName) {
        this.fromUserName = fromUserName;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Boolean getIsSend() {
        return isSend;
    }

    public void setIsSend(Boolean isSend) {
        this.isSend = isSend;
    }

    public Boolean getSendSucces() {
        return sendSucces;
    }

    public void setSendSucces(Boolean sendSucces) {
        this.sendSucces = sendSucces;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }
}
