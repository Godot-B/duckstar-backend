package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class QuarterHandler extends GeneralException {
  public QuarterHandler(BaseErrorCode code) {
    super(code);
  }
}
