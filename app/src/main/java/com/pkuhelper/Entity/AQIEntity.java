package com.pkuhelper.entity;

/**
 * Created by zyxu on 3/10/16.
 */
public class AQIEntity {
    public static final int AQI_0_100 = 0;
    public static final int AQI_100_200 = 1;
    public static final int AQI_200_300 =2;
    public static final int AQI_300_400 =3;
    public static final int AQI_400_500 =4;
    public static final int AQI_500_INF =5;

    int stage;
    int aqi;
    String updateTime;

    public AQIEntity(){
        aqi=0;
    }
    public AQIEntity(int aqi, String time){
        this.aqi = aqi;
        updateTime = time;
        updateStage();
    }

    public AQIEntity(String string){

        String[] s = string.split(" ");
        this.aqi = Integer.valueOf(s[0]);
        updateTime = s[1];
        updateStage();
    }
    private void updateStage(){
        if (aqi<100)
            stage=AQI_0_100;
        else if (aqi<200)
            stage=AQI_100_200;
        else if (aqi<300)
            stage=AQI_200_300;
        else if (aqi<400)
            stage=AQI_300_400;
        else if (aqi<500)
            stage=AQI_400_500;
        else
            stage=AQI_500_INF;
    }

    public int getStage(){
        return this.stage;
    }
    public int getAQI(){
        return this.aqi;
    }
}
