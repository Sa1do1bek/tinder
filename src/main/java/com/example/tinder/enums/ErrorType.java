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
    OUTER_DEVICE_NOT_FOUND("outer.device.not.found", HttpStatus.NOT_FOUND ),
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
    OLD_PASSWORD_INCORRECT("old.password.incorrect", HttpStatus.BAD_REQUEST ),;

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
