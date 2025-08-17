package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class VoteHandler extends GeneralException {
    public VoteHandler(BaseErrorCode code) {
        super(code);
    }
}
