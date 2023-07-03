package org.breezyweather.common.basic.models.weather;

import java.io.Serializable;
import java.util.Date;

/**
 * Alert.
 *
 * All properties are {@link androidx.annotation.NonNull}.
 */
public class Alert implements Serializable {

    private final long alertId;
    private final Date startDate;
    private final Date endDate;

    private final String description;
    private final String content;

    private final String type;
    private final int priority;

    public Alert(long alertId, Date startDate, Date endDate,
                 String description, String content,
                 String type, int priority) {
        this.alertId = alertId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.content = content;
        this.type = type;
        this.priority = priority;
    }

    public long getAlertId() {
        return alertId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getDescription() {
        return description;
    }

    public String getContent() {
        return content;
    }

    public String getType() {
        return type;
    }

    public int getPriority() {
        return priority;
    }
}
