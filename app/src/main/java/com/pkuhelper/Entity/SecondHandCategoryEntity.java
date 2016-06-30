package com.pkuhelper.entity;

import java.util.ArrayList;

/**
 * Created by zyxu on 4/7/16.
 */
public class SecondHandCategoryEntity {
    private String name;
    private String id;
    private int showOrder;
    private ArrayList<SecondHandItemEntity> subCategories;

    public SecondHandCategoryEntity() {
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getShowOrder() {
        return showOrder;
    }

    public void setShowOrder(int showOrder) {
        this.showOrder = showOrder;
    }

    public ArrayList<SecondHandItemEntity> getSubCategories() {
        return subCategories;
    }

    public void setSubCategories(ArrayList<SecondHandItemEntity> subCategories) {
        this.subCategories = subCategories;
    }
}
