package wangdaye.com.geometricweather.basic.model.option;

import android.content.Context;

import androidx.annotation.Nullable;

import wangdaye.com.geometricweather.R;
import wangdaye.com.geometricweather.basic.model.option.utils.OptionUtils;

public enum CardOrder {

    DAILY_FIRST("daily_first"),
    HOURLY_FIRST("hourly_first");

    private String orderId;

    CardOrder(String orderId) {
        this.orderId = orderId;
    }

    @Nullable
    public String getCardOrderName(Context context) {
        return OptionUtils.getNameByValue(
                context,
                orderId,
                R.array.card_orders,
                R.array.card_order_values
        );
    }
}
