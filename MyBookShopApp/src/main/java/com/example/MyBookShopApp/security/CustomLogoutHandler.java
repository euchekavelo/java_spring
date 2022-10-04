package com.example.MyBookShopApp.security;

import com.example.MyBookShopApp.service.JWTBlackListService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Component
public class CustomLogoutHandler implements LogoutSuccessHandler {

    @Autowired
    private JWTBlackListService jwtBlackListService;

    @Override
    public void onLogoutSuccess(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse,
                                Authentication authentication) throws IOException {
        HttpSession session = httpServletRequest.getSession();
        SecurityContextHolder.clearContext();
        if (session != null) {
            session.invalidate();
        }

        Cookie[] cookies = httpServletRequest.getCookies();
        if (cookies != null) {
            for (Cookie cookie : httpServletRequest.getCookies()) {
                if (cookie.getName().equals("token")) {
                    String token = cookie.getValue();
                    jwtBlackListService.saveToken(token);
                }

                cookie.setMaxAge(0);
            }
        }

        httpServletResponse.sendRedirect("/signin");
    }
}
