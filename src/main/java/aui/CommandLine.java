package aui;

import aui.coach.CoachService;
import aui.swimmer.SwimmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class CommandLine implements CommandLineRunner {
    private final CoachService coachService;
    private final SwimmerService swimmerService;

    @Autowired
    public CommandLine(CoachService coachService, SwimmerService swimmerService) {
        this.coachService = coachService;
        this.swimmerService = swimmerService;
    }

    @Override
    public void run(String... args) {
        coachService.findAll().forEach(System.out::println);
    }
}

