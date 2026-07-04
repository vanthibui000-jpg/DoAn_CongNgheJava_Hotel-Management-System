package com.example.do_an_java.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "CTDATPHONG")
public class CtDatPhong {
    @Id
    @Column(name = "MaCTDatPhong")
    private Integer maCtDatPhong;

    @Column(name = "NgayThucHien")
    private LocalDate ngayThucHien;

    @ManyToOne
    @JoinColumn(name = "MaKhachHang")
    private KhachHang khachHang;

    @Column(name = "NgayNhan")
    private LocalDate ngayNhan;

    @Column(name = "NgayTra")
    private LocalDate ngayTra;

    @ManyToOne
    @JoinColumn(name = "MaPhong")
    private Phong phong;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    @Column(name = "TrangThai", length = 50)
    private String trangThai;

    @Column(name = "payment_status", length = 50)
    private String paymentStatus;

    @Column(name = "payment_requested_at")
    private LocalDateTime paymentRequestedAt;

    @Column(name = "payment_expired_at")
    private LocalDateTime paymentExpiredAt;

    public Integer getMaCtDatPhong() { return maCtDatPhong; }
    public void setMaCtDatPhong(Integer maCtDatPhong) { this.maCtDatPhong = maCtDatPhong; }

    public LocalDate getNgayThucHien() { return ngayThucHien; }
    public void setNgayThucHien(LocalDate ngayThucHien) { this.ngayThucHien = ngayThucHien; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public LocalDate getNgayNhan() { return ngayNhan; }
    public void setNgayNhan(LocalDate ngayNhan) { this.ngayNhan = ngayNhan; }

    public LocalDate getNgayTra() { return ngayTra; }
    public void setNgayTra(LocalDate ngayTra) { this.ngayTra = ngayTra; }

    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public LocalDateTime getPaymentRequestedAt() { return paymentRequestedAt; }
    public void setPaymentRequestedAt(LocalDateTime paymentRequestedAt) { this.paymentRequestedAt = paymentRequestedAt; }

    public LocalDateTime getPaymentExpiredAt() { return paymentExpiredAt; }
    public void setPaymentExpiredAt(LocalDateTime paymentExpiredAt) { this.paymentExpiredAt = paymentExpiredAt; }

    public String getPaymentStatusHienThi() {
        if (paymentStatus == null || paymentStatus.isBlank() || "CHUA_TAO".equals(paymentStatus)) {
            return "Chưa tạo";
        }
        return switch (paymentStatus) {
            case "CHUA_THANH_TOAN" -> "Chưa thanh toán";
            case "CHO_THANH_TOAN" -> "Chờ thanh toán";
            case "CHO_XAC_NHAN" -> "Chờ xác nhận";
            case "DA_THANH_TOAN" -> "Đã thanh toán";
            case "HET_HAN" -> "Hết hạn";
            case "DA_HUY" -> "Đã hủy";
            default -> paymentStatus;
        };
    }

    public String getTrangThaiHienThi() {
        if (trangThai == null || trangThai.isBlank()) {
            return "Chờ admin duyệt";
        }
        return switch (trangThai) {
            case "CHO_ADMIN_DUYET" -> "Chờ admin duyệt";
            case "CHO_THANH_TOAN" -> "Chờ thanh toán";
            case "CHO_XAC_NHAN_THANH_TOAN" -> "Chờ xác nhận thanh toán";
            case "QUA_HAN_THANH_TOAN" -> "Quá hạn thanh toán";
            case "CHO_XAC_NHAN" -> "Chờ xác nhận";
            case "CHO_HUY" -> "Chờ duyệt hủy";
            case "DA_XAC_NHAN" -> "Đã xác nhận";
            case "DA_HUY" -> "Đã hủy";
            case "DA_THANH_TOAN" -> "Đã thanh toán";
            case "DA_TRA_PHONG" -> "Đã trả phòng";
            default -> "Chờ admin duyệt";
        };
    }

    public long getSoDem() {
        if (ngayNhan == null || ngayTra == null || ngayTra.isBefore(ngayNhan)) {
            return 0;
        }
        return Math.max(1, ChronoUnit.DAYS.between(ngayNhan, ngayTra));
    }

    public Long getTongTienPhong() {
        if (phong == null || phong.getLoaiPhong() == null || phong.getLoaiPhong().getGiaPhong() == null) {
            return null;
        }
        long soDem = getSoDem();
        return soDem <= 0 ? 0L : soDem * phong.getLoaiPhong().getGiaPhong();
    }
}
