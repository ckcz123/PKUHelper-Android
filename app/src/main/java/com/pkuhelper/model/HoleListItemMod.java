package com.pkuhelper.model;

/**
 * Created by LuoLiangchen on 15/11/30.
 */
public class HoleListItemMod {
    private int pid;
    private String text;
    private String type;
    private long timestamp;
    private int reply;
    private int likenum;
    private int extra;
    private String url;

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
}
