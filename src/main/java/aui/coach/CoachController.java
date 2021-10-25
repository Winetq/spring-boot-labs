package aui.coach;

import aui.coach.dto.GETCoachDTO;
import aui.coach.dto.POSTCoachDTO;
import aui.swimmer.Swimmer;
import aui.swimmer.SwimmerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("coaches")
public class CoachController {
    private final CoachService coachService;
    private final SwimmerService swimmerService;

    @Autowired
    public CoachController(CoachService coachService, SwimmerService swimmerService) {
        this.coachService = coachService;
        this.swimmerService = swimmerService;
    }

    @GetMapping
    public ResponseEntity<List<GETCoachDTO>> getCoaches() {
        List<Coach> coaches = coachService.findAll();
        List<GETCoachDTO> coachesDTO = GETCoachDTO.entityToDTO(coaches);
        return new ResponseEntity<>(coachesDTO, HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<GETCoachDTO> getCoach(@PathVariable Long id) {
        Optional<Coach> coach = coachService.find(id);
        if (coach.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(GETCoachDTO.entityToDTO(coach.get()), HttpStatus.OK);
    }

    @PostMapping
    public ResponseEntity<String> createCoach(@RequestBody POSTCoachDTO coachDTO) {
        Coach coach = POSTCoachDTO.dtoToEntity(coachDTO);
        List<Coach> coaches = coachService.findAll();
        if (coaches.contains(coach)) return new ResponseEntity<>("This coach was already created!", HttpStatus.CREATED);
        coachService.create(coach);
        return new ResponseEntity<>("A coach was added to the database!", HttpStatus.OK);
    }

    @PutMapping("{coach_id}/{swimmer_id}")
    public ResponseEntity<String> assignSwimmerToCoach(@PathVariable Long coach_id, @PathVariable Long swimmer_id) {
        Optional<Coach> coach = coachService.find(coach_id);
        Optional<Swimmer> swimmer = swimmerService.find(swimmer_id);
        if (coach.isEmpty() || swimmer.isEmpty()) return new ResponseEntity<>("This coach or swimmer does not exist!", HttpStatus.NOT_FOUND);
        swimmer.get().assignCoach(coach.get());
        coach.get().addSwimmer(swimmer.get());
        swimmerService.create(swimmer.get()); // save a change in the database
        return new ResponseEntity<>(swimmer.get().getName() + " was assigned to the coach " + coach.get().getName(), HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<String> deleteCoach(@PathVariable Long id) {
        Optional<Coach> coach = coachService.find(id);
        if (coach.isEmpty()) return new ResponseEntity<>("This coach does not exist!", HttpStatus.NOT_FOUND);
        coach.get().getSwimmers().forEach(x -> {
            x.assignCoach(null);
            swimmerService.create(x);
        });
        coachService.delete(coach.get());
        return new ResponseEntity<>("This coach was successfully deleted!", HttpStatus.OK);
    }
}

