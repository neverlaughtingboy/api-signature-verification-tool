package com.neverlaughtingboy.apisignatureverification.exception;

/**
 * Business Exception
 *
 * @author mengyuan.xiang
 * @date 2022/5/19
 */
public class BusinessException extends AbstractException {

    private static final String DEFAULT_ERR_CODE = "SYSTEM_ERROR";

    public BusinessException(String errMessage) {
        super(DEFAULT_ERR_CODE, errMessage);
    }

    public BusinessException(String errCode, String errMessage) {
        super(errCode, errMessage);
    }

    public BusinessException(String errMessage, Throwable e) {
        super(DEFAULT_ERR_CODE, errMessage, e);
    }

    public BusinessException(String errorCode, String errMessage, Throwable e) {
        super(errorCode, errMessage, e);
    }

}
