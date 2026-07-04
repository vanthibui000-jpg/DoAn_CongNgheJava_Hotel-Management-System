package com.example.do_an_java.controller;

import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.CtDichVu;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.CtDichVuRepository;
import com.example.do_an_java.repository.HoaDonRepository;
import com.example.do_an_java.repository.KhachHangRepository;
import com.example.do_an_java.repository.NhanVienRepository;
import com.example.do_an_java.repository.PhongRepository;
import com.example.do_an_java.service.BookingService;
import com.example.do_an_java.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
@RequestMapping("/admin/dat-phong")
public class DatPhongController {
    private final CtDatPhongRepository ctDatPhongRepository;
    private final CtDichVuRepository ctDichVuRepository;
    private final KhachHangRepository khachHangRepository;
    private final PhongRepository phongRepository;
    private final NhanVienRepository nhanVienRepository;
    private final HoaDonRepository hoaDonRepository;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    public DatPhongController(CtDatPhongRepository ctDatPhongRepository,
                              CtDichVuRepository ctDichVuRepository,
                              KhachHangRepository khachHangRepository,
                              PhongRepository phongRepository,
                              NhanVienRepository nhanVienRepository,
                              HoaDonRepository hoaDonRepository,
                              BookingService bookingService,
                              PaymentService paymentService) {
        this.ctDatPhongRepository = ctDatPhongRepository;
        this.ctDichVuRepository = ctDichVuRepository;
        this.khachHangRepository = khachHangRepository;
        this.phongRepository = phongRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.bookingService = bookingService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public String list(Model model) {
        paymentService.expireOverduePayments();
        model.addAttribute("items", ctDatPhongRepository.findAllByOrderByNgayThucHienDesc());
        return "dat-phong/list";
    }

    @GetMapping("/chi-tiet/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        CtDatPhong datPhong = ctDatPhongRepository.findById(id).orElseThrow();
        List<CtDichVu> dichVuDaDung = ctDichVuRepository.findByCtDatPhong_MaCtDatPhong(id);
        long tienPhong = datPhong.getTongTienPhong() == null ? 0 : datPhong.getTongTienPhong();
        int tongTienDichVu = dichVuDaDung.stream()
                .mapToInt(item -> item.getTongTienDichVu() == null ? 0 : item.getTongTienDichVu())
                .sum();
        model.addAttribute("datPhong", datPhong);
        model.addAttribute("dichVuDaDung", dichVuDaDung);
        model.addAttribute("soDem", datPhong.getSoDem());
        model.addAttribute("tienPhong", tienPhong);
        model.addAttribute("tongTienDichVu", tongTienDichVu);
        model.addAttribute("tongCong", tienPhong + tongTienDichVu);
        return "dat-phong/chi-tiet";
    }

    @GetMapping("/chon-khach")
    public String chooseCustomer(@RequestParam(defaultValue = "") String keyword, Model model) {
        String key = keyword == null ? "" : keyword.trim().toLowerCase();
        List<com.example.do_an_java.entity.KhachHang> khachHangs = khachHangRepository.findAll().stream()
                .filter(kh -> key.isBlank()
                        || contains(kh.getHoTen(), key)
                        || contains(kh.getCmnd(), key)
                        || contains(kh.getDienThoai(), key)
                        || contains(kh.getEmail(), key))
                .toList();
        model.addAttribute("keyword", keyword);
        model.addAttribute("khachHangs", khachHangs);
        return "dat-phong/chon-khach";
    }

    @GetMapping("/tao-don/{maKhachHang}")
    public String createBookingForCustomer(@PathVariable Integer maKhachHang, Model model) {
        CtDatPhong item = new CtDatPhong();
        item.setMaCtDatPhong(ctDatPhongRepository.findMaxMaCtDatPhong() + 1);
        item.setNgayThucHien(LocalDate.now());
        item.setKhachHang(khachHangRepository.findById(maKhachHang).orElseThrow());
        item.setPhong(new com.example.do_an_java.entity.Phong());
        item.setNhanVien(new com.example.do_an_java.entity.NhanVien());
        item.setTrangThai("CHO_ADMIN_DUYET");
        item.setPaymentStatus("CHUA_TAO");
        ensureNestedModel(item);
        model.addAttribute("item", item);
        model.addAttribute("lockCustomerSelection", true);
        addSelectData(model);
        return "dat-phong/form";
    }

    @GetMapping("/them")
    public String add(Model model) {
        CtDatPhong item = new CtDatPhong();
        item.setMaCtDatPhong(ctDatPhongRepository.findMaxMaCtDatPhong() + 1);
        item.setNgayThucHien(LocalDate.now());
        item.setTrangThai("CHO_ADMIN_DUYET");
        item.setPaymentStatus("CHUA_TAO");
        ensureNestedModel(item);
        model.addAttribute("item", item);
        model.addAttribute("lockCustomerSelection", false);
        addSelectData(model);
        return "dat-phong/form";
    }

    @GetMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        CtDatPhong item = ctDatPhongRepository.findById(id).orElseThrow();
        ensureNestedModel(item);
        model.addAttribute("item", item);
        model.addAttribute("lockCustomerSelection", false);
        addSelectData(model);
        return "dat-phong/form";
    }

    @PostMapping("/luu")
    public String save(@ModelAttribute("item") CtDatPhong item,
                       @RequestParam(defaultValue = "false") boolean lockCustomerSelection,
                       Model model) {
        if (item.getMaCtDatPhong() == null) {
            item.setMaCtDatPhong(ctDatPhongRepository.findMaxMaCtDatPhong() + 1);
        }
        if (item.getNgayThucHien() == null) {
            item.setNgayThucHien(LocalDate.now());
        }
        if (item.getTrangThai() == null || item.getTrangThai().isBlank()) {
            item.setTrangThai("CHO_ADMIN_DUYET");
        }
        if (item.getPaymentStatus() == null || item.getPaymentStatus().isBlank()) {
            item.setPaymentStatus("CHUA_TAO");
        }

        hydrateRelations(item);
        if (("DA_THANH_TOAN".equals(item.getTrangThai()) || "DA_TRA_PHONG".equals(item.getTrangThai()))
                && !hasPaidInvoice(item.getMaCtDatPhong())) {
            return showBookingFormError(
                    model,
                    item,
                    lockCustomerSelection,
                    "Đơn phải có hóa đơn đã thanh toán trước khi chuyển sang Đã thanh toán hoặc Đã trả phòng."
            );
        }

        String error = bookingService.validate(item, item.getMaCtDatPhong(), false);
        if (error != null) {
            return showBookingFormError(model, item, lockCustomerSelection, error);
        }

        ctDatPhongRepository.save(item);
        return "redirect:/admin/dat-phong";
    }

    @GetMapping("/xac-nhan/{id}")
    public String confirm(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        CtDatPhong datPhong = ctDatPhongRepository.findById(id).orElse(null);
        if (datPhong == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt phòng.");
            return "redirect:/admin/dat-phong";
        }
        if (!isStatus(datPhong, null, "CHO_ADMIN_DUYET", "CHO_XAC_NHAN")) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể xác nhận đơn đang chờ admin duyệt.");
            return "redirect:/admin/dat-phong";
        }
        String error = bookingService.validate(datPhong, id, false);
        if (error != null) {
            redirectAttributes.addFlashAttribute("error", error);
            return "redirect:/admin/dat-phong";
        }
        try {
            paymentService.approveBookingAndCreatePayment(id);
            redirectAttributes.addFlashAttribute("message", "Đã xác nhận đặt phòng #" + id + " và tạo yêu cầu thanh toán VietQR.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể tạo yêu cầu thanh toán VietQR: " + ex.getMessage());
        }
        return "redirect:/admin/dat-phong";
    }

    @GetMapping("/huy/{id}")
    public String cancel(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        CtDatPhong datPhong = ctDatPhongRepository.findById(id).orElse(null);
        if (datPhong == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt phòng.");
            return "redirect:/admin/dat-phong";
        }
        if (!isStatus(datPhong, null, "CHO_ADMIN_DUYET", "CHO_XAC_NHAN", "CHO_HUY", "QUA_HAN_THANH_TOAN")) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể hủy đơn chờ duyệt, chờ xác nhận hủy hoặc quá hạn thanh toán.");
            return "redirect:/admin/dat-phong";
        }
        if (hoaDonRepository.existsByCtDatPhong_MaCtDatPhongAndTrangThai(id, "DA_THANH_TOAN")) {
            redirectAttributes.addFlashAttribute("error", "Đơn này đã có hóa đơn đã thanh toán nên không thể hủy.");
            return "redirect:/admin/dat-phong";
        }
        hoaDonRepository.findFirstByCtDatPhong_MaCtDatPhongOrderByMaHoaDonDesc(id)
                .filter(hoaDon -> !"DA_THANH_TOAN".equals(hoaDon.getTrangThai()))
                .ifPresent(hoaDonRepository::delete);
        paymentService.cancelPayment(id);
        redirectAttributes.addFlashAttribute("message", "Đã hủy đơn đặt phòng #" + id + ".");
        return "redirect:/admin/dat-phong";
    }

    @GetMapping("/tu-choi-huy/{id}")
    public String rejectCancel(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        CtDatPhong datPhong = ctDatPhongRepository.findById(id).orElse(null);
        if (datPhong == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt phòng.");
            return "redirect:/admin/dat-phong";
        }
        if (!isStatus(datPhong, "CHO_HUY")) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể từ chối đơn đang chờ duyệt hủy.");
            return "redirect:/admin/dat-phong";
        }
        datPhong.setTrangThai("CHO_THANH_TOAN");
        ctDatPhongRepository.save(datPhong);
        redirectAttributes.addFlashAttribute("message", "Đã từ chối yêu cầu hủy đơn #" + id + ".");
        return "redirect:/admin/dat-phong";
    }

    @GetMapping("/tra-phong/{id}")
    public String checkout(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        CtDatPhong datPhong = ctDatPhongRepository.findById(id).orElse(null);
        if (datPhong == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy đơn đặt phòng.");
            return "redirect:/admin/dat-phong";
        }
        if (!isStatus(datPhong, "DA_THANH_TOAN") || !hasPaidInvoice(id)) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể trả phòng sau khi đơn đã thanh toán.");
            return "redirect:/admin/dat-phong";
        }
        if (datPhong.getNgayNhan() != null && LocalDate.now().isBefore(datPhong.getNgayNhan())) {
            redirectAttributes.addFlashAttribute("error", "Chưa đến ngày nhận phòng nên chưa thể trả phòng.");
            return "redirect:/admin/dat-phong";
        }
        datPhong.setTrangThai("DA_TRA_PHONG");
        ctDatPhongRepository.save(datPhong);
        redirectAttributes.addFlashAttribute("message", "Đã ghi nhận trả phòng cho đơn #" + id + ".");
        return "redirect:/admin/dat-phong";
    }

    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        if (hoaDonRepository.existsByCtDatPhong_MaCtDatPhong(id)) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa đơn đã có hóa đơn.");
            return "redirect:/admin/dat-phong";
        }
        if (!ctDichVuRepository.findByCtDatPhong_MaCtDatPhong(id).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa đơn đã phát sinh dịch vụ.");
            return "redirect:/admin/dat-phong";
        }
        ctDatPhongRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Đã xóa đơn đặt phòng #" + id + ".");
        return "redirect:/admin/dat-phong";
    }

    private String showBookingFormError(Model model,
                                        CtDatPhong item,
                                        boolean lockCustomerSelection,
                                        String error) {
        model.addAttribute("error", error);
        ensureNestedModel(item);
        model.addAttribute("item", item);
        model.addAttribute("lockCustomerSelection", lockCustomerSelection);
        addSelectData(model);
        return "dat-phong/form";
    }

    private boolean contains(String source, String keyword) {
        return source != null && source.toLowerCase().contains(keyword);
    }

    private void hydrateRelations(CtDatPhong item) {
        if (item.getKhachHang() != null && item.getKhachHang().getMaKhachHang() != null) {
            item.setKhachHang(khachHangRepository.findById(item.getKhachHang().getMaKhachHang()).orElse(null));
        }
        if (item.getPhong() != null && item.getPhong().getMaPhong() != null) {
            item.setPhong(phongRepository.findById(item.getPhong().getMaPhong()).orElse(null));
        }
        if (item.getNhanVien() != null && item.getNhanVien().getMaNhanVien() != null) {
            item.setNhanVien(nhanVienRepository.findById(item.getNhanVien().getMaNhanVien()).orElse(null));
        }
    }

    private void ensureNestedModel(CtDatPhong item) {
        if (item.getKhachHang() == null) {
            item.setKhachHang(new com.example.do_an_java.entity.KhachHang());
        }
        if (item.getPhong() == null) {
            item.setPhong(new com.example.do_an_java.entity.Phong());
        }
        if (item.getNhanVien() == null) {
            item.setNhanVien(new com.example.do_an_java.entity.NhanVien());
        }
    }

    private void addSelectData(Model model) {
        model.addAttribute("khachHangs", khachHangRepository.findAll());
        model.addAttribute("phongs", phongRepository.findAll());
        model.addAttribute("nhanViens", nhanVienRepository.findAll());
    }

    private boolean isStatus(CtDatPhong datPhong, String... statuses) {
        String current = datPhong.getTrangThai();
        for (String status : statuses) {
            if (status == null) {
                if (current == null || current.isBlank()) {
                    return true;
                }
            } else if (status.equals(current)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasPaidInvoice(Integer maCtDatPhong) {
        return maCtDatPhong != null
                && hoaDonRepository.existsByCtDatPhong_MaCtDatPhongAndTrangThai(maCtDatPhong, "DA_THANH_TOAN");
    }
}
