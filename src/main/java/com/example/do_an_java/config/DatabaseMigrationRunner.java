package com.example.do_an_java.config;

import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DatabaseMigrationRunner {
    private final JdbcTemplate jdbcTemplate;

    public DatabaseMigrationRunner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @PostConstruct
    public void migrate() {
        try {
            createChucVuQuyenTableIfMissing();
            createPaymentsTableIfMissing();
            seedDefaultRolePermissions();

            addColumnIfMissing("KHACHHANG", "MatKhau", "VARCHAR(100) DEFAULT '1'");
            addColumnIfMissing("KHACHHANG", "TrangThai", "VARCHAR(30) DEFAULT 'HOAT_DONG'");
            addColumnIfMissing("LOAIPHONG", "MoTa", "VARCHAR(255) NULL");
            addColumnIfMissing("LOAIPHONG", "DaXoa", "BOOLEAN DEFAULT FALSE");
            addColumnIfMissing("PHONG", "HinhAnhDaiDien", "VARCHAR(255) NULL");
            addColumnIfMissing("PHONG", "TrangThai", "VARCHAR(30) DEFAULT 'Trống'");
            addColumnIfMissing("CTDATPHONG", "TrangThai", "VARCHAR(50) DEFAULT 'CHO_ADMIN_DUYET'");
            addColumnIfMissing("CTDATPHONG", "payment_status", "VARCHAR(50) DEFAULT 'CHUA_TAO'");
            addColumnIfMissing("CTDATPHONG", "payment_requested_at", "DATETIME NULL");
            addColumnIfMissing("CTDATPHONG", "payment_expired_at", "DATETIME NULL");
            addColumnIfMissing("CTDANHGIA", "MaKhachHang", "INT NULL");
            addColumnIfMissing("CTDANHGIA", "MaCTDatPhong", "INT NULL");
            addColumnIfMissing("CTDANHGIA", "SoSao", "INT NOT NULL DEFAULT 5");
            addColumnIfMissing("HOADON", "TrangThai", "VARCHAR(30) DEFAULT 'CHUA_THANH_TOAN'");
            addColumnIfMissing("DICHVU", "HinhAnh", "VARCHAR(255) NULL");
            addColumnIfMissing("PAYMENTS", "payment_requested_at", "DATETIME NULL");
            addColumnIfMissing("PAYMENTS", "expired_at", "DATETIME NULL");
            addColumnIfMissing("PAYMENTS", "paid_at", "DATETIME NULL");
            addColumnIfMissing("PAYMENTS", "cancelled_at", "DATETIME NULL");

            jdbcTemplate.update("UPDATE KHACHHANG SET MatKhau = '1' WHERE MatKhau IS NULL OR MatKhau = ''");
            jdbcTemplate.update("UPDATE KHACHHANG SET TrangThai = 'HOAT_DONG' WHERE TrangThai IS NULL OR TrangThai = ''");
            jdbcTemplate.update("UPDATE PHONG SET TrangThai = 'Trống' WHERE TrangThai IS NULL OR TrangThai = ''");
            jdbcTemplate.update("UPDATE CTDATPHONG SET TrangThai = 'CHO_ADMIN_DUYET' WHERE TrangThai IS NULL OR TrangThai = ''");
            jdbcTemplate.update("UPDATE CTDATPHONG SET payment_status = 'CHUA_TAO' WHERE payment_status IS NULL OR payment_status = ''");
            jdbcTemplate.update("ALTER TABLE CTDATPHONG MODIFY TrangThai VARCHAR(50) DEFAULT 'CHO_ADMIN_DUYET'");
            jdbcTemplate.update("ALTER TABLE CTDATPHONG MODIFY payment_status VARCHAR(50) DEFAULT 'CHUA_TAO'");
            jdbcTemplate.update("UPDATE CTDANHGIA SET SoSao = 5 WHERE SoSao IS NULL");
            jdbcTemplate.update("UPDATE HOADON SET TrangThai = 'CHUA_THANH_TOAN' WHERE TrangThai IS NULL OR TrangThai = '' OR TrangThai = 'CHƯA THANH TOÁN'");
            jdbcTemplate.update("ALTER TABLE HOADON MODIFY TrangThai VARCHAR(30) DEFAULT 'CHUA_THANH_TOAN'");
            seedDefaultImages();
        } catch (Exception ignored) {
            // Khong chan ung dung khoi dong neu tai khoan MySQL khong co quyen ALTER.
            // Khi do nguoi dung co the chay file SQL bo sung trong project.
        }
    }

    private void seedDefaultImages() {
        updateServiceImage(1, "/Images/DichVu/BuaSang/service.jpg");
        updateServiceImage(2, "/Images/DichVu/BuaTrua/service.jpg");
        updateServiceImage(3, "/Images/DichVu/BuaToi/service.jpg");
        updateServiceImage(4, "/Images/DichVu/Massage/service.jpg");
        updateServiceImage(5, "/Images/DichVu/DuThuyen/service.jpg");

        for (int floor = 1; floor <= 3; floor++) {
            for (int roomIndex = 1; roomIndex <= 5; roomIndex++) {
                int roomNumber = floor * 100 + roomIndex;
                updateRoomImage(roomNumber, "/Images/ChiTietPhong/Phong" + roomNumber + "/01.jpg");
            }
        }
    }

    private void updateServiceImage(int maDichVu, String imagePath) {
        jdbcTemplate.update(
                "UPDATE DICHVU SET HinhAnh = ? WHERE MaDichVu = ? AND (HinhAnh IS NULL OR HinhAnh = '')",
                imagePath,
                maDichVu
        );
    }

    private void updateRoomImage(int maPhong, String imagePath) {
        jdbcTemplate.update(
                "UPDATE PHONG SET HinhAnhDaiDien = ? WHERE MaPhong = ? AND (HinhAnhDaiDien IS NULL OR HinhAnhDaiDien = '')",
                imagePath,
                maPhong
        );
    }

    private void addColumnIfMissing(String tableName, String columnName, String definition) {
        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.COLUMNS "
                        + "WHERE TABLE_SCHEMA = DATABASE() AND TABLE_NAME = ? AND COLUMN_NAME = ?",
                Integer.class,
                tableName,
                columnName
        );
        if (count == null || count == 0) {
            jdbcTemplate.execute("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
        }
    }

    private void createChucVuQuyenTableIfMissing() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS CHUCVU_QUYEN (
                    MaChucVu INT NOT NULL,
                    MaQuyen VARCHAR(50) NOT NULL,
                    PRIMARY KEY (MaChucVu, MaQuyen),
                    CONSTRAINT FK_CHUCVU_QUYEN_CHUCVU
                        FOREIGN KEY (MaChucVu) REFERENCES CHUCVU(MaChucVu)
                        ON DELETE CASCADE
                )
                """);
    }

    private void createPaymentsTableIfMissing() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS PAYMENTS (
                    id INT AUTO_INCREMENT PRIMARY KEY,
                    booking_id INT NOT NULL,
                    amount DECIMAL(12,2) NOT NULL,
                    method VARCHAR(30) DEFAULT 'VIETQR',
                    status VARCHAR(30) DEFAULT 'CHO_THANH_TOAN',
                    qr_url TEXT,
                    transfer_content VARCHAR(50),
                    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                    payment_requested_at DATETIME NULL,
                    expired_at DATETIME NULL,
                    paid_at DATETIME NULL,
                    cancelled_at DATETIME NULL,
                    CONSTRAINT FK_PAYMENTS_CTDATPHONG
                        FOREIGN KEY (booking_id) REFERENCES CTDATPHONG(MaCTDatPhong)
                )
                """);
    }

    private void seedDefaultRolePermissions() {
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM CHUCVU_QUYEN", Integer.class);
        if (count != null && count > 0) {
            return;
        }

        List<String> allPermissions = List.of(
                "DASHBOARD",
                "NHAN_VIEN",
                "CHUC_VU",
                "KHACH_HANG",
                "LOAI_PHONG",
                "PHONG",
                "DAT_PHONG",
                "DICH_VU",
                "CT_DICH_VU",
                "HOA_DON",
                "PHAN_CONG",
                "DANH_GIA",
                "THONG_KE"
        );

        insertPermissions(1, allPermissions);
        insertPermissions(2, List.of("DASHBOARD", "KHACH_HANG", "DAT_PHONG", "DICH_VU", "CT_DICH_VU", "HOA_DON"));
        insertPermissions(3, List.of("DASHBOARD"));
        insertPermissions(4, List.of("DASHBOARD", "PHAN_CONG"));
    }

    private void insertPermissions(int maChucVu, List<String> permissions) {
        Integer roleExists = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM CHUCVU WHERE MaChucVu = ?",
                Integer.class,
                maChucVu
        );
        if (roleExists == null || roleExists == 0) {
            return;
        }
        for (String permission : permissions) {
            jdbcTemplate.update(
                    "INSERT IGNORE INTO CHUCVU_QUYEN (MaChucVu, MaQuyen) VALUES (?, ?)",
                    maChucVu,
                    permission
            );
        }
    }
}
