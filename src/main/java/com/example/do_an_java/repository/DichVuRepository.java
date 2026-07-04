package com.example.do_an_java.repository;

import com.example.do_an_java.entity.DichVu;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DichVuRepository extends JpaRepository<DichVu, Integer> {
    List<DichVu> findByTenDichVuContainingIgnoreCase(String keyword);

}
