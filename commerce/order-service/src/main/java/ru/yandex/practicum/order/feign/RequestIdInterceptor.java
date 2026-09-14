package ru.yandex.practicum.order.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RequestIdInterceptor implements RequestInterceptor {

    private static final String REQUEST_ID = "X-Request-Id";

    private final HttpServletRequest request;

    @Override
    public void apply(RequestTemplate template) {
        String requestId = request.getHeader(REQUEST_ID);

        if (requestId == null || requestId.isBlank()) {
            requestId = UUID.randomUUID().toString();
        }

        template.header(REQUEST_ID, requestId);
    }
}
