package com.nanda.research.job;

import com.nanda.research.followup.FollowUpTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FollowUpReminderJob {

    private final FollowUpTaskService followUpTaskService;

    @Scheduled(cron = "0 0 8 * * ?")
    public void remindUpcoming() {
        int today = followUpTaskService.listDueReminders(0).size();
        int ahead = followUpTaskService.listDueReminders(3).size();
        log.info("followUpReminder today={} ahead3days={}", today, ahead);
    }

    @Scheduled(cron = "0 30 0 * * ?")
    public void markOverdue() {
        int overdue = followUpTaskService.markOverdueTasks();
        log.info("followUpOverdue marked={}", overdue);
    }
}
