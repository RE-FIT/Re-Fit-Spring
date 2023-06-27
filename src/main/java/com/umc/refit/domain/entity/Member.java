package com.umc.refit.domain.entity;

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
    private Integer region;
    private String address;
    private String socialType;


    public Member(String name) {
        this.name = name;
    }

    public Member() {

    }
}
