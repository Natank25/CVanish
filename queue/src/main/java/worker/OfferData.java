package worker;

import java.util.List;
import java.util.Arrays;

public enum OfferData {


    USER_ID("user_id"),
    FIRST_NAME("first_name"),
    LAST_NAME("last_name"),
    EMAIL("email"),
    COUNTRY("country"),
    CITY("city");
    final String label;

    OfferData(String str){
        this.label = str;
    }

    public String getLabel(){
        return this.label;
    }

    public static final List<OfferData> DB_COLUMNS = Arrays.stream(values())
    .toList();
}
