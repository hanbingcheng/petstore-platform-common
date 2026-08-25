package com.example.petstore.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * リクエストIDを採番し、MDC に格納するフィルター。
 *
 * <p>X-Request-Id ヘッダーが存在すればそれを採用し、無ければ UUID を新規採番する。
 * MDC に格納された requestId は、構造化ログ（5.1.2.1）により JSON のフィールドとして出力される。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER = "X-Request-Id";
  public static final String REQUEST_ID_MDC_KEY = "requestId";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // 1. ヘッダーがあれば採用、無ければ新規採番
    String requestId = request.getHeader(REQUEST_ID_HEADER);
    if (requestId == null || requestId.isBlank()) {
      requestId = UUID.randomUUID().toString();
    }

    // 2. MDC に格納（以降のログに自動付与される）
    MDC.put(REQUEST_ID_MDC_KEY, requestId);

    // 3. レスポンスにも付与し、クライアントや後続サービスが参照できるようにする
    response.setHeader(REQUEST_ID_HEADER, requestId);

    try {
      filterChain.doFilter(request, response);
    } finally {
      // 4. スレッドプール再利用時に値が残留しないよう必ずクリア
      MDC.remove(REQUEST_ID_MDC_KEY);
    }
  }
}