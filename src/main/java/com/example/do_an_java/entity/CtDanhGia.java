package com.example.do_an_java.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "CTDANHGIA")
public class CtDanhGia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Integer id;

    @Column(name = "NoiDung", length = 255)
    private String noiDung;

    @Column(name = "NgayDanhGia")
    private LocalDate ngayDanhGia;

    @ManyToOne
    @JoinColumn(name = "MaPhong")
    private Phong phong;

    @ManyToOne
    @JoinColumn(name = "MaKhachHang")
    private KhachHang khachHang;

    @ManyToOne
    @JoinColumn(name = "MaCTDatPhong")
    private CtDatPhong ctDatPhong;

    @Column(name = "SoSao")
    private Integer soSao = 5;

    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }

    public String getNoiDung() { return noiDung; }
    public void setNoiDung(String noiDung) { this.noiDung = noiDung; }

    public LocalDate getNgayDanhGia() { return ngayDanhGia; }
    public void setNgayDanhGia(LocalDate ngayDanhGia) { this.ngayDanhGia = ngayDanhGia; }

    public Phong getPhong() { return phong; }
    public void setPhong(Phong phong) { this.phong = phong; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public CtDatPhong getCtDatPhong() { return ctDatPhong; }
    public void setCtDatPhong(CtDatPhong ctDatPhong) { this.ctDatPhong = ctDatPhong; }

    public Integer getSoSao() { return soSao; }
    public void setSoSao(Integer soSao) { this.soSao = soSao; }
}
