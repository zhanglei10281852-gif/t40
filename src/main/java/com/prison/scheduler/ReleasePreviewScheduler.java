package com.prison.scheduler;

import com.prison.entity.Inmate;
import com.prison.repository.InmateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReleasePreviewScheduler {

    private final InmateRepository inmateRepository;

    @Scheduled(cron = "0 0 1 * * ?")
    public void scanUpcomingRelease() {
        log.info("开始执行刑满预告扫描任务");

        LocalDate today = LocalDate.now();
        LocalDate date90 = today.plusDays(90);
        LocalDate date30 = today.plusDays(30);
        LocalDate date7 = today.plusDays(7);

        List<Inmate> within90Days = inmateRepository.findUpcomingRelease(today, date90);
        List<Inmate> within30Days = inmateRepository.findUpcomingRelease(today, date30);
        List<Inmate> within7Days = inmateRepository.findUpcomingRelease(today, date7);

        log.info("刑满预告扫描完成 - 90天内: {}人, 30天内: {}人, 7天内: {}人",
                within90Days.size(), within30Days.size(), within7Days.size());

        for (Inmate inmate : within7Days) {
            log.warn("【7天释放提醒】服刑人员: {} ({}), 预计释放日期: {}",
                    inmate.getName(), inmate.getInmateNo(), inmate.getExpectedRelease());
        }

        for (Inmate inmate : within30Days) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, inmate.getExpectedRelease());
            if (daysLeft > 7) {
                log.info("【30天释放提醒】服刑人员: {} ({}), 距离释放还有 {} 天，需落实安置帮教",
                        inmate.getName(), inmate.getInmateNo(), daysLeft);
            }
        }

        for (Inmate inmate : within90Days) {
            long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, inmate.getExpectedRelease());
            if (daysLeft > 30) {
                log.info("【90天释放提醒】服刑人员: {} ({}), 距离释放还有 {} 天，需启动出监教育",
                        inmate.getName(), inmate.getInmateNo(), daysLeft);
            }
        }

        log.info("刑满预告扫描任务执行完毕");
    }
}
