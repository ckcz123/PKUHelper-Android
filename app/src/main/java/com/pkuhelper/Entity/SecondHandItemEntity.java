package com.pkuhelper.entity;

import java.util.ArrayList;

/**
 * Created by zyxu on 3/31/16.
 */
public class SecondHandItemEntity {

    String type;
    String category1;
    String category2;
    String name;
    String description;
    double price;
    boolean daoable;
    ArrayList<ItemImage> itemImages = new ArrayList<>(5);

    public SecondHandItemEntity() {

    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory1() {
        return category1;
    }

    public void setCategory1(String category1) {
        this.category1 = category1;
    }

    public String getCategory2() {
        return category2;
    }

    public void setCategory2(String category2) {
        this.category2 = category2;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public boolean isDaoable() {
        return daoable;
    }

    public void setDaoable(boolean daoable) {
        this.daoable = daoable;
    }

    public class ItemImage{
        String filename;
        String description;
        int showOrder;

        public ItemImage() {
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public int getShowOrder() {
            return showOrder;
        }

        public void setShowOrder(int showOrder) {
            this.showOrder = showOrder;
        }
    }
}
