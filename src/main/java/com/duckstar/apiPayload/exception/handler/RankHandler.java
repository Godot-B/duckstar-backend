package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class RankHandler extends GeneralException {
    public RankHandler(BaseErrorCode code) {
        super(code);
    }
}
