package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class AuthHandler extends GeneralException {
    public AuthHandler(BaseErrorCode code) {
        super(code);
    }
}
