package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.WalletTransactionDto;
import com.example.tinder.dto.wallet.WalletTransactionProjection;
import com.example.tinder.dto.wallet.record.UserDetailsDto;
import com.example.tinder.dto.wallet.record.WalletResponseDto;
import com.example.tinder.entity.SuperLikeHistory;
import com.example.tinder.entity.User;
import com.example.tinder.entity.WalletTransaction;
import com.example.tinder.enums.SuccessType;
import com.example.tinder.enums.TransactionStatus;
import com.example.tinder.enums.TransactionType;
import com.example.tinder.enums.payment.PaymentMethod;
import com.example.tinder.mapper.WalletTransactionMapper;
import com.example.tinder.repository.WalletTransactionRepository;
import com.example.tinder.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {
    private final WalletTransactionRepository walletTransactionRepository;
    private final WalletTransactionMapper walletTransactionMapper;

    @Override
    public void createTransaction(User user, TransactionType type, BigDecimal amount, BigDecimal balanceAfter, SuperLikeHistory history, String description) {
        WalletTransaction transaction = WalletTransaction.builder()
                .user(user)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter != null ? balanceAfter : new BigDecimal("0.0"))
                .superLikeHistory(history)
                .description(description)
                .status(TransactionStatus.COMPLETED)
                .build();
        walletTransactionRepository.save(transaction);
    }

    @Override
    public ApiResponse<?> getMyTransactions(Long userId, Pageable pageable) {
        Page<WalletTransaction> transactions = walletTransactionRepository.findAllByUserId(userId, pageable);

        Page<WalletTransactionDto> dtos = transactions.map(walletTransactionMapper::toDto);

        Map<String, Object> response = new HashMap<>();
        response.put("transactions", dtos.getContent());
        response.put("currentPage", dtos.getNumber());
        response.put("totalPages", dtos.getTotalPages());
        response.put("totalElements", dtos.getTotalElements());

        return ApiResponse.success(response, SuccessType.TRANSACTIONS_FETCHED_SUCCESSFULLY);
    }

    @Override
    @Transactional(readOnly = true)
    public ApiResponse<Page<?>> getAllWallet(LocalDate start, LocalDate end, PaymentMethod paymentMethod, int page, int size) {


        Page<WalletTransactionProjection> allProjected = validateProjection(start,end,paymentMethod,page,size);

            return ApiResponse.success(allProjected.map(w -> new WalletResponseDto(
                w.getId(),
                w.getAmount(),
                w.getBalanceAfter(),
                w.getType(),
                w.getStatus(),
                w.getCreatedAt(),
                w.getPaymentMethod(),
                w.getDescription(),
                new UserDetailsDto(
                        w.getUser().getId(),
                        w.getUser().getUsername(),
                        w.getUser().getProfilePic(),
                        w.getUser().getEmail(),
                        w.getUser().getPhoneNumber()
        ))), SuccessType.PAYMENT_SUCCESSFULLY_GET);

        }


    //******************
    // HELPER METHODS
    //******************

    private Page<WalletTransactionProjection> getWithStartDate(LocalDate start, Pageable pageable){
        LocalDateTime startDate = start.atStartOfDay();
        return walletTransactionRepository.findAllProjected(startDate,pageable);
    }

    private Page<WalletTransactionProjection> getWithStartDateAndEndDate(LocalDate start, LocalDate end, Pageable pageable){
        LocalDateTime startDate = start.atStartOfDay();
        return walletTransactionRepository.findAllProjected(startDate,end.atStartOfDay().plusDays(1),pageable);
    }

    private Page<WalletTransactionProjection> getWithEndDate(LocalDate endDate, Pageable pageable){
        return walletTransactionRepository.findAllProjectedWithEndDate(endDate.atStartOfDay().plusDays(1),pageable);
    }

    private Page<WalletTransactionProjection> getWithMethod(PaymentMethod method, Pageable pageable){
        return walletTransactionRepository.findAllProjected(method, pageable);
    }

    private Page<WalletTransactionProjection> validateProjection(LocalDate start, LocalDate end, PaymentMethod paymentMethod, int page, int size){
        PageRequest pageRequest = PageRequest.of(page, size);

        Page<WalletTransactionProjection> allProjected;

        if (start != null && end != null && paymentMethod != null) {
            allProjected = walletTransactionRepository
                    .findAllProjected(
                            paymentMethod,
                            start.atStartOfDay(),
                            end.atStartOfDay().plusDays(1),
                            pageRequest);
        } else if (start == null && end == null && paymentMethod != null) {
            allProjected = getWithMethod(paymentMethod,pageRequest);
        } else if (start != null && end == null && paymentMethod == null){
            allProjected = getWithStartDate(start,pageRequest);
        } else if (start == null && end != null && paymentMethod == null) {
            allProjected = getWithEndDate(end,pageRequest);
        } else if (start != null && end != null) {
            allProjected = getWithStartDateAndEndDate(start,end,pageRequest);
        }else {
            allProjected = walletTransactionRepository.findAllProjected(pageRequest);
        }

        return allProjected;
    }
}
