package com.duckstar.util;

public class ChosungUtil {

    private static final String[] CHOSUNG_LIST = {
            "ㄱ", "ㄲ", "ㄴ", "ㄷ", "ㄸ", "ㄹ", "ㅁ", "ㅂ", "ㅃ", "ㅅ",
            "ㅆ", "ㅇ", "ㅈ", "ㅉ", "ㅊ", "ㅋ", "ㅌ", "ㅍ", "ㅎ"
    };

    public static boolean searchMatch(String searchQuery, String targetText) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return false;
        }

        String query = searchQuery.trim().toLowerCase();
        String target = targetText.toLowerCase();

        if (isChosungOnly(query)) {
            String targetChosung = extractChosung(target);
            String queryChosung = removeSpaces(query);
            return targetChosung.contains(queryChosung);
        } else {
            // 일반 키워드 검색
            // 1. 원본 텍스트에서 검색
            if (target.contains(query)) {
                return true;
            }

            // 2. 띄어쓰기 무시하고 검색
            String targetNoSpace = removeSpaces(target);
            String queryNoSpace = removeSpaces(query);
            return targetNoSpace.contains(queryNoSpace);
        }
    }

    // 한글 초성 추출 함수
    private static String extractChosung(String text) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            int code = (int) ch;

            // 한글 유니코드 범위: 44032(가) ~ 55203(힣)
            if (code >= 44032 && code <= 55203) {
                // 한글 초성 계산: (유니코드 - 44032) / 28 / 21
                int chosungIndex = (code - 44032) / (28 * 21);
                result.append(CHOSUNG_LIST[chosungIndex]);
            } else if (ch != ' ') {
                // 공백이 아닌 다른 문자는 그대로 유지
                result.append(ch);
            }
        }

        return result.toString();
    }

    // 초성만 입력했는지 확인 (한글 자음과 띄어쓰기만 있는지)
    private static boolean isChosungOnly(String query) {
        return query.matches("^[ㄱ-ㅎ\\s]+$");
    }

    // 띄어쓰기 제거
    private static String removeSpaces(String text) {
        return text.replaceAll("\\s+", "");
    }
}
