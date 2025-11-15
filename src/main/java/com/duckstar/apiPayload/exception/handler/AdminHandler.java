package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class AdminHandler extends GeneralException {
    public AdminHandler(BaseErrorCode code) {
        super(code);
    }
}
