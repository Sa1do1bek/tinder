package com.example.tinder.enums;

import lombok.Getter;

@Getter
public enum SuccessType {
    SUCCESS("success"),
    SUCCESSFULLY_CREATED("successfully.created"),
    SUPER_LIKES_PURCHASED_SUCCESSFULLY("superlikes.purchased.successfullly"),
    VERIFICATION_CODE_SENT_TO_EMAIL("verification.code.sent.to.email"),
    USER_REGISTERED_SUCCESSFULLY("user.registered.successfully"),
    USER_LOGIN_SUCCESSFULLY("user.login.successfully"),
    CODE_SENT_TO_EMAIL("code.sent.to.email"),
    CODE_CHECKED_SUCCESSFULLY("code.checked.successfully"),
    EMAIL_AVAILABLE_FOR_REGISTRATION("email.available.for.registration"),
    USER_BLOCKED_SUCCESSFULLY("user.blocked.successfully"),
    USER_UNBLOCKED_SUCCESSFULLY("user.unblocked.successfully"),
    BLOCKED_USERS_FETCHED_SUCCESSFULLY("blocked.users.fetched.successfully"),
    CARD_BALANCE_FETCHED_SUCCESSFULLY("card.balance.fetched.successfully"),
    CONVERSATION_CREATED_SUCCESSFULLY("conversation.created.successfully"),
    CONVERSATION_ALREADY_EXISTS("conversation.already.exists"),
    CONVERSATION_MARKED_AS_MATCH_SUCCESSFULLY("conversation.marked.as.match.successfully"),
    CONVERSATION_FOUND_SUCCESSFULLY("conversation.found.successfully"),
    CONVERSATION_DELETED_SUCCESSFULLY_FOR_YOU("conversation.deleted.successfully.for.you"),
    CONVERSATION_DELETED_SUCCESSFULLY_FOR_ALL("conversation.deleted.successfully.for.all"),
    NO_CONVERSATIONS_FOUND("no.conversations.found"),
    USER_ALL_UNREAD_COUNTS_FOUND_SUCCESSFULLY("user.all.unread.counts.found.successfully"),
    USER_CONVERSATIONS_FETCHED_SUCCESSFULLY("user.conversations.fetched.successfully"),
    USERS_DISCOVERED_SUCCESSFULLY("users.discovered.successfully"),
    USERS_FOUND_BY_INTEREST("users.found.by.interest"),
    TOP_LIKED_USERS_FETCHED_SUCCESSFULLY("top.liked.users.fetched.successfully"),
    MESSAGE_SENT_SUCCESSFULLY("message.sent.successfully"),
    PICTURES_UPLOADED_SUCCESSFULLY("pictures.uploaded.successfully"),
    MESSAGES_FETCHED_SUCCESSFULLY("messages.fetched.successfully"),
    MESSAGE_DELETED_SUCCESSFULLY("message.deleted.successfully"),
    MESSAGE_UPDATED_SUCCESSFULLY("message.updated.successfully"),
    MESSAGE_LIKED_SUCCESSFULLY("message.liked.successfully"),
    MESSAGE_MARKED_AS_READ_SUCCESSFULLY("message.marked.as.read.successfully"),
    MESSAGE_DELETED_FOR_ALL_SUCCESSFULLY("message.deleted.for.all.successfully"),
    FCM_TOKEN_SAVED_SUCCESSFULLY("fcmToken.saved.successfully"),
    NOTIFICATION_SENT_SUCCESSFULLY("notification.sent.successfully"),
    SUCCESSFULLY_UPDATE_PREFERENCE("successfully.update.preference"),
    USER_PICTURES_FETCHED_SUCCESSFULLY("user.pictures.fetched.successfully"),
    PICTURE_DELETED_SUCCESSFULLY("picture.deleted.successfully"),
    PICTURE_SET_AS_MAIN_SUCCESSFULLY("picture.setAsMain.successfully"),
    NO_SUPER_LIKE_PACKAGES_FOUND("no.super.likepackages.found"),
    SUPER_LIKE_PACKAGES_FETCHED_SUCCESSFULLY("superLike.packages.fetched.successfully"),
    SUPER_LIKE_SENT_SUCCESSFULLY("superLike.sent.successfully"),
    CARD_FETCHED_SUCCESSFULLY("card.fetched.successfully"),
    STATISTICS_FETCHED_SUCCESSFULLY("statistics.fetched.successfully"),
    SUPER_LIKES_PURCHASES_FETCHED_SUCCESSFULLY("superLikes.purchases.fetched.successfully"),
    SENT_SUPER_LIKES_FETCHED_SUCCESSFULLY("sentSuperLikes.fetched.successfully"),
    RECEIVED_SUPER_LIKES_FETCHED_SUCCESSFULLY("receivedSuperLikes.fetched.successfully"),
    TRANSACTIONS_FETCHED_SUCCESSFULLY("transactions.fetched.successfully"),
    PACKAGE_CREATED_SUCCESSFULLY("package.created.successfully"),
    PACKAGE_DELETED_SUCCESSFULLY("package.deleted.successfully"),
    MATCH_FOUND_YOU_CAN_START_CHAT("match.found.you.can.start.chat"),
    SWIPE_RECORDED("swipe.recorded"),
    SWIPES_FETCHED("swipes.fetched"),
    INTEREST_CREATED_SUCCESSFULLY("interest.created.successfully"),
    INTEREST_DELETED_SUCCESSFULLY("interest.deleted.successfully"),
    INTEREST_FETCHED_SUCCESSFULLY("interest.fetched.successfully"),
    USER_LOCATION_UPDATED_SUCCESSFULLY("user.location.updated.successfully"),
    USER_UPDATED_SUCCESSFULLY("user.updated.successfully"),
    USER_FOUND_SUCCESSFULLY("user.found.successfully"),
    USERS_FETCHED_SUCCESSFULLY("users.fetched.successfully"),
    AUTHENTICATION_CODE_SENT_SUCCESSFULLY("authentication.code.sent.successfully"),
    EMAIL_UPDATED_SUCCESSFULLY("email.updated.successfully"),
    PASSWORD_UPDATED_SUCCESSFULLY("password.updated.successfully"),
    USERNAME_AVAILABLE_FOR_REGISTRATION("username.available.for.registration"),
    GOOGLE_AUTH_SUCCESSFULLY("google.auth.successfully"),
    SEARCH_RESULTS_FETCHED_SUCCESSFULLY("search.results.fetched.successfully"),
    USER_DELETED_SUCCESSFULLY("user.deleted.successfully"),
    USER_INTEREST_ADDED_SUCCESSFULLY("user.interest.added.successfully"),
    USERNAME_UPDATED_SUCCESSFULLY("user.username.updated.successfully"),
    SHOW_PROFILE_UPDATED_SUCCESSFULLY("show.profile.updated.successfully"),
    ONLINE_USERS_FETCHED_SUCCESSFULLY("online.users.fetched.successfully"),
    VERIFICATION_UPDATED_SUCCESSFULLY("verification.updated.successfully"),
    VERIFICATION_FOUND("verification.found"),
    APPROVE_IMAGE_UPLOADED_SUCCESSFULLY("approve.image.uploaded.successfully"),

//    **********
    SUBSCRIPTION_PLAN_CREATED_SUCCESSFULLY("subscription.plan.created.successfully"),
    SUBSCRIPTION_PLAN_FETCHED_SUCCESSFULLY("subscription.plan.fetched.successfully"),
    SUBSCRIPTION_PLAN_DELETED_SUCCESSFULLY("successfully.deleted"),
    SUBSCRIPTION_CREATED_SUCCESSFULLY("subscription.created.successfully"),
    FREE_TONIGHT_UPDATED_SUCCESSFULLY("free.tonight.updated.successfully"),
    TELEGRAM_USER_ENTERED_SUCCESSFULLY("telegram.user.entered.successfully"),;



    private final String key;
    SuccessType(String s) {
        this.key = s;
    }
}



























