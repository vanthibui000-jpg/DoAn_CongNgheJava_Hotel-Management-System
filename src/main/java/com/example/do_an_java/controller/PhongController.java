package com.example.do_an_java.controller;

import com.example.do_an_java.entity.LoaiPhong;
import com.example.do_an_java.entity.Phong;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.LoaiPhongRepository;
import com.example.do_an_java.repository.PhongRepository;
import com.example.do_an_java.service.ImageStorageService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Controller
@RequestMapping("/admin/phong")
public class PhongController {
    private final PhongRepository phongRepository;
    private final LoaiPhongRepository loaiPhongRepository;
    private final CtDatPhongRepository ctDatPhongRepository;
    private final ImageStorageService imageStorageService;

    public PhongController(PhongRepository phongRepository,
                           LoaiPhongRepository loaiPhongRepository,
                           CtDatPhongRepository ctDatPhongRepository,
                           ImageStorageService imageStorageService) {
        this.phongRepository = phongRepository;
        this.loaiPhongRepository = loaiPhongRepository;
        this.ctDatPhongRepository = ctDatPhongRepository;
        this.imageStorageService = imageStorageService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(required = false) Integer maLoaiPhong,
                       @RequestParam(required = false) Integer giaTu,
                       @RequestParam(required = false) Integer giaDen,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayNhan,
                       @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ngayTra,
                       Model model) {
        List<Phong> allRooms = phongRepository.findAll();
        List<Phong> items = allRooms.stream()
                .filter(phong -> matchesFilters(phong, keyword, maLoaiPhong, giaTu, giaDen))
                .toList();

        if (ngayNhan != null && ngayTra != null) {
            if (!ngayTra.isAfter(ngayNhan)) {
                model.addAttribute("dateError", "Ngày trả phải sau ngày nhận ít nhất 1 ngày.");
            } else {
                List<Integer> maPhongBiDat = ctDatPhongRepository.findMaPhongBiDatTrongKhoang(ngayNhan, ngayTra);
                items = items.stream()
                        .filter(phong -> !"Bảo trì".equalsIgnoreCase(normalizeStatus(phong.getTrangThai())))
                        .filter(phong -> !maPhongBiDat.contains(phong.getMaPhong()))
                        .toList();
            }
        }

        long phongTrong = allRooms.stream().filter(phong -> isStatus(phong, "Trống")).count();
        long phongDangThue = allRooms.stream()
                .filter(phong -> isStatus(phong, "Đang thuê")
                        || isStatus(phong, "Đang sử dụng")
                        || isStatus(phong, "Đã đặt"))
                .count();
        long phongBaoTri = allRooms.stream().filter(phong -> isStatus(phong, "Bảo trì")).count();

        model.addAttribute("items", items);
        model.addAttribute("loaiPhongs", loaiPhongRepository.findAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("maLoaiPhong", maLoaiPhong);
        model.addAttribute("giaTu", giaTu);
        model.addAttribute("giaDen", giaDen);
        model.addAttribute("ngayNhan", ngayNhan);
        model.addAttribute("ngayTra", ngayTra);
        model.addAttribute("phongTrong", phongTrong);
        model.addAttribute("phongDangThue", phongDangThue);
        model.addAttribute("phongBaoTri", phongBaoTri);
        return "phong/list";
    }

    @GetMapping("/them")
    public String add(Model model) {
        Phong item = new Phong();
        item.setTrangThai("Trống");
        item.setLoaiPhong(new LoaiPhong());
        model.addAttribute("item", item);
        model.addAttribute("loaiPhongs", loaiPhongRepository.findAll());
        model.addAttribute("isEdit", false);
        model.addAttribute("roomGallery", imageStorageService.getRoomGallery(item));
        return "phong/form";
    }

    @GetMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        Phong item = phongRepository.findById(id).orElseThrow();
        if (item.getLoaiPhong() == null) {
            item.setLoaiPhong(new LoaiPhong());
        }
        model.addAttribute("item", item);
        model.addAttribute("loaiPhongs", loaiPhongRepository.findAll());
        model.addAttribute("isEdit", true);
        model.addAttribute("roomGallery", imageStorageService.getRoomGallery(item));
        return "phong/form";
    }

    @PostMapping("/luu")
    public String save(@ModelAttribute("item") Phong item,
                       @RequestParam(defaultValue = "false") boolean isEdit,
                       @RequestParam(value = "hinhAnhFiles", required = false) MultipartFile[] hinhAnhFiles,
                       Model model) {
        try {
            imageStorageService.validateImageFiles(hinhAnhFiles);
        } catch (IllegalArgumentException ex) {
            return showFormError(model, item, isEdit, ex.getMessage());
        }
        if (item.getMaPhong() == null) {
            return showFormError(model, item, isEdit, "Vui lòng nhập mã phòng.");
        }
        if (!isEdit && phongRepository.existsById(item.getMaPhong())) {
            return showFormError(model, item, false, "Mã phòng này đã tồn tại, vui lòng nhập mã khác.");
        }
        if (item.getTrangThai() == null || item.getTrangThai().isBlank()) {
            item.setTrangThai("Trống");
        }
        if (item.getLoaiPhong() != null && item.getLoaiPhong().getMaLoaiPhong() != null) {
            item.setLoaiPhong(loaiPhongRepository.findById(item.getLoaiPhong().getMaLoaiPhong()).orElse(null));
        }
        try {
            phongRepository.save(item);
            String imagePath = imageStorageService.storeRoomImages(hinhAnhFiles, item.getMaPhong());
            if (imagePath != null) {
                item.setHinhAnhDaiDien(imagePath);
                phongRepository.save(item);
            }
        } catch (IOException | IllegalArgumentException ex) {
            return showFormError(model, item, isEdit, ex.getMessage());
        }
        return "redirect:/admin/phong";
    }

    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id) {
        phongRepository.deleteById(id);
        try {
            imageStorageService.deleteRoomImages(id);
        } catch (IOException ignored) {
        }
        return "redirect:/admin/phong";
    }

    private String showFormError(Model model, Phong item, boolean isEdit, String error) {
        model.addAttribute("error", error);
        model.addAttribute("item", item);
        model.addAttribute("loaiPhongs", loaiPhongRepository.findAll());
        model.addAttribute("isEdit", isEdit);
        model.addAttribute("roomGallery", imageStorageService.getRoomGallery(item));
        return "phong/form";
    }

    private boolean matchesFilters(Phong phong, String keyword, Integer maLoaiPhong, Integer giaTu, Integer giaDen) {
        if (keyword != null && !keyword.isBlank()) {
            String key = keyword.trim().toLowerCase(Locale.ROOT);
            String maPhong = phong.getMaPhong() == null ? "" : phong.getMaPhong().toString();
            if (!maPhong.contains(key)) {
                return false;
            }
        }
        if (maLoaiPhong != null) {
            if (phong.getLoaiPhong() == null || !Objects.equals(phong.getLoaiPhong().getMaLoaiPhong(), maLoaiPhong)) {
                return false;
            }
        }
        Integer giaPhong = phong.getLoaiPhong() == null ? null : phong.getLoaiPhong().getGiaPhong();
        if (giaTu != null && (giaPhong == null || giaPhong < giaTu)) {
            return false;
        }
        if (giaDen != null && (giaPhong == null || giaPhong > giaDen)) {
            return false;
        }
        return true;
    }

    private boolean isStatus(Phong phong, String expected) {
        return expected.equalsIgnoreCase(normalizeStatus(phong.getTrangThai()));
    }

    private String normalizeStatus(String status) {
        return status == null || status.isBlank() ? "Trống" : status.trim();
    }
}
