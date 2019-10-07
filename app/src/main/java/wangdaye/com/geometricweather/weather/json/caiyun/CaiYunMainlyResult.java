package wangdaye.com.geometricweather.weather.json.caiyun;

import java.util.Date;
import java.util.List;

public class CaiYunMainlyResult {

    /**
     * current : {"feelsLike":{"unit":"℃","value":"17"},"humidity":{"unit":"%","value":"94"},"pressure":{"unit":"mb","value":"1012.0"},"pubTime":"2019-10-02T20:57:32+08:00","temperature":{"unit":"℃","value":"18"},"uvIndex":"0","visibility":{"unit":"km","value":""},"weather":"0","wind":{"direction":{"unit":"°","value":"45"},"speed":{"unit":"km/h","value":"3.7"}}}
     * forecastDaily : {"aqi":{"brandInfo":{"brands":[{"brandId":"caiyun","logo":"http://f5.market.mi-img.com/download/MiSafe/07fa34263d698a7a9a8050dde6a7c63f8f243dbf3/a.webp","names":{"zh_TW":"彩雲天氣","en_US":"彩云天气","zh_CN":"彩云天气"},"url":""}]},"pubTime":"2019-10-02T00:00:00+08:00","status":0,"value":[75,0,0,0,0,0,0,0,0,0,0,0,0,0,0]},"precipitationProbability":{"status":0,"value":["15","0","25","0","0"]},"pubTime":"2019-10-02T20:00:00+08:00","status":0,"sunRiseSet":{"status":0,"value":[{"from":"2019-10-02T05:50:00+08:00","to":"2019-10-02T17:38:00+08:00"},{"from":"2019-10-03T05:51:00+08:00","to":"2019-10-03T17:37:00+08:00"},{"from":"2019-10-04T05:52:00+08:00","to":"2019-10-04T17:35:00+08:00"},{"from":"2019-10-05T05:53:00+08:00","to":"2019-10-05T17:34:00+08:00"},{"from":"2019-10-06T05:53:00+08:00","to":"2019-10-06T17:32:00+08:00"},{"from":"2019-10-07T05:54:00+08:00","to":"2019-10-07T17:31:00+08:00"},{"from":"2019-10-08T05:55:00+08:00","to":"2019-10-08T17:29:00+08:00"},{"from":"2019-10-09T05:56:00+08:00","to":"2019-10-09T17:28:00+08:00"},{"from":"2019-10-10T05:57:00+08:00","to":"2019-10-10T17:26:00+08:00"},{"from":"2019-10-11T05:58:00+08:00","to":"2019-10-11T17:25:00+08:00"},{"from":"2019-10-12T05:59:00+08:00","to":"2019-10-12T17:23:00+08:00"},{"from":"2019-10-13T06:00:00+08:00","to":"2019-10-13T17:22:00+08:00"},{"from":"2019-10-14T06:01:00+08:00","to":"2019-10-14T17:20:00+08:00"},{"from":"2019-10-15T06:02:00+08:00","to":"2019-10-15T17:19:00+08:00"},{"from":"2019-10-16T06:03:00+08:00","to":"2019-10-16T17:17:00+08:00"}]},"temperature":{"status":0,"unit":"℃","value":[{"from":"25","to":"17"},{"from":"23","to":"11"},{"from":"13","to":"9"},{"from":"18","to":"10"},{"from":"19","to":"10"},{"from":"22","to":"16"},{"from":"20","to":"13"},{"from":"16","to":"13"},{"from":"18","to":"15"},{"from":"17","to":"14"},{"from":"17","to":"15"},{"from":"18","to":"15"},{"from":"18","to":"16"},{"from":"19","to":"17"},{"from":"19","to":"18"}]},"weather":{"status":0,"value":[{"from":"0","to":"0"},{"from":"0","to":"1"},{"from":"2","to":"1"},{"from":"0","to":"0"},{"from":"0","to":"1"},{"from":"1","to":"1"},{"from":"0","to":"0"},{"from":"0","to":"2"},{"from":"2","to":"1"},{"from":"2","to":"1"},{"from":"1","to":"0"},{"from":"0","to":"0"},{"from":"0","to":"1"},{"from":"1","to":"1"},{"from":"1","to":"1"}]},"wind":{"direction":{"status":0,"unit":"°","value":[{"from":"45","to":"45"},{"from":"0","to":"0"},{"from":"0","to":"0"},{"from":"0","to":"0"},{"from":"225","to":"225"},{"from":"225","to":"225"},{"from":"0","to":"0"},{"from":"225","to":"225"},{"from":"225","to":"270"},{"from":"270","to":"135"},{"from":"45","to":"0"},{"from":"0","to":"45"},{"from":"45","to":"45"},{"from":"270","to":"225"},{"from":"180","to":"180"}]},"speed":{"status":0,"unit":"km/h","value":[{"from":"3.7","to":"3.7"},{"from":"50.0","to":"50.0"},{"from":"50.0","to":"50.0"},{"from":"50.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"20.0","to":"0.0"},{"from":"20.0","to":"20.0"},{"from":"0.0","to":"29.0"},{"from":"20.0","to":"0.0"},{"from":"0.0","to":"0.0"},{"from":"0.0","to":"0.0"}]}}}
     * forecastHourly : {"aqi":{"brandInfo":{"brands":[{"brandId":"caiyun","logo":"http://f5.market.mi-img.com/download/MiSafe/07fa34263d698a7a9a8050dde6a7c63f8f243dbf3/a.webp","names":{"zh_TW":"彩雲天氣","en_US":"彩云天气","zh_CN":"彩云天气"},"url":""}]},"pubTime":"2019-10-02T22:00:00+08:00","status":0,"value":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]},"desc":"逐小时预报","status":0,"temperature":{"pubTime":"2019-10-02T22:00:00+08:00","status":0,"unit":"℃","value":[17,17,17,17,18,18,18,18,19,19,20,20,21,23,22,22,21,21,21,20,18,16,16]},"weather":{"pubTime":"2019-10-02T22:00:00+08:00","status":0,"value":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]},"wind":{"status":0,"value":[{"datetime":"2019-10-02T22:00:00.000+08:00","direction":"327.28","speed":"8.96"},{"datetime":"2019-10-02T23:00:00.000+08:00","direction":"322.45","speed":"12.45"},{"datetime":"2019-10-03T00:00:00.000+08:00","direction":"326.37","speed":"15.21"},{"datetime":"2019-10-03T01:00:00.000+08:00","direction":"332.69","speed":"15.06"},{"datetime":"2019-10-03T02:00:00.000+08:00","direction":"330.49","speed":"15.04"},{"datetime":"2019-10-03T03:00:00.000+08:00","direction":"331.71","speed":"15.76"},{"datetime":"2019-10-03T04:00:00.000+08:00","direction":"326.13","speed":"16.8"},{"datetime":"2019-10-03T05:00:00.000+08:00","direction":"315.11","speed":"20.21"},{"datetime":"2019-10-03T06:00:00.000+08:00","direction":"311.14","speed":"20.39"},{"datetime":"2019-10-03T07:00:00.000+08:00","direction":"313.99","speed":"20.48"},{"datetime":"2019-10-03T08:00:00.000+08:00","direction":"315.05","speed":"20.88"},{"datetime":"2019-10-03T09:00:00.000+08:00","direction":"327.01","speed":"19.58"},{"datetime":"2019-10-03T10:00:00.000+08:00","direction":"351.85","speed":"22.96"},{"datetime":"2019-10-03T11:00:00.000+08:00","direction":"7.12","speed":"26.56"},{"datetime":"2019-10-03T12:00:00.000+08:00","direction":"1.14","speed":"28.93"},{"datetime":"2019-10-03T13:00:00.000+08:00","direction":"356.62","speed":"30.45"},{"datetime":"2019-10-03T14:00:00.000+08:00","direction":"354.95","speed":"34.27"},{"datetime":"2019-10-03T15:00:00.000+08:00","direction":"359.29","speed":"34.82"},{"datetime":"2019-10-03T16:00:00.000+08:00","direction":"358.46","speed":"35.2"},{"datetime":"2019-10-03T17:00:00.000+08:00","direction":"358.83","speed":"37.99"},{"datetime":"2019-10-03T18:00:00.000+08:00","direction":"5.76","speed":"35.79"},{"datetime":"2019-10-03T19:00:00.000+08:00","direction":"12.38","speed":"37.09"},{"datetime":"2019-10-03T20:00:00.000+08:00","direction":"15.31","speed":"38.93"}]}}
     * indices : {"indices":[{"type":"uvIndex","value":"6"},{"type":"humidity","value":"94"},{"type":"feelsLike","value":"17"},{"type":"pressure","value":"1012.0"},{"type":"carWash","value":"1"},{"type":"sports","value":"1"}],"pubTime":"","status":0}
     * alerts : [{"locationKey":"weathercn:101070205","images":{"icon":"http://f4.market.xiaomi.com/download/MiSafe/02efa4e1664fb1decb148d4a1fd0c36f1b840a224/a.webp","notice":"http://f3.market.xiaomi.com/download/MiSafe/0034524cf8c2543333dc2ab7fef54c2d3ca6fa0a0/a.webp"},"level":"橙色","defense":[{"defenseText":"注意防护，佩戴防护口罩","defenseIcon":"http://f3.market.mi-img.com/download/Weather/004784c916d30b58ea08abe5a1381055540424fd1/20190220170058_681.png"},{"defenseText":"小心驾驶，减速慢行","defenseIcon":"http://f3.market.mi-img.com/download/Weather/00154470362bba8e0bd999f2a36a148465341d31a/20190220170150_60.png"}],"pubTime":"2019-10-02T19:56:10+08:00","alertId":"weathercn:101070205-1570017370000-大雾橙色","detail":"大连市气象台2019年10月02日19时56分发布大雾橙色预警信号：预计2日20时到3日8时，大连地区及沿岸海域将出现能见度小于200米的浓雾天气，请注意防范。","title":"旅顺发布大雾橙色预警","type":"大雾"},{"locationKey":"weathercn:101070205","images":{"icon":"http://f5.market.xiaomi.com/download/MiSafe/0f9daf5ba050f4eda2eea7bbebb22d33bbefa7e48/a.webp","notice":"http://f5.market.xiaomi.com/download/MiSafe/0698347331408e6b152610a85b32892b64f404343/a.webp"},"level":"蓝色","defense":[{"defenseText":"关好门窗，注意室内空气净化","defenseIcon":"http://f4.market.mi-img.com/download/Weather/00154470362bba8e0bd998f2a86a188469341d31a/20190220170024_798.png"},{"defenseText":"注意防护，佩戴防护口罩","defenseIcon":"http://f3.market.mi-img.com/download/Weather/004784c916d30b58ea08abe5a1381055540424fd1/20190220170058_681.png"},{"defenseText":"小心驾驶，减速慢行","defenseIcon":"http://f3.market.mi-img.com/download/Weather/00154470362bba8e0bd999f2a36a148465341d31a/20190220170150_60.png"},{"defenseText":"小心坠物，远离路牌等","defenseIcon":"http://f3.market.xiaomi.com/download/Weather/00154470362bba8e0bd999f2ab6a1c846f341d31a/20190220170206_984.png"}],"pubTime":"2019-10-02T15:10:45+08:00","alertId":"weathercn:101070205-1570000245000-大风蓝色","detail":"大连市气象台2019年10月02日15时10分发布大风蓝色预警信号：预计2日夜间到3日白天，渤海西南风5到6级3日早晨转偏北风6到7级阵风8级傍晚增强到7到8级阵风9级，渤海海峡偏北风5到6级3日上午增强到6到7级阵风8级傍晚增强到7到8级阵风9级，黄海北部偏北风6级阵风7到8级，陆地偏北风4到5级3日上午增强到5到6级阵风7级3日傍晚增强到6到7级阵风8级，请注意防范。","title":"旅顺发布大风蓝色预警","type":"大风"}]
     * yesterday : {"aqi":"","date":"2019-10-01T12:00:00+08:00","status":0,"sunRise":"2019-10-01T05:49:00+08:00","sunSet":"2019-10-01T17:40:00+08:00","tempMax":"25","tempMin":"19","weatherEnd":"0","weatherStart":"0","windDircEnd":"135","windDircStart":"135","windSpeedEnd":"29.0","windSpeedStart":"29.0"}
     * url : {"weathercn":"","caiyun":""}
     * brandInfo : {"brands":[{"brandId":"caiyun","logo":"http://t5.market.xiaomi.com/download/MiSafe/0d74f4cf0f60ede546bc7549b9c35bc3a9e4149b3/a.webp","names":{"zh_TW":"彩雲天氣","en_US":"彩云天气","zh_CN":"彩云天气"},"url":""},{"brandId":"weatherbj","logo":"","names":{"zh_TW":"北京气象局","en_US":"北京气象局","zh_CN":"北京气象局"},"url":""}]}
     * updateTime : 1570022468110
     * aqi : {"pm10Desc":"PM10对人的影响要大于其他任何污染物，长期暴露于污染环境可能导致罹患心血管和呼吸道疾病甚至肺癌","o3":"90","src":"中国环境监测总站","pubTime":"2019-10-02T20:00:00+08:00","pm10":"72","suggest":"空气质量可以接受，可能对少数异常敏感的人群健康有较弱影响","co":"0.84","o3Desc":"空气中过多臭氧可能导致呼吸问题，引发哮喘，降低肺功能并引起肺部疾病，对人类健康影响较大","no2":"29","so2Desc":"二氧化硫是一种无色气体，当空气中SO2达到一定浓度时，空气中会有刺鼻的气味","coDesc":"一氧化碳八成来自汽车尾气，交通高峰期时，公路沿线产生的CO浓度会高于平常","pm25":"54","so2":"6","aqi":"75","pm25Desc":"PM2.5指的是直径小于或等于2.5微米的颗粒物，又称为细颗粒物","no2Desc":"二氧化氮是硝酸盐气溶胶的主要来源，是构成PM2.5和紫外线作用下产生臭氧的主要成分","brandInfo":{"brands":[{"names":{"zh_TW":"中國環境監測總站","en_US":"CNEMC","zh_CN":"中国环境监测总站"},"brandId":"CNEMC","logo":"","url":""}]},"primary":"","status":0}
     */

    public CurrentBean current;
    public ForecastDailyBean forecastDaily;
    public ForecastHourlyBean forecastHourly;
    public IndicesBeanX indices;
    public YesterdayBean yesterday;
    public UrlBean url;
    public BrandInfoBeanXX brandInfo;
    public long updateTime;
    public AqiBeanXX aqi;
    public List<AlertsBean> alerts;

    public CurrentBean getCurrent() {
        return current;
    }

    public void setCurrent(CurrentBean current) {
        this.current = current;
    }

    public ForecastDailyBean getForecastDaily() {
        return forecastDaily;
    }

    public void setForecastDaily(ForecastDailyBean forecastDaily) {
        this.forecastDaily = forecastDaily;
    }

    public ForecastHourlyBean getForecastHourly() {
        return forecastHourly;
    }

    public void setForecastHourly(ForecastHourlyBean forecastHourly) {
        this.forecastHourly = forecastHourly;
    }

    public IndicesBeanX getIndices() {
        return indices;
    }

    public void setIndices(IndicesBeanX indices) {
        this.indices = indices;
    }

    public YesterdayBean getYesterday() {
        return yesterday;
    }

    public void setYesterday(YesterdayBean yesterday) {
        this.yesterday = yesterday;
    }

    public UrlBean getUrl() {
        return url;
    }

    public void setUrl(UrlBean url) {
        this.url = url;
    }

    public BrandInfoBeanXX getBrandInfo() {
        return brandInfo;
    }

    public void setBrandInfo(BrandInfoBeanXX brandInfo) {
        this.brandInfo = brandInfo;
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(long updateTime) {
        this.updateTime = updateTime;
    }

    public AqiBeanXX getAqi() {
        return aqi;
    }

    public void setAqi(AqiBeanXX aqi) {
        this.aqi = aqi;
    }

    public List<AlertsBean> getAlerts() {
        return alerts;
    }

    public void setAlerts(List<AlertsBean> alerts) {
        this.alerts = alerts;
    }

    public static class CurrentBean {
        /**
         * feelsLike : {"unit":"℃","value":"17"}
         * humidity : {"unit":"%","value":"94"}
         * pressure : {"unit":"mb","value":"1012.0"}
         * pubTime : 2019-10-02T20:57:32+08:00
         * temperature : {"unit":"℃","value":"18"}
         * uvIndex : 0
         * visibility : {"unit":"km","value":""}
         * weather : 0
         * wind : {"direction":{"unit":"°","value":"45"},"speed":{"unit":"km/h","value":"3.7"}}
         */

        public FeelsLikeBean feelsLike;
        public HumidityBean humidity;
        public PressureBean pressure;
        public Date pubTime;
        public TemperatureBean temperature;
        public String uvIndex;
        public VisibilityBean visibility;
        public String weather;
        public WindBean wind;

        public FeelsLikeBean getFeelsLike() {
            return feelsLike;
        }

        public void setFeelsLike(FeelsLikeBean feelsLike) {
            this.feelsLike = feelsLike;
        }

        public HumidityBean getHumidity() {
            return humidity;
        }

        public void setHumidity(HumidityBean humidity) {
            this.humidity = humidity;
        }

        public PressureBean getPressure() {
            return pressure;
        }

        public void setPressure(PressureBean pressure) {
            this.pressure = pressure;
        }

        public Date getPubTime() {
            return pubTime;
        }

        public void setPubTime(Date pubTime) {
            this.pubTime = pubTime;
        }

        public TemperatureBean getTemperature() {
            return temperature;
        }

        public void setTemperature(TemperatureBean temperature) {
            this.temperature = temperature;
        }

        public String getUvIndex() {
            return uvIndex;
        }

        public void setUvIndex(String uvIndex) {
            this.uvIndex = uvIndex;
        }

        public VisibilityBean getVisibility() {
            return visibility;
        }

        public void setVisibility(VisibilityBean visibility) {
            this.visibility = visibility;
        }

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public WindBean getWind() {
            return wind;
        }

        public void setWind(WindBean wind) {
            this.wind = wind;
        }

        public static class FeelsLikeBean {
            /**
             * unit : ℃
             * value : 17
             */

            public String unit;
            public String value;

            public String getUnit() {
                return unit;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        public static class HumidityBean {
            /**
             * unit : %
             * value : 94
             */

            public String unit;
            public String value;

            public String getUnit() {
                return unit;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        public static class PressureBean {
            /**
             * unit : mb
             * value : 1012.0
             */

            public String unit;
            public String value;

            public String getUnit() {
                return unit;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        public static class TemperatureBean {
            /**
             * unit : ℃
             * value : 18
             */

            public String unit;
            public String value;

            public String getUnit() {
                return unit;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        public static class VisibilityBean {
            /**
             * unit : km
             * value : 
             */

            public String unit;
            public String value;

            public String getUnit() {
                return unit;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        public static class WindBean {
            /**
             * direction : {"unit":"°","value":"45"}
             * speed : {"unit":"km/h","value":"3.7"}
             */

            public DirectionBean direction;
            public SpeedBean speed;

            public DirectionBean getDirection() {
                return direction;
            }

            public void setDirection(DirectionBean direction) {
                this.direction = direction;
            }

            public SpeedBean getSpeed() {
                return speed;
            }

            public void setSpeed(SpeedBean speed) {
                this.speed = speed;
            }

            public static class DirectionBean {
                /**
                 * unit : °
                 * value : 45
                 */

                public String unit;
                public String value;

                public String getUnit() {
                    return unit;
                }

                public void setUnit(String unit) {
                    this.unit = unit;
                }

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }

            public static class SpeedBean {
                /**
                 * unit : km/h
                 * value : 3.7
                 */

                public String unit;
                public String value;

                public String getUnit() {
                    return unit;
                }

                public void setUnit(String unit) {
                    this.unit = unit;
                }

                public String getValue() {
                    return value;
                }

                public void setValue(String value) {
                    this.value = value;
                }
            }
        }
    }

    public static class ForecastDailyBean {
        /**
         * aqi : {"brandInfo":{"brands":[{"brandId":"caiyun","logo":"http://f5.market.mi-img.com/download/MiSafe/07fa34263d698a7a9a8050dde6a7c63f8f243dbf3/a.webp","names":{"zh_TW":"彩雲天氣","en_US":"彩云天气","zh_CN":"彩云天气"},"url":""}]},"pubTime":"2019-10-02T00:00:00+08:00","status":0,"value":[75,0,0,0,0,0,0,0,0,0,0,0,0,0,0]}
         * precipitationProbability : {"status":0,"value":["15","0","25","0","0"]}
         * pubTime : 2019-10-02T20:00:00+08:00
         * status : 0
         * sunRiseSet : {"status":0,"value":[{"from":"2019-10-02T05:50:00+08:00","to":"2019-10-02T17:38:00+08:00"},{"from":"2019-10-03T05:51:00+08:00","to":"2019-10-03T17:37:00+08:00"},{"from":"2019-10-04T05:52:00+08:00","to":"2019-10-04T17:35:00+08:00"},{"from":"2019-10-05T05:53:00+08:00","to":"2019-10-05T17:34:00+08:00"},{"from":"2019-10-06T05:53:00+08:00","to":"2019-10-06T17:32:00+08:00"},{"from":"2019-10-07T05:54:00+08:00","to":"2019-10-07T17:31:00+08:00"},{"from":"2019-10-08T05:55:00+08:00","to":"2019-10-08T17:29:00+08:00"},{"from":"2019-10-09T05:56:00+08:00","to":"2019-10-09T17:28:00+08:00"},{"from":"2019-10-10T05:57:00+08:00","to":"2019-10-10T17:26:00+08:00"},{"from":"2019-10-11T05:58:00+08:00","to":"2019-10-11T17:25:00+08:00"},{"from":"2019-10-12T05:59:00+08:00","to":"2019-10-12T17:23:00+08:00"},{"from":"2019-10-13T06:00:00+08:00","to":"2019-10-13T17:22:00+08:00"},{"from":"2019-10-14T06:01:00+08:00","to":"2019-10-14T17:20:00+08:00"},{"from":"2019-10-15T06:02:00+08:00","to":"2019-10-15T17:19:00+08:00"},{"from":"2019-10-16T06:03:00+08:00","to":"2019-10-16T17:17:00+08:00"}]}
         * temperature : {"status":0,"unit":"℃","value":[{"from":"25","to":"17"},{"from":"23","to":"11"},{"from":"13","to":"9"},{"from":"18","to":"10"},{"from":"19","to":"10"},{"from":"22","to":"16"},{"from":"20","to":"13"},{"from":"16","to":"13"},{"from":"18","to":"15"},{"from":"17","to":"14"},{"from":"17","to":"15"},{"from":"18","to":"15"},{"from":"18","to":"16"},{"from":"19","to":"17"},{"from":"19","to":"18"}]}
         * weather : {"status":0,"value":[{"from":"0","to":"0"},{"from":"0","to":"1"},{"from":"2","to":"1"},{"from":"0","to":"0"},{"from":"0","to":"1"},{"from":"1","to":"1"},{"from":"0","to":"0"},{"from":"0","to":"2"},{"from":"2","to":"1"},{"from":"2","to":"1"},{"from":"1","to":"0"},{"from":"0","to":"0"},{"from":"0","to":"1"},{"from":"1","to":"1"},{"from":"1","to":"1"}]}
         * wind : {"direction":{"status":0,"unit":"°","value":[{"from":"45","to":"45"},{"from":"0","to":"0"},{"from":"0","to":"0"},{"from":"0","to":"0"},{"from":"225","to":"225"},{"from":"225","to":"225"},{"from":"0","to":"0"},{"from":"225","to":"225"},{"from":"225","to":"270"},{"from":"270","to":"135"},{"from":"45","to":"0"},{"from":"0","to":"45"},{"from":"45","to":"45"},{"from":"270","to":"225"},{"from":"180","to":"180"}]},"speed":{"status":0,"unit":"km/h","value":[{"from":"3.7","to":"3.7"},{"from":"50.0","to":"50.0"},{"from":"50.0","to":"50.0"},{"from":"50.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"20.0","to":"0.0"},{"from":"20.0","to":"20.0"},{"from":"0.0","to":"29.0"},{"from":"20.0","to":"0.0"},{"from":"0.0","to":"0.0"},{"from":"0.0","to":"0.0"}]}}
         */

        public AqiBean aqi;
        public PrecipitationProbabilityBean precipitationProbability;
        public String pubTime;
        public int status;
        public SunRiseSetBean sunRiseSet;
        public TemperatureBeanX temperature;
        public WeatherBean weather;
        public WindBeanX wind;

        public AqiBean getAqi() {
            return aqi;
        }

        public void setAqi(AqiBean aqi) {
            this.aqi = aqi;
        }

        public PrecipitationProbabilityBean getPrecipitationProbability() {
            return precipitationProbability;
        }

        public void setPrecipitationProbability(PrecipitationProbabilityBean precipitationProbability) {
            this.precipitationProbability = precipitationProbability;
        }

        public String getPubTime() {
            return pubTime;
        }

        public void setPubTime(String pubTime) {
            this.pubTime = pubTime;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public SunRiseSetBean getSunRiseSet() {
            return sunRiseSet;
        }

        public void setSunRiseSet(SunRiseSetBean sunRiseSet) {
            this.sunRiseSet = sunRiseSet;
        }

        public TemperatureBeanX getTemperature() {
            return temperature;
        }

        public void setTemperature(TemperatureBeanX temperature) {
            this.temperature = temperature;
        }

        public WeatherBean getWeather() {
            return weather;
        }

        public void setWeather(WeatherBean weather) {
            this.weather = weather;
        }

        public WindBeanX getWind() {
            return wind;
        }

        public void setWind(WindBeanX wind) {
            this.wind = wind;
        }

        public static class AqiBean {
            /**
             * brandInfo : {"brands":[{"brandId":"caiyun","logo":"http://f5.market.mi-img.com/download/MiSafe/07fa34263d698a7a9a8050dde6a7c63f8f243dbf3/a.webp","names":{"zh_TW":"彩雲天氣","en_US":"彩云天气","zh_CN":"彩云天气"},"url":""}]}
             * pubTime : 2019-10-02T00:00:00+08:00
             * status : 0
             * value : [75,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
             */

            public BrandInfoBean brandInfo;
            public String pubTime;
            public int status;
            public List<Integer> value;

            public BrandInfoBean getBrandInfo() {
                return brandInfo;
            }

            public void setBrandInfo(BrandInfoBean brandInfo) {
                this.brandInfo = brandInfo;
            }

            public String getPubTime() {
                return pubTime;
            }

            public void setPubTime(String pubTime) {
                this.pubTime = pubTime;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public List<Integer> getValue() {
                return value;
            }

            public void setValue(List<Integer> value) {
                this.value = value;
            }

            public static class BrandInfoBean {
                public List<BrandsBean> brands;

                public List<BrandsBean> getBrands() {
                    return brands;
                }

                public void setBrands(List<BrandsBean> brands) {
                    this.brands = brands;
                }

                public static class BrandsBean {
                    /**
                     * brandId : caiyun
                     * logo : http://f5.market.mi-img.com/download/MiSafe/07fa34263d698a7a9a8050dde6a7c63f8f243dbf3/a.webp
                     * names : {"zh_TW":"彩雲天氣","en_US":"彩云天气","zh_CN":"彩云天气"}
                     * url : 
                     */

                    public String brandId;
                    public String logo;
                    public NamesBean names;
                    public String url;

                    public String getBrandId() {
                        return brandId;
                    }

                    public void setBrandId(String brandId) {
                        this.brandId = brandId;
                    }

                    public String getLogo() {
                        return logo;
                    }

                    public void setLogo(String logo) {
                        this.logo = logo;
                    }

                    public NamesBean getNames() {
                        return names;
                    }

                    public void setNames(NamesBean names) {
                        this.names = names;
                    }

                    public String getUrl() {
                        return url;
                    }

                    public void setUrl(String url) {
                        this.url = url;
                    }

                    public static class NamesBean {
                        /**
                         * zh_TW : 彩雲天氣
                         * en_US : 彩云天气
                         * zh_CN : 彩云天气
                         */

                        public String zh_TW;
                        public String en_US;
                        public String zh_CN;

                        public String getZh_TW() {
                            return zh_TW;
                        }

                        public void setZh_TW(String zh_TW) {
                            this.zh_TW = zh_TW;
                        }

                        public String getEn_US() {
                            return en_US;
                        }

                        public void setEn_US(String en_US) {
                            this.en_US = en_US;
                        }

                        public String getZh_CN() {
                            return zh_CN;
                        }

                        public void setZh_CN(String zh_CN) {
                            this.zh_CN = zh_CN;
                        }
                    }
                }
            }
        }

        public static class PrecipitationProbabilityBean {
            /**
             * status : 0
             * value : ["15","0","25","0","0"]
             */

            public int status;
            public List<String> value;

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public List<String> getValue() {
                return value;
            }

            public void setValue(List<String> value) {
                this.value = value;
            }
        }

        public static class SunRiseSetBean {
            /**
             * status : 0
             * value : [{"from":"2019-10-02T05:50:00+08:00","to":"2019-10-02T17:38:00+08:00"},{"from":"2019-10-03T05:51:00+08:00","to":"2019-10-03T17:37:00+08:00"},{"from":"2019-10-04T05:52:00+08:00","to":"2019-10-04T17:35:00+08:00"},{"from":"2019-10-05T05:53:00+08:00","to":"2019-10-05T17:34:00+08:00"},{"from":"2019-10-06T05:53:00+08:00","to":"2019-10-06T17:32:00+08:00"},{"from":"2019-10-07T05:54:00+08:00","to":"2019-10-07T17:31:00+08:00"},{"from":"2019-10-08T05:55:00+08:00","to":"2019-10-08T17:29:00+08:00"},{"from":"2019-10-09T05:56:00+08:00","to":"2019-10-09T17:28:00+08:00"},{"from":"2019-10-10T05:57:00+08:00","to":"2019-10-10T17:26:00+08:00"},{"from":"2019-10-11T05:58:00+08:00","to":"2019-10-11T17:25:00+08:00"},{"from":"2019-10-12T05:59:00+08:00","to":"2019-10-12T17:23:00+08:00"},{"from":"2019-10-13T06:00:00+08:00","to":"2019-10-13T17:22:00+08:00"},{"from":"2019-10-14T06:01:00+08:00","to":"2019-10-14T17:20:00+08:00"},{"from":"2019-10-15T06:02:00+08:00","to":"2019-10-15T17:19:00+08:00"},{"from":"2019-10-16T06:03:00+08:00","to":"2019-10-16T17:17:00+08:00"}]
             */

            public int status;
            public List<ValueBean> value;

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public List<ValueBean> getValue() {
                return value;
            }

            public void setValue(List<ValueBean> value) {
                this.value = value;
            }

            public static class ValueBean {
                /**
                 * from : 2019-10-02T05:50:00+08:00
                 * to : 2019-10-02T17:38:00+08:00
                 */

                public Date from;
                public Date to;

                public Date getFrom() {
                    return from;
                }

                public void setFrom(Date from) {
                    this.from = from;
                }

                public Date getTo() {
                    return to;
                }

                public void setTo(Date to) {
                    this.to = to;
                }
            }
        }

        public static class TemperatureBeanX {
            /**
             * status : 0
             * unit : ℃
             * value : [{"from":"25","to":"17"},{"from":"23","to":"11"},{"from":"13","to":"9"},{"from":"18","to":"10"},{"from":"19","to":"10"},{"from":"22","to":"16"},{"from":"20","to":"13"},{"from":"16","to":"13"},{"from":"18","to":"15"},{"from":"17","to":"14"},{"from":"17","to":"15"},{"from":"18","to":"15"},{"from":"18","to":"16"},{"from":"19","to":"17"},{"from":"19","to":"18"}]
             */

            public int status;
            public String unit;
            public List<ValueBeanX> value;

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public String getUnit() {
                return unit;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public List<ValueBeanX> getValue() {
                return value;
            }

            public void setValue(List<ValueBeanX> value) {
                this.value = value;
            }

            public static class ValueBeanX {
                /**
                 * from : 25
                 * to : 17
                 */

                public String from;
                public String to;

                public String getFrom() {
                    return from;
                }

                public void setFrom(String from) {
                    this.from = from;
                }

                public String getTo() {
                    return to;
                }

                public void setTo(String to) {
                    this.to = to;
                }
            }
        }

        public static class WeatherBean {
            /**
             * status : 0
             * value : [{"from":"0","to":"0"},{"from":"0","to":"1"},{"from":"2","to":"1"},{"from":"0","to":"0"},{"from":"0","to":"1"},{"from":"1","to":"1"},{"from":"0","to":"0"},{"from":"0","to":"2"},{"from":"2","to":"1"},{"from":"2","to":"1"},{"from":"1","to":"0"},{"from":"0","to":"0"},{"from":"0","to":"1"},{"from":"1","to":"1"},{"from":"1","to":"1"}]
             */

            public int status;
            public List<ValueBeanXX> value;

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public List<ValueBeanXX> getValue() {
                return value;
            }

            public void setValue(List<ValueBeanXX> value) {
                this.value = value;
            }

            public static class ValueBeanXX {
                /**
                 * from : 0
                 * to : 0
                 */

                public String from;
                public String to;

                public String getFrom() {
                    return from;
                }

                public void setFrom(String from) {
                    this.from = from;
                }

                public String getTo() {
                    return to;
                }

                public void setTo(String to) {
                    this.to = to;
                }
            }
        }

        public static class WindBeanX {
            /**
             * direction : {"status":0,"unit":"°","value":[{"from":"45","to":"45"},{"from":"0","to":"0"},{"from":"0","to":"0"},{"from":"0","to":"0"},{"from":"225","to":"225"},{"from":"225","to":"225"},{"from":"0","to":"0"},{"from":"225","to":"225"},{"from":"225","to":"270"},{"from":"270","to":"135"},{"from":"45","to":"0"},{"from":"0","to":"45"},{"from":"45","to":"45"},{"from":"270","to":"225"},{"from":"180","to":"180"}]}
             * speed : {"status":0,"unit":"km/h","value":[{"from":"3.7","to":"3.7"},{"from":"50.0","to":"50.0"},{"from":"50.0","to":"50.0"},{"from":"50.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"20.0","to":"0.0"},{"from":"20.0","to":"20.0"},{"from":"0.0","to":"29.0"},{"from":"20.0","to":"0.0"},{"from":"0.0","to":"0.0"},{"from":"0.0","to":"0.0"}]}
             */

            public DirectionBeanX direction;
            public SpeedBeanX speed;

            public DirectionBeanX getDirection() {
                return direction;
            }

            public void setDirection(DirectionBeanX direction) {
                this.direction = direction;
            }

            public SpeedBeanX getSpeed() {
                return speed;
            }

            public void setSpeed(SpeedBeanX speed) {
                this.speed = speed;
            }

            public static class DirectionBeanX {
                /**
                 * status : 0
                 * unit : °
                 * value : [{"from":"45","to":"45"},{"from":"0","to":"0"},{"from":"0","to":"0"},{"from":"0","to":"0"},{"from":"225","to":"225"},{"from":"225","to":"225"},{"from":"0","to":"0"},{"from":"225","to":"225"},{"from":"225","to":"270"},{"from":"270","to":"135"},{"from":"45","to":"0"},{"from":"0","to":"45"},{"from":"45","to":"45"},{"from":"270","to":"225"},{"from":"180","to":"180"}]
                 */

                public int status;
                public String unit;
                public List<ValueBeanXXX> value;

                public int getStatus() {
                    return status;
                }

                public void setStatus(int status) {
                    this.status = status;
                }

                public String getUnit() {
                    return unit;
                }

                public void setUnit(String unit) {
                    this.unit = unit;
                }

                public List<ValueBeanXXX> getValue() {
                    return value;
                }

                public void setValue(List<ValueBeanXXX> value) {
                    this.value = value;
                }

                public static class ValueBeanXXX {
                    /**
                     * from : 45
                     * to : 45
                     */

                    public String from;
                    public String to;

                    public String getFrom() {
                        return from;
                    }

                    public void setFrom(String from) {
                        this.from = from;
                    }

                    public String getTo() {
                        return to;
                    }

                    public void setTo(String to) {
                        this.to = to;
                    }
                }
            }

            public static class SpeedBeanX {
                /**
                 * status : 0
                 * unit : km/h
                 * value : [{"from":"3.7","to":"3.7"},{"from":"50.0","to":"50.0"},{"from":"50.0","to":"50.0"},{"from":"50.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"29.0","to":"29.0"},{"from":"20.0","to":"0.0"},{"from":"20.0","to":"20.0"},{"from":"0.0","to":"29.0"},{"from":"20.0","to":"0.0"},{"from":"0.0","to":"0.0"},{"from":"0.0","to":"0.0"}]
                 */

                public int status;
                public String unit;
                public List<ValueBeanXXXX> value;

                public int getStatus() {
                    return status;
                }

                public void setStatus(int status) {
                    this.status = status;
                }

                public String getUnit() {
                    return unit;
                }

                public void setUnit(String unit) {
                    this.unit = unit;
                }

                public List<ValueBeanXXXX> getValue() {
                    return value;
                }

                public void setValue(List<ValueBeanXXXX> value) {
                    this.value = value;
                }

                public static class ValueBeanXXXX {
                    /**
                     * from : 3.7
                     * to : 3.7
                     */

                    public String from;
                    public String to;

                    public String getFrom() {
                        return from;
                    }

                    public void setFrom(String from) {
                        this.from = from;
                    }

                    public String getTo() {
                        return to;
                    }

                    public void setTo(String to) {
                        this.to = to;
                    }
                }
            }
        }
    }

    public static class ForecastHourlyBean {
        /**
         * aqi : {"brandInfo":{"brands":[{"brandId":"caiyun","logo":"http://f5.market.mi-img.com/download/MiSafe/07fa34263d698a7a9a8050dde6a7c63f8f243dbf3/a.webp","names":{"zh_TW":"彩雲天氣","en_US":"彩云天气","zh_CN":"彩云天气"},"url":""}]},"pubTime":"2019-10-02T22:00:00+08:00","status":0,"value":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]}
         * desc : 逐小时预报
         * status : 0
         * temperature : {"pubTime":"2019-10-02T22:00:00+08:00","status":0,"unit":"℃","value":[17,17,17,17,18,18,18,18,19,19,20,20,21,23,22,22,21,21,21,20,18,16,16]}
         * weather : {"pubTime":"2019-10-02T22:00:00+08:00","status":0,"value":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]}
         * wind : {"status":0,"value":[{"datetime":"2019-10-02T22:00:00.000+08:00","direction":"327.28","speed":"8.96"},{"datetime":"2019-10-02T23:00:00.000+08:00","direction":"322.45","speed":"12.45"},{"datetime":"2019-10-03T00:00:00.000+08:00","direction":"326.37","speed":"15.21"},{"datetime":"2019-10-03T01:00:00.000+08:00","direction":"332.69","speed":"15.06"},{"datetime":"2019-10-03T02:00:00.000+08:00","direction":"330.49","speed":"15.04"},{"datetime":"2019-10-03T03:00:00.000+08:00","direction":"331.71","speed":"15.76"},{"datetime":"2019-10-03T04:00:00.000+08:00","direction":"326.13","speed":"16.8"},{"datetime":"2019-10-03T05:00:00.000+08:00","direction":"315.11","speed":"20.21"},{"datetime":"2019-10-03T06:00:00.000+08:00","direction":"311.14","speed":"20.39"},{"datetime":"2019-10-03T07:00:00.000+08:00","direction":"313.99","speed":"20.48"},{"datetime":"2019-10-03T08:00:00.000+08:00","direction":"315.05","speed":"20.88"},{"datetime":"2019-10-03T09:00:00.000+08:00","direction":"327.01","speed":"19.58"},{"datetime":"2019-10-03T10:00:00.000+08:00","direction":"351.85","speed":"22.96"},{"datetime":"2019-10-03T11:00:00.000+08:00","direction":"7.12","speed":"26.56"},{"datetime":"2019-10-03T12:00:00.000+08:00","direction":"1.14","speed":"28.93"},{"datetime":"2019-10-03T13:00:00.000+08:00","direction":"356.62","speed":"30.45"},{"datetime":"2019-10-03T14:00:00.000+08:00","direction":"354.95","speed":"34.27"},{"datetime":"2019-10-03T15:00:00.000+08:00","direction":"359.29","speed":"34.82"},{"datetime":"2019-10-03T16:00:00.000+08:00","direction":"358.46","speed":"35.2"},{"datetime":"2019-10-03T17:00:00.000+08:00","direction":"358.83","speed":"37.99"},{"datetime":"2019-10-03T18:00:00.000+08:00","direction":"5.76","speed":"35.79"},{"datetime":"2019-10-03T19:00:00.000+08:00","direction":"12.38","speed":"37.09"},{"datetime":"2019-10-03T20:00:00.000+08:00","direction":"15.31","speed":"38.93"}]}
         */

        public AqiBeanX aqi;
        public String desc;
        public int status;
        public TemperatureBeanXX temperature;
        public WeatherBeanX weather;
        public WindBeanXX wind;

        public AqiBeanX getAqi() {
            return aqi;
        }

        public void setAqi(AqiBeanX aqi) {
            this.aqi = aqi;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public TemperatureBeanXX getTemperature() {
            return temperature;
        }

        public void setTemperature(TemperatureBeanXX temperature) {
            this.temperature = temperature;
        }

        public WeatherBeanX getWeather() {
            return weather;
        }

        public void setWeather(WeatherBeanX weather) {
            this.weather = weather;
        }

        public WindBeanXX getWind() {
            return wind;
        }

        public void setWind(WindBeanXX wind) {
            this.wind = wind;
        }

        public static class AqiBeanX {
            /**
             * brandInfo : {"brands":[{"brandId":"caiyun","logo":"http://f5.market.mi-img.com/download/MiSafe/07fa34263d698a7a9a8050dde6a7c63f8f243dbf3/a.webp","names":{"zh_TW":"彩雲天氣","en_US":"彩云天气","zh_CN":"彩云天气"},"url":""}]}
             * pubTime : 2019-10-02T22:00:00+08:00
             * status : 0
             * value : [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
             */

            public BrandInfoBeanX brandInfo;
            public String pubTime;
            public int status;
            public List<Integer> value;

            public BrandInfoBeanX getBrandInfo() {
                return brandInfo;
            }

            public void setBrandInfo(BrandInfoBeanX brandInfo) {
                this.brandInfo = brandInfo;
            }

            public String getPubTime() {
                return pubTime;
            }

            public void setPubTime(String pubTime) {
                this.pubTime = pubTime;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public List<Integer> getValue() {
                return value;
            }

            public void setValue(List<Integer> value) {
                this.value = value;
            }

            public static class BrandInfoBeanX {
                public List<BrandsBeanX> brands;

                public List<BrandsBeanX> getBrands() {
                    return brands;
                }

                public void setBrands(List<BrandsBeanX> brands) {
                    this.brands = brands;
                }

                public static class BrandsBeanX {
                    /**
                     * brandId : caiyun
                     * logo : http://f5.market.mi-img.com/download/MiSafe/07fa34263d698a7a9a8050dde6a7c63f8f243dbf3/a.webp
                     * names : {"zh_TW":"彩雲天氣","en_US":"彩云天气","zh_CN":"彩云天气"}
                     * url : 
                     */

                    public String brandId;
                    public String logo;
                    public NamesBeanX names;
                    public String url;

                    public String getBrandId() {
                        return brandId;
                    }

                    public void setBrandId(String brandId) {
                        this.brandId = brandId;
                    }

                    public String getLogo() {
                        return logo;
                    }

                    public void setLogo(String logo) {
                        this.logo = logo;
                    }

                    public NamesBeanX getNames() {
                        return names;
                    }

                    public void setNames(NamesBeanX names) {
                        this.names = names;
                    }

                    public String getUrl() {
                        return url;
                    }

                    public void setUrl(String url) {
                        this.url = url;
                    }

                    public static class NamesBeanX {
                        /**
                         * zh_TW : 彩雲天氣
                         * en_US : 彩云天气
                         * zh_CN : 彩云天气
                         */

                        public String zh_TW;
                        public String en_US;
                        public String zh_CN;

                        public String getZh_TW() {
                            return zh_TW;
                        }

                        public void setZh_TW(String zh_TW) {
                            this.zh_TW = zh_TW;
                        }

                        public String getEn_US() {
                            return en_US;
                        }

                        public void setEn_US(String en_US) {
                            this.en_US = en_US;
                        }

                        public String getZh_CN() {
                            return zh_CN;
                        }

                        public void setZh_CN(String zh_CN) {
                            this.zh_CN = zh_CN;
                        }
                    }
                }
            }
        }

        public static class TemperatureBeanXX {
            /**
             * pubTime : 2019-10-02T22:00:00+08:00
             * status : 0
             * unit : ℃
             * value : [17,17,17,17,18,18,18,18,19,19,20,20,21,23,22,22,21,21,21,20,18,16,16]
             */

            public String pubTime;
            public int status;
            public String unit;
            public List<Integer> value;

            public String getPubTime() {
                return pubTime;
            }

            public void setPubTime(String pubTime) {
                this.pubTime = pubTime;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public String getUnit() {
                return unit;
            }

            public void setUnit(String unit) {
                this.unit = unit;
            }

            public List<Integer> getValue() {
                return value;
            }

            public void setValue(List<Integer> value) {
                this.value = value;
            }
        }

        public static class WeatherBeanX {
            /**
             * pubTime : 2019-10-02T22:00:00+08:00
             * status : 0
             * value : [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
             */

            public String pubTime;
            public int status;
            public List<Integer> value;

            public String getPubTime() {
                return pubTime;
            }

            public void setPubTime(String pubTime) {
                this.pubTime = pubTime;
            }

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public List<Integer> getValue() {
                return value;
            }

            public void setValue(List<Integer> value) {
                this.value = value;
            }
        }

        public static class WindBeanXX {
            /**
             * status : 0
             * value : [{"datetime":"2019-10-02T22:00:00.000+08:00","direction":"327.28","speed":"8.96"},{"datetime":"2019-10-02T23:00:00.000+08:00","direction":"322.45","speed":"12.45"},{"datetime":"2019-10-03T00:00:00.000+08:00","direction":"326.37","speed":"15.21"},{"datetime":"2019-10-03T01:00:00.000+08:00","direction":"332.69","speed":"15.06"},{"datetime":"2019-10-03T02:00:00.000+08:00","direction":"330.49","speed":"15.04"},{"datetime":"2019-10-03T03:00:00.000+08:00","direction":"331.71","speed":"15.76"},{"datetime":"2019-10-03T04:00:00.000+08:00","direction":"326.13","speed":"16.8"},{"datetime":"2019-10-03T05:00:00.000+08:00","direction":"315.11","speed":"20.21"},{"datetime":"2019-10-03T06:00:00.000+08:00","direction":"311.14","speed":"20.39"},{"datetime":"2019-10-03T07:00:00.000+08:00","direction":"313.99","speed":"20.48"},{"datetime":"2019-10-03T08:00:00.000+08:00","direction":"315.05","speed":"20.88"},{"datetime":"2019-10-03T09:00:00.000+08:00","direction":"327.01","speed":"19.58"},{"datetime":"2019-10-03T10:00:00.000+08:00","direction":"351.85","speed":"22.96"},{"datetime":"2019-10-03T11:00:00.000+08:00","direction":"7.12","speed":"26.56"},{"datetime":"2019-10-03T12:00:00.000+08:00","direction":"1.14","speed":"28.93"},{"datetime":"2019-10-03T13:00:00.000+08:00","direction":"356.62","speed":"30.45"},{"datetime":"2019-10-03T14:00:00.000+08:00","direction":"354.95","speed":"34.27"},{"datetime":"2019-10-03T15:00:00.000+08:00","direction":"359.29","speed":"34.82"},{"datetime":"2019-10-03T16:00:00.000+08:00","direction":"358.46","speed":"35.2"},{"datetime":"2019-10-03T17:00:00.000+08:00","direction":"358.83","speed":"37.99"},{"datetime":"2019-10-03T18:00:00.000+08:00","direction":"5.76","speed":"35.79"},{"datetime":"2019-10-03T19:00:00.000+08:00","direction":"12.38","speed":"37.09"},{"datetime":"2019-10-03T20:00:00.000+08:00","direction":"15.31","speed":"38.93"}]
             */

            public int status;
            public List<ValueBeanXXXXX> value;

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }

            public List<ValueBeanXXXXX> getValue() {
                return value;
            }

            public void setValue(List<ValueBeanXXXXX> value) {
                this.value = value;
            }

            public static class ValueBeanXXXXX {
                /**
                 * datetime : 2019-10-02T22:00:00.000+08:00
                 * direction : 327.28
                 * speed : 8.96
                 */

                public String datetime;
                public String direction;
                public String speed;

                public String getDatetime() {
                    return datetime;
                }

                public void setDatetime(String datetime) {
                    this.datetime = datetime;
                }

                public String getDirection() {
                    return direction;
                }

                public void setDirection(String direction) {
                    this.direction = direction;
                }

                public String getSpeed() {
                    return speed;
                }

                public void setSpeed(String speed) {
                    this.speed = speed;
                }
            }
        }
    }

    public static class IndicesBeanX {
        /**
         * indices : [{"type":"uvIndex","value":"6"},{"type":"humidity","value":"94"},{"type":"feelsLike","value":"17"},{"type":"pressure","value":"1012.0"},{"type":"carWash","value":"1"},{"type":"sports","value":"1"}]
         * pubTime : 
         * status : 0
         */

        public String pubTime;
        public int status;
        public List<IndicesBean> indices;

        public String getPubTime() {
            return pubTime;
        }

        public void setPubTime(String pubTime) {
            this.pubTime = pubTime;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public List<IndicesBean> getIndices() {
            return indices;
        }

        public void setIndices(List<IndicesBean> indices) {
            this.indices = indices;
        }

        public static class IndicesBean {
            /**
             * type : uvIndex
             * value : 6
             */

            public String type;
            public String value;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }
    }

    public static class YesterdayBean {
        /**
         * aqi : 
         * date : 2019-10-01T12:00:00+08:00
         * status : 0
         * sunRise : 2019-10-01T05:49:00+08:00
         * sunSet : 2019-10-01T17:40:00+08:00
         * tempMax : 25
         * tempMin : 19
         * weatherEnd : 0
         * weatherStart : 0
         * windDircEnd : 135
         * windDircStart : 135
         * windSpeedEnd : 29.0
         * windSpeedStart : 29.0
         */

        public String aqi;
        public String date;
        public int status;
        public String sunRise;
        public String sunSet;
        public String tempMax;
        public String tempMin;
        public String weatherEnd;
        public String weatherStart;
        public String windDircEnd;
        public String windDircStart;
        public String windSpeedEnd;
        public String windSpeedStart;

        public String getAqi() {
            return aqi;
        }

        public void setAqi(String aqi) {
            this.aqi = aqi;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public String getSunRise() {
            return sunRise;
        }

        public void setSunRise(String sunRise) {
            this.sunRise = sunRise;
        }

        public String getSunSet() {
            return sunSet;
        }

        public void setSunSet(String sunSet) {
            this.sunSet = sunSet;
        }

        public String getTempMax() {
            return tempMax;
        }

        public void setTempMax(String tempMax) {
            this.tempMax = tempMax;
        }

        public String getTempMin() {
            return tempMin;
        }

        public void setTempMin(String tempMin) {
            this.tempMin = tempMin;
        }

        public String getWeatherEnd() {
            return weatherEnd;
        }

        public void setWeatherEnd(String weatherEnd) {
            this.weatherEnd = weatherEnd;
        }

        public String getWeatherStart() {
            return weatherStart;
        }

        public void setWeatherStart(String weatherStart) {
            this.weatherStart = weatherStart;
        }

        public String getWindDircEnd() {
            return windDircEnd;
        }

        public void setWindDircEnd(String windDircEnd) {
            this.windDircEnd = windDircEnd;
        }

        public String getWindDircStart() {
            return windDircStart;
        }

        public void setWindDircStart(String windDircStart) {
            this.windDircStart = windDircStart;
        }

        public String getWindSpeedEnd() {
            return windSpeedEnd;
        }

        public void setWindSpeedEnd(String windSpeedEnd) {
            this.windSpeedEnd = windSpeedEnd;
        }

        public String getWindSpeedStart() {
            return windSpeedStart;
        }

        public void setWindSpeedStart(String windSpeedStart) {
            this.windSpeedStart = windSpeedStart;
        }
    }

    public static class UrlBean {
        /**
         * weathercn : 
         * caiyun : 
         */

        public String weathercn;
        public String caiyun;

        public String getWeathercn() {
            return weathercn;
        }

        public void setWeathercn(String weathercn) {
            this.weathercn = weathercn;
        }

        public String getCaiyun() {
            return caiyun;
        }

        public void setCaiyun(String caiyun) {
            this.caiyun = caiyun;
        }
    }

    public static class BrandInfoBeanXX {
        public List<BrandsBeanXX> brands;

        public List<BrandsBeanXX> getBrands() {
            return brands;
        }

        public void setBrands(List<BrandsBeanXX> brands) {
            this.brands = brands;
        }

        public static class BrandsBeanXX {
            /**
             * brandId : caiyun
             * logo : http://t5.market.xiaomi.com/download/MiSafe/0d74f4cf0f60ede546bc7549b9c35bc3a9e4149b3/a.webp
             * names : {"zh_TW":"彩雲天氣","en_US":"彩云天气","zh_CN":"彩云天气"}
             * url : 
             */

            public String brandId;
            public String logo;
            public NamesBeanXX names;
            public String url;

            public String getBrandId() {
                return brandId;
            }

            public void setBrandId(String brandId) {
                this.brandId = brandId;
            }

            public String getLogo() {
                return logo;
            }

            public void setLogo(String logo) {
                this.logo = logo;
            }

            public NamesBeanXX getNames() {
                return names;
            }

            public void setNames(NamesBeanXX names) {
                this.names = names;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public static class NamesBeanXX {
                /**
                 * zh_TW : 彩雲天氣
                 * en_US : 彩云天气
                 * zh_CN : 彩云天气
                 */

                public String zh_TW;
                public String en_US;
                public String zh_CN;

                public String getZh_TW() {
                    return zh_TW;
                }

                public void setZh_TW(String zh_TW) {
                    this.zh_TW = zh_TW;
                }

                public String getEn_US() {
                    return en_US;
                }

                public void setEn_US(String en_US) {
                    this.en_US = en_US;
                }

                public String getZh_CN() {
                    return zh_CN;
                }

                public void setZh_CN(String zh_CN) {
                    this.zh_CN = zh_CN;
                }
            }
        }
    }

    public static class AqiBeanXX {
        /**
         * pm10Desc : PM10对人的影响要大于其他任何污染物，长期暴露于污染环境可能导致罹患心血管和呼吸道疾病甚至肺癌
         * o3 : 90
         * src : 中国环境监测总站
         * pubTime : 2019-10-02T20:00:00+08:00
         * pm10 : 72
         * suggest : 空气质量可以接受，可能对少数异常敏感的人群健康有较弱影响
         * co : 0.84
         * o3Desc : 空气中过多臭氧可能导致呼吸问题，引发哮喘，降低肺功能并引起肺部疾病，对人类健康影响较大
         * no2 : 29
         * so2Desc : 二氧化硫是一种无色气体，当空气中SO2达到一定浓度时，空气中会有刺鼻的气味
         * coDesc : 一氧化碳八成来自汽车尾气，交通高峰期时，公路沿线产生的CO浓度会高于平常
         * pm25 : 54
         * so2 : 6
         * aqi : 75
         * pm25Desc : PM2.5指的是直径小于或等于2.5微米的颗粒物，又称为细颗粒物
         * no2Desc : 二氧化氮是硝酸盐气溶胶的主要来源，是构成PM2.5和紫外线作用下产生臭氧的主要成分
         * brandInfo : {"brands":[{"names":{"zh_TW":"中國環境監測總站","en_US":"CNEMC","zh_CN":"中国环境监测总站"},"brandId":"CNEMC","logo":"","url":""}]}
         * primary : 
         * status : 0
         */

        public String pm10Desc;
        public String o3;
        public String src;
        public String pubTime;
        public String pm10;
        public String suggest;
        public String co;
        public String o3Desc;
        public String no2;
        public String so2Desc;
        public String coDesc;
        public String pm25;
        public String so2;
        public String aqi;
        public String pm25Desc;
        public String no2Desc;
        public BrandInfoBeanXXX brandInfo;
        public String primary;
        public int status;

        public String getPm10Desc() {
            return pm10Desc;
        }

        public void setPm10Desc(String pm10Desc) {
            this.pm10Desc = pm10Desc;
        }

        public String getO3() {
            return o3;
        }

        public void setO3(String o3) {
            this.o3 = o3;
        }

        public String getSrc() {
            return src;
        }

        public void setSrc(String src) {
            this.src = src;
        }

        public String getPubTime() {
            return pubTime;
        }

        public void setPubTime(String pubTime) {
            this.pubTime = pubTime;
        }

        public String getPm10() {
            return pm10;
        }

        public void setPm10(String pm10) {
            this.pm10 = pm10;
        }

        public String getSuggest() {
            return suggest;
        }

        public void setSuggest(String suggest) {
            this.suggest = suggest;
        }

        public String getCo() {
            return co;
        }

        public void setCo(String co) {
            this.co = co;
        }

        public String getO3Desc() {
            return o3Desc;
        }

        public void setO3Desc(String o3Desc) {
            this.o3Desc = o3Desc;
        }

        public String getNo2() {
            return no2;
        }

        public void setNo2(String no2) {
            this.no2 = no2;
        }

        public String getSo2Desc() {
            return so2Desc;
        }

        public void setSo2Desc(String so2Desc) {
            this.so2Desc = so2Desc;
        }

        public String getCoDesc() {
            return coDesc;
        }

        public void setCoDesc(String coDesc) {
            this.coDesc = coDesc;
        }

        public String getPm25() {
            return pm25;
        }

        public void setPm25(String pm25) {
            this.pm25 = pm25;
        }

        public String getSo2() {
            return so2;
        }

        public void setSo2(String so2) {
            this.so2 = so2;
        }

        public String getAqi() {
            return aqi;
        }

        public void setAqi(String aqi) {
            this.aqi = aqi;
        }

        public String getPm25Desc() {
            return pm25Desc;
        }

        public void setPm25Desc(String pm25Desc) {
            this.pm25Desc = pm25Desc;
        }

        public String getNo2Desc() {
            return no2Desc;
        }

        public void setNo2Desc(String no2Desc) {
            this.no2Desc = no2Desc;
        }

        public BrandInfoBeanXXX getBrandInfo() {
            return brandInfo;
        }

        public void setBrandInfo(BrandInfoBeanXXX brandInfo) {
            this.brandInfo = brandInfo;
        }

        public String getPrimary() {
            return primary;
        }

        public void setPrimary(String primary) {
            this.primary = primary;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public static class BrandInfoBeanXXX {
            public List<BrandsBeanXXX> brands;

            public List<BrandsBeanXXX> getBrands() {
                return brands;
            }

            public void setBrands(List<BrandsBeanXXX> brands) {
                this.brands = brands;
            }

            public static class BrandsBeanXXX {
                /**
                 * names : {"zh_TW":"中國環境監測總站","en_US":"CNEMC","zh_CN":"中国环境监测总站"}
                 * brandId : CNEMC
                 * logo : 
                 * url : 
                 */

                public NamesBeanXXX names;
                public String brandId;
                public String logo;
                public String url;

                public NamesBeanXXX getNames() {
                    return names;
                }

                public void setNames(NamesBeanXXX names) {
                    this.names = names;
                }

                public String getBrandId() {
                    return brandId;
                }

                public void setBrandId(String brandId) {
                    this.brandId = brandId;
                }

                public String getLogo() {
                    return logo;
                }

                public void setLogo(String logo) {
                    this.logo = logo;
                }

                public String getUrl() {
                    return url;
                }

                public void setUrl(String url) {
                    this.url = url;
                }

                public static class NamesBeanXXX {
                    /**
                     * zh_TW : 中國環境監測總站
                     * en_US : CNEMC
                     * zh_CN : 中国环境监测总站
                     */

                    public String zh_TW;
                    public String en_US;
                    public String zh_CN;

                    public String getZh_TW() {
                        return zh_TW;
                    }

                    public void setZh_TW(String zh_TW) {
                        this.zh_TW = zh_TW;
                    }

                    public String getEn_US() {
                        return en_US;
                    }

                    public void setEn_US(String en_US) {
                        this.en_US = en_US;
                    }

                    public String getZh_CN() {
                        return zh_CN;
                    }

                    public void setZh_CN(String zh_CN) {
                        this.zh_CN = zh_CN;
                    }
                }
            }
        }
    }

    public static class AlertsBean {
        /**
         * locationKey : weathercn:101070205
         * images : {"icon":"http://f4.market.xiaomi.com/download/MiSafe/02efa4e1664fb1decb148d4a1fd0c36f1b840a224/a.webp","notice":"http://f3.market.xiaomi.com/download/MiSafe/0034524cf8c2543333dc2ab7fef54c2d3ca6fa0a0/a.webp"}
         * level : 橙色
         * defense : [{"defenseText":"注意防护，佩戴防护口罩","defenseIcon":"http://f3.market.mi-img.com/download/Weather/004784c916d30b58ea08abe5a1381055540424fd1/20190220170058_681.png"},{"defenseText":"小心驾驶，减速慢行","defenseIcon":"http://f3.market.mi-img.com/download/Weather/00154470362bba8e0bd999f2a36a148465341d31a/20190220170150_60.png"}]
         * pubTime : 2019-10-02T19:56:10+08:00
         * alertId : weathercn:101070205-1570017370000-大雾橙色
         * detail : 大连市气象台2019年10月02日19时56分发布大雾橙色预警信号：预计2日20时到3日8时，大连地区及沿岸海域将出现能见度小于200米的浓雾天气，请注意防范。
         * title : 旅顺发布大雾橙色预警
         * type : 大雾
         */

        public String locationKey;
        public ImagesBean images;
        public String level;
        public Date pubTime;
        public String alertId;
        public String detail;
        public String title;
        public String type;
        public List<DefenseBean> defense;

        public String getLocationKey() {
            return locationKey;
        }

        public void setLocationKey(String locationKey) {
            this.locationKey = locationKey;
        }

        public ImagesBean getImages() {
            return images;
        }

        public void setImages(ImagesBean images) {
            this.images = images;
        }

        public String getLevel() {
            return level;
        }

        public void setLevel(String level) {
            this.level = level;
        }

        public Date getPubTime() {
            return pubTime;
        }

        public void setPubTime(Date pubTime) {
            this.pubTime = pubTime;
        }

        public String getAlertId() {
            return alertId;
        }

        public void setAlertId(String alertId) {
            this.alertId = alertId;
        }

        public String getDetail() {
            return detail;
        }

        public void setDetail(String detail) {
            this.detail = detail;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public List<DefenseBean> getDefense() {
            return defense;
        }

        public void setDefense(List<DefenseBean> defense) {
            this.defense = defense;
        }

        public static class ImagesBean {
            /**
             * icon : http://f4.market.xiaomi.com/download/MiSafe/02efa4e1664fb1decb148d4a1fd0c36f1b840a224/a.webp
             * notice : http://f3.market.xiaomi.com/download/MiSafe/0034524cf8c2543333dc2ab7fef54c2d3ca6fa0a0/a.webp
             */

            public String icon;
            public String notice;

            public String getIcon() {
                return icon;
            }

            public void setIcon(String icon) {
                this.icon = icon;
            }

            public String getNotice() {
                return notice;
            }

            public void setNotice(String notice) {
                this.notice = notice;
            }
        }

        public static class DefenseBean {
            /**
             * defenseText : 注意防护，佩戴防护口罩
             * defenseIcon : http://f3.market.mi-img.com/download/Weather/004784c916d30b58ea08abe5a1381055540424fd1/20190220170058_681.png
             */

            public String defenseText;
            public String defenseIcon;

            public String getDefenseText() {
                return defenseText;
            }

            public void setDefenseText(String defenseText) {
                this.defenseText = defenseText;
            }

            public String getDefenseIcon() {
                return defenseIcon;
            }

            public void setDefenseIcon(String defenseIcon) {
                this.defenseIcon = defenseIcon;
            }
        }
    }
}
