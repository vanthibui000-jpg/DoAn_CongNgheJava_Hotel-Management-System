package com.example.do_an_java.controller;

import com.example.do_an_java.entity.BangPhanCong;
import com.example.do_an_java.entity.NhanVien;
import com.example.do_an_java.repository.BangPhanCongRepository;
import com.example.do_an_java.repository.NhanVienRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/phan-cong")
public class PhanCongController {

    private final BangPhanCongRepository bangPhanCongRepository;
    private final NhanVienRepository nhanVienRepository;

    public PhanCongController(
            BangPhanCongRepository bangPhanCongRepository,
            NhanVienRepository nhanVienRepository) {

        this.bangPhanCongRepository = bangPhanCongRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    @GetMapping
    public String list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String ngay,
            Model model) {

        List<BangPhanCong> items;

        if (keyword != null && !keyword.isBlank()
                && ngay != null && !ngay.isBlank()) {

            items = bangPhanCongRepository
                    .findByLoaiCongViecContainingIgnoreCaseAndNgayPhanCong(
                            keyword,
                            LocalDate.parse(ngay)
                    );

        } else if (keyword != null && !keyword.isBlank()) {

            items = bangPhanCongRepository
                    .findByLoaiCongViecContainingIgnoreCase(keyword);

        } else if (ngay != null && !ngay.isBlank()) {

            items = bangPhanCongRepository
                    .findByNgayPhanCong(LocalDate.parse(ngay));

        } else {

            items = bangPhanCongRepository.findAll();
        }

        model.addAttribute("items", items);
        model.addAttribute("keyword", keyword);
        model.addAttribute("ngay", ngay);

        return "phan-cong/list";
    }

    @GetMapping("/them")
    public String add(Model model) {

        BangPhanCong item = new BangPhanCong();
        item.setNhanVien(new NhanVien());
        ensureNestedModel(item);
        model.addAttribute("item", item);
        model.addAttribute(
                "nhanViens",
                nhanVienRepository.findAll()
        );

        return "phan-cong/form";
    }

    @GetMapping("/sua/{id}")
    public String edit(
            @PathVariable Integer id,
            Model model) {

        BangPhanCong item =
                bangPhanCongRepository.findById(id)
                        .orElseThrow();

        ensureNestedModel(item);
        model.addAttribute("item", item);

        model.addAttribute(
                "nhanViens",
                nhanVienRepository.findAll()
        );

        return "phan-cong/form";
    }

    @PostMapping("/luu")
    public String save(
            @ModelAttribute("item") BangPhanCong item,
            Model model) {

        if (item.getNgayPhanCong() == null) {

            model.addAttribute(
                    "error",
                    "Ngày phân công không được để trống"
            );
            ensureNestedModel(item);
            model.addAttribute("item", item);

            model.addAttribute(
                    "nhanViens",
                    nhanVienRepository.findAll()
            );

            return "phan-cong/form";
        }

        if (item.getNgayPhanCong()
                .isBefore(LocalDate.now())) {

            model.addAttribute(
                    "error",
                    "Không được phân công ngày trong quá khứ"
            );
            ensureNestedModel(item);
            model.addAttribute("item", item);

            model.addAttribute(
                    "nhanViens",
                    nhanVienRepository.findAll()
            );

            return "phan-cong/form";
        }

        hydrateRelations(item);
        bangPhanCongRepository.save(item);

        return "redirect:/admin/phan-cong";
    }

    private void hydrateRelations(BangPhanCong item) {
        if (item.getNhanVien() != null && item.getNhanVien().getMaNhanVien() != null) {
            item.setNhanVien(nhanVienRepository.findById(item.getNhanVien().getMaNhanVien()).orElse(null));
        }
    }

    private void ensureNestedModel(BangPhanCong item) {
        if (item.getNhanVien() == null) {
            item.setNhanVien(new NhanVien());
        }
    }

    @GetMapping("/xoa/{id}")
    public String delete(
            @PathVariable Integer id) {

        bangPhanCongRepository.deleteById(id);

        return "redirect:/admin/phan-cong";
    }

    @GetMapping("/hom-nay")
    public String homNay(Model model) {

        model.addAttribute(
                "items",
                bangPhanCongRepository.findByNgayPhanCong(
                        LocalDate.now()
                )
        );

        return "phan-cong/list";
    }
}