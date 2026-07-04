package com.example.do_an_java.controller;

import com.example.do_an_java.entity.ChucVu;
import com.example.do_an_java.entity.NhanVien;
import com.example.do_an_java.repository.ChucVuRepository;
import com.example.do_an_java.repository.NhanVienRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/nhan-vien")
public class NhanVienController {

    private final NhanVienRepository nhanVienRepository;
    private final ChucVuRepository chucVuRepository;

    public NhanVienController(NhanVienRepository nhanVienRepository, ChucVuRepository chucVuRepository) {
        this.nhanVienRepository = nhanVienRepository;
        this.chucVuRepository = chucVuRepository;
    }

    @GetMapping
    public String list(
            @RequestParam(defaultValue = "") String keyword,
            Model model
    ) {

        model.addAttribute("keyword", keyword);

        if (keyword == null || keyword.isBlank()) {
            model.addAttribute("items", nhanVienRepository.findAll());
        } else {
            model.addAttribute("items",
                    nhanVienRepository.search(keyword.trim()));
        }

        return "nhan-vien/list";
    }

    @GetMapping("/them")
    public String add(Model model) {
        NhanVien nhanVien = new NhanVien();
        nhanVien.setChucVu(new ChucVu());
        model.addAttribute("item", nhanVien);
        addFormData(model);
        return "nhan-vien/form";
    }

    @GetMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        NhanVien nhanVien = nhanVienRepository.findById(id).orElseThrow();
        if (nhanVien.getChucVu() == null) {
            nhanVien.setChucVu(new ChucVu());
        }
        model.addAttribute("item", nhanVien);
        addFormData(model);
        return "nhan-vien/form";
    }

    @PostMapping("/luu")
    public String save(@ModelAttribute("item") NhanVien item, Model model) {
        item.setUsername(item.getUsername() == null ? null : item.getUsername().trim());
        if (item.getUsername() == null || item.getUsername().isBlank()) {
            return showFormError(model, item, "Vui lòng nhập tài khoản nhân viên.");
        }
        if (isUsernameUsedByAnotherEmployee(item)) {
            return showFormError(model, item, "Tài khoản này đã tồn tại, vui lòng nhập tài khoản khác.");
        }

        Integer maChucVu = item.getChucVu() == null ? null : item.getChucVu().getMaChucVu();
        if (maChucVu != null) {
            ChucVu chucVu = chucVuRepository.findById(maChucVu).orElse(null);
            item.setChucVu(chucVu);
        } else {
            item.setChucVu(null);
        }
        nhanVienRepository.save(item);
        return "redirect:/admin/nhan-vien";
    }

    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        nhanVienRepository.deleteById(id);
        return "redirect:/admin/nhan-vien";
    }

    private boolean isUsernameUsedByAnotherEmployee(NhanVien item) {
        return nhanVienRepository.findByUsernameIgnoreCase(item.getUsername())
                .filter(existing -> item.getMaNhanVien() == null
                        || !existing.getMaNhanVien().equals(item.getMaNhanVien()))
                .isPresent();
    }

    private String showFormError(Model model, NhanVien item, String error) {
        model.addAttribute("error", error);
        model.addAttribute("item", item);
        addFormData(model);
        return "nhan-vien/form";
    }

    private void addFormData(Model model) {
        model.addAttribute("chucVus", chucVuRepository.findAll());
    }
}
