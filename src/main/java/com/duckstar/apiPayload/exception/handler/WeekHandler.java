package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class WeekHandler extends GeneralException {
    public WeekHandler(BaseErrorCode code) {
        super(code);
    }
}
