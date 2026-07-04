package com.example.do_an_java.service;

import com.example.do_an_java.entity.ChucVuQuyen;
import com.example.do_an_java.entity.NhanVien;
import com.example.do_an_java.repository.ChucVuQuyenRepository;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class RolePermissionService {
    public static final String DASHBOARD = "DASHBOARD";
    public static final String NHAN_VIEN = "NHAN_VIEN";
    public static final String CHUC_VU = "CHUC_VU";
    public static final String KHACH_HANG = "KHACH_HANG";
    public static final String LOAI_PHONG = "LOAI_PHONG";
    public static final String PHONG = "PHONG";
    public static final String DAT_PHONG = "DAT_PHONG";
    public static final String DICH_VU = "DICH_VU";
    public static final String CT_DICH_VU = "CT_DICH_VU";
    public static final String HOA_DON = "HOA_DON";
    public static final String PHAN_CONG = "PHAN_CONG";
    public static final String DANH_GIA = "DANH_GIA";
    public static final String THONG_KE = "THONG_KE";

    private final ChucVuQuyenRepository chucVuQuyenRepository;

    public RolePermissionService(ChucVuQuyenRepository chucVuQuyenRepository) {
        this.chucVuQuyenRepository = chucVuQuyenRepository;
    }

    public List<PermissionOption> getPermissionOptions() {
        Map<String, String> options = new LinkedHashMap<>();
        options.put(DASHBOARD, "Dashboard");
        options.put(NHAN_VIEN, "Quản lý nhân viên");
        options.put(CHUC_VU, "Quản lý chức vụ");
        options.put(KHACH_HANG, "Quản lý khách hàng");
        options.put(LOAI_PHONG, "Quản lý loại phòng");
        options.put(PHONG, "Quản lý phòng");
        options.put(DAT_PHONG, "Quản lý đặt phòng");
        options.put(DICH_VU, "Quản lý dịch vụ");
        options.put(CT_DICH_VU, "Sử dụng dịch vụ");
        options.put(HOA_DON, "Quản lý hóa đơn");
        options.put(PHAN_CONG, "Quản lý phân công");
        options.put(DANH_GIA, "Quản lý đánh giá");
        options.put(THONG_KE, "Thống kê");
        return options.entrySet().stream()
                .map(entry -> new PermissionOption(entry.getKey(), entry.getValue()))
                .toList();
    }

    public List<String> getPermissionCodes(NhanVien user) {
        if (user == null || user.getChucVu() == null || user.getChucVu().getMaChucVu() == null) {
            return List.of();
        }

        List<String> saved;
        try {
            saved = chucVuQuyenRepository.findByChucVu_MaChucVu(user.getChucVu().getMaChucVu())
                    .stream()
                    .map(ChucVuQuyen::getMaQuyen)
                    .filter(code -> code != null && !code.isBlank())
                    .toList();
        } catch (DataAccessException ex) {
            return defaultPermissions(user);
        }
        if (!saved.isEmpty()) {
            return saved;
        }

        return defaultPermissions(user);
    }

    public boolean canAccess(NhanVien user, String adminPath) {
        if (adminPath == null) {
            return false;
        }
        if (adminPath.startsWith("/doi-mat-khau") || adminPath.startsWith("/logout")) {
            return true;
        }
        List<String> permissions = getPermissionCodes(user);
        if (adminPath.startsWith("/dashboard")) {
            return permissions.contains(DASHBOARD);
        }
        if (adminPath.startsWith("/nhan-vien")) {
            return permissions.contains(NHAN_VIEN);
        }
        if (adminPath.startsWith("/chuc-vu")) {
            return permissions.contains(CHUC_VU);
        }
        if (adminPath.startsWith("/khach-hang")) {
            return permissions.contains(KHACH_HANG);
        }
        if (adminPath.startsWith("/loai-phong")) {
            return permissions.contains(LOAI_PHONG);
        }
        if (adminPath.startsWith("/phong")) {
            return permissions.contains(PHONG);
        }
        if (adminPath.startsWith("/dat-phong")) {
            return permissions.contains(DAT_PHONG);
        }
        if (adminPath.startsWith("/dich-vu")) {
            return permissions.contains(DICH_VU);
        }
        if (adminPath.startsWith("/ct-dich-vu")) {
            return permissions.contains(CT_DICH_VU);
        }
        if (adminPath.startsWith("/hoa-don")) {
            return permissions.contains(HOA_DON);
        }
        if (adminPath.startsWith("/thanh-toan")) {
            return permissions.contains(HOA_DON);
        }
        if (adminPath.startsWith("/phan-cong")) {
            return permissions.contains(PHAN_CONG);
        }
        if (adminPath.startsWith("/danh-gia")) {
            return permissions.contains(DANH_GIA);
        }
        if (adminPath.startsWith("/thong-ke")) {
            return permissions.contains(THONG_KE);
        }
        return false;
    }

    public List<String> defaultPermissions(NhanVien user) {
        if (isQuanLy(user)) {
            return getPermissionOptions().stream().map(PermissionOption::code).toList();
        }
        if (isLeTan(user)) {
            return List.of(DASHBOARD, KHACH_HANG, DAT_PHONG, DICH_VU, CT_DICH_VU, HOA_DON);
        }
        if (isBaoVe(user)) {
            return List.of(DASHBOARD);
        }
        if (isTapVu(user)) {
            return List.of(DASHBOARD, PHAN_CONG);
        }
        return new ArrayList<>();
    }

    private boolean isQuanLy(NhanVien user) {
        return hasRole(user, 1, "quanly", "quan li", "quan ly", "quanli", "manager");
    }

    private boolean isLeTan(NhanVien user) {
        return hasRole(user, 2, "letan", "le tan", "receptionist");
    }

    private boolean isBaoVe(NhanVien user) {
        return hasRole(user, 3, "baove", "bao ve", "security");
    }

    private boolean isTapVu(NhanVien user) {
        return hasRole(user, 4, "tapvu", "tap vu", "housekeeping");
    }

    private boolean hasRole(NhanVien user, int roleId, String... roleNames) {
        if (user == null || user.getChucVu() == null) {
            return false;
        }
        if (user.getChucVu().getMaChucVu() != null && user.getChucVu().getMaChucVu() == roleId) {
            return true;
        }
        String normalized = normalize(user.getChucVu().getTenChucVu());
        for (String roleName : roleNames) {
            if (normalized.equals(normalize(roleName))) {
                return true;
            }
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        String fixed = value.replace('Đ', 'D').replace('đ', 'd');
        String noMark = Normalizer.normalize(fixed, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return noMark.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
    }

    public record PermissionOption(String code, String label) {
    }
}
