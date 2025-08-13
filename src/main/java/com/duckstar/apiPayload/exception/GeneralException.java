package com.duckstar.apiPayload.exception;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.code.ErrorReasonDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GeneralException extends RuntimeException {

    private BaseErrorCode code;

    public ErrorReasonDTO getErrorReason() {
        return code.getReason();
    }

    public ErrorReasonDTO getErrorReasonHttpStatus(){
        return code.getReasonHttpStatus();
    }
}