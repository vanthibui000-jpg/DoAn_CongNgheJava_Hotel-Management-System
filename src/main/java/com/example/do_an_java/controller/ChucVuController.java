package com.example.do_an_java.controller;

import com.example.do_an_java.entity.ChucVu;
import com.example.do_an_java.entity.ChucVuQuyen;
import com.example.do_an_java.repository.ChucVuRepository;
import com.example.do_an_java.repository.ChucVuQuyenRepository;
import com.example.do_an_java.service.RolePermissionService;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin/chuc-vu")
public class ChucVuController {
    private final ChucVuRepository chucVuRepository;
    private final ChucVuQuyenRepository chucVuQuyenRepository;
    private final RolePermissionService rolePermissionService;

    public ChucVuController(ChucVuRepository chucVuRepository,
                            ChucVuQuyenRepository chucVuQuyenRepository,
                            RolePermissionService rolePermissionService) {
        this.chucVuRepository = chucVuRepository;
        this.chucVuQuyenRepository = chucVuQuyenRepository;
        this.rolePermissionService = rolePermissionService;
    }

    @GetMapping
    public String list(Model model) {
        List<ChucVu> items = chucVuRepository.findAll();
        Map<Integer, String> permissionLabels = items.stream()
                .collect(Collectors.toMap(
                        ChucVu::getMaChucVu,
                        item -> selectedPermissionLabels(item.getMaChucVu())
                ));
        model.addAttribute("items", items);
        model.addAttribute("permissionLabels", permissionLabels);
        return "chuc-vu/list";
    }

    @GetMapping("/them")
    public String add(Model model) {
        model.addAttribute("item", new ChucVu());
        addPermissionData(model, List.of());
        return "chuc-vu/form";
    }

    @GetMapping("/sua/{id}")
    public String edit(@PathVariable Integer id, Model model) {
        model.addAttribute("item", chucVuRepository.findById(id).orElseThrow());
        addPermissionData(model, selectedPermissionCodes(id));
        return "chuc-vu/form";
    }

    @PostMapping("/luu")
    @Transactional
    public String save(@ModelAttribute("item") ChucVu item,
                       @RequestParam(required = false) List<String> quyenIds,
                       Model model) {
        if (item.getTenChucVu() == null || item.getTenChucVu().trim().isBlank()) {
            model.addAttribute("error", "Vui lòng nhập tên chức vụ.");
            addPermissionData(model, quyenIds == null ? List.of() : quyenIds);
            return "chuc-vu/form";
        }
        item.setTenChucVu(item.getTenChucVu().trim());
        ChucVu saved = chucVuRepository.save(item);
        chucVuQuyenRepository.deleteByChucVu_MaChucVu(saved.getMaChucVu());
        if (quyenIds != null) {
            quyenIds.stream()
                    .filter(code -> code != null && !code.isBlank())
                    .distinct()
                    .map(code -> new ChucVuQuyen(saved, code))
                    .forEach(chucVuQuyenRepository::save);
        }
        return "redirect:/admin/chuc-vu";
    }

    @GetMapping("/xoa/{id}")
    @Transactional
    public String delete(@PathVariable Integer id) {
        chucVuQuyenRepository.deleteByChucVu_MaChucVu(id);
        chucVuRepository.deleteById(id);
        return "redirect:/admin/chuc-vu";
    }

    private void addPermissionData(Model model, List<String> selectedPermissions) {
        model.addAttribute("permissionOptions", rolePermissionService.getPermissionOptions());
        model.addAttribute("selectedPermissions", selectedPermissions == null ? List.of() : selectedPermissions);
    }

    private List<String> selectedPermissionCodes(Integer maChucVu) {
        try {
            return chucVuQuyenRepository.findByChucVu_MaChucVu(maChucVu)
                    .stream()
                    .map(ChucVuQuyen::getMaQuyen)
                    .toList();
        } catch (DataAccessException ex) {
            return List.of();
        }
    }

    private String selectedPermissionLabels(Integer maChucVu) {
        List<String> selected = selectedPermissionCodes(maChucVu);
        if (selected.isEmpty()) {
            return "Theo quyền mặc định";
        }
        Map<String, String> labelByCode = rolePermissionService.getPermissionOptions()
                .stream()
                .collect(Collectors.toMap(
                        RolePermissionService.PermissionOption::code,
                        RolePermissionService.PermissionOption::label
                ));
        return selected.stream()
                .map(code -> labelByCode.getOrDefault(code, code))
                .collect(Collectors.joining(", "));
    }
}
