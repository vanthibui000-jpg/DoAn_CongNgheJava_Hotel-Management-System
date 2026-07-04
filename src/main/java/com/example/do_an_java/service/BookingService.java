package com.example.do_an_java.service;

import com.example.do_an_java.entity.CtDatPhong;
import com.example.do_an_java.entity.LoaiPhong;
import com.example.do_an_java.entity.Phong;
import com.example.do_an_java.repository.CtDatPhongRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class BookingService {
    private final CtDatPhongRepository ctDatPhongRepository;

    public BookingService(CtDatPhongRepository ctDatPhongRepository) {
        this.ctDatPhongRepository = ctDatPhongRepository;
    }

    public String validate(CtDatPhong datPhong, Integer ignoreBookingId, boolean customerBooking) {
        if (datPhong.getKhachHang() == null || datPhong.getKhachHang().getMaKhachHang() == null) {
            return "Vui lòng chọn khách hàng.";
        }
        if (datPhong.getPhong() == null || datPhong.getPhong().getMaPhong() == null) {
            return "Vui lòng chọn phòng.";
        }
        if (datPhong.getNgayNhan() == null || datPhong.getNgayTra() == null) {
            return "Vui lòng chọn ngày nhận và ngày trả.";
        }
        if (!datPhong.getNgayTra().isAfter(datPhong.getNgayNhan())) {
            return "Ngày trả phải sau ngày nhận ít nhất 1 ngày.";
        }
        if (customerBooking && datPhong.getNgayNhan().isBefore(LocalDate.now())) {
            return "Ngày nhận không được nhỏ hơn ngày hiện tại.";
        }

        Phong phong = datPhong.getPhong();
        if ("Bảo trì".equals(phong.getTrangThai())) {
            return "Phòng đang bảo trì, vui lòng chọn phòng khác.";
        }

        LoaiPhong loaiPhong = phong.getLoaiPhong();
        if (loaiPhong != null && Boolean.TRUE.equals(loaiPhong.getDaXoa())) {
            return "Loại phòng này hiện không còn kinh doanh.";
        }

        if ("DA_HUY".equals(datPhong.getTrangThai())) {
            return null;
        }

        LocalDate ngayNhanMoi = datPhong.getNgayNhan();
        LocalDate ngayTraMoi = effectiveCheckout(datPhong);
        List<CtDatPhong> conflicts = ctDatPhongRepository.findActiveBookingsByRoom(phong.getMaPhong());
        boolean hasConflict = conflicts.stream()
                .filter(item -> ignoreBookingId == null || !ignoreBookingId.equals(item.getMaCtDatPhong()))
                .anyMatch(item -> overlaps(ngayNhanMoi, ngayTraMoi, item));
        if (hasConflict) {
            return "Phòng đã có lịch đặt trong khoảng ngày này.";
        }

        return null;
    }

    private boolean overlaps(LocalDate start, LocalDate end, CtDatPhong existing) {
        if (existing.getNgayNhan() == null || existing.getNgayTra() == null) {
            return false;
        }
        LocalDate existingStart = existing.getNgayNhan();
        LocalDate existingEnd = effectiveCheckout(existing);
        return existingStart.isBefore(end) && existingEnd.isAfter(start);
    }

    private LocalDate effectiveCheckout(CtDatPhong datPhong) {
        if (datPhong.getNgayTra().isAfter(datPhong.getNgayNhan())) {
            return datPhong.getNgayTra();
        }
        return datPhong.getNgayTra().plusDays(1);
    }
}
