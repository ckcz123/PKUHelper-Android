package com.pkuhelper.entity;

/**
 * Created by LuoLiangchen on 16/1/8.
 */
public class HoleListItemEntity {
    private int pid;
    private String text;
    private String type;
    private long timestamp;
    private int reply;
    private int likenum;
    private int extra;
    private String url;
    private long hot;

    public int getPid() {
        return pid;
    }

    public String getText() {
        return text;
    }

    public String getType() {
        return type;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public int getReply() {
        return reply;
    }

    public int getLikenum() {
        return likenum;
    }

    public int getExtra() {
        return extra;
    }

    public String getUrl() {
        return url;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setReply(int reply) {
        this.reply = reply;
    }

    public void setLikenum(int likenum) {
        this.likenum = likenum;
    }

    public void setExtra(int extra) {
        this.extra = extra;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public long getHot() {
        return hot;
    }

    public void setHot(long hot) {
        this.hot = hot;
    }
}
