package com.pratk.movie_RS.movie_RS.records;

import java.math.BigDecimal;

public record PaymentInitiationResponse(
        Long paymentId,
        Long bookingId,
        BigDecimal amount,
        String currency,
        String provider,
        String providerOrderId,
        String keyId
) {
}