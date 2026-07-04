package com.example.do_an_java.service;

import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.CtDichVu;
import com.example.do_an_java.entity.Payment;
import com.example.do_an_java.repository.CtDatPhongRepository;
import com.example.do_an_java.repository.CtDichVuRepository;
import com.example.do_an_java.repository.PaymentRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class InvoiceService {
    private final CtDatPhongRepository ctDatPhongRepository;
    private final CtDichVuRepository ctDichVuRepository;
    private final PaymentRepository paymentRepository;

    public InvoiceService(CtDatPhongRepository ctDatPhongRepository,
                          CtDichVuRepository ctDichVuRepository,
                          PaymentRepository paymentRepository) {
        this.ctDatPhongRepository = ctDatPhongRepository;
        this.ctDichVuRepository = ctDichVuRepository;
        this.paymentRepository = paymentRepository;
    }

    public boolean canPrintInvoice(int bookingId) {
        return paymentRepository.findFirstByBooking_MaCtDatPhongOrderByIdDesc(bookingId)
                .map(payment -> Payment.DA_THANH_TOAN.equals(payment.getStatus()))
                .orElse(false);
    }

    public InvoiceData generateInvoiceData(int bookingId) {
        CtDatPhong booking = ctDatPhongRepository.findById(bookingId).orElseThrow();
        Payment payment = paymentRepository.findFirstByBooking_MaCtDatPhongOrderByIdDesc(bookingId).orElseThrow();
        List<CtDichVu> services = ctDichVuRepository.findByCtDatPhong_MaCtDatPhong(bookingId);
        long roomTotal = booking.getTongTienPhong() == null ? 0 : booking.getTongTienPhong();
        int serviceTotal = services.stream()
                .mapToInt(item -> item.getTongTienDichVu() == null ? 0 : item.getTongTienDichVu())
                .sum();
        return new InvoiceData(
                booking,
                payment,
                services,
                BigDecimal.valueOf(roomTotal),
                BigDecimal.valueOf(serviceTotal),
                BigDecimal.valueOf(roomTotal + serviceTotal)
        );
    }

    public void printInvoice(int bookingId) {
        if (!canPrintInvoice(bookingId)) {
            throw new IllegalStateException("Don nay chua thanh toan nen chua the in hoa don.");
        }
    }

    public record InvoiceData(CtDatPhong booking,
                              Payment payment,
                              List<CtDichVu> services,
                              BigDecimal roomTotal,
                              BigDecimal serviceTotal,
                              BigDecimal grandTotal) {
    }
}
