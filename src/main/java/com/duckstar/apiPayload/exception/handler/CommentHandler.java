package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class CommentHandler extends GeneralException {
    public CommentHandler(BaseErrorCode code) {
        super(code);
    }
}

