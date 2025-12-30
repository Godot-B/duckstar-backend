package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class SurveyHandler extends GeneralException {
  public SurveyHandler(BaseErrorCode code) {
    super(code);
  }
}
