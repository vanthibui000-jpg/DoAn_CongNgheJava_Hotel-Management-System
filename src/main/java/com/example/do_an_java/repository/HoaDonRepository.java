package com.example.do_an_java.repository;

import com.example.do_an_java.entity.HoaDon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface HoaDonRepository extends JpaRepository<HoaDon, Integer> {

    @Query("select coalesce(sum(h.soTienThu), 0) from HoaDon h where h.trangThai = 'DA_THANH_TOAN'")
    Long tinhTongDoanhThu();

    boolean existsByCtDatPhong_MaCtDatPhong(Integer maCtDatPhong);

    boolean existsByCtDatPhong_MaCtDatPhongAndMaHoaDonNot(Integer maCtDatPhong, Integer maHoaDon);

    boolean existsByCtDatPhong_MaCtDatPhongAndTrangThai(Integer maCtDatPhong, String trangThai);

    Optional<HoaDon> findFirstByCtDatPhong_MaCtDatPhongOrderByMaHoaDonDesc(Integer maCtDatPhong);

    @Query("""
            select coalesce(sum(h.soTienThu),0)
            from HoaDon h
            where h.trangThai = 'DA_THANH_TOAN'
              and day(h.ngayThuTien)=:ngay
              and month(h.ngayThuTien)=:thang
              and year(h.ngayThuTien)=:nam
            """)
    Long tinhDoanhThuTheoNgay(
            @Param("ngay") int ngay,
            @Param("thang") int thang,
            @Param("nam") int nam
    );

    @Query("""
            select coalesce(sum(h.soTienThu),0)
            from HoaDon h
            where h.trangThai = 'DA_THANH_TOAN'
              and month(h.ngayThuTien)=:thang
              and year(h.ngayThuTien)=:nam
            """)
    Long tinhDoanhThuTheoThang(
            @Param("thang") int thang,
            @Param("nam") int nam
    );
}
