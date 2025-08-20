package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class MemberHandler extends GeneralException {
    public MemberHandler(BaseErrorCode code) {
        super(code);
    }
}
