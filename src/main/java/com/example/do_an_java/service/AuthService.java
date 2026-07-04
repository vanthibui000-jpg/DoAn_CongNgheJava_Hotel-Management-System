package com.example.do_an_java.service;

import com.example.do_an_java.entity.NhanVien;
import com.example.do_an_java.repository.NhanVienRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    private final NhanVienRepository nhanVienRepository;

    public AuthService(NhanVienRepository nhanVienRepository) {
        this.nhanVienRepository = nhanVienRepository;
    }

    public Optional<NhanVien> login(String username, String password) {
        return nhanVienRepository.findByUsernameAndPassword(username, password);
    }
}
