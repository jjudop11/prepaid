package com.prepaid.auth.validator;

public class PasswordValidator {

    /**
     * 비밀번호가 아이디 일부를 포함하는지 체크
     * 연속된 3자 이상 포함 시 true 반환 (대소문자 무시)
     */
    public static boolean containsUsername(String password, String username) {
        if (password == null || username == null || username.length() < 3) {
            return false;
        }

        String lowerPassword = password.toLowerCase();
        String lowerUsername = username.toLowerCase();

        // 연속된 3자 이상이 포함되면 true
        for (int i = 0; i <= lowerUsername.length() - 3; i++) {
            String substring = lowerUsername.substring(i, i + 3);
            if (lowerPassword.contains(substring)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 비밀번호 강도 검증 (영문+숫자+특수기호)
     */
    public static boolean isValidPasswordStrength(String password) {
        if (password == null || password.length() < 8) {
            return false;
        }

        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecial = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{}|;:,.<>?].*");

        return hasLetter && hasDigit && hasSpecial;
    }
}
