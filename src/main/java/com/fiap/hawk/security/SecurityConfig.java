package com.fiap.hawk.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fiap.hawk.exception.ErrorResponse;
import com.fiap.hawk.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;
import java.util.UUID;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	UserDetailsService userDetailsService(UserRepository userRepository) {
		return username -> userRepository.findByEmailIgnoreCase(username)
				.map(UserPrincipal::from)
				.orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
	}

	@Bean
	DaoAuthenticationProvider authenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
		return configuration.getAuthenticationManager();
	}

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtFilter) throws Exception {
		ObjectMapper objectMapper = new ObjectMapper();
		http
				.csrf(AbstractHttpConfigurer::disable)
				.cors(Customizer.withDefaults())
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(unauthorizedEntryPoint(objectMapper))
						.accessDeniedHandler(accessDeniedHandler(objectMapper)))
				.authorizeHttpRequests(auth -> auth
						.requestMatchers(
								"/api/v1/auth/login",
								"/api/v1/auth/refresh",
								"/swagger-ui/**",
								"/swagger-ui.html",
								"/v3/api-docs/**",
								"/actuator/health"
						).permitAll()
						.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
						.requestMatchers("/api/v1/dashboard/**").hasRole("LIDER")
						.requestMatchers(HttpMethod.POST, "/api/v1/orientacoes").hasRole("LIDER")
						.requestMatchers(HttpMethod.PUT, "/api/v1/orientacoes/**").hasRole("LIDER")
						.requestMatchers(HttpMethod.DELETE, "/api/v1/orientacoes/**").hasRole("LIDER")
						.requestMatchers(HttpMethod.GET, "/api/v1/orientacoes", "/api/v1/orientacoes/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/v1/ideias").hasRole("OPERADOR")
						.requestMatchers(HttpMethod.PATCH, "/api/v1/ideias/**").hasRole("GESTOR")
						.requestMatchers(HttpMethod.GET, "/api/v1/ideias", "/api/v1/ideias/**").authenticated()
						.requestMatchers(HttpMethod.POST, "/api/v1/projetos").hasRole("GESTOR")
						.requestMatchers(HttpMethod.PATCH, "/api/v1/projetos/**").hasRole("GESTOR")
						.requestMatchers(HttpMethod.GET, "/api/v1/projetos", "/api/v1/projetos/**").hasAnyRole("GESTOR", "LIDER")
						.requestMatchers(HttpMethod.GET, "/api/v1/users").hasAnyRole("GESTOR", "LIDER")
						.anyRequest().authenticated())
				.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration config = new CorsConfiguration();
		config.setAllowedOriginPatterns(List.of("*"));
		config.setAllowedMethods(List.of("*"));
		config.setAllowedHeaders(List.of("*"));
		config.setExposedHeaders(List.of("*"));
		config.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);
		return source;
	}

	private AuthenticationEntryPoint unauthorizedEntryPoint(ObjectMapper objectMapper) {
		return (request, response, authException) -> writeError(response, objectMapper, 401, "UNAUTHORIZED",
				"Token ausente ou expirado.");
	}

	private AccessDeniedHandler accessDeniedHandler(ObjectMapper objectMapper) {
		return (request, response, accessDeniedException) -> writeError(response, objectMapper, 403, "FORBIDDEN",
				"Você não tem permissão para esta operação.");
	}

	private void writeError(HttpServletResponse response, ObjectMapper objectMapper, int status, String code, String message)
			throws java.io.IOException {
		response.setStatus(status);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getOutputStream(),
				new ErrorResponse(code, message, null, UUID.randomUUID().toString().substring(0, 8)));
	}
}
