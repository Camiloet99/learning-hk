package org.inventorysystem.inventoryservice.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCode {

    private static final String BASE_CODE = "INV-00";

    public static final String INSUFFICIENT_STOCK_ERROR = BASE_CODE + "01";
    public static final String INVENTORY_NOT_FOUND_ERROR = BASE_CODE + "02";
    public static final String KAFKA_PUBLISH_ERROR = BASE_CODE + "03";

}
