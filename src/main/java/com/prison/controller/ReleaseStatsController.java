package com.prison.controller;

import com.prison.entity.Inmate;
import com.prison.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/release-stats")
@RequiredArgsConstructor
public class ReleaseStatsController {

    private final InmateRepository inmateRepository;
    private final ReleaseEducationRepository releaseEducationRepository;
    private final AssistanceDockingRepository assistanceDockingRepository;
    private final FollowUpRecordRepository followUpRecordRepository;
    private final ReleaseDestinationRepository releaseDestinationRepository;
    private final ReleaseProcedureRepository releaseProcedureRepository;

    @GetMapping("/monthly")
    public ResponseEntity<?> getMonthlyStats(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        if (startDate == null) {
            startDate = LocalDate.now().withDayOfMonth(1);
        }
        if (endDate == null) {
            endDate = LocalDate.now();
        }

        Map<String, Object> stats = new LinkedHashMap<>();

        long monthlyRelease = inmateRepository.countReleasedBetween(startDate, endDate);
        stats.put("monthlyReleaseCount", monthlyRelease);

        long totalInEducation = releaseEducationRepository.countTotalInmates();
        long completedEducation = releaseEducationRepository.countCompletedInmates();
        double educationCompletionRate = totalInEducation > 0 ? (double) completedEducation / totalInEducation * 100 : 0;
        stats.put("educationCompletionRate", Math.round(educationCompletionRate * 100) / 100.0);
        stats.put("totalInEducation", totalInEducation);
        stats.put("completedEducation", completedEducation);

        long totalDocking = assistanceDockingRepository.countBySendDateBetween(startDate, endDate);
        long receiptCount = assistanceDockingRepository.countByReceiptAcceptedTrue();
        double dockingCoverage = monthlyRelease > 0 ? (double) receiptCount / monthlyRelease * 100 : 0;
        stats.put("dockingCoverageRate", Math.round(dockingCoverage * 100) / 100.0);
        stats.put("totalDocking", totalDocking);
        stats.put("receiptCount", receiptCount);

        long totalFollowUps = followUpRecordRepository.countByFollowUpDateBetween(startDate, endDate);
        long contactValid = followUpRecordRepository.countByContactValidTrueAndFollowUpDateBetween(startDate, endDate);
        double followUpRate = monthlyRelease > 0 ? (double) totalFollowUps / (monthlyRelease * 3) * 100 : 0;
        stats.put("followUpRate", Math.round(followUpRate * 100) / 100.0);
        stats.put("totalFollowUps", totalFollowUps);

        Map<String, Long> destinationDistribution = new LinkedHashMap<>();
        destinationDistribution.put("回原籍", releaseDestinationRepository.countByDestinationType("回原籍"));
        destinationDistribution.put("投靠亲友", releaseDestinationRepository.countByDestinationType("投靠亲友"));
        destinationDistribution.put("其他", releaseDestinationRepository.countByDestinationType("其他"));
        stats.put("destinationDistribution", destinationDistribution);

        long employed = followUpRecordRepository.countByCurrentStatusAndFollowUpDateBetween("已就业", startDate, endDate);
        double employmentRate = contactValid > 0 ? (double) employed / contactValid * 100 : 0;
        stats.put("employmentRate", Math.round(employmentRate * 100) / 100.0);
        stats.put("employedCount", employed);
        stats.put("contactValidCount", contactValid);

        stats.put("startDate", startDate);
        stats.put("endDate", endDate);

        return ResponseEntity.ok(stats);
    }

    @GetMapping("/summary")
    public ResponseEntity<?> getSummaryStats() {
        LocalDate today = LocalDate.now();
        LocalDate monthStart = today.withDayOfMonth(1);

        Map<String, Object> summary = new LinkedHashMap<>();

        summary.put("upcoming90Days", inmateRepository.countUpcomingRelease(today, today.plusDays(90)));
        summary.put("upcoming30Days", inmateRepository.countUpcomingRelease(today, today.plusDays(30)));
        summary.put("upcoming7Days", inmateRepository.countUpcomingRelease(today, today.plusDays(7)));

        summary.put("monthlyRelease", inmateRepository.countReleasedBetween(monthStart, today));
        summary.put("totalReleased", inmateRepository.countByStatus("已释放"));

        long totalInEducation = releaseEducationRepository.countTotalInmates();
        long completedEducation = releaseEducationRepository.countCompletedInmates();
        summary.put("educationCompletionRate", totalInEducation > 0
                ? Math.round((double) completedEducation / totalInEducation * 10000) / 100.0
                : 0);

        summary.put("pendingDocking", assistanceDockingRepository.countByStatus("待发送"));
        summary.put("sentDocking", assistanceDockingRepository.countByStatus("已发送"));

        summary.put("noClearDestination", releaseDestinationRepository.countByHasClearDestinationFalse());

        long lostContact = followUpRecordRepository.countByCurrentStatusAndFollowUpDateBetween(
                "失联", today.minusMonths(12), today);
        summary.put("lostContactCount", lostContact);

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/destination-distribution")
    public ResponseEntity<?> getDestinationDistribution() {
        Map<String, Object> result = new LinkedHashMap<>();

        long total = releaseDestinationRepository.count();

        List<Map<String, Object>> distribution = new ArrayList<>();

        Map<String, Object> item1 = new LinkedHashMap<>();
        item1.put("type", "回原籍");
        item1.put("count", releaseDestinationRepository.countByDestinationType("回原籍"));
        item1.put("percentage", total > 0
                ? Math.round((double) releaseDestinationRepository.countByDestinationType("回原籍") / total * 10000) / 100.0
                : 0);
        distribution.add(item1);

        Map<String, Object> item2 = new LinkedHashMap<>();
        item2.put("type", "投靠亲友");
        item2.put("count", releaseDestinationRepository.countByDestinationType("投靠亲友"));
        item2.put("percentage", total > 0
                ? Math.round((double) releaseDestinationRepository.countByDestinationType("投靠亲友") / total * 10000) / 100.0
                : 0);
        distribution.add(item2);

        Map<String, Object> item3 = new LinkedHashMap<>();
        item3.put("type", "其他");
        item3.put("count", releaseDestinationRepository.countByDestinationType("其他"));
        item3.put("percentage", total > 0
                ? Math.round((double) releaseDestinationRepository.countByDestinationType("其他") / total * 10000) / 100.0
                : 0);
        distribution.add(item3);

        result.put("total", total);
        result.put("distribution", distribution);

        return ResponseEntity.ok(result);
    }
}
