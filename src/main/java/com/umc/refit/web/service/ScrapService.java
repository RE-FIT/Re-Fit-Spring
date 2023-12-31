package com.umc.refit.web.service;

import com.umc.refit.domain.dto.community.PostMainResponseDto;
import com.umc.refit.domain.dto.community.ScrapDto;
import com.umc.refit.domain.entity.Member;
import com.umc.refit.domain.entity.Posts;
import com.umc.refit.domain.entity.Scrap;
import com.umc.refit.exception.community.CommunityException;
import com.umc.refit.web.repository.ScrapRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.umc.refit.exception.ExceptionType.*;


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

        Member member = communityService.findMember(authentication);
        Posts post = communityService.findPost(postId);

        checkMyPost(member, post);

        //스크랩한 글인지 확인
        Optional<Scrap> findScrap =findScrapByMemAndPostId(member, postId);

        /*스크랩이 이미 존재하면 스크랩 해제
        아니면 새로 스크랩*/
        findScrap.ifPresentOrElse(
                this::remove,
                () -> {
                    ScrapDto scrapDto = new ScrapDto(member, post);
                    save(new Scrap(scrapDto));
                }
        );
    }

    public void checkMyPost(Member member, Posts post){
        if(post.getMember().getId().equals(member.getId())){
            throw new CommunityException(SELF_SCRAP_NOT_ALLOWED, SELF_SCRAP_NOT_ALLOWED.getCode(), SELF_SCRAP_NOT_ALLOWED.getErrorMessage());
        }
    }


    /*스크랩한 글인지 확인*/
    public Optional<Scrap> findScrapByMemAndPostId(Member member, Long postId) {
        return scrapRepository.findByMemberAndPostId(member, postId);
    }

    /*내가 스크랩한 글 목록*/
    public List<PostMainResponseDto> findMyScraps(Integer postType, Authentication authentication){
        Member member = communityService.findMember(authentication);

        List<Scrap> scraps = scrapRepository.findByMember(member);
        return convertToDtoListScrap(scraps.stream()
                .map(Scrap::getPost)
                .filter(post -> post.getPostType().equals(postType))
                .collect(Collectors.toList()));
    }

    /*해당 게시글에 대한 유저의 스크랩 여부 판별*/
    public boolean isPostScrappedByUser(Member member, Long postId) {
        //사용자가 특정 게시글을 스크랩했는지 확인
        return scrapRepository.existsByMemberAndPostId(member, postId);
    }


    public void save(Scrap scrap) {
        scrapRepository.save(scrap);
    }
    public void remove(Scrap scrap) {
        scrapRepository.delete(scrap);
    }

    public List<PostMainResponseDto> convertToDtoListScrap(List<Posts> postsList) {
        return postsList.stream()
                .map(posts -> {
                    boolean scrapFlag = true;
                    return new PostMainResponseDto(
                            posts.getId(),
                            posts.getTitle(),
                            posts.getImage().get(0).getImageUrl(),
                            posts.getGender(),
                            posts.getDeliveryType(),
                            posts.getAddress(),
                            posts.getPrice(),
                            posts.getSize(),
                            scrapFlag);
                })
                .collect(Collectors.toList());
    }
}
