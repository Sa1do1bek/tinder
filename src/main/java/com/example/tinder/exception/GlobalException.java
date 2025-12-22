package com.example.tinder.exception;

//
//@RestControllerAdvice
//@RequiredArgsConstructor
//public class GlobalException {
//
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ApiResponse handleValidationExceptions(MethodArgumentNotValidException e) {
//        Map<String, String> errors = new HashMap<>();
//        e.getBindingResult().getAllErrors().forEach((error) -> {
//            String fieldName = ((FieldError) error).getField();
//            String errorMessage = error.getDefaultMessage();
//            errors.put(fieldName, errorMessage);
//        });
//
//        return ApiResponse.builder()
//                .status(HttpStatus.BAD_REQUEST)
//                .message(e.getMessage())
//                .errors(errors)
//                .build();
//    }
//
//    @ExceptionHandler(ResourceNotFoundException.class)
//    public ApiResponse handleResourceNotFoundException(ResourceNotFoundException e) {
//        return ApiResponse.builder()
//                .message(e.getMessage())
//                .status(HttpStatus.NOT_FOUND)
//                .build();
//    }
//
//    @ExceptionHandler(BadRequestException.class)
//    public ResponseEntity<?> handleBadRequestException(BadRequestException e) {
//        return ResponseEntity.badRequest().body(ApiResponse.builder()
//                .status(HttpStatus.BAD_REQUEST)
//                .message(e.getMessage())
//                .build()
//        );
//
//    }
//}
