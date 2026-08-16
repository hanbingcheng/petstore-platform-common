package com.example.petstore.common.logging;

import java.lang.annotation.*;

/** execute メソッドに付与することで、処理開始・完了・例外発生時のログを自動出力するアノテーション。 Spring AOP の @Around アドバイスと組み合わせて使用します。 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface StartEndLog {
  /** サービス名（例: "UserCreateService"）。未指定の場合はクラス名が使用されます。 */
  String value() default "";
}
