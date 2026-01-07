package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.dto.superLike.*;
import com.example.tinder.entity.*;
import com.example.tinder.enums.*;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.*;
import com.example.tinder.repository.*;
import com.example.tinder.service.SuperLikeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SuperLikeServiceImpl implements SuperLikeService {

    private final SuperLikePackageRepository packageRepository;
    private final CardRepository cardRepository;
    private final SuperLikePurchaseRepository purchaseRepository;
    private final SuperLikeHistoryRepository historyRepository;
    private final WalletTransactionRepository transactionRepository;
    private final PlatformRevenueRepository revenueRepository;
    private final UserRepository userRepository;
    private final SwipeRepository swipeRepository;
    private final CardMapper cardMapper;
    private final SuperLikeHistoryMapper superLikeHistoryMapper;
    private final SuperLikePackageMapper superLikePackageMapper;
    private final SuperLikePurchaseMapper superLikePurchaseMapper;
    private final WalletTransactionMapper walletTransactionMapper;
    private final SuperLikePackageRepository superLikePackageRepository;

    private static final BigDecimal REWARD_PER_SUPERLIKE = new BigDecimal("5000.0");



    @Override
    public ApiResponse<?> getAllPackages() {
        List<SuperLikePackage> packages = packageRepository.findAllActivePackages();

        if (packages.isEmpty()) {
            return ApiResponse.success(new ArrayList<>(), "NO PACKAGES AVAILABLE");
        }

        List<SuperLikePackageDto> dtos = packages.stream()
                .map(superLikePackageMapper::toDto)
                .collect(Collectors.toList());

        return ApiResponse.success(dtos, "SUPER LIKE PACKAGES FETCHED SUCCESSFULLY");
    }

    @Override
    @Transactional
    public ApiResponse<?> purchaseSuperLikes(Long userId, PurchaseSuperLikeRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.USER_NOT_FOUND));

        SuperLikePackage pkg = packageRepository.findByIdAndVisibilityTrue(request.getPackageId())
                .orElseThrow(() ->  RestException.restThrow(ErrorType.PACKAGE_NOT_FOUND));

        Card card = cardRepository.findByUserId(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.CARD_NOT_FOUND));

        // To'lov jarayoni
        String transactionId = processPayment(card ,request.getPaymentMethod(), pkg.getPriceUZS());

        SuperLikePurchase purchase = SuperLikePurchase.builder()
                .user(user)
                .superLikePackage(pkg)
                .quantity(pkg.getQuantity())
                .amountPaid(pkg.getPriceUZS())
                .paymentMethod(request.getPaymentMethod())
                .transactionId(transactionId)
                .status(TransactionStatus.COMPLETED)
                .build();
        purchaseRepository.save(purchase);

        // Card ga super like qo'shish
        Integer oldCount = card.getSuperLikeCount();
        card.setSuperLikeCount(oldCount + pkg.getQuantity());
        cardRepository.save(card);


        // Wallet transaction yaratish
        createTransaction(user,
                TransactionType.PURCHASE,
                pkg.getPriceUZS(),
                card.getBalance(),
                null,
                "Super Like sotib olindi: " + pkg.getQuantity() + " ta");

        // Platform revenue hisobga olish
        calculatePlatformRevenue(purchase, pkg);

        return ApiResponse.success(superLikePurchaseMapper.toDto(purchase), SuccessType.SUPER_LIKES_PURCHASED_SUCCESSFULLY);
    }

    @Override
    @Transactional
    public ApiResponse<?> sendSuperLike(Long senderId, SendSuperLikeRequest request) {
        User sender = userRepository.findById(senderId)
                .orElseThrow(() -> {
                    log.error("Sender not found: {}", senderId);
                    return RestException.restThrow(ErrorType.USER_NOT_FOUND);
                });

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> {
                    log.error("Receiver not found: {}", request.getReceiverId());
                    return RestException.restThrow(ErrorType.USER_NOT_FOUND);
                });

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
                .orElseThrow(() ->  RestException.restThrow(ErrorType.CARD_NOT_FOUND));

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
        createTransaction(receiver, TransactionType.EARN,
                REWARD_PER_SUPERLIKE,
                receiverCard.getBalance(),
                history,
                "Super Like dan topildi: " + sender.getUsername());

        // Swipe yaratish (SUPERLIKE)
        Swipe swipe = new Swipe();
        swipe.setSwiper(sender);
        swipe.setSwiped(receiver);
        swipe.setSwipe(SwipeStatus.SUPERLIKE);
        swipeRepository.save(swipe);

        return ApiResponse.success(
                superLikeHistoryMapper.toDto(history), "SUPER LIKE SENT SUCCESSFULLY");
    }


    @Override
    public ApiResponse<?> getMyCard(Long userId) {
        Card card = cardRepository.findByUserId(userId)
                .orElseThrow(() -> RestException.restThrow(ErrorType.CARD_NOT_FOUND));

        return ApiResponse.success(cardMapper.toDto(card), "CARD FETCHED SUCCESSFULLY");
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

        return ApiResponse.success(stats, "STATISTICS FETCHED SUCCESSFULLY");
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

        return ApiResponse.success(response, "PURCHASE HISTORY FETCHED SUCCESSFULLY");
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

        return ApiResponse.success(response, "SENT SUPER LIKES FETCHED SUCCESSFULLY");
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

        return ApiResponse.success(response, "RECEIVED SUPER LIKES FETCHED SUCCESSFULLY");
    }


    @Override
    public ApiResponse<?> getMyTransactions(Long userId, Pageable pageable) {
        Page<WalletTransaction> transactions = transactionRepository.findAllByUserId(userId, pageable);

        Page<WalletTransactionDto> dtos = transactions.map(walletTransactionMapper::toDto);

        Map<String, Object> response = new HashMap<>();
        response.put("transactions", dtos.getContent());
        response.put("currentPage", dtos.getNumber());
        response.put("totalPages", dtos.getTotalPages());
        response.put("totalElements", dtos.getTotalElements());

        return ApiResponse.success(response, "TRANSACTIONS FETCHED SUCCESSFULLY");
    }

    @Override
    public ApiResponse<?> createPackage(SuperLikePackageDto.SuperLikePackageCreateDto dto) {
        if (dto.getQuantity() <= 0) throw RestException.restThrow(ErrorType.INVALID_PACKAGE_QUANTITY);
        if (dto.getPriceUZS() == null || dto.getPriceUZS().compareTo(BigDecimal.ZERO) <= 0)
            throw RestException.restThrow(ErrorType.INVALID_PACKAGE_PRICE);
        if (dto.getPricePerUnit() == null || dto.getPricePerUnit().compareTo(BigDecimal.ZERO) <= 0)
            throw RestException.restThrow(ErrorType.INVALID_PACKAGE_PRICE);
        if (dto.getPlatformCommission() == null || dto.getPlatformCommission().compareTo(BigDecimal.ZERO) < 0)
            throw RestException.restThrow(ErrorType.INVALID_PLATFORM_COMMISSION);

        SuperLikePackage save = packageRepository.save(superLikePackageMapper.fromCreateDto(dto));
        return ApiResponse.success(superLikePackageMapper.toDto(save), "PACKAGE CREATED SUCCESSFULLY");
    }

    @Override
    public ApiResponse<?> deletePackage(Long packageId) {
        Optional<SuperLikePackage> optional = superLikePackageRepository.findByIdAndVisibilityTrue(packageId);
        if (optional.isEmpty()) throw RestException.restThrow(ErrorType.PACKAGE_NOT_FOUND);

        SuperLikePackage entity = optional.get();
        entity.setVisibility(false);
        superLikePackageRepository.save(entity);

        return ApiResponse.success(superLikePackageMapper.toDto(entity), "PACKAGE DELETED SUCCESSFULLY");
    }


    private String processPayment(Card card,PaymentMethod method, BigDecimal amount) {
        //TODO Bu yerda to'lov gateway (Click, Payme) bilan integratsiya bo'ladi
        // Hozircha mock transaction ID qaytaramiz

        if (card.getBalance().intValue() < amount.intValue()){
            throw RestException.restThrow(ErrorType.INSUFFICIENT_BALANCE);
        }
        card.setBalance(card.getBalance().subtract(amount));
        cardRepository.save(card);
        return method.name() + "_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    private void createTransaction(User user, TransactionType type, BigDecimal amount,
                                   BigDecimal balanceAfter, SuperLikeHistory history, String description) {
        WalletTransaction transaction = WalletTransaction.builder()
                .user(user)
                .type(type)
                .amount(amount)
                .balanceAfter(balanceAfter != null ? balanceAfter : new BigDecimal("0.0"))
                .superLikeHistory(history)
                .description(description)
                .status(TransactionStatus.COMPLETED)
                .build();
        transactionRepository.save(transaction);
    }


    private void calculatePlatformRevenue(SuperLikePurchase purchase, SuperLikePackage pkg) {
        BigDecimal totalAmount = purchase.getAmountPaid();
        BigDecimal totalRewards = REWARD_PER_SUPERLIKE.multiply(BigDecimal.valueOf(pkg.getQuantity()));
        BigDecimal platformAmount = totalAmount.subtract(totalRewards);

        PlatformRevenue revenue = PlatformRevenue.builder()
                .purchase(purchase)
                .totalAmount(totalAmount)
                .userRewardAmount(totalRewards)
                .platformAmount(platformAmount)
                .commissionRate(pkg.getPlatformCommission())
                .build();

        revenueRepository.save(revenue);

    }

}