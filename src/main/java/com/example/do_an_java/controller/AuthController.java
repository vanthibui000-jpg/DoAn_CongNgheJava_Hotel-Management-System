package com.example.do_an_java.controller;

import com.example.do_an_java.entity.NhanVien;
import com.example.do_an_java.repository.NhanVienRepository;
import com.example.do_an_java.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {

    private final AuthService authService;
    private final NhanVienRepository nhanVienRepository;

    public AuthController(
            AuthService authService,
            NhanVienRepository nhanVienRepository) {

        this.authService = authService;
        this.nhanVienRepository = nhanVienRepository;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/login")
    public String login(
            @RequestParam String username,
            @RequestParam String password,
            HttpSession session,
            Model model
    ) {

        return authService.login(username, password)
                .map(user -> {

                    session.setAttribute(
                            "loggedInUser",
                            user);

                    return "redirect:/admin";
                })
                .orElseGet(() -> {

                    model.addAttribute(
                            "error",
                            "Sai tài khoản hoặc mật khẩu");

                    model.addAttribute(
                            "username",
                            username);

                    return "login";
                });
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {

        session.invalidate();

        return "redirect:/login";
    }

    // ==========================
    // CÂU 4 - ĐỔI MẬT KHẨU
    // ==========================

    @GetMapping("/doi-mat-khau")
    public String doiMatKhauPage(HttpSession session) {

        if (session.getAttribute("loggedInUser") == null) {
            return "redirect:/login";
        }

        return "doi-mat-khau";
    }

    @PostMapping("/doi-mat-khau")
    public String doiMatKhau(
            @RequestParam String matKhauCu,
            @RequestParam String matKhauMoi,
            @RequestParam String xacNhanMatKhau,
            HttpSession session,
            Model model
    ) {

        NhanVien user =
                (NhanVien) session.getAttribute(
                        "loggedInUser");

        if (user == null) {
            return "redirect:/login";
        }

        // kiểm tra mật khẩu cũ
        if (!user.getPassword().equals(matKhauCu)) {

            model.addAttribute(
                    "error",
                    "Mật khẩu cũ không đúng");

            return "doi-mat-khau";
        }

        // kiểm tra xác nhận mật khẩu
        if (!matKhauMoi.equals(xacNhanMatKhau)) {

            model.addAttribute(
                    "error",
                    "Xác nhận mật khẩu không khớp");

            return "doi-mat-khau";
        }

        // không cho nhập giống mật khẩu cũ
        if (matKhauMoi.equals(matKhauCu)) {

            model.addAttribute(
                    "error",
                    "Mật khẩu mới phải khác mật khẩu cũ");

            return "doi-mat-khau";
        }

        // cập nhật DB
        user.setPassword(matKhauMoi);

        nhanVienRepository.save(user);

        // cập nhật session
        session.setAttribute(
                "loggedInUser",
                user);

        model.addAttribute(
                "success",
                "Đổi mật khẩu thành công");

        return "doi-mat-khau";
    }
}