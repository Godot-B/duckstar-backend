package com.duckstar.apiPayload.exception.handler;

import com.duckstar.apiPayload.code.BaseErrorCode;
import com.duckstar.apiPayload.exception.GeneralException;

public class ImageHandler extends GeneralException {
  public ImageHandler(BaseErrorCode code) {
    super(code);
  }
}
