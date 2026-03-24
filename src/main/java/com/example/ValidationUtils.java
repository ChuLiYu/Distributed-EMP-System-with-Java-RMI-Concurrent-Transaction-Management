package com.example;

import java.util.regex.Pattern;

public class ValidationUtils {
    private static final Pattern ASSET_SYMBOL_PATTERN = Pattern.compile("^[A-Z]{1,10}$");
    private static final int MAX_NAME_LENGTH = 255;
    private static final int MAX_ID_LENGTH = 100;

    public static class ValidationResult {
        private final boolean valid;
        private final String errorMessage;

        public ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        public boolean isValid() {
            return valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }
    }

    public static ValidationResult validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            return new ValidationResult(false, "userId is required");
        }
        if (userId.length() > MAX_ID_LENGTH) {
            return new ValidationResult(false, "userId exceeds maximum length of " + MAX_ID_LENGTH);
        }
        return new ValidationResult(true, null);
    }

    public static ValidationResult validateRequestId(String requestId) {
        if (requestId == null || requestId.isBlank()) {
            return new ValidationResult(false, "requestId is required");
        }
        if (requestId.length() > MAX_ID_LENGTH) {
            return new ValidationResult(false, "requestId exceeds maximum length of " + MAX_ID_LENGTH);
        }
        return new ValidationResult(true, null);
    }

    public static ValidationResult validatePortfolioId(String portfolioId) {
        if (portfolioId == null || portfolioId.isBlank()) {
            return new ValidationResult(false, "portfolioId is required");
        }
        if (portfolioId.length() > MAX_ID_LENGTH) {
            return new ValidationResult(false, "portfolioId exceeds maximum length of " + MAX_ID_LENGTH);
        }
        return new ValidationResult(true, null);
    }

    public static ValidationResult validateAssetSymbol(String assetSymbol) {
        if (assetSymbol == null || assetSymbol.isBlank()) {
            return new ValidationResult(false, "assetSymbol is required");
        }
        if (!ASSET_SYMBOL_PATTERN.matcher(assetSymbol).matches()) {
            return new ValidationResult(false, "assetSymbol must be 1-10 uppercase letters");
        }
        return new ValidationResult(true, null);
    }

    public static ValidationResult validatePaymentId(String paymentId) {
        if (paymentId == null || paymentId.isBlank()) {
            return new ValidationResult(false, "paymentId is required");
        }
        if (paymentId.length() > MAX_ID_LENGTH) {
            return new ValidationResult(false, "paymentId exceeds maximum length of " + MAX_ID_LENGTH);
        }
        return new ValidationResult(true, null);
    }

    public static ValidationResult validateName(String name) {
        if (name == null || name.isBlank()) {
            return new ValidationResult(false, "name is required");
        }
        if (name.length() > MAX_NAME_LENGTH) {
            return new ValidationResult(false, "name exceeds maximum length of " + MAX_NAME_LENGTH);
        }
        return new ValidationResult(true, null);
    }

    public static ValidationResult validateQuantity(double quantity) {
        if (quantity < 0) {
            return new ValidationResult(false, "quantity cannot be negative");
        }
        return new ValidationResult(true, null);
    }

    public static ValidationResult validateAmount(double amount) {
        if (amount <= 0) {
            return new ValidationResult(false, "amount must be positive");
        }
        return new ValidationResult(true, null);
    }

    public static ValidationResult validateInput(String fieldName, String value, boolean required, Pattern pattern, int maxLength) {
        if (required && (value == null || value.isBlank())) {
            return new ValidationResult(false, fieldName + " is required");
        }
        
        if (value != null && maxLength > 0 && value.length() > maxLength) {
            return new ValidationResult(false, fieldName + " exceeds maximum length of " + maxLength);
        }
        
        if (pattern != null && value != null && !pattern.matcher(value).matches()) {
            return new ValidationResult(false, fieldName + " has invalid format");
        }
        
        return new ValidationResult(true, null);
    }
}
