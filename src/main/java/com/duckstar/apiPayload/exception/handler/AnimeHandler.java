package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class AnimeHandler extends GeneralException {
  public AnimeHandler(BaseErrorCode code) {
    super(code);
  }
}
