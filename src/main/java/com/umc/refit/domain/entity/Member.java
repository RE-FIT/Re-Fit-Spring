package com.umc.refit.domain.entity;

import com.umc.refit.domain.dto.member.JoinDto;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Entity
@Getter @Setter
public class Member {

    @Id @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String loginId;
    private String password;
    private String email;
    private String name;
    private String birth;
    private String socialType;


    public Member(String name) {
        this.name = name;
    }

    public Member(JoinDto joinDto) {
        this.loginId = joinDto.getLoginId();
        this.password = joinDto.getPassword();
        this.email = joinDto.getEmail();
        this.name = joinDto.getName();
        this.birth = joinDto.getBirth();
    }

    public Member() {

    }
}
