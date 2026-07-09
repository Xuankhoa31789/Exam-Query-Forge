package com.eqf.controller;

import com.eqf.dto.CastVoteRequest;
import com.eqf.dto.VoteResponse;
import com.eqf.security.AuthenticatedUser;
import com.eqf.service.VoteService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
public class VoteController {
    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    /** Vote với tư cách người đang đăng nhập (voterId lấy từ JWT). */
    @PostMapping("/{candidateId}/vote")
    public VoteResponse castVote(@PathVariable Long candidateId,
                                 @RequestBody CastVoteRequest request,
                                 @AuthenticationPrincipal AuthenticatedUser user) {
        return VoteResponse.from(voteService.castVote(candidateId, user.id(), request));
    }
}
