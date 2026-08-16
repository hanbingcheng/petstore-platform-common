package com.example.petstore.common.exception;

/** リソースが見つからない場合にスローする例外。 HTTP ステータス 404 にマッピングされます。 */
public class ResourceNotFoundException extends RuntimeException {

  private final String code;

  public ResourceNotFoundException(String message) {
    this(null, message);
  }

  public ResourceNotFoundException(String code, String message) {
    super(message);
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
