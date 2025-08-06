package org.inventorysystem.orderservice.exception;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ErrorCode {

    private static final String BASE_CODE = "ORD-00";

    public static final String ORDER_NOT_COMPLETED_ERROR = BASE_CODE + "01";
    public static final String ORDER_NOT_FOUND = BASE_CODE + "02";

}
