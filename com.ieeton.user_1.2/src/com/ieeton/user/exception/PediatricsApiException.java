package com.ieeton.user.exception;

import com.ieeton.user.models.ErrorMessage;


/**
 * Thrown when there were problems parsing the response to an API call,
 * either because the response was empty, or it was malformed.
 */
public class PediatricsApiException extends Exception {

    private static final long serialVersionUID = -5143101071713313135L;
    
    
    //错误信息
    private ErrorMessage mErrMessage;
    

    public ErrorMessage getErrMessage() {
        return mErrMessage;
    }


    public PediatricsApiException() {
        super();
    }

    public PediatricsApiException(String detailMessage) {
        super(detailMessage);
    }
    
    public PediatricsApiException(ErrorMessage err) {
        super("Error Code:" + err.errorcode + ",Reason:"
                + err.errmsg);
        mErrMessage = err;
    }

    public PediatricsApiException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public PediatricsApiException(Throwable throwable) {
        super(throwable);
    }
        
}
