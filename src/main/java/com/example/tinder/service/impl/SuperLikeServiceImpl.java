package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.*;
import com.example.tinder.entity.*;
import com.example.tinder.enums.*;
import com.example.tinder.enums.payment.PaymentMethod;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.SuperLikeHistoryMapper;
import com.example.tinder.mapper.SuperLikePackageMapper;
import com.example.tinder.mapper.SuperLikePurchaseMapper;
import com.example.tinder.repository.*;
import com.example.tinder.service.PlatformRevenueService;
import com.example.tinder.service.SuperLikeService;
import com.example.tinder.service.SwipeService;
import com.example.tinder.service.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuperLikeServiceImpl implements SuperLikeService {

    private final SuperLikePackageRepository packageRepository;
    private final CardRepository cardRepository;
    private final SuperLikePurchaseRepository purchaseRepository;
    private final SuperLikeHistoryRepository historyRepository;
    private final UserRepository userRepository;
    private final SuperLikeHistoryMapper superLikeHistoryMapper;
    private final SuperLikePackageMapper superLikePackageMapper;
    private final SuperLikePurchaseMapper superLikePurchaseMapper;
    private final SwipeService swipeService;
    private final PlatformRevenueService platformRevenueService;
    private final WalletTransactionService walletTransactionService;
    private final WalletTransactionRepository walletTransactionRepository;

    private static final BigDecimal REWARD_PER_SUPERLIKE = new BigDecimal("5000.0");

    @Override
    public ApiResponse<?> getAllPackages() {
        List<SuperLikePackage> packages = packageRepository.findAllActivePackages();

        if (packages.isEmpty()) {
            return ApiResponse.success(new ArrayList<>(), SuccessType.NO_SUPER_LIKE_PACKAGES_FOUND);
        }

        List<SuperLikePackageDto> result = packages.stream()
                .map(superLikePackageMapper::toDto)
                .toList();

        return ApiResponse.success(result, SuccessType.SUPER_LIKE_PACKAGES_FETCHED_SUCCESSFULLY);
    }

    @Override
    @Transactional
    public ApiResponse<?> purchaseSuperLikes(Long userId, PurchaseSuperLikeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        SuperLikePackage pkg = packageRepository.findByIdAndVisibilityTrue(request.getPackageId())
                .orElseThrow(() -> RestException.restThrow(ErrorType.PACKAGE_NOT_FOUND));

        Card card = cardRepository.findByUserId(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.CARD_NOT_FOUND));

        // To'lov jarayoni
        String transactionId = processPayment(card, pkg.getPriceUZS());

        SuperLikePurchase purchase = SuperLikePurchase.builder()
                .user(user)
                .superLikePackage(pkg)
                .quantity(pkg.getQuantity())
                .amountPaid(pkg.getPriceUZS())
                .transactionId(transactionId)
                .status(TransactionStatus.COMPLETED)
                .build();
        purchaseRepository.save(purchase);

        // Card ga super like qo'shish
        Integer oldCount = card.getSuperLikeCount();
        card.setSuperLikeCount(oldCount + pkg.getQuantity());
        cardRepository.save(card);

        // 1. WalletTransaction yaratish va saqlash
        WalletTransaction walletTx = WalletTransaction.builder()
                .user(user)
                .type(TransactionType.PURCHASE)
                .amount(pkg.getPriceUZS())
                .balanceAfter(card.getBalance())           // processPayment da balans yangilangan
                .paymentMethod(PaymentMethod.PAYME)         // yoki kerakli usul
                .description("Super Like sotib olindi: " + pkg.getQuantity() + " ta")
                .status(TransactionStatus.COMPLETED)
                .build();

        walletTransactionRepository.save(walletTx);   //


        // Wallet transaction yaratish
//        walletTransactionService.createTransaction(user,
//                TransactionType.PURCHASE,
//                pkg.getPriceUZS(),
//                card.getBalance(),
//                null,
//                "Super Like sotib olindi: " + pkg.getQuantity() + " ta");

        // Platform revenue hisobga olish
        platformRevenueService.recordPlatformRevenue(purchase, pkg,walletTx );

        log.info("SuperLike sotib olindi → PurchaseId: {}, WalletTxId: {}",
                purchase.getId(), walletTx.getId());

        return ApiResponse.success(superLikePurchaseMapper.toDto(purchase), SuccessType.SUPER_LIKES_PURCHASED_SUCCESSFULLY);
    }

    @Override
    @Transactional
    public ApiResponse<?> sendSuperLike(Long senderId, SendSuperLikeRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        if (senderId.equals(request.getReceiverId())) {
            throw RestException.restThrow(ErrorType.CANNOT_SUPERLIKE_YOURSELF);
        }

        // Sender card'ini tekshirish
        Card senderCard = cardRepository.findByUserId(senderId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.CARD_NOT_FOUND));

        if (senderCard.getSuperLikeCount() <= 0) {
            throw RestException.restThrow(ErrorType.INSUFFICIENT_SUPERLIKES);
        }

        if (historyRepository.existsBySenderIdAndReceiverId(senderId, request.getReceiverId())) {
            throw RestException.restThrow(ErrorType.SUPERLIKE_ALREADY_SENT);
        }

        Card receiverCard = cardRepository.findByUserId(request.getReceiverId())
                .orElseThrow(() -> RestException.restThrow(ErrorType.CARD_NOT_FOUND));

        // Super Like ni kamaytirish
        Integer oldSenderCount = senderCard.getSuperLikeCount();
        senderCard.setSuperLikeCount(oldSenderCount - 1);
        cardRepository.save(senderCard);


        // Super Like history yaratish
        SuperLikeHistory history = SuperLikeHistory.builder()
                .sender(sender)
                .receiver(receiver)
                .rewardAmount(REWARD_PER_SUPERLIKE)
                .isRewardPaid(true)
                .build();
        historyRepository.save(history);

        // Receiver card'iga pul tushirish
        BigDecimal currentBalance = receiverCard.getBalance() != null ? receiverCard.getBalance() : new BigDecimal("0.0");
        BigDecimal currentTotalEarned = receiverCard.getTotalEarnedUZS() != null ? receiverCard.getTotalEarnedUZS() : new BigDecimal("0.0");
        Integer currentEarnedCount = receiverCard.getTotalSuperLikesEarned();

        BigDecimal newBalance = currentBalance.add(REWARD_PER_SUPERLIKE);
        BigDecimal newTotalEarned = currentTotalEarned.add(REWARD_PER_SUPERLIKE);
        Integer newEarnedCount = currentEarnedCount + 1;

        receiverCard.setBalance(newBalance);
        receiverCard.setTotalSuperLikesEarned(newEarnedCount);
        receiverCard.setTotalEarnedUZS(newTotalEarned);
        cardRepository.save(receiverCard);

        // Transaction yaratish
        walletTransactionService.createTransaction(receiver, TransactionType.EARN,
                REWARD_PER_SUPERLIKE,
                receiverCard.getBalance(),
                history,
                "Super Like dan topildi: " + sender.getUsername());

        return swipeService.swipe(senderId, request.getReceiverId(), SwipeStatus.SUPERLIKE);
    }

    @Override
    public ApiResponse<?> getSuperLikeStats(Long userId) {
        Card card = cardRepository.findByUserId(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.CARD_NOT_FOUND));

        Long unpaidRewards = historyRepository.countUnpaidRewards(userId);
        Long sentCount = historyRepository.countSentSuperLikes(userId);
        Long receivedCount = historyRepository.countReceivedSuperLikes(userId);

        SuperLikeStatsDto stats = SuperLikeStatsDto.builder()
                .totalSuperLikesAvailable(card.getSuperLikeCount())
                .totalSuperLikesSent(sentCount.intValue())
                .totalSuperLikesReceived(receivedCount.intValue())
                .totalEarningsUZS(card.getTotalEarnedUZS() != null ? card.getTotalEarnedUZS() : new BigDecimal("0.0"))
                .currentBalanceUZS(card.getBalance() != null ? card.getBalance() : new BigDecimal("0.0"))
                .unpaidRewardsCount(unpaidRewards)
                .build();

        return ApiResponse.success(stats, SuccessType.STATISTICS_FETCHED_SUCCESSFULLY);
    }


    @Override
    public ApiResponse<?> getMyPurchases(Long userId, Pageable pageable) {
        Page<SuperLikePurchase> purchases = purchaseRepository.findAllByUserId(userId, pageable);

        Page<SuperLikePurchaseDto> dtos = purchases.map(superLikePurchaseMapper::toDto);

        Map<String, Object> response = new HashMap<>();
        response.put("purchases", dtos.getContent());
        response.put("currentPage", dtos.getNumber());
        response.put("totalPages", dtos.getTotalPages());
        response.put("totalElements", dtos.getTotalElements());

        return ApiResponse.success(response, SuccessType.SUPER_LIKES_PURCHASES_FETCHED_SUCCESSFULLY);
    }

    @Override
    public ApiResponse<?> getMySentSuperLikes(Long userId, Pageable pageable) {
        Page<SuperLikeHistory> history = historyRepository.findAllSentSuperLikes(userId, pageable);

        Page<SuperLikeHistoryDto> dtos = history.map(superLikeHistoryMapper::toDto);

        Map<String, Object> response = new HashMap<>();
        response.put("superLikes", dtos.getContent());
        response.put("currentPage", dtos.getNumber());
        response.put("totalPages", dtos.getTotalPages());
        response.put("totalElements", dtos.getTotalElements());

        return ApiResponse.success(response, SuccessType.SENT_SUPER_LIKES_FETCHED_SUCCESSFULLY);
    }


    @Override
    public ApiResponse<?> getMyReceivedSuperLikes(Long userId, Pageable pageable) {
        Page<SuperLikeHistory> history = historyRepository.findAllReceivedSuperLikes(userId, pageable);

        Page<SuperLikeHistoryDto> dtos = history.map(superLikeHistoryMapper::toDto);

        Map<String, Object> response = new HashMap<>();
        response.put("superLikes", dtos.getContent());
        response.put("currentPage", dtos.getNumber());
        response.put("totalPages", dtos.getTotalPages());
        response.put("totalElements", dtos.getTotalElements());

        return ApiResponse.success(response, SuccessType.RECEIVED_SUPER_LIKES_FETCHED_SUCCESSFULLY);
    }


    private String processPayment(Card card, BigDecimal amount) {
        //TODO Bu yerda to'lov gateway (Click, Payme) bilan integratsiya bo'ladi
        // Hozircha mock transaction ID qaytaramiz

        if (card.getBalance().intValue() < amount.intValue()) {
            throw RestException.restThrow(ErrorType.INSUFFICIENT_BALANCE);
        }
        card.setBalance(card.getBalance().subtract(amount));
        cardRepository.save(card);
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


}