package com.eqf.controller;

import com.eqf.dto.CastVoteRequest;
import com.eqf.dto.VoteResponse;
import com.eqf.service.VoteService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
public class VoteController {
    private final VoteService voteService;

    public VoteController(VoteService voteService) {
        this.voteService = voteService;
    }

    @PostMapping("/{candidateId}/vote")
    public VoteResponse castVote(@PathVariable Long candidateId,
                                 @RequestBody CastVoteRequest request) {
        return VoteResponse.from(voteService.castVote(candidateId, request));
    }
}
