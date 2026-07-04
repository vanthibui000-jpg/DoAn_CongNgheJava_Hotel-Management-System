package com.example.do_an_java.controller;

import com.example.do_an_java.service.DashboardService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping({"/admin", "/admin/dashboard", "/dashboard"})
    public String dashboard(Model model) {
        model.addAttribute("stats", dashboardService.getStats());
        return "dashboard";
    }
}
