package com.pkuhelper.manager;

import java.util.GregorianCalendar;

/**
 * Created by zyxu on 1/21/16.
 */
public class CalendarManager {

    public static String getDeltaTime(long timestamp){
        long timestampNow = (new GregorianCalendar()).getTimeInMillis();
        long deltaSecond = (timestampNow - timestamp)/1000;
        String deltaTimeInString;

        if (deltaSecond<0)
            return "时间解析错误";

        if (deltaSecond<60)
            deltaTimeInString=deltaSecond+"秒";
        else if (deltaSecond<3600)
            deltaTimeInString=""+deltaSecond/60+"分钟";
        else if (deltaSecond<(60*60*24))
            deltaTimeInString=""+deltaSecond/(60*60)+"小时";
        else
            deltaTimeInString=""+deltaSecond/(60*60*24)+"天";

        deltaTimeInString+="前";

        return deltaTimeInString;
    }
}
