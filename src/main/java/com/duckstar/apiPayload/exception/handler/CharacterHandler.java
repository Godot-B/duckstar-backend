package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class CharacterHandler extends GeneralException {
    public CharacterHandler(BaseErrorCode code) {
        super(code);
    }
}
