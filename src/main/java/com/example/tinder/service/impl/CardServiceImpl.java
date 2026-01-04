package com.example.tinder.service.impl;

import com.example.tinder.dto.base.ApiResponse;
import com.example.tinder.entity.Card;
import com.example.tinder.entity.User;
import com.example.tinder.enums.ErrorType;
import com.example.tinder.exception.RestException;
import com.example.tinder.mapper.CardMapper;
import com.example.tinder.repository.CardRepository;
import com.example.tinder.service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {
    private final CardRepository cardRepository;
    private final CardMapper cardMapper;


    @Override
    public ApiResponse<?> getBalance(User user) {
        Optional<Card> card = cardRepository.findByUserId(user.getId());
        if (card.isEmpty()) throw RestException.restThrow(ErrorType.CARD_NOT_FOUND);
        return ApiResponse.success(cardMapper.toDto(card.get()), "Card balance retrieved successfully");
    }
}