package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.payment.req.ClickCompleteRequest;
import com.example.tinder.dto.payment.req.ClickPrepareRequest;
import com.example.tinder.dto.payment.res.ClickCompleteResponse;
import com.example.tinder.dto.payment.res.ClickPrepareResponse;
import com.example.tinder.entity.*;
import com.example.tinder.enums.*;
import com.example.tinder.exception.ClickException;
import com.example.tinder.exception.PaymentException;
import com.example.tinder.repository.*;
import com.example.tinder.service.PaymentService;
import com.example.tinder.utils.ClickUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final CardRepository cardRepository;
    private final ClickTransactionRepository clickTransactionRepository;
    private final PayMeTransactionRepository payMeTransactionRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;

    @Value("${payme.merchant-id}")
    private String paymeMerchantId;

    @Value("${payme.secret-key}")
    private String paymeSecretKey;

    // =========================
    // CLICK METHODS
    // =========================
    @Override
    @Transactional
    public ClickPrepareResponse prepare(ClickPrepareRequest request) {
        log.info("Click Prepare request: {}", request);
        if (!Objects.equals(request.getAction(), ClickUtil.PREPARE_ACTION)){
            throw new ClickException(ClickResponseCode.ACTION_NOT_FOUND);
        }

        User user = getUserOrThrowExc(request.getMerchantTransId()); // merchant_trans_id

        //  AMOUNT
        String amountStr = request.getAmount().toString();

        BigDecimal amount;
        try {
            amount = new BigDecimal(amountStr);
            if (amount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new ClickException(ClickResponseCode.INVALID_AMOUNT);
            }
        } catch (Exception e) {
            throw new ClickException(ClickResponseCode.INVALID_AMOUNT);
        }

        if (!cardRepository.existsByUserId(user.getId())) {
            throw new ClickException(ClickResponseCode.USER_CARD_NOT_FOUND);
        }

        ClickTransaction save = clickTransactionRepository.save(ClickTransaction.builder()
                .id(request.getClickTransId())
                .amount(request.getAmount())
                .prepareTime(LocalDateTime.now())
                .userId(user.getId())
                .status(PaymentStatus.PREPARE)
                .build());

        return ClickPrepareResponse.from(request, save.getId());

    }

    @Override
    @Transactional
    public ClickCompleteResponse complete(ClickCompleteRequest request) {
        log.info("Click Complete request: {}", request);

        if (!Objects.equals(request.getAction(), ClickUtil.COMPLETE_ACTION)) {
            throw new ClickException(ClickResponseCode.ACTION_NOT_FOUND);
        }

        ClickTransaction clickTransaction = getTransactionOrThrowExc(request.getMerchantPrepareId());
        User user = getUserOrThrowExc(clickTransaction.getUserId());

        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ClickException(ClickResponseCode.USER_CARD_NOT_FOUND));

        if (clickTransaction.isComplete()) {
            throw new ClickException(ClickResponseCode.ALREADY_PAID);
        }

        if (clickTransaction.isCancel()) {
            throw new ClickException(ClickResponseCode.TRANSACTION_CANCELLED);
        }

        if (request.getError() != ClickResponseCode.SUCCESS.getCode()) {
            clickTransaction.setStatus(PaymentStatus.CANCEL);
            clickTransactionRepository.save(clickTransaction);
            throw new ClickException(ClickResponseCode.TRANSACTION_CANCELLED);
        }

        BigDecimal oldBalance = card.getBalance();
        card.setBalance(card.getBalance().add(clickTransaction.getAmount()));
        cardRepository.save(card);

        clickTransaction.setStatus(PaymentStatus.COMPLETE);
        clickTransactionRepository.save(clickTransaction);

        walletTransactionRepository.save(
                WalletTransaction.builder()
                        .user(user)
                        .type(TransactionType.EARN)
                        .amount(clickTransaction.getAmount())
                        .balanceAfter(card.getBalance())
                        .description("Click orqali balans to'ldirildi")
                        .status(TransactionStatus.COMPLETED)
                        .build()
        );

        log.info("Click transaction completed: {} | Amount: {} | Balance: {} -> {}",
                clickTransaction.getId(), clickTransaction.getAmount(), oldBalance, card.getBalance());

        return ClickCompleteResponse.from(request);
    }

    // =========================
    // PAYME METHODS
    // =========================
    @Override
    @Transactional
    public Map<String, Object> handle(Map<String, Object> body, String authHeader) {
        log.info("PayMe request: {}", body);

        if (!isValidAuth(authHeader)) {
            log.warn("Unauthorized PayMe request");
            return error(-32504, "Unauthorized", body.get("id"));
        }

        String method = body.get("method").toString();
        Map<String, Object> params = (Map<String, Object>) body.get("params");

        return switch (method) {
            case "CheckPerformTransaction" -> checkPerform(params, body.get("id"));
            case "CreateTransaction" -> createTransaction(params, body.get("id"));
            case "PerformTransaction" -> performTransaction(params, body.get("id"));
            case "CancelTransaction" -> cancelTransaction(params, body.get("id"));
            case "CheckTransaction" -> checkTransaction(params, body.get("id"));
            default -> {
                log.warn("Unknown PayMe method: {}", method);
                yield error(-32601, "Method not found", body.get("id"));
            }
        };
    }

    @Override
    public ApiResponse<String> getUrlForClick(BigDecimal amount) {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ClickException(ClickResponseCode.UNAUTHORIZED);
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ApiResponse.success(ClickUtil.generatePaymentUrl(user.getId(), amount) , SuccessType.SUCCESS);
    }

    private boolean isValidAuth(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Basic ")) {
            return false;
        }

        try {
            String base64 = authHeader.substring(6);
            String decoded = new String(Base64.getDecoder().decode(base64));
            String expected = paymeMerchantId + ":" + paymeSecretKey;
            return decoded.equals(expected);
        } catch (IllegalArgumentException e) {
            log.error("Base64 decode error", e);
            return false;
        }
    }

    private Map<String, Object> checkPerform(Map<String, Object> params, Object id) {
        Long userId;
        try {
            userId = Long.valueOf(((Map<?, ?>) params.get("account")).get("user_id").toString());
        } catch (Exception e) {
            log.warn("Invalid user_id in CheckPerform", e);
            return error(-31050, "Noto'g'ri user ID", id);
        }

        if (!cardRepository.existsByUserId(userId)) {
            log.warn("User not found in CheckPerform: {}", userId);
            return error(-31050, "User topilmadi", id);
        }

        log.info("CheckPerform success for user: {}", userId);
        return result(Map.of("allow", true), id);
    }

    private Map<String, Object> createTransaction(Map<String, Object> params, Object id) {
        String paymeTransId = params.get("id").toString();

        BigDecimal amount;
        try {
            long amountInTiyin = Long.parseLong(params.get("amount").toString());
            if (amountInTiyin <= 0) {
                log.warn("Invalid amount: {}", amountInTiyin);
                return error(-31001, "Noto'g'ri summa", id);
            }
            amount = BigDecimal.valueOf(amountInTiyin).divide(BigDecimal.valueOf(100));
        } catch (NumberFormatException e) {
            log.warn("Invalid amount format", e);
            return error(-31001, "Noto'g'ri summa formati", id);
        }

        Long userId;
        try {
            Object accountObj = params.get("account");
            if (accountObj instanceof Map<?, ?> accountMap) {
                Object uid = accountMap.get("user_id");
                userId = Long.valueOf(uid.toString());
            } else {
                throw new IllegalArgumentException("Invalid account format");
            }
        } catch (Exception e) {
            log.warn("Invalid user_id in CreateTransaction", e);
            return error(-31050, "Noto'g'ri user ID", id);
        }

        if (!cardRepository.existsByUserId(userId)) {
            log.warn("User not found: {}", userId);
            return error(-31050, "User topilmadi", id);
        }

        if (payMeTransactionRepository.existsByPaymeTransId(paymeTransId)) {
            log.warn("PayMe transaction already exists: {}", paymeTransId);
            return error(-31008, "Transaction allaqachon mavjud", id);
        }

        PayMeTransaction tx = PayMeTransaction.builder()
                .paymeTransId(paymeTransId)
                .userId(userId)
                .amount(amount)
                .status(TransactionStatus.PENDING)
                .build();

        payMeTransactionRepository.save(tx);
        log.info("PayMe transaction created: {} | Amount: {} | User: {}", paymeTransId, amount, userId);

        long createTime = tx.getCreatedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        return result(Map.of(
                "transaction", paymeTransId,
                "state", 1,
                "create_time", createTime
        ), id);
    }

    @Transactional
    public Map<String, Object> performTransaction(Map<String, Object> params, Object id) {
        String paymeTransId = params.get("id").toString();

        PayMeTransaction tx = payMeTransactionRepository.findByPaymeTransId(paymeTransId)
                .orElse(null);

        if (tx == null) {
            log.error("Transaction not found in Perform: {}", paymeTransId);
            return error(-31003, "Transaction topilmadi", id);
        }

        if (tx.getStatus() == TransactionStatus.COMPLETED) {
            log.info("Transaction already performed: {}", paymeTransId);
            long performTime = tx.getUpdatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            return result(Map.of(
                    "transaction", paymeTransId,
                    "state", 2,
                    "perform_time", performTime
            ), id);
        }

        if (tx.getStatus() == TransactionStatus.CANCELLED) {
            log.warn("Cannot perform cancelled transaction: {}", paymeTransId);
            return error(-31008, "Transaction bekor qilingan", id);
        }

        Card card = cardRepository.findByUserId(tx.getUserId())
                .orElseThrow(() -> new PaymentException(-31050, "Card topilmadi"));

        User user = userRepository.findById(tx.getUserId())
                .orElseThrow(() -> new PaymentException(-31050, "User topilmadi"));

        BigDecimal oldBalance = card.getBalance();
        card.setBalance(card.getBalance().add(tx.getAmount()));
        cardRepository.save(card);

        tx.setStatus(TransactionStatus.COMPLETED);
        payMeTransactionRepository.save(tx);

        walletTransactionRepository.save(
                WalletTransaction.builder()
                        .user(user)
                        .type(TransactionType.EARN)
                        .amount(tx.getAmount())
                        .balanceAfter(card.getBalance())
                        .description("PayMe orqali balans to'ldirildi")
                        .status(TransactionStatus.COMPLETED)
                        .build()
        );

        long performTime = tx.getUpdatedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        log.info("PayMe transaction performed: {} | Amount: {} | Balance: {} -> {}",
                paymeTransId, tx.getAmount(), oldBalance, card.getBalance());

        return result(Map.of(
                "transaction", paymeTransId,
                "state", 2,
                "perform_time", performTime
        ), id);
    }

    // 5️⃣ CancelTransaction
    @Transactional
    public Map<String, Object> cancelTransaction(Map<String, Object> params, Object id) {
        String paymeTransId = params.get("id").toString();

        PayMeTransaction tx = payMeTransactionRepository.findByPaymeTransId(paymeTransId)
                .orElse(null);

        if (tx == null) {
            log.error("Transaction not found in Cancel: {}", paymeTransId);
            return error(-31003, "Transaction topilmadi", id);
        }

        if (tx.getStatus() == TransactionStatus.CANCELLED) {
            log.info("Transaction already cancelled: {}", paymeTransId);
            long cancelTime = tx.getUpdatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();

            return result(Map.of(
                    "transaction", paymeTransId,
                    "state", -1,
                    "cancel_time", cancelTime
            ), id);
        }

        if (tx.getStatus() == TransactionStatus.COMPLETED) {
            Card card = cardRepository.findByUserId(tx.getUserId())
                    .orElseThrow(() -> new PaymentException(-31050, "Card topilmadi"));

            User user = userRepository.findById(tx.getUserId())
                    .orElseThrow(() -> new PaymentException(-31050, "User topilmadi"));

            if (card.getBalance().compareTo(tx.getAmount()) < 0) {
                log.warn("Insufficient balance for refund: {}", paymeTransId);
                return error(-31007, "Balansda mablag' yetarli emas", id);
            }

            BigDecimal oldBalance = card.getBalance();
            card.setBalance(card.getBalance().subtract(tx.getAmount()));
            cardRepository.save(card);

            walletTransactionRepository.save(
                    WalletTransaction.builder()
                            .user(user)
                            .type(TransactionType.REFUND)
                            .amount(tx.getAmount())
                            .balanceAfter(card.getBalance())
                            .description("PayMe transaction bekor qilindi")
                            .status(TransactionStatus.CANCELLED)
                            .build()
            );

            log.info("PayMe transaction refunded: {} | Amount: {} | Balance: {} -> {}",
                    paymeTransId, tx.getAmount(), oldBalance, card.getBalance());
        }

        tx.setStatus(TransactionStatus.CANCELLED);
        payMeTransactionRepository.save(tx);

        long cancelTime = tx.getUpdatedAt()
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli();

        log.info("PayMe transaction cancelled: {}", paymeTransId);

        return result(Map.of(
                "transaction", paymeTransId,
                "state", -1,
                "cancel_time", cancelTime
        ), id);
    }

    private Map<String, Object> checkTransaction(Map<String, Object> params, Object id) {
        String paymeTransId = params.get("id").toString();

        PayMeTransaction tx = payMeTransactionRepository.findByPaymeTransId(paymeTransId)
                .orElse(null);

        if (tx == null) {
            return error(-31003, "Transaction topilmadi", id);
        }

        int state = switch (tx.getStatus()) {
            case PENDING -> 1;
            case COMPLETED -> 2;
            case CANCELLED -> -1;
        };

        Map<String, Object> response = new HashMap<>();
        response.put("transaction", paymeTransId);
        response.put("state", state);

        if (tx.getCreatedAt() != null) {
            long createTime = tx.getCreatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            response.put("create_time", createTime);
        }

        if (tx.getStatus() == TransactionStatus.COMPLETED && tx.getUpdatedAt() != null) {
            long performTime = tx.getUpdatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            response.put("perform_time", performTime);
        }

        if (tx.getStatus() == TransactionStatus.CANCELLED && tx.getUpdatedAt() != null) {
            long cancelTime = tx.getUpdatedAt()
                    .atZone(ZoneId.systemDefault())
                    .toInstant()
                    .toEpochMilli();
            response.put("cancel_time", cancelTime);
        }

        return result(response, id);
    }

    // =========================
    // RESPONSE HELPERS
    // =========================
    private Map<String, Object> result(Object result, Object id) {
        return Map.of("jsonrpc", "2.0", "result", result, "id", id);
    }

    private Map<String, Object> error(int code, String message, Object id) {
        return Map.of(
                "jsonrpc", "2.0",
                "error", Map.of("code", code, "message", message),
                "id", id
        );
    }

    private User getUserOrThrowExc(Long userId) {
        User user = userRepository.findById(userId).orElse(null);

        if (Objects.isNull(user)) {
            throw new ClickException(ClickResponseCode.USER_NOT_FOUND);
        }

        return user;
    }

    private ClickTransaction getTransactionOrThrowExc(Long transactionId) {
        return clickTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ClickException(ClickResponseCode.TRANSACTION_NOT_FOUND));
    }
}