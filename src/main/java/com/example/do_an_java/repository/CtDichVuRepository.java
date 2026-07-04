package com.example.do_an_java.repository;

import com.example.do_an_java.entity.CtDichVu;
import com.example.do_an_java.entity.CtDichVuId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CtDichVuRepository
        extends JpaRepository<CtDichVu, CtDichVuId> {

    List<CtDichVu> findByCtDatPhong_MaCtDatPhong(
            Integer maCtDatPhong);

    @Query("SELECT COALESCE(MAX(c.maCtDichVu), 0) FROM CtDichVu c")
    Integer findMaxMaCtDichVu();

    boolean existsByMaCtDichVu(Integer maCtDichVu);
}
