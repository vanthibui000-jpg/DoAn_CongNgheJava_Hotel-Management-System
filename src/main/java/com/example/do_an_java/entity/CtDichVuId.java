package com.example.do_an_java.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class CtDichVuId implements Serializable {
    @Column(name = "MaDichVu")
    private Integer maDichVu;

    @Column(name = "MaCTDatPhong")
    private Integer maCtDatPhong;

    public CtDichVuId() {
    }

    public CtDichVuId(Integer maDichVu, Integer maCtDatPhong) {
        this.maDichVu = maDichVu;
        this.maCtDatPhong = maCtDatPhong;
    }

    public Integer getMaDichVu() { return maDichVu; }
    public void setMaDichVu(Integer maDichVu) { this.maDichVu = maDichVu; }

    public Integer getMaCtDatPhong() { return maCtDatPhong; }
    public void setMaCtDatPhong(Integer maCtDatPhong) { this.maCtDatPhong = maCtDatPhong; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CtDichVuId that)) return false;
        return Objects.equals(maDichVu, that.maDichVu)
                && Objects.equals(maCtDatPhong, that.maCtDatPhong);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maDichVu, maCtDatPhong);
    }
}
