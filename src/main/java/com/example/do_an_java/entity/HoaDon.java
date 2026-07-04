package com.example.do_an_java.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "HOADON")
public class HoaDon {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaHoaDon")
    private Integer maHoaDon;

    @Column(name = "NgayThuTien")
    private LocalDate ngayThuTien;

    @Column(name = "SoTienThu")
    private Integer soTienThu;

    @ManyToOne
    @JoinColumn(name = "MaCTDatPhong")
    private CtDatPhong ctDatPhong;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    public Integer getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(Integer maHoaDon) { this.maHoaDon = maHoaDon; }

    public LocalDate getNgayThuTien() { return ngayThuTien; }
    public void setNgayThuTien(LocalDate ngayThuTien) { this.ngayThuTien = ngayThuTien; }

    public Integer getSoTienThu() { return soTienThu; }
    public void setSoTienThu(Integer soTienThu) { this.soTienThu = soTienThu; }

    public CtDatPhong getCtDatPhong() { return ctDatPhong; }
    public void setCtDatPhong(CtDatPhong ctDatPhong) { this.ctDatPhong = ctDatPhong; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    @Column(name = "TrangThai")
    private String trangThai;

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }
}
