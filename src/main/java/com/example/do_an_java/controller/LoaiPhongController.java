package com.example.do_an_java.controller;

import com.example.do_an_java.entity.LoaiPhong;
import com.example.do_an_java.repository.LoaiPhongRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/loai-phong")
public class LoaiPhongController {
    private final LoaiPhongRepository loaiPhongRepository;

    public LoaiPhongController(LoaiPhongRepository loaiPhongRepository) {
        this.loaiPhongRepository = loaiPhongRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", loaiPhongRepository.findAll());
        return "loai-phong/list";
    }

    @GetMapping("/them")
    public String add(Model model) {
        LoaiPhong item = new LoaiPhong();
        item.setDaXoa(false);
        model.addAttribute("item", item);
        return "loai-phong/form";
    }

    @GetMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("item", loaiPhongRepository.findById(id).orElseThrow());
        return "loai-phong/form";
    }

    @PostMapping("/luu")
    public String save(@ModelAttribute("item") LoaiPhong item) {
        if (item.getDaXoa() == null) {
            item.setDaXoa(false);
        }
        loaiPhongRepository.save(item);
        return "redirect:/admin/loai-phong";
    }

    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        loaiPhongRepository.findById(id).ifPresent(item -> {
            item.setDaXoa(true);
            loaiPhongRepository.save(item);
        });
        return "redirect:/admin/loai-phong";
    }

    @GetMapping("/khoi-phuc/{id}")
    public String restore(@PathVariable Integer id) {
        loaiPhongRepository.findById(id).ifPresent(item -> {
            item.setDaXoa(false);
            loaiPhongRepository.save(item);
        });
        return "redirect:/admin/loai-phong";
    }
}
