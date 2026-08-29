package com.example.petstore.common.client;

import com.example.petstore.common.filter.RequestIdFilter;
import java.io.IOException;
import org.slf4j.MDC;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/** サービス間の outbound HTTP 呼び出し時に、MDC の requestId を X-Request-Id ヘッダーとして伝播するインターセプター。 */
public class RequestIdPropagationInterceptor implements ClientHttpRequestInterceptor {

  @Override
  public ClientHttpResponse intercept(
      HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {

    String requestId = MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY);
    if (requestId != null && !requestId.isBlank()) {
      request.getHeaders().set(RequestIdFilter.REQUEST_ID_HEADER, requestId);
    }
    return execution.execute(request, body);
  }
}
