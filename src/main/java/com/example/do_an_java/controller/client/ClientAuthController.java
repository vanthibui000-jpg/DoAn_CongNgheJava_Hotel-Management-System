package com.example.do_an_java.controller.client;

import com.example.do_an_java.entity.KhachHang;
import com.example.do_an_java.repository.KhachHangRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClientAuthController {
    private final KhachHangRepository khachHangRepository;

    public ClientAuthController(KhachHangRepository khachHangRepository) {
        this.khachHangRepository = khachHangRepository;
    }

    @GetMapping("/dang-nhap")
    public String loginPage() {
        return "client/login";
    }

    @PostMapping("/dang-nhap")
    public String login(@RequestParam String email,
                        @RequestParam String matKhau,
                        HttpSession session,
                        Model model) {
        email = email == null ? "" : email.trim();
        String loginEmail = email;
        return khachHangRepository.findByEmailIgnoreCaseAndMatKhau(loginEmail, matKhau)
                .map(khachHang -> {
                    if (khachHang.getTrangThai() != null && !"HOAT_DONG".equals(khachHang.getTrangThai())) {
                        model.addAttribute("error", "Tài khoản đang bị khóa hoặc ngưng hoạt động.");
                        model.addAttribute("email", loginEmail);
                        return "client/login";
                    }
                    session.setAttribute(ClientSession.CUSTOMER_SESSION_KEY, khachHang);
                    return "redirect:/tai-khoan";
                })
                .orElseGet(() -> {
                    model.addAttribute("error", "Email hoặc mật khẩu không đúng.");
                    model.addAttribute("email", loginEmail);
                    return "client/login";
                });
    }

    @GetMapping("/dang-ky")
    public String registerPage() {
        return "client/register";
    }

    @PostMapping("/dang-ky")
    public String register(@RequestParam String hoTen,
                           @RequestParam String email,
                           @RequestParam String dienThoai,
                           @RequestParam String matKhau,
                           @RequestParam(required = false) String diaChi,
                           HttpSession session,
                           Model model) {
        email = email == null ? "" : email.trim();
        addRegisterValues(model, hoTen, email, dienThoai, diaChi);

        if (khachHangRepository.findByEmailIgnoreCase(email).isPresent()) {
            model.addAttribute("error", "Email này đã có tài khoản, vui lòng đăng nhập hoặc dùng email khác.");
            return "client/register";
        }
        if (matKhau == null || matKhau.length() < 3) {
            model.addAttribute("error", "Mật khẩu cần ít nhất 3 ký tự.");
            return "client/register";
        }

        KhachHang khachHang = new KhachHang();
        khachHang.setMaKhachHang(khachHangRepository.findMaxMaKhachHang() + 1);
        khachHang.setHoTen(hoTen);
        khachHang.setEmail(email);
        khachHang.setDienThoai(dienThoai);
        khachHang.setDiaChi(diaChi);
        khachHang.setMatKhau(matKhau);
        khachHang.setTrangThai("HOAT_DONG");
        khachHangRepository.save(khachHang);
        session.setAttribute(ClientSession.CUSTOMER_SESSION_KEY, khachHang);
        return "redirect:/tai-khoan";
    }

    @GetMapping("/dang-xuat")
    public String logout(HttpSession session) {
        session.removeAttribute(ClientSession.CUSTOMER_SESSION_KEY);
        return "redirect:/";
    }

    private void addRegisterValues(Model model, String hoTen, String email, String dienThoai, String diaChi) {
        model.addAttribute("hoTen", hoTen);
        model.addAttribute("email", email);
        model.addAttribute("dienThoai", dienThoai);
        model.addAttribute("diaChi", diaChi);
    }
}
