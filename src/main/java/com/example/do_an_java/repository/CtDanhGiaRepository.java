package com.example.do_an_java.repository;

import com.example.do_an_java.entity.CtDanhGia;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CtDanhGiaRepository extends JpaRepository<CtDanhGia, Integer> {
    List<CtDanhGia> findByPhong_MaPhongOrderByNgayDanhGiaDesc(Integer maPhong);

    List<CtDanhGia> findAllByOrderByNgayDanhGiaDesc();
}
