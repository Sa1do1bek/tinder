package com.example.tinder.mapper;

import com.example.tinder.dto.CardDto;
import com.example.tinder.entity.Card;
import org.springframework.stereotype.Component;

@Component
public class CardMapper {

    public Card toCard(CardDto cardDto) {
        if (cardDto == null) return null;
        Card card = new Card();
        card.setBalance(cardDto.getBalance());
        card.setNumber(cardDto.getNumber());
        card.setUserId(cardDto.getUserId());
        card.setId(cardDto.getId());

        card.setSuperLikeCount(cardDto.getSuperLikeCount());
        card.setTotalEarnedUZS(cardDto.getTotalEarnedUZS());
        card.setTotalSuperLikesEarned(cardDto.getTotalSuperLikesEarned());
        return card;
    }
    public CardDto toDto(Card card) {
        if (card == null) return null;
        CardDto cardDto = new CardDto();
        cardDto.setBalance(card.getBalance());
        cardDto.setNumber(card.getNumber());
        cardDto.setUserId(card.getUserId());
        cardDto.setId(card.getId());
        cardDto.setTotalEarnedUZS(card.getTotalEarnedUZS());
        cardDto.setSuperLikeCount(card.getSuperLikeCount());
        cardDto.setTotalSuperLikesEarned(card.getTotalSuperLikesEarned());
        return cardDto;
    }
}