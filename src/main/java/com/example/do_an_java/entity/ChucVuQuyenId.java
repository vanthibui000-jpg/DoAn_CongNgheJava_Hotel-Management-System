package com.example.do_an_java.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class ChucVuQuyenId implements Serializable {
    @Column(name = "MaChucVu")
    private Integer maChucVu;

    @Column(name = "MaQuyen", length = 50)
    private String maQuyen;

    public ChucVuQuyenId() {
    }

    public ChucVuQuyenId(Integer maChucVu, String maQuyen) {
        this.maChucVu = maChucVu;
        this.maQuyen = maQuyen;
    }

    public Integer getMaChucVu() {
        return maChucVu;
    }

    public void setMaChucVu(Integer maChucVu) {
        this.maChucVu = maChucVu;
    }

    public String getMaQuyen() {
        return maQuyen;
    }

    public void setMaQuyen(String maQuyen) {
        this.maQuyen = maQuyen;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChucVuQuyenId that)) return false;
        return Objects.equals(maChucVu, that.maChucVu)
                && Objects.equals(maQuyen, that.maQuyen);
    }

    @Override
    public int hashCode() {
        return Objects.hash(maChucVu, maQuyen);
    }
}
