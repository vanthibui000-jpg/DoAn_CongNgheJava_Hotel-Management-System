package com.example.do_an_java.controller;

import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.CtDichVu;
import com.example.do_an_java.entity.CtDichVuId;
import com.example.do_an_java.entity.DichVu;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.CtDichVuRepository;
import com.example.do_an_java.repository.DichVuRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin/ct-dich-vu")
public class CtDichVuController {
    private final CtDichVuRepository ctDichVuRepository;
    private final DichVuRepository dichVuRepository;
    private final CtDatPhongRepository ctDatPhongRepository;

    public CtDichVuController(CtDichVuRepository ctDichVuRepository,
                              DichVuRepository dichVuRepository,
                              CtDatPhongRepository ctDatPhongRepository) {
        this.ctDichVuRepository = ctDichVuRepository;
        this.dichVuRepository = dichVuRepository;
        this.ctDatPhongRepository = ctDatPhongRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", ctDichVuRepository.findAll());
        return "ct-dich-vu/list";
    }

    @GetMapping("/them")
    public String add(Model model) {
        CtDichVu item = new CtDichVu();
        item.setMaCtDichVu(ctDichVuRepository.findMaxMaCtDichVu() + 1);
        item.setDichVu(new DichVu());
        item.setCtDatPhong(new CtDatPhong());
        item.setSoLuong(1);
        model.addAttribute("item", item);
        addSelectData(model);
        return "ct-dich-vu/form";
    }

    @PostMapping("/luu")
    public String save(@ModelAttribute("item") CtDichVu item, Model model) {
        if (item.getMaCtDichVu() == null) {
            item.setMaCtDichVu(ctDichVuRepository.findMaxMaCtDichVu() + 1);
        }
        if (ctDichVuRepository.existsByMaCtDichVu(item.getMaCtDichVu())) {
            return showFormError(model, item, "Mã chi tiết dịch vụ đã tồn tại, vui lòng nhập mã khác.");
        }

        hydrateAndCalculate(item);
        if (item.getDichVu() == null || item.getCtDatPhong() == null || item.getId() == null) {
            return showFormError(model, item, "Vui lòng chọn đầy đủ đơn đặt phòng và dịch vụ.");
        }
        if (ctDichVuRepository.existsById(item.getId())) {
            return showFormError(model, item, "Dịch vụ này đã được thêm cho đơn đặt phòng đã chọn, vui lòng chọn dịch vụ hoặc đơn khác.");
        }

        ctDichVuRepository.save(item);
        return "redirect:/admin/ct-dich-vu";
    }

    @GetMapping("/them-vao-don")
    public String addToBooking(@RequestParam Integer maCtDatPhong, Model model) {
        CtDatPhong datPhong = ctDatPhongRepository.findById(maCtDatPhong).orElseThrow();
        model.addAttribute("datPhong", datPhong);
        model.addAttribute("dichVus", dichVuRepository.findAll());
        return "ct-dich-vu/them-vao-don";
    }

    @PostMapping("/luu-vao-don")
    public String saveToBooking(@RequestParam Integer maCtDatPhong,
                                @RequestParam Integer maDichVu,
                                @RequestParam(defaultValue = "1") Integer soLuong) {
        CtDatPhong datPhong = ctDatPhongRepository.findById(maCtDatPhong).orElseThrow();
        DichVu dichVu = dichVuRepository.findById(maDichVu).orElseThrow();
        CtDichVuId id = new CtDichVuId(maDichVu, maCtDatPhong);
        CtDichVu item = ctDichVuRepository.findById(id).orElseGet(CtDichVu::new);
        item.setId(id);
        item.setDichVu(dichVu);
        item.setCtDatPhong(datPhong);
        if (item.getMaCtDichVu() == null) {
            item.setMaCtDichVu(ctDichVuRepository.findMaxMaCtDichVu() + 1);
        }
        item.setSoLuong(soLuong == null || soLuong < 1 ? 1 : soLuong);
        item.setTongTienDichVu((dichVu.getGiaDichVu() == null ? 0 : dichVu.getGiaDichVu()) * item.getSoLuong());
        ctDichVuRepository.save(item);
        return "redirect:/admin/dat-phong/chi-tiet/" + maCtDatPhong;
    }

    @GetMapping("/xoa")
    public String delete(@RequestParam Integer maDichVu,
                         @RequestParam Integer maCtDatPhong,
                         @RequestParam(required = false) String back) {
        ctDichVuRepository.deleteById(new CtDichVuId(maDichVu, maCtDatPhong));
        if ("chi-tiet".equals(back)) {
            return "redirect:/admin/dat-phong/chi-tiet/" + maCtDatPhong;
        }
        return "redirect:/admin/ct-dich-vu";
    }

    private void hydrateAndCalculate(CtDichVu item) {
        if (item.getDichVu() != null && item.getDichVu().getMaDichVu() != null) {
            item.setDichVu(dichVuRepository.findById(item.getDichVu().getMaDichVu()).orElse(null));
        }
        if (item.getCtDatPhong() != null && item.getCtDatPhong().getMaCtDatPhong() != null) {
            item.setCtDatPhong(ctDatPhongRepository.findById(item.getCtDatPhong().getMaCtDatPhong()).orElse(null));
        }
        if (item.getDichVu() != null && item.getCtDatPhong() != null) {
            item.setId(new CtDichVuId(item.getDichVu().getMaDichVu(), item.getCtDatPhong().getMaCtDatPhong()));
        }
        int soLuong = item.getSoLuong() == null || item.getSoLuong() < 1 ? 1 : item.getSoLuong();
        item.setSoLuong(soLuong);
        int gia = item.getDichVu() == null || item.getDichVu().getGiaDichVu() == null ? 0 : item.getDichVu().getGiaDichVu();
        item.setTongTienDichVu(gia * soLuong);
    }

    private void ensureNestedModel(CtDichVu item) {
        if (item.getDichVu() == null) {
            item.setDichVu(new DichVu());
        }
        if (item.getCtDatPhong() == null) {
            item.setCtDatPhong(new CtDatPhong());
        }
    }

    private void addSelectData(Model model) {
        model.addAttribute("dichVus", dichVuRepository.findAll());
        model.addAttribute("datPhongs", ctDatPhongRepository.findAll());
    }

    private String showFormError(Model model, CtDichVu item, String error) {
        model.addAttribute("error", error);
        ensureNestedModel(item);
        model.addAttribute("item", item);
        addSelectData(model);
        return "ct-dich-vu/form";
    }
}
