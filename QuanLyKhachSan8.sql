-- ========================================================
-- DATABASE QUẢN LÝ KHÁCH SẠN + TÍCH HỢP VIETQR
-- File được gộp từ:
--   1) QuanLyKhachSan7.sql
--   2) vietqr_payments(1).sql
-- Ghi chú:
--   - Các cột VietQR đã được đưa trực tiếp vào CREATE TABLE CTDATPHONG.
--   - Bảng PAYMENTS đã dùng phiên bản mới có thời gian yêu cầu, hết hạn, thanh toán, hủy.
--   - Không dùng UPDATE hàng loạt để tránh lỗi Safe Update Mode trong MySQL Workbench.
-- ========================================================

DROP DATABASE IF EXISTS QUANLYKHACHSAN;
CREATE DATABASE QUANLYKHACHSAN CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE QUANLYKHACHSAN;

-- ========================================================
-- I. ĐỊNH NGHĨA CẤU TRÚC BẢNG (SCHEMA)
-- ========================================================

-- 1. BẢNG CHỨC VỤ
CREATE TABLE CHUCVU
(
    MaChucVu INT AUTO_INCREMENT PRIMARY KEY,
    TenChucVu VARCHAR(100)
);

-- 2. BẢNG PHÂN QUYỀN CHỨC VỤ (Cập nhật vị trí hợp lý)
CREATE TABLE CHUCVU_QUYEN (
    MaChucVu INT NOT NULL,
    MaQuyen VARCHAR(50) NOT NULL,
    PRIMARY KEY (MaChucVu, MaQuyen),
    CONSTRAINT FK_CHUCVU_QUYEN_CHUCVU 
        FOREIGN KEY (MaChucVu) REFERENCES CHUCVU(MaChucVu) ON DELETE CASCADE
);

-- 3. BẢNG NHÂN VIÊN
CREATE TABLE NHANVIEN
(
    MaNhanVien INT AUTO_INCREMENT PRIMARY KEY,
    Username VARCHAR(20),
    `Password` VARCHAR(30),
    TenNhanVien VARCHAR(100),
    NgaySinh DATE,
    CMND INT,
    NgayVaoLam DATE,
    MaChucVu INT,
    CONSTRAINT FK_NHANVIEN_CHUCVU 
        FOREIGN KEY (MaChucVu) REFERENCES CHUCVU(MaChucVu)
);

-- 4. BẢNG BẢNG PHÂN CÔNG
CREATE TABLE BANGPHANCONG
(
    MaPhanCong INT AUTO_INCREMENT PRIMARY KEY,
    NgayPhanCong DATE,
    LoaiCongViec VARCHAR(50),
    MaNhanVien INT,
    CONSTRAINT FK_BANGPHANCONG_NHANVIEN 
        FOREIGN KEY (MaNhanVien) REFERENCES NHANVIEN(MaNhanVien)
);

-- 5. BẢNG KHÁCH HÀNG
CREATE TABLE KHACHHANG
(
    MaKhachHang INT PRIMARY KEY,
    HoTen VARCHAR(255),
    Email VARCHAR(30),
    DiaChi VARCHAR(50),
    DienThoai VARCHAR(20),
    CMND VARCHAR(20),
    MatKhau VARCHAR(100),
    TrangThai VARCHAR(30) DEFAULT 'HOAT_DONG'
);

-- 6. BẢNG LOẠI PHÒNG
CREATE TABLE LOAIPHONG
(
    MaLoaiPhong INT AUTO_INCREMENT PRIMARY KEY,
    TenLoaiPhong VARCHAR(20),
    GiaPhong INT,
    MoTa VARCHAR(255),
    DaXoa BOOLEAN DEFAULT FALSE
);

-- 7. BẢNG PHÒNG
CREATE TABLE PHONG
(
    MaPhong INT PRIMARY KEY,
    MaLoaiPhong INT,
    HinhAnhDaiDien VARCHAR(255),
    TrangThai VARCHAR(30) DEFAULT 'Trống',
    CONSTRAINT FK_PHONG_LOAIPHONG 
        FOREIGN KEY (MaLoaiPhong) REFERENCES LOAIPHONG(MaLoaiPhong)
);

-- 8. BẢNG CHI TIẾT ĐẶT PHÒNG
CREATE TABLE CTDATPHONG
(
    MaCTDatPhong INT PRIMARY KEY,
    NgayThucHien DATE,
    MaKhachHang INT,
    NgayNhan DATE,
    NgayTra DATE,
    MaPhong INT,
    MaNhanVien INT,
    TrangThai VARCHAR(50) DEFAULT 'CHO_ADMIN_DUYET',
    payment_status VARCHAR(50) DEFAULT 'CHUA_TAO',
    payment_requested_at DATETIME NULL,
    payment_expired_at DATETIME NULL,
    CONSTRAINT FK_CTDATPHONG_KHACHHANG 
        FOREIGN KEY (MaKhachHang) REFERENCES KHACHHANG(MaKhachHang),
    CONSTRAINT FK_CTDATPHONG_PHONG 
        FOREIGN KEY (MaPhong) REFERENCES PHONG(MaPhong),
    CONSTRAINT FK_CTDATPHONG_NHANVIEN 
        FOREIGN KEY (MaNhanVien) REFERENCES NHANVIEN(MaNhanVien)
);

-- 8B. BẢNG THANH TOÁN VIETQR
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
);

-- 9. BẢNG DANH GIÁ
CREATE TABLE CTDANHGIA
(
    ID INT AUTO_INCREMENT PRIMARY KEY,
    NoiDung VARCHAR(255),
    NgayDanhGia DATE,
    MaPhong INT,
    MaKhachHang INT NULL,
    MaCTDatPhong INT NULL,
    SoSao INT NOT NULL DEFAULT 5,
    CONSTRAINT FK_CTDANHGIA_PHONG 
        FOREIGN KEY (MaPhong) REFERENCES PHONG(MaPhong),
    CONSTRAINT FK_CTDANHGIA_KHACHHANG 
        FOREIGN KEY (MaKhachHang) REFERENCES KHACHHANG(MaKhachHang),
    CONSTRAINT FK_CTDANHGIA_CTDATPHONG 
        FOREIGN KEY (MaCTDatPhong) REFERENCES CTDATPHONG(MaCTDatPhong)
);

-- 10. BẢNG HÓA ĐƠN (Đã sửa lại TrangThai chuẩn ngay từ đầu)
CREATE TABLE HOADON
(
    MaHoaDon INT AUTO_INCREMENT PRIMARY KEY,
    NgayThuTien DATE,
    SoTienThu INT,
    MaCTDatPhong INT,
    MaNhanVien INT,
    TrangThai VARCHAR(30) DEFAULT 'CHUA_THANH_TOAN',
    CONSTRAINT FK_HOADON_CTDATPHONG 
        FOREIGN KEY (MaCTDatPhong) REFERENCES CTDATPHONG(MaCTDatPhong),
    CONSTRAINT FK_HOADON_NHANVIEN 
        FOREIGN KEY (MaNhanVien) REFERENCES NHANVIEN(MaNhanVien)
);

-- 11. BẢNG DỊCH VỤ
CREATE TABLE DICHVU
(
    MaDichVu INT AUTO_INCREMENT PRIMARY KEY,
    TenDichVu VARCHAR(50),
    GiaDichVu INT,
    HinhAnh VARCHAR(255)
);

-- 12. BẢNG CHI TIẾT DỊCH VỤ
CREATE TABLE CTDICHVU
(
    MaCTDichVu INT,
    MaDichVu INT,
    SoLuong INT,
    MaCTDatPhong INT,
    TongTienDichVu INT,
    CONSTRAINT PK_CTDICHVU PRIMARY KEY (MaDichVu, MaCTDatPhong),
    CONSTRAINT FK_CTDICHVU_DICHVU 
        FOREIGN KEY (MaDichVu) REFERENCES DICHVU(MaDichVu),
    CONSTRAINT FK_CTDICHVU_CTDATPHONG 
        FOREIGN KEY (MaCTDatPhong) REFERENCES CTDATPHONG(MaCTDatPhong)
);


-- ========================================================
-- II. CHÈN DỮ LIỆU MẪU (DATA INITIALIZATION)
-- ========================================================

-- Chức vụ
INSERT INTO CHUCVU(TenChucVu) VALUES ('Quản lý'), ('Lễ tân'), ('Bảo vệ'), ('Tạp vụ');

-- Nhân viên 
INSERT INTO NHANVIEN(Username, `Password`, TenNhanVien, NgaySinh, CMND, NgayVaoLam, MaChucVu) VALUES 
('viet', '1', 'Nguyễn Quốc Việt', '2001-05-20', 225786369, '2026-01-01', 1), 
('binh', '1', 'Ngô Quốc Bình', '2001-08-15', 225764728, '2026-01-02', 2),    
('vung', '1', 'Hoàng Trọng Vững', '2001-03-10', 225712598, '2026-01-05', 3), 
('van', '1', 'Bùi Thị Vấn', '2001-11-25', 225896128, '2026-01-06', 4);       

-- Quyền truy cập hệ thống
INSERT IGNORE INTO CHUCVU_QUYEN (MaChucVu, MaQuyen) VALUES
(1, 'DASHBOARD'), (1, 'NHAN_VIEN'), (1, 'CHUC_VU'), (1, 'KHACH_HANG'), (1, 'LOAI_PHONG'),
(1, 'PHONG'), (1, 'DAT_PHONG'), (1, 'DICH_VU'), (1, 'CT_DICH_VU'), (1, 'HOA_DON'),
(1, 'PHAN_CONG'), (1, 'DANH_GIA'), (1, 'THONG_KE'),
(2, 'DASHBOARD'), (2, 'KHACH_HANG'), (2, 'DAT_PHONG'), (2, 'DICH_VU'), (2, 'CT_DICH_VU'), (2, 'HOA_DON'),
(3, 'DASHBOARD'),
(4, 'DASHBOARD'), (4, 'PHAN_CONG');

-- Loại phòng
INSERT INTO LOAIPHONG(TenLoaiPhong, GiaPhong, MoTa) VALUES 
('Bình Dân', 300000, 'Một giường đôi. Máy lạnh'),
('Thương Gia', 20000000, 'Một giường đôi. Máy lạnh. View ngắm biển. Tivi. Có phòng làm việc'),
('Vip', 4000000, 'Một giường đôi. Máy lạnh. View ngắm biển. Tivi');

-- Phòng
INSERT INTO PHONG(MaPhong, MaLoaiPhong, HinhAnhDaiDien) VALUES 
(101, 1, '/Images/ChiTietPhong/Phong101/01.jpg'),
(102, 1, '/Images/ChiTietPhong/Phong102/01.jpg'),
(103, 1, '/Images/ChiTietPhong/Phong103/01.jpg'),
(104, 1, '/Images/ChiTietPhong/Phong104/01.jpg'),
(105, 1, '/Images/ChiTietPhong/Phong105/01.jpg'),
(201, 2, '/Images/ChiTietPhong/Phong201/01.jpg'),
(202, 2, '/Images/ChiTietPhong/Phong202/01.jpg'),
(203, 2, '/Images/ChiTietPhong/Phong203/01.jpg'),
(204, 2, '/Images/ChiTietPhong/Phong204/01.jpg'),
(205, 2, '/Images/ChiTietPhong/Phong205/01.jpg'),
(301, 3, '/Images/ChiTietPhong/Phong301/01.jpg'),
(302, 3, '/Images/ChiTietPhong/Phong302/01.jpg'),
(303, 3, '/Images/ChiTietPhong/Phong303/01.jpg'),
(304, 3, '/Images/ChiTietPhong/Phong304/01.jpg'),
(305, 3, '/Images/ChiTietPhong/Phong305/01.jpg');

-- Dịch vụ
INSERT INTO DICHVU(TenDichVu, GiaDichVu, HinhAnh) VALUES 
('Ăn sáng', 30000, '/Images/DichVu/BuaSang/service.jpg'),
('Ăn trưa', 35000, '/Images/DichVu/BuaTrua/service.jpg'),
('Ăn tối', 35000, '/Images/DichVu/BuaToi/service.jpg'),
('Massage', 600000, '/Images/DichVu/Massage/service.jpg'),
('Du thuyền', 1000000, '/Images/DichVu/DuThuyen/service.jpg');

-- Khách hàng
INSERT INTO KHACHHANG(MaKhachHang, HoTen, Email, DiaChi, DienThoai, CMND, MatKhau, TrangThai) VALUES 
(1, 'Lê Văn Tèo', 'teo@gmail.com', 'Hà Nội', '0912345678', '258963147', '1', 'HOAT_DONG'),
(2, 'Lê Văn Tí', 'ti@gmail.com', 'Hồ Chí Minh', '0987654321', '258963148', '1', 'HOAT_DONG'),
(3, 'Nguyễn Văn A', 'vana@gmail.com', 'Đà Nẵng', '0905123456', '258963149', '1', 'HOAT_DONG'),
(4, 'Trần Thị B', 'thib@gmail.com', 'Cần Thơ', '0934123456', '258963150', '1', 'HOAT_DONG'),
(5, 'Phạm Minh C', 'minhc@gmail.com', 'Hải Phòng', '0977123456', '258925647', '1', 'HOAT_DONG');

-- Chi tiết đặt phòng
INSERT INTO CTDATPHONG(MaCTDatPhong, NgayThucHien, MaKhachHang, NgayNhan, NgayTra, MaPhong, MaNhanVien, TrangThai) VALUES 
(1, '2026-05-01', 1, '2026-05-05', '2026-05-07', 101, 2, 'DA_XAC_NHAN'),
(2, '2026-05-01', 2, '2026-05-06', '2026-05-08', 102, 2, 'DA_XAC_NHAN'),
(3, '2026-05-02', 3, '2026-05-08', '2026-05-10', 201, 2, 'DA_XAC_NHAN'),
(4, '2026-05-03', 4, '2026-05-08', '2026-05-10', 103, 2, 'DA_XAC_NHAN'),
(5, '2026-05-04', 5, '2026-05-11', '2026-05-13', 201, 2, 'DA_XAC_NHAN');

-- Không cần UPDATE payment_status cho dữ liệu mẫu.
-- Cột payment_status đã có DEFAULT 'CHUA_TAO' ngay trong CREATE TABLE CTDATPHONG,
-- nên khi INSERT không truyền payment_status thì MySQL tự điền giá trị mặc định.

-- Đánh giá mẫu
INSERT INTO CTDANHGIA(NoiDung, NgayDanhGia, MaPhong, MaKhachHang, MaCTDatPhong, SoSao) VALUES
('Phòng sạch sẽ, nhân viên hỗ trợ rất nhiệt tình', '2026-05-07', 101, 1, 1, 5),
('Giao diện đặt phòng dễ dùng, phòng view đẹp', '2026-05-08', 102, 2, 2, 5);

-- Hóa đơn
INSERT INTO HOADON(NgayThuTien, SoTienThu, MaCTDatPhong, MaNhanVien) VALUES 
('2026-05-07', 150000, 2, 2),
('2026-05-08', 200000, 2, 2),
('2026-05-10', 3000000, 3, 2),
('2026-05-10', 350000, 3, 2),
('2026-05-13', 350000, 5, 2);

-- Kiểm tra dữ liệu
SELECT * FROM KHACHHANG;
SELECT * FROM NHANVIEN;
SELECT * FROM HOADON;
SELECT * FROM CHUCVU_QUYEN;
SELECT * FROM CHUCVU;
