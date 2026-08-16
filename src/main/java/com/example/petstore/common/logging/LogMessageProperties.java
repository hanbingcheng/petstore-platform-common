package com.example.petstore.common.logging;

import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 全モジュールの log-messages.yml に定義された logging.messages を集約するプロパティホルダー。 @PropertySource
 * を指定しないため、spring.config.import で読み込まれたすべての log-messages.yml が Spring Boot
 * の @ConfigurationProperties により自動マージされます。
 *
 * <p>使い方： - 各モジュールの application.yml に spring.config.import: classpath:log-messages.yml を記述 -
 * common/src/main/resources/log-messages.yml に全サービス共通メッセージを定義 - 各サービスの log-messages.yml
 * にサービス固有メッセージを追加 - キー重複時は後から読み込まれた値が優先（サービス固有定義で上書き可能）
 */
@Setter
@Component
@ConfigurationProperties(prefix = "logging")
public class LogMessageProperties {
  private Map<String, Message> messages;

  public String get(String key) {
    Message message = messages != null ? messages.get(key) : null;
    return message != null ? message.getText() : null;
  }

  public String getCode(String key) {
    Message message = messages != null ? messages.get(key) : null;
    return message != null ? message.getCode() : null;
  }

  @Getter
  @Setter
  public static class Message {
    private String code;
    private String text;
  }
}
