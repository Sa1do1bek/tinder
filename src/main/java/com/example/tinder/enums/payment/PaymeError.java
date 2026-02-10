package com.example.tinder.enums.payment;

import com.example.tinder.utils.Message;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum PaymeError {
    INVALID_AMOUNT(-31001, new Message("Noto'g'ri summa", "Недопустимая сумма", "Invalid amount")),
    USER_NOT_FOUND(-31050, new Message("Biz userni topolmadik.", "Нам не удалось найти пользователя.", "We could not find the user.")),
    USER_CARD_NOT_FOUND(-31051 , new Message("Biz userni kartasini topolmadik.", "Нам не удалось найти карту пользователя." , "We could not find the user card.")),
    CANT_DO_OPERATION(-31008, new Message("Biz operatsiyani bajara olmaymiz", "Мы не можем сделать операцию", "We can't do operation")),
    TRANSACTION_NOT_FOUND(-31003, new Message("Tranzaktsiya topilmadi", "Транзакция не найдена", "Transaction not found")),
    ALREADY_DONE(-31060, new Message("Mahsulot uchun to'lov qilingan", "Оплачено за товар", "Paid for the product")),
    PENDING(-31050, new Message("Mahsulot uchun to'lov kutilayapti", "Ожидается оплата товар", "Payment for the product is pending")),
    INVALID_AUTHORIZATION(-32504, new Message("Avtorizatsiya yaroqsiz", "Авторизация недействительна", "Authorization invalid")),
    INVALID_METHOD(-32504, new Message("Noto'g'ri metod", "Неверный метод", "Invalid method"));

    private final int code;
    private final Message message;
}
