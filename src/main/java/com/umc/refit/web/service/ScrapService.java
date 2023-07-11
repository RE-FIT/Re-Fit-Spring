package com.umc.refit.web.service;

import com.umc.refit.domain.dto.community.ScrapDto;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.Posts;
import com.umc.refit.domain.entity.Scrap;
import com.umc.refit.web.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.NoSuchElementException;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Transactional
public class ScrapService {

    private final ScrapRepository scrapRepository;
    private final MemberService memberService;
    private final CommunityService communityService;


    /*스크랩 API
    * 기존에 스크랩한 게시글이면 스크랩에서 제거
    * 아니면 스크랩
    * */
    public void scrap(Long postId, Authentication authentication){

        String userId = authentication.getName();
        Member findMember = memberService.findMemberByLoginId(userId)
                .orElseThrow(() -> new NoSuchElementException("No member found with this user id"));

        Posts findPost = communityService.findPostById(postId)
                .orElseThrow(() -> new NoSuchElementException("No post found with this post id"));

        if(findPost.getMember().getId().equals(findMember.getId())){
            throw new IllegalStateException("본인 글은 스크랩 불가합니다.");
            //추후 다른 예외 처리 코드로 바꿀 예정 SelfScrapNotAllowedException
        }

        Optional<Scrap> findScrap =findScrapByMemAndPostId(findMember, postId);

        /*스크랩이 이미 존재하면 스크랩 해제
        아니면 새로 스크랩*/
        findScrap.ifPresentOrElse(
                this::remove,
                () -> {
                    ScrapDto scrapDto = new ScrapDto(findMember, findPost);
                    save(new Scrap(scrapDto));
                }
        );
    }

    public Optional<Scrap> findScrapByMemAndPostId(Member member, Long postId) {
        return scrapRepository.findByMemberAndPostId(member, postId);
    }

    public void save(Scrap scrap) {
        scrapRepository.save(scrap);
    }
    public void remove(Scrap scrap) {
        scrapRepository.delete(scrap);
    }
}
