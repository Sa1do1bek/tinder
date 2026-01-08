package com.example.tinder.mapper;

import com.example.tinder.dto.CardDto;
import com.example.tinder.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {


    public CardDto toDto(Card card) {
        if (card == null) return null;
        CardDto cardDto = new CardDto();
        cardDto.setBalance(card.getBalance());
        cardDto.setNumber(card.getNumber());
        cardDto.setUserId(card.getUser().getId());
        cardDto.setId(card.getId());
        cardDto.setTotalEarnedUZS(card.getTotalEarnedUZS());
        cardDto.setSuperLikeCount(card.getSuperLikeCount());
        cardDto.setTotalSuperLikesEarned(card.getTotalSuperLikesEarned());
        return cardDto;
    }
}