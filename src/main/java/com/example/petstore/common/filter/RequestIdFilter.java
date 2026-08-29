package com.example.petstore.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** 流入時に X-Request-Id を採番（または採用）し、MDC とリクエストヘッダーへ設定して後段サービスへ伝播するフィルター。 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestIdFilter extends OncePerRequestFilter {

  public static final String REQUEST_ID_HEADER = "X-Request-Id";
  public static final String REQUEST_ID_MDC_KEY = "requestId";
  public static final String HTTP_METHOD_MDC_KEY = "httpMethod";
  public static final String HTTP_PATH_MDC_KEY = "httpPath";

  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    // 匿名内部クラス（HttpServletRequestWrapper）から参照するため final にする
    String headerRequestId = request.getHeader(REQUEST_ID_HEADER);
    final String requestId =
        (headerRequestId == null || headerRequestId.isBlank())
            ? UUID.randomUUID().toString()
            : headerRequestId;

    MDC.put(REQUEST_ID_MDC_KEY, requestId);
    MDC.put(HTTP_METHOD_MDC_KEY, request.getMethod());
    MDC.put(HTTP_PATH_MDC_KEY, request.getRequestURI());
    response.setHeader(REQUEST_ID_HEADER, requestId);

    // 後段サービスへ X-Request-Id を伝播するため、ヘッダーを付与したラッパーでラップする
    HttpServletRequest wrappedRequest =
        new HttpServletRequestWrapper(request) {
          @Override
          public String getHeader(String name) {
            if (REQUEST_ID_HEADER.equalsIgnoreCase(name)) {
              return requestId;
            }
            return super.getHeader(name);
          }

          @Override
          public Enumeration<String> getHeaders(String name) {
            if (REQUEST_ID_HEADER.equalsIgnoreCase(name)) {
              return Collections.enumeration(Collections.singletonList(requestId));
            }
            return super.getHeaders(name);
          }

          @Override
          public Enumeration<String> getHeaderNames() {
            // Spring Cloud Gateway MVC は getHeaderNames() で列挙したヘッダーを後段へ転送するため、
            // X-Request-Id をヘッダー名一覧に追加しておく必要がある
            List<String> names = new ArrayList<>(Collections.list(super.getHeaderNames()));
            if (!names.contains(REQUEST_ID_HEADER)) {
              names.add(REQUEST_ID_HEADER);
            }
            return Collections.enumeration(names);
          }
        };

    try {
      filterChain.doFilter(wrappedRequest, response);
    } finally {
      MDC.remove(REQUEST_ID_MDC_KEY);
      MDC.remove(HTTP_METHOD_MDC_KEY);
      MDC.remove(HTTP_PATH_MDC_KEY);
    }
  }
}