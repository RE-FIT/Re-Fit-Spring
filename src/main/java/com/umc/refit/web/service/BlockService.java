package com.umc.refit.web.service;

import com.umc.refit.domain.dto.community.BlockDto;
import com.umc.refit.domain.entity.Block;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.web.repository.BlockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BlockService {

    private final BlockRepository blockRepository;
    private final MemberService memberService;


    /*사용자 차단*/
    public void block(BlockDto blockDto, Authentication authentication){

        //차단 요청한 유저
        String userId = authentication.getName();
        Member requestMem = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));
        blockDto.setRequestMember(requestMem);

        //차단 당하는 유저
        Member blockedMem = memberService.findMemberByName(blockDto.getBlockedMember().getName())
                .orElseThrow(() -> new NoSuchElementException("No member found with this name"));
        blockDto.setBlockedMember(blockedMem);

        // 자기 자신을 차단한 경우
        if(requestMem.getId().equals(blockedMem.getId())){
            throw new IllegalStateException("자기 자신은 차단 불가합니다.");
            //추후 다른 예외 처리 코드로 바꿀 예정
        }

        // 이미 차단한 유저일 경우
        if(blockRepository.findByRequestMemberAndBlockedMember(requestMem, blockedMem).isPresent()){
            throw new IllegalStateException("이미 차단한 유저입니다.");
            //추후 다른 예외 처리 코드로 바꿀 예정
        }

        save(new Block(blockDto));
    }

    /*로그인 멤버가 차단한 유저 아이디 목록*/
    public List<Long> getBlockMemIds(Member requestMember) {
        // 차단된 사용자 목록
        List<Block> blocks = blockRepository.findByRequestMember(requestMember);

        // 차단된 사용자들의 ID 목록
        List<Long> blockedMemberIds = blocks.stream()
                .map(block -> block.getBlockedMember().getId())
                .collect(Collectors.toList());
        return blockedMemberIds;
    }

    public void save(Block block) {
        blockRepository.save(block);
    }
}
