package com.example.do_an_java.repository;

import com.example.do_an_java.entity.ChucVuQuyen;
import com.example.do_an_java.entity.ChucVuQuyenId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChucVuQuyenRepository extends JpaRepository<ChucVuQuyen, ChucVuQuyenId> {
    List<ChucVuQuyen> findByChucVu_MaChucVu(Integer maChucVu);

    void deleteByChucVu_MaChucVu(Integer maChucVu);
}
