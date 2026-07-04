package com.example.do_an_java.repository;

import com.example.do_an_java.entity.KhachHang;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface KhachHangRepository extends JpaRepository<KhachHang, Integer> {
    List<KhachHang> findByHoTenContainingIgnoreCase(String keyword);

    List<KhachHang> findByCmndContainingIgnoreCase(String keyword);

    List<KhachHang> findByDienThoaiContainingIgnoreCase(String keyword);

    Optional<KhachHang> findByEmailIgnoreCase(String email);

    Optional<KhachHang> findByEmailIgnoreCaseAndMatKhau(String email, String matKhau);

    @Query("SELECT COALESCE(MAX(k.maKhachHang), 0) FROM KhachHang k")
    Integer findMaxMaKhachHang();
}
