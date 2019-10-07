package wangdaye.com.geometricweather.weather.json.caiyun;

import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.List;

public class CaiYunForecastResult {

    /**
     * precipitation : {"headDescription":"未来2小时无雨","headIconType":"rain_0","isRainOrSnow":2,"pubTime":"2019-10-02T21:21:55+08:00","weather":"0","description":"未来两小时不会下雨，放心出门吧","modifyInHour":true,"shortDescription":"","isModify":false,"value":[0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0],"isShow":false,"status":0}
     * new : new
     * status : 0
     */

    public PrecipitationBean precipitation;
    @SerializedName("new") public String newX;
    public int status;

    public PrecipitationBean getPrecipitation() {
        return precipitation;
    }

    public void setPrecipitation(PrecipitationBean precipitation) {
        this.precipitation = precipitation;
    }

    public String getNewX() {
        return newX;
    }

    public void setNewX(String newX) {
        this.newX = newX;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public static class PrecipitationBean {
        /**
         * headDescription : 未来2小时无雨
         * headIconType : rain_0
         * isRainOrSnow : 2
         * pubTime : 2019-10-02T21:21:55+08:00
         * weather : 0
         * description : 未来两小时不会下雨，放心出门吧
         * modifyInHour : true
         * shortDescription : 
         * isModify : false
         * value : [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]
         * isShow : false
         * status : 0
         */

        public String headDescription;
        public String headIconType;
        public int isRainOrSnow;
        public Date pubTime;
        public String weather;
        public String description;
        public boolean modifyInHour;
        public String shortDescription;
        public boolean isModify;
        public boolean isShow;
        public int status;
        public List<Double> value;

        public String getHeadDescription() {
            return headDescription;
        }

        public void setHeadDescription(String headDescription) {
            this.headDescription = headDescription;
        }

        public String getHeadIconType() {
            return headIconType;
        }

        public void setHeadIconType(String headIconType) {
            this.headIconType = headIconType;
        }

        public int getIsRainOrSnow() {
            return isRainOrSnow;
        }

        public void setIsRainOrSnow(int isRainOrSnow) {
            this.isRainOrSnow = isRainOrSnow;
        }

        public Date getPubTime() {
            return pubTime;
        }

        public void setPubTime(Date pubTime) {
            this.pubTime = pubTime;
        }

        public String getWeather() {
            return weather;
        }

        public void setWeather(String weather) {
            this.weather = weather;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public boolean isModifyInHour() {
            return modifyInHour;
        }

        public void setModifyInHour(boolean modifyInHour) {
            this.modifyInHour = modifyInHour;
        }

        public String getShortDescription() {
            return shortDescription;
        }

        public void setShortDescription(String shortDescription) {
            this.shortDescription = shortDescription;
        }

        public boolean isIsModify() {
            return isModify;
        }

        public void setIsModify(boolean isModify) {
            this.isModify = isModify;
        }

        public boolean isIsShow() {
            return isShow;
        }

        public void setIsShow(boolean isShow) {
            this.isShow = isShow;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }

        public List<Double> getValue() {
            return value;
        }

        public void setValue(List<Double> value) {
            this.value = value;
        }
    }
}
