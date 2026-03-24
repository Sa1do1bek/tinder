package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.*;
import com.example.tinder.entity.SubscriptionPlan;
import com.example.tinder.enums.*;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.UserSubscriptionMapper;
import com.example.tinder.repository.*;
import com.example.tinder.service.UserSubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSubscriptionServiceImpl implements UserSubscriptionService {
    private final UserSubscriptionRepository userSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final WalletTransactionRepository walletTransactionRepository;
    private final UserSubscriptionMapper userSubscriptionMapper;

    @Override
    public ApiResponse<?> create(User user, Long subscriptionPlanId) {

        Optional<SubscriptionPlan> optionalPlan = subscriptionPlanRepository.findById(subscriptionPlanId);
        if (optionalPlan.isEmpty()) throw RestException.restThrow(ErrorType.SUBSCRIPTION_PLAN_NOT_FOUND);
        SubscriptionPlan plan = optionalPlan.get();

        Card card = cardRepository.findByUserId(user.getId())
                .orElseThrow(() -> RestException.restThrow(ErrorType.CARD_NOT_FOUND));

        if (card.getBalance().intValue() < plan.getPrice().intValue()){
            throw RestException.restThrow(ErrorType.INSUFFICIENT_BALANCE);
        }
        card.setBalance(card.getBalance().subtract(plan.getPrice()));
        Card savedCard = cardRepository.save(card);
        BigDecimal balanceAfter = savedCard.getBalance();

        WalletTransaction transaction = WalletTransaction.builder()
                .user(user)
                .type(TransactionType.WITHDRAW)
                .amount(plan.getPrice())
                .balanceAfter(balanceAfter != null ? balanceAfter : new BigDecimal("0.0"))
                .superLikeHistory(null)
                .description("Subscription Plan Payment")
                .status(TransactionStatus.COMPLETED)
                .build();
        walletTransactionRepository.save(transaction);

        UserSubscription entity = new UserSubscription();
        entity.setUser(user);
        entity.setSubscriptionPlan(plan);
        entity.setSubscriptionType(plan.getType());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setEndAt(LocalDateTime.now().plusMonths(plan.getMonths()));
        entity.setVisibility(true);
        userSubscriptionRepository.save(entity);

        user.setSubscriptionType(plan.getType());
        userRepository.save(user);

        return ApiResponse.success(userSubscriptionMapper.toDto(entity), SuccessType.SUBSCRIPTION_CREATED_SUCCESSFULLY);
    }



    @Override
    public void checkSubscriptions() {
        LocalDateTime now = LocalDateTime.now();

        List<UserSubscription> expiredSubscriptions =
                userSubscriptionRepository.findAllExpiredAndActive(now);

        for (UserSubscription subscription : expiredSubscriptions) {
            expireSubscription(subscription);
        }
    }

    private void expireSubscription(UserSubscription subscription) {

        User user = subscription.getUser();
        user.setSubscriptionType(SubscriptionType.FREE);
        userRepository.save(user);

        subscription.setVisibility(false);
        userSubscriptionRepository.save(subscription);
    }

}
