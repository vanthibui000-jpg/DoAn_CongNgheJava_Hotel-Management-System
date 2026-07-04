package com.example.do_an_java.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "DICHVU")
public class DichVu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaDichVu")
    private Integer maDichVu;

    @Column(name = "TenDichVu", length = 50)
    private String tenDichVu;

    @Column(name = "GiaDichVu")
    private Integer giaDichVu;

    @Column(name = "HinhAnh", length = 255)
    private String hinhAnh;

    public Integer getMaDichVu() { return maDichVu; }
    public void setMaDichVu(Integer maDichVu) { this.maDichVu = maDichVu; }

    public String getTenDichVu() { return tenDichVu; }
    public void setTenDichVu(String tenDichVu) { this.tenDichVu = tenDichVu; }

    public Integer getGiaDichVu() { return giaDichVu; }
    public void setGiaDichVu(Integer giaDichVu) { this.giaDichVu = giaDichVu; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public String getHinhAnhHienThi() {
        if (hinhAnh != null && !hinhAnh.isBlank()) {
            return hinhAnh;
        }
        if (maDichVu == null) {
            return null;
        }
        return switch (maDichVu) {
            case 1 -> "/Images/DichVu/BuaSang/service.jpg";
            case 2 -> "/Images/DichVu/BuaTrua/service.jpg";
            case 3 -> "/Images/DichVu/BuaToi/service.jpg";
            case 4 -> "/Images/DichVu/Massage/service.jpg";
            case 5 -> "/Images/DichVu/DuThuyen/service.jpg";
            default -> null;
        };
    }

    public String getTenDichVuHienThi() {
        return tenDichVu == null || tenDichVu.isBlank() ? "Dịch vụ" : tenDichVu;
    }
}
