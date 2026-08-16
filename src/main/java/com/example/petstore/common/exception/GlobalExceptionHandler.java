package com.example.petstore.common.exception;

import com.example.petstore.common.message.MessageCode;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/** 全サービス共通のグローバル例外ハンドラ。 common モジュールに配置することで、各サービスに個別実装する必要がなくなります。 */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return error(
        HttpStatus.NOT_FOUND,
        codeOrDefault(ex.getCode(), MessageCode.RESOURCE_NOT_FOUND),
        ex.getMessage());
  }

  @ExceptionHandler(DuplicateResourceException.class)
  public ResponseEntity<Map<String, Object>> handleDuplicateResource(
      DuplicateResourceException ex) {
    log.warn("Duplicate resource: {}", ex.getMessage());
    return error(
        HttpStatus.CONFLICT,
        codeOrDefault(ex.getCode(), MessageCode.DUPLICATE_RESOURCE),
        ex.getMessage());
  }

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
    log.warn("Unauthorized: {}", ex.getMessage());
    return error(
        HttpStatus.UNAUTHORIZED,
        codeOrDefault(ex.getCode(), MessageCode.UNAUTHORIZED),
        ex.getMessage());
  }

  // ===== バリデーション例外 =====

  /**
   * @RequestBody の @Valid 検証エラー
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, Object>> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex) {
    log.warn("Request body validation failed: {}", ex.getMessage());
    Map<String, Object> body =
        baseBody(
            HttpStatus.BAD_REQUEST,
            MessageCode.METHOD_ARGUMENT_NOT_VALID.getCode(),
            "Validation failed");
    body.put("errors", toFieldErrors(ex.getBindingResult().getFieldErrors()));
    return ResponseEntity.badRequest().body(body);
  }

  /**
   * @RequestParam / @PathVariable の @Validated 検証エラー
   */
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<Map<String, Object>> handleConstraintViolation(
      ConstraintViolationException ex) {
    log.warn("Constraint violation: {}", ex.getMessage());
    Map<String, Object> body =
        baseBody(
            HttpStatus.BAD_REQUEST,
            MessageCode.CONSTRAINT_VIOLATION.getCode(),
            "Validation failed");
    List<Map<String, String>> errors =
        ex.getConstraintViolations().stream()
            .map(
                cv -> {
                  Map<String, String> error = new LinkedHashMap<>();
                  error.put("field", cv.getPropertyPath().toString());
                  error.put("message", cv.getMessage());
                  return error;
                })
            .toList();
    body.put("errors", errors);
    return ResponseEntity.badRequest().body(body);
  }

  /** フォーム/モデル属性のバインディング・検証エラー */
  @ExceptionHandler(BindException.class)
  public ResponseEntity<Map<String, Object>> handleBind(BindException ex) {
    log.warn("Binding validation failed: {}", ex.getMessage());
    Map<String, Object> body =
        baseBody(HttpStatus.BAD_REQUEST, MessageCode.BIND_ERROR.getCode(), "Validation failed");
    body.put("errors", toFieldErrors(ex.getBindingResult().getFieldErrors()));
    return ResponseEntity.badRequest().body(body);
  }

  // ===== リクエスト解析例外 =====

  /** リクエストボディのJSON解析エラー（不正なJSONなど） */
  @ExceptionHandler(HttpMessageNotReadableException.class)
  public ResponseEntity<Map<String, Object>> handleHttpMessageNotReadable(
      HttpMessageNotReadableException ex) {
    log.warn("Malformed request body: {}", ex.getMessage());
    return error(
        HttpStatus.BAD_REQUEST,
        MessageCode.MESSAGE_NOT_READABLE.getCode(),
        "Malformed request body");
  }

  /** リクエストパラメータの型変換エラー（例: String → Long） */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<Map<String, Object>> handleTypeMismatch(
      MethodArgumentTypeMismatchException ex) {
    String message =
        String.format("Parameter '%s' has invalid value: %s", ex.getName(), ex.getValue());
    log.warn("Type mismatch: {}", message);
    return error(HttpStatus.BAD_REQUEST, MessageCode.TYPE_MISMATCH.getCode(), message);
  }

  /** 必須リクエストパラメータ欠落 */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<Map<String, Object>> handleMissingParameter(
      MissingServletRequestParameterException ex) {
    String message = String.format("Required parameter '%s' is missing", ex.getParameterName());
    log.warn("Missing parameter: {}", message);
    return error(HttpStatus.BAD_REQUEST, MessageCode.MISSING_PARAMETER.getCode(), message);
  }

  /** 必須リクエストヘッダ欠落 */
  @ExceptionHandler(MissingRequestHeaderException.class)
  public ResponseEntity<Map<String, Object>> handleMissingHeader(MissingRequestHeaderException ex) {
    String message = String.format("Required header '%s' is missing", ex.getHeaderName());
    log.warn("Missing header: {}", message);
    return error(HttpStatus.BAD_REQUEST, MessageCode.MISSING_HEADER.getCode(), message);
  }

  /** リクエストバインディングエラー（パラメータ/ヘッダ欠落などの基底クラス） */
  @ExceptionHandler(ServletRequestBindingException.class)
  public ResponseEntity<Map<String, Object>> handleServletRequestBinding(
      ServletRequestBindingException ex) {
    log.warn("Request binding failed: {}", ex.getMessage());
    return error(HttpStatus.BAD_REQUEST, MessageCode.REQUEST_BINDING.getCode(), ex.getMessage());
  }

  /** multipart リクエストの必須パート欠落 */
  @ExceptionHandler(MissingServletRequestPartException.class)
  public ResponseEntity<Map<String, Object>> handleMissingPart(
      MissingServletRequestPartException ex) {
    String message = String.format("Required part '%s' is missing", ex.getRequestPartName());
    log.warn("Missing request part: {}", message);
    return error(HttpStatus.BAD_REQUEST, MessageCode.MISSING_PART.getCode(), message);
  }

  // ===== HTTPプロトコル例外 =====

  /** サポートされていないHTTPメソッド */
  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleMethodNotSupported(
      HttpRequestMethodNotSupportedException ex) {
    log.warn("Method not supported: {}", ex.getMessage());
    return error(
        HttpStatus.METHOD_NOT_ALLOWED, MessageCode.METHOD_NOT_SUPPORTED.getCode(), ex.getMessage());
  }

  /** サポートされていないContent-Type */
  @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
  public ResponseEntity<Map<String, Object>> handleMediaTypeNotSupported(
      HttpMediaTypeNotSupportedException ex) {
    log.warn("Media type not supported: {}", ex.getMessage());
    return error(
        HttpStatus.UNSUPPORTED_MEDIA_TYPE,
        MessageCode.MEDIA_TYPE_NOT_SUPPORTED.getCode(),
        ex.getMessage());
  }

  /** Accept ヘッダで指定されたメディアタイプを生成できない */
  @ExceptionHandler(HttpMediaTypeNotAcceptableException.class)
  public ResponseEntity<Map<String, Object>> handleMediaTypeNotAcceptable(
      HttpMediaTypeNotAcceptableException ex) {
    log.warn("Media type not acceptable: {}", ex.getMessage());
    return error(
        HttpStatus.NOT_ACCEPTABLE,
        MessageCode.MEDIA_TYPE_NOT_ACCEPTABLE.getCode(),
        ex.getMessage());
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<Map<String, Object>> handleAll(Exception ex) {
    log.error("Unexpected error occurred", ex);
    return error(
        HttpStatus.INTERNAL_SERVER_ERROR,
        MessageCode.INTERNAL_ERROR.getCode(),
        "Internal server error");
  }

  // ===== ヘルパー =====

  private ResponseEntity<Map<String, Object>> error(
      HttpStatus status, String code, String message) {
    return ResponseEntity.status(status).body(baseBody(status, code, message));
  }

  private Map<String, Object> baseBody(HttpStatus status, String code, String message) {
    Map<String, Object> body = new LinkedHashMap<>();
    body.put("status", status.value());
    body.put("code", code);
    body.put("message", message);
    body.put("timestamp", LocalDateTime.now());
    return body;
  }

  private String codeOrDefault(String code, MessageCode defaultCode) {
    return code != null ? code : defaultCode.getCode();
  }

  private List<Map<String, String>> toFieldErrors(List<FieldError> fieldErrors) {
    return fieldErrors.stream()
        .map(
            fe -> {
              Map<String, String> error = new LinkedHashMap<>();
              error.put("field", fe.getField());
              error.put(
                  "message",
                  fe.getDefaultMessage() == null ? "Invalid value" : fe.getDefaultMessage());
              return error;
            })
        .toList();
  }
}
