package com.example.do_an_java.controller.client;

import com.example.do_an_java.repository.DichVuRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ClientServiceController {
    private final DichVuRepository dichVuRepository;

    public ClientServiceController(DichVuRepository dichVuRepository) {
        this.dichVuRepository = dichVuRepository;
    }

    @GetMapping("/dich-vu")
    public String services(@RequestParam(defaultValue = "") String keyword, Model model) {
        model.addAttribute("keyword", keyword);
        model.addAttribute("dichVus", keyword == null || keyword.isBlank()
                ? dichVuRepository.findAll()
                : dichVuRepository.findByTenDichVuContainingIgnoreCase(keyword));
        return "client/services";
    }
}
