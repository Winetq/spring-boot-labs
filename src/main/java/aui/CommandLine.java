package aui;

import aui.coach.Coach;
import aui.coach.CoachService;
import aui.swimmer.Swimmer;
import aui.swimmer.SwimmerService;
import aui.swimmer.SwimmingStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.PrintStream;
import java.util.Arrays;
import java.util.Optional;
import java.util.Scanner;
import java.util.UUID;

@Component
public class CommandLine implements CommandLineRunner {
    private final CoachService coachService;
    private final SwimmerService swimmerService;
    private final Scanner scanner = new Scanner(System.in);
    private final PrintStream out = System.out;

    @Autowired
    public CommandLine(CoachService coachService, SwimmerService swimmerService) {
        this.coachService = coachService;
        this.swimmerService = swimmerService;
    }

    @Override
    public void run(String... args) {
        boolean running = true;
        while (running) {
            out.print("command: ");
            String command = scanner.nextLine();
            switch (command) {
                case "swimmers" -> swimmerService.findAll().forEach(out::println);
                case "coaches" -> coachService.findAll().forEach(out::println);
                case "find swimmer" -> findSwimmerById();
                case "find coach" -> findCoachById();
                case "create swimmer" -> createNewSwimmer();
                case "delete swimmer" -> deleteSwimmerById();
                case "exit" -> running = false;
                default -> showAvailableCommands();
            }
        }
    }

    private void findSwimmerById() {
        out.print("id: ");
        String id = scanner.nextLine();
        try {
            UUID uuid = UUID.fromString(id);
            out.println(swimmerService.find(uuid));
        } catch (IllegalArgumentException e) {
            out.println("Incorrect id!");
        }
    }

    private void findCoachById() {
        out.print("id: ");
        String id = scanner.nextLine();
        try {
            UUID uuid = UUID.fromString(id);
            out.println(coachService.find(uuid));
        } catch (IllegalArgumentException e) {
            out.println("Incorrect id!");
        }
    }

    private void createNewSwimmer() {
        out.print("name: ");
        String name = scanner.nextLine();
        out.println(Arrays.toString(SwimmingStyle.values()));
        out.print("style: ");
        String style = scanner.nextLine();
        try {
            SwimmingStyle specialization = SwimmingStyle.of(style);
            coachService.findAll().forEach(out::println);
            out.print("coach id: ");
            String id = scanner.nextLine();
            UUID uuid = UUID.fromString(id);
            Optional<Coach> coach = coachService.find(uuid);
            coach.ifPresent(value -> swimmerService.create(new Swimmer(name, value, specialization)));
        } catch (IllegalArgumentException e) {
            out.println("Incorrect data!");
        }
    }

    private void deleteSwimmerById() {
        out.print("id: ");
        String id = scanner.nextLine();
        try {
            UUID uuid = UUID.fromString(id);
            Optional<Swimmer> swimmer = swimmerService.find(uuid);
            swimmer.ifPresent(value -> {
                value.getCoach().getSwimmers().remove(value);
                swimmerService.delete(value);
            });
        } catch (IllegalArgumentException e) {
            out.println("Incorrect id!");
        }
    }

    private void showAvailableCommands() {
        out.println("Commands:");
        out.println("swimmers");
        out.println("coaches");
        out.println("find swimmer");
        out.println("find coach");
        out.println("create swimmer");
        out.println("delete swimmer");
        out.println("exit");
    }
}

