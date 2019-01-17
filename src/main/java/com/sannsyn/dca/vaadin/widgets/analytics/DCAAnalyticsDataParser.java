package com.sannsyn.dca.vaadin.widgets.analytics;

import com.google.gson.JsonObject;
import com.sannsyn.dca.util.DCADateUtils;

import java.time.LocalDate;
import java.util.Date;
import java.util.Optional;

import static com.sannsyn.dca.util.DCADateUtils.makeDateString;
import static com.sannsyn.dca.util.DCADateUtils.toLocalDate;

/**
 * Utility class for json parsing and formatting
 * <p>
 * Created by jobaer on 4/28/17.
 */
public class DCAAnalyticsDataParser {
    public static final String SALES_TEXT = "salesText";
    public static final String RECS_TEXT = "recsText";
    public static final String IS_LOGARITHMIC = "isLogarithmic";
    public static final String FROM_DATE = "from";
    public static final String TO_DATE = "to";
    public static final String KEY_UUID = "uuid";
    public static final String ACCOUNT = "accountName";

    private static final String DB_OBJECT = "dbObject";

    static final String NAME = "name";
    static final String TYPE = "type";

    public static String getPropertySafe(JsonObject item, String propertyName) {
        String value = "";

        if (!item.has(DB_OBJECT)) return value;
        JsonObject dbObject = item.get(DB_OBJECT).getAsJsonObject();
        if (!dbObject.has(propertyName)) return value;
        value = dbObject.get(propertyName).getAsString();

        return value;
    }

    public static Boolean getBooleanProperty(JsonObject item, String propertyName) {
        Boolean value;

        if (!item.has(DB_OBJECT)) return false;
        JsonObject dbObject = item.get(DB_OBJECT).getAsJsonObject();
        if (!dbObject.has(propertyName)) return false;
        value = dbObject.get(propertyName).getAsBoolean();

        return value;
    }

    public static Optional<LocalDate> getDateProperty(JsonObject item, String propertyName) {
        if (!item.has(DB_OBJECT)) return Optional.empty();
        JsonObject dbObject = item.get(DB_OBJECT).getAsJsonObject();
        if (!dbObject.has(propertyName)) return Optional.empty();
        String dateString = dbObject.get(propertyName).getAsString();

        LocalDate localDate = DCADateUtils.getDateFromString(dateString);
        return Optional.of(localDate);
    }

    static JsonObject makePreviewJson(JsonObject dbObject) {
        JsonObject object = new JsonObject();
        object.add(DB_OBJECT, dbObject);
        return object;
    }

    static JsonObject createDbObject(String uuid, String name, String type, String salesText,
                                     String recsText, Boolean isLogarithmic, Date fromDate, Date toDate) {
        JsonObject dbObject = new JsonObject();
        dbObject.addProperty(KEY_UUID, uuid);
        dbObject.addProperty(NAME, name);
        dbObject.addProperty(TYPE, type);
        dbObject.addProperty(SALES_TEXT, salesText);
        dbObject.addProperty(RECS_TEXT, recsText);
        dbObject.addProperty(IS_LOGARITHMIC, isLogarithmic);

        if (fromDate != null) {
            dbObject.addProperty(FROM_DATE, makeDateString(toLocalDate(fromDate)));
        }

        if (toDate != null) {
            dbObject.addProperty(TO_DATE, makeDateString(toLocalDate(toDate)));
        }

        return dbObject;
    }

    static JsonObject format(JsonObject dbObject) {
        JsonObject jsonObject = new JsonObject();
        String name = dbObject.get("name").getAsString();
        jsonObject.addProperty("name", name);

        String uuid = dbObject.get(KEY_UUID).getAsString();
        jsonObject.addProperty(KEY_UUID, uuid);

        String from = dbObject.get("from").getAsString();
        String to = dbObject.get("to").getAsString();
        String timePeriod = from + " - " + to;
        jsonObject.addProperty("timePeriod", timePeriod);

        jsonObject.addProperty("channels", "Web");

        jsonObject.add(DB_OBJECT, dbObject);
        return jsonObject;
    }
}


