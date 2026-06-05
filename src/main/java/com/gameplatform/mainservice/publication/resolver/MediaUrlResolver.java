package com.gameplatform.mainservice.publication.resolver;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class MediaUrlResolver {

    private static final String RU_API_HOST = "ru-api.gameops-platform.dev";

    private final String publicBaseUrl;
    private final String ruPublicBaseUrl;

    public MediaUrlResolver(String publicBaseUrl) {
        this(publicBaseUrl, "https://ru-media.gameops-platform.dev");
    }

    @Autowired
    public MediaUrlResolver(
            @Value("${app.minio.public-base-url}") String publicBaseUrl,
            @Value("${app.minio.ru-public-base-url:https://ru-media.gameops-platform.dev}") String ruPublicBaseUrl
    ) {
        this.publicBaseUrl = publicBaseUrl;
        this.ruPublicBaseUrl = ruPublicBaseUrl;
    }

    public String resolveUrl(String bucket, String objectKey) {
        if (bucket == null || objectKey == null) {
            return null;
        }

        String base = resolveBaseUrl();

        return String.format("%s/%s/%s", base, bucket, objectKey);
    }

    private String resolveBaseUrl() {
        String currentHost = resolveCurrentHost();

        if (RU_API_HOST.equals(currentHost)) {
            return trimTrailingSlash(ruPublicBaseUrl);
        }

        return trimTrailingSlash(publicBaseUrl);
    }

    private String resolveCurrentHost() {
        ServletRequestAttributes requestAttributes =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (requestAttributes == null) {
            return "";
        }

        HttpServletRequest request = requestAttributes.getRequest();
        String forwardedHost = normalizeHost(request.getHeader("X-Forwarded-Host"));
        if (!forwardedHost.isBlank()) {
            return forwardedHost;
        }

        String host = normalizeHost(request.getHeader("Host"));
        if (!host.isBlank()) {
            return host;
        }

        return normalizeHost(request.getServerName());
    }

    private String normalizeHost(String value) {
        if (value == null || value.isBlank()) {
            return "";
        }

        String firstHost = value.split(",")[0].trim().toLowerCase();
        int portSeparatorIndex = firstHost.indexOf(':');

        return portSeparatorIndex >= 0
                ? firstHost.substring(0, portSeparatorIndex)
                : firstHost;
    }

    private String trimTrailingSlash(String value) {
        return value.endsWith("/")
                ? value.substring(0, value.length() - 1)
                : value;
    }
}
