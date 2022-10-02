package com.example.MyBookShopApp.security.jwt;

import com.example.MyBookShopApp.security.BookstoreUserDetails;
import com.example.MyBookShopApp.security.service.BookstoreUserDetailsService;
import com.example.MyBookShopApp.dto.ContactConfirmationError;
import com.example.MyBookShopApp.service.JWTBlackListService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.logging.Logger;

@Component
public class JWTRequestFilter extends OncePerRequestFilter {

    private final BookstoreUserDetailsService bookstoreUserDetailsService;
    private final JWTBlackListService jwtBlackListService;
    private final JWTUtil jwtUtil;

    public JWTRequestFilter(BookstoreUserDetailsService bookstoreUserDetailsService,
                            JWTBlackListService jwtBlackListService, JWTUtil jwtUtil) {

        this.bookstoreUserDetailsService = bookstoreUserDetailsService;
        this.jwtBlackListService = jwtBlackListService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String token = null;
            String username = null;
            Cookie[] cookies = httpServletRequest.getCookies();

            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if (cookie.getName().equals("token")) {
                        token = cookie.getValue();
                        if (jwtBlackListService.tokenIsNotInTheTable(token))
                            username = jwtUtil.extractUsername(token);
                    }

                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        BookstoreUserDetails userDetails =
                                (BookstoreUserDetails) bookstoreUserDetailsService.loadUserByUsername(username);
                        if (jwtUtil.validateToken(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authenticationToken =
                                    new UsernamePasswordAuthenticationToken(
                                            userDetails, null, userDetails.getAuthorities());

                            authenticationToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));
                            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                        }
                    }
                }
            }

            filterChain.doFilter(httpServletRequest, httpServletResponse);
        } catch (JwtException|IllegalArgumentException ex) {
            returnAnErrorResponse(ex, httpServletResponse);
        }
    }

    private void returnAnErrorResponse(Exception ex, HttpServletResponse httpServletResponse) throws IOException {
        Logger.getLogger(this.getClass().getSimpleName()).severe(ex.getMessage());
        ContactConfirmationError contactConfirmationError = new ContactConfirmationError();
        contactConfirmationError.setResult(false);
        contactConfirmationError.setError(ex.getMessage());
        httpServletResponse.setStatus(401);
        httpServletResponse.setContentType(MimeTypeUtils.APPLICATION_JSON_VALUE);
        httpServletResponse.getWriter().write(new ObjectMapper().writeValueAsString(contactConfirmationError));
        httpServletResponse.getWriter().flush();
    }
}
