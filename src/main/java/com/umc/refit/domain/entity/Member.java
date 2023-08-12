package com.umc.refit.domain.entity;

import com.umc.refit.domain.dto.member.JoinDto;
import com.umc.refit.domain.dto.mypage.UpdateMyInfoRequestDto;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Getter
@Setter
public class Member implements UserDetails {

    @Id
    @GeneratedValue
    @Column(name = "member_id")
    private Long id;
    private String loginId;
    private String password;
    private String email;
    private String name;
    private String birth;
    private String socialType;
    private Integer gender;
    private String imageUrl;
    private String fcm;
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    public Member(String name) {
        this.name = name;
    }

    public Member(String email, String password, String name) {
        this.password = password;
        this.email = email;
        this.loginId = email;
        this.name = name;
        this.socialType = "KAKAO";
    }

    public Member(JoinDto joinDto) {
        this.loginId = joinDto.getLoginId();
        this.password = joinDto.getPassword();
        this.email = joinDto.getEmail();
        this.name = joinDto.getName();
        this.birth = joinDto.getBirth();
        this.gender = joinDto.getGender();
        this.getRoles().add("USER");
    }

    public Member() {

    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.roles.stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() {
        return loginId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public void updateMemberByMyInfo(UpdateMyInfoRequestDto request, String requestedImageUrl) {
        name = request.getName();
        birth = request.getBirth();
        gender = request.getGender();
        imageUrl = requestedImageUrl;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }
}
