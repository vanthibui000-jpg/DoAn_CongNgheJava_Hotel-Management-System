package com.example.do_an_java.controller.client;

import com.example.do_an_java.entity.KhachHang;
import jakarta.servlet.http.HttpSession;

public final class ClientSession {
    public static final String CUSTOMER_SESSION_KEY = "khachHangDangNhap";

    private ClientSession() {
    }

    public static KhachHang currentCustomer(HttpSession session) {
        Object value = session.getAttribute(CUSTOMER_SESSION_KEY);
        return value instanceof KhachHang khachHang ? khachHang : null;
    }
}
