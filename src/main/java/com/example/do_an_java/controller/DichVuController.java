package com.example.do_an_java.controller;

import com.example.do_an_java.entity.DichVu;
import com.example.do_an_java.repository.DichVuRepository;
import com.example.do_an_java.service.ImageStorageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/admin/dich-vu")
public class DichVuController {
    private final DichVuRepository dichVuRepository;
    private final ImageStorageService imageStorageService;

    public DichVuController(DichVuRepository dichVuRepository,
                            ImageStorageService imageStorageService) {
        this.dichVuRepository = dichVuRepository;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("items", keyword.isBlank()
                ? dichVuRepository.findAll()
                : dichVuRepository.findByTenDichVuContainingIgnoreCase(keyword));
        return "dich-vu/list";
    }

    @GetMapping("/them")
    public String add(Model model) {
        model.addAttribute("item", new DichVu());
        return "dich-vu/form";
    }

    @GetMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("item", dichVuRepository.findById(id).orElseThrow());
        return "dich-vu/form";
    }

    @PostMapping("/luu")
    public String save(@ModelAttribute("item") DichVu item,
                       @RequestParam(value = "hinhAnhFile", required = false) MultipartFile hinhAnhFile,
                       Model model) {
        try {
            imageStorageService.validateImageFile(hinhAnhFile);
            DichVu saved = dichVuRepository.save(item);
            String imagePath = imageStorageService.storeServiceImage(hinhAnhFile, saved.getMaDichVu());
            if (imagePath != null) {
                saved.setHinhAnh(imagePath);
                dichVuRepository.save(saved);
            }
        } catch (IOException | IllegalArgumentException ex) {
            model.addAttribute("item", item);
            model.addAttribute("error", ex.getMessage());
            return "dich-vu/form";
        }
        return "redirect:/admin/dich-vu";
    }

    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        dichVuRepository.deleteById(id);
        return "redirect:/admin/dich-vu";
    }
}
