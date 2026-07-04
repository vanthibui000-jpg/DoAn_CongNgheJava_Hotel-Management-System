package com.example.do_an_java.service;

import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.CtDichVu;
import com.example.do_an_java.entity.HoaDon;
import com.example.do_an_java.entity.Payment;
import com.example.do_an_java.entity.Phong;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.CtDichVuRepository;
import com.example.do_an_java.repository.HoaDonRepository;
import com.example.do_an_java.repository.PaymentRepository;
import com.example.do_an_java.repository.PhongRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@PropertySource(value = "classpath:config/payment.properties", ignoreResourceNotFound = true)
public class PaymentService {
    private final PaymentRepository paymentRepository;
    private final CtDatPhongRepository ctDatPhongRepository;
    private final CtDichVuRepository ctDichVuRepository;
    private final HoaDonRepository hoaDonRepository;
    private final PhongRepository phongRepository;
    private final VietQRService vietQRService;

    @Value("${payment.expireMinutes:5}")
    private long expireMinutes;

    public PaymentService(PaymentRepository paymentRepository,
                          CtDatPhongRepository ctDatPhongRepository,
                          CtDichVuRepository ctDichVuRepository,
                          HoaDonRepository hoaDonRepository,
                          PhongRepository phongRepository,
                          VietQRService vietQRService) {
        this.paymentRepository = paymentRepository;
        this.ctDatPhongRepository = ctDatPhongRepository;
        this.ctDichVuRepository = ctDichVuRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.phongRepository = phongRepository;
        this.vietQRService = vietQRService;
    }

    @Transactional
    public Payment approveBookingAndCreatePayment(int bookingId) {
        CtDatPhong booking = ctDatPhongRepository.findById(bookingId).orElseThrow();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiredAt = now.plusMinutes(expireMinutes);
        BigDecimal amount = calculateTotal(booking);

        Payment payment = paymentRepository.findFirstByBooking_MaCtDatPhongOrderByIdDesc(bookingId)
                .orElseGet(Payment::new);
        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setMethod(Payment.METHOD_VIETQR);
        payment.setStatus(Payment.CHO_THANH_TOAN);
        payment.setTransferContent(vietQRService.generateTransferContent(bookingId));
        payment.setQrUrl(vietQRService.generateQrUrl(bookingId, amount));
        payment.setPaymentRequestedAt(now);
        payment.setExpiredAt(expiredAt);
        payment.setPaidAt(null);
        payment.setCancelledAt(null);
        Payment saved = paymentRepository.save(payment);

        booking.setTrangThai("CHO_THANH_TOAN");
        booking.setPaymentStatus(Payment.CHO_THANH_TOAN);
        booking.setPaymentRequestedAt(now);
        booking.setPaymentExpiredAt(expiredAt);
        ctDatPhongRepository.save(booking);
        return saved;
    }

    public Optional<Payment> findByBookingId(int bookingId) {
        return paymentRepository.findFirstByBooking_MaCtDatPhongOrderByIdDesc(bookingId);
    }

    @Transactional
    public Payment customerConfirmTransferred(int bookingId) {
        expireOverduePayments();
        Payment payment = paymentRepository.findFirstByBooking_MaCtDatPhongOrderByIdDesc(bookingId)
                .orElseThrow();
        if (Payment.HET_HAN.equals(payment.getStatus()) || isExpired(payment)) {
            markExpired(payment);
            throw new IllegalStateException("Yeu cau thanh toan da het han, vui long lien he admin.");
        }
        if (!Payment.CHO_THANH_TOAN.equals(payment.getStatus())) {
            return payment;
        }

        payment.setStatus(Payment.CHO_XAC_NHAN);
        Payment saved = paymentRepository.save(payment);
        CtDatPhong booking = payment.getBooking();
        if (booking != null) {
            booking.setTrangThai("CHO_XAC_NHAN_THANH_TOAN");
            booking.setPaymentStatus(Payment.CHO_XAC_NHAN);
            ctDatPhongRepository.save(booking);
        }
        return saved;
    }

    @Transactional
    public Payment adminConfirmPaid(int bookingId) {
        expireOverduePayments();
        Payment payment = paymentRepository.findFirstByBooking_MaCtDatPhongOrderByIdDesc(bookingId)
                .orElseThrow();
        if (!Payment.CHO_XAC_NHAN.equals(payment.getStatus())) {
            throw new IllegalStateException("Chi xac nhan tien cho payment dang cho xac nhan.");
        }

        payment.setStatus(Payment.DA_THANH_TOAN);
        payment.setPaidAt(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);

        CtDatPhong booking = payment.getBooking();
        if (booking != null) {
            booking.setTrangThai("DA_THANH_TOAN");
            booking.setPaymentStatus(Payment.DA_THANH_TOAN);
            ctDatPhongRepository.save(booking);
            createOrUpdatePaidInvoice(booking, payment);
        }
        return saved;
    }

    @Transactional
    public void expireOverduePayments() {
        List<Payment> waiting = paymentRepository.findByStatusOrderByCreatedAtDesc(Payment.CHO_THANH_TOAN);
        for (Payment payment : waiting) {
            if (isExpired(payment)) {
                markExpired(payment);
            }
        }
    }

    @Transactional
    public void cancelExpiredBooking(int bookingId) {
        Payment payment = paymentRepository.findFirstByBooking_MaCtDatPhongOrderByIdDesc(bookingId)
                .orElseThrow();
        CtDatPhong booking = payment.getBooking();
        if (Payment.DA_THANH_TOAN.equals(payment.getStatus()) || Payment.CHO_XAC_NHAN.equals(payment.getStatus())) {
            throw new IllegalStateException("Khong the huy don da thanh toan hoac dang cho xac nhan tien.");
        }
        if (isExpired(payment)) {
            markExpired(payment);
        }
        boolean overdue = Payment.HET_HAN.equals(payment.getStatus())
                || (booking != null && "QUA_HAN_THANH_TOAN".equals(booking.getTrangThai()));
        if (!overdue) {
            throw new IllegalStateException("Chi co the huy don da qua han thanh toan.");
        }
        payment.setStatus(Payment.DA_HUY);
        payment.setCancelledAt(LocalDateTime.now());
        paymentRepository.save(payment);

        if (booking != null) {
            booking.setTrangThai("DA_HUY");
            booking.setPaymentStatus(Payment.DA_HUY);
            ctDatPhongRepository.save(booking);
            releaseRoomIfBookingCancelled(booking);
        }
    }

    @Transactional
    public void cancelPayment(int bookingId) {
        Optional<Payment> existingPayment = paymentRepository.findFirstByBooking_MaCtDatPhongOrderByIdDesc(bookingId);
        existingPayment.ifPresent(payment -> {
            payment.setStatus(Payment.DA_HUY);
            payment.setCancelledAt(LocalDateTime.now());
            paymentRepository.save(payment);
        });

        CtDatPhong booking = existingPayment
                .map(Payment::getBooking)
                .orElseGet(() -> ctDatPhongRepository.findById(bookingId).orElse(null));
        if (booking != null) {
            booking.setTrangThai("DA_HUY");
            booking.setPaymentStatus(Payment.DA_HUY);
            ctDatPhongRepository.save(booking);
            releaseRoomIfBookingCancelled(booking);
        }
    }

    public List<Payment> getPendingPayments() {
        expireOverduePayments();
        return paymentRepository.findByStatusInOrderByCreatedAtDesc(List.of(Payment.CHO_THANH_TOAN, Payment.CHO_XAC_NHAN));
    }

    public List<Payment> getPaymentsByStatus(String status) {
        expireOverduePayments();
        return paymentRepository.findByStatusOrderByCreatedAtDesc(status);
    }

    public List<Payment> getAllPayments() {
        expireOverduePayments();
        return paymentRepository.findAllByOrderByCreatedAtDesc();
    }

    public BigDecimal calculateTotal(CtDatPhong booking) {
        long tienPhong = booking.getTongTienPhong() == null ? 0 : booking.getTongTienPhong();
        int tienDichVu = ctDichVuRepository.findByCtDatPhong_MaCtDatPhong(booking.getMaCtDatPhong()).stream()
                .mapToInt(item -> item.getTongTienDichVu() == null ? 0 : item.getTongTienDichVu())
                .sum();
        return BigDecimal.valueOf(tienPhong + tienDichVu);
    }

    public long getExpireMinutes() {
        return expireMinutes;
    }

    private boolean isExpired(Payment payment) {
        return payment.getExpiredAt() != null
                && LocalDateTime.now().isAfter(payment.getExpiredAt())
                && Payment.CHO_THANH_TOAN.equals(payment.getStatus());
    }

    private void markExpired(Payment payment) {
        payment.setStatus(Payment.HET_HAN);
        paymentRepository.save(payment);
        CtDatPhong booking = payment.getBooking();
        if (booking != null) {
            booking.setTrangThai("QUA_HAN_THANH_TOAN");
            booking.setPaymentStatus(Payment.HET_HAN);
            ctDatPhongRepository.save(booking);
        }
    }

    private void releaseRoomIfBookingCancelled(CtDatPhong booking) {
        if (booking.getPhong() == null || booking.getPhong().getMaPhong() == null) {
            return;
        }
        List<CtDatPhong> activeBookings = ctDatPhongRepository.findActiveBookingsByRoom(booking.getPhong().getMaPhong());
        if (activeBookings.isEmpty()) {
            Phong phong = booking.getPhong();
            phong.setTrangThai("Trống");
            phongRepository.save(phong);
        }
    }

    private void createOrUpdatePaidInvoice(CtDatPhong booking, Payment payment) {
        HoaDon hoaDon = hoaDonRepository.findFirstByCtDatPhong_MaCtDatPhongOrderByMaHoaDonDesc(booking.getMaCtDatPhong())
                .orElseGet(HoaDon::new);
        hoaDon.setCtDatPhong(booking);
        hoaDon.setNgayThuTien(LocalDate.now());
        hoaDon.setSoTienThu(payment.getAmount() == null ? 0 : payment.getAmount().intValue());
        hoaDon.setTrangThai(Payment.DA_THANH_TOAN);
        hoaDonRepository.save(hoaDon);
    }
}
