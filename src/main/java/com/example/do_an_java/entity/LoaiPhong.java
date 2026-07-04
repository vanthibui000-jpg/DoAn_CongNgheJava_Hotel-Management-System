package com.example.do_an_java.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "LOAIPHONG")
public class LoaiPhong {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaLoaiPhong")
    private Integer maLoaiPhong;

    @Column(name = "TenLoaiPhong", length = 20)
    private String tenLoaiPhong;

    @Column(name = "GiaPhong")
    private Integer giaPhong;

    @Column(name = "MoTa", length = 255)
    private String moTa;

    @Column(name = "DaXoa")
    private Boolean daXoa = false;

    public Integer getMaLoaiPhong() { return maLoaiPhong; }
    public void setMaLoaiPhong(Integer maLoaiPhong) { this.maLoaiPhong = maLoaiPhong; }

    public String getTenLoaiPhong() { return tenLoaiPhong; }
    public void setTenLoaiPhong(String tenLoaiPhong) { this.tenLoaiPhong = tenLoaiPhong; }

    public Integer getGiaPhong() { return giaPhong; }
    public void setGiaPhong(Integer giaPhong) { this.giaPhong = giaPhong; }

    public String getMoTa() { return moTa; }
    public void setMoTa(String moTa) { this.moTa = moTa; }

    public Boolean getDaXoa() { return daXoa; }
    public void setDaXoa(Boolean daXoa) { this.daXoa = daXoa; }
}
