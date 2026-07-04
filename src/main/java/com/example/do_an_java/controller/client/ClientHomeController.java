package com.example.do_an_java.controller.client;

import com.example.do_an_java.repository.DichVuRepository;
import com.example.do_an_java.repository.PhongRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ClientHomeController {
    private final PhongRepository phongRepository;
    private final DichVuRepository dichVuRepository;

    public ClientHomeController(PhongRepository phongRepository, DichVuRepository dichVuRepository) {
        this.phongRepository = phongRepository;
        this.dichVuRepository = dichVuRepository;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("phongs", phongRepository.findClientVisibleRooms().stream().limit(6).toList());
        model.addAttribute("dichVus", dichVuRepository.findAll().stream().limit(4).toList());
        return "client/home";
    }

    @GetMapping("/lien-he")
    public String contact() {
        return "client/contact";
    }
}
