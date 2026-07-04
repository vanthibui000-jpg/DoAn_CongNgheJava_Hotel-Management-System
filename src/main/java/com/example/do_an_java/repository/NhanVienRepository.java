package com.example.do_an_java.repository;

import com.example.do_an_java.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface NhanVienRepository extends JpaRepository<NhanVien, Integer> {

    // Đăng nhập
    Optional<NhanVien> findByUsernameAndPassword(
            String username,
            String password
    );

    // Tìm theo tên
    List<NhanVien> findByTenNhanVienContainingIgnoreCase(
            String tenNhanVien
    );

    // Câu 1: Tìm kiếm theo tên, username, chức vụ
    @Query("""
            SELECT n
            FROM NhanVien n
            LEFT JOIN n.chucVu c
            WHERE LOWER(n.tenNhanVien) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(n.username) LIKE LOWER(CONCAT('%', :keyword, '%'))
               OR LOWER(c.tenChucVu) LIKE LOWER(CONCAT('%', :keyword, '%'))
            """)
    List<NhanVien> search(
            @Param("keyword") String keyword
    );

    // Hỗ trợ kiểm tra username tồn tại
    Optional<NhanVien> findByUsername(
            String username
    );

    Optional<NhanVien> findByUsernameIgnoreCase(
            String username
    );
}
