package com.example.do_an_java.config;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class UploadExceptionHandler {
    private static final String UPLOAD_SIZE_MESSAGE =
            "Anh tai len qua lon. Moi anh toi da 20MB va tong moi lan tai len toi da 80MB.";

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxUploadSizeExceeded(HttpServletRequest request,
                                              RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", UPLOAD_SIZE_MESSAGE);

        String referer = request.getHeader("Referer");
        if (referer != null && !referer.isBlank()) {
            return "redirect:" + referer;
        }
        return "redirect:/admin/phong/them";
    }
}
