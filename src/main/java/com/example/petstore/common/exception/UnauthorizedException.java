package com.example.petstore.common.exception;

/** 認証・認可エラーが発生した場合にスローする例外。 HTTP ステータス 401 にマッピングされます。 */
public class UnauthorizedException extends RuntimeException {

  private final String code;

  public UnauthorizedException(String message) {
    this(null, message);
  }

  public UnauthorizedException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
