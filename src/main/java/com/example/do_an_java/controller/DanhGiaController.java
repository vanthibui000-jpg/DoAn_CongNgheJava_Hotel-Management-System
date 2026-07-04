package com.example.do_an_java.controller;

import com.example.do_an_java.entity.CtDanhGia;
import com.example.do_an_java.entity.Phong;
import com.example.do_an_java.repository.CtDanhGiaRepository;
import com.example.do_an_java.repository.PhongRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/admin/danh-gia")
public class DanhGiaController {
    private final CtDanhGiaRepository ctDanhGiaRepository;
    private final PhongRepository phongRepository;

    public DanhGiaController(CtDanhGiaRepository ctDanhGiaRepository,
                             PhongRepository phongRepository) {
        this.ctDanhGiaRepository = ctDanhGiaRepository;
        this.phongRepository = phongRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", ctDanhGiaRepository.findAll());
        return "danh-gia/list";
    }

    @GetMapping("/them")
    public String add(Model model) {
        CtDanhGia item = new CtDanhGia();
        item.setPhong(new Phong());
        item.setNgayDanhGia(LocalDate.now());
        item.setSoSao(5);
        model.addAttribute("item", item);
        model.addAttribute("phongs", phongRepository.findAll());
        return "danh-gia/form";
    }

    @GetMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        CtDanhGia item = ctDanhGiaRepository.findById(id).orElseThrow();
        ensureNestedModel(item);
        model.addAttribute("item", item);
        model.addAttribute("phongs", phongRepository.findAll());
        return "danh-gia/form";
    }

    @PostMapping("/luu")
    public String save(@ModelAttribute("item") CtDanhGia item, Model model) {
        if (item.getNgayDanhGia() == null) {
            item.setNgayDanhGia(LocalDate.now());
        }
        if (item.getSoSao() == null || item.getSoSao() < 1 || item.getSoSao() > 5) {
            item.setSoSao(5);
        }
        if (item.getNoiDung() != null && item.getNoiDung().length() > 255) {
            item.setNoiDung(item.getNoiDung().substring(0, 255));
        }
        if (item.getPhong() != null && item.getPhong().getMaPhong() != null) {
            item.setPhong(phongRepository.findById(item.getPhong().getMaPhong()).orElse(null));
        }
        if (item.getPhong() == null) {
            model.addAttribute("error", "Vui lòng chọn phòng cần đánh giá.");
            ensureNestedModel(item);
            model.addAttribute("item", item);
            model.addAttribute("phongs", phongRepository.findAll());
            return "danh-gia/form";
        }
        ctDanhGiaRepository.save(item);
        return "redirect:/admin/danh-gia";
    }

    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        ctDanhGiaRepository.deleteById(id);
        return "redirect:/admin/danh-gia";
    }

    private void ensureNestedModel(CtDanhGia item) {
        if (item.getPhong() == null) {
            item.setPhong(new Phong());
        }
    }
}
