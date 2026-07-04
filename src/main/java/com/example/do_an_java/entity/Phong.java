package com.example.do_an_java.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "PHONG")
public class Phong {
    @Id
    @Column(name = "MaPhong")
    private Integer maPhong;

    @ManyToOne
    @JoinColumn(name = "MaLoaiPhong")
    private LoaiPhong loaiPhong;

    @Column(name = "TrangThai", length = 30)
    private String trangThai;

    @Column(name = "HinhAnhDaiDien", length = 255)
    private String hinhAnhDaiDien;

    public Integer getMaPhong() { return maPhong; }
    public void setMaPhong(Integer maPhong) { this.maPhong = maPhong; }

    public LoaiPhong getLoaiPhong() { return loaiPhong; }
    public void setLoaiPhong(LoaiPhong loaiPhong) { this.loaiPhong = loaiPhong; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getHinhAnhDaiDien() { return hinhAnhDaiDien; }
    public void setHinhAnhDaiDien(String hinhAnhDaiDien) { this.hinhAnhDaiDien = hinhAnhDaiDien; }

    public String getHinhAnhDaiDienHienThi() {
        if (hinhAnhDaiDien != null && !hinhAnhDaiDien.isBlank()) {
            return hinhAnhDaiDien;
        }
        if (!hasDefaultRoomImages()) {
            return null;
        }
        return "/Images/ChiTietPhong/Phong" + maPhong + "/01.jpg";
    }

    private boolean hasDefaultRoomImages() {
        if (maPhong == null) {
            return false;
        }
        int floor = maPhong / 100;
        int roomIndex = maPhong % 100;
        return floor >= 1 && floor <= 3 && roomIndex >= 1 && roomIndex <= 5;
    }
}
