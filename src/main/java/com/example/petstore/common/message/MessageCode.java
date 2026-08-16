package com.example.petstore.common.message;

/**
 * common モジュールで使用するメッセージコード定義。
 *
 * <p>形式: {メッセージ種別}{機能別コード}{コード枝番}（計7桁）
 *
 * <ul>
 *   <li>メッセージ種別: I（Info）/ W（Warn）/ E（Error）
 *   <li>機能別コード: 3桁（common = 000, mgmt-service = 001, user-service = 002, order-service = 003）
 *   <li>コード枝番: 3桁（001 から連番）
 * </ul>
 */
public enum MessageCode {
  RESOURCE_NOT_FOUND("E000001"),
  DUPLICATE_RESOURCE("E000002"),
  UNAUTHORIZED("E000003"),
  METHOD_ARGUMENT_NOT_VALID("E000004"),
  CONSTRAINT_VIOLATION("E000005"),
  BIND_ERROR("E000006"),
  MESSAGE_NOT_READABLE("E000007"),
  TYPE_MISMATCH("E000008"),
  MISSING_PARAMETER("E000009"),
  MISSING_HEADER("E000010"),
  REQUEST_BINDING("E000011"),
  MISSING_PART("E000012"),
  METHOD_NOT_SUPPORTED("E000013"),
  MEDIA_TYPE_NOT_SUPPORTED("E000014"),
  MEDIA_TYPE_NOT_ACCEPTABLE("E000015"),
  INTERNAL_ERROR("E000016");

  private final String code;

  MessageCode(String code) {
    this.code = code;
  }

  public String getCode() {
    return code;
  }
}
