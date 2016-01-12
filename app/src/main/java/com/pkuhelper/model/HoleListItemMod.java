package com.pkuhelper.model;

import android.content.Context;
import android.graphics.Bitmap;

import com.pkuhelper.lib.MyBitmapFactory;
import com.pkuhelper.lib.MyFile;
import com.pkuhelper.lib.Util;

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
    //add by zy
    private Bitmap bitmap;

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

    //add by zy
    public Bitmap getBitmap(Context context) {
        if (!type.equals("image")) return null;
        if (bitmap != null) return bitmap;
        try {
            String hash = Util.getHash(url);
            bitmap = MyBitmapFactory.getCompressedBitmap(MyFile.getCache(context, hash).getAbsolutePath(), 2);
            return bitmap;
        } catch (Exception e) {
        }
        return null;
    }
}
