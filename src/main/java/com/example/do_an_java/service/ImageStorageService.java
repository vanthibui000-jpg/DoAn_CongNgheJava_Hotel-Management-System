package com.example.do_an_java.service;

import com.example.do_an_java.entity.Phong;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;

@Service
public class ImageStorageService {
    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp", "gif");
    private final Path bundledImagesRoot = Paths.get("src", "main", "resources", "static", "Images")
            .toAbsolutePath()
            .normalize();
    private final Path uploadImagesRoot;

    public ImageStorageService(@Value("${app.upload.images-root:uploads/Images}") String uploadImagesRoot) {
        this.uploadImagesRoot = Paths.get(uploadImagesRoot).toAbsolutePath().normalize();
    }

    public void validateImageFile(MultipartFile file) {
        if (isEmpty(file)) {
            return;
        }
        String extension = extensionOf(file.getOriginalFilename());
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Chi ho tro tep anh JPG, PNG, WEBP hoac GIF.");
        }
    }

    public void validateImageFiles(MultipartFile[] files) {
        if (files == null) {
            return;
        }
        for (MultipartFile file : files) {
            validateImageFile(file);
        }
    }

    public String storeServiceImage(MultipartFile file, Integer maDichVu) throws IOException {
        if (isEmpty(file) || maDichVu == null) {
            return null;
        }
        validateImageFile(file);

        String extension = extensionOf(file.getOriginalFilename());
        Path directory = uploadImagesRoot.resolve(Paths.get("DichVu", "DichVu" + maDichVu)).normalize();
        Files.createDirectories(directory);

        String fileName = "service." + extension;
        Path target = directory.resolve(fileName).normalize();
        ensureInsideUploadRoot(target);
        file.transferTo(target);
        return toUrl(uploadImagesRoot.relativize(target));
    }

    public String storeRoomImages(MultipartFile[] files, Integer maPhong) throws IOException {
        if (!hasAnyFile(files) || maPhong == null) {
            return null;
        }
        validateImageFiles(files);

        Path directory = uploadImagesRoot.resolve(Paths.get("ChiTietPhong", "Phong" + maPhong)).normalize();
        ensureInsideUploadRoot(directory);
        deleteDirectory(directory);
        Files.createDirectories(directory);

        String firstStoredUrl = null;
        int index = 1;
        for (MultipartFile file : files) {
            if (isEmpty(file)) {
                continue;
            }
            String fileName = String.format("%02d-%s", index++, cleanFilename(file.getOriginalFilename()));
            Path target = directory.resolve(fileName).normalize();
            ensureInsideUploadRoot(target);
            file.transferTo(target);
            if (firstStoredUrl == null) {
                firstStoredUrl = toUrl(uploadImagesRoot.relativize(target));
            }
        }
        return firstStoredUrl;
    }

    public void deleteRoomImages(Integer maPhong) throws IOException {
        if (maPhong == null) {
            return;
        }
        Path directory = uploadImagesRoot.resolve(Paths.get("ChiTietPhong", "Phong" + maPhong)).normalize();
        ensureInsideUploadRoot(directory);
        deleteDirectory(directory);
    }

    public List<String> getRoomGallery(Phong phong) {
        if (phong == null || phong.getMaPhong() == null) {
            return List.of();
        }

        String roomFolder = "Phong" + phong.getMaPhong();
        Path uploadDirectory = uploadImagesRoot.resolve(Paths.get("ChiTietPhong", roomFolder)).normalize();
        Path bundledDirectory = bundledImagesRoot.resolve(Paths.get("ChiTietPhong", roomFolder)).normalize();
        List<String> imageUrls = new ArrayList<>();
        imageUrls.addAll(listImageUrls(uploadDirectory, uploadImagesRoot));
        imageUrls.addAll(listImageUrls(bundledDirectory, bundledImagesRoot));
        imageUrls = new ArrayList<>(imageUrls.stream().distinct().toList());

        String cover = phong.getHinhAnhDaiDienHienThi();
        if (hasText(cover) && (isExternalUrl(cover) || imageUrlExists(cover))) {
            imageUrls.remove(cover);
            imageUrls.add(0, cover);
        }

        return imageUrls.stream().limit(4).toList();
    }

    private List<String> listImageUrls(Path directory, Path root) {
        if (!Files.isDirectory(directory)) {
            return new ArrayList<>();
        }
        try (var stream = Files.list(directory)) {
            return new ArrayList<>(stream
                    .filter(Files::isRegularFile)
                    .filter(path -> ALLOWED_EXTENSIONS.contains(extensionOf(path.getFileName().toString())))
                    .sorted(Comparator.comparing(path -> path.getFileName().toString(), String.CASE_INSENSITIVE_ORDER))
                    .map(path -> toUrl(root.relativize(path)))
                    .toList());
        } catch (IOException ignored) {
            return new ArrayList<>();
        }
    }

    private int nextImageIndex(Path directory) throws IOException {
        if (!Files.isDirectory(directory)) {
            return 1;
        }
        try (var stream = Files.list(directory)) {
            return (int) stream
                    .filter(Files::isRegularFile)
                    .filter(path -> ALLOWED_EXTENSIONS.contains(extensionOf(path.getFileName().toString())))
                    .count() + 1;
        }
    }

    private void deleteDirectory(Path directory) throws IOException {
        if (!Files.exists(directory)) {
            return;
        }
        try (var stream = Files.walk(directory)) {
            List<Path> paths = stream
                    .sorted(Comparator.reverseOrder())
                    .toList();
            for (Path path : paths) {
                Files.deleteIfExists(path);
            }
        }
    }

    private boolean imageUrlExists(String url) {
        if (!url.startsWith("/Images/")) {
            return false;
        }
        String relative = url.substring("/Images/".length()).replace("/", java.io.File.separator);
        Path uploadPath = uploadImagesRoot.resolve(relative).normalize();
        if (uploadPath.startsWith(uploadImagesRoot) && Files.isRegularFile(uploadPath)) {
            return true;
        }
        Path bundledPath = bundledImagesRoot.resolve(relative).normalize();
        return bundledPath.startsWith(bundledImagesRoot) && Files.isRegularFile(bundledPath);
    }

    private String toUrl(Path relativePath) {
        return "/Images/" + relativePath.toString().replace('\\', '/');
    }

    private String cleanFilename(String originalFilename) {
        String extension = extensionOf(originalFilename);
        String name = originalFilename == null ? "image" : Paths.get(originalFilename).getFileName().toString();
        int dotIndex = name.lastIndexOf('.');
        String baseName = dotIndex > 0 ? name.substring(0, dotIndex) : name;
        baseName = Normalizer.normalize(baseName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replaceAll("[^A-Za-z0-9._-]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("(^-|-$)", "");
        if (baseName.isBlank()) {
            baseName = "image";
        }
        return baseName + "." + extension;
    }

    private String extensionOf(String filename) {
        if (filename == null || filename.isBlank() || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }

    private void ensureInsideUploadRoot(Path target) {
        if (!target.startsWith(uploadImagesRoot)) {
            throw new IllegalArgumentException("Duong dan luu anh khong hop le.");
        }
    }

    private boolean isExternalUrl(String url) {
        return url.startsWith("http://") || url.startsWith("https://");
    }

    private boolean isEmpty(MultipartFile file) {
        return file == null || file.isEmpty();
    }

    private boolean hasAnyFile(MultipartFile[] files) {
        if (files == null) {
            return false;
        }
        for (MultipartFile file : files) {
            if (!isEmpty(file)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
