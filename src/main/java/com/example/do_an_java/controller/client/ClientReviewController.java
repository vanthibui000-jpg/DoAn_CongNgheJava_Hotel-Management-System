package com.example.do_an_java.controller.client;

import com.example.do_an_java.entity.CtDanhGia;
import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.KhachHang;
import com.example.do_an_java.entity.Phong;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.CtDanhGiaRepository;
import com.example.do_an_java.repository.PhongRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ClientReviewController {
    private final CtDanhGiaRepository ctDanhGiaRepository;
    private final CtDatPhongRepository ctDatPhongRepository;
    private final PhongRepository phongRepository;

    public ClientReviewController(CtDanhGiaRepository ctDanhGiaRepository,
                                  CtDatPhongRepository ctDatPhongRepository,
                                  PhongRepository phongRepository) {
        this.ctDanhGiaRepository = ctDanhGiaRepository;
        this.ctDatPhongRepository = ctDatPhongRepository;
        this.phongRepository = phongRepository;
    }

    @GetMapping("/danh-gia")
    public String reviews(Model model, HttpSession session) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        model.addAttribute("items", loadReviewsSafe());
        model.addAttribute("phongs", loadReviewableRoomsSafe(khachHang));
        return "client/reviews";
    }

    @PostMapping("/danh-gia")
    public String submitReview(@RequestParam Integer maPhong,
                               @RequestParam String noiDung,
                               @RequestParam(defaultValue = "5") Integer soSao,
                               HttpSession session,
                               Model model) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        Phong phong = phongRepository.findById(maPhong).orElse(null);
        if (phong == null) {
            model.addAttribute("error", "Không tìm thấy phòng cần đánh giá.");
            return reviews(model, session);
        }

        String noiDungDaNhap = noiDung == null ? "" : noiDung.trim();
        if (noiDungDaNhap.isBlank()) {
            model.addAttribute("error", "Vui lòng nhập nội dung đánh giá.");
            return reviews(model, session);
        }
        if (soSao == null || soSao < 1 || soSao > 5) {
            model.addAttribute("error", "Số sao phải nằm trong khoảng 1 đến 5.");
            return reviews(model, session);
        }
        if (noiDungDaNhap.length() > 255) {
            model.addAttribute("error", "Nội dung đánh giá quá dài, vui lòng rút gọn dưới 255 ký tự.");
            return reviews(model, session);
        }

        List<CtDatPhong> bookings = loadCompletedBookingsForReviewSafe(khachHang, maPhong);
        if (bookings.isEmpty()) {
            model.addAttribute("error", "Bạn chỉ được đánh giá phòng đã trả phòng hoặc đã thanh toán.");
            return reviews(model, session);
        }

        CtDanhGia danhGia = new CtDanhGia();
        danhGia.setPhong(phong);
        danhGia.setKhachHang(khachHang);
        danhGia.setCtDatPhong(bookings.get(0));
        danhGia.setSoSao(soSao);
        danhGia.setNoiDung(noiDungDaNhap);
        danhGia.setNgayDanhGia(LocalDate.now());
        ctDanhGiaRepository.save(danhGia);
        return "redirect:/danh-gia";
    }


    private List<CtDatPhong> loadCompletedBookingsForReviewSafe(KhachHang khachHang, Integer maPhong) {
        try {
            return ctDatPhongRepository.findCompletedBookingsForReview(
                    khachHang.getMaKhachHang(), maPhong);
        } catch (Exception ex) {
            try {
                List<CtDatPhong> bookings = ctDatPhongRepository.findByKhachHang_MaKhachHang(khachHang.getMaKhachHang());
                return bookings.stream()
                        .filter(booking -> booking.getPhong() != null
                                && maPhong.equals(booking.getPhong().getMaPhong()))
                        .filter(booking -> "DA_TRA_PHONG".equalsIgnoreCase(booking.getTrangThai())
                                || "DA_THANH_TOAN".equalsIgnoreCase(booking.getTrangThai()))
                        .toList();
            } catch (Exception ignored) {
                return new ArrayList<>();
            }
        }
    }

    private List<CtDanhGia> loadReviewsSafe() {
        try {
            return ctDanhGiaRepository.findAllByOrderByNgayDanhGiaDesc();
        } catch (Exception ex) {
            return new ArrayList<>();
        }
    }

    private List<Phong> loadReviewableRoomsSafe(KhachHang khachHang) {
        if (khachHang == null || khachHang.getMaKhachHang() == null) {
            return new ArrayList<>();
        }
        try {
            return ctDatPhongRepository.findReviewableRoomsByCustomer(khachHang.getMaKhachHang());
        } catch (Exception ex) {
            try {
                List<CtDatPhong> bookings = ctDatPhongRepository.findByKhachHang_MaKhachHang(khachHang.getMaKhachHang());
                Map<Integer, Phong> rooms = new LinkedHashMap<>();
                for (CtDatPhong booking : bookings) {
                    if (booking.getPhong() != null
                            && booking.getPhong().getMaPhong() != null
                            && ("DA_TRA_PHONG".equalsIgnoreCase(booking.getTrangThai())
                            || "DA_THANH_TOAN".equalsIgnoreCase(booking.getTrangThai()))) {
                        rooms.putIfAbsent(booking.getPhong().getMaPhong(), booking.getPhong());
                    }
                }
                return new ArrayList<>(rooms.values());
            } catch (Exception ignored) {
                return new ArrayList<>();
            }
        }
    }
}
