package wangdaye.com.geometricweather.data.entity.result;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Juhe result.
 * */

public class JuheResult {

    /**
     * reason : successed!
     * result : {"data":{"realtime":{"city_code":"101120201","city_name":"青岛","date":"2016-09-29","time":"08:00:00","week":4,"moon":"八月廿九","dataUptime":1475108278,"weather":{"temperature":"14","humidity":"56","info":"阴","img":"2"},"wind":{"direct":"北风","power":"4级","offset":null,"windspeed":null}},"life":{"date":"2016-9-29","info":{"chuanyi":["较舒适","建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。"],"ganmao":["易发","天冷风大且昼夜温差也很大，易发生感冒，请注意适当增减衣服。"],"kongtiao":["较少开启","您将感到很舒适，一般不需要开启空调。"],"xiche":["较不宜","较不宜洗车，未来一天无雨，风力较大，如果执意擦洗汽车，要做好蒙上污垢的心理准备。"],"yundong":["较适宜","天气较好，但风力较大，推荐您进行室内运动，若在户外运动请注意避风保暖。"],"ziwaixian":["弱","紫外线强度较弱，建议出门前涂擦SPF在12-15之间、PA+的防晒护肤品。"]}},"weather":[{"date":"2016-09-29","info":{"day":["1","多云","22","东北风","4-5 级","05:52"],"night":["1","多云","15","东北风","3-4 级","17:45"]},"week":"四","nongli":"八月廿九"},{"date":"2016-09-30","info":{"dawn":["1","多云","15","东北风","3-4 级","17:45"],"day":["1","多云","22","北风","3-4 级","05:53"],"night":["1","多云","17","北风","3-4 级","17:43"]},"week":"五","nongli":"八月三十"},{"date":"2016-10-01","info":{"dawn":["1","多云","17","北风","3-4 级","17:43"],"day":["1","多云","25","北风","3-4 级","05:53"],"night":["3","阵雨","21","南风","3-4 级","17:42"]},"week":"六","nongli":"九月初一"},{"date":"2016-10-02","info":{"dawn":["3","阵雨","21","南风","3-4 级","17:42"],"day":["7","小雨","21","北风","3-4 级","05:54"],"night":["3","阵雨","19","北风","3-4 级","17:41"]},"week":"日","nongli":"九月初二"},{"date":"2016-10-03","info":{"dawn":["3","阵雨","19","北风","3-4 级","17:41"],"day":["1","多云","24","南风","3-4 级","05:55"],"night":["1","多云","19","南风","3-4 级","17:39"]},"week":"一","nongli":"九月初三"}],"f3h":{"temperature":[{"jg":"20160929080000","jb":"14"},{"jg":"20160929110000","jb":"19"},{"jg":"20160929140000","jb":"21"},{"jg":"20160929170000","jb":"20"},{"jg":"20160929200000","jb":"19"},{"jg":"20160929230000","jb":"17"},{"jg":"20160930020000","jb":"17"},{"jg":"20160930050000","jb":"15"},{"jg":"20160930080000","jb":"16"}],"precipitation":[{"jg":"20160929080000","jf":"0"},{"jg":"20160929110000","jf":"0"},{"jg":"20160929140000","jf":"0"},{"jg":"20160929170000","jf":"0"},{"jg":"20160929200000","jf":"0"},{"jg":"20160929230000","jf":"0"},{"jg":"20160930020000","jf":"0"},{"jg":"20160930050000","jf":"0"},{"jg":"20160930080000","jf":"0"}]},"pm25":{"key":"Qingdao","show_desc":0,"pm25":{"curPm":"40","pm25":"17","pm10":"42","level":1,"quality":"优","des":"可正常活动。"},"dateTime":"2016年09月29日07时","cityName":"青岛"},"jingqu":"","jingqutq":"","date":"","isForeign":"0"}}
     * error_code : 0
     */

    public String reason;
    /**
     * data : {"realtime":{"city_code":"101120201","city_name":"青岛","date":"2016-09-29","time":"08:00:00","week":4,"moon":"八月廿九","dataUptime":1475108278,"weather":{"temperature":"14","humidity":"56","info":"阴","img":"2"},"wind":{"direct":"北风","power":"4级","offset":null,"windspeed":null}},"life":{"date":"2016-9-29","info":{"chuanyi":["较舒适","建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。"],"ganmao":["易发","天冷风大且昼夜温差也很大，易发生感冒，请注意适当增减衣服。"],"kongtiao":["较少开启","您将感到很舒适，一般不需要开启空调。"],"xiche":["较不宜","较不宜洗车，未来一天无雨，风力较大，如果执意擦洗汽车，要做好蒙上污垢的心理准备。"],"yundong":["较适宜","天气较好，但风力较大，推荐您进行室内运动，若在户外运动请注意避风保暖。"],"ziwaixian":["弱","紫外线强度较弱，建议出门前涂擦SPF在12-15之间、PA+的防晒护肤品。"]}},"weather":[{"date":"2016-09-29","info":{"day":["1","多云","22","东北风","4-5 级","05:52"],"night":["1","多云","15","东北风","3-4 级","17:45"]},"week":"四","nongli":"八月廿九"},{"date":"2016-09-30","info":{"dawn":["1","多云","15","东北风","3-4 级","17:45"],"day":["1","多云","22","北风","3-4 级","05:53"],"night":["1","多云","17","北风","3-4 级","17:43"]},"week":"五","nongli":"八月三十"},{"date":"2016-10-01","info":{"dawn":["1","多云","17","北风","3-4 级","17:43"],"day":["1","多云","25","北风","3-4 级","05:53"],"night":["3","阵雨","21","南风","3-4 级","17:42"]},"week":"六","nongli":"九月初一"},{"date":"2016-10-02","info":{"dawn":["3","阵雨","21","南风","3-4 级","17:42"],"day":["7","小雨","21","北风","3-4 级","05:54"],"night":["3","阵雨","19","北风","3-4 级","17:41"]},"week":"日","nongli":"九月初二"},{"date":"2016-10-03","info":{"dawn":["3","阵雨","19","北风","3-4 级","17:41"],"day":["1","多云","24","南风","3-4 级","05:55"],"night":["1","多云","19","南风","3-4 级","17:39"]},"week":"一","nongli":"九月初三"}],"f3h":{"temperature":[{"jg":"20160929080000","jb":"14"},{"jg":"20160929110000","jb":"19"},{"jg":"20160929140000","jb":"21"},{"jg":"20160929170000","jb":"20"},{"jg":"20160929200000","jb":"19"},{"jg":"20160929230000","jb":"17"},{"jg":"20160930020000","jb":"17"},{"jg":"20160930050000","jb":"15"},{"jg":"20160930080000","jb":"16"}],"precipitation":[{"jg":"20160929080000","jf":"0"},{"jg":"20160929110000","jf":"0"},{"jg":"20160929140000","jf":"0"},{"jg":"20160929170000","jf":"0"},{"jg":"20160929200000","jf":"0"},{"jg":"20160929230000","jf":"0"},{"jg":"20160930020000","jf":"0"},{"jg":"20160930050000","jf":"0"},{"jg":"20160930080000","jf":"0"}]},"pm25":{"key":"Qingdao","show_desc":0,"pm25":{"curPm":"40","pm25":"17","pm10":"42","level":1,"quality":"优","des":"可正常活动。"},"dateTime":"2016年09月29日07时","cityName":"青岛"},"jingqu":"","jingqutq":"","date":"","isForeign":"0"}
     */

    public Result result;
    public int error_code;

    public static class Result {
        /**
         * realtime : {"city_code":"101120201","city_name":"青岛","date":"2016-09-29","time":"08:00:00","week":4,"moon":"八月廿九","dataUptime":1475108278,"weather":{"temperature":"14","humidity":"56","info":"阴","img":"2"},"wind":{"direct":"北风","power":"4级","offset":null,"windspeed":null}}
         * life : {"date":"2016-9-29","info":{"chuanyi":["较舒适","建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。"],"ganmao":["易发","天冷风大且昼夜温差也很大，易发生感冒，请注意适当增减衣服。"],"kongtiao":["较少开启","您将感到很舒适，一般不需要开启空调。"],"xiche":["较不宜","较不宜洗车，未来一天无雨，风力较大，如果执意擦洗汽车，要做好蒙上污垢的心理准备。"],"yundong":["较适宜","天气较好，但风力较大，推荐您进行室内运动，若在户外运动请注意避风保暖。"],"ziwaixian":["弱","紫外线强度较弱，建议出门前涂擦SPF在12-15之间、PA+的防晒护肤品。"]}}
         * weather : [{"date":"2016-09-29","info":{"day":["1","多云","22","东北风","4-5 级","05:52"],"night":["1","多云","15","东北风","3-4 级","17:45"]},"week":"四","nongli":"八月廿九"},{"date":"2016-09-30","info":{"dawn":["1","多云","15","东北风","3-4 级","17:45"],"day":["1","多云","22","北风","3-4 级","05:53"],"night":["1","多云","17","北风","3-4 级","17:43"]},"week":"五","nongli":"八月三十"},{"date":"2016-10-01","info":{"dawn":["1","多云","17","北风","3-4 级","17:43"],"day":["1","多云","25","北风","3-4 级","05:53"],"night":["3","阵雨","21","南风","3-4 级","17:42"]},"week":"六","nongli":"九月初一"},{"date":"2016-10-02","info":{"dawn":["3","阵雨","21","南风","3-4 级","17:42"],"day":["7","小雨","21","北风","3-4 级","05:54"],"night":["3","阵雨","19","北风","3-4 级","17:41"]},"week":"日","nongli":"九月初二"},{"date":"2016-10-03","info":{"dawn":["3","阵雨","19","北风","3-4 级","17:41"],"day":["1","多云","24","南风","3-4 级","05:55"],"night":["1","多云","19","南风","3-4 级","17:39"]},"week":"一","nongli":"九月初三"}]
         * f3h : {"temperature":[{"jg":"20160929080000","jb":"14"},{"jg":"20160929110000","jb":"19"},{"jg":"20160929140000","jb":"21"},{"jg":"20160929170000","jb":"20"},{"jg":"20160929200000","jb":"19"},{"jg":"20160929230000","jb":"17"},{"jg":"20160930020000","jb":"17"},{"jg":"20160930050000","jb":"15"},{"jg":"20160930080000","jb":"16"}],"precipitation":[{"jg":"20160929080000","jf":"0"},{"jg":"20160929110000","jf":"0"},{"jg":"20160929140000","jf":"0"},{"jg":"20160929170000","jf":"0"},{"jg":"20160929200000","jf":"0"},{"jg":"20160929230000","jf":"0"},{"jg":"20160930020000","jf":"0"},{"jg":"20160930050000","jf":"0"},{"jg":"20160930080000","jf":"0"}]}
         * pm25 : {"key":"Qingdao","show_desc":0,"pm25":{"curPm":"40","pm25":"17","pm10":"42","level":1,"quality":"优","des":"可正常活动。"},"dateTime":"2016年09月29日07时","cityName":"青岛"}
         * jingqu :
         * jingqutq :
         * date :
         * isForeign : 0
         */

        public Data data;

        public static class Data {
            /**
             * city_code : 101120201
             * city_name : 青岛
             * date : 2016-09-29
             * time : 08:00:00
             * week : 4
             * moon : 八月廿九
             * dataUptime : 1475108278
             * weather : {"temperature":"14","humidity":"56","info":"阴","img":"2"}
             * wind : {"direct":"北风","power":"4级","offset":null,"windspeed":null}
             */

            public Realtime realtime;
            /**
             * date : 2016-9-29
             * info : {"chuanyi":["较舒适","建议着薄外套、开衫牛仔衫裤等服装。年老体弱者应适当添加衣物，宜着夹克衫、薄毛衣等。"],"ganmao":["易发","天冷风大且昼夜温差也很大，易发生感冒，请注意适当增减衣服。"],"kongtiao":["较少开启","您将感到很舒适，一般不需要开启空调。"],"xiche":["较不宜","较不宜洗车，未来一天无雨，风力较大，如果执意擦洗汽车，要做好蒙上污垢的心理准备。"],"yundong":["较适宜","天气较好，但风力较大，推荐您进行室内运动，若在户外运动请注意避风保暖。"],"ziwaixian":["弱","紫外线强度较弱，建议出门前涂擦SPF在12-15之间、PA+的防晒护肤品。"]}
             */

            public Life life;
            public F3h f3h;
            /**
             * key : Qingdao
             * show_desc : 0
             * pm25 : {"curPm":"40","pm25":"17","pm10":"42","level":1,"quality":"优","des":"可正常活动。"}
             * dateTime : 2016年09月29日07时
             * cityName : 青岛
             */

            @SerializedName("pm25")
            public Aqi aqi;
            public String jingqu;
            public String jingqutq;
            public String date;
            public String isForeign;
            /**
             * date : 2016-09-29
             * info : {"day":["1","多云","22","东北风","4-5 级","05:52"],"night":["1","多云","15","东北风","3-4 级","17:45"]}
             * week : 四
             * nongli : 八月廿九
             */

            public List<Weather> weather;

            public static class Realtime {
                public String city_code;
                public String city_name;
                public String date;
                public String time;
                public int week;
                public String moon;
                public int dataUptime;
                /**
                 * temperature : 14
                 * humidity : 56
                 * info : 阴
                 * img : 2
                 */

                public Weather weather;
                /**
                 * direct : 北风
                 * power : 4级
                 * offset : null
                 * windspeed : null
                 */

                public Wind wind;

                public static class Weather {
                    public String temperature;
                    public String humidity;
                    public String info;
                    public String img;
                }

                public static class Wind {
                    public String direct;
                    public String power;
                    public Object offset;
                    public Object windspeed;
                }
            }

            public static class Life {
                public String date;
                public Info info;

                public static class Info {
                    public List<String> chuanyi;
                    public List<String> ganmao;
                    public List<String> kongtiao;
                    public List<String> xiche;
                    public List<String> yundong;
                    public List<String> ziwaixian;
                }
            }

            public static class F3h {
                /**
                 * jg : 20160929080000
                 * jb : 14
                 */

                public List<Temperature> temperature;
                /**
                 * jg : 20160929080000
                 * jf : 0
                 */

                public List<Precipitation> precipitation;

                public static class Temperature {
                    public String jg;
                    public String jb;
                }

                public static class Precipitation {
                    public String jg;
                    public String jf;
                }
            }

            public static class Aqi {
                public String key;
                public int show_desc;
                /**
                 * curPm : 40
                 * pm25 : 17
                 * pm10 : 42
                 * level : 1
                 * quality : 优
                 * des : 可正常活动。
                 */

                public Pm25 pm25;
                public String dateTime;
                public String cityName;

                public static class Pm25 {
                    public String curPm;
                    public String pm25;
                    public String pm10;
                    public int level;
                    public String quality;
                    public String des;
                }
            }

            public static class Weather {
                public String date;
                public Info info;
                public String week;
                public String nongli;

                public static class Info {
                    public List<String> day;
                    public List<String> night;
                }
            }
        }
    }
}
