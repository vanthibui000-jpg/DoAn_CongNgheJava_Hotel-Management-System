package com.example.do_an_java.controller;

import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.KhachHang;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.KhachHangRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping({"/admin/khach-hang", "/khach-hang"})
public class KhachHangController {
    private final KhachHangRepository khachHangRepository;
    private final CtDatPhongRepository ctDatPhongRepository;

    public KhachHangController(KhachHangRepository khachHangRepository,
                               CtDatPhongRepository ctDatPhongRepository) {
        this.khachHangRepository = khachHangRepository;
        this.ctDatPhongRepository = ctDatPhongRepository;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "ten") String searchType,
                       Model model) {
        String key = keyword == null ? "" : keyword.trim();
        List<KhachHang> items;
        if (key.isBlank()) {
            items = khachHangRepository.findAll();
        } else {
            items = switch (searchType) {
                case "cmnd" -> khachHangRepository.findByCmndContainingIgnoreCase(key);
                case "sdt" -> khachHangRepository.findByDienThoaiContainingIgnoreCase(key);
                default -> khachHangRepository.findByHoTenContainingIgnoreCase(key);
            };
        }

        model.addAttribute("keyword", key);
        model.addAttribute("searchType", searchType);
        model.addAttribute("items", items);
        return "khach-hang/list";
    }

    @GetMapping("/lich-su/{id}")
    public String history(@PathVariable Integer id, Model model) {
        KhachHang khachHang = khachHangRepository.findById(id).orElseThrow();
        List<CtDatPhong> lichSu = ctDatPhongRepository.findByKhachHang_MaKhachHangOrderByNgayThucHienDesc(id);
        model.addAttribute("khachHang", khachHang);
        model.addAttribute("lichSu", lichSu);
        return "khach-hang/lich-su";
    }

    @GetMapping("/them")
    public String add(Model model) {
        KhachHang khachHang = new KhachHang();
        khachHang.setMaKhachHang(khachHangRepository.findMaxMaKhachHang() + 1);
        khachHang.setTrangThai("HOAT_DONG");
        model.addAttribute("item", khachHang);
        model.addAttribute("isEdit", false);
        return "khach-hang/form";
    }

    @GetMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("item", khachHangRepository.findById(id).orElseThrow());
        model.addAttribute("isEdit", true);
        return "khach-hang/form";
    }

    @PostMapping("/luu")
    public String save(@ModelAttribute("item") KhachHang item,
                       @RequestParam(defaultValue = "false") boolean isEdit,
                       Model model) {
        if (item.getMaKhachHang() == null) {
            item.setMaKhachHang(khachHangRepository.findMaxMaKhachHang() + 1);
        }
        if (!isEdit && khachHangRepository.existsById(item.getMaKhachHang())) {
            return showFormError(model, item, false, "Mã khách hàng này đã tồn tại, vui lòng nhập mã khác.");
        }

        item.setEmail(item.getEmail() == null ? null : item.getEmail().trim());
        if (item.getEmail() != null && !item.getEmail().isBlank() && isEmailUsedByAnotherCustomer(item)) {
            return showFormError(model, item, isEdit, "Email này đã có tài khoản, vui lòng nhập email khác.");
        }

        if (item.getTrangThai() == null || item.getTrangThai().isBlank()) {
            item.setTrangThai("HOAT_DONG");
        }
        if (item.getMatKhau() == null || item.getMatKhau().isBlank()) {
            item.setMatKhau("1");
        }
        khachHangRepository.save(item);
        return "redirect:/admin/khach-hang";
    }

    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        khachHangRepository.deleteById(id);
        return "redirect:/admin/khach-hang";
    }

    private boolean isEmailUsedByAnotherCustomer(KhachHang item) {
        return khachHangRepository.findByEmailIgnoreCase(item.getEmail())
                .filter(existing -> item.getMaKhachHang() == null
                        || !existing.getMaKhachHang().equals(item.getMaKhachHang()))
                .isPresent();
    }

    private String showFormError(Model model, KhachHang item, boolean isEdit, String error) {
        model.addAttribute("error", error);
        model.addAttribute("item", item);
        model.addAttribute("isEdit", isEdit);
        return "khach-hang/form";
    }
}
