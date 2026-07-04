package com.example.do_an_java.controller;

import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.CtDichVu;
import com.example.do_an_java.entity.HoaDon;
import com.example.do_an_java.entity.NhanVien;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.CtDichVuRepository;
import com.example.do_an_java.repository.HoaDonRepository;
import com.example.do_an_java.repository.NhanVienRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/admin/hoa-don")
public class HoaDonController {

    private final HoaDonRepository hoaDonRepository;
    private final CtDatPhongRepository ctDatPhongRepository;
    private final NhanVienRepository nhanVienRepository;
    private final CtDichVuRepository ctDichVuRepository;

    public HoaDonController(
            HoaDonRepository hoaDonRepository,
            CtDatPhongRepository ctDatPhongRepository,
            NhanVienRepository nhanVienRepository,
            CtDichVuRepository ctDichVuRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.ctDatPhongRepository = ctDatPhongRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.ctDichVuRepository = ctDichVuRepository;
    }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("items", hoaDonRepository.findAll());
        return "hoa-don/list";
    }

    @GetMapping("/chi-tiet/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElseThrow();
        CtDatPhong datPhong = hoaDon.getCtDatPhong();
        List<CtDichVu> dichVuDaDung = datPhong == null
                ? List.of()
                : ctDichVuRepository.findByCtDatPhong_MaCtDatPhong(datPhong.getMaCtDatPhong());
        long tienPhong = datPhong == null || datPhong.getTongTienPhong() == null ? 0 : datPhong.getTongTienPhong();
        int tongTienDichVu = dichVuDaDung.stream()
                .mapToInt(item -> item.getTongTienDichVu() == null ? 0 : item.getTongTienDichVu())
                .sum();
        model.addAttribute("hoaDon", hoaDon);
        model.addAttribute("datPhong", datPhong);
        model.addAttribute("dichVuDaDung", dichVuDaDung);
        model.addAttribute("tienPhong", tienPhong);
        model.addAttribute("tongTienDichVu", tongTienDichVu);
        model.addAttribute("tongCong", tienPhong + tongTienDichVu);
        return "hoa-don/detail";
    }

    @GetMapping("/them")
    public String add(Model model) {
        HoaDon hoaDon = new HoaDon();
        hoaDon.setNgayThuTien(LocalDate.now());
        hoaDon.setTrangThai("CHUA_THANH_TOAN");
        hoaDon.setCtDatPhong(new CtDatPhong());
        hoaDon.setNhanVien(new NhanVien());
        model.addAttribute("item", hoaDon);
        model.addAttribute("today", LocalDate.now());
        addSelectData(model);
        return "hoa-don/form";
    }

    @GetMapping("/tao-tu-don/{id}")
    public String createFromBooking(@PathVariable Integer id, Model model, RedirectAttributes redirectAttributes) {
        CtDatPhong ctDatPhong = ctDatPhongRepository.findById(id).orElseThrow();
        if (hoaDonRepository.existsByCtDatPhong_MaCtDatPhong(id)) {
            redirectAttributes.addFlashAttribute("error", "Đơn đặt phòng này đã có hóa đơn, không thể tạo thêm.");
            return "redirect:/admin/dat-phong/chi-tiet/" + id;
        }
        if ("DA_THANH_TOAN".equals(ctDatPhong.getTrangThai())) {
            redirectAttributes.addFlashAttribute("error", "Đơn đặt phòng này đã thanh toán, không thể tạo thêm hóa đơn.");
            return "redirect:/admin/dat-phong/chi-tiet/" + id;
        }
        if (!canCreateInvoiceFor(ctDatPhong)) {
            redirectAttributes.addFlashAttribute("error", "Chỉ có thể lập hóa đơn cho đơn đã xác nhận hoặc đã trả phòng.");
            return "redirect:/admin/dat-phong/chi-tiet/" + id;
        }
        HoaDon hoaDon = new HoaDon();
        hoaDon.setNgayThuTien(LocalDate.now());
        hoaDon.setTrangThai("CHUA_THANH_TOAN");
        hoaDon.setCtDatPhong(ctDatPhong);
        hoaDon.setNhanVien(new NhanVien());
        hoaDon.setSoTienThu(tinhTongTien(ctDatPhong));
        model.addAttribute("item", hoaDon);
        model.addAttribute("message", "Hóa đơn đã được tạo từ đơn đặt phòng #" + id + ". Số tiền đã tự tính gồm tiền phòng và dịch vụ.");
        model.addAttribute("lockBookingSelection", true);
        model.addAttribute("today", LocalDate.now());
        addSelectData(model);
        return "hoa-don/form";
    }

    @GetMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElseThrow();
        if (hoaDon.getCtDatPhong() == null) {
            hoaDon.setCtDatPhong(new CtDatPhong());
        }
        if (hoaDon.getNhanVien() == null) {
            hoaDon.setNhanVien(new NhanVien());
        }
        if (hoaDon.getNgayThuTien() == null) {
            hoaDon.setNgayThuTien(LocalDate.now());
        }
        model.addAttribute("item", hoaDon);
        model.addAttribute("today", LocalDate.now());
        addSelectData(model);
        return "hoa-don/form";
    }

    @PostMapping("/luu")
    public String save(@ModelAttribute("item") HoaDon item, Model model) {
        if (item.getCtDatPhong() == null || item.getCtDatPhong().getMaCtDatPhong() == null) {
            model.addAttribute("error", "Vui lòng chọn chi tiết đặt phòng");
            prepareFormOnError(item, model);
            return "hoa-don/form";
        }

        Integer maCtDatPhong = item.getCtDatPhong().getMaCtDatPhong();
        boolean duplicateInvoice = item.getMaHoaDon() == null
                ? hoaDonRepository.existsByCtDatPhong_MaCtDatPhong(maCtDatPhong)
                : hoaDonRepository.existsByCtDatPhong_MaCtDatPhongAndMaHoaDonNot(maCtDatPhong, item.getMaHoaDon());
        if (duplicateInvoice) {
            model.addAttribute("error", "Chi tiết đặt phòng này đã có hóa đơn");
            prepareFormOnError(item, model);
            return "hoa-don/form";
        }

        CtDatPhong ctDatPhong = ctDatPhongRepository
                .findById(maCtDatPhong)
                .orElseThrow();
        if (!canCreateInvoiceFor(ctDatPhong) && !"DA_THANH_TOAN".equals(ctDatPhong.getTrangThai())) {
            model.addAttribute("error", "Chỉ có thể lập hóa đơn cho đơn đã xác nhận hoặc đã trả phòng");
            prepareFormOnError(item, model);
            return "hoa-don/form";
        }
        item.setCtDatPhong(ctDatPhong);

        if (item.getNhanVien() != null && item.getNhanVien().getMaNhanVien() != null) {
            item.setNhanVien(nhanVienRepository.findById(item.getNhanVien().getMaNhanVien()).orElse(null));
        } else {
            item.setNhanVien(null);
        }

        item.setSoTienThu(tinhTongTien(ctDatPhong));
        if (item.getNgayThuTien() == null) {
            item.setNgayThuTien(LocalDate.now());
        }
        if (item.getTrangThai() == null || item.getTrangThai().isBlank()) {
            item.setTrangThai("CHUA_THANH_TOAN");
        }

        hoaDonRepository.save(item);

        if ("DA_THANH_TOAN".equals(item.getTrangThai())) {
            ctDatPhong.setTrangThai("DA_THANH_TOAN");
            ctDatPhongRepository.save(ctDatPhong);
        }

        return "redirect:/admin/hoa-don";
    }

    @GetMapping("/thanh-toan/{id}")
    public String thanhToan(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElse(null);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy hóa đơn.");
            return "redirect:/admin/hoa-don";
        }
        if ("DA_THANH_TOAN".equals(hoaDon.getTrangThai())) {
            redirectAttributes.addFlashAttribute("message", "Hóa đơn #" + id + " đã được thanh toán trước đó.");
            return "redirect:/admin/hoa-don";
        }
        if (hoaDon.getCtDatPhong() == null || !canCreateInvoiceFor(hoaDon.getCtDatPhong())) {
            redirectAttributes.addFlashAttribute("error", "Không thể thanh toán hóa đơn của đơn chưa xác nhận hoặc đã hủy.");
            return "redirect:/admin/hoa-don";
        }
        hoaDonRepository.findById(id).ifPresent(item -> {
            item.setTrangThai("DA_THANH_TOAN");
            item.setNgayThuTien(LocalDate.now());
            if (item.getCtDatPhong() != null) {
                CtDatPhong datPhong = item.getCtDatPhong();
                datPhong.setTrangThai("DA_THANH_TOAN");
                ctDatPhongRepository.save(datPhong);
            }
            hoaDonRepository.save(item);
        });
        redirectAttributes.addFlashAttribute("message", "Đã thanh toán hóa đơn #" + id + ".");
        return "redirect:/admin/hoa-don";
    }

    @GetMapping("/xoa/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes redirectAttributes) {
        HoaDon hoaDon = hoaDonRepository.findById(id).orElse(null);
        if (hoaDon == null) {
            redirectAttributes.addFlashAttribute("error", "Không tìm thấy hóa đơn.");
            return "redirect:/admin/hoa-don";
        }
        if ("DA_THANH_TOAN".equals(hoaDon.getTrangThai())) {
            redirectAttributes.addFlashAttribute("error", "Không thể xóa hóa đơn đã thanh toán.");
            return "redirect:/admin/hoa-don";
        }
        hoaDonRepository.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Đã xóa hóa đơn #" + id + ".");
        return "redirect:/admin/hoa-don";
    }

    private void prepareFormOnError(HoaDon item, Model model) {
        if (item.getCtDatPhong() == null) {
            item.setCtDatPhong(new CtDatPhong());
        }
        if (item.getNhanVien() == null) {
            item.setNhanVien(new NhanVien());
        }
        if (item.getNgayThuTien() == null) {
            item.setNgayThuTien(LocalDate.now());
        }
        model.addAttribute("item", item);
        model.addAttribute("today", LocalDate.now());
        addSelectData(model);
    }

    private void addSelectData(Model model) {
        model.addAttribute("datPhongs", ctDatPhongRepository.findAll());
        model.addAttribute("nhanViens", nhanVienRepository.findAll());
    }

    private Integer tinhTongTien(CtDatPhong ctDatPhong) {
        if (ctDatPhong == null || ctDatPhong.getNgayNhan() == null || ctDatPhong.getNgayTra() == null) {
            return 0;
        }
        long soNgay = ChronoUnit.DAYS.between(ctDatPhong.getNgayNhan(), ctDatPhong.getNgayTra());
        if (soNgay <= 0) {
            soNgay = 1;
        }

        int giaPhong = 0;
        if (ctDatPhong.getPhong() != null
                && ctDatPhong.getPhong().getLoaiPhong() != null
                && ctDatPhong.getPhong().getLoaiPhong().getGiaPhong() != null) {
            giaPhong = ctDatPhong.getPhong().getLoaiPhong().getGiaPhong();
        }
        int tienPhong = Math.toIntExact(soNgay * giaPhong);
        int tienDichVu = 0;

        List<CtDichVu> dsDichVu = ctDichVuRepository.findByCtDatPhong_MaCtDatPhong(ctDatPhong.getMaCtDatPhong());
        for (CtDichVu dv : dsDichVu) {
            if (dv.getTongTienDichVu() != null) {
                tienDichVu += dv.getTongTienDichVu();
            }
        }

        return tienPhong + tienDichVu;
    }

    private boolean canCreateInvoiceFor(CtDatPhong ctDatPhong) {
        if (ctDatPhong == null) {
            return false;
        }
        String status = ctDatPhong.getTrangThai();
        return "DA_XAC_NHAN".equals(status) || "DA_TRA_PHONG".equals(status);
    }
}
