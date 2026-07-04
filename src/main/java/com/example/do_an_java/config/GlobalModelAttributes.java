package com.example.do_an_java.config;

import com.example.do_an_java.entity.NhanVien;
import com.example.do_an_java.service.RolePermissionService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
public class GlobalModelAttributes {
    private final RolePermissionService rolePermissionService;

    public GlobalModelAttributes(RolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @ModelAttribute("navPermissions")
    public List<String> navPermissions(HttpSession session) {
        Object user = session.getAttribute("loggedInUser");
        if (user instanceof NhanVien nhanVien) {
            return rolePermissionService.getPermissionCodes(nhanVien);
        }
        return List.of();
    }
}
