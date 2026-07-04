package com.example.do_an_java.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "CTDICHVU")
public class CtDichVu {
    @EmbeddedId
    private CtDichVuId id = new CtDichVuId();

    @Column(name = "MaCTDichVu")
    private Integer maCtDichVu;

    @ManyToOne
    @MapsId("maDichVu")
    @JoinColumn(name = "MaDichVu")
    private DichVu dichVu;

    @ManyToOne
    @MapsId("maCtDatPhong")
    @JoinColumn(name = "MaCTDatPhong")
    private CtDatPhong ctDatPhong;

    @Column(name = "SoLuong")
    private Integer soLuong;

    @Column(name = "TongTienDichVu")
    private Integer tongTienDichVu;

    public CtDichVuId getId() { return id; }
    public void setId(CtDichVuId id) { this.id = id; }

    public Integer getMaCtDichVu() { return maCtDichVu; }
    public void setMaCtDichVu(Integer maCtDichVu) { this.maCtDichVu = maCtDichVu; }

    public DichVu getDichVu() { return dichVu; }
    public void setDichVu(DichVu dichVu) {
        this.dichVu = dichVu;
        if (dichVu != null) this.id.setMaDichVu(dichVu.getMaDichVu());
    }

    public CtDatPhong getCtDatPhong() { return ctDatPhong; }
    public void setCtDatPhong(CtDatPhong ctDatPhong) {
        this.ctDatPhong = ctDatPhong;
        if (ctDatPhong != null) this.id.setMaCtDatPhong(ctDatPhong.getMaCtDatPhong());
    }

    public Integer getSoLuong() { return soLuong; }
    public void setSoLuong(Integer soLuong) { this.soLuong = soLuong; }

    public Integer getTongTienDichVu() { return tongTienDichVu; }
    public void setTongTienDichVu(Integer tongTienDichVu) { this.tongTienDichVu = tongTienDichVu; }
}
