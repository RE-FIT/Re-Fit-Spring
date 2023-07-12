package com.umc.refit.domain.entity;

import com.umc.refit.domain.dto.community.BlockDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Block {
    @Id
    @GeneratedValue
    @Column(name = "block_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "request_member_id")
    private Member requestMember;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_member_id")
    private Member blockedMember;

    public Block(BlockDto blockDto) {
        this.requestMember = blockDto.getRequestMember();
        this.blockedMember = blockDto.getBlockedMember();
    }
}
