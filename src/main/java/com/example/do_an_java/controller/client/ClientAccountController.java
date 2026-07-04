package com.example.do_an_java.controller.client;

import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.CtDichVu;
import com.example.do_an_java.entity.HoaDon;
import com.example.do_an_java.entity.KhachHang;
import com.example.do_an_java.entity.Payment;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.CtDichVuRepository;
import com.example.do_an_java.repository.HoaDonRepository;
import com.example.do_an_java.repository.KhachHangRepository;
import com.example.do_an_java.service.InvoiceService;
import com.example.do_an_java.service.PaymentService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Controller
public class ClientAccountController {
    private final CtDatPhongRepository ctDatPhongRepository;
    private final CtDichVuRepository ctDichVuRepository;
    private final HoaDonRepository hoaDonRepository;
    private final KhachHangRepository khachHangRepository;
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    public ClientAccountController(CtDatPhongRepository ctDatPhongRepository,
                                   CtDichVuRepository ctDichVuRepository,
                                   HoaDonRepository hoaDonRepository,
                                   KhachHangRepository khachHangRepository,
                                   PaymentService paymentService,
                                   InvoiceService invoiceService) {
        this.ctDatPhongRepository = ctDatPhongRepository;
        this.ctDichVuRepository = ctDichVuRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.khachHangRepository = khachHangRepository;
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
    }

    @GetMapping("/tai-khoan")
    public String profile(Model model, HttpSession session) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }
        model.addAttribute("khachHang", khachHang);
        return "client/profile";
    }

    @GetMapping("/tai-khoan/doi-mat-khau")
    public String changePasswordPage(HttpSession session) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }
        return "client/change-password";
    }

    @GetMapping("/tai-khoan/cap-nhat")
    public String editProfilePage(Model model, HttpSession session) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }
        model.addAttribute("khachHang", khachHang);
        return "client/edit-profile";
    }

    @PostMapping("/tai-khoan/cap-nhat")
    public String updateProfile(@RequestParam String hoTen,
                                @RequestParam String email,
                                @RequestParam String dienThoai,
                                @RequestParam(required = false) String diaChi,
                                @RequestParam(required = false) String cmnd,
                                HttpSession session,
                                Model model) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        String emailMoi = email == null ? "" : email.trim();
        if (hoTen == null || hoTen.trim().isBlank()) {
            model.addAttribute("error", "Vui lòng nhập họ tên.");
            model.addAttribute("khachHang", khachHang);
            return "client/edit-profile";
        }
        if (emailMoi.isBlank()) {
            model.addAttribute("error", "Vui lòng nhập email.");
            model.addAttribute("khachHang", khachHang);
            return "client/edit-profile";
        }
        boolean emailDaDung = khachHangRepository.findByEmailIgnoreCase(emailMoi)
                .filter(item -> !item.getMaKhachHang().equals(khachHang.getMaKhachHang()))
                .isPresent();
        if (emailDaDung) {
            model.addAttribute("error", "Email đã được sử dụng bởi tài khoản khác.");
            model.addAttribute("khachHang", khachHang);
            return "client/edit-profile";
        }

        khachHang.setHoTen(hoTen.trim());
        khachHang.setEmail(emailMoi);
        khachHang.setDienThoai(dienThoai == null ? "" : dienThoai.trim());
        khachHang.setDiaChi(diaChi == null ? "" : diaChi.trim());
        khachHang.setCmnd(cmnd == null ? "" : cmnd.trim());
        khachHangRepository.save(khachHang);
        session.setAttribute(ClientSession.CUSTOMER_SESSION_KEY, khachHang);

        model.addAttribute("success", "Cập nhật thông tin thành công.");
        model.addAttribute("khachHang", khachHang);
        return "client/edit-profile";
    }

    @PostMapping("/tai-khoan/doi-mat-khau")
    public String changePassword(@RequestParam String matKhauCu,
                                 @RequestParam String matKhauMoi,
                                 @RequestParam String xacNhanMatKhau,
                                 HttpSession session,
                                 Model model) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }
        if (khachHang.getMatKhau() == null || !khachHang.getMatKhau().equals(matKhauCu)) {
            model.addAttribute("error", "Mật khẩu cũ không đúng.");
            return "client/change-password";
        }
        if (matKhauMoi == null || matKhauMoi.length() < 3) {
            model.addAttribute("error", "Mật khẩu mới cần ít nhất 3 ký tự.");
            return "client/change-password";
        }
        if (!matKhauMoi.equals(xacNhanMatKhau)) {
            model.addAttribute("error", "Xác nhận mật khẩu không khớp.");
            return "client/change-password";
        }
        if (matKhauMoi.equals(matKhauCu)) {
            model.addAttribute("error", "Mật khẩu mới phải khác mật khẩu cũ.");
            return "client/change-password";
        }

        khachHang.setMatKhau(matKhauMoi);
        khachHangRepository.save(khachHang);
        session.setAttribute(ClientSession.CUSTOMER_SESSION_KEY, khachHang);
        model.addAttribute("success", "Đổi mật khẩu thành công.");
        return "client/change-password";
    }

    @GetMapping({"/tai-khoan/lich-su-dat-phong", "/lich-su-dat-phong"})
    public String bookingHistory(Model model, HttpSession session) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }
        paymentService.expireOverduePayments();
        List<BookingView> items = ctDatPhongRepository.findAll().stream()
                .filter(item -> item.getKhachHang() != null
                        && khachHang.getMaKhachHang().equals(item.getKhachHang().getMaKhachHang()))
                .sorted(Comparator.comparing(
                        CtDatPhong::getNgayThucHien,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .map(BookingView::new)
                .toList();
        model.addAttribute("items", items);
        return "client/booking-history";
    }

    @GetMapping({"/tai-khoan/lich-su-dat-phong/{id}", "/lich-su-dat-phong/{id}"})
    public String bookingDetail(@PathVariable Integer id, Model model, HttpSession session) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        CtDatPhong datPhong = ctDatPhongRepository.findById(id).orElse(null);
        if (datPhong == null
                || datPhong.getKhachHang() == null
                || !khachHang.getMaKhachHang().equals(datPhong.getKhachHang().getMaKhachHang())) {
            return "redirect:/tai-khoan/lich-su-dat-phong";
        }
        paymentService.expireOverduePayments();
        datPhong = ctDatPhongRepository.findById(id).orElse(datPhong);

        List<CtDichVu> dichVuDaDung = ctDichVuRepository.findByCtDatPhong_MaCtDatPhong(id);
        BookingView bookingView = new BookingView(datPhong);
        long tienPhong = bookingView.getTongTienPhong() == null ? 0 : bookingView.getTongTienPhong();
        int tongTienDichVu = dichVuDaDung.stream()
                .mapToInt(item -> item.getTongTienDichVu() == null ? 0 : item.getTongTienDichVu())
                .sum();

        model.addAttribute("datPhong", bookingView);
        model.addAttribute("dichVuDaDung", dichVuDaDung);
        model.addAttribute("tienPhong", tienPhong);
        model.addAttribute("tongTienDichVu", tongTienDichVu);
        model.addAttribute("tongCong", tienPhong + tongTienDichVu);
        model.addAttribute("hoaDon", invoiceService.canPrintInvoice(id) ? findInvoice(id).orElse(null) : null);
        model.addAttribute("payment", paymentService.findByBookingId(id).orElse(null));
        model.addAttribute("coTheHuy", canCustomerCancel(datPhong));
        model.addAttribute("coTheThanhToan", canCustomerPay(datPhong));
        model.addAttribute("coTheInHoaDon", invoiceService.canPrintInvoice(id));
        return "client/booking-detail";
    }

    @PostMapping({"/tai-khoan/lich-su-dat-phong/huy/{id}", "/lich-su-dat-phong/huy/{id}"})
    public String cancelBooking(@PathVariable Integer id,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        CtDatPhong datPhong = findCustomerBooking(id, khachHang);
        if (datPhong == null) {
            redirectAttributes.addFlashAttribute("error", "Khong tim thay don dat phong.");
            return "redirect:/tai-khoan/lich-su-dat-phong";
        }
        if (!canCustomerCancel(datPhong)) {
            redirectAttributes.addFlashAttribute("error", "Chi co the huy don dang cho xac nhan hoac da xac nhan va chua thanh toan.");
            return "redirect:/tai-khoan/lich-su-dat-phong/" + id;
        }

        if ("DA_XAC_NHAN".equals(datPhong.getTrangThai())) {
            datPhong.setTrangThai("CHO_HUY");
            ctDatPhongRepository.save(datPhong);
            redirectAttributes.addFlashAttribute("message", "Da gui yeu cau huy don #" + id + ". Vui long cho admin dong y huy.");
            return "redirect:/tai-khoan/lich-su-dat-phong";
        }

        findInvoice(id).ifPresent(hoaDon -> {
            if (!"DA_THANH_TOAN".equals(hoaDon.getTrangThai())) {
                hoaDonRepository.delete(hoaDon);
            }
        });
        paymentService.cancelPayment(id);
        redirectAttributes.addFlashAttribute("message", "Da huy don dat phong #" + id + ".");
        return "redirect:/tai-khoan/lich-su-dat-phong";
    }

    @GetMapping({"/tai-khoan/thanh-toan/{id}", "/thanh-toan/{id}"})
    public String paymentPage(@PathVariable Integer id,
                              Model model,
                              HttpSession session,
                              RedirectAttributes redirectAttributes) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        CtDatPhong datPhong = findCustomerBooking(id, khachHang);
        if (datPhong == null) {
            return "redirect:/tai-khoan/lich-su-dat-phong";
        }
        if (!canCustomerPay(datPhong)) {
            redirectAttributes.addFlashAttribute("error", paymentBlockedMessage(datPhong));
            return "redirect:/tai-khoan/lich-su-dat-phong/" + id;
        }

        paymentService.expireOverduePayments();
        Payment payment = paymentService.findByBookingId(id).orElse(null);
        if (payment == null) {
            redirectAttributes.addFlashAttribute("error", "Admin chưa tạo yêu cầu thanh toán cho đơn này.");
            return "redirect:/tai-khoan/lich-su-dat-phong/" + id;
        }
        if (Payment.HET_HAN.equals(payment.getStatus())) {
            redirectAttributes.addFlashAttribute("error", "Yêu cầu thanh toán đã hết hạn, vui lòng liên hệ admin.");
            return "redirect:/tai-khoan/lich-su-dat-phong/" + id;
        }
        List<CtDichVu> dichVuDaDung = ctDichVuRepository.findByCtDatPhong_MaCtDatPhong(id);
        InvoiceTotals totals = calculateTotals(datPhong, dichVuDaDung);

        model.addAttribute("datPhong", new BookingView(datPhong));
        model.addAttribute("payment", payment);
        model.addAttribute("dichVuDaDung", dichVuDaDung);
        model.addAttribute("tienPhong", totals.tienPhong());
        model.addAttribute("tongTienDichVu", totals.tongTienDichVu());
        model.addAttribute("tongCong", totals.tongCong());
        return "client/payment";
    }

    @PostMapping({"/tai-khoan/thanh-toan/{id}", "/thanh-toan/{id}"})
    public String payOnline(@PathVariable Integer id,
                            HttpSession session,
                            RedirectAttributes redirectAttributes) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        CtDatPhong datPhong = findCustomerBooking(id, khachHang);
        if (datPhong == null) {
            return "redirect:/tai-khoan/lich-su-dat-phong";
        }
        if (!canCustomerPay(datPhong)) {
            redirectAttributes.addFlashAttribute("error", paymentBlockedMessage(datPhong));
            return "redirect:/tai-khoan/lich-su-dat-phong/" + id;
        }

        try {
            paymentService.customerConfirmTransferred(id);
            redirectAttributes.addFlashAttribute("message", "Đã ghi nhận bạn đã chuyển khoản. Vui lòng chờ admin xác nhận thanh toán.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể cập nhật trạng thái thanh toán: " + ex.getMessage());
        }
        return "redirect:/tai-khoan/lich-su-dat-phong/" + id;
    }

    @GetMapping({"/tai-khoan/hoa-don/{id}", "/hoa-don-cua-toi/{id}"})
    public String invoiceDetail(@PathVariable Integer id,
                                Model model,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        KhachHang khachHang = ClientSession.currentCustomer(session);
        if (khachHang == null) {
            return "redirect:/dang-nhap";
        }

        CtDatPhong datPhong = findCustomerBooking(id, khachHang);
        if (datPhong == null) {
            return "redirect:/tai-khoan/lich-su-dat-phong";
        }

        if (!invoiceService.canPrintInvoice(id)) {
            redirectAttributes.addFlashAttribute("error", "Đơn này chưa thanh toán nên chưa thể in hóa đơn.");
            return "redirect:/tai-khoan/lich-su-dat-phong/" + id;
        }
        model.addAttribute("invoice", invoiceService.generateInvoiceData(id));
        return "payment/invoice";
    }

    private CtDatPhong findCustomerBooking(Integer id, KhachHang khachHang) {
        CtDatPhong datPhong = ctDatPhongRepository.findById(id).orElse(null);
        if (datPhong == null
                || datPhong.getKhachHang() == null
                || !khachHang.getMaKhachHang().equals(datPhong.getKhachHang().getMaKhachHang())) {
            return null;
        }
        return datPhong;
    }

    private java.util.Optional<HoaDon> findInvoice(Integer maCtDatPhong) {
        return hoaDonRepository.findFirstByCtDatPhong_MaCtDatPhongOrderByMaHoaDonDesc(maCtDatPhong);
    }

    private InvoiceTotals calculateTotals(CtDatPhong datPhong, List<CtDichVu> dichVuDaDung) {
        long tienPhong = datPhong.getTongTienPhong() == null ? 0 : datPhong.getTongTienPhong();
        int tongTienDichVu = dichVuDaDung.stream()
                .mapToInt(item -> item.getTongTienDichVu() == null ? 0 : item.getTongTienDichVu())
                .sum();
        return new InvoiceTotals(tienPhong, tongTienDichVu, tienPhong + tongTienDichVu);
    }

    private boolean canCustomerCancel(CtDatPhong datPhong) {
        if (datPhong == null) {
            return false;
        }
        String status = datPhong.getTrangThai();
        boolean cancelableStatus = status == null
                || status.isBlank()
                || "CHO_ADMIN_DUYET".equals(status)
                || "CHO_XAC_NHAN".equals(status)
                || "DA_XAC_NHAN".equals(status)
                || "CHO_THANH_TOAN".equals(status);
        boolean hasPaidInvoice = findInvoice(datPhong.getMaCtDatPhong())
                .map(hoaDon -> "DA_THANH_TOAN".equals(hoaDon.getTrangThai()))
                .orElse(false);
        boolean hasActivePayment = paymentService.findByBookingId(datPhong.getMaCtDatPhong())
                .map(payment -> Payment.CHO_THANH_TOAN.equals(payment.getStatus())
                        || Payment.CHO_XAC_NHAN.equals(payment.getStatus())
                        || Payment.DA_THANH_TOAN.equals(payment.getStatus()))
                .orElse(false);
        return cancelableStatus && !hasPaidInvoice && !hasActivePayment;
    }

    private boolean canCustomerPay(CtDatPhong datPhong) {
        if (datPhong == null || "DA_HUY".equals(datPhong.getTrangThai()) || "CHO_HUY".equals(datPhong.getTrangThai())) {
            return false;
        }
        String paymentStatus = datPhong.getPaymentStatus();
        return Payment.CHO_THANH_TOAN.equals(paymentStatus);
    }

    private String paymentBlockedMessage(CtDatPhong datPhong) {
        if (datPhong == null) {
            return "Khong tim thay don dat phong.";
        }
        if (Payment.CHO_XAC_NHAN.equals(datPhong.getPaymentStatus())) {
            return "Bạn đã báo chuyển khoản, vui lòng chờ admin xác nhận.";
        }
        if (Payment.DA_THANH_TOAN.equals(datPhong.getPaymentStatus())
                || "DA_THANH_TOAN".equals(datPhong.getTrangThai())
                || "DA_TRA_PHONG".equals(datPhong.getTrangThai())) {
            return "Đơn đặt phòng này đã thanh toán.";
        }
        if ("DA_HUY".equals(datPhong.getTrangThai())) {
            return "Don dat phong da huy nen khong the thanh toan.";
        }
        return "Don dat phong hien chua the thanh toan online.";
    }

    private record InvoiceTotals(long tienPhong, int tongTienDichVu, long tongCong) {
    }

    public static class BookingView {
        private final CtDatPhong datPhong;

        public BookingView(CtDatPhong datPhong) {
            this.datPhong = datPhong;
        }

        public Integer getMaCtDatPhong() {
            return datPhong.getMaCtDatPhong();
        }

        public LocalDate getNgayThucHien() {
            return datPhong.getNgayThucHien();
        }

        public LocalDate getNgayNhan() {
            return datPhong.getNgayNhan();
        }

        public LocalDate getNgayTra() {
            return datPhong.getNgayTra();
        }

        public com.example.do_an_java.entity.Phong getPhong() {
            return datPhong.getPhong();
        }

        public long getSoDem() {
            if (datPhong.getNgayNhan() == null
                    || datPhong.getNgayTra() == null
                    || datPhong.getNgayTra().isBefore(datPhong.getNgayNhan())) {
                return 0;
            }
            return Math.max(1, ChronoUnit.DAYS.between(datPhong.getNgayNhan(), datPhong.getNgayTra()));
        }

        public Long getTongTienPhong() {
            if (datPhong.getPhong() == null
                    || datPhong.getPhong().getLoaiPhong() == null
                    || datPhong.getPhong().getLoaiPhong().getGiaPhong() == null) {
                return null;
            }
            long soDem = getSoDem();
            return soDem <= 0 ? 0L : soDem * datPhong.getPhong().getLoaiPhong().getGiaPhong();
        }

        public String getTrangThaiHienThi() {
            return datPhong.getTrangThaiHienThi();
        }

        public String getTrangThai() {
            return datPhong.getTrangThai();
        }

        public boolean isCoTheHuy() {
            String status = datPhong.getTrangThai();
            return status == null
                    || status.isBlank()
                    || "CHO_ADMIN_DUYET".equals(status)
                    || "CHO_XAC_NHAN".equals(status)
                    || "DA_XAC_NHAN".equals(status);
        }

        public boolean isCoTheThanhToan() {
            if ("DA_HUY".equals(datPhong.getTrangThai()) || "CHO_HUY".equals(datPhong.getTrangThai())) {
                return false;
            }
            String paymentStatus = datPhong.getPaymentStatus();
            return Payment.CHO_THANH_TOAN.equals(paymentStatus);
        }
    }
}
