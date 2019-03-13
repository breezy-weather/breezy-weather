package wangdaye.com.geometricweather.weather.json.cn;

import java.util.List;

/**
 * CN weather result.
 * */

public class CNWeatherResult {

    /**
     * historyWeather : {"history":{}}
     * area : [["山东","10112"],["青岛","1011202"],["黄岛","101120206"]]
     * life : {"date":"2018-03-20","info":{"kongtiao":["较少开启","您将感到很舒适，一般不需要开启空调。"],"daisan":["带伞","有降水，如果您要短时间外出的话可不必带雨伞。"],"ziwaixian":["最弱","属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。"],"yundong":["较不宜","有降水，且天气寒冷，风力极强，推荐您在室内进行低强度运动；若坚持户外运动，请选择合适的运动并注意保暖。"],"ganmao":["极易发","将有一次强降温过程，天气寒冷，且风力较强，极易发生感冒，请特别注意增加衣服保暖防寒。"],"xiche":["不宜","不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。"],"diaoyu":["不宜","风力太大，不适合垂钓。"],"guomin":["不易发","天气条件不易诱发过敏，有降水，风力较大，特殊体质人群应注意防风，预防感冒可能引发的过敏。"],"wuran":["优","气象条件非常有利于空气污染物稀释、扩散和清除，可在室外正常活动。"],"chuanyi":["冷","天气冷，建议着棉服、羽绒服、皮夹克加羊毛衫等冬季服装。年老体弱者宜着厚棉衣、冬大衣或厚羽绒服。"]}}
     * realtime : {"mslp":"","wind":{"windspeed":"8.0","direct":"西北风","power":"5级"},"time":"08:40:00","pressure":"1024","weather":{"humidity":"58","img":"1","info":"多云","temperature":"3"},"feelslike_c":"2","dataUptime":"1521506400","date":"2018-03-20"}
     * alert : [{"content":"青岛市气象台2018年03月19日15时00分继续发布大风蓝色预警信号：受海上气旋影响，预计今天下午到明天白天，我市东北风陆地仍可达5到6级阵风8级，海上6到7级阵风9级。请注意防范。","pubTime":"2018-03-19 15:00","originUrl":"http://mobile.weathercn.com/alert.do?id=37021141600000_20180319150431","alarmTp2":"蓝色","alarmTp1":"大风","type":1,"alarmPic2":"01","alarmPic1":"05"},{"content":"青岛市气象台2018年03月19日15时00分继续发布大风蓝色预警信号：受海上气旋影响，预计今天下午到明天白天，我市东北风陆地仍可达5到6级阵风8级，海上6到7级阵风9级，请注意防范。","pubTime":"2018-03-19 15:00","originUrl":"http://mobile.weathercn.com/alert.do?id=37020041600000_20180319150254","alarmTp2":"蓝色","alarmTp1":"大风","type":1,"alarmPic2":"01","alarmPic1":"05"}]
     * trafficalert : []
     * weather : [{"aqi":"138","date":"2018-03-19","info":{"night":["1","多云","4","东北风","5-6级","18:10"],"day":["1","多云","13","东北风","5-6级","06:08"]}},{"aqi":"83","date":"2018-03-20","info":{"night":["1","多云","1","北风","3-5级","18:11"],"day":["3","阵雨","7","东北风","5-6级","06:06"]}},{"aqi":"106","date":"2018-03-21","info":{"night":["0","晴","-3","北风","3-5级","18:12"],"day":["1","多云","10","北风","3-5级","06:05"]}},{"aqi":"102","date":"2018-03-22","info":{"night":["0","晴","1","南风","3-5级","18:12"],"day":["1","多云","11","西南风","3-5级","06:03"]}},{"aqi":"66","date":"2018-03-23","info":{"night":["1","多云","5","北风","3-5级","18:13"],"day":["1","多云","14","南风","3-5级","06:02"]}},{"aqi":"95","date":"2018-03-24","info":{"night":["0","晴","6","南风","3-5级","18:14"],"day":["0","晴","16","南风","3-5级","06:00"]}},{"aqi":"71","date":"2018-03-25","info":{"night":["0","晴","6","南风","4-5级","18:15"],"day":["0","晴","14","南风","3-5级","05:59"]}},{"aqi":"48","date":"2018-03-26","info":{"night":["7","小雨","8","持续无风向","微风","18:16"],"day":["1","多云","14","南风","4-5级","05:57"]}},{"aqi":"","date":"2018-03-27","info":{"night":["7","小雨","8","东南风","微风","18:17"],"day":["1","多云","15","东南风","微风","05:56"]}},{"aqi":"","date":"2018-03-28","info":{"night":["0","晴","9","南风","微风","18:17"],"day":["7","小雨","17","东风","微风","05:55"]}},{"aqi":"","date":"2018-03-29","info":{"night":["1","多云","10","西南风","微风","18:18"],"day":["1","多云","14","东风","微风","05:53"]}},{"aqi":"","date":"2018-03-30","info":{"night":["7","小雨","9","西南风","微风","18:19"],"day":["1","多云","19","南风","3-5级","05:52"]}},{"aqi":"","date":"2018-03-31","info":{"night":["2","阴","7","西南风","微风","18:20"],"day":["1","多云","16","南风","微风","05:50"]}},{"aqi":"","date":"2018-04-01","info":{"night":["7","小雨","5","西南风","4-5级","18:21"],"day":["7","小雨","14","东南风","4-5级","05:49"]}},{"aqi":"","date":"2018-04-02","info":{"night":["2","阴","5","南风","4-5级","18:22"],"day":["7","小雨","8","东风","3-5级","05:47"]}},{"aqi":"","date":"2018-04-03","info":{"night":["1","多云","3","西北风","4-5级","18:22"],"day":["2","阴","11","东北风","4-5级","05:46"]}}]
     * pm25 : {"so2":10,"o3":45,"parent":"青岛,101120201","co":"0.6","level":2,"color":"#ffff00","no2":36,"aqi":57,"quality":"良","pm10":64,"pm25":23,"advice":"今天的空气质量是可以接受的，除少数异常敏感体质的人群外，大家可在户外正常活动。","chief":"PM10","upDateTime":1521500400000}
     * hourly_forecast : [{"img":"01","wind_speed":"8","hour":"8","wind_direct":"北风","temperature":"6","info":"多云"},{"img":"01","wind_speed":"9","hour":"9","wind_direct":"北风","temperature":"7","info":"多云"},{"img":"01","wind_speed":"9","hour":"10","wind_direct":"东北风","temperature":"7","info":"多云"},{"img":"01","wind_speed":"9","hour":"11","wind_direct":"东北风","temperature":"8","info":"多云"},{"img":"01","wind_speed":"9","hour":"12","wind_direct":"东北风","temperature":"8","info":"多云"},{"img":"01","wind_speed":"9","hour":"13","wind_direct":"东北风","temperature":"7","info":"多云"},{"img":"01","wind_speed":"9","hour":"14","wind_direct":"东北风","temperature":"7","info":"多云"},{"img":"01","wind_speed":"9","hour":"15","wind_direct":"东北风","temperature":"6","info":"多云"},{"img":"03","wind_speed":"9","hour":"16","wind_direct":"东北风","temperature":"6","info":"阵雨"},{"img":"01","wind_speed":"8","hour":"17","wind_direct":"东北风","temperature":"6","info":"多云"},{"img":"03","wind_speed":"8","hour":"18","wind_direct":"东北风","temperature":"5","info":"阵雨"},{"img":"01","wind_speed":"7","hour":"19","wind_direct":"东北风","temperature":"5","info":"多云"},{"img":"01","wind_speed":"6","hour":"20","wind_direct":"东北风","temperature":"5","info":"多云"},{"img":"01","wind_speed":"6","hour":"21","wind_direct":"东北风","temperature":"4","info":"多云"},{"img":"01","wind_speed":"5","hour":"22","wind_direct":"东北风","temperature":"4","info":"多云"},{"img":"01","wind_speed":"5","hour":"23","wind_direct":"东北风","temperature":"4","info":"多云"},{"img":"01","wind_speed":"5","hour":"0","wind_direct":"北风","temperature":"4","info":"多云"},{"img":"01","wind_speed":"5","hour":"1","wind_direct":"北风","temperature":"3","info":"多云"},{"img":"01","wind_speed":"5","hour":"2","wind_direct":"北风","temperature":"3","info":"多云"},{"img":"01","wind_speed":"6","hour":"3","wind_direct":"西北风","temperature":"2","info":"多云"},{"img":"01","wind_speed":"6","hour":"4","wind_direct":"西北风","temperature":"2","info":"多云"},{"img":"01","wind_speed":"6","hour":"5","wind_direct":"西北风","temperature":"2","info":"多云"},{"img":"01","wind_speed":"7","hour":"6","wind_direct":"西北风","temperature":"1","info":"多云"},{"img":"01","wind_speed":"5","hour":"7","wind_direct":"西北风","temperature":"2","info":"多云"}]
     */

    public Life life;
    public Realtime realtime;
    public Pm25 pm25;
    public List<List<String>> area;
    public List<Alert> alert;
    public List<WeatherX> weather;
    public List<HourlyForecast> hourly_forecast;

    public static class Life {
        /**
         * date : 2018-03-20
         * info : {"kongtiao":["较少开启","您将感到很舒适，一般不需要开启空调。"],"daisan":["带伞","有降水，如果您要短时间外出的话可不必带雨伞。"],"ziwaixian":["最弱","属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。"],"yundong":["较不宜","有降水，且天气寒冷，风力极强，推荐您在室内进行低强度运动；若坚持户外运动，请选择合适的运动并注意保暖。"],"ganmao":["极易发","将有一次强降温过程，天气寒冷，且风力较强，极易发生感冒，请特别注意增加衣服保暖防寒。"],"xiche":["不宜","不宜洗车，未来24小时内有雨，如果在此期间洗车，雨水和路上的泥水可能会再次弄脏您的爱车。"],"diaoyu":["不宜","风力太大，不适合垂钓。"],"guomin":["不易发","天气条件不易诱发过敏，有降水，风力较大，特殊体质人群应注意防风，预防感冒可能引发的过敏。"],"wuran":["优","气象条件非常有利于空气污染物稀释、扩散和清除，可在室外正常活动。"],"chuanyi":["冷","天气冷，建议着棉服、羽绒服、皮夹克加羊毛衫等冬季服装。年老体弱者宜着厚棉衣、冬大衣或厚羽绒服。"]}
         */

        public String date;
        public Info info;

        public static class Info {
            public List<String> kongtiao;
            public List<String> daisan;
            public List<String> ziwaixian;
            public List<String> yundong;
            public List<String> ganmao;
            public List<String> xiche;
            public List<String> diaoyu;
            public List<String> guomin;
            public List<String> wuran;
            public List<String> chuanyi;
        }
    }

    public static class Realtime {
        /**
         * mslp :
         * wind : {"windspeed":"8.0","direct":"西北风","power":"5级"}
         * time : 08:40:00
         * pressure : 1024
         * weather : {"humidity":"58","img":"1","info":"多云","temperature":"3"}
         * feelslike_c : 2
         * dataUptime : 1521506400
         * date : 2018-03-20
         */

        public String mslp;
        public Wind wind;
        public String time;
        public String pressure;
        public Weather weather;
        public String feelslike_c;
        public String dataUptime;
        public String date;

        public static class Wind {
            /**
             * windspeed : 8.0
             * direct : 西北风
             * power : 5级
             */

            public String windspeed;
            public String direct;
            public String power;
        }

        public static class Weather {
            /**
             * humidity : 58
             * img : 1
             * info : 多云
             * temperature : 3
             */

            public String humidity;
            public String img;
            public String info;
            public String temperature;
        }
    }

    public static class Pm25 {
        /**
         * so2 : 10
         * o3 : 45
         * parent : 青岛,101120201
         * co : 0.6
         * level : 2
         * color : #ffff00
         * no2 : 36
         * aqi : 57
         * quality : 良
         * pm10 : 64
         * pm25 : 23
         * advice : 今天的空气质量是可以接受的，除少数异常敏感体质的人群外，大家可在户外正常活动。
         * chief : PM10
         * upDateTime : 1521500400000
         */

        public int so2;
        public int o3;
        public String parent;
        public String co;
        public int level;
        public String color;
        public int no2;
        public int aqi;
        public String quality;
        public int pm10;
        public int pm25;
        public String advice;
        public String chief;
        public long upDateTime;
    }

    public static class Alert {
        /**
         * content : 青岛市气象台2018年03月19日15时00分继续发布大风蓝色预警信号：受海上气旋影响，预计今天下午到明天白天，我市东北风陆地仍可达5到6级阵风8级，海上6到7级阵风9级。请注意防范。
         * pubTime : 2018-03-19 15:00
         * originUrl : http://mobile.weathercn.com/alert.do?id=37021141600000_20180319150431
         * alarmTp2 : 蓝色
         * alarmTp1 : 大风
         * type : 1
         * alarmPic2 : 01
         * alarmPic1 : 05
         */

        public String content;
        public String pubTime;
        public String originUrl;
        public String alarmTp2;
        public String alarmTp1;
        public int type;
        public String alarmPic2;
        public String alarmPic1;
    }

    public static class WeatherX {
        /**
         * aqi : 138
         * date : 2018-03-19
         * info : {"night":["1","多云","4","东北风","5-6级","18:10"],"day":["1","多云","13","东北风","5-6级","06:08"]}
         */

        public String aqi;
        public String date;
        public InfoX info;

        public static class InfoX {
            public List<String> night;
            public List<String> day;
        }
    }

    public static class HourlyForecast {
        /**
         * img : 01
         * wind_speed : 8
         * hour : 8
         * wind_direct : 北风
         * temperature : 6
         * info : 多云
         */

        public String img;
        public String wind_speed;
        public String hour;
        public String wind_direct;
        public String temperature;
        public String info;
    }
}
