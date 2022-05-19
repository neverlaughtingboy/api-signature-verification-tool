package com.neverlaughtingboy.apisignatureverification.exception;

/**
 * AbstractException
 *
 * @author mengyuan.xiang
 * @date 2022/5/19
 */
public abstract class AbstractException extends RuntimeException{
    private String errCode;

    public AbstractException(String errMessage) {
        super(errMessage);
    }

    public AbstractException(String errCode, String errMessage) {
        super(errMessage);
        this.errCode = errCode;
    }

    public AbstractException(String errMessage, Throwable e) {
        super(errMessage, e);
    }

    public AbstractException(String errCode, String errMessage, Throwable e) {
        super(errMessage, e);
        this.errCode = errCode;
    }

    public String getErrCode() {
        return this.errCode;
    }

    public void setErrCode(String errCode) {
        this.errCode = errCode;
    }
}
