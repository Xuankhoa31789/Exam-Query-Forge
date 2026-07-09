package com.eqf.service;

import com.eqf.dto.CastVoteRequest;
import com.eqf.model.ExamCandidate;
import com.eqf.model.ExamStatus;
import com.eqf.model.User;
import com.eqf.model.VerifyStatus;
import com.eqf.model.Vote;
import com.eqf.repository.ExamCandidateRepository;
import com.eqf.repository.UserRepository;
import com.eqf.repository.VoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class VoteService {
    private final VoteRepository voteRepository;
    private final ExamCandidateRepository examCandidateRepository;
    private final UserRepository userRepository;

    public VoteService(VoteRepository voteRepository,
                       ExamCandidateRepository examCandidateRepository,
                       UserRepository userRepository) {
        this.voteRepository = voteRepository;
        this.examCandidateRepository = examCandidateRepository;
        this.userRepository = userRepository;
    }

    /** voterId lấy từ JWT trong SecurityContext, không nhận từ client. */
    @Transactional
    public Vote castVote(Long candidateId, Long voterId, CastVoteRequest request) {
        if (candidateId == null) {
            throw new IllegalArgumentException("candidateId la bat buoc");
        }
        if (request == null) {
            throw new IllegalArgumentException("Du lieu vote la bat buoc");
        }
        if (voterId == null) {
            throw new IllegalArgumentException("voterId la bat buoc");
        }
        Short value = request.getValue();
        if (value == null || (value != -1 && value != 0 && value != 1)) {
            throw new IllegalArgumentException("Gia tri vote chi duoc la -1, 0 hoac 1");
        }
        String comment = normalizeComment(request.getComment());
        if (value != 1 && comment == null) {
            throw new IllegalArgumentException("Vote 0 hoac -1 bat buoc phai co comment");
        }

        ExamCandidate candidate = examCandidateRepository.findDetailedById(candidateId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay candidate id=" + candidateId));
        if (candidate.getExam().getStatus() != ExamStatus.REVIEW) {
            throw new IllegalArgumentException("Chi duoc vote khi ky thi dang o trang thai REVIEW");
        }

        User voter = userRepository.findById(voterId)
                .orElseThrow(() -> new IllegalArgumentException("Khong tim thay voter id=" + voterId));
        if (voter.getVerifyStatus() != VerifyStatus.VERIFIED) {
            throw new IllegalArgumentException("Chi giao vien da xac minh moi duoc vote");
        }

        Vote vote = voteRepository.findByCandidateIdAndVoterId(candidateId, voterId)
                .orElseGet(Vote::new);
        vote.setCandidate(candidate);
        vote.setVoter(voter);
        vote.setValue(value);
        vote.setComment(comment);
        return voteRepository.save(vote);
    }

    private String normalizeComment(String comment) {
        if (comment == null || comment.isBlank()) {
            return null;
        }
        return comment.trim();
    }
}
