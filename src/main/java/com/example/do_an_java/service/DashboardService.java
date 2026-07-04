package com.example.do_an_java.service;

import com.example.do_an_java.dto.DashboardStats;
import com.example.do_an_java.repository.*;
import org.springframework.stereotype.Service;

@Service
public class DashboardService {
    private final NhanVienRepository nhanVienRepository;
    private final KhachHangRepository khachHangRepository;
    private final PhongRepository phongRepository;
    private final DichVuRepository dichVuRepository;
    private final CtDatPhongRepository ctDatPhongRepository;
    private final HoaDonRepository hoaDonRepository;

    public DashboardService(
            NhanVienRepository nhanVienRepository,
            KhachHangRepository khachHangRepository,
            PhongRepository phongRepository,
            DichVuRepository dichVuRepository,
            CtDatPhongRepository ctDatPhongRepository,
            HoaDonRepository hoaDonRepository
    ) {
        this.nhanVienRepository = nhanVienRepository;
        this.khachHangRepository = khachHangRepository;
        this.phongRepository = phongRepository;
        this.dichVuRepository = dichVuRepository;
        this.ctDatPhongRepository = ctDatPhongRepository;
        this.hoaDonRepository = hoaDonRepository;
    }

    public DashboardStats getStats() {
        return new DashboardStats(
                nhanVienRepository.count(),
                khachHangRepository.count(),
                phongRepository.count(),
                dichVuRepository.count(),
                ctDatPhongRepository.count(),
                hoaDonRepository.count(),
                hoaDonRepository.tinhTongDoanhThu()
        );
    }
}
