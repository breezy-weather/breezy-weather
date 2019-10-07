package wangdaye.com.geometricweather.weather.json.accu;

import java.util.Date;
import java.util.List;

/**
 * Accu alert result.
 * */

public class AccuAlertResult {

    /**
     * CountryCode : CN
     * AlertID : 7797
     * Description : {"Localized":"霾黄色预警","English":"Yellow Warning of Haze"}
     * Category : NON-PRECIPITATION
     * Priority : 69
     * Type : 霾
     * TypeID : CN270
     * Class : null
     * Level : 黄色
     * Color : {"Name":"Yellow","Red":255,"Green":255,"Blue":0,"Hex":"#FFFF00"}
     * SettingsWeatherSource : 中国气象局公共气象服务中心
     * SourceId : 5
     * Disclaimer : null
     * Area : [{"Name":"青岛市","StartTime":"2016-12-21T14:50:00+08:00","EpochStartTime":1482303000,"EndTime":null,"EpochEndTime":null,"LastAction":{"Localized":"新建","English":"New"},"Text":"青岛市气象台2016年12月21日14时50分继续发布霾黄色预警信号：预计今天下午到夜间，气象扩散条件一般，我市大部分地区仍有中度霾天气，短时可能出现重度霾，请注意防范。","LanguageCode":"zh-cn","Summary":"霾黄色预警 生效。来源：中国气象局公共气象服务中心"}]
     * MobileLink : http://m.accuweather.com/zh/cn/licang-district/2333323/weather-warnings/2333323?lang=zh-cn
     * Link : http://www.accuweather.com/zh/cn/licang-district/2333323/weather-warnings/2333323?lang=zh-cn
     */

    public String CountryCode;
    public int AlertID;
    /**
     * Localized : 霾黄色预警
     * English : Yellow Warning of Haze
     */

    public Description Description;
    public String Category;
    public int Priority;
    public String Type;
    public String TypeID;
    public String Level;
    /**
     * Name : Yellow
     * Red : 255
     * Green : 255
     * Blue : 0
     * Hex : #FFFF00
     */

    public Color Color;
    public String Source;
    public int SourceId;
    public String MobileLink;
    public String Link;
    /**
     * Name : 青岛市
     * StartTime : 2016-12-21T14:50:00+08:00
     * EpochStartTime : 1482303000
     * EndTime : null
     * EpochEndTime : null
     * LastAction : {"Localized":"新建","English":"New"}
     * Text : 青岛市气象台2016年12月21日14时50分继续发布霾黄色预警信号：预计今天下午到夜间，气象扩散条件一般，我市大部分地区仍有中度霾天气，短时可能出现重度霾，请注意防范。
     * LanguageCode : zh-cn
     * Summary : 霾黄色预警 生效。来源：中国气象局公共气象服务中心
     */

    public List<Area> Area;

    public static class Description {
        public String Localized;
        public String English;
    }

    public static class Color {
        public String Name;
        public int Red;
        public int Green;
        public int Blue;
        public String Hex;
    }

    public static class Area {
        public String Name;
        public Date StartTime;
        public long EpochStartTime;
        public Date EndTime;
        public long EpochEndTime;
        /**
         * Localized : 新建
         * English : New
         */

        public LastAction LastAction;
        public String Text;
        public String LanguageCode;
        public String Summary;

        public static class LastAction {
            public String Localized;
            public String English;
        }
    }
}
