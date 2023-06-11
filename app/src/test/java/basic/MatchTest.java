package basic;

import android.text.TextUtils;

import androidx.annotation.NonNull;

import org.junit.jupiter.api.Test;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatchTest {

    @Test
    public void pattern() {
        String text = "Frigid with snow, acuu an additional 1-3 cm; limited outdoor activity. 2-4 cm, 4-5cm";
        String NumberPattern = "\\d+-\\d+(\\s+)?cm";

        Pattern pattern = Pattern.compile(NumberPattern);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()){
            int start = matcher.start();
            int end = matcher.end();
            System.out.println(text.substring(start, end));
        }
    }

    @Test
    public void split() {
        String text = "dadasd dsad   dad";
        System.out.println(Arrays.toString(text.split("-")));
    }

    @Test
    public void convertUnit() {
        String str = "Frigid with snow, acuu an additional 1-3 cm; limited outdoor activity. 2-4 cm, 4-5cm";

        if (TextUtils.isEmpty(str)) {
            return;
        }

        // cm.
        str = convertUnit(str, "cm", value -> value * 10);

        System.out.println(str);
    }

    @Test
    public void formatFloat() {
        System.out.println(formatFloat(7.00646f, 2));
        System.out.println(formatFloat(7.00246f, 2));
    }

    static String formatFloat(float value, int decimalNumber) {
        float factor = (float) Math.pow(10, decimalNumber);
        if (Math.round(value) * factor == Math.round(value * factor)) {
            return String.valueOf(Math.round(value));
        }
        return String.format(
                "%." + decimalNumber + "f",
                value
        );
    }

    private interface MilliMeterConverter {
        float toMilliMeters(float value);
    }

    private static String convertUnit(@NonNull String str,
                                      String targetUnit,
                                      MilliMeterConverter converter) {
        String numberPattern = "\\d+-\\d+(\\s+)?";

        // cm
        Matcher matcher = Pattern.compile(numberPattern + targetUnit).matcher(str);
        List<String> targetList = new ArrayList<>();
        List<String> resultList = new ArrayList<>();
        while (matcher.find()) {
            String target = str.substring(matcher.start(), matcher.end());
            targetList.add(target);

            String[] targetSplitResults = target.replaceAll(" ", "").split(targetUnit);
            String[] numberTexts = targetSplitResults[0].split("-");
            for (int i = 0; i < numberTexts.length; i++) {
                float number = Float.parseFloat(numberTexts[i]);
                number = converter.toMilliMeters(number);
                numberTexts[i] = floatToString(number);
            }

            resultList.add(arrayToString(numberTexts, '-')
                    + " " + "mm");
        }
        for (int i = 0; i < targetList.size(); i++) {
            str = str.replace(targetList.get(i), resultList.get(i));
        }

        return str;
    }

    private static String floatToString(float number) {
        if ((int) number * 1000 == (int) (number * 1000)) {
            return String.valueOf((int) number);
        } else {
            return new DecimalFormat("######0.0").format(number);
        }
    }

    private static String arrayToString(String[] array, char separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            builder.append(array[i]);
            if (i < array.length - 1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }

    private static String listToString(List<String> list, char separator) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < list.size(); i++) {
            builder.append(list.get(i));
            if (i < list.size() - 1) {
                builder.append(separator);
            }
        }
        return builder.toString();
    }
}
