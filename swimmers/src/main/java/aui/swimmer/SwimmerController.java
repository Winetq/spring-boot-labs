package aui.swimmer;

import aui.coach.Coach;
import aui.coach.CoachService;
import aui.swimmer.dto.GETSwimmerDTO;
import aui.swimmer.dto.POSTSwimmerDTO;
import aui.swimmer.dto.POSTSwimmerWithCoachDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("swimmers")
class SwimmerController {
    private final SwimmerService swimmerService;
    private final CoachService coachService;

    @Autowired
    SwimmerController(SwimmerService swimmerService, CoachService coachService) {
        this.swimmerService = swimmerService;
        this.coachService = coachService;
    }

    @GetMapping
    ResponseEntity<List<GETSwimmerDTO>> getSwimmers() {
        List<Swimmer> swimmers = swimmerService.findAll();
        List<GETSwimmerDTO> swimmersDTO = GETSwimmerDTO.entityToDTO(swimmers);
        return new ResponseEntity<>(swimmersDTO, HttpStatus.OK);
    }

    @GetMapping("{id}")
    ResponseEntity<GETSwimmerDTO> getSwimmer(@PathVariable Long id) {
        Optional<Swimmer> swimmer = swimmerService.find(id);
        if (swimmer.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(GETSwimmerDTO.entityToDTO(swimmer.get()), HttpStatus.OK);
    }

    @GetMapping("{id}/coach")
    ResponseEntity<String> getSwimmerCoach(@PathVariable Long id) {
        Optional<Swimmer> swimmer = swimmerService.find(id);
        if (swimmer.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return swimmerService.getSwimmerCoach(swimmer.get());
    }

    @PostMapping
    ResponseEntity<String> createSwimmer(@RequestBody POSTSwimmerDTO swimmerDTO) {
        Swimmer swimmer = POSTSwimmerDTO.dtoTOEntity(swimmerDTO);
        List<Swimmer> swimmers = swimmerService.findAll();
        if (swimmers.contains(swimmer)) return new ResponseEntity<>("This swimmer was already created!", HttpStatus.BAD_REQUEST);
        swimmerService.create(swimmer);
        return new ResponseEntity<>("A swimmer was added to the database!", HttpStatus.OK);
    }

    @PostMapping("with_coach")
    ResponseEntity<String> createSwimmerWithCoach(@RequestBody POSTSwimmerWithCoachDTO swimmerDTO) {
        Optional<Coach> coach = coachService.find(swimmerDTO.getCoach_id());
        if (coach.isEmpty()) return new ResponseEntity<>("This coach does not exist!", HttpStatus.NOT_FOUND);
        Swimmer swimmer = POSTSwimmerWithCoachDTO.dtoTOEntity(swimmerDTO, coach.get());
        List<Swimmer> swimmers = swimmerService.findAll();
        if (swimmers.contains(swimmer)) return new ResponseEntity<>("This swimmer was already created!", HttpStatus.BAD_REQUEST);
        swimmerService.create(swimmer);
        return new ResponseEntity<>("A swimmer was added to the database!", HttpStatus.OK);
    }

    @PutMapping("{id}")
    ResponseEntity<String> changeSwimmerSpecialization(@PathVariable Long id,
                                                              @RequestParam(value = "specialization") String specialization) {
        Optional<Swimmer> swimmer = swimmerService.find(id);
        if (swimmer.isEmpty()) return new ResponseEntity<>("This swimmer does not exist", HttpStatus.NOT_FOUND);
        swimmer.get().updateSwimmerSpecialization(SwimmingStyle.of(specialization));
        swimmerService.create(swimmer.get());
        return new ResponseEntity<>("A swimmer specialization was updated!", HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    ResponseEntity<String> deleteSwimmer(@PathVariable Long id) {
        Optional<Swimmer> swimmer = swimmerService.find(id);
        if (swimmer.isEmpty()) return new ResponseEntity<>("This swimmer does not exist!", HttpStatus.NOT_FOUND);
        swimmerService.delete(swimmer.get());
        return new ResponseEntity<>("This swimmer was successfully deleted!", HttpStatus.OK);
    }
}

