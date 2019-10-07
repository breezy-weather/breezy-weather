package wangdaye.com.geometricweather.db.converter;

import java.util.ArrayList;
import java.util.List;

import wangdaye.com.geometricweather.basic.model.weather.Alert;
import wangdaye.com.geometricweather.db.entity.AlertEntity;

public class AlertEntityConverter {

    public static AlertEntity convertToEntity(String cityId, Alert alert) {
        AlertEntity entity = new AlertEntity();

        entity.cityId = cityId;

        entity.alertId = alert.getAlertId();
        entity.date = alert.getDate();
        entity.time = alert.getTime();

        entity.description = alert.getDescription();
        entity.content = alert.getContent();

        entity.type = alert.getType();
        entity.priority = alert.getPriority();
        entity.color = alert.getColor();

        return entity;
    }

    public static List<AlertEntity> convertToEntityList(String cityId, List<Alert> alertList) {
        List<AlertEntity> entityList = new ArrayList<>(alertList.size());
        for (Alert alert : alertList) {
            entityList.add(convertToEntity(cityId, alert));
        }
        return entityList;
    }

    public static Alert convertToModule(AlertEntity entity) {
        return new Alert(
                entity.alertId,
                entity.date,
                entity.time,
                entity.description,
                entity.content,
                entity.type,
                entity.priority,
                entity.color
        );
    }

    public static List<Alert> convertToModuleList(List<AlertEntity> entityList) {
        List<Alert> dailyList = new ArrayList<>(entityList.size());
        for (AlertEntity entity : entityList) {
            dailyList.add(convertToModule(entity));
        }
        return dailyList;
    }
}
