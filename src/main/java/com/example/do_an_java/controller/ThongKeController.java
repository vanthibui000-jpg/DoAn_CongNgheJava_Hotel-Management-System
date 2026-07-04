package com.example.do_an_java.controller;

import com.example.do_an_java.repository.HoaDonRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
public class ThongKeController {

    private final HoaDonRepository hoaDonRepository;

    public ThongKeController(HoaDonRepository hoaDonRepository) {
        this.hoaDonRepository = hoaDonRepository;
    }

    @GetMapping("/admin/thong-ke")
    public String thongKe(
            @RequestParam(required = false) String ngay,
            @RequestParam(required = false) Integer thang,
            @RequestParam(required = false) Integer nam,
            Model model
    ) {

        Long doanhThuNgay = null;
        Long doanhThuThang = null;

        if (ngay != null && !ngay.isBlank()) {

            LocalDate d = LocalDate.parse(ngay);

            doanhThuNgay =
                    hoaDonRepository.tinhDoanhThuTheoNgay(
                            d.getDayOfMonth(),
                            d.getMonthValue(),
                            d.getYear()
                    );
        }

        if (thang != null && nam != null) {

            doanhThuThang =
                    hoaDonRepository.tinhDoanhThuTheoThang(
                            thang,
                            nam
                    );
        }

        model.addAttribute("doanhThuNgay", doanhThuNgay);
        model.addAttribute("doanhThuThang", doanhThuThang);
        model.addAttribute("selectedNgay", ngay);
        model.addAttribute("selectedThang", thang == null ? LocalDate.now().getMonthValue() : thang);
        model.addAttribute("selectedNam", nam == null ? LocalDate.now().getYear() : nam);

        return "thong-ke";
    }
}
