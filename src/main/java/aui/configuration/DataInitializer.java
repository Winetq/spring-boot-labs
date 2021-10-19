package aui.configuration;

import aui.coach.Coach;
import aui.coach.CoachService;
import aui.swimmer.Swimmer;
import aui.swimmer.SwimmerService;
import aui.swimmer.SwimmingStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Component
public class DataInitializer {
    private final CoachService coachService;
    private final SwimmerService swimmerService;

    @Autowired
    public DataInitializer(CoachService coachService, SwimmerService swimmerService) {
        this.coachService = coachService;
        this.swimmerService = swimmerService;
    }

    @PostConstruct
    private synchronized void init() {
        Swimmer swimmer1 = new Swimmer("swimmer-1", SwimmingStyle.FREESTYLE);
        Swimmer swimmer2 = new Swimmer("swimmer-2", SwimmingStyle.BACKSTROKE);
        Swimmer swimmer3 = new Swimmer("swimmer-3", SwimmingStyle.BREASTSTROKE);
        Swimmer swimmer4 = new Swimmer("swimmer-4", SwimmingStyle.BUTTERFLY);
        Swimmer swimmer5 = new Swimmer("swimmer-5", SwimmingStyle.BACKSTROKE);
        Swimmer swimmer6 = new Swimmer("swimmer-6", SwimmingStyle.BUTTERFLY);
        Swimmer swimmer7 = new Swimmer("swimmer-7", SwimmingStyle.BREASTSTROKE);
        Swimmer swimmer8 = new Swimmer("swimmer-8", SwimmingStyle.FREESTYLE);

        Coach coach1 = new Coach("coach-1", new ArrayList<>(List.of(swimmer1, swimmer2, swimmer3, swimmer4)), 5);
        Coach coach2 = new Coach("coach-2", new ArrayList<>(List.of(swimmer5, swimmer6, swimmer7, swimmer8)), 10);

        swimmerService.create(swimmer1);
        swimmerService.create(swimmer2);
        swimmerService.create(swimmer3);
        swimmerService.create(swimmer4);
        swimmerService.create(swimmer5);
        swimmerService.create(swimmer6);
        swimmerService.create(swimmer7);
        swimmerService.create(swimmer8);

        coachService.create(coach1);
        coachService.create(coach2);

        coach2.addSwimmer(swimmer1);
    }
}

