package wangdaye.com.geometricweather.utils;

import android.content.Context;

import wangdaye.com.geometricweather.R;

/**
 * Value utils.
 * */

public class ValueUtils {

    public static String getChineseSource(Context c, String value) {
        switch (value) {
            case "cn":
                return c.getResources().getStringArray(R.array.chinese_sources)[0];

            default:
                return c.getResources().getStringArray(R.array.chinese_sources)[1];
        }
    }

    public static String getUIStyle(Context c, String value) {
        switch (value) {
            case "circular":
                return c.getResources().getStringArray(R.array.ui_styles)[0];

            default:
                return c.getResources().getStringArray(R.array.ui_styles)[1];
        }
    }

    public static String getCardOrder(Context c, String value) {
        switch (value) {
            case "daily_first":
                return c.getResources().getStringArray(R.array.card_orders)[0];

            default:
                return c.getResources().getStringArray(R.array.card_orders)[1];
        }
    }

    public static float getRefreshRateScale(String value) {
        switch (value) {
            case "0:30":
                return 0.5f;

            case "1:00":
                return 1.0f;

            case "2:00":
                return 2.0f;

            case "2:30":
                return 2.5f;

            case "3:00":
                return 3.0f;

            case "3:30":
                return 3.0f;

            case "4:00":
                return 4.0f;

            default:
                return 1.5f;
        }
    }

    public static String getLanguage(Context c, String value) {
        switch (value) {
            case "follow_system":
                return c.getResources().getStringArray(R.array.languages)[0];

            case "chinese":
                return c.getResources().getStringArray(R.array.languages)[1];

            case "unsimplified_chinese":
                return c.getResources().getStringArray(R.array.languages)[2];

            case "english_america":
                return c.getResources().getStringArray(R.array.languages)[3];

            case "english_britain":
                return c.getResources().getStringArray(R.array.languages)[4];

            case "english_australia":
                return c.getResources().getStringArray(R.array.languages)[5];

            case "turkish":
                return c.getResources().getStringArray(R.array.languages)[6];

            case "french":
                return c.getResources().getStringArray(R.array.languages)[7];

            case "russian":
                return c.getResources().getStringArray(R.array.languages)[8];

            case "german":
                return c.getResources().getStringArray(R.array.languages)[9];

            case "serbian":
                return c.getResources().getStringArray(R.array.languages)[10];

            case "spanish":
                return c.getResources().getStringArray(R.array.languages)[11];

            case "italian":
                return c.getResources().getStringArray(R.array.languages)[12];

            default:
                return null;
        }
    }

    public static String getNotificationTextColor(Context c, String value) {
        switch (value) {
            case "dark":
                return c.getResources().getStringArray(R.array.notification_text_colors)[0];

            case "grey":
                return c.getResources().getStringArray(R.array.notification_text_colors)[1];

            default:
                return c.getResources().getStringArray(R.array.notification_text_colors)[2];
        }
    }

    public static String getIconStyle(Context c, String value) {
        switch (value) {
            case "material":
                return c.getResources().getStringArray(R.array.icon_styles)[0];

            default:
                return c.getResources().getStringArray(R.array.icon_styles)[1];
        }
    }

    public static String buildCurrentTemp(int temp, boolean space, boolean f) {
        if (f) {
            return calcFahrenheit(temp) + (space ? " ℉" : "℉");
        } else {
            return temp + (space ? " ℃" : "℃");
        }
    }

    public static String buildAbbreviatedCurrentTemp(int temp, boolean f) {
        if (f) {
            return calcFahrenheit(temp) + "°";
        } else {
            return temp + "°";
        }
    }

    public static String buildDailyTemp(int[] temps, boolean space, boolean f) {
        if (f) {
            return calcFahrenheit(temps[1]) + (space ? "° / " : "/") + calcFahrenheit(temps[0]) + "°";
        } else {
            return temps[1] + (space ? "° / " : "/") + temps[0] + "°";
        }
    }

    public static int calcFahrenheit(int temp) {
        return (int) (9.0 / 5.0 * temp + 32);
    }

    public static int getTempIconId(int temp) {
        if (temp >= 0) {
            switch (temp) {
                case 0:
                    return R.drawable.notif_temp_0;

                case 1:
                    return R.drawable.notif_temp_1;

                case 2:
                    return R.drawable.notif_temp_2;

                case 3:
                    return R.drawable.notif_temp_3;

                case 4:
                    return R.drawable.notif_temp_4;

                case 5:
                    return R.drawable.notif_temp_5;

                case 6:
                    return R.drawable.notif_temp_6;

                case 7:
                    return R.drawable.notif_temp_7;

                case 8:
                    return R.drawable.notif_temp_8;

                case 9:
                    return R.drawable.notif_temp_9;

                case 10:
                    return R.drawable.notif_temp_10;

                case 11:
                    return R.drawable.notif_temp_11;

                case 12:
                    return R.drawable.notif_temp_12;

                case 13:
                    return R.drawable.notif_temp_13;

                case 14:
                    return R.drawable.notif_temp_14;

                case 15:
                    return R.drawable.notif_temp_15;

                case 16:
                    return R.drawable.notif_temp_16;

                case 17:
                    return R.drawable.notif_temp_17;

                case 18:
                    return R.drawable.notif_temp_18;

                case 19:
                    return R.drawable.notif_temp_19;

                case 20:
                    return R.drawable.notif_temp_20;

                case 21:
                    return R.drawable.notif_temp_21;

                case 22:
                    return R.drawable.notif_temp_22;

                case 23:
                    return R.drawable.notif_temp_23;

                case 24:
                    return R.drawable.notif_temp_24;

                case 25:
                    return R.drawable.notif_temp_25;

                case 26:
                    return R.drawable.notif_temp_26;

                case 27:
                    return R.drawable.notif_temp_27;

                case 28:
                    return R.drawable.notif_temp_28;

                case 29:
                    return R.drawable.notif_temp_29;

                case 30:
                    return R.drawable.notif_temp_30;

                case 31:
                    return R.drawable.notif_temp_31;

                case 32:
                    return R.drawable.notif_temp_32;

                case 33:
                    return R.drawable.notif_temp_33;

                case 34:
                    return R.drawable.notif_temp_34;

                case 35:
                    return R.drawable.notif_temp_35;

                case 36:
                    return R.drawable.notif_temp_36;

                case 37:
                    return R.drawable.notif_temp_37;

                case 38:
                    return R.drawable.notif_temp_38;

                case 39:
                    return R.drawable.notif_temp_39;

                case 40:
                    return R.drawable.notif_temp_40;

                case 41:
                    return R.drawable.notif_temp_41;

                case 42:
                    return R.drawable.notif_temp_42;

                case 43:
                    return R.drawable.notif_temp_43;

                case 44:
                    return R.drawable.notif_temp_44;

                case 45:
                    return R.drawable.notif_temp_45;

                case 46:
                    return R.drawable.notif_temp_46;

                case 47:
                    return R.drawable.notif_temp_47;

                case 48:
                    return R.drawable.notif_temp_48;

                case 49:
                    return R.drawable.notif_temp_49;

                case 50:
                    return R.drawable.notif_temp_50;

                case 51:
                    return R.drawable.notif_temp_51;

                case 52:
                    return R.drawable.notif_temp_52;

                case 53:
                    return R.drawable.notif_temp_53;

                case 54:
                    return R.drawable.notif_temp_54;

                case 55:
                    return R.drawable.notif_temp_55;

                case 56:
                    return R.drawable.notif_temp_56;

                case 57:
                    return R.drawable.notif_temp_57;

                case 58:
                    return R.drawable.notif_temp_58;

                case 59:
                    return R.drawable.notif_temp_59;

                case 60:
                    return R.drawable.notif_temp_60;

                case 61:
                    return R.drawable.notif_temp_61;

                case 62:
                    return R.drawable.notif_temp_62;

                case 63:
                    return R.drawable.notif_temp_63;

                case 64:
                    return R.drawable.notif_temp_64;

                case 65:
                    return R.drawable.notif_temp_65;

                case 66:
                    return R.drawable.notif_temp_66;

                case 67:
                    return R.drawable.notif_temp_67;

                case 68:
                    return R.drawable.notif_temp_68;

                case 69:
                    return R.drawable.notif_temp_69;

                case 70:
                    return R.drawable.notif_temp_70;

                case 71:
                    return R.drawable.notif_temp_71;

                case 72:
                    return R.drawable.notif_temp_72;

                case 73:
                    return R.drawable.notif_temp_73;

                case 74:
                    return R.drawable.notif_temp_74;

                case 75:
                    return R.drawable.notif_temp_75;

                case 76:
                    return R.drawable.notif_temp_76;

                case 77:
                    return R.drawable.notif_temp_77;

                case 78:
                    return R.drawable.notif_temp_78;

                case 79:
                    return R.drawable.notif_temp_79;

                case 80:
                    return R.drawable.notif_temp_80;

                case 81:
                    return R.drawable.notif_temp_81;

                case 82:
                    return R.drawable.notif_temp_82;

                case 83:
                    return R.drawable.notif_temp_83;

                case 84:
                    return R.drawable.notif_temp_84;

                case 85:
                    return R.drawable.notif_temp_85;

                case 86:
                    return R.drawable.notif_temp_86;

                case 87:
                    return R.drawable.notif_temp_87;

                case 88:
                    return R.drawable.notif_temp_88;

                case 89:
                    return R.drawable.notif_temp_89;

                case 90:
                    return R.drawable.notif_temp_90;

                case 91:
                    return R.drawable.notif_temp_91;

                case 92:
                    return R.drawable.notif_temp_92;

                case 93:
                    return R.drawable.notif_temp_93;

                case 94:
                    return R.drawable.notif_temp_94;

                case 95:
                    return R.drawable.notif_temp_95;

                case 96:
                    return R.drawable.notif_temp_96;

                case 97:
                    return R.drawable.notif_temp_97;

                case 98:
                    return R.drawable.notif_temp_98;

                case 99:
                    return R.drawable.notif_temp_99;

                case 100:
                    return R.drawable.notif_temp_100;

                case 101:
                    return R.drawable.notif_temp_101;

                case 102:
                    return R.drawable.notif_temp_102;

                case 103:
                    return R.drawable.notif_temp_103;

                case 104:
                    return R.drawable.notif_temp_104;

                case 105:
                    return R.drawable.notif_temp_105;

                case 106:
                    return R.drawable.notif_temp_106;

                case 107:
                    return R.drawable.notif_temp_107;

                case 108:
                    return R.drawable.notif_temp_108;

                case 109:
                    return R.drawable.notif_temp_109;

                case 110:
                    return R.drawable.notif_temp_110;

                case 111:
                    return R.drawable.notif_temp_111;

                case 112:
                    return R.drawable.notif_temp_112;

                case 113:
                    return R.drawable.notif_temp_113;

                case 114:
                    return R.drawable.notif_temp_114;

                case 115:
                    return R.drawable.notif_temp_115;

                case 116:
                    return R.drawable.notif_temp_116;

                case 117:
                    return R.drawable.notif_temp_117;

                case 118:
                    return R.drawable.notif_temp_118;

                case 119:
                    return R.drawable.notif_temp_119;

                case 120:
                    return R.drawable.notif_temp_120;

                case 121:
                    return R.drawable.notif_temp_121;

                case 122:
                    return R.drawable.notif_temp_122;

                case 123:
                    return R.drawable.notif_temp_123;

                case 124:
                    return R.drawable.notif_temp_124;

                case 125:
                    return R.drawable.notif_temp_125;

                case 126:
                    return R.drawable.notif_temp_126;

                case 127:
                    return R.drawable.notif_temp_127;

                case 128:
                    return R.drawable.notif_temp_128;

                case 129:
                    return R.drawable.notif_temp_129;

                case 130:
                    return R.drawable.notif_temp_130;

                case 131:
                    return R.drawable.notif_temp_131;

                case 132:
                    return R.drawable.notif_temp_132;

                case 133:
                    return R.drawable.notif_temp_133;

                case 134:
                    return R.drawable.notif_temp_134;

                case 135:
                    return R.drawable.notif_temp_135;

                case 136:
                    return R.drawable.notif_temp_136;

                case 137:
                    return R.drawable.notif_temp_137;

                case 138:
                    return R.drawable.notif_temp_138;

                case 139:
                    return R.drawable.notif_temp_139;

                default:
                    return R.drawable.notif_temp_140;
            }
        } else {
            switch (temp) {
                case -1:
                    return R.drawable.notif_temp_neg_1;

                case -2:
                    return R.drawable.notif_temp_neg_2;

                case -3:
                    return R.drawable.notif_temp_neg_3;

                case -4:
                    return R.drawable.notif_temp_neg_4;

                case -5:
                    return R.drawable.notif_temp_neg_5;

                case -6:
                    return R.drawable.notif_temp_neg_6;

                case -7:
                    return R.drawable.notif_temp_neg_7;

                case -8:
                    return R.drawable.notif_temp_neg_8;

                case -9:
                    return R.drawable.notif_temp_neg_9;

                case -10:
                    return R.drawable.notif_temp_neg_10;

                case -11:
                    return R.drawable.notif_temp_neg_11;

                case -12:
                    return R.drawable.notif_temp_neg_12;

                case -13:
                    return R.drawable.notif_temp_neg_13;

                case -14:
                    return R.drawable.notif_temp_neg_14;

                case -15:
                    return R.drawable.notif_temp_neg_15;

                case -16:
                    return R.drawable.notif_temp_neg_16;

                case -17:
                    return R.drawable.notif_temp_neg_17;

                case -18:
                    return R.drawable.notif_temp_neg_18;

                case -19:
                    return R.drawable.notif_temp_neg_19;

                case -20:
                    return R.drawable.notif_temp_neg_20;

                case -21:
                    return R.drawable.notif_temp_neg_21;

                case -22:
                    return R.drawable.notif_temp_neg_22;

                case -23:
                    return R.drawable.notif_temp_neg_23;

                case -24:
                    return R.drawable.notif_temp_neg_24;

                case -25:
                    return R.drawable.notif_temp_neg_25;

                case -26:
                    return R.drawable.notif_temp_neg_26;

                case -27:
                    return R.drawable.notif_temp_neg_27;

                case -28:
                    return R.drawable.notif_temp_neg_28;

                case -29:
                    return R.drawable.notif_temp_neg_29;

                case -30:
                    return R.drawable.notif_temp_neg_30;

                case -31:
                    return R.drawable.notif_temp_neg_31;

                case -32:
                    return R.drawable.notif_temp_neg_32;

                case -33:
                    return R.drawable.notif_temp_neg_33;

                case -34:
                    return R.drawable.notif_temp_neg_34;

                case -35:
                    return R.drawable.notif_temp_neg_35;

                case -36:
                    return R.drawable.notif_temp_neg_36;

                case -37:
                    return R.drawable.notif_temp_neg_37;

                case -38:
                    return R.drawable.notif_temp_neg_38;

                case -39:
                    return R.drawable.notif_temp_neg_39;

                case -40:
                    return R.drawable.notif_temp_neg_40;

                case -41:
                    return R.drawable.notif_temp_neg_41;

                case -42:
                    return R.drawable.notif_temp_neg_42;

                case -43:
                    return R.drawable.notif_temp_neg_43;

                case -44:
                    return R.drawable.notif_temp_neg_44;

                case -45:
                    return R.drawable.notif_temp_neg_45;

                case -46:
                    return R.drawable.notif_temp_neg_46;

                case -47:
                    return R.drawable.notif_temp_neg_47;

                case -48:
                    return R.drawable.notif_temp_neg_48;

                case -49:
                    return R.drawable.notif_temp_neg_49;

                case -50:
                    return R.drawable.notif_temp_neg_50;

                case -51:
                    return R.drawable.notif_temp_neg_51;

                case -52:
                    return R.drawable.notif_temp_neg_52;

                case -53:
                    return R.drawable.notif_temp_neg_53;

                case -54:
                    return R.drawable.notif_temp_neg_54;

                case -55:
                    return R.drawable.notif_temp_neg_55;

                case -56:
                    return R.drawable.notif_temp_neg_56;

                case -57:
                    return R.drawable.notif_temp_neg_57;

                case -58:
                    return R.drawable.notif_temp_neg_58;

                case -59:
                    return R.drawable.notif_temp_neg_59;

                case -60:
                    return R.drawable.notif_temp_neg_60;

                case -61:
                    return R.drawable.notif_temp_neg_61;

                case -62:
                    return R.drawable.notif_temp_neg_62;

                case -63:
                    return R.drawable.notif_temp_neg_63;

                case -64:
                    return R.drawable.notif_temp_neg_64;

                case -65:
                    return R.drawable.notif_temp_neg_65;

                case -66:
                    return R.drawable.notif_temp_neg_66;

                case -67:
                    return R.drawable.notif_temp_neg_67;

                case -68:
                    return R.drawable.notif_temp_neg_68;

                case -69:
                    return R.drawable.notif_temp_neg_69;

                case -70:
                    return R.drawable.notif_temp_neg_70;

                case -71:
                    return R.drawable.notif_temp_neg_71;

                case -72:
                    return R.drawable.notif_temp_neg_72;

                case -73:
                    return R.drawable.notif_temp_neg_73;

                case -74:
                    return R.drawable.notif_temp_neg_74;

                case -75:
                    return R.drawable.notif_temp_neg_75;

                case -76:
                    return R.drawable.notif_temp_neg_76;

                case -77:
                    return R.drawable.notif_temp_neg_77;

                case -78:
                    return R.drawable.notif_temp_neg_78;

                case -79:
                    return R.drawable.notif_temp_neg_79;

                case -80:
                    return R.drawable.notif_temp_neg_80;

                case -81:
                    return R.drawable.notif_temp_neg_81;

                case -82:
                    return R.drawable.notif_temp_neg_82;

                case -83:
                    return R.drawable.notif_temp_neg_83;

                case -84:
                    return R.drawable.notif_temp_neg_84;

                case -85:
                    return R.drawable.notif_temp_neg_85;

                case -86:
                    return R.drawable.notif_temp_neg_86;

                case -87:
                    return R.drawable.notif_temp_neg_87;

                case -88:
                    return R.drawable.notif_temp_neg_88;

                case -89:
                    return R.drawable.notif_temp_neg_89;

                case -90:
                    return R.drawable.notif_temp_neg_90;

                case -91:
                    return R.drawable.notif_temp_neg_91;

                case -92:
                    return R.drawable.notif_temp_neg_92;

                case -93:
                    return R.drawable.notif_temp_neg_93;

                case -94:
                    return R.drawable.notif_temp_neg_94;

                case -95:
                    return R.drawable.notif_temp_neg_95;

                case -96:
                    return R.drawable.notif_temp_neg_96;

                case -97:
                    return R.drawable.notif_temp_neg_97;

                case -98:
                    return R.drawable.notif_temp_neg_98;

                case -99:
                    return R.drawable.notif_temp_neg_99;

                case -100:
                    return R.drawable.notif_temp_neg_100;

                case -101:
                    return R.drawable.notif_temp_neg_101;

                case -102:
                    return R.drawable.notif_temp_neg_102;

                case -103:
                    return R.drawable.notif_temp_neg_103;

                case -104:
                    return R.drawable.notif_temp_neg_104;

                case -105:
                    return R.drawable.notif_temp_neg_105;

                case -106:
                    return R.drawable.notif_temp_neg_106;

                case -107:
                    return R.drawable.notif_temp_neg_107;

                case -108:
                    return R.drawable.notif_temp_neg_108;

                case -109:
                    return R.drawable.notif_temp_neg_109;

                case -110:
                    return R.drawable.notif_temp_neg_110;

                case -111:
                    return R.drawable.notif_temp_neg_111;

                case -112:
                    return R.drawable.notif_temp_neg_112;

                case -113:
                    return R.drawable.notif_temp_neg_113;

                case -114:
                    return R.drawable.notif_temp_neg_114;

                case -115:
                    return R.drawable.notif_temp_neg_115;

                case -116:
                    return R.drawable.notif_temp_neg_116;

                case -117:
                    return R.drawable.notif_temp_neg_117;

                case -118:
                    return R.drawable.notif_temp_neg_118;

                case -119:
                    return R.drawable.notif_temp_neg_119;

                case -120:
                    return R.drawable.notif_temp_neg_120;

                case -121:
                    return R.drawable.notif_temp_neg_121;

                case -122:
                    return R.drawable.notif_temp_neg_122;

                case -123:
                    return R.drawable.notif_temp_neg_123;

                case -124:
                    return R.drawable.notif_temp_neg_124;

                case -125:
                    return R.drawable.notif_temp_neg_125;

                case -126:
                    return R.drawable.notif_temp_neg_126;

                case -127:
                    return R.drawable.notif_temp_neg_127;

                case -128:
                    return R.drawable.notif_temp_neg_128;

                case -129:
                    return R.drawable.notif_temp_neg_129;

                case -130:
                    return R.drawable.notif_temp_neg_130;

                case -131:
                    return R.drawable.notif_temp_neg_131;

                case -132:
                    return R.drawable.notif_temp_neg_132;

                case -133:
                    return R.drawable.notif_temp_neg_133;

                case -134:
                    return R.drawable.notif_temp_neg_134;

                case -135:
                    return R.drawable.notif_temp_neg_135;

                case -136:
                    return R.drawable.notif_temp_neg_136;

                case -137:
                    return R.drawable.notif_temp_neg_137;

                case -138:
                    return R.drawable.notif_temp_neg_138;

                case -139:
                    return R.drawable.notif_temp_neg_139;

                default:
                    return R.drawable.notif_temp_neg_140;
            }
        }
    }
}
