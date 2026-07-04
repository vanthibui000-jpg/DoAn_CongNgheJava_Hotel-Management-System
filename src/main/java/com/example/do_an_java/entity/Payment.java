package com.example.do_an_java.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "PAYMENTS")
public class Payment {
    public static final String METHOD_VIETQR = "VIETQR";
    public static final String CHUA_TAO = "CHUA_TAO";
    public static final String CHO_THANH_TOAN = "CHO_THANH_TOAN";
    public static final String CHO_XAC_NHAN = "CHO_XAC_NHAN";
    public static final String DA_THANH_TOAN = "DA_THANH_TOAN";
    public static final String HET_HAN = "HET_HAN";
    public static final String DA_HUY = "DA_HUY";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "booking_id", nullable = false)
    private CtDatPhong booking;

    @Column(name = "amount", precision = 12, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "method", length = 30)
    private String method = METHOD_VIETQR;

    @Column(name = "status", length = 30)
    private String status = CHO_THANH_TOAN;

    @Column(name = "qr_url", columnDefinition = "TEXT")
    private String qrUrl;

    @Column(name = "transfer_content", length = 50)
    private String transferContent;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "payment_requested_at")
    private LocalDateTime paymentRequestedAt;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (method == null || method.isBlank()) {
            method = METHOD_VIETQR;
        }
        if (status == null || status.isBlank()) {
            status = CHO_THANH_TOAN;
        }
    }

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public CtDatPhong getBooking() { return booking; }
    public void setBooking(CtDatPhong booking) { this.booking = booking; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getQrUrl() { return qrUrl; }
    public void setQrUrl(String qrUrl) { this.qrUrl = qrUrl; }

    public String getTransferContent() { return transferContent; }
    public void setTransferContent(String transferContent) { this.transferContent = transferContent; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getPaymentRequestedAt() { return paymentRequestedAt; }
    public void setPaymentRequestedAt(LocalDateTime paymentRequestedAt) { this.paymentRequestedAt = paymentRequestedAt; }

    public LocalDateTime getExpiredAt() { return expiredAt; }
    public void setExpiredAt(LocalDateTime expiredAt) { this.expiredAt = expiredAt; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCancelledAt() { return cancelledAt; }
    public void setCancelledAt(LocalDateTime cancelledAt) { this.cancelledAt = cancelledAt; }

    public String getStatusHienThi() {
        if (status == null || status.isBlank() || CHUA_TAO.equals(status)) {
            return "Chưa tạo";
        }
        return switch (status) {
            case CHO_THANH_TOAN -> "Chờ thanh toán";
            case CHO_XAC_NHAN -> "Chờ xác nhận";
            case DA_THANH_TOAN -> "Đã thanh toán";
            case HET_HAN -> "Hết hạn";
            case DA_HUY -> "Đã hủy";
            default -> status;
        };
    }
}
