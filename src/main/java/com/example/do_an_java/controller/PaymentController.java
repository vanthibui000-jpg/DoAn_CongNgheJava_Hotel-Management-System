package com.example.do_an_java.controller;

import com.example.do_an_java.entity.Payment;
import com.example.do_an_java.service.InvoiceService;
import com.example.do_an_java.service.PaymentService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/thanh-toan")
public class PaymentController {
    private final PaymentService paymentService;
    private final InvoiceService invoiceService;

    public PaymentController(PaymentService paymentService, InvoiceService invoiceService) {
        this.paymentService = paymentService;
        this.invoiceService = invoiceService;
    }

    @GetMapping
    public String list(@RequestParam(defaultValue = "all") String status, Model model) {
        paymentService.expireOverduePayments();
        model.addAttribute("status", status);
        model.addAttribute("items", "all".equalsIgnoreCase(status)
                ? paymentService.getAllPayments()
                : paymentService.getPaymentsByStatus(status));
        return "payment/list";
    }

    @PostMapping("/xac-nhan-tien/{bookingId}")
    public String confirmPaid(@PathVariable Integer bookingId, RedirectAttributes redirectAttributes) {
        try {
            paymentService.adminConfirmPaid(bookingId);
            redirectAttributes.addFlashAttribute("message", "Đã xác nhận đã nhận tiền cho đơn #" + bookingId + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể xác nhận thanh toán: " + ex.getMessage());
        }
        return "redirect:/admin/thanh-toan?status=" + Payment.CHO_XAC_NHAN;
    }

    @PostMapping("/huy-qua-han/{bookingId}")
    public String cancelExpired(@PathVariable Integer bookingId, RedirectAttributes redirectAttributes) {
        try {
            paymentService.cancelExpiredBooking(bookingId);
            redirectAttributes.addFlashAttribute("message", "Đã hủy đơn quá hạn #" + bookingId + ".");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Không thể hủy đơn: " + ex.getMessage());
        }
        return "redirect:/admin/thanh-toan?status=" + Payment.HET_HAN;
    }

    @GetMapping("/hoa-don/{bookingId}")
    public String invoice(@PathVariable Integer bookingId, Model model, RedirectAttributes redirectAttributes) {
        if (!invoiceService.canPrintInvoice(bookingId)) {
            redirectAttributes.addFlashAttribute("error", "Đơn này chưa thanh toán nên chưa thể in hóa đơn.");
            return "redirect:/admin/thanh-toan";
        }
        model.addAttribute("invoice", invoiceService.generateInvoiceData(bookingId));
        return "payment/invoice";
    }
}
