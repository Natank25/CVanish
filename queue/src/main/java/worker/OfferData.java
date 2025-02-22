package worker;

import java.util.List;

public enum OfferData {


    USER_ID("user_id"),
    FIRST_NAME("first_name"),
    LAST_NAME("last_name");
    final String label;

    OfferData(String str){
        this.label = str;
    }

    public String getLabel(){
        return this.label;
    }


    public static final List<OfferData> DB_COLUMNS = List.of(USER_ID, FIRST_NAME, LAST_NAME);
}
