package com.example.do_an_java.repository;

import com.example.do_an_java.entity.BangPhanCong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BangPhanCongRepository
        extends JpaRepository<BangPhanCong, Integer> {

    List<BangPhanCong> findByLoaiCongViecContainingIgnoreCase(
            String keyword
    );

    List<BangPhanCong> findByNgayPhanCong(
            LocalDate ngayPhanCong
    );

    List<BangPhanCong> findByLoaiCongViecContainingIgnoreCaseAndNgayPhanCong(
            String keyword,
            LocalDate ngayPhanCong
    );
}