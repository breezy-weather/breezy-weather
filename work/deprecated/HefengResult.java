package com.mbestavros.geometricweather.basic.deprecated;
/*
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Hefeng result.
 * */
/*
public class HefengResult {

    /**
     * aqiAqi : {"city":{"aqiAqi":"196","co":"0","no2":"14","o3":"45","pm10":"116","aqiPm25":"196","qlty":"优","so2":"2"}}
     * basic : {"city":"北京","cnty":"中国","id":"CN101010100","lat":"39.904000","lon":"116.391000","update":{"loc":"2016-10-01 08:51","utc":"2016-10-01 00:51"}}
     * daily_forecast : [{"astro":{"sr":"06:10","ss":"17:57"},"cond":{"code_d":"502","code_n":"502","txt_d":"霾","txt_n":"霾"},"date":"2016-10-01","hum":"28","pcpn":"0.1","pop":"0","pres":"1013","tmp":{"max":"25","min":"15"},"vis":"10","wind":{"deg":"182","dir":"无持续风向","sc":"微风","spd":"8"}},{"astro":{"sr":"06:11","ss":"17:55"},"cond":{"code_d":"502","code_n":"502","txt_d":"霾","txt_n":"霾"},"date":"2016-10-02","hum":"27","pcpn":"0.0","pop":"4","pres":"1011","tmp":{"max":"27","min":"16"},"vis":"10","wind":{"deg":"193","dir":"无持续风向","sc":"微风","spd":"8"}},{"astro":{"sr":"06:12","ss":"17:53"},"cond":{"code_d":"502","code_n":"502","txt_d":"霾","txt_n":"霾"},"date":"2016-10-03","hum":"29","pcpn":"0.0","pop":"0","pres":"1009","tmp":{"max":"27","min":"19"},"vis":"10","wind":{"deg":"89","dir":"无持续风向","sc":"微风","spd":"2"}},{"astro":{"sr":"06:13","ss":"17:52"},"cond":{"code_d":"300","code_n":"100","txt_d":"阵雨","txt_n":"晴"},"date":"2016-10-04","hum":"83","pcpn":"5.9","pop":"99","pres":"1009","tmp":{"max":"24","min":"16"},"vis":"9","wind":{"deg":"350","dir":"北风","sc":"3-4","spd":"16"}},{"astro":{"sr":"06:14","ss":"17:50"},"cond":{"code_d":"100","code_n":"101","txt_d":"晴","txt_n":"多云"},"date":"2016-10-05","hum":"21","pcpn":"0.0","pop":"0","pres":"1017","tmp":{"max":"23","min":"14"},"vis":"10","wind":{"deg":"284","dir":"无持续风向","sc":"微风","spd":"10"}},{"astro":{"sr":"06:15","ss":"17:49"},"cond":{"code_d":"101","code_n":"104","txt_d":"多云","txt_n":"阴"},"date":"2016-10-06","hum":"20","pcpn":"0.0","pop":"17","pres":"1026","tmp":{"max":"20","min":"13"},"vis":"10","wind":{"deg":"38","dir":"无持续风向","sc":"微风","spd":"3"}},{"astro":{"sr":"06:16","ss":"17:47"},"cond":{"code_d":"104","code_n":"305","txt_d":"阴","txt_n":"小雨"},"date":"2016-10-07","hum":"81","pcpn":"9.2","pop":"81","pres":"1027","tmp":{"max":"19","min":"12"},"vis":"2","wind":{"deg":"39","dir":"无持续风向","sc":"微风","spd":"5"}}]
     * hourly_forecast : [{"date":"2016-10-01 10:00","hum":"35","pop":"0","pres":"1015","tmp":"24","wind":{"deg":"146","dir":"东南风","sc":"微风","spd":"6"}},{"date":"2016-10-01 13:00","hum":"50","pop":"0","pres":"1025","tmp":"13","wind":{"deg":"70","dir":"东北风","sc":"微风","spd":"6"}},{"date":"2016-10-01 16:00","hum":"50","pop":"0","pres":"1025","tmp":"13","wind":{"deg":"70","dir":"东北风","sc":"微风","spd":"6"}},{"date":"2016-10-01 19:00","hum":"52","pop":"0","pres":"1027","tmp":"13","wind":{"deg":"100","dir":"东风","sc":"微风","spd":"5"}},{"date":"2016-10-01 22:00","hum":"83","pop":"10","pres":"1026","tmp":"14","wind":{"deg":"95","dir":"东风","sc":"微风","spd":"8"}}]
     * now : {"cond":{"code":"101","txt":"多云"},"fl":"14","hum":"80","pcpn":"0","pres":"1015","tmp":"15","vis":"6","wind":{"deg":"122","dir":"东北风","sc":"3-4","spd":"13"}}
     * status : ok
     * suggestion : {"comf":{"brf":"舒适","txt":"白天不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。"},"cw":{"brf":"不宜","txt":"不宜洗车，未来24小时内有霾，如果在此期间洗车，会弄脏您的爱车。"},"drsg":{"brf":"舒适","txt":"建议着长袖T恤、衬衫加单裤等服装。年老体弱者宜着针织长袖衬衫、马甲和长裤。"},"flu":{"brf":"较易发","txt":"天气转凉，空气湿度较大，较易发生感冒，体质较弱的朋友请注意适当防护。"},"sport":{"brf":"较不宜","txt":"有扬沙或浮尘，建议适当停止户外运动，选择在室内进行运动，以避免吸入更多沙尘，有损健康。"},"trav":{"brf":"较不宜","txt":"空气质量差，不适宜旅游"},"uv":{"brf":"最弱","txt":"属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。"}}
     */
/*
    @SerializedName("HeWeather data service 3.0")
    public List<HeWeather> heWeather;

    public static class HeWeather {
        /**
         * city : {"aqiAqi":"196","co":"0","no2":"14","o3":"45","pm10":"116","aqiPm25":"196","qlty":"优","so2":"2"}
         */
/*
        public Aqi aqi;
        /**
         * city : 北京
         * cnty : 中国
         * id : CN101010100
         * lat : 39.904000
         * lon : 116.391000
         * update : {"loc":"2016-10-01 08:51","utc":"2016-10-01 00:51"}
         */
/*
        public Basic basic;
        /**
         * cond : {"code":"101","txt":"多云"}
         * fl : 14
         * hum : 80
         * pcpn : 0
         * pres : 1015
         * tmp : 15
         * vis : 6
         * wind : {"deg":"122","dir":"东北风","sc":"3-4","spd":"13"}
         */
/*
        public Now now;
        public String status;
        /**
         * comf : {"brf":"舒适","txt":"白天不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。"}
         * cw : {"brf":"不宜","txt":"不宜洗车，未来24小时内有霾，如果在此期间洗车，会弄脏您的爱车。"}
         * drsg : {"brf":"舒适","txt":"建议着长袖T恤、衬衫加单裤等服装。年老体弱者宜着针织长袖衬衫、马甲和长裤。"}
         * flu : {"brf":"较易发","txt":"天气转凉，空气湿度较大，较易发生感冒，体质较弱的朋友请注意适当防护。"}
         * sport : {"brf":"较不宜","txt":"有扬沙或浮尘，建议适当停止户外运动，选择在室内进行运动，以避免吸入更多沙尘，有损健康。"}
         * trav : {"brf":"较不宜","txt":"空气质量差，不适宜旅游"}
         * uv : {"brf":"最弱","txt":"属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。"}
         */
/*
        public Suggestion suggestion;
        /**
         * astro : {"sr":"06:10","ss":"17:57"}
         * cond : {"code_d":"502","code_n":"502","txt_d":"霾","txt_n":"霾"}
         * date : 2016-10-01
         * hum : 28
         * pcpn : 0.1
         * pop : 0
         * pres : 1013
         * tmp : {"max":"25","min":"15"}
         * vis : 10
         * wind : {"deg":"182","dir":"无持续风向","sc":"微风","spd":"8"}
         */
/*
        public List<DailyForecast> daily_forecast;
        /**
         * date : 2016-10-01 10:00
         * hum : 35
         * pop : 0
         * pres : 1015
         * tmp : 24
         * wind : {"deg":"146","dir":"东南风","sc":"微风","spd":"6"}
         */
/*
        public List<HourlyForecast> hourly_forecast;

        public static class Aqi {
            /**
             * aqiAqi : 196
             * co : 0
             * no2 : 14
             * o3 : 45
             * pm10 : 116
             * aqiPm25 : 196
             * qlty : 优
             * so2 : 2
             */
/*
            public City city;

            public static class City {
                public String aqi;
                public String co;
                public String no2;
                public String o3;
                public String pm10;
                public String pm25;
                public String qlty;
                public String so2;
            }
        }

        public static class Basic {
            public String city;
            public String cnty;
            public String id;
            public String lat;
            public String lon;
            /**
             * loc : 2016-10-01 08:51
             * utc : 2016-10-01 00:51
             */
/*
            public Update update;

            public static class Update {
                public String loc;
                public String utc;
            }
        }

        public static class Now {
            /**
             * code : 101
             * txt : 多云
             */
/*
            public Cond cond;
            public String fl;
            public String hum;
            public String pcpn;
            public String pres;
            public String tmp;
            public String vis;
            /**
             * deg : 122
             * dir : 东北风
             * sc : 3-4
             * spd : 13
             */
/*
            public Wind wind;

            public static class Cond {
                public String code;
                public String txt;
            }

            public static class Wind {
                public String deg;
                public String dir;
                public String sc;
                public String spd;
            }
        }

        public static class Suggestion {
            /**
             * brf : 舒适
             * txt : 白天不太热也不太冷，风力不大，相信您在这样的天气条件下，应会感到比较清爽和舒适。
             */
/*
            public Comf comf;
            /**
             * brf : 不宜
             * txt : 不宜洗车，未来24小时内有霾，如果在此期间洗车，会弄脏您的爱车。
             */
/*
            public Cw cw;
            /**
             * brf : 舒适
             * txt : 建议着长袖T恤、衬衫加单裤等服装。年老体弱者宜着针织长袖衬衫、马甲和长裤。
             */
/*
            public Drsg drsg;
            /**
             * brf : 较易发
             * txt : 天气转凉，空气湿度较大，较易发生感冒，体质较弱的朋友请注意适当防护。
             */
/*
            public Flu flu;
            /**
             * brf : 较不宜
             * txt : 有扬沙或浮尘，建议适当停止户外运动，选择在室内进行运动，以避免吸入更多沙尘，有损健康。
             */
/*
            public Sport sport;
            /**
             * brf : 较不宜
             * txt : 空气质量差，不适宜旅游
             */
/*
            public Trav trav;
            /**
             * brf : 最弱
             * txt : 属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。
             */
/*
            public Uv uv;

            public static class Comf {
                public String brf;
                public String txt;
            }

            public static class Cw {
                public String brf;
                public String txt;
            }

            public static class Drsg {
                public String brf;
                public String txt;
            }

            public static class Flu {
                public String brf;
                public String txt;
            }

            public static class Sport {
                public String brf;
                public String txt;
            }

            public static class Trav {
                public String brf;
                public String txt;
            }

            public static class Uv {
                public String brf;
                public String txt;
            }
        }

        public static class DailyForecast {
            /**
             * sr : 06:10
             * ss : 17:57
             */
/*
            public Astro astro;
            /**
             * code_d : 502
             * code_n : 502
             * txt_d : 霾
             * txt_n : 霾
             */
/*
            public Cond cond;
            public String date;
            public String hum;
            public String pcpn;
            public String pop;
            public String pres;
            /**
             * max : 25
             * min : 15
             */
/*
            public Tmp tmp;
            public String vis;
            /**
             * deg : 182
             * dir : 无持续风向
             * sc : 微风
             * spd : 8
             */
/*
            public Wind wind;

            public static class Astro {
                public String sr;
                public String ss;
            }

            public static class Cond {
                public String code_d;
                public String code_n;
                public String txt_d;
                public String txt_n;
            }

            public static class Tmp {
                public String max;
                public String min;
            }

            public static class Wind {
                public String deg;
                public String dir;
                public String sc;
                public String spd;
            }
        }

        public static class HourlyForecast {
            public String date;
            public String hum;
            public String pop;
            public String pres;
            public String tmp;
            /**
             * deg : 146
             * dir : 东南风
             * sc : 微风
             * spd : 6
             */
/*
            public Wind wind;

            public static class Wind {
                public String deg;
                public String dir;
                public String sc;
                public String spd;
            }
        }
    }
}
*/