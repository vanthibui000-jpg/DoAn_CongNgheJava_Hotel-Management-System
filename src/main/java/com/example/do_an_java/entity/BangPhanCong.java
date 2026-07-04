package com.example.do_an_java.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "BANGPHANCONG")
public class BangPhanCong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaPhanCong")
    private Integer maPhanCong;

    @Column(name = "NgayPhanCong")
    private LocalDate ngayPhanCong;

    @Column(name = "LoaiCongViec", length = 50)
    private String loaiCongViec;

    @ManyToOne
    @JoinColumn(name = "MaNhanVien")
    private NhanVien nhanVien;

    public Integer getMaPhanCong() { return maPhanCong; }
    public void setMaPhanCong(Integer maPhanCong) { this.maPhanCong = maPhanCong; }

    public LocalDate getNgayPhanCong() { return ngayPhanCong; }
    public void setNgayPhanCong(LocalDate ngayPhanCong) { this.ngayPhanCong = ngayPhanCong; }

    public String getLoaiCongViec() { return loaiCongViec; }
    public void setLoaiCongViec(String loaiCongViec) { this.loaiCongViec = loaiCongViec; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }
}
