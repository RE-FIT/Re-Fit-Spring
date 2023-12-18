package com.umc.refit.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.*;

@Getter
@RequiredArgsConstructor
public enum ExceptionType {

    /*회원 관련 예외*/

    //회원 가입 예외
    ID_EMPTY(BAD_REQUEST, 10011, "필수 정보입니다."),
    ID_INVALID(BAD_REQUEST, 10012, "8~16자의 영문, 숫자를 포함해야 합니다."),
    ID_ALREADY_EXIST(BAD_REQUEST, 10013, "이미 존재하는 아이디입니다."),
    PASSWORD_EMPTY(BAD_REQUEST, 10014, "필수 정보입니다."),
    PASSWORD_INVALID(BAD_REQUEST, 10015, "8-16자의 영문 대소문자, 숫자, 특수문자 ((!), (_) , (-))를 포합해야합니다."),
    NAME_EMPTY(BAD_REQUEST, 10019, "필수 정보입니다."),
    NAME_ALREADY_EXIST(BAD_REQUEST, 10020, "이미 존재하는 닉네임입니다."),
    BIRTH_EMPTY(BAD_REQUEST, 10021, "필수 정보입니다."),
    BIRTH_ALREADY_EXIST(BAD_REQUEST, 10022, "YYYY/MM/DD 형식으로 작성해야 합니다."),

    //회원 이메일 예외
    EMAIL_EMPTY(BAD_REQUEST, 10016, "필수 정보입니다."),
    EMAIL_ALREADY_EXIST(BAD_REQUEST, 10017, "이미 존재하는 이메일입니다."),
    EMAIL_INVALID(BAD_REQUEST, 10018, "이메일 형식이 올바르지 않습니다."),

    //회원 아이디 찾기 및 패스워드 재설정 예외
    MEMBER_IS_NOT_EXIST(BAD_REQUEST, 10004, "아이디 찾기에 실패했습니다."),
    PASSWORD_RESET_FAIL(BAD_REQUEST, 10005, "비밀번호 재설정에 실패하였습니다."),
    PASSWORD_IS_NOT_MATCH(BAD_REQUEST, 1006, "현재 비밀번호와 입력하신 비밀번호가 일치하지 않습니다."),

    // Token 예외
    TOKEN_NOT_EXIST(BAD_REQUEST, 10100, "JWT Token이 존재하지 않습니다."),
    TOKEN_INVALID(BAD_REQUEST, 10101, "유효하지 않은 JWT Token 입니다."),
    TOKEN_EXPIRED(BAD_REQUEST, 10102, "토큰 만료기간이 지났습니다."),

    //로그인 예외
    LOGIN_FAILED(BAD_REQUEST, 10103, "존재하지 않는 계정입니다."),
    LOGIN_FAILED_UNKNOWN(BAD_REQUEST, 10104, "알 수 없는 이유로 로그인 할 수 없습니다."),
    KAKAO_MEMBER_EXIST(BAD_REQUEST, 10105, "카카오 로그인 계정이 존재합니다."),
    BASIC_MEMBER_EXIST(BAD_REQUEST, 10106, "일반 로그인 계정이 존재합니다."),


    /* 옷장 관련 예외 */
    CLOTHE_EMPTY(BAD_REQUEST, 20001, "해당하는 옷장 정보를 찾을 수 없습니다."),
    ONE_CATEGORY_OVER_TWO_COUNT(BAD_REQUEST, 20002, "해당하는 옷장에 카테고리는 이미 2회를 입으셨습니다."),



    /*커뮤니티 관련 예외*/

    //커뮤니티 게시글 작성 예외
    TITLE_EMPTY(BAD_REQUEST, 30001, "제목은 필수 입력값입니다."),
    GENDER_EMPTY(BAD_REQUEST, 30002, "추천 성별은 필수 입력값입니다."),
    GENDER_RANGE_ERR(BAD_REQUEST, 30003, "추천 성별 범위에 해당되지 않는 요청값입니다."),
    POST_TYPE_EMPTY(BAD_REQUEST, 30004, "게시글 타입은 필수 입력값입니다."),
    POST_TYPE_RANGE_ERR(BAD_REQUEST, 30005, "게시글 타입 범위에 해당되지 않는 요청값입니다."),
    CATEGORY_EMPTY(BAD_REQUEST, 30006, "카테고리는 필수 입력값입니다."),
    CATEGORY_RANGE_ERR(BAD_REQUEST, 30007, "카테고리 범위에 해당되지 않는 요청값입니다."),
    SIZE_EMPTY(BAD_REQUEST, 30008, "사이즈는 필수 입력값입니다."),
    SIZE_RANGE_ERR(BAD_REQUEST, 30009, "사이즈 범위에 해당되지 않는 요청값입니다."),
    DELIVERY_TYPE_EMPTY(BAD_REQUEST, 30010, "배송 방법은 필수 입력값입니다."),
    DELIVERY_TYPE_RANGE_ERR(BAD_REQUEST, 30011, "배송 방법 범위에 해당되지 않는 요청값입니다."),
    DETAIL_EMPTY(BAD_REQUEST, 30012, "상세설명은 필수 입력값입니다."),
    PRICE_EMPTY(BAD_REQUEST, 30013, "판매 글일 경우 가격은 필수 입력값입니다."),
    DELIVERY_FEE_EMPTY(BAD_REQUEST, 30014, "택배 배송일 경우 배송비는 필수 입력값입니다."),
    REGION_EMPTY(BAD_REQUEST, 30015, "직거래일 경우 거래 희망 지역은 필수 입력값입니다."),
    IMAGE_EMPTY(BAD_REQUEST, 30016, "이미지는 1개 이상 첨부해야 합니다."),
    IMAGE_LIMIT_EXCEEDED(BAD_REQUEST, 30017, "첨부 가능 이미지 개수를 초과했습니다."),


    //차단,신고 관련 예외
    BLOCKED_USER_POST(FORBIDDEN, 30100, "차단한 유저의 글입니다."),
    ALREADY_BLOCKED_USER(BAD_REQUEST, 30101, "이미 차단한 유저입니다."),
    SELF_BLOCK_NOT_ALLOWED(BAD_REQUEST, 30102, "본인 계정은 차단 불가합니다."),

    ALREADY_REPORTED_USER(BAD_REQUEST, 30103, "이미 신고한 유저입니다."),
    SELF_REPORT_NOT_ALLOWED(BAD_REQUEST, 30104, "본인 계정은 신고 불가합니다."),
    REPORT_REASON_EMPTY(BAD_REQUEST, 30105, "신고 사유는 필수 입력값입니다."),

    //게시글 권한 관련 예외
    PERMISSION_DENIED(FORBIDDEN, 30200, "해당 게시글에 권한이 없습니다."),
    SELF_SCRAP_NOT_ALLOWED(BAD_REQUEST, 30201, "본인 글은 스크랩 할 수 없습니다."),

    //해당 아이디 멤버 없음
    NO_SUCH_MEMBER(NOT_FOUND, 30300, "해당 멤버가 존재하지 않습니다."),
    //헤당 아이디 게시글 없음
    NO_SUCH_POST(NOT_FOUND, 30301, "해당 게시글이 존재하지 않습니다."),

    /*거래 관련 예외*/
    ALREADY_COMPLETED_TRADE(BAD_REQUEST, 40001, "이미 거래 완료된 글입니다."),

    /*파일 관련 예외*/
    FILE_UPLOAD_FAILED(INTERNAL_SERVER_ERROR, 50001, "파일 업로드 중 오류가 발생하였습니다."),
    FILE_DELETE_FAILED(INTERNAL_SERVER_ERROR, 50002, "이미지 삭제 중 오류가 발생하였습니다."),
    S3_ERROR(INTERNAL_SERVER_ERROR, 50003, "S3 서비스와 통신 중 오류가 발생했습니다.");

    private final HttpStatus httpStatus;
    private final int code;
    private final String errorMessage;
}