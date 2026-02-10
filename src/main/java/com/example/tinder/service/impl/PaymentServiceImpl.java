package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.payment.click.req.ClickCompleteRequest;
import com.example.tinder.dto.payment.click.req.ClickPrepareRequest;
import com.example.tinder.dto.payment.click.res.ClickCompleteResponse;
import com.example.tinder.dto.payment.click.res.ClickPrepareResponse;
import com.example.tinder.dto.payment.payme.req.PaymeParam;
import com.example.tinder.dto.payment.payme.req.PaymeRequest;
import com.example.tinder.dto.payment.payme.res.*;
import com.example.tinder.entity.*;
import com.example.tinder.enums.*;
import com.example.tinder.enums.payment.*;
import com.example.tinder.enums.payment.PaymeError;
import com.example.tinder.exception.ClickException;
import com.example.tinder.exception.PaymeException;
import com.example.tinder.repository.*;
import com.example.tinder.service.PaymentService;
import com.example.tinder.utils.ClickUtil;
import com.example.tinder.utils.PaymeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final CardRepository cardRepository;
    private final ClickTransactionRepository clickTransactionRepository;
    private final PaymeTransactionRepository paymeTransactionRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;

    private static final Long time_expired = 43_200_000L;

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
        log.info("merchant_trans_id (userId): {}", request.getMerchantTransId()); // BU QO'SHING
        System.out.println("merchant_trans_id (userId): " + request.getMerchantTransId());
        if (!Objects.equals(request.getAction(), ClickUtil.PREPARE_ACTION)) {
            throw new ClickException(ClickResponseCode.ACTION_NOT_FOUND);
        }

        User user = getUserOrThrowExcForClick(request.getMerchantTransId()); // merchant_trans_id

        BigDecimal amount = validateAmount(request.getAmount());

        if (!cardRepository.existsByUserId(user.getId())) {
            throw new ClickException(ClickResponseCode.USER_CARD_NOT_FOUND);
        }

        ClickTransaction save = clickTransactionRepository.save(ClickTransaction.builder()
                .id(request.getClickTransId())
                .amount(amount)
                .prepareTime(LocalDateTime.now())
                .userId(user.getId())
                .status(PaymentStatusForClick.PREPARE)
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
        User user = getUserOrThrowExcForClick(clickTransaction.getUserId());

        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ClickException(ClickResponseCode.USER_CARD_NOT_FOUND));

        if (clickTransaction.isComplete()) {
            throw new ClickException(ClickResponseCode.ALREADY_PAID);
        }

        if (clickTransaction.isCancel()) {
            throw new ClickException(ClickResponseCode.TRANSACTION_CANCELLED);
        }

        if (request.getError() != ClickResponseCode.SUCCESS.getCode()) {
            clickTransaction.setStatus(PaymentStatusForClick.CANCEL);
            clickTransactionRepository.save(clickTransaction);
            throw new ClickException(ClickResponseCode.TRANSACTION_CANCELLED);
        }

        BigDecimal oldBalance = card.getBalance();
        BigDecimal newBalance = oldBalance.add(clickTransaction.getAmount());

        WalletTransaction walletTransaction = walletTransactionRepository.save(
                WalletTransaction.builder()
                        .user(user)
                        .type(TransactionType.EARN)
                        .amount(clickTransaction.getAmount())
                        .balanceAfter(newBalance)
                        .description("Click orqali balans to'ldirildi")
                        .status(TransactionStatus.COMPLETED)
                        .paymentMethod(PaymentMethod.CLICK)
                        .build()
        );

        clickTransaction.setWalletTransactionId(walletTransaction.getId());
        clickTransaction.setStatus(PaymentStatusForClick.COMPLETE);
        clickTransactionRepository.save(clickTransaction);

        card.setBalance(newBalance);
        cardRepository.save(card);

        log.info("Click transaction completed: {} | Amount: {} | Balance: {} -> {}",
                clickTransaction.getId(), clickTransaction.getAmount(), oldBalance, newBalance);

        return ClickCompleteResponse.from(request);
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

        return ApiResponse.success(ClickUtil.generatePaymentUrl(user.getId(), amount), SuccessType.SUCCESS);
    }

    private ClickTransaction getTransactionOrThrowExc(Long transactionId) {
        return clickTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new ClickException(ClickResponseCode.TRANSACTION_NOT_FOUND));
    }

    // =========================
    // PAYME METHODS
    // =========================
    @Override
    @Transactional
    public PaymeSuccessResponse pay(PaymeRequest request) {
        return new PaymeSuccessResponse(switch (request.getMethod()) {
            case "CheckPerformTransaction" -> checkPerformTransaction(request.getParams());
            case "CreateTransaction" -> createTransaction(request.getParams());
            case "PerformTransaction" -> performTransaction(request.getParams());
            case "CancelTransaction" -> cancelTransaction(request.getParams());
            case "CheckTransaction" -> checkTransaction(request.getParams());
            case "GetStatement" -> getStatement(request.getParams());
            default -> throw new PaymeException(PaymeError.INVALID_METHOD);
        });
    }

    /**
     * CheckPerformTransaction
     * tranzaksiyani yaratishdan oldin tekshirish
     * ya'ni bizda shu tranzaksiya uchun order borligini va tranzaksiya summasi to'g'ri kelishini tekshirish
     */
    private CheckPerformTransactionResult checkPerformTransaction(PaymeParam params) {
        User user = getUserOrThrowExcForPayme(params.getAccount().getUserId());
        // 2. Amount null emas va 0 dan kattami?
        if (params.getAmount() == null || params.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymeException(PaymeError.INVALID_AMOUNT);
        }

        if (!cardRepository.existsByUserId(user.getId())) {
            throw new PaymeException(PaymeError.USER_CARD_NOT_FOUND);
        }

        return new CheckPerformTransactionResult(true);
    }

    /**
     * CreateTransaction - tranzaksiya yaratish bu CheckPerformTransactiondan keyin keladi
     */
    private CreateTransactionResult createTransaction(PaymeParam params) {
        PaymeTransaction transaction = paymeTransactionRepository.findById(params.getId()).orElse(null);

        if (Objects.nonNull(transaction)) {
            ifTransactionExist(transaction);
        } else {
            // agar tranzaksiya mavjud emas bo'lsa bu holatda tranzaksiya summasi va shunaqa ma'lumotlarni tekshirish uchun
            // CheckPerformTransaction ni chaqiramiz
            checkPerformTransaction(params);

            // ✅ Tiyndan so'mga o'tkazish
            BigDecimal amountInSom = convertTiynToSom(params.getAmount());

            transaction = paymeTransactionRepository.save(
                    PaymeTransaction.builder()
                            .id(params.getId())
                            .userId(params.getAccount().getUserId())
                            .amount(amountInSom)  // ✅ So'mda saqlash
                            .createTime(System.currentTimeMillis())
                            .state(PaymeTransactionState.PENDING)
                            .build()
            );
        }

        return CreateTransactionResult.from(transaction);
    }

    /**
     * PerformTransaction - tranzaksiyani amalga oshirish
     * bu CreateTransaction dan keyin bo'ladi va to‘lov tasdiqlanadi, mablag‘ merchant hisobiga o‘tkaziladi va buyurtma holati "PAID" deb belgilanadi.
     */
    private PerformTransactionResult performTransaction(PaymeParam params) {
        PaymeTransaction transaction = getTransactionOrThrowExc(params.getId());

        if (!Objects.equals(transaction.getState(), PaymeTransactionState.PENDING)) {
            if (!Objects.equals(transaction.getState(), PaymeTransactionState.PAID)) {
                throw new PaymeException(PaymeError.CANT_DO_OPERATION);
            }

            // Agar tranzaksiya holati PAID bo'lsa bu tranzaksiyani shunchaki qaytaramiz
            return PerformTransactionResult.from(transaction);
        }
        User user = getUserOrThrowExcForPayme(transaction.getUserId());
        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new PaymeException(PaymeError.USER_CARD_NOT_FOUND));

        checkTimeoutOrThrowExc(transaction);

        transaction.setState(PaymeTransactionState.PAID);
        transaction.setPerformTime(System.currentTimeMillis());
        paymeTransactionRepository.save(transaction);

        BigDecimal oldBalance = card.getBalance();
        BigDecimal newBalance = oldBalance.add(transaction.getAmount());

        // ✅ WalletTransaction ni save qilib, ID ni olamiz
        WalletTransaction walletTransaction = walletTransactionRepository.save(
                WalletTransaction.builder()
                        .user(user)
                        .type(TransactionType.EARN)
                        .amount(transaction.getAmount())
                        .balanceAfter(newBalance)
                        .description("Payme orqali balans to'ldirildi")
                        .status(TransactionStatus.COMPLETED)
                        .paymentMethod(PaymentMethod.PAYME)
                        .build()
        );

        // ✅ WalletTransaction ID ni PaymeTransaction ga saqlaymiz
        transaction.setWalletTransactionId(walletTransaction.getId());
        transaction.setState(PaymeTransactionState.PAID);
        transaction.setPerformTime(System.currentTimeMillis());
        paymeTransactionRepository.save(transaction);

        card.setBalance(newBalance);
        cardRepository.save(card);

        return PerformTransactionResult.from(transaction);
    }

    /**
     * CancelTransaction - tranzaksiyani bekor qilish
     */
    private CancelTransactionResult cancelTransaction(PaymeParam params) {
        PaymeTransaction paymeTransaction = getTransactionOrThrowExc(params.getId());
        User user = getUserOrThrowExcForPayme(paymeTransaction.getUserId());
        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new PaymeException(PaymeError.USER_CARD_NOT_FOUND));

        boolean canceled = false;

        if (Objects.equals(paymeTransaction.getState(), PaymeTransactionState.PENDING)) {
            paymeTransaction.setState(PaymeTransactionState.PENDING_CANCELED);
            canceled = true;

        } else if (Objects.equals(paymeTransaction.getState(), PaymeTransactionState.PAID)) {
            paymeTransaction.setState(PaymeTransactionState.PAID_CANCELED);


            if (paymeTransaction.getWalletTransactionId() != null) {
                WalletTransaction walletTransaction = walletTransactionRepository
                        .findById(paymeTransaction.getWalletTransactionId())
                        .orElse(null);

                if (walletTransaction != null && walletTransaction.getStatus() == TransactionStatus.COMPLETED) {
                    walletTransaction.setStatus(TransactionStatus.CANCELLED);
                    walletTransactionRepository.save(walletTransaction);

                    card.setBalance(card.getBalance().subtract(paymeTransaction.getAmount()));
                    cardRepository.save(card);
                }
            }

            canceled = true;
        }

        if (canceled) {
            paymeTransaction.setCancelTime(System.currentTimeMillis());
            paymeTransaction.setReason(params.getReason());
        }

        paymeTransactionRepository.save(paymeTransaction);

        return CancelTransactionResult.from(paymeTransaction);
    }

    /**
     * CheckTransaction - tranzaksiyani tekshirish
     */
    private CheckTransactionResult checkTransaction(PaymeParam params) {
        PaymeTransaction transaction = getTransactionOrThrowExc(params.getId());

        return CheckTransactionResult.from(transaction);
    }

    /**
     * GetStatement - bu ma'lum vaqt oralig'ida tranzaksiyalarni olish
     */
    private GetStatementResult getStatement(PaymeParam params) {
        // from va to larni qiyamatini birga kamaytiramiz va ko'paytiramiz
        // chunki between'da ikkala sonlarni o'rtasidagilar olinadi lekin biz teng bo'lganlarini ham qaytarishimiz kerak
        return new GetStatementResult(PaymeUtil.mapToGetTransactionDto(
                paymeTransactionRepository.findAllByCreateTimeIsBetween(params.getFrom() - 1, params.getTo() + 1))
        );
    }

    private PaymeTransaction getTransactionOrThrowExc(String id) {
        PaymeTransaction transaction = paymeTransactionRepository.findById(id).orElse(null);

        if (Objects.isNull(transaction)) {
            throw new PaymeException(PaymeError.TRANSACTION_NOT_FOUND);
        }

        return transaction;
    }

    // =========================
    // HELPER METHODS
    // =========================

    private User getUserOrThrowExcForClick(Long userId) {
        System.out.println("user_id: " + userId);
        User user = userRepository.findByIdCustom(userId).orElse(null);

        if (Objects.isNull(user)) {
            throw new ClickException(ClickResponseCode.USER_NOT_FOUND);
        }

        return user;
    }

    private User getUserOrThrowExcForPayme(Long userId) {
        User user = userRepository.findByIdCustom(userId).orElse(null);

        if (Objects.isNull(user)) {
            throw new PaymeException(PaymeError.USER_NOT_FOUND);
        }

        return user;
    }

    private void ifTransactionExist(PaymeTransaction transaction) {
        // agar tranzaksiya holati pending bo'lmasa exception otamiz
        if (transaction.getState() != PaymeTransactionState.PENDING) {
            throw new PaymeException(PaymeError.CANT_DO_OPERATION);
        }

        // agar timeout bo'lsa true qaytaradi
        checkTimeoutOrThrowExc(transaction);
    }

    private void checkTimeoutOrThrowExc(PaymeTransaction transaction) {
        if (System.currentTimeMillis() - transaction.getCreateTime() > time_expired) {
            cancelTransactionDueToTimeout(transaction);

            throw new PaymeException(PaymeError.CANT_DO_OPERATION);
        }
    }

    private void cancelTransactionDueToTimeout(PaymeTransaction transaction) {
        transaction.setState(PaymeTransactionState.PENDING_CANCELED);
        transaction.setReason(4);
        transaction.setCancelTime(System.currentTimeMillis());
        paymeTransactionRepository.save(transaction);
    }

    private BigDecimal convertTiynToSom(BigDecimal amountInTiyn) {
        return amountInTiyn.divide(new BigDecimal(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ClickException(ClickResponseCode.INVALID_AMOUNT);
        }
        return amount;
    }
}