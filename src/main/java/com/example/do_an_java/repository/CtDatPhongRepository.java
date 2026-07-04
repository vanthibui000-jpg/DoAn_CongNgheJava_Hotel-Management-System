package com.example.do_an_java.repository;

import com.example.do_an_java.entity.CtDatPhong;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CtDatPhongRepository extends JpaRepository<CtDatPhong, Integer> {
    List<CtDatPhong> findByKhachHang_MaKhachHang(Integer maKhachHang);

    List<CtDatPhong> findByKhachHang_MaKhachHangOrderByNgayThucHienDesc(Integer maKhachHang);

    List<CtDatPhong> findByTrangThai(String trangThai);

    List<CtDatPhong> findAllByOrderByNgayThucHienDesc();

    @Query("""
            SELECT c
            FROM CtDatPhong c
            WHERE c.phong.maPhong = :maPhong
              AND (c.trangThai IS NULL OR c.trangThai <> 'DA_HUY')
            """)
    List<CtDatPhong> findActiveBookingsByRoom(@Param("maPhong") Integer maPhong);

    @Query("""
            SELECT DISTINCT c.phong.maPhong
            FROM CtDatPhong c
            WHERE c.phong IS NOT NULL
              AND c.ngayNhan < :ngayTra
              AND c.ngayTra > :ngayNhan
              AND (c.trangThai IS NULL OR c.trangThai <> 'DA_HUY')
            """)
    List<Integer> findMaPhongBiDatTrongKhoang(@Param("ngayNhan") LocalDate ngayNhan,
                                              @Param("ngayTra") LocalDate ngayTra);

    @Query("""
            SELECT c
            FROM CtDatPhong c
            WHERE c.phong.maPhong = :maPhong
              AND c.ngayNhan < :ngayTra
              AND c.ngayTra > :ngayNhan
              AND (c.trangThai IS NULL OR c.trangThai <> 'DA_HUY')
            """)
    List<CtDatPhong> findActiveBookingsForRoomBetween(@Param("maPhong") Integer maPhong,
                                                      @Param("ngayNhan") LocalDate ngayNhan,
                                                      @Param("ngayTra") LocalDate ngayTra);

    @Query("SELECT COALESCE(MAX(c.maCtDatPhong), 0) FROM CtDatPhong c")
    Integer findMaxMaCtDatPhong();


    @Query("""
            SELECT DISTINCT c.phong
            FROM CtDatPhong c
            WHERE c.khachHang.maKhachHang = :maKhachHang
              AND c.phong IS NOT NULL
              AND c.trangThai IN ('DA_TRA_PHONG', 'DA_THANH_TOAN')
            ORDER BY c.phong.maPhong
            """)
    List<com.example.do_an_java.entity.Phong> findReviewableRoomsByCustomer(@Param("maKhachHang") Integer maKhachHang);

    @Query("""
            SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
            FROM CtDatPhong c
            WHERE c.khachHang.maKhachHang = :maKhachHang
              AND c.phong.maPhong = :maPhong
              AND c.trangThai IN ('DA_TRA_PHONG', 'DA_THANH_TOAN')
            """)
    boolean existsCompletedBookingForReview(@Param("maKhachHang") Integer maKhachHang,
                                            @Param("maPhong") Integer maPhong);

    @Query("""
            SELECT c
            FROM CtDatPhong c
            WHERE c.khachHang.maKhachHang = :maKhachHang
              AND c.phong.maPhong = :maPhong
              AND c.trangThai IN ('DA_TRA_PHONG', 'DA_THANH_TOAN')
            ORDER BY c.ngayTra DESC
            """)
    List<CtDatPhong> findCompletedBookingsForReview(@Param("maKhachHang") Integer maKhachHang,
                                                    @Param("maPhong") Integer maPhong);

}
