package qble2.cookbook.security;

import java.util.Arrays;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import lombok.RequiredArgsConstructor;
import qble2.cookbook.role.model.RoleEnum;
import qble2.cookbook.user.UserService;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
// @Profile(value = {"development", "production"})
@Profile("!test")
public class SecurityConfiguration {

  @Autowired
  private UserService userService;

  @Autowired
  private JwtUtils jwtUtils;

  @Bean
  SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    AuthenticationConfiguration authenticationConfiguration =
        http.getSharedObject(AuthenticationConfiguration.class);

    JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(this.userService,
        this.jwtUtils, authenticationManager(authenticationConfiguration));
    jwtAuthenticationFilter.setFilterProcessesUrl("/api/auth/login"); // default is /login

    http
        // disable Cross-Site Request Forgery as we do not serve browser clients
        .csrf().disable()

        // disable Cross-Origin resource sharing
        .cors().configurationSource(request -> {
          CorsConfiguration configuration = new CorsConfiguration();
          configuration
              .setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://localhost:4201"));
          configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "PATCH"));
          configuration.setAllowedHeaders(List.of("*"));
          return configuration;
        })

        // make sure we use stateless session, session will not be used to store user's state
        .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)

        // allow access restriction using request matcher
        .and().authorizeRequests()

        // define rules
        // vvv HERE vvv

        .antMatchers("/h2-console/**", "/favicon.ico", "/error", "/api/auth/**").permitAll()

        // needed to allow HAL Browser
        .antMatchers(HttpMethod.GET, "/**").permitAll()
        // .antMatchers(HttpMethod.GET, "/explorer/**")
        // .permitAll()

        .antMatchers(HttpMethod.GET, "/api/metadata/**", "/api/recipes/**", "/api/ingredients/**",
            "/api/reviews/**")
        .permitAll()
        // .antMatchers(HttpMethod.GET, "/api/**")
        // .permitAll()

        .antMatchers("/api/roles/**").hasAnyAuthority(RoleEnum.ROLE_ADMIN.toString())

        .antMatchers(HttpMethod.GET, "/api/users/**")
        .hasAnyAuthority(RoleEnum.ROLE_ADMIN.toString(), RoleEnum.ROLE_USER.toString())

        .antMatchers(HttpMethod.PUT, "/api/users/**")
        .hasAnyAuthority(RoleEnum.ROLE_ADMIN.toString(), RoleEnum.ROLE_USER.toString())

        .antMatchers(HttpMethod.DELETE, "/api/users/**")
        .hasAnyAuthority(RoleEnum.ROLE_ADMIN.toString())

        .antMatchers(HttpMethod.POST, "/api/recipes/search/**").permitAll()

        .antMatchers(HttpMethod.POST, "/api/recipes/**", "/api/reviews/**")
        .hasAnyAuthority(RoleEnum.ROLE_ADMIN.toString(), RoleEnum.ROLE_USER.toString())

        .antMatchers(HttpMethod.DELETE, "/api/recipes/**", "/api/reviews/**")
        .hasAnyAuthority(RoleEnum.ROLE_ADMIN.toString(), RoleEnum.ROLE_USER.toString())

        // .antMatchers(HttpMethod.GET, "/api/**")
        // .denyAll()

        // ^^^ HERE ^^^

        //
        .anyRequest()
        // .permitAll() // allow all other requests
        .authenticated() // require auth for all other requests

        // add customer authorization filter (JWT filter)
        .and().addFilter(jwtAuthenticationFilter)

        // will be processed before
        .addFilterBefore(new JwtAuthorizationFilter(this.jwtUtils, this.userService),
            UsernamePasswordAuthenticationFilter.class)

        // handlers
        .exceptionHandling().authenticationEntryPoint(authenticationExceptionHandler())
    // .authenticationEntryPoint(new BearerTokenAuthenticationEntryPoint()) // Spring Security Oauth
    // 2
    ;

    return http.build();
  }

  /**
   * XXX BKE There is no need to explicitly set the userDetailsService and passwordEncoder on the
   * AuthenticationManager. When userDetailsService and passwordEncoder are registered as beans, the
   * default AuthenticationManager created by Spring Security will pick them up.
   *
   * You can define custom authentication by exposing a custom UserDetailsService as a bean, simply
   * register a Bean that implements UserDetailsService interface
   */
  // register a Global AuthenticationManager
  @Bean
  AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration) throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }

  @Bean
  AuthenticationEntryPoint authenticationExceptionHandler() {
    return new AuthenticationExceptionHandler();
  }

}
