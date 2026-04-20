package example.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class TpcConstants {
    public static final int districtsPerWarehouse = 10;
    public static final int customersPerDistrict = 3000;
    public static final int items = 100000;

    private static final String[] CUSTOMER_LAST_NAME_SYLLABLES = {
            "BAR", "OUGHT", "ABLE", "PRI", "PRES",
            "ESE", "ANTI", "CALLY", "ATION", "EING"
    };

    public static String customerLastNameFor(long customerId) {
        int nameNumber = Math.floorMod((int) customerId - 1, 1000);
        return CUSTOMER_LAST_NAME_SYLLABLES[nameNumber / 100]
                + CUSTOMER_LAST_NAME_SYLLABLES[(nameNumber / 10) % 10]
                + CUSTOMER_LAST_NAME_SYLLABLES[nameNumber % 10];
    }
}
