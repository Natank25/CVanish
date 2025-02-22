package worker;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static worker.OfferData.DB_COLUMNS;
import static worker.OfferData.USER_ID;

public class InputOffer {
    ;
    Map<OfferData, String> data = new HashMap<>();

    public static InputOffer createFromJson(JSONObject json) {
        InputOffer offer = new InputOffer();

        for (OfferData value : DB_COLUMNS) {
            System.err.println("value:" + value.label);
            offer.data.put(value, json.getString(value.label));
        }
        return offer;
    }

    public String createInsertStatement() {
        String statement = "INSERT INTO applications (";
        statement += data.keySet().stream()
                .map(offerData -> "\"" + offerData.getLabel() + "\"")
                .collect(Collectors.joining(", "));
        statement += ") VALUES (";

        statement += String.join(", ", data.values().stream().map(s -> "'" + s + "'").toList());
        statement += ")";
        return statement;
    }

    public String createUpdateStatement() {

        String statement = "UPDATE applications SET ";
        statement += data.entrySet().stream().filter(offerDataStringEntry -> offerDataStringEntry.getKey() != USER_ID)
                .map(entry -> "\"" + entry.getKey().getLabel() + "\"" + " = " + "\"" + entry.getValue() + "\"")
                .collect(Collectors.joining(", "));
        statement += " WHERE " + USER_ID.label + " = " + data.get(USER_ID);
        return statement;
    }
}
