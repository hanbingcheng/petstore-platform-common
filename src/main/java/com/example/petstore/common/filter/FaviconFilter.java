package com.example.petstore.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class FaviconFilter extends OncePerRequestFilter {

  /** favicon.icoが見つからないエラーを抑止するための対応 */
  @Override
  protected void doFilterInternal(
      HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (request.getRequestURI().equals("/favicon.ico")) {
      response.setStatus(HttpServletResponse.SC_NO_CONTENT);
      return;
    }
    filterChain.doFilter(request, response);
  }
}
