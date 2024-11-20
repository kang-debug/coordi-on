package com.fivefull.coordiback.controller;

import com.fivefull.coordiback.dto.SnapDto;
import com.fivefull.coordiback.entity.Comment;
import com.fivefull.coordiback.entity.Member;
import com.fivefull.coordiback.entity.Snap;
import com.fivefull.coordiback.service.SnapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/snaps")
public class SnapController {

    @Autowired
    private SnapService snapService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadSnap(
            @RequestParam("memberId") Long memberId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("images") List<MultipartFile> images,
            @RequestParam("categories") String categoriesJson) {
        try {
            Member member = new Member();
            member.setMemberId(memberId);

            List<String> categories = List.of(categoriesJson.replaceAll("[\\[\\]\"]", "").split(","));
            Snap snap = snapService.createSnap(member, description, images, categories);
            return ResponseEntity.ok(snap);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("서버 내부 오류");
        }
    }

    @GetMapping("/{snapId}")
    public ResponseEntity<SnapDto> getSnap(@PathVariable Long snapId) {
        Snap snap = snapService.getSnapById(snapId);
        SnapDto snapDto = snapService.convertToDto(snap);
        return ResponseEntity.ok(snapDto);
    }

    @DeleteMapping("/{snapId}")
    public ResponseEntity<?> deleteSnap(@PathVariable Long snapId, Principal principal) {
        try {
            snapService.deleteSnap(snapId, principal.getName());
            return ResponseEntity.ok("게시물이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{snapId}")
    public ResponseEntity<?> updateSnap(
            @PathVariable Long snapId,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam(value = "categories", required = false) String categoriesJson,
            @RequestParam(value = "newImages", required = false) List<MultipartFile> newImages,
            @RequestParam(value = "removedImageUrls", required = false) List<String> removedImageUrls,
            Principal principal) {
        try {
            List<String> categories = categoriesJson != null
                    ? List.of(categoriesJson.replaceAll("[\\[\\]\"]", "").split(","))
                    : null;

            Snap updatedSnap = snapService.updateSnap(snapId, principal.getName(), description, newImages, removedImageUrls, categories);
            return ResponseEntity.ok(updatedSnap);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("게시물 수정 중 오류가 발생했습니다.");
        }
    }
    @GetMapping
    public ResponseEntity<List<SnapDto>> getSnaps(@RequestParam(value = "category", required = false) String category) {
        List<SnapDto> snaps = category != null && !category.isEmpty()
                ? snapService.getSnapsByCategory(category)
                : snapService.getAllSnaps();
        return ResponseEntity.ok(snaps);
    }

    @GetMapping("/{snapId}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable Long snapId) {
        List<Comment> comments = snapService.getCommentsBySnapId(snapId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{snapId}/liked")
    public ResponseEntity<Map<String, Boolean>> checkIfLiked(@PathVariable Long snapId, Principal principal) {
        boolean liked = snapService.checkIfLiked(snapId, principal.getName());
        return ResponseEntity.ok(Collections.singletonMap("liked", liked));
    }

    @GetMapping("/{snapId}/like-count")
    public ResponseEntity<Map<String, Integer>> getLikeCount(@PathVariable Long snapId) {
        int likeCount = snapService.getLikeCount(snapId);
        return ResponseEntity.ok(Collections.singletonMap("likeCount", likeCount));
    }

    @PostMapping("/{snapId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable Long snapId, @RequestBody Map<String, String> payload, Principal principal) {
        Comment comment = snapService.addComment(snapId, principal.getName(), payload.get("content"));
        return ResponseEntity.ok(comment);
    }

    @PostMapping("/{snapId}/like")
    public ResponseEntity<Void> addLike(@PathVariable Long snapId, Principal principal) {
        snapService.addLike(snapId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{snapId}/like")
    public ResponseEntity<Void> removeLike(@PathVariable Long snapId, Principal principal) {
        snapService.removeLike(snapId, principal.getName());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-snaps")
    public ResponseEntity<List<SnapDto>> getMySnaps(Principal principal) {
        List<SnapDto> mySnaps = snapService.getSnapsByAuthor(principal.getName());
        return ResponseEntity.ok(mySnaps);
    }

    @GetMapping("/liked-snaps")
    public ResponseEntity<List<SnapDto>> getLikedSnaps(Principal principal) {
        List<SnapDto> likedSnaps = snapService.getLikedSnaps(principal.getName());
        return ResponseEntity.ok(likedSnaps);
    }

    @DeleteMapping("/{snapId}/comments/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long snapId, @PathVariable Long commentId, Principal principal) {
        try {
            snapService.deleteComment(snapId, commentId, principal.getName());
            return ResponseEntity.ok("댓글이 삭제되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{snapId}/comments/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long snapId, @PathVariable Long commentId,
                                           @RequestBody Map<String, String> payload, Principal principal) {
        try {
            snapService.updateComment(snapId, commentId, principal.getName(), payload.get("content"));
            return ResponseEntity.ok("댓글이 수정되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
