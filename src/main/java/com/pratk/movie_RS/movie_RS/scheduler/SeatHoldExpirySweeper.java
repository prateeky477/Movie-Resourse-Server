package com.pratk.movie_RS.movie_RS.scheduler;

import com.pratk.movie_RS.movie_RS.entity.ShowtimeSeat;
import com.pratk.movie_RS.movie_RS.repository.ShowtimeSeatRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class SeatHoldExpirySweeper {
// commented code since no need for scheduled sweeper if using redis
//    private final ShowtimeSeatRepository showtimeSeatRepository;
//
//    @Scheduled(fixedRate = 60_000)
//    @Transactional
//    public void releaseExpiredHolds() {
//        List<ShowtimeSeat> expired = showtimeSeatRepository
//                .findByStatusAndLockExpiresAtBefore(ShowtimeSeat.SeatStatus.LOCKED, LocalDateTime.now());
//
//        if (expired.isEmpty()) return;
//
//        expired.forEach(seat -> {
//            seat.setStatus(ShowtimeSeat.SeatStatus.AVAILABLE);
//            seat.setLockedByUserId(null);
//            seat.setLockExpiresAt(null);
//            try {
//                showtimeSeatRepository.save(seat);
//            } catch (ObjectOptimisticLockingFailureException ex) {
//                // already released by a concurrent release() call — fine, skip
//            }
//        });
//
//        log.info("Released {} expired seat holds", expired.size());
//    }
}