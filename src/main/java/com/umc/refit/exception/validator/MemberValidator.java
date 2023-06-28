package com.umc.refit.exception.validator;

import java.util.regex.Pattern;

public class MemberValidator {

    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@(.+)$";
    private static final String ID_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,16}$";
    private static final String PASSWORD_REGEX = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[!_-]).{8,16}$";
    private static final String BIRTH_REGEX = "^\\d{4}/(0[1-9]|1[0-2])/(0[1-9]|[12][0-9]|3[01])$";

    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final Pattern ID_PATTERN = Pattern.compile(ID_REGEX);
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(PASSWORD_REGEX);
    private static final Pattern BIRTH_PATTERN = Pattern.compile(BIRTH_REGEX);

    public static boolean isEmailValid(String email) {
        return EMAIL_PATTERN.matcher(email).matches();
    }

    public static boolean isLoginValid(String loginId) {
        return ID_PATTERN.matcher(loginId).matches();
    }

    public static boolean isPasswordValid(String password) {
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    public static boolean isBirthValid(String birth) {
        return BIRTH_PATTERN.matcher(birth).matches();
    }
}