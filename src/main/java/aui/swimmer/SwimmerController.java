package aui.swimmer;

import aui.coach.Coach;
import aui.coach.CoachService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("swimmers")
public class SwimmerController {
    private final SwimmerService swimmerService;
    private final CoachService coachService;

    @Autowired
    public SwimmerController(SwimmerService swimmerService, CoachService coachService) {
        this.swimmerService = swimmerService;
        this.coachService = coachService;
    }

    @GetMapping
    public ResponseEntity<List<Swimmer>> getSwimmers() {
        return new ResponseEntity<>(swimmerService.findAll(), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<Optional<Swimmer>> getSwimmer(@PathVariable Long id) {
        return new ResponseEntity<>(swimmerService.find(id), HttpStatus.OK);
    }

    @PostMapping("{name}/{specialization}")
    public ResponseEntity<String> createSwimmer(@PathVariable String name, @PathVariable String specialization) {
        List<Swimmer> swimmers = swimmerService.findAll();
        Swimmer swimmer = new Swimmer(name, SwimmingStyle.of(specialization));
        if (swimmers.contains(swimmer)) return new ResponseEntity<>("This swimmer was already created!", HttpStatus.CREATED);
        swimmerService.create(swimmer);
        return new ResponseEntity<>("A swimmer was added to the database!", HttpStatus.OK);
    }

    @PostMapping("{name}/{coach_id}/{specialization}")
    public ResponseEntity<String> createSwimmerWithCoach(@PathVariable String name, @PathVariable Long coach_id,
                                                         @PathVariable String specialization) {
        Optional<Coach> coach = coachService.find(coach_id);
        if (coach.isEmpty()) return new ResponseEntity<>("This coach is not exist!", HttpStatus.NOT_FOUND);
        List<Swimmer> swimmers = swimmerService.findAll();
        Swimmer swimmer = new Swimmer(name, coach.get(), SwimmingStyle.of(specialization));
        if (swimmers.contains(swimmer)) return new ResponseEntity<>("This swimmer was already created!", HttpStatus.CREATED);
        swimmerService.create(swimmer);
        return new ResponseEntity<>("A swimmer was added to the database!", HttpStatus.OK);
    }
}

