package com.pratk.movie_RS.movie_RS.controller;

import com.pratk.movie_RS.movie_RS.entity.Payment;
import com.pratk.movie_RS.movie_RS.records.PaymentInitiationResponse;
import com.pratk.movie_RS.movie_RS.service.PaymentService;
import com.razorpay.RazorpayException;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("api/v1/")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/payments/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String rawBody,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        paymentService.handleWebhook(rawBody, signature);

        return ResponseEntity.ok().build();
    }

    @GetMapping("payments/{id}")
    public Payment getPaymentDetails(@PathVariable Long id){
        return paymentService.getPaymentDetails(id);
    }

    @PostMapping("/bookings/{id}/payment")
        public PaymentInitiationResponse initiatePayment(
                @PathVariable Long id,
                @AuthenticationPrincipal Jwt jwt) throws RazorpayException {
            return paymentService.initiatePayment(id,jwt.getSubject());
        }

}
