/**
 * This file is part of Breezy Weather.
 *
 * Breezy Weather is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by the
 * Free Software Foundation, version 3 of the License.
 *
 * Breezy Weather is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Breezy Weather. If not, see <https://www.gnu.org/licenses/>.
 */

package org.breezyweather.sources.cwa

import breezyweather.domain.location.model.Location
import com.google.maps.android.SphericalUtil
import com.google.maps.android.model.LatLng

// This function returns the geographically nearest CWA station ID from a list of stations.
// This function is only used for getting UV Index and temperature normals.
fun getNearestStation(location: Location, stationList: Map<String, Map<String, Double>>): String? {
    var distance: Double
    var nearestStation: String? = null
    var nearestDistance: Double

    nearestDistance = Double.POSITIVE_INFINITY
    stationList.forEach { station ->
        distance = SphericalUtil.computeDistanceBetween(
            LatLng(location.latitude, location.longitude),
            LatLng(station.value["lat"] as Double, station.value["lon"] as Double)
        )
        if (distance < nearestDistance) {
            nearestStation = station.key
            nearestDistance = distance
        }
    }
    return nearestStation
}

// Temperature normals are only available at 26 stations (out of 700+).
// They are not available from the main weather API call,
// and must be called with a different endpoint with the exact station ID.
// This list allows matching a location to the nearest of those 26 stations.
//
// Information is updated once every 10 years. Last update was after 2020.
//
// Source (last checked 2024-05-29):
// https://opendata.cwa.gov.tw/dataset/climate/C-B0027-001
val CWA_NORMALS_STATIONS = mapOf<String, Map<String, Double>>(
    "466880" to mapOf("lat" to 24.997646, "lon" to 121.44202),  // 板橋 BANQIAO
    "466900" to mapOf("lat" to 25.164888, "lon" to 121.448906), // 淡水 TAMSUI
    "466910" to mapOf("lat" to 25.182587, "lon" to 121.52973),  // 鞍部 ANBU
    "466920" to mapOf("lat" to 25.037659, "lon" to 121.514854), // 臺北 TAIPEI
    "466930" to mapOf("lat" to 25.162079, "lon" to 121.54455),  // 竹子湖 ZHUZIHU
    "466940" to mapOf("lat" to 25.133314, "lon" to 121.74048),  // 基隆 KEELUNG
    "466950" to mapOf("lat" to 25.627975, "lon" to 122.07974),  // 彭佳嶼 PENGJIAYU
    "466990" to mapOf("lat" to 23.975128, "lon" to 121.61327),  // 花蓮 HUALIEN
    "467060" to mapOf("lat" to 24.596737, "lon" to 121.85737),  // 蘇澳 SU-AO
    "467080" to mapOf("lat" to 24.763975, "lon" to 121.75653),  // 宜蘭 YILAN
    "467300" to mapOf("lat" to 23.25695, "lon" to 119.667465),  // 東吉島 DONGJIDAO
    "467350" to mapOf("lat" to 23.565502, "lon" to 119.563095), // 澎湖 PENGHU
    "467410" to mapOf("lat" to 22.993238, "lon" to 120.20477),  // 臺南 TAINAN
    "467440" to mapOf("lat" to 22.565992, "lon" to 120.315735), // 高雄 KAOHSIUNG
    "467480" to mapOf("lat" to 23.495926, "lon" to 120.43291),  // 嘉義 CHIAYI
    "467490" to mapOf("lat" to 24.145737, "lon" to 120.684074), // 臺中 TAICHUNG
    "467530" to mapOf("lat" to 23.508207, "lon" to 120.81324),  // 阿里山 ALISHAN
    "467540" to mapOf("lat" to 22.355675, "lon" to 120.903786), // 大武 DAWU
    "467550" to mapOf("lat" to 23.487614, "lon" to 120.95952),  // 玉山 YUSHAN
    "467571" to mapOf("lat" to 24.827852, "lon" to 121.01422),  // 新竹 HSINCHU
    "467590" to mapOf("lat" to 22.003897, "lon" to 120.74634),  // 恆春 HENGCHUN
    "467610" to mapOf("lat" to 23.097486, "lon" to 121.37343),  // 成功 CHENGGONG
    "467620" to mapOf("lat" to 22.036968, "lon" to 121.55834),  // 蘭嶼 LANYU
    "467650" to mapOf("lat" to 23.881325, "lon" to 120.90805),  // 日月潭 SUN MOON LAKE
    "467660" to mapOf("lat" to 22.75221, "lon" to 121.15459),   // 臺東 TAITUNG
    "467770" to mapOf("lat" to 24.256002, "lon" to 120.523384)  // 梧棲 WUQI
)

// CWA issues warnings for different counties and specific townships classified as:
//  • 山區 Mountain ("M"): 59 townships
//  • 基隆北海岸 Keelung North Coast ("K"): 15 townships
//  • 恆春半島 Hengchun Peninsula ("H"): 6 townships
//  • 蘭嶼綠島 Lanyu and Ludao ("L"): 2 townships
//
// Source of township specification (last checked 2024-25-29):
// https://www.cwa.gov.tw/Data/js/info/Info_Town.js
//
// (Township codes in this list have been normalized
//  to match results from the reverse geocoding call.)
val CWA_TOWNSHIP_WARNING_AREAS = mapOf<String, String>(
    "10002110" to "M", // 宜蘭縣大同鄉 Datong Township, Yilan County
    "10002120" to "M", // 宜蘭縣南澳鄉 Nan’ao Township, Yilan County
    "10004080" to "M", // 新竹縣橫山鄉 Hengshan Township, Hsinchu County
    "10004090" to "M", // 新竹縣北埔鄉 Beipu Township, Hsinchu County
    "10004120" to "M", // 新竹縣尖石鄉 Jianshi Township, Hsinchu County
    "10004130" to "M", // 新竹縣五峰鄉 Wufeng Township, Hsinchu County
    "10005070" to "M", // 苗栗縣卓蘭鎮 Zhuolan Township, Miaoli County
    "10005080" to "M", // 苗栗縣大湖鄉 Dahu Township, Miaoli County
    "10005110" to "M", // 苗栗縣南庄鄉 Nanzhuang Township, Miaoli County
    "10005170" to "M", // 苗栗縣獅潭鄉 Shitan Township, Miaoli County
    "10005180" to "M", // 苗栗縣泰安鄉 Tai’an Township, Miaoli County
    "10008020" to "M", // 南投縣埔里鎮 Puli Township, Nantou County
    "10008040" to "M", // 南投縣竹山鎮 Zhushan Township, Nantou County
    "10008070" to "M", // 南投縣鹿谷鄉 Lugu Township, Nantou County
    "10008090" to "M", // 南投縣魚池鄉 Yuchi Township, Nantou County
    "10008100" to "M", // 南投縣國姓鄉 Guoxing Township, Nantou County
    "10008110" to "M", // 南投縣水里鄉 Shuili Township, Nantou County
    "10008120" to "M", // 南投縣信義鄉 Xinyi Township, Nantou County
    "10008130" to "M", // 南投縣仁愛鄉 Ren’ai Township, Nantou County
    "10009070" to "M", // 雲林縣古坑鄉 Gukeng Township, Yunlin County
    "10010140" to "M", // 嘉義縣竹崎鄉 Zhuqi Township, Chiayi County
    "10010150" to "M", // 嘉義縣梅山鄉 Meishan Township, Chiayi County
    "10010160" to "M", // 嘉義縣番路鄉 Fanlu Township, Chiayi County
    "10010170" to "M", // 嘉義縣大埔鄉 Dapu Township, Chiayi County
    "10010180" to "M", // 嘉義縣阿里山鄉 Alishan Township, Chiayi County
    "10013040" to "H", // 屏東縣恆春鎮 Hengchun Township, Pingtung County
    "10013230" to "H", // 屏東縣車城鄉 Checheng Township, Pingtung County
    "10013240" to "H", // 屏東縣滿州鄉 Manzhou Township, Pingtung County
    "10013250" to "H", // 屏東縣枋山鄉 Fangshan Township, Pingtung County
    "10013260" to "M", // 屏東縣三地門鄉 Sandimen Township, Pingtung County
    "10013270" to "M", // 屏東縣霧臺鄉 Wutai Township, Pingtung County
    "10013280" to "M", // 屏東縣瑪家鄉 Majia Township, Pingtung County
    "10013290" to "M", // 屏東縣泰武鄉 Taiwu Township, Pingtung County
    "10013300" to "M", // 屏東縣來義鄉 Laiyi Township, Pingtung County
    "10013310" to "M", // 屏東縣春日鄉 Chunri Township, Pingtung County
    "10013320" to "H", // 屏東縣獅子鄉 Shizi Township, Pingtung County
    "10013330" to "H", // 屏東縣牡丹鄉 Mudan Township, Pingtung County
    "10014040" to "M", // 臺東縣卑南鄉 Beinan Township, Taitung County
    "10014110" to "L", // 臺東縣綠島鄉 Ludao Township, Taitung County
    "10014120" to "M", // 臺東縣海端鄉 Haiduan Township, Taitung County
    "10014130" to "M", // 臺東縣延平鄉 Yanping Township, Taitung County
    "10014140" to "M", // 臺東縣金峰鄉 Jinfeng Township, Taitung County
    "10014150" to "M", // 臺東縣達仁鄉 Daren Township, Taitung County
    "10014160" to "L", // 臺東縣蘭嶼鄉 Lanyu Township, Taitung County
    "10015110" to "M", // 花蓮縣秀林鄉 Xiulin Township, Hualien County
    "10015120" to "M", // 花蓮縣萬榮鄉 Wanrong Township, Hualien County
    "10015130" to "M", // 花蓮縣卓溪鄉 Zhuoxi Township, Hualien County
    "10017010" to "K", // 基隆市中正區 Zhongzheng District, Keelung City
    "10017020" to "K", // 基隆市七堵區 Qidu District, Keelung City
    "10017030" to "K", // 基隆市暖暖區 Nuannuan District, Keelung City
    "10017040" to "K", // 基隆市仁愛區 Ren’ai District, Keelung City
    "10017050" to "K", // 基隆市中山區 Zhongshan District, Keelung City
    "10017060" to "K", // 基隆市安樂區 Anle District, Keelung City
    "10017070" to "K", // 基隆市信義區 Xinyi District, Keelung City
    "63000110" to "M", // 臺北市士林區 Shilin District, Taipei City
    "63000120" to "M", // 臺北市北投區 Beitou District, Taipei City
    "64000320" to "M", // 高雄市六龜區 Liugui District, Kaohsiung City
    "64000330" to "M", // 高雄市甲仙區 Jiaxian District, Kaohsiung City
    "64000360" to "M", // 高雄市茂林區 Maolin District, Kaohsiung City
    "64000370" to "M", // 高雄市桃源區 Taoyuan District, Kaohsiung City
    "64000380" to "M", // 高雄市那瑪夏區 Namaxia District, Kaohsiung City
    "65000090" to "M", // 新北市三峽區 Sanxia District, New Taipei City
    "65000100" to "K", // 新北市淡水區 Tamsui District, New Taipei City
    "65000120" to "K", // 新北市瑞芳區 Ruifang District, New Taipei City
    "65000190" to "M", // 新北市石碇區 Shiding District, New Taipei City
    "65000200" to "M", // 新北市坪林區 Pinglin District, New Taipei City
    "65000210" to "K", // 新北市三芝區 Sanzhi District, New Taipei City
    "65000220" to "K", // 新北市石門區 Shimen District, New Taipei City
    "65000240" to "M", // 新北市平溪區 Pingxi District, New Taipei City
    "65000250" to "K", // 新北市雙溪區 Shuangxi District, New Taipei City
    "65000260" to "K", // 新北市貢寮區 Gongliao District, New Taipei City
    "65000270" to "K", // 新北市金山區 Jinshan District, New Taipei City
    "65000280" to "K", // 新北市萬里區 Wanli District, New Taipei City
    "65000290" to "M", // 新北市烏來區 Wulai District, New Taipei City
    "66000100" to "M", // 臺中市東勢區 Dongshi District, Taichung City
    "66000190" to "M", // 臺中市新社區 Xinshe District, Taichung City
    "66000200" to "M", // 臺中市石岡區 Shigang District, Taichung City
    "66000270" to "M", // 臺中市太平區 Taiping District, Taichung City
    "66000290" to "M", // 臺中市和平區 Heping District, Taichung City
    "67000240" to "M", // 臺南市楠西區 Nanxi District, Tainan City
    "67000250" to "M", // 臺南市南化區 Nanhua District, Tainan City
    "68000130" to "M"  // 桃園市復興區 Fuxing District, Taoyuan City
)

// API endpoints for "Weather Assistant", a collection of human-written text-based
// forecast summary for the general public. "Assistants" are organized by counties.
// List of endpoints can be seen on this page:
// https://opendata.cwa.gov.tw/dataset/all?page=1
// Search for 天氣小幫手
val CWA_ASSISTANT_ENDPOINTS = mapOf<String, String>(
    "臺北市" to "F-C0032-009", // Taipei City
    "新北市" to "F-C0032-010", // New Taipei City
    "基隆市" to "F-C0032-011", // Keelung City
    "花蓮縣" to "F-C0032-012", // Hualien County
    "宜蘭縣" to "F-C0032-013", // Yilan County
    "金門縣" to "F-C0032-014", // Kinmen County
    "澎湖縣" to "F-C0032-015", // Penghu County
    "臺南市" to "F-C0032-016", // Tainan City
    "高雄市" to "F-C0032-017", // Kaohsiung City
    "嘉義縣" to "F-C0032-018", // Chiayi County
    "嘉義市" to "F-C0032-019", // Chiayi City
    "苗栗縣" to "F-C0032-020", // Miaoli County
    "臺中市" to "F-C0032-021", // Taichung City
    "桃園市" to "F-C0032-022", // Taoyuan City
    "新竹縣" to "F-C0032-023", // Hsinchu County
    "新竹市" to "F-C0032-024", // Hsinchu City
    "屏東縣" to "F-C0032-025", // Pingtung County
    "南投縣" to "F-C0032-026", // Nantou County
    "臺東縣" to "F-C0032-027", // Taitung County
    "彰化縣" to "F-C0032-028", // Changhua County
    "雲林縣" to "F-C0032-029", // Yunlin County
    "連江縣" to "F-C0032-030"  // Lienchiang County
)