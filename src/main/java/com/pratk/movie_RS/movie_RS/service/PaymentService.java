package com.pratk.movie_RS.movie_RS.service;

import com.pratk.movie_RS.movie_RS.entity.Payment;
import com.pratk.movie_RS.movie_RS.records.PaymentInitiationResponse;
import com.razorpay.RazorpayException;

public interface PaymentService {

     PaymentInitiationResponse initiatePayment(
            Long bookingId,
            String userId
    ) throws RazorpayException;

    Payment getPaymentDetails(Long id);

    void handleWebhook(String rawBody, String signature);
}
