package com.umc.refit;

import java.util.Random;

public class Util {

    /**Random 문자열*/
    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";

    public static String generateRandomString(int length) {
        Random random = new Random();
        StringBuilder stringBuilder = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            int randomIndex = random.nextInt(CHARACTERS.length());
            stringBuilder.append(CHARACTERS.charAt(randomIndex));
        }

        return stringBuilder.toString();
    }

    /**시간*/
    public static final int ONE_HOUR = 60 * 60 * 1000;
    public static final int ONE_DAY = 24 * 60 * 60 * 1000;
    public static final int ONE_WEEK = 7 * 24 * 60 * 60 * 1000;

    /**PATH*/
    public static final String KAKAO_API = "https://kapi.kakao.com/v2/user/me";
}
