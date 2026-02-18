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
import com.example.tinder.enums.SuccessType;
import com.example.tinder.enums.TransactionStatus;
import com.example.tinder.enums.TransactionType;
import com.example.tinder.enums.payment.PaymeError;
import com.example.tinder.enums.payment.*;
import com.example.tinder.exception.ClickException;
import com.example.tinder.exception.PaymeException;
import com.example.tinder.repository.*;
import com.example.tinder.service.PaymentService;
import com.example.tinder.utils.ClickUtil;
import com.example.tinder.utils.PaymeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private static final long TIME_EXPIRED = 43_200_000L;

    private final CardRepository cardRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserRepository userRepository;
    private final LogRepository logRepository;

    // =========================
    // CLICK METHODS
    // =========================
    @Override
    @Transactional
    public ClickPrepareResponse prepare(ClickPrepareRequest request) {
        saveLog(
                Log.builder()
                        .text("Click Prepare request: " + request)
                        .date(LocalDateTime.now())
                        .build()
        );

        log.info("Click Prepare request: {}", request);

        validateClickActionAndService(request.getAction(), ClickUtil.PREPARE_ACTION, request.getServiceId());

        User user = getUserOrThrowExcForClick(request.getMerchantTransId());
        BigDecimal amount = validateAmount(request.getAmount());

        if (!cardRepository.existsByUserId(user.getId())) {
            saveLog(
                    Log.builder()
                            .text("USER_CARD_NOT_FOUND in prepare()")
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new ClickException(ClickResponseCode.USER_CARD_NOT_FOUND);
        }

        String providerTxId = String.valueOf(request.getClickTransId());

        PaymentTransaction existing = paymentTransactionRepository
                .findByPaymentMethodAndProviderTransactionId(PaymentMethod.CLICK, providerTxId)
                .orElse(null);

        if (existing != null) {
            if (existing.getClickPaydocId() == null && request.getClickPaydocId() != null) {
                existing.setClickPaydocId(request.getClickPaydocId());
                paymentTransactionRepository.save(existing);
            }
            validateExistingClickPrepare(existing, request, amount, user.getId());
            return ClickPrepareResponse.from(request, existing.getId());
        }

        PaymentTransaction saved = paymentTransactionRepository.save(
                PaymentTransaction.builder()
                        .paymentMethod(PaymentMethod.CLICK)
                        .providerTransactionId(providerTxId)
                        .merchantTransactionId(request.getMerchantTransId())
                        .clickPaydocId(request.getClickPaydocId())
                        .userId(user.getId())
                        .amount(amount)
                        .prepareTime(LocalDateTime.now())
                        .clickStatus(PaymentStatusForClick.PREPARE)
                        .build()
        );
        saveLog(
                Log.builder()
                        .text("request saved.  TransactionId: " + saved.getId()
                        )
                        .date(LocalDateTime.now())
                        .build()
        );

        return ClickPrepareResponse.from(request, saved.getId());
    }

    @Override
    @Transactional
    public ClickCompleteResponse complete(ClickCompleteRequest request) {
        saveLog(
                Log.builder()
                        .text("Click Complete request: " + request)
                        .date(LocalDateTime.now())
                        .build()
        );

        log.info("Click Complete request: {}", request);

        validateClickActionAndService(request.getAction(), ClickUtil.COMPLETE_ACTION, request.getServiceId());

        PaymentTransaction transaction = getClickTransactionOrThrowExc(request.getMerchantPrepareId());
        validateClickCompleteRequest(transaction, request);

        User user = getUserOrThrowExcForClick(transaction.getUserId());
        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ClickException(ClickResponseCode.USER_CARD_NOT_FOUND));

        if (transaction.isClickComplete()) {
            saveLog(
                    Log.builder()
                            .text("Transaction Complete request: " + request
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            return ClickCompleteResponse.from(request, transaction.getWalletTransactionId());
        }

        if (transaction.getClickPaydocId() == null && request.getClickPaydocId() != null) {
            transaction.setClickPaydocId(request.getClickPaydocId());
            paymentTransactionRepository.save(transaction);
        }

        if (transaction.isClickCancel()) {

            saveLog(
                    Log.builder()
                            .text("Transaction Cancelled "
                            )
                            .date(LocalDateTime.now())
                            .build()
            );

            throw new ClickException(ClickResponseCode.TRANSACTION_CANCELLED);
        }

        if (request.getError() != ClickResponseCode.SUCCESS.getCode()) {
            transaction.setClickStatus(PaymentStatusForClick.CANCEL);
            transaction.setClickCancelTime(LocalDateTime.now());
            paymentTransactionRepository.save(transaction);

            saveLog(
                    Log.builder()
                            .text("Transaction Cancelled "
                            )
                            .date(LocalDateTime.now())
                            .build()
            );

            throw new ClickException(ClickResponseCode.TRANSACTION_CANCELLED);
        }

        BigDecimal oldBalance = card.getBalance();
        BigDecimal newBalance = oldBalance.add(transaction.getAmount());

        WalletTransaction walletTransaction = walletTransactionRepository.save(
                WalletTransaction.builder()
                        .user(user)
                        .type(TransactionType.EARN)
                        .amount(transaction.getAmount())
                        .balanceAfter(newBalance)
                        .description("Click orqali balans to'ldirildi")
                        .status(TransactionStatus.COMPLETED)
                        .paymentMethod(PaymentMethod.CLICK)
                        .build()
        );

        transaction.setWalletTransactionId(walletTransaction.getId());
        transaction.setClickStatus(PaymentStatusForClick.COMPLETE);
        transaction.setCompleteTime(LocalDateTime.now());
        paymentTransactionRepository.save(transaction);

        card.setBalance(newBalance);
        cardRepository.save(card);

        saveLog(
                Log.builder()
                        .text("Click transaction completed:  " + transaction.getId() + "\n" +
                                "Amount: " + transaction.getAmount() + "\n" +
                                "Balance: " + oldBalance + "-> \n " + newBalance
                        )
                        .date(LocalDateTime.now())
                        .build()
        );

        log.info("Click transaction completed: {} | Amount: {} | Balance: {} -> {}",
                transaction.getId(), transaction.getAmount(), oldBalance, newBalance);

        return ClickCompleteResponse.from(request, walletTransaction.getId());
    }

    @Override
    public ApiResponse<String> getUrlForClick(BigDecimal amount) {
        validateAmount(amount);

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new ClickException(ClickResponseCode.UNAUTHORIZED);
        }

        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ApiResponse.success(ClickUtil.generatePaymentUrl(user.getId(), amount), SuccessType.SUCCESS);
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

    private CheckPerformTransactionResult checkPerformTransaction(PaymeParam params) {
        User user = getUserOrThrowExcForPayme(params.getAccount().getUserId());

        if (params.getAmount() == null || params.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymeException(PaymeError.INVALID_AMOUNT);
        }

        if (!cardRepository.existsByUserId(user.getId())) {
            saveLog(
                    Log.builder()
                            .text("USER_CARD_NOT_FOUND in checkPerformTransaction()")
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new PaymeException(PaymeError.USER_CARD_NOT_FOUND);
        }

        return new CheckPerformTransactionResult(true);
    }

    private CreateTransactionResult createTransaction(PaymeParam params) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findByPaymentMethodAndProviderTransactionId(PaymentMethod.PAYME, params.getId())
                .orElse(null);

        if (Objects.nonNull(transaction)) {
            ifTransactionExist(transaction);
            return CreateTransactionResult.from(transaction);
        }

        checkPerformTransaction(params);

        BigDecimal amountInSom = convertTiynToSom(params.getAmount());

        PaymentTransaction saved = paymentTransactionRepository.save(
                PaymentTransaction.builder()
                        .paymentMethod(PaymentMethod.PAYME)
                        .providerTransactionId(params.getId())
                        .merchantTransactionId(params.getAccount().getUserId())
                        .userId(params.getAccount().getUserId())
                        .amount(amountInSom)
                        .createTime(System.currentTimeMillis())
                        .state(PaymeTransactionState.PENDING)
                        .build()
        );

        return CreateTransactionResult.from(saved);
    }

    private PerformTransactionResult performTransaction(PaymeParam params) {
        PaymentTransaction transaction = getPaymeTransactionOrThrowExc(params.getId());

        if (!Objects.equals(transaction.getState(), PaymeTransactionState.PENDING)) {
            if (!Objects.equals(transaction.getState(), PaymeTransactionState.PAID)) {

                saveLog(
                        Log.builder()
                                .text("CANT_DO_OPERATION in performTransaction()"
                                )
                                .date(LocalDateTime.now())
                                .build()
                );

                throw new PaymeException(PaymeError.CANT_DO_OPERATION);
            }
            return PerformTransactionResult.from(transaction);
        }

        User user = getUserOrThrowExcForPayme(transaction.getUserId());
        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new PaymeException(PaymeError.USER_CARD_NOT_FOUND));

        checkTimeoutOrThrowExc(transaction);

        BigDecimal oldBalance = card.getBalance();
        BigDecimal newBalance = oldBalance.add(transaction.getAmount());

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

        transaction.setWalletTransactionId(walletTransaction.getId());
        transaction.setState(PaymeTransactionState.PAID);
        transaction.setPerformTime(System.currentTimeMillis());
        paymentTransactionRepository.save(transaction);

        card.setBalance(newBalance);
        cardRepository.save(card);

        return PerformTransactionResult.from(transaction);
    }

    private CancelTransactionResult cancelTransaction(PaymeParam params) {
        PaymentTransaction transaction = getPaymeTransactionOrThrowExc(params.getId());
        User user = getUserOrThrowExcForPayme(transaction.getUserId());
        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> new PaymeException(PaymeError.USER_CARD_NOT_FOUND));

        boolean canceled = false;

        if (Objects.equals(transaction.getState(), PaymeTransactionState.PENDING)) {
            transaction.setState(PaymeTransactionState.PENDING_CANCELED);
            canceled = true;
        } else if (Objects.equals(transaction.getState(), PaymeTransactionState.PAID)) {
            transaction.setState(PaymeTransactionState.PAID_CANCELED);

            if (transaction.getWalletTransactionId() != null) {
                WalletTransaction walletTransaction = walletTransactionRepository
                        .findById(transaction.getWalletTransactionId())
                        .orElse(null);

                if (walletTransaction != null && walletTransaction.getStatus() == TransactionStatus.COMPLETED) {
                    walletTransaction.setStatus(TransactionStatus.CANCELLED);
                    walletTransactionRepository.save(walletTransaction);

                    card.setBalance(card.getBalance().subtract(transaction.getAmount()));
                    cardRepository.save(card);
                }
            }

            canceled = true;
        }

        if (canceled) {
            transaction.setCancelTime(System.currentTimeMillis());
            transaction.setReason(params.getReason());
        }

        paymentTransactionRepository.save(transaction);

        return CancelTransactionResult.from(transaction);
    }

    private CheckTransactionResult checkTransaction(PaymeParam params) {
        PaymentTransaction transaction = getPaymeTransactionOrThrowExc(params.getId());
        return CheckTransactionResult.from(transaction);
    }

    private GetStatementResult getStatement(PaymeParam params) {
        return new GetStatementResult(PaymeUtil.mapToGetTransactionDto(
                paymentTransactionRepository.findAllByPaymentMethodAndCreateTimeIsBetween(
                        PaymentMethod.PAYME,
                        params.getFrom() - 1,
                        params.getTo() + 1
                )
        ));
    }

    // =========================
    // HELPER METHODS
    // =========================

    private void validateClickActionAndService(Integer action, Integer expectedAction, Integer serviceId) {
        if (!Objects.equals(action, expectedAction)) {
            saveLog(
                    Log.builder()
                            .text("Action not found in validateClickActionAndService() "
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new ClickException(ClickResponseCode.ACTION_NOT_FOUND);
        }

        if (!ClickUtil.isValidServiceId(serviceId)) {
            saveLog(
                    Log.builder()
                            .text("Bad Request in validateClickActionAndService()"
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new ClickException(ClickResponseCode.BAD_REQUEST);
        }
    }

    private void validateExistingClickPrepare(PaymentTransaction existing, ClickPrepareRequest request,
                                              BigDecimal requestedAmount, Long userId) {
        if (!Objects.equals(existing.getUserId(), userId)
                || !Objects.equals(existing.getMerchantTransactionId(), request.getMerchantTransId())
                || existing.getAmount().compareTo(requestedAmount) != 0) {

            saveLog(
                    Log.builder()
                            .text("BAD_REQUEST in validateExistingClickPrepare() "
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new ClickException(ClickResponseCode.BAD_REQUEST);
        }

        if (existing.isClickComplete()) {
            saveLog(
                    Log.builder()
                            .text("ALREADY_PAID in validateExistingClickPrepare() "
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new ClickException(ClickResponseCode.ALREADY_PAID);
        }

        if (existing.isClickCancel()) {
            saveLog(
                    Log.builder()
                            .text("Transaction Cancelled in validateExistingClickPrepare() "
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new ClickException(ClickResponseCode.TRANSACTION_CANCELLED);
        }
    }

    private void validateClickCompleteRequest(PaymentTransaction transaction, ClickCompleteRequest request) {
        if (!Objects.equals(transaction.getProviderTransactionId(), String.valueOf(request.getClickTransId()))) {
            saveLog(
                    Log.builder()
                            .text("Transaction not found in validateClickCompleteRequest() "
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new ClickException(ClickResponseCode.TRANSACTION_NOT_FOUND);
        }

        if (!Objects.equals(transaction.getMerchantTransactionId(), request.getMerchantTransId())) {

            saveLog(
                    Log.builder()
                            .text("Bad Request in validateClickCompleteRequest() "
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new ClickException(ClickResponseCode.BAD_REQUEST);
        }

        if (request.getClickPaydocId() != null
                && transaction.getClickPaydocId() != null
                && !Objects.equals(transaction.getClickPaydocId(), request.getClickPaydocId())) {
            throw new ClickException(ClickResponseCode.BAD_REQUEST);
        }

        BigDecimal amount = validateAmount(request.getAmount());
        if (transaction.getAmount().compareTo(amount) != 0) {
            throw new ClickException(ClickResponseCode.INVALID_AMOUNT);
        }
    }

    private PaymentTransaction getClickTransactionOrThrowExc(Long transactionId) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findByIdAndPaymentMethod(transactionId, PaymentMethod.CLICK)
                .orElse(null);

        if (Objects.isNull(transaction)) {
            saveLog(
                    Log.builder()
                            .text("Transaction not found in getClickTransactionOrThrowEx() "
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new ClickException(ClickResponseCode.TRANSACTION_NOT_FOUND);
        }

        return transaction;
    }

    private PaymentTransaction getPaymeTransactionOrThrowExc(String id) {
        PaymentTransaction transaction = paymentTransactionRepository
                .findByPaymentMethodAndProviderTransactionId(PaymentMethod.PAYME, id)
                .orElse(null);

        if (Objects.isNull(transaction)) {
            saveLog(
                    Log.builder()
                            .text("Transaction not found in getPaymeTransactionOrThrowExc() "
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new PaymeException(PaymeError.TRANSACTION_NOT_FOUND);
        }

        return transaction;
    }

    private User getUserOrThrowExcForClick(Long userId) {
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

    private void ifTransactionExist(PaymentTransaction transaction) {
        if (transaction.getState() != PaymeTransactionState.PENDING) {
            throw new PaymeException(PaymeError.CANT_DO_OPERATION);
        }

        checkTimeoutOrThrowExc(transaction);
    }

    private void checkTimeoutOrThrowExc(PaymentTransaction transaction) {
        if (System.currentTimeMillis() - transaction.getCreateTime() > TIME_EXPIRED) {
            cancelTransactionDueToTimeout(transaction);
            saveLog(
                    Log.builder()
                            .text("CANT_DO_OPERATION in checkTimeoutOrThrowExc()"
                            )
                            .date(LocalDateTime.now())
                            .build()
            );
            throw new PaymeException(PaymeError.CANT_DO_OPERATION);
        }
    }

    private void cancelTransactionDueToTimeout(PaymentTransaction transaction) {
        transaction.setState(PaymeTransactionState.PENDING_CANCELED);
        transaction.setReason(4);
        transaction.setCancelTime(System.currentTimeMillis());
        paymentTransactionRepository.save(transaction);
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

    private void saveLog(Log logEntry) {
        try {
            logRepository.save(logEntry);
        } catch (Exception firstEx) {
            try {
                if (logEntry.getId() == null) {
                    long fallbackId = Math.abs(System.currentTimeMillis() * 1000L + java.util.concurrent.ThreadLocalRandom.current().nextInt(1000));
                    logEntry.setId(fallbackId);
                    logRepository.save(logEntry);
                    return;
                }
            } catch (Exception secondEx) {
                log.error("PaymentService log write failed: {}", secondEx.getMessage());
                return;
            }

            log.error("PaymentService log write failed: {}", firstEx.getMessage());
        }
    }

}
