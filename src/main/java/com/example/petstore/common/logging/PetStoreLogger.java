package com.example.petstore.common.logging;

import java.text.MessageFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * PetStore 共通カスタムロガー。 Logger インスタンスを呼び出し元が渡す必要はなく、PetStoreLogger 内部でスタックトレースから 呼び出し元クラスを特定し、対応する
 * Logger を取得します。
 */
@Component
public class PetStoreLogger {

  private final LogMessageProperties logMessageProperties;

  public PetStoreLogger(LogMessageProperties logMessageProperties) {
    this.logMessageProperties = logMessageProperties;
  }

  /** DEBUG レベルのログを出力します。 */
  public void debug(String messageKey, Object... args) {
    Logger log = resolveLogger();
    if (log.isDebugEnabled()) {
      log.debug(format(messageKey, args));
    }
  }

  /** INFO レベルのログを出力します。 */
  public void info(String messageKey, Object... args) {
    Logger log = resolveLogger();
    if (log.isInfoEnabled()) {
      log.info(format(messageKey, args));
    }
  }

  /** WARN レベルのログを出力します。 */
  public void warn(String messageKey, Object... args) {
    Logger log = resolveLogger();
    if (log.isWarnEnabled()) {
      log.warn(format(messageKey, args));
    }
  }

  /** ERROR レベルのログを出力します。 例外オブジェクトを渡すことで、スタックトレースがログに出力されます。 */
  public void error(String messageKey, Throwable e, Object... args) {
    Logger log = resolveLogger();
    if (log.isErrorEnabled()) {
      log.error(format(messageKey, args), e);
    }
  }

  /** 呼び出し元クラスの Logger を取得します。 スタックトレースを走査し、PetStoreLogger 自身のフレームをスキップして 実際の呼び出し元クラスを特定します。 */
  private Logger resolveLogger() {
    return StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE)
        .walk(
            frames ->
                frames
                    .filter(f -> !f.getDeclaringClass().equals(PetStoreLogger.class))
                    .findFirst()
                    .map(f -> LoggerFactory.getLogger(f.getDeclaringClass()))
                    .orElse(LoggerFactory.getLogger(PetStoreLogger.class)));
  }

  private String format(String messageKey, Object... args) {
    String pattern = logMessageProperties.get(messageKey);
    if (pattern == null) {
      return "[PetStore] Unknown message key: " + messageKey;
    }
    String formatted = MessageFormat.format(pattern, args);
    String code = logMessageProperties.getCode(messageKey);
    return code != null ? "[" + code + "] " + formatted : formatted;
  }
}
