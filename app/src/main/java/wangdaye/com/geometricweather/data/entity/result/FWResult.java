package wangdaye.com.geometricweather.data.entity.result;

import java.util.List;

/**
 * Flyme result.
 * */

public class FWResult {

    /**
     * alarms : [{"alarmContent":"青岛市气象台2016年12月14日14时30分继续发布寒潮蓝色预警信号：受北方冷空气影响，预计今天下午到明天夜间，我市北风陆地4到5级阵风8级，沿海地区5到6级阵风9级，海上7到8级阵风10级。14日和15日夜间气温较低，最低气温市区及沿海地区-3℃左右，内陆地区-7到-5℃，请注意防范。","alarmDesc":"48小时内最低气温下降8℃以上，最低气温≤4℃，并可能持续。","alarmId":"201612141432548570寒潮蓝色","alarmLevelNo":"01","alarmLevelNoDesc":"蓝色","alarmType":"04","alarmTypeDesc":"寒潮预警","precaution":"1.准备手套、帽子、围巾、口罩等御寒衣物；\n\n2.检查取暖设备，确保正常使用。","publishTime":"12月14日 14:30 发布"}]
     * city : 青岛
     * cityid : 101120201
     * indexes : [{"abbreviation":"zs","alias":"","content":"温度不高，其他各项气象条件适宜，中暑机率极低。","level":"无","name":"中暑指数"},{"abbreviation":"ys","alias":"","content":"天气较好，不会降水，因此您可放心出门，无须带雨伞。","level":"不带伞","name":"雨伞指数"},{"abbreviation":"yh","alias":"","content":"天气较冷，且室外有风，外出约会可能会让恋人受些苦，最好在温暖的室内促膝谈心。","level":"较不适宜","name":"约会指数"},{"abbreviation":"yd","alias":"","content":"天气较好，但考虑风力较大，天气寒冷，推荐您进行室内运动，若在户外运动须注意保暖。","level":"较不宜","name":"运动指数"},{"abbreviation":"xq","alias":"","content":"天气较好，气温较低，会让人觉得有些压抑，不妨与朋友同事沟通交流下，舒缓下心情。","level":"较差","name":"心情指数"},{"abbreviation":"xc","alias":"","content":"较不宜洗车，未来一天无雨，风力较大，如果执意擦洗汽车，要做好蒙上污垢的心理准备。","level":"较不宜","name":"洗车指数"},{"abbreviation":"wc","alias":"","content":"感觉有点冷，室外活动要穿厚实一点，年老体弱者要适当注意保暖。","level":"冷","name":"风寒指数"},{"abbreviation":"uv","alias":"","content":"属弱紫外线辐射天气，无需特别防护。若长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。","level":"最弱","name":"紫外线强度指数"},{"abbreviation":"tr","alias":"","content":"天空状况还是比较好的，但温度稍微有点低，且风稍大，会让您感觉些许凉意。外出请注意防风。","level":"一般","name":"旅游指数"},{"abbreviation":"pp","alias":"","content":"天气寒冷，请用滋润保湿型化妆品，少扑粉，使用润唇膏后再抹口红。","level":"保湿","name":"化妆指数"},{"abbreviation":"pl","alias":"","content":"气象条件有利于空气污染物稀释、扩散和清除，可在室外正常活动。","level":"良","name":"空气污染扩散条件指数"},{"abbreviation":"pk","alias":"","content":"天气寒冷，不适宜放风筝。","level":"不宜","name":"放风筝指数"},{"abbreviation":"pj","alias":"","content":"寒冷的天气可能会减弱啤酒对您的诱惑，可少量饮用常温啤酒。","level":"不适宜","name":"啤酒指数"},{"abbreviation":"nl","alias":"","content":"天气较好，但是风力很大，会影响到您的出行，建议夜生活最好在室内进行。","level":"较不适宜","name":"夜生活指数"},{"abbreviation":"mf","alias":"","content":"天气较冷，风较大，注意防晒，还要头发保持清洁，建议选用保湿防晒型洗发护发品。出门请戴上遮阳帽或打遮阳伞。","level":"一般","name":"美发指数"},{"abbreviation":"ls","alias":"","content":"天气不错，午后温暖的阳光仍能满足你驱潮消霉杀菌的晾晒需求。","level":"基本适宜","name":"晾晒指数"},{"abbreviation":"lk","alias":"","content":"天气较好，路面比较干燥，路况较好。","level":"干燥","name":"路况指数"},{"abbreviation":"jt","alias":"","content":"天气较好，路面干燥，交通气象条件良好，车辆可以正常行驶。","level":"良好","name":"交通指数"},{"abbreviation":"hc","alias":"","content":"白天天气寒冷，不适宜划船。","level":"不适宜","name":"划船指数"},{"abbreviation":"gm","alias":"","content":"天冷风大且昼夜温差也很大，易发生感冒，请注意适当增减衣服。","level":"易发","name":"感冒指数"},{"abbreviation":"gj","alias":"","content":"天气较好，虽然风力较大，还是较适宜逛街的，不过出门前要穿暖和一点，千万别着凉了。","level":"较适宜","name":"逛街指数"},{"abbreviation":"fs","alias":"","content":"属弱紫外辐射天气，长期在户外，建议涂擦SPF在8-12之间的防晒护肤品。","level":"弱","name":"防晒指数"},{"abbreviation":"dy","alias":"","content":"天气冷，不适合垂钓。","level":"不宜","name":"钓鱼指数"},{"abbreviation":"ct","alias":"","content":"天气冷，建议着棉服、羽绒服、皮夹克加羊毛衫等冬季服装。年老体弱者宜着厚棉衣、冬大衣或厚羽绒服。","level":"冷","name":"穿衣指数"},{"abbreviation":"co","alias":"","content":"白天天气较凉，且风力较强，您会感觉偏冷，不很舒适，请注意添加衣物，以防感冒。","level":"较不舒适","name":"舒适度指数"},{"abbreviation":"cl","alias":"","content":"风力稍大，较不宜晨练，室外锻炼请注意选择避风的地点，避免迎风锻炼。","level":"较不宜","name":"晨练指数"},{"abbreviation":"ag","alias":"","content":"天气条件极不易诱发过敏，可放心外出，享受生活。","level":"极不易发","name":"过敏指数"},{"abbreviation":"ac","alias":"","content":"您将感到有些冷，可以适当开启制暖空调调节室内温度，以免着凉感冒。","level":"开启制暖空调","name":"空调开启指数"}]
     * pm25 : {"advice":"","aqi":"49","citycount":1767,"cityrank":73,"co":"0.0","color":"","level":"0","no2":"0","o3":"0","pm10":"32","pm25":"24","quality":"优","so2":"0","timestamp":"0","upDateTime":"2016-12-14 20:00:00"}
     * provinceName : 山东省
     * realtime : {"img":"1","sD":"59","sendibleTemp":"-1","temp":"-1","time":"2016-12-14 20:00:00","wD":"北风","wS":"4级","weather":"多云","ziwaixian":"N/A"}
     * weatherDetailsInfo : {"publishTime":"2016-12-14 21:00:00","weather24HoursDetailsInfos":[{"endTime":"2016-12-14 23:00:00","highestTemperature":"-4","img":"1","isRainFall":"无降水","lowerestTemperature":"-4","precipitation":"0","startTime":"2016-12-14 22:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 00:00:00","highestTemperature":"-3","img":"2","isRainFall":"无降水","lowerestTemperature":"-3","precipitation":"0","startTime":"2016-12-14 23:00:00","wd":"","weather":"阴","ws":""},{"endTime":"2016-12-15 01:00:00","highestTemperature":"-3","img":"1","isRainFall":"无降水","lowerestTemperature":"-3","precipitation":"0","startTime":"2016-12-15 00:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 02:00:00","highestTemperature":"-3","img":"1","isRainFall":"无降水","lowerestTemperature":"-3","precipitation":"0","startTime":"2016-12-15 01:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 03:00:00","highestTemperature":"-4","img":"1","isRainFall":"无降水","lowerestTemperature":"-4","precipitation":"0","startTime":"2016-12-15 02:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 04:00:00","highestTemperature":"-4","img":"1","isRainFall":"无降水","lowerestTemperature":"-4","precipitation":"0","startTime":"2016-12-15 03:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 05:00:00","highestTemperature":"-5","img":"1","isRainFall":"无降水","lowerestTemperature":"-5","precipitation":"0","startTime":"2016-12-15 04:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 06:00:00","highestTemperature":"-5","img":"1","isRainFall":"无降水","lowerestTemperature":"-5","precipitation":"0","startTime":"2016-12-15 05:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 07:00:00","highestTemperature":"-5","img":"0","isRainFall":"无降水","lowerestTemperature":"-5","precipitation":"0","startTime":"2016-12-15 06:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 08:00:00","highestTemperature":"-5","img":"0","isRainFall":"无降水","lowerestTemperature":"-5","precipitation":"0","startTime":"2016-12-15 07:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 09:00:00","highestTemperature":"-3","img":"0","isRainFall":"无降水","lowerestTemperature":"-3","precipitation":"0","startTime":"2016-12-15 08:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 10:00:00","highestTemperature":"0","img":"0","isRainFall":"无降水","lowerestTemperature":"0","precipitation":"0","startTime":"2016-12-15 09:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 11:00:00","highestTemperature":"1","img":"0","isRainFall":"无降水","lowerestTemperature":"1","precipitation":"0","startTime":"2016-12-15 10:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 12:00:00","highestTemperature":"2","img":"0","isRainFall":"无降水","lowerestTemperature":"2","precipitation":"0","startTime":"2016-12-15 11:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 13:00:00","highestTemperature":"3","img":"0","isRainFall":"无降水","lowerestTemperature":"3","precipitation":"0","startTime":"2016-12-15 12:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 14:00:00","highestTemperature":"4","img":"0","isRainFall":"无降水","lowerestTemperature":"4","precipitation":"0","startTime":"2016-12-15 13:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 15:00:00","highestTemperature":"4","img":"1","isRainFall":"无降水","lowerestTemperature":"4","precipitation":"0","startTime":"2016-12-15 14:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 16:00:00","highestTemperature":"4","img":"2","isRainFall":"无降水","lowerestTemperature":"4","precipitation":"0","startTime":"2016-12-15 15:00:00","wd":"","weather":"阴","ws":""},{"endTime":"2016-12-15 17:00:00","highestTemperature":"3","img":"1","isRainFall":"无降水","lowerestTemperature":"3","precipitation":"0","startTime":"2016-12-15 16:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 18:00:00","highestTemperature":"1","img":"1","isRainFall":"无降水","lowerestTemperature":"1","precipitation":"0","startTime":"2016-12-15 17:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 19:00:00","highestTemperature":"1","img":"1","isRainFall":"无降水","lowerestTemperature":"1","precipitation":"0","startTime":"2016-12-15 18:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 20:00:00","highestTemperature":"0","img":"1","isRainFall":"无降水","lowerestTemperature":"0","precipitation":"0","startTime":"2016-12-15 19:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 21:00:00","highestTemperature":"-1","img":"1","isRainFall":"无降水","lowerestTemperature":"-1","precipitation":"0","startTime":"2016-12-15 20:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 22:00:00","highestTemperature":"-1","img":"2","isRainFall":"无降水","lowerestTemperature":"-1","precipitation":"0","startTime":"2016-12-15 21:00:00","wd":"","weather":"阴","ws":""},{"endTime":"2016-12-15 23:00:00","highestTemperature":"-2","img":"2","isRainFall":"无降水","lowerestTemperature":"-2","precipitation":"0","startTime":"2016-12-15 22:00:00","wd":"","weather":"阴","ws":""}]}
     * weathers : [{"date":"2016-12-14","img":"1","sun_down_time":"16:45","sun_rise_time":"07:01","temp_day_c":"3","temp_day_f":"37.4","temp_night_c":"-3","temp_night_f":"26.6","wd":"无持续风向","weather":"多云","week":"星期三","ws":"3级"},{"date":"2016-12-15","img":"1","sun_down_time":"16:45","sun_rise_time":"07:02","temp_day_c":"4","temp_day_f":"39.2","temp_night_c":"-3","temp_night_f":"26.6","wd":"无持续风向","weather":"多云","week":"星期四","ws":"2级"},{"date":"2016-12-16","img":"0","sun_down_time":"16:45","sun_rise_time":"07:02","temp_day_c":"6","temp_day_f":"42.8","temp_night_c":"1","temp_night_f":"33.8","wd":"南风","weather":"晴","week":"星期五","ws":"1级"},{"date":"2016-12-17","img":"1","sun_down_time":"16:46","sun_rise_time":"07:03","temp_day_c":"8","temp_day_f":"46.4","temp_night_c":"5","temp_night_f":"41.0","wd":"南风","weather":"多云","week":"星期六","ws":"2级"},{"date":"2016-12-18","img":"1","sun_down_time":"16:46","sun_rise_time":"07:04","temp_day_c":"11","temp_day_f":"51.8","temp_night_c":"5","temp_night_f":"41.0","wd":"南风","weather":"多云","week":"星期日","ws":"1级"},{"date":"2016-12-19","img":"0","sun_down_time":"16:47","sun_rise_time":"07:04","temp_day_c":"11","temp_day_f":"51.8","temp_night_c":"4","temp_night_f":"39.2","wd":"北风","weather":"晴","week":"星期一","ws":"1级"},{"date":"2016-12-13","img":"2","sun_down_time":"16:45","sun_rise_time":"07:00","temp_day_c":"3","temp_day_f":"37.4","temp_night_c":"-2","temp_night_f":"28.4","wd":"无持续风向","weather":"阴","week":"星期二","ws":"2级"}]
     */

    public String city;
    public int cityid;
    /**
     * advice :
     * aqi : 49
     * citycount : 1767
     * cityrank : 73
     * co : 0.0
     * color :
     * level : 0
     * no2 : 0
     * o3 : 0
     * pm10 : 32
     * pm25 : 24
     * quality : 优
     * so2 : 0
     * timestamp : 0
     * upDateTime : 2016-12-14 20:00:00
     */

    public Pm25 pm25;
    public String provinceName;
    /**
     * img : 1
     * sD : 59
     * sendibleTemp : -1
     * temp : -1
     * time : 2016-12-14 20:00:00
     * wD : 北风
     * wS : 4级
     * weather : 多云
     * ziwaixian : N/A
     */

    public Realtime realtime;
    /**
     * publishTime : 2016-12-14 21:00:00
     * weather24HoursDetailsInfos : [{"endTime":"2016-12-14 23:00:00","highestTemperature":"-4","img":"1","isRainFall":"无降水","lowerestTemperature":"-4","precipitation":"0","startTime":"2016-12-14 22:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 00:00:00","highestTemperature":"-3","img":"2","isRainFall":"无降水","lowerestTemperature":"-3","precipitation":"0","startTime":"2016-12-14 23:00:00","wd":"","weather":"阴","ws":""},{"endTime":"2016-12-15 01:00:00","highestTemperature":"-3","img":"1","isRainFall":"无降水","lowerestTemperature":"-3","precipitation":"0","startTime":"2016-12-15 00:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 02:00:00","highestTemperature":"-3","img":"1","isRainFall":"无降水","lowerestTemperature":"-3","precipitation":"0","startTime":"2016-12-15 01:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 03:00:00","highestTemperature":"-4","img":"1","isRainFall":"无降水","lowerestTemperature":"-4","precipitation":"0","startTime":"2016-12-15 02:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 04:00:00","highestTemperature":"-4","img":"1","isRainFall":"无降水","lowerestTemperature":"-4","precipitation":"0","startTime":"2016-12-15 03:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 05:00:00","highestTemperature":"-5","img":"1","isRainFall":"无降水","lowerestTemperature":"-5","precipitation":"0","startTime":"2016-12-15 04:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 06:00:00","highestTemperature":"-5","img":"1","isRainFall":"无降水","lowerestTemperature":"-5","precipitation":"0","startTime":"2016-12-15 05:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 07:00:00","highestTemperature":"-5","img":"0","isRainFall":"无降水","lowerestTemperature":"-5","precipitation":"0","startTime":"2016-12-15 06:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 08:00:00","highestTemperature":"-5","img":"0","isRainFall":"无降水","lowerestTemperature":"-5","precipitation":"0","startTime":"2016-12-15 07:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 09:00:00","highestTemperature":"-3","img":"0","isRainFall":"无降水","lowerestTemperature":"-3","precipitation":"0","startTime":"2016-12-15 08:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 10:00:00","highestTemperature":"0","img":"0","isRainFall":"无降水","lowerestTemperature":"0","precipitation":"0","startTime":"2016-12-15 09:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 11:00:00","highestTemperature":"1","img":"0","isRainFall":"无降水","lowerestTemperature":"1","precipitation":"0","startTime":"2016-12-15 10:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 12:00:00","highestTemperature":"2","img":"0","isRainFall":"无降水","lowerestTemperature":"2","precipitation":"0","startTime":"2016-12-15 11:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 13:00:00","highestTemperature":"3","img":"0","isRainFall":"无降水","lowerestTemperature":"3","precipitation":"0","startTime":"2016-12-15 12:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 14:00:00","highestTemperature":"4","img":"0","isRainFall":"无降水","lowerestTemperature":"4","precipitation":"0","startTime":"2016-12-15 13:00:00","wd":"","weather":"晴","ws":""},{"endTime":"2016-12-15 15:00:00","highestTemperature":"4","img":"1","isRainFall":"无降水","lowerestTemperature":"4","precipitation":"0","startTime":"2016-12-15 14:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 16:00:00","highestTemperature":"4","img":"2","isRainFall":"无降水","lowerestTemperature":"4","precipitation":"0","startTime":"2016-12-15 15:00:00","wd":"","weather":"阴","ws":""},{"endTime":"2016-12-15 17:00:00","highestTemperature":"3","img":"1","isRainFall":"无降水","lowerestTemperature":"3","precipitation":"0","startTime":"2016-12-15 16:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 18:00:00","highestTemperature":"1","img":"1","isRainFall":"无降水","lowerestTemperature":"1","precipitation":"0","startTime":"2016-12-15 17:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 19:00:00","highestTemperature":"1","img":"1","isRainFall":"无降水","lowerestTemperature":"1","precipitation":"0","startTime":"2016-12-15 18:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 20:00:00","highestTemperature":"0","img":"1","isRainFall":"无降水","lowerestTemperature":"0","precipitation":"0","startTime":"2016-12-15 19:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 21:00:00","highestTemperature":"-1","img":"1","isRainFall":"无降水","lowerestTemperature":"-1","precipitation":"0","startTime":"2016-12-15 20:00:00","wd":"","weather":"多云","ws":""},{"endTime":"2016-12-15 22:00:00","highestTemperature":"-1","img":"2","isRainFall":"无降水","lowerestTemperature":"-1","precipitation":"0","startTime":"2016-12-15 21:00:00","wd":"","weather":"阴","ws":""},{"endTime":"2016-12-15 23:00:00","highestTemperature":"-2","img":"2","isRainFall":"无降水","lowerestTemperature":"-2","precipitation":"0","startTime":"2016-12-15 22:00:00","wd":"","weather":"阴","ws":""}]
     */

    public WeatherDetailsInfo weatherDetailsInfo;
    /**
     * alarmContent : 青岛市气象台2016年12月14日14时30分继续发布寒潮蓝色预警信号：受北方冷空气影响，预计今天下午到明天夜间，我市北风陆地4到5级阵风8级，沿海地区5到6级阵风9级，海上7到8级阵风10级。14日和15日夜间气温较低，最低气温市区及沿海地区-3℃左右，内陆地区-7到-5℃，请注意防范。
     * alarmDesc : 48小时内最低气温下降8℃以上，最低气温≤4℃，并可能持续。
     * alarmId : 201612141432548570寒潮蓝色
     * alarmLevelNo : 01
     * alarmLevelNoDesc : 蓝色
     * alarmType : 04
     * alarmTypeDesc : 寒潮预警
     * precaution : 1.准备手套、帽子、围巾、口罩等御寒衣物；

     2.检查取暖设备，确保正常使用。
     * publishTime : 12月14日 14:30 发布
     */

    public List<Alarms> alarms;
    /**
     * abbreviation : zs
     * alias :
     * content : 温度不高，其他各项气象条件适宜，中暑机率极低。
     * level : 无
     * name : 中暑指数
     */

    public List<Indexes> indexes;
    /**
     * date : 2016-12-14
     * img : 1
     * sun_down_time : 16:45
     * sun_rise_time : 07:01
     * temp_day_c : 3
     * temp_day_f : 37.4
     * temp_night_c : -3
     * temp_night_f : 26.6
     * wd : 无持续风向
     * weather : 多云
     * week : 星期三
     * ws : 3级
     */

    public List<Weathers> weathers;

    public static class Pm25 {
        public String advice;
        public String aqi;
        public int citycount;
        public int cityrank;
        public String co;
        public String color;
        public String level;
        public String no2;
        public String o3;
        public String pm10;
        public String pm25;
        public String quality;
        public String so2;
        public String timestamp;
        public String upDateTime;
    }

    public static class Realtime {
        public String img;
        public String sD;
        public String sendibleTemp;
        public String temp;
        public String time;
        public String wD;
        public String wS;
        public String weather;
        public String ziwaixian;
    }

    public static class WeatherDetailsInfo {
        public String publishTime;
        /**
         * endTime : 2016-12-14 23:00:00
         * highestTemperature : -4
         * img : 1
         * isRainFall : 无降水
         * lowerestTemperature : -4
         * precipitation : 0
         * startTime : 2016-12-14 22:00:00
         * wd :
         * weather : 多云
         * ws :
         */

        public List<Weather24HoursDetailsInfos> weather24HoursDetailsInfos;

        public static class Weather24HoursDetailsInfos {
            public String endTime;
            public String highestTemperature;
            public String img;
            public String isRainFall;
            public String lowerestTemperature;
            public String precipitation;
            public String startTime;
            public String wd;
            public String weather;
            public String ws;
        }
    }

    public static class Alarms {
        public String alarmContent;
        public String alarmDesc;
        public String alarmId;
        public String alarmLevelNo;
        public String alarmLevelNoDesc;
        public String alarmType;
        public String alarmTypeDesc;
        public String precaution;
        public String publishTime;
    }

    public static class Indexes {
        public String abbreviation;
        public String alias;
        public String content;
        public String level;
        public String name;
    }

    public static class Weathers {
        public String date;
        public String img;
        public String sun_down_time;
        public String sun_rise_time;
        public String temp_day_c;
        public String temp_day_f;
        public String temp_night_c;
        public String temp_night_f;
        public String wd;
        public String weather;
        public String week;
        public String ws;
    }
}
