package com.example.do_an_java.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

@Entity
@Table(name = "CHUCVU_QUYEN")
public class ChucVuQuyen {
    @EmbeddedId
    private ChucVuQuyenId id = new ChucVuQuyenId();

    @ManyToOne
    @MapsId("maChucVu")
    @JoinColumn(name = "MaChucVu")
    private ChucVu chucVu;

    public ChucVuQuyen() {
    }

    public ChucVuQuyen(ChucVu chucVu, String maQuyen) {
        setChucVu(chucVu);
        this.id.setMaQuyen(maQuyen);
    }

    public ChucVuQuyenId getId() {
        return id;
    }

    public void setId(ChucVuQuyenId id) {
        this.id = id;
    }

    public ChucVu getChucVu() {
        return chucVu;
    }

    public void setChucVu(ChucVu chucVu) {
        this.chucVu = chucVu;
        if (chucVu != null) {
            this.id.setMaChucVu(chucVu.getMaChucVu());
        }
    }

    public String getMaQuyen() {
        return id == null ? null : id.getMaQuyen();
    }
}
