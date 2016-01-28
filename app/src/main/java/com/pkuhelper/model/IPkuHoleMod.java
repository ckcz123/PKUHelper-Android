package com.pkuhelper.model;

import com.pkuhelper.entity.HoleCommentListItemEntity;
import com.pkuhelper.entity.HoleListItemEntity;

import java.util.ArrayList;

/**
 * Created by LuoLiangchen on 16/1/9.
 */
public interface IPkuHoleMod {
    /**
     * URL片段 - 图片
     */
    String URL_IMAGES = "images";

    /**
     * URL片段 - 语音
     */
    String URL_AUDIOS = "audios";

    /**
     * 树洞类型 - 文字树洞
     */
    String TYPE_TEXT = "text";

    /**
     * 树洞类型 - 图片树洞
     */
    String TYPE_IMAGE = "image";

    /**
     * 树洞类型 - 语音树洞
     */
    String TYPE_AUDIO = "audio";

    /**
     * 关注设置 - 开启
     */
    int ATTENTION_ON = 1;

    /**
     * 关注设置 - 关闭
     */
    int ATTENTION_OFF = 0;

    /**
     * 树洞新消息 - 有新消息
     */
    int NEW_HOLE_EXIST = 1;

    /**
     * 树洞新消息 - 无新消息
     */
    int NEW_HOLE_NOT_EXIST = 0;

    /**
     * 推送设置
     */
    int PUSH_ON = 0x02;
    int PUSH_OFF = 0x00;
    int PUSH_SHOW_CONTENT = 0x01;

    /**
     * 获取第page页的树洞列表
     * @param page 页
     * @param callback 回调
     */
    void getHoleList(int page, final Callback<ArrayList<HoleListItemEntity>> callback);

    /**
     * 获取指定pid的树洞评论列表
     * @param pid PID
     * @param callback 回调
     */
    void getCommentList(int pid, final Callback<ArrayList<HoleCommentListItemEntity>> callback);

    /**
     * 发布树洞
     * @param type 树洞类型
     * @param text 树洞文字内容
     * @param data （可选）图片或语音的Base64
     * @param length （可选）语音的长度
     * @param callback 回调
     */
    void post(String type, String text, String data, int length, final Callback<Void> callback);

    /**
     * 回复树洞
     * @param pid 树洞PID
     * @param text 回复内容
     * @param callback 回调
     */
    void reply(int pid, String text, final Callback<Void> callback);

    /**
     * 设置是否关注
     * @param pid 树洞PID
     * @param what 关注：ATTENTION_ON；取关：ATTENTION_OFF
     * @param callback 回调
     */
    void setAttention(int pid, int what, final Callback<Void> callback);

    /**
     * 是否有新树洞
     * @param callback 回调
     */
    void isNewHoleExist(final Callback<Void> callback);

    /**
     * 获取收藏树洞列表
     * @param callback 回调
     */
    void getAttentionList(final Callback<ArrayList<HoleListItemEntity>> callback);

    /**
     * 从服务器获取树洞推送设置
     * @param callback 回调，传回的数据是推送设置的参数，判断时对其进行&运算，例如if param & PUSH_ON then
     */
    void getPushSettings(final Callback<Integer> callback);

    /**
     * 向服务器发送树洞推送设置
     * @param param 推送参数：PUSH_ON, PUSH_OFF, PUSH_SHOW_CONTENT；传入时使用"|"连接，例如PUSH_ON | SHOW_CONTENT
     * @param callback 回调
     */
    void setPushSettings(int param, final Callback<Void> callback);

    /**
     * 举报树洞
     * @param pid 树洞PID
     * @param reason 举报理由
     * @param callback 回调
     */
    void report(int pid, String reason, final Callback<Void> callback);

    /**
     * 搜索树洞
     * @param keywords 关键词，可以空格分开多个
     * @param page （可选）开始搜索的页
     * @param pageSize （可选）搜索显示的个数？
     * @param type （可选）搜索树洞的类型
     * @param callback 回调
     */
    void search(String keywords, int page, int pageSize, String type, Callback<ArrayList<HoleListItemEntity>> callback);

    /**
     * 获取树洞中图片或语音的URL
     * @param type 树洞类型
     * @param url URL后缀
     * @return 完整URL
     */
    String getResourceUrl(String type, String url);

    <Entity> String getJson(Entity entity);

    /**
     * 配置关注树洞的PID的集合
     * @param entities 关注的树洞的Entity实体的List
     */
    void setupAttentionSet(ArrayList<HoleListItemEntity> entities);

    /**
     * 判断某个树洞是否被当前用户关注
     * @param pid 树洞PID
     * @return true：用户关注；false：用户未关注
     */
    boolean isOnAttention(int pid);

    /**
     * 判断某个树洞是否被当前用户关注
     * @param pid 树洞PID
     * @param what Attention ON or OFF
     */
    void setOnAttention(int pid, int what);
}
