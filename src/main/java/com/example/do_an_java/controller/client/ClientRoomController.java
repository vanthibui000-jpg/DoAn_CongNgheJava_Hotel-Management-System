package com.example.do_an_java.controller.client;

import com.example.do_an_java.controller.PaginationUtil;
import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.Phong;
import com.example.do_an_java.repository.CtDanhGiaRepository;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.PhongRepository;
import com.example.do_an_java.service.ImageStorageService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Controller
public class ClientRoomController {
    private final PhongRepository phongRepository;
    private final CtDanhGiaRepository ctDanhGiaRepository;
    private final CtDatPhongRepository ctDatPhongRepository;
    private final ImageStorageService imageStorageService;

    public ClientRoomController(PhongRepository phongRepository,
                                CtDanhGiaRepository ctDanhGiaRepository,
                                CtDatPhongRepository ctDatPhongRepository,
                                ImageStorageService imageStorageService) {
        this.phongRepository = phongRepository;
        this.ctDanhGiaRepository = ctDanhGiaRepository;
        this.ctDatPhongRepository = ctDatPhongRepository;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping("/phong")
    public String rooms(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayNhan,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayTra,
                        @RequestParam(defaultValue = "0") int page,
                        Model model,
                        HttpServletRequest request) {
        List<Phong> phongs = phongRepository.findClientVisibleRooms();
        if (ngayNhan != null && ngayTra != null) {
            if (ngayNhan.isBefore(LocalDate.now())) {
                model.addAttribute("dateError", "Ngày nhận không được nhỏ hơn ngày hiện tại.");
            } else if (ngayTra.isAfter(ngayNhan)) {
                LocalDate ngayTraHieuLuc = ngayTra;
                phongs = phongs.stream()
                        .filter(phong -> !"Bảo trì".equals(phong.getTrangThai()))
                        .filter(phong -> ctDatPhongRepository.findActiveBookingsByRoom(phong.getMaPhong()).stream()
                                .noneMatch(datPhong -> overlaps(ngayNhan, ngayTraHieuLuc, datPhong)))
                        .toList();
            } else {
                model.addAttribute("dateError", "Ngày trả phải sau ngày nhận ít nhất 1 ngày.");
            }
        }

        PaginationUtil.paginate(model, phongs, page, request);
        model.addAttribute("phongs", model.asMap().get("items"));
        model.addAttribute("ngayNhan", ngayNhan);
        model.addAttribute("ngayTra", ngayTra);
        model.addAttribute("today", LocalDate.now());
        return "client/rooms";
    }

    @GetMapping("/phong/{id}")
    public String roomDetail(@PathVariable Integer id, Model model) {
        Phong phong = phongRepository.findById(id).orElseThrow();
        model.addAttribute("phong", phong);
        model.addAttribute("roomGallery", imageStorageService.getRoomGallery(phong));
        model.addAttribute("danhGias", ctDanhGiaRepository.findByPhong_MaPhongOrderByNgayDanhGiaDesc(id));
        return "client/room-detail";
    }

    private boolean overlaps(LocalDate start, LocalDate end, CtDatPhong existing) {
        if (existing.getNgayNhan() == null || existing.getNgayTra() == null) {
            return false;
        }
        LocalDate existingStart = existing.getNgayNhan();
        LocalDate existingEnd = existing.getNgayTra();
        return existingStart.isBefore(end) && existingEnd.isAfter(start);
    }
}
