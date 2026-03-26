package com.example.tinder.mapper;

import com.example.tinder.dto.subsciption.SubscriptionPlanDto;
import com.example.tinder.entity.SubscriptionPlan;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Component
public class SubscriptionPlanMapper {

    public SubscriptionPlanDto toDto(SubscriptionPlan dto) {
        if (dto == null) return null;


        SubscriptionPlanDto entity = new SubscriptionPlanDto();
        entity.setId(dto.getId());
        entity.setCode(dto.getCode());
        entity.setMonths(dto.getMonths());
        entity.setPrice(dto.getPrice());
        entity.setType(dto.getType());

        // monthly_price = price / months
        if (dto.getPrice() != null && dto.getMonths() > 0) {
            entity.setMonthly_price(
                    dto.getPrice().divide(
                            BigDecimal.valueOf(dto.getMonths()),
                            2,
                            RoundingMode.HALF_UP
                    )
            );
        } else {
            entity.setMonthly_price(BigDecimal.ZERO);
        }

        /**@DISCOUNT "HISOBLASH"*/
        if (dto.getParent() != null && dto.getParent().getPrice() != null) {

            BigDecimal oldPrice = dto.getParent().getPrice();
            BigDecimal newPrice = dto.getPrice();


            /**
             * Only "calculates discount" if new price is less than old price or greater than 0
             */

            if (oldPrice.compareTo(BigDecimal.ZERO) > 0 && newPrice.compareTo(oldPrice) < 0) {
                BigDecimal discount = oldPrice.subtract(newPrice)
                        .divide(oldPrice, 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                entity.setDiscountPercent(discount.doubleValue());
            } else {
                entity.setDiscountPercent(0.0);
            }

            entity.setOldPrice(dto.getParent().getPrice());

        } else {
            entity.setDiscountPercent(0.0);
        }


        return entity;
    }

    public SubscriptionPlan fromCreateDto(SubscriptionPlanDto.SubscriptionPlanCreateDto dto) {
        if (dto == null) return null;

        SubscriptionPlan entity = new SubscriptionPlan();
        entity.setCode(dto.getCode()    );
        entity.setMonths(dto.getMonths());
        entity.setPrice(dto.getPrice());
        entity.setType(dto.getType());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setUpdatedAt(LocalDateTime.now());
        entity.setVisibility(true);
        return entity;
    }
}
