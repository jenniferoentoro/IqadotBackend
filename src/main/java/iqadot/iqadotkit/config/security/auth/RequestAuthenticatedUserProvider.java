package iqadot.iqadotkit.config.security.auth;

import iqadot.iqadotkit.config.security.token.*;
import org.springframework.context.annotation.*;
import org.springframework.security.core.*;
import org.springframework.security.core.context.*;
import org.springframework.web.context.*;

@Configuration
public class RequestAuthenticatedUserProvider {

    @Bean
    @Scope(value = WebApplicationContext.SCOPE_REQUEST, proxyMode = ScopedProxyMode.TARGET_CLASS)
    public AccessToken getAuthenticatedUserInRequest() {
        final SecurityContext context = SecurityContextHolder.getContext();
        if (context == null) {
            return null;
        }

        final Authentication authentication = context.getAuthentication();
        if (authentication == null) {
            return null;
        }

        final Object details = authentication.getDetails();
        if (!(details instanceof AccessToken)) {
            return null;
        }

        return (AccessToken) authentication.getDetails();
    }
}
