package com.example.do_an_java.repository;

import com.example.do_an_java.entity.Phong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PhongRepository extends JpaRepository<Phong, Integer> {
    @Query("""
            SELECT p
            FROM Phong p
            WHERE p.loaiPhong IS NULL
               OR p.loaiPhong.daXoa IS NULL
               OR p.loaiPhong.daXoa = false
            ORDER BY p.maPhong
            """)
    List<Phong> findClientVisibleRooms();
}
