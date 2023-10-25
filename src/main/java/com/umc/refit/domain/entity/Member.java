package com.umc.refit.domain.entity;

import com.umc.refit.domain.dto.member.JoinDto;
import com.umc.refit.domain.dto.mypage.UpdateMyInfoRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.*;
import java.time.LocalDate;
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
    private LocalDate joinDate;

    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles = new ArrayList<>();

    public Member(String name) {
        this.name = name;
    }

    public Member(String email, String password, String name, String fcm) {
        this.email = email;
        this.loginId = email;
        this.password = password;
        this.name = name;
        this.birth = LocalDate.now().toString().replace('-', '/');
        this.gender = 0;
        this.getRoles().add("USER");
        this.joinDate = LocalDate.now();
        this.fcm = fcm;
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
        this.joinDate = LocalDate.now();
    }

    public Member(JoinDto joinDto, String imageUrl) {
        this.loginId = joinDto.getLoginId();
        this.password = joinDto.getPassword();
        this.email = joinDto.getEmail();
        this.name = joinDto.getName();
        this.birth = joinDto.getBirth();
        this.gender = joinDto.getGender();
        this.getRoles().add("USER");
        this.joinDate = LocalDate.now();
        this.imageUrl = imageUrl;
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
