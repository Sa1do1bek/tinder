package com.example.tinder.enums;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorType {

    INTERNAL_ERROR("internal.server.error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHORIZED("unauthorized", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("forbidden", HttpStatus.FORBIDDEN),
    PAGE_NOT_FOUND("page.not.found", HttpStatus.NOT_FOUND),
    METHOD_NOT_ALLOWED("method.not.allowed", HttpStatus.METHOD_NOT_ALLOWED),
    VALIDATION_ERROR("validation.error", HttpStatus.BAD_REQUEST),
    BAD_JSON("bad.json", HttpStatus.BAD_REQUEST),
    TYPE_MISMATCH("type.mismatch", HttpStatus.BAD_REQUEST),
    MISSING_PARAMETER("missing.parameter", HttpStatus.BAD_REQUEST),
    UNSUPPORTED_MEDIA_TYPE("unsupported.media.type", HttpStatus.UNSUPPORTED_MEDIA_TYPE),
    AWS_S3_UNABLE_TO_UPLOAD("aws.s3.unable.upload"),
    AWS_S3_UNABLE_TO_DOWNLOAD("aws.s3.unable.download"),
    UNSUPPORTED_UPLOAD_FILE("unsupported.upload.file"),
    ATTACHMENT_NOT_FOUND("attachment.not.found", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("user.not.found", HttpStatus.NOT_FOUND),
    USER_ALREADY_EXISTS("user.already.exists", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_NUMBER("invalid.phone.number", HttpStatus.BAD_REQUEST ),
    SOMETHING_ERROR("something.error", HttpStatus.BAD_REQUEST ),
    ERROR_SEND_CODE_TO_PHONE("error.send.code.to.phone", HttpStatus.BAD_REQUEST ),
    CODE_IS_INVALID( "code.is.invalid", HttpStatus.BAD_REQUEST ),
    PHONE_NUMBER_IS_NOT_FOUND("phone.number.is.not.found", HttpStatus.BAD_REQUEST ),
    INVALID_PHONE_NUMBER_OR_PASSWORD("invalid.phone.number.or.password", HttpStatus.BAD_REQUEST ),
    NULL_REQUEST_BODY("null.request.body", HttpStatus.BAD_REQUEST ),
    SENDER_ACCOUNT_INACTIVE("sender.account.inactive", HttpStatus.BAD_REQUEST ),
    RECEIVER_ACCOUNT_INACTIVE("receiver.account.inactive", HttpStatus.BAD_REQUEST ),
    FAILED_TO_SEND_MESSAGE("failed.to.send.message", HttpStatus.INTERNAL_SERVER_ERROR ),
    INVALID_EMAIL_FORMAT("invalid.email.format", HttpStatus.BAD_REQUEST ),
    EMAIL_ALREADY_TAKEN("email.already.taken", HttpStatus.BAD_REQUEST ),
    USERNAME_ALREADY_TAKEN("username.already.taken", HttpStatus.BAD_REQUEST ),
    ERROR_SENDING_VERIFICATION_CODE("error.sending.verification.code", HttpStatus.BAD_REQUEST ),
    INVALID_VERIFICATION_CODE_OR_EMAIL("invalid.verification.code.or.email", HttpStatus.BAD_REQUEST ),
    VERIFICATION_CODE_EXPIRED("Verification.code.has.expired", HttpStatus.BAD_REQUEST ),
    USER_EMAIL_INVALID("user.email.invalid", HttpStatus.BAD_REQUEST ),
    INVALID_AUTHENTICATION_CHARACTERS("invalid.authentication.characters", HttpStatus.BAD_REQUEST ),
    INVALID_CREDENTIALS("invalid.credentials", HttpStatus.BAD_REQUEST ),
    ACCOUNT_NOT_FOUND("account.not.found", HttpStatus.NOT_FOUND ),
    CONVERSATION_NOT_FOUND("conversation.not.found", HttpStatus.NOT_FOUND ),
    USER_INACTIVE("user.inactive", HttpStatus.BAD_REQUEST),
    PROFILE_PICTURE_NOT_FOUND("profile.not.found", HttpStatus.NOT_FOUND ),
    CANNOT_DELETE_OTHER_USERS_PICTURE("cannot.delete.other.users.picture", HttpStatus.BAD_REQUEST ),
    PICTURE_NOT_FOUND("picture.not.found", HttpStatus.NOT_FOUND ),
    CANNOT_SET_OTHER_USERS_PICTURE("cannot.set.other.users.picture", HttpStatus.BAD_REQUEST ),
    YOU_CANNOT_SWIPE_YOURSELF("you.can.not.swipe.yourself", HttpStatus.BAD_REQUEST ),
    THIS_USER_IS_NO_LONGER_AVAILABLE("this.user.is.no.longer.available", HttpStatus.BAD_REQUEST ),
    INVALID_EMAIL(" invalid.email", HttpStatus.BAD_REQUEST ),
    AUTHENTICATION_CODE_ERROR("authentication.code.error", HttpStatus.BAD_REQUEST ),
    OLD_PASSWORD_INCORRECT("old.password.incorrect", HttpStatus.BAD_REQUEST ),
    DEVICE_NOT_FOUND("device.not.found", HttpStatus.NOT_FOUND ),
    INTEREST_ALREADY_EXISTS("interest.already.exists", HttpStatus.BAD_REQUEST ),
    INTEREST_NOT_FOUND("interest.not.found", HttpStatus.NOT_FOUND ),
    INVALID_CATEGORY_TYPE("invalid.category.type", HttpStatus.BAD_REQUEST ),
    USER_LOCATION_NOT_FOUND("user.location.not.found",  HttpStatus.NOT_FOUND ),
    PREFERENCE_NOT_FOUND("preference.not.found",  HttpStatus.NOT_FOUND ),
    LOCATION_NOT_FOUND("location.not.found",  HttpStatus.NOT_FOUND ),
    USERNAME_ALREADY_EXISTS("username.already.exists",  HttpStatus.BAD_REQUEST ),
    MESSAGE_NOT_FOUND("message.not.found", HttpStatus.NOT_FOUND ),
    ACCESS_DENIED("access.denied", HttpStatus.BAD_REQUEST ),
    OPERATION_FAILED("operation.failed", HttpStatus.BAD_REQUEST ),
    FILE_NOT_FOUND("file.not.found", HttpStatus.NOT_FOUND ),
    INVALID_OPERATION("invalid.operation", HttpStatus.BAD_REQUEST ),
    ALREADY_BLOCKED("already.blocked", HttpStatus.BAD_REQUEST ),
    FCM_TOKEN_NOT_FOUND("fcm.token.not.found", HttpStatus.NOT_FOUND ),
    BLOCK_NOT_FOUND("block.not.found", HttpStatus.NOT_FOUND ),
    FIREBASE_INITIALIZATION_ERROR("firebase.initialization.error", HttpStatus.BAD_REQUEST ),
    VERIFICATION_NOT_FOUND("verification.not.found", HttpStatus.NOT_FOUND ),
    USER_ALREADY_APPROVED("user.already.approved", HttpStatus.BAD_REQUEST ),
    INVALID_FCM_TOKEN("invalid.fcm.token", HttpStatus.BAD_REQUEST ),
    CARD_NOT_FOUND("card.not.found", HttpStatus.NOT_FOUND ),
    PACKAGE_NOT_FOUND("package.not.found", HttpStatus.NOT_FOUND ),
    CANNOT_SUPERLIKE_YOURSELF("cannot.superlike.yourself", HttpStatus.BAD_REQUEST ),
    INSUFFICIENT_SUPERLIKES("insufficient.superlikes", HttpStatus.BAD_REQUEST ),
    SUPERLIKE_ALREADY_SENT("superlike.already.sent", HttpStatus.BAD_REQUEST ),
    PAYMENT_FAILED("payment.failed", HttpStatus.BAD_REQUEST ),
    INSUFFICIENT_BALANCE("insufficient.balance", HttpStatus.BAD_REQUEST ),
    INVALID_PACKAGE_QUANTITY("invalid.package.quantity", HttpStatus.BAD_REQUEST ),
    INVALID_PACKAGE_PRICE("invalid.package.price", HttpStatus.BAD_REQUEST ),
    INVALID_PLATFORM_COMMISSION("invalid.platform.commission", HttpStatus.BAD_REQUEST ),
    CONVERSATION_ALREADY_EXISTS("conversation.already.exists", HttpStatus.BAD_REQUEST ),
    SUBSCRIPTION_PLAN_ALREADY_EXISTS("subscription.plan.already.exists", HttpStatus.BAD_REQUEST ),
    SUBSCRIPTION_PLAN_NOT_FOUND("subscription.plan.not.found", HttpStatus.NOT_FOUND ),
    CODE_NOT_FOUND("code.not.found", HttpStatus.NOT_FOUND ),
    CODE_ATTEMPT_LIMIT_EXCEEDED("code.attempt.limit.exceeded", HttpStatus.BAD_REQUEST ),
    PHONE_ALREADY_TAKEN("phone.already.taken", HttpStatus.BAD_REQUEST ),
    INVALID_VERIFICATION_CODE_OR_PHONE("invalid.verification.code.or.phone", HttpStatus.BAD_REQUEST ),
    USER_PHONE_INVALID("user.phone.invalid", HttpStatus.BAD_REQUEST ),
    MINIMUM_WITHDRAW_NOT_MET("minimum.withdrawn.not.met", HttpStatus.BAD_REQUEST ),
    PHOTOS_ARE_DOES_NOT_SIMILAR("photos.are.does.not.similar", HttpStatus.BAD_REQUEST ),
    REPORT_NOT_FOUND("report.not.found", HttpStatus.NOT_FOUND),
    REPORT_ALREADY_EXISTS("already.Reported", HttpStatus.ALREADY_REPORTED),
    REPORT_ALREADY_UPDATED("cannot.update.already.updated.report.status", HttpStatus.ALREADY_REPORTED),
    REPORT_ADMIN_ALREADY_ASSIGNED("admin.already.signed.to.this.report", HttpStatus.ALREADY_REPORTED),
    USER_NOT_AUTHENTICATED("user.not.authenticated", HttpStatus.UNAUTHORIZED),
    REPORT_STATUS_CAN_NOT_BE_UPDATE_TO_PENDING("report.status.can.not.be.update.to.pending", HttpStatus.BAD_REQUEST),
    REPORTS_NOT_FOUND_BY_REPORTED_ID("reports.not.found.by.reported.id", HttpStatus.NOT_FOUND),
    OTHER_WITH_NO_DESCRIPTION("other.with.no.description", HttpStatus.BAD_REQUEST),
    USER_BLOCK_FAILED("user.block.failed",HttpStatus.BAD_REQUEST),
    START_DATE_CAN_NOT_BE_AFTER_END_DATE("start.date.can.not.be.after.end.date",HttpStatus.BAD_REQUEST),
    INVALID_DATE("invalid.data", HttpStatus.BAD_REQUEST ),
    USER_BANNED_PERMANENT( "Sizning akkauntingiz umrbod bloklangan",HttpStatus.FORBIDDEN),
    USER_BANNED_TEMP( "Sizning akkauntingiz vaqtinchalik bloklangan",HttpStatus.FORBIDDEN);
    //***************************



    private final String msg;
    private HttpStatus status = HttpStatus.BAD_REQUEST;

    ErrorType(String msg, HttpStatus status) {
        this.msg = msg;
        this.status = status;
    }

    ErrorType(String msg) {
        this.msg = msg;
    }

    public int getStatusValue() {
        return status.value();
    }
}
