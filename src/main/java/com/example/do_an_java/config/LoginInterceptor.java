package com.example.do_an_java.config;

import com.example.do_an_java.entity.NhanVien;
import com.example.do_an_java.service.RolePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    private final RolePermissionService rolePermissionService;

    public LoginInterceptor(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) throws Exception {

        String uri = request.getRequestURI();

        if (uri.equals("/login")
                || uri.equals("/logout")
                || uri.startsWith("/css/")
                || uri.startsWith("/js/")
                || uri.startsWith("/images/")
                || uri.startsWith("/Images/")
                || uri.startsWith("/webjars/")) {
            return true;
        }

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("loggedInUser") == null) {
            response.sendRedirect("/login");
            return false;
        }

        NhanVien user = (NhanVien) session.getAttribute("loggedInUser");
        if (user.getChucVu() == null) {
            response.sendRedirect("/login");
            return false;
        }

        String path = normalizeAdminPath(uri);

        if (rolePermissionService.canAccess(user, path)) {
            return true;
        }

        response.sendRedirect("/admin");
        return false;
    }

    private String normalizeAdminPath(String uri) {
        if (uri.equals("/admin") || uri.equals("/admin/")) {
            return "/dashboard";
        }
        if (uri.startsWith("/admin/")) {
            return uri.substring("/admin".length());
        }
        return uri;
    }
}
