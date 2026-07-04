package com.example.do_an_java.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "CHUCVU")
public class ChucVu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MaChucVu")
    private Integer maChucVu;

    @Column(name = "TenChucVu")
    private String tenChucVu;

    public ChucVu() {
    }

    public ChucVu(Integer maChucVu) {
        this.maChucVu = maChucVu;
    }

    public Integer getMaChucVu() {
        return maChucVu;
    }

    public void setMaChucVu(Integer maChucVu) {
        this.maChucVu = maChucVu;
    }

    public String getTenChucVu() {
        return tenChucVu;
    }

    public void setTenChucVu(String tenChucVu) {
        this.tenChucVu = tenChucVu;
    }
}
