package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class LikeHandler extends GeneralException {
    public LikeHandler(BaseErrorCode code) {
        super(code);
    }
}
