package sparta.miniproject.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import sparta.miniproject.dto.CommentRequestDto;
import sparta.miniproject.dto.CommentResponseDto;
import sparta.miniproject.model.Comment;
import sparta.miniproject.service.CommentService;

import java.util.List;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "*", allowedHeaders = "*")
public class CommentController {

    private final CommentService commentService;


    //댓글 작성
    @PostMapping("/api/auth/comment/{boardId}")
    public Comment createComment(@PathVariable Long boardId, @RequestBody CommentRequestDto commentRequestDto) {
        return commentService.createComment(boardId, commentRequestDto);
    }

    //댓글 수정
    @PutMapping("/api/auth/comment/{commentId}")
    public void updateComment(@PathVariable Long commentId, @RequestBody CommentRequestDto commentRequestDto) {
        commentService.updateComment(commentId, commentRequestDto); //컨트롤러를 통해 서비스를 들어갔다가 컨트롤러를 통해 다시 나간다.
    }

    //댓글삭제
    @DeleteMapping("/api/auth/comment/{commentId}")
    public void deleteComment(@PathVariable Long commentId) {
        commentService.deleteComment(commentId);
    }

    @GetMapping("/api/comment/{boardId}")
    public List<CommentResponseDto> getCommentList(@PathVariable Long boardId) {
        return commentService.getCommentList(boardId);
    }

}
