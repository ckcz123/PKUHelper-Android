package com.pkuhelper.model;

import android.telecom.Call;

import com.pkuhelper.entity.SecondHandCategoryEntity;
import com.pkuhelper.entity.SecondHandItemEntity;

import java.util.ArrayList;

/**
 * Created by zyxu on 3/31/16.
 */
public interface ISecondHandMod {

    /**
     * 获取列表(完整版)
     * @param type sale/require
     * @param page 页数
     * @param category1 第一类别
     * @param Category2 第二类别
     * @param keyword 搜索关键字
     */
    void getItemList(String type, int page, String category1, String category2, String keyword, Callback<ArrayList<SecondHandItemEntity>> callback);

    /**
     * 获取列表(不搜索)
     * @param page 页数
     * @param callback 返回
     */
    void getItemList(int page, Callback<ArrayList<SecondHandItemEntity>> callback);

    /**
     * 获取第0页
     * @param callback 返回
     */
    void getItemList(Callback<ArrayList<SecondHandItemEntity>> callback);

    /**
     * @param itemID 商品id
     * @param callback 返回商品实例
     */
    void getItem(String itemID, Callback<SecondHandItemEntity> callback);

    /**
     * 发布商品
     * @param entity 商品信息
     * @param callback 返回itemID
     */
    void postItem(SecondHandItemEntity entity, Callback<Integer> callback);

    /**
     * 修改商品信息
     * @param entity
     */
    void changeItem(SecondHandItemEntity entity);

    /**
     * @param itemID 商品id
     * @param status active/deal/cancel/canceldeal/reactive
     */
    void changeItemStatus(int itemID, String status);

    /**
     * @param callback
     */
    void getCategoryList(Callback<ArrayList<SecondHandCategoryEntity>> callback);

    /**
     * @param itemID
     * @param callback 返回chatTo
     */
    void createSession(String itemID, Callback<String> callback);
}
