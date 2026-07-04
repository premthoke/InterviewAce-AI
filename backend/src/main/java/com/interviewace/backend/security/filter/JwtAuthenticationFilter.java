package com.interviewace.backend.security.filter;

import com.interviewace.backend.security.jwt.JwtTokenService;
import com.interviewace.backend.security.util.JwtConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Intercepts every HTTP request to extract and validate a JWT from the Authorization header.
 *
 * Flow:
 *   1. Read the Authorization header
 *   2. If missing or not prefixed with "Bearer ", skip — let the request continue unauthenticated
 *   3. Extract the JWT and parse the username (email)
 *   4. If the SecurityContext already contains an Authentication, skip — already authenticated
 *   5. Load the UserDetails from the database via UserDetailsService
 *   6. Validate the JWT against the loaded UserDetails
 *   7. On success, create a UsernamePasswordAuthenticationToken and store it in the SecurityContext
 *   8. Continue the filter chain
 *
 * This filter is registered before UsernamePasswordAuthenticationFilter in SecurityConfig.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenService jwtTokenService;
    private final UserDetailsService userDetailsService;

    public JwtAuthenticationFilter(JwtTokenService jwtTokenService,
                                   UserDetailsService userDetailsService) {
        this.jwtTokenService = jwtTokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // 1. Read Authorization header
        final String authHeader = request.getHeader(JwtConstants.HEADER);

        // 2. Skip if no Bearer token present
        if (authHeader == null || !authHeader.startsWith(JwtConstants.TOKEN_PREFIX)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract JWT (strip "Bearer " prefix)
        final String jwt = authHeader.substring(JwtConstants.TOKEN_PREFIX.length());

        try {
            // 4. Extract username from JWT
            final String username = jwtTokenService.extractUsername(jwt);

            // 5. Skip if SecurityContext already has authentication
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Load UserDetails from database
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                // 7. Validate JWT against UserDetails
                if (jwtTokenService.isTokenValid(jwt, userDetails)) {

                    // 8. Create authentication token
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    // Attach request details (remote address, session ID)
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 9. Store authentication in SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);

                    log.debug("JWT authentication successful for user: {}", username);
                }
            }
        } catch (Exception ex) {
            // Invalid/expired/malformed token — do not authenticate, let Spring Security
            // handle the unauthenticated request (returns 401 via the entry point)
            log.debug("JWT authentication failed: {}", ex.getMessage());
        }

        // 10. Continue filter chain
        filterChain.doFilter(request, response);
    }

}
