package com.pratk.movie_RS.movie_RS.service.serviceimpl;

import com.pratk.movie_RS.movie_RS.entity.Booking;
import com.pratk.movie_RS.movie_RS.entity.Payment;
import com.pratk.movie_RS.movie_RS.entity.ShowtimeSeat;
import com.pratk.movie_RS.movie_RS.records.PaymentInitiationResponse;
import com.pratk.movie_RS.movie_RS.repository.BookingRepository;
import com.pratk.movie_RS.movie_RS.repository.PaymentRepository;
import com.pratk.movie_RS.movie_RS.service.PaymentService;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Utils;
import jakarta.persistence.EntityNotFoundException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
public class PaymentServiceImpl implements PaymentService {
    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;

    private final String razorpayKeyId;
    @Value("${razorpay.webhook.secret}")
    private String razorpayWebhookSecret;

    public PaymentServiceImpl(
            BookingRepository bookingRepository,
            PaymentRepository paymentRepository,
            RazorpayClient razorpayClient,
            @Value("${razorpay.key.id}") String razorpayKeyId,
            @Value("${razorpay.webhook.secret}") String razorpayWebhookSecret
    ) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
        this.razorpayKeyId = razorpayKeyId;
        this.razorpayWebhookSecret = razorpayWebhookSecret;
    }

    @Override
    @Transactional
    public void handleWebhook(String rawBody, String signature) {

        try {

            boolean valid = Utils.verifyWebhookSignature(
                    rawBody,
                    signature,
                    razorpayWebhookSecret
            );

            if (!valid) {
                throw new IllegalStateException(
                        "Invalid Razorpay webhook signature"
                );
            }

            JSONObject payload = new JSONObject(rawBody);

            String event = payload.getString("event");

            switch (event) {

                case "payment.captured":
                    handlePaymentCaptured(payload);
                    break;

                case "payment.failed":
                    handlePaymentFailed(payload);
                    break;

                default:
                    // Ignore events we don't need
                    break;
            }

        } catch (RazorpayException e) {
            throw new IllegalStateException(
                    "Failed to verify Razorpay webhook",
                    e
            );
        }
    }

    @Transactional
    public PaymentInitiationResponse initiatePayment(
            Long bookingId,
            String userId
    ) throws RazorpayException {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Booking not found: " + bookingId
                        ));

        // Assuming you already have this method
        validateOwnership(booking, userId);

        if (booking.getStatus() != Booking.BookingStatus.PENDING) {
            throw new IllegalStateException(
                    "Booking is not pending"
            );
        }

        if (booking.getExpiresAt() == null ||
                booking.getExpiresAt().isBefore(LocalDateTime.now())) {

            throw new IllegalStateException(
                    "Booking has expired"
            );
        }

        // Don't create another payment for the same booking
        Payment existingPayment =
                paymentRepository.findByBookingId(bookingId)
                        .orElse(null);

        if (existingPayment != null) {

            if (existingPayment.getStatus() == Payment.PaymentStatus.SUCCESS) {
                throw new IllegalStateException(
                        "Booking is already paid"
                );
            }

            if (existingPayment.getStatus() == Payment.PaymentStatus.PENDING) {
                return new PaymentInitiationResponse(
                        existingPayment.getId(),
                        booking.getId(),
                        existingPayment.getAmount(),
                        "INR",
                        existingPayment.getProvider(),
                        existingPayment.getProviderOrderId(),
                        razorpayKeyId
                );
            }
        }

        BigDecimal amount = booking.getTotalAmount();

        /*
         * Razorpay expects amount in the smallest currency unit.
         *
         * ₹500.00 -> 50000
         */
        long amountInPaise = amount
                .movePointRight(2)
                .longValueExact();

        JSONObject orderRequest = new JSONObject();

        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put(
                "receipt",
                "booking_" + booking.getId()
        );

        JSONObject notes = new JSONObject();
        notes.put("booking_id", booking.getId().toString());
        notes.put("user_id", userId);

        orderRequest.put("notes", notes);

        Order razorpayOrder =
                razorpayClient.orders.create(orderRequest);

        String razorpayOrderId =
                razorpayOrder.get("id");

        Payment payment = new Payment();

        payment.setBooking(booking);
        payment.setAmount(amount);
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setProvider("razorpay");
        payment.setProviderOrderId(razorpayOrderId);

        Payment savedPayment =
                paymentRepository.save(payment);

        return new PaymentInitiationResponse(
                savedPayment.getId(),
                booking.getId(),
                amount,
                "INR",
                "razorpay",
                razorpayOrderId,
                razorpayKeyId
        );
    }

    private void validateOwnership(
            Booking booking,
            String userId
    ) {
        if (!booking.getUserId().equals(userId)) {
            throw new IllegalStateException(
                    "You do not own this booking"
            );
        }
    }

    public Payment getPaymentDetails( Long id){
        return paymentRepository.findById(id).orElseThrow(() ->
                new EntityNotFoundException(
                        "Payment not found: " + id
                ));
    }

    private void handlePaymentCaptured(JSONObject payload) {

        JSONObject paymentEntity =
                payload
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

        String razorpayPaymentId =
                paymentEntity.getString("id");

        String razorpayOrderId =
                paymentEntity.getString("order_id");

        Payment payment =
                paymentRepository
                        .findByProviderOrderId(razorpayOrderId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Payment not found for Razorpay order: "
                                                + razorpayOrderId
                                ));

        // Idempotency
        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            return;
        }

        Booking booking = payment.getBooking();

        payment.setStatus(Payment.PaymentStatus.SUCCESS);
        payment.setTransactionId(razorpayPaymentId);

        paymentRepository.save(payment);

        booking.setStatus(Booking.BookingStatus.CONFIRMED);

        booking.getBookingSeats().forEach(bookingSeat -> {

            ShowtimeSeat showtimeSeat =
                    bookingSeat.getShowtimeSeat();

            showtimeSeat.setStatus(
                    ShowtimeSeat.SeatStatus.BOOKED
            );

            showtimeSeat.setLockExpiresAt(null);
            showtimeSeat.setLockedByUserId(null);
        });

        bookingRepository.save(booking);
    }

    private void handlePaymentFailed(JSONObject payload) {

        JSONObject paymentEntity =
                payload
                        .getJSONObject("payload")
                        .getJSONObject("payment")
                        .getJSONObject("entity");

        String razorpayPaymentId =
                paymentEntity.getString("id");

        String razorpayOrderId =
                paymentEntity.getString("order_id");

        Payment payment =
                paymentRepository
                        .findByProviderOrderId(razorpayOrderId)
                        .orElseThrow(() ->
                                new EntityNotFoundException(
                                        "Payment not found for Razorpay order: "
                                                + razorpayOrderId
                                ));

        if (payment.getStatus() == Payment.PaymentStatus.SUCCESS) {
            return;
        }

        payment.setStatus(Payment.PaymentStatus.FAILED);
        payment.setTransactionId(razorpayPaymentId);

        paymentRepository.save(payment);
    }
}