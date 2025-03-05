package com.acme.commons.security.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.oidc.web.logout.OidcClientInitiatedLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestCustomizers;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;

/**
 * This class overrides the default Spring Security configuration since the default behaviour
 * protecting all the endpoints inside the application.
 *
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private ClientRegistrationRepository clientRegistrationRepository;

    /**
     * This method builds a custom instance of the security filter chain.
     * .
     * @param http an instance of HttpSecurity. It represents the security configuration for all request incoming.
     * @return a custom instance of SecurityFilterChain.
     * @throws Exception an instance of Exception.
     *
     * @see HttpSecurity
     * @see SecurityFilterChain
     */
    @Bean
    @Order(SecurityProperties.BASIC_AUTH_ORDER)
    public SecurityFilterChain defaultSecurityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(
                authorize ->
                        authorize.requestMatchers("/bugtracker/ui").authenticated()
                                .requestMatchers("/bugtracker/ui/admin/**").hasAnyAuthority("SCOPE_bugtracker.admin")
                                .requestMatchers("/bugtracker/ui/**").hasAnyAuthority("SCOPE_bugtracker.admin", "SCOPE_bugtracker.user")
                                .anyRequest().authenticated())
                .oauth2Login(oauth2 ->
                        oauth2.authorizationEndpoint(
                                cfg -> cfg.authorizationRequestResolver(
                                        pkceResolver(clientRegistrationRepository))))
                .logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler()));

        return http.build();
    }

    private LogoutSuccessHandler oidcLogoutSuccessHandler() {
        OidcClientInitiatedLogoutSuccessHandler oidcLogoutSuccessHandler =
                new OidcClientInitiatedLogoutSuccessHandler(this.clientRegistrationRepository);

        // To set the location where the user will be redirected after logout
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}/bugtracker/ui");

        return oidcLogoutSuccessHandler;
    }

    /**
     * By default, PKEC is not enabled in Spring Security. So that, if you are enabled
     * Authorization Code with PKCE in your identity providers you can get next error:
     *
     * This method enables
     * This method resolves the error:
     * org.springframework.security.oauth2.core.OAuth2AuthenticationException:
     * [invalid_request] Missing parameter: code_challenge_method
     *
     * This means that by default, Spring security does not send any code challenge
     * and code verifier for confidential clients.
     *
     * See:
     * <a href="https://www.baeldung.com/spring-security-pkce-secret-clients">
     *     spring-security-pkce-secret-clients
     * </a>
     *
     * @param repo
     * @return
     */
    public OAuth2AuthorizationRequestResolver pkceResolver(ClientRegistrationRepository repo) {
        var resolver =
                new DefaultOAuth2AuthorizationRequestResolver(
                        repo, "/oauth2/authorization");

        resolver.setAuthorizationRequestCustomizer(OAuth2AuthorizationRequestCustomizers.withPkce());

        return resolver;
    }

}
