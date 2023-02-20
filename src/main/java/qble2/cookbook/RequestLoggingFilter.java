package qble2.cookbook;

import javax.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.AbstractRequestLoggingFilter;

@Configuration
public class RequestLoggingFilter extends AbstractRequestLoggingFilter {

  @Override
  protected boolean shouldLog(HttpServletRequest request) {
    if (request.getRequestURI().startsWith("/h2-console")
        || request.getRequestURI().startsWith("/favicon")) {
      return false;
    }
    return logger.isDebugEnabled();
  }

  @Override
  protected void beforeRequest(HttpServletRequest request, String message) {
    logger.debug(message);
  }

  @Override
  protected void afterRequest(HttpServletRequest request, String message) {
    logger.debug(message);
  }
}
