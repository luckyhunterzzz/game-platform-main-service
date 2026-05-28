package com.gameplatform.mainservice.configuration;

import io.sentry.Sentry;
import io.sentry.SentryEvent;
import io.sentry.SentryOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.EOFException;
import java.net.SocketException;
import java.util.Locale;

@Configuration
public class SentryConfig {

    @Bean
    public Sentry.OptionsConfiguration<SentryOptions> sentryOptionsConfiguration() {
        return options -> options.setBeforeSend((event, hint) ->
                shouldDropEvent(event) ? null : event
        );
    }

    private boolean shouldDropEvent(SentryEvent event) {
        Throwable throwable = event.getThrowable();
        if (throwable == null) {
            return false;
        }

        return isClientDisconnect(throwable);
    }

    private boolean isClientDisconnect(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String className = current.getClass().getName();
            String simpleName = current.getClass().getSimpleName();
            String message = current.getMessage() == null
                    ? ""
                    : current.getMessage().toLowerCase(Locale.ROOT);

            if ("org.springframework.web.context.request.async.AsyncRequestNotUsableException".equals(className)) {
                return true;
            }

            if ("org.apache.catalina.connector.ClientAbortException".equals(className)) {
                return true;
            }

            if (current instanceof EOFException) {
                return true;
            }

            if (current instanceof SocketException
                    && (message.contains("broken pipe") || message.contains("connection reset"))) {
                return true;
            }

            if (simpleName.contains("AbortedException") || simpleName.contains("ClientAbort")) {
                return true;
            }

            current = current.getCause();
        }

        return false;
    }
}
