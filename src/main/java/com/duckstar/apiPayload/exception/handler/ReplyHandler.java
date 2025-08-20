package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class ReplyHandler extends GeneralException {
    public ReplyHandler(BaseErrorCode code) {
        super(code);
    }
}
