package com.fivefull.coordiback.service;

import com.fivefull.coordiback.dto.AuthorDto;
import com.fivefull.coordiback.dto.SnapDto;
import com.fivefull.coordiback.entity.*;
import com.fivefull.coordiback.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SnapService {

    @Autowired
    private SnapRepository snapRepository;

    @Autowired
    private SnapTagRepository snapTagRepository;

    @Autowired
    private CategoriRepository categoriRepository;

    @Autowired
    private GCSService gcsService;

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Transactional
    public Snap createSnap(Member member, String description, List<MultipartFile> images, List<String> categories) throws IOException {
        if (images.isEmpty()) throw new IllegalArgumentException("At least one image is required");

        List<String> uploadedImageUrls = images.stream()
                .map(image -> {
                    try {
                        return gcsService.uploadFile(image);
                    } catch (IOException e) {
                        throw new RuntimeException("Image upload failed", e);
                    }
                }).collect(Collectors.toList());

        Snap snap = new Snap();
        snap.setMember(member);
        snap.setSnapDescription(description);
        snap.setSnapImageUrls(uploadedImageUrls);
        snap.setSnapCreatedDate(LocalDateTime.now());

        snapRepository.save(snap);

        for (String categoryName : categories) {
            Categori categori = categoriRepository.findByCategoryName(categoryName)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid category: " + categoryName));

            SnapTag snapTag = new SnapTag();
            snapTag.setSnap(snap);
            snapTag.setCategori(categori);
            snapTagRepository.save(snapTag);
        }

        return snap;
    }


    @Transactional
    public void deleteSnap(Long snapId, String email) {
        Snap snap = snapRepository.findById(snapId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        if (!snap.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("삭제 권한이 없습니다.");
        }

        // 1. Snap과 연관된 Like 삭제
        likeRepository.deleteAllBySnap(snap);

        // 2. Snap과 연관된 Comments 삭제
        commentRepository.deleteAllBySnap(snap);

        // 3. Snap과 연관된 SnapTag 삭제
        snapTagRepository.deleteAllBySnap(snap);

        // 4. Snap 삭제
        snapRepository.delete(snap);
    }

    @Transactional
    public Snap updateSnap(Long snapId, String email, String description, List<MultipartFile> newImages, List<String> removedImageUrls, List<String> categories) throws IOException {
        Snap snap = snapRepository.findById(snapId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        if (!snap.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("수정 권한이 없습니다.");
        }

        // 설명 업데이트
        if (description != null) {
            snap.setSnapDescription(description);
        }

        // 기존 이미지 삭제 처리
        List<String> updatedImageUrls = new ArrayList<>(snap.getSnapImageUrls());
        if (removedImageUrls != null) {
            updatedImageUrls.removeAll(removedImageUrls);
        }

        // 새 이미지 추가
        if (newImages != null && !newImages.isEmpty()) {
            for (MultipartFile image : newImages) {
                String uploadedUrl = gcsService.uploadFile(image);
                updatedImageUrls.add(uploadedUrl);
            }
        }
        snap.setSnapImageUrls(updatedImageUrls);

        // 카테고리 업데이트
        if (categories != null) {
            snapTagRepository.deleteAllBySnap(snap);
            for (String categoryName : categories) {
                Categori categori = categoriRepository.findByCategoryName(categoryName)
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 카테고리입니다: " + categoryName));
                SnapTag snapTag = new SnapTag();
                snapTag.setSnap(snap);
                snapTag.setCategori(categori);
                snapTagRepository.save(snapTag);
            }
        }

        return snapRepository.save(snap);
    }


    public List<SnapDto> getAllSnaps() {
        return snapRepository.findAllByOrderBySnapCreatedDateDesc().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<SnapDto> getSnapsByCategory(String categoryName) {
        List<SnapTag> snapTags = snapTagRepository.findByCategori_CategoryName(categoryName);
        return snapTags.stream()
                .map(SnapTag::getSnap)
                .distinct()
                .sorted((s1, s2) -> s2.getSnapCreatedDate().compareTo(s1.getSnapCreatedDate()))
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public SnapDto convertToDto(Snap snap) {
        List<String> categories = snapTagRepository.findBySnap(snap).stream()
                .map(tag -> tag.getCategori().getCategoryName())
                .collect(Collectors.toList());


        AuthorDto author = AuthorDto.builder()
                .nickname(snap.getMember().getNickname())
                .profileImageUrl(snap.getMember().getProfileImageUrl())
                .build();


        return SnapDto.builder()
                .snapId(snap.getSnapId())
                .snapImageUrls(snap.getSnapImageUrls())
                .snapDescription(snap.getSnapDescription())
                .snapCreatedDate(snap.getSnapCreatedDate())
                .categories(categories)
                .author(author) // 작성자 정보 포함
                .build();
    }

    public Snap getSnapById(Long snapId) {
        return snapRepository.findById(snapId)
                .orElseThrow(() -> new IllegalArgumentException("Snap not found"));
    }

    public List<Comment> getCommentsBySnapId(Long snapId) {
        return commentRepository.findBySnap_SnapIdOrderByCreateDateAsc(snapId);
    }

    public int getLikeCount(Long snapId) {
        Snap snap = snapRepository.findById(snapId)
                .orElseThrow(() -> new IllegalArgumentException("Snap not found"));
        return likeRepository.countBySnap(snap);
    }

    public boolean checkIfLiked(Long snapId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Snap snap = snapRepository.findById(snapId)
                .orElseThrow(() -> new IllegalArgumentException("Snap not found"));

        return likeRepository.existsBySnapAndMember(snap, member);
    }

    public Comment addComment(Long snapId, String email, String content) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Snap snap = snapRepository.findById(snapId)
                .orElseThrow(() -> new IllegalArgumentException("Snap not found"));

        Comment comment = new Comment();
        comment.setSnap(snap);
        comment.setMember(member);
        comment.setContent(content);
        comment.setCreateDate(LocalDateTime.now());

        return commentRepository.save(comment);
    }

    public void addLike(Long snapId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Snap snap = snapRepository.findById(snapId)
                .orElseThrow(() -> new IllegalArgumentException("Snap not found"));

        if (likeRepository.existsBySnapAndMember(snap, member)) {
            throw new IllegalStateException("Already liked");
        }

        Like like = new Like();
        like.setSnap(snap);
        like.setMember(member);

        likeRepository.save(like);
    }

    public void removeLike(Long snapId, String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Snap snap = snapRepository.findById(snapId)
                .orElseThrow(() -> new IllegalArgumentException("Snap not found"));

        Like like = likeRepository.findBySnapAndMember(snap, member)
                .orElseThrow(() -> new IllegalArgumentException("Like not found"));

        likeRepository.delete(like);
    }

    @Transactional(readOnly = true)
    public List<SnapDto> getSnapsByAuthor(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return snapRepository.findByMember(member).stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<SnapDto> getLikedSnaps(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return likeRepository.findByMember(member).stream()
                .map(like -> convertToDto(like.getSnap()))
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteComment(Long snapId, Long commentId, String email) {
        // 해당 Snap을 가져오기
        Snap snap = snapRepository.findById(snapId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        // 해당 댓글 가져오기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 댓글이 해당 Snap에 속하지 않는 경우 예외 처리
        if (!comment.getSnap().equals(snap)) {
            throw new IllegalArgumentException("댓글이 게시물에 속하지 않습니다.");
        }

        // 댓글 작성자와 요청한 사용자가 다른 경우 예외 처리
        if (!comment.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("댓글 삭제 권한이 없습니다.");
        }

        // 댓글 삭제
        commentRepository.delete(comment);
    }

    @Transactional
    public void updateComment(Long snapId, Long commentId, String email, String content) {
        // 해당 Snap을 가져오기
        Snap snap = snapRepository.findById(snapId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 게시물입니다."));

        // 해당 댓글 가져오기
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 댓글입니다."));

        // 댓글이 해당 Snap에 속하지 않는 경우 예외 처리
        if (!comment.getSnap().equals(snap)) {
            throw new IllegalArgumentException("댓글이 게시물에 속하지 않습니다.");
        }

        // 댓글 작성자와 요청한 사용자가 다른 경우 예외 처리 
        if (!comment.getMember().getEmail().equals(email)) {
            throw new IllegalArgumentException("댓글 수정 권한이 없습니다.");
        }

        // 댓글 내용 수정
        comment.setContent(content);
        commentRepository.save(comment);
    }

}
