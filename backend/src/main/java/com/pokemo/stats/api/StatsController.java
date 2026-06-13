package com.pokemo.stats.api;

import com.pokemo.common.CurrentUserProvider;
import com.pokemo.stats.service.StatsService;
import java.security.Principal;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;
    private final CurrentUserProvider currentUserProvider;

    public StatsController(StatsService statsService, CurrentUserProvider currentUserProvider) {
        this.statsService = statsService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/trend")
    public List<AccuracyTrendResponse> getAccuracyTrend(Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return statsService.getAccuracyTrend(userId);
    }

    @GetMapping("/progress")
    public List<SubjectProgressResponse> getSubjectProgress(Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return statsService.getSubjectProgress(userId);
    }

    @GetMapping("/weekly")
    public List<WeeklyStudyResponse> getWeeklyStudy(Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return statsService.getWeeklyStudy(userId);
    }

    @GetMapping("/type-accuracy")
    public List<TypeAccuracyResponse> getTypeAccuracy(Principal principal) {
        long userId = currentUserProvider.userId(principal);
        return statsService.getTypeAccuracy(userId);
    }
}
