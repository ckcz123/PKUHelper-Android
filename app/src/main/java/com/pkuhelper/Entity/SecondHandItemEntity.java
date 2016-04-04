package com.pkuhelper.entity;

import java.util.ArrayList;

/**
 * Created by zyxu on 3/31/16.
 */
public class SecondHandItemEntity {

    private String timestamp;
    private String type;
    private String category1;
    private String category2;
    private String name;
    private String description;
    private int price;
    private boolean daoable;
    private ArrayList<String> images = new ArrayList<>();
    private ArrayList<ItemImage> itemImages = new ArrayList<>(5);

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

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public boolean isDaoable() {
        return daoable;
    }

    public void setDaoable(boolean daoable) {
        this.daoable = daoable;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }


    public ArrayList<ItemImage> getItemImages() {
        return itemImages;
    }

    public void setItemImages(ArrayList<ItemImage> itemImages) {
        this.itemImages = itemImages;
    }

    public ArrayList<String> getImages() {
        return images;
    }

    public void setImages(ArrayList<String> images) {
        this.images = images;
    }

    public class ItemImage{
        private String filename;
        private String description;
        private int showOrder;

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
