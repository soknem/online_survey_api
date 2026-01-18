package com.setec.online_survey.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.authorization.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

 import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
 import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
 import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;


import java.awt.*;
import java.util.Arrays;
import java.util.UUID;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final TokenService tokenService;
    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomUserDetailsService userDetailsService;

    @Value("${security.issuer}")
    private String issuer_uri;

    @Value("${security.postLogoutRedirectUri}")
    private String postLogoutRedirectUri;

    @Value("${security.redirectUri}")
    private String redirectUri;

    @Value("${security.sendRedirect}")
    private String sendRedirect;

    @Value("${security.clientId}")
    private String clientId;

    @Value("${security.clientSecret}")
    private String clientSecret;

    @Bean
    public OAuth2AuthorizationRequestResolver authorizationRequestResolver(
            ClientRegistrationRepository clientRegistrationRepository) {

        DefaultOAuth2AuthorizationRequestResolver resolver =
                new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");

        // This forces the "prompt" parameter into the request sent to Google
        resolver.setAuthorizationRequestCustomizer(customizer ->
                customizer.additionalParameters(params -> params.put("prompt", "select_account"))
        );

        return resolver;
    }

    @Bean
    @Order(1)
    public SecurityFilterChain authServerFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfigurer authServerConfigurer = new OAuth2AuthorizationServerConfigurer();

        http
                // This ensures the chain ONLY looks at OAuth2 Auth Server endpoints
                .securityMatcher(authServerConfigurer.getEndpointsMatcher())
                .cors(AbstractHttpConfigurer::disable)
                .with(authServerConfigurer, (authorizationServer) ->
                        authorizationServer.oidc(Customizer.withDefaults())
                )
                // Add this to ensure that even within this chain, we only care about its specific needs
                .authorizeHttpRequests(auth -> auth.anyRequest().authenticated())
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                // If it's not HTML, don't redirect to /login
                                (request, response, authException) -> response.sendError(HttpServletResponse.SC_UNAUTHORIZED),
                                new MediaTypeRequestMatcher(MediaType.APPLICATION_JSON)
                        )
                        .authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login"))
                );

        return http.build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain appSecurityFilterChain(HttpSecurity http,ClientRegistrationRepository clientRegistrationRepository) throws Exception {
        http
                .securityMatcher("/api/**")
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**","/api/v1/auth/logout","/api/v1/test/send-mail").permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(exceptions -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                (request, response, authException) -> {
                                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                                    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                                    String json = "{\"error\": {\"code\": \"401\", \"description\": \"Unauthorized access\"}}";
                                    response.getWriter().write(json);
                                },
                                // This Lambda replaces AntPathRequestMatcher
                                request -> request.getServletPath().startsWith("/api/")
                        )
                )
                .oauth2ResourceServer(oauth -> oauth
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtToken -> {
                            UserDetails details = userDetailsService.loadUserByUsername(jwtToken.getSubject());
                            return new UsernamePasswordAuthenticationToken(details, jwtToken, details.getAuthorities());
                        }))
                        .bearerTokenResolver(request -> {
                            // Extract JWT from HttpOnly Cookie
                            if (request.getCookies() == null) return null;
                            return Arrays.stream(request.getCookies())
                                    .filter(c -> "access_token".equals(c.getName()))
                                    .map(Cookie::getValue).findFirst().orElse(null);
                        })
                );
        return http.build();
    }

    @Bean
    @Order(3)
    public SecurityFilterChain uiSecurityFilterChain(
            HttpSecurity http,
            ClientRegistrationRepository clientRegistrationRepository) throws Exception {

        http
                .cors(AbstractHttpConfigurer::disable)
                .securityMatcher("/", "/login/**", "/oauth2/**", "/error", "/favicon.ico")
                .authorizeHttpRequests(auth -> auth
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .authorizationEndpoint(auth -> auth
                                .authorizationRequestResolver(
                                        authorizationRequestResolver(clientRegistrationRepository))
                        )
                        .userInfoEndpoint(ui -> ui
                                .userService(customOAuth2UserService)
                                .oidcUserService(customOidcUserService())
                        )
                        .successHandler((req, res, auth) -> {
                            tokenService.setTokensAsCookies(auth, res);
                            res.sendRedirect(sendRedirect);
                        })
                );

        return http.build();
    }


    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId(clientId)
                // Use {noop} for plain text secret or remove if using encrypted secrets
                .clientSecret("{noop}" + clientSecret)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_BASIC)
                .clientAuthenticationMethod(ClientAuthenticationMethod.CLIENT_SECRET_POST)
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)
                .redirectUri(redirectUri)
                .postLogoutRedirectUri(postLogoutRedirectUri)
                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope("email")
                .clientSettings(ClientSettings.builder().requireAuthorizationConsent(true).build())
                .build();

        return new InMemoryRegisteredClientRepository(oidcClient);
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
//                .issuer("http://localhost:8080")
                .issuer(issuer_uri)
                .build();
    }

    private OAuth2UserService<OidcUserRequest, OidcUser> customOidcUserService() {
        return userRequest -> {
            // Now this returns a CustomUserDetails which IS an OidcUser
            return (OidcUser) customOAuth2UserService.loadUser(userRequest);
        };
    }
}