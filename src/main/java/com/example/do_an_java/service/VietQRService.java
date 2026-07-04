package com.example.do_an_java.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.Normalizer;

@Service
@PropertySource(value = "classpath:config/payment.properties", ignoreResourceNotFound = true)
public class VietQRService {
    private static final String QUICK_LINK_BASE = "https://img.vietqr.io/image";

    @Value("${vietqr.enabled:true}")
    private boolean enabled;

    @Value("${vietqr.bankId:}")
    private String bankId;

    @Value("${vietqr.accountNo:}")
    private String accountNo;

    @Value("${vietqr.accountName:}")
    private String accountName;

    @Value("${vietqr.template:compact2}")
    private String template;

    public String generateTransferContent(int bookingId) {
        return "DP" + bookingId;
    }

    public void loadConfig() {
        validateConfig();
    }

    public String generateQrUrl(int bookingId, BigDecimal amount) {
        validateConfig();
        String transferContent = generateTransferContent(bookingId);
        String amountText = amount == null ? "0" : amount.setScale(0, java.math.RoundingMode.HALF_UP).toPlainString();
        return QUICK_LINK_BASE + "/" + cleanPath(bankId) + "-" + cleanPath(accountNo) + "-" + cleanPath(template) + ".png"
                + "?amount=" + encode(amountText)
                + "&addInfo=" + encode(transferContent)
                + "&accountName=" + encode(accountName);
    }

    private void validateConfig() {
        if (!enabled) {
            throw new IllegalStateException("VietQR dang tat trong config/payment.properties.");
        }
        if (isBlank(bankId) || isBlank(accountNo) || isBlank(accountName)) {
            throw new IllegalStateException("Thieu cau hinh VietQR. Vui long sua config/payment.properties.");
        }
        if (accountNo.startsWith("YOUR_") || accountName.startsWith("YOUR ")) {
            throw new IllegalStateException("Vui long thay thong tin tai khoan demo trong config/payment.properties truoc khi tao QR.");
        }
    }

    private String cleanPath(String value) {
        return value == null ? "" : value.trim().replaceAll("[^A-Za-z0-9_-]", "");
    }

    private String encode(String value) {
        String normalized = value == null ? "" : Normalizer.normalize(value.trim(), Normalizer.Form.NFC);
        return URLEncoder.encode(normalized, StandardCharsets.UTF_8);
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isBlank();
    }
}
