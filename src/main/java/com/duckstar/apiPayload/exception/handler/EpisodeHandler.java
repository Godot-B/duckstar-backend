package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class EpisodeHandler extends GeneralException {
    public EpisodeHandler(BaseErrorCode code) {
        super(code);
    }
}
