package com.example.do_an_java.controller.client;

import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.CtDichVu;
import com.example.do_an_java.entity.CtDichVuId;
import com.example.do_an_java.entity.DichVu;
import com.example.do_an_java.entity.KhachHang;
import com.example.do_an_java.entity.Phong;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.CtDichVuRepository;
import com.example.do_an_java.repository.DichVuRepository;
import com.example.do_an_java.repository.PhongRepository;
import com.example.do_an_java.service.BookingService;
import jakarta.servlet.http.HttpSession;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class ClientBookingController {
    private final PhongRepository phongRepository;
    private final CtDatPhongRepository ctDatPhongRepository;
    private final DichVuRepository dichVuRepository;
    private final CtDichVuRepository ctDichVuRepository;
    private final BookingService bookingService;

    public ClientBookingController(PhongRepository phongRepository,
                                   CtDatPhongRepository ctDatPhongRepository,
                                   DichVuRepository dichVuRepository,
                                   CtDichVuRepository ctDichVuRepository,
                                   BookingService bookingService) {
        this.phongRepository = phongRepository;
        this.ctDatPhongRepository = ctDatPhongRepository;
        this.dichVuRepository = dichVuRepository;
        this.ctDichVuRepository = ctDichVuRepository;
        this.bookingService = bookingService;
    }

    @GetMapping("/dat-phong")
    public String bookingPage(@RequestParam(required = false) Integer maPhong,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayNhan,
                              @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayTra,
                              Model model,
                              HttpSession session) {
        return showBookingPage(maPhong, ngayNhan, ngayTra, model, session);
    }

    private String showBookingPage(Integer maPhong,
                                   LocalDate ngayNhan,
                                   LocalDate ngayTra,
                                   Model model,
                                   HttpSession session) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        model.addAttribute("khachHang", khachHang);
        model.addAttribute("phongs", phongRepository.findClientVisibleRooms().stream()
                .filter(phong -> !"Bảo trì".equals(phong.getTrangThai()))
                .toList());
        if (maPhong != null) {
            model.addAttribute("phongChon", phongRepository.findById(maPhong).orElse(null));
        }
        model.addAttribute("lockRoomSelection", maPhong != null);
        model.addAttribute("dichVus", dichVuRepository.findAll());
        model.addAttribute("ngayNhan", ngayNhan);
        model.addAttribute("ngayTra", ngayTra);
        model.addAttribute("today", LocalDate.now());
        return "client/booking";
    }

    @PostMapping("/dat-phong")
    public String book(@RequestParam Integer maPhong,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayNhan,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayTra,
                       @RequestParam(required = false) List<Integer> dichVuIds,
                       Model model,
                       HttpSession session,
                       RedirectAttributes redirectAttributes) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        Phong phong = phongRepository.findById(maPhong).orElse(null);
        if (phong == null) {
            model.addAttribute("error", "Không tìm thấy phòng.");
            model.addAttribute("selectedDichVuIds", dichVuIds);
            return showBookingPage(maPhong, ngayNhan, ngayTra, model, session);
        }

        CtDatPhong datPhong = new CtDatPhong();
        datPhong.setMaCtDatPhong(ctDatPhongRepository.findMaxMaCtDatPhong() + 1);
        datPhong.setNgayThucHien(LocalDate.now());
        datPhong.setKhachHang(khachHang);
        datPhong.setPhong(phong);
        datPhong.setNgayNhan(ngayNhan);
        datPhong.setNgayTra(ngayTra);
        datPhong.setTrangThai("CHO_ADMIN_DUYET");
        datPhong.setPaymentStatus("CHUA_TAO");

        String error = bookingService.validate(datPhong, null, true);
        if (error != null) {
            model.addAttribute("error", error);
            model.addAttribute("selectedDichVuIds", dichVuIds);
            return showBookingPage(maPhong, ngayNhan, ngayTra, model, session);
        }

        ctDatPhongRepository.save(datPhong);
        saveSelectedServices(datPhong, dichVuIds);
        redirectAttributes.addFlashAttribute("message", "Đơn đặt phòng đã được gửi. Vui lòng chờ admin xác nhận.");
        return "redirect:/tai-khoan/lich-su-dat-phong";
    }

    private void saveSelectedServices(CtDatPhong datPhong, List<Integer> dichVuIds) {
        if (dichVuIds == null || dichVuIds.isEmpty()) {
            return;
        }

        int soDem = (int) Math.max(1, ChronoUnit.DAYS.between(datPhong.getNgayNhan(), datPhong.getNgayTra()));
        int nextMaCtDichVu = ctDichVuRepository.findMaxMaCtDichVu() + 1;

        for (Integer maDichVu : dichVuIds.stream().distinct().toList()) {
            DichVu dichVu = dichVuRepository.findById(maDichVu).orElse(null);
            if (dichVu == null) {
                continue;
            }
            CtDichVu item = new CtDichVu();
            item.setMaCtDichVu(nextMaCtDichVu++);
            item.setId(new CtDichVuId(maDichVu, datPhong.getMaCtDatPhong()));
            item.setDichVu(dichVu);
            item.setCtDatPhong(datPhong);
            item.setSoLuong(soDem);
            item.setTongTienDichVu((dichVu.getGiaDichVu() == null ? 0 : dichVu.getGiaDichVu()) * soDem);
            ctDichVuRepository.save(item);
        }
    }
}
