package com.example.petstore.common.exception;

/** リソースの重複が発生した場合にスローする例外。 HTTP ステータス 409 にマッピングされます。 */
public class DuplicateResourceException extends RuntimeException {

  private final String code;

  public DuplicateResourceException(String message) {
    this(null, message);
  }

  public DuplicateResourceException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
