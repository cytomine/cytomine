package be.cytomine.config;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ForcedApplicationJsonContentTypeFilter implements Filter {

    // see ContentTypeRequestWrapper for explanation
    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
        throws IOException,
        ServletException {
        chain.doFilter(new ContentTypeRequestWrapper((HttpServletRequest) req), resp);
    }

}
