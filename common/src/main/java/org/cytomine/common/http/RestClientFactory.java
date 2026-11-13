package org.cytomine.common.http;

import java.io.IOException;

import org.cytomine.common.config.security.CytomineAuthenticationSupport;
import org.cytomine.common.security.IncomingAuthorizationContext;
import org.cytomine.common.security.PageableHttpServiceArgumentResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.support.RestClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

@Component
public class RestClientFactory {

    public <T> T createClient(RestClient repositoryRestClient, Class<T> repoType) {
        RestClientAdapter adapter = RestClientAdapter.create(repositoryRestClient);
        HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(adapter)
            .customArgumentResolver(new PageableHttpServiceArgumentResolver())
            .build();
        return factory.createClient(repoType);
    }

    public ClientHttpResponse forwardAuthorizationHeader(
        HttpRequest request,
        byte[] body,
        ClientHttpRequestExecution execution
    ) throws IOException {
        IncomingAuthorizationContext.get().ifPresent(incoming -> {
            HttpHeaders headers = request.getHeaders();
            if (incoming.authorization() != null) {
                headers.set(HttpHeaders.AUTHORIZATION, normalizeAuthorization(incoming.authorization()));
            }
            // A CYTOMINE-scheme signature is an HMAC over method + contentMd5 + contentType + date,
            // so repository needs the exact values the caller signed to recompute a matching signature.
            if (incoming.date() != null) {
                headers.set("date", incoming.date());
            }
            if (incoming.contentMd5() != null) {
                headers.set("content-MD5", incoming.contentMd5());
            }
            if (incoming.contentType() != null) {
                headers.set(HttpHeaders.CONTENT_TYPE, incoming.contentType());
            }
        });
        return execution.execute(request, body);
    }

    private String normalizeAuthorization(String authorization) {
        if (authorization.startsWith("Bearer ") || authorization.startsWith(CytomineAuthenticationSupport.SCHEME)) {
            return authorization;
        }
        return "Bearer " + authorization;
    }
}
