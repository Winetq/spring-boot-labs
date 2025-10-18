package aui.swimmer;

import aui.coach.Coach;
import aui.coach.CoachService;
import aui.swimmer.dto.GetSwimmerDto;
import aui.swimmer.dto.PostSwimmerDto;
import aui.swimmer.dto.PostSwimmerWithCoachDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("swimmers")
@RequiredArgsConstructor
class SwimmerController {

    private final SwimmerService swimmerService;
    private final CoachService coachService;

    @GetMapping
    ResponseEntity<List<GetSwimmerDto>> getSwimmers() {
        List<Swimmer> swimmers = swimmerService.findAll();
        List<GetSwimmerDto> swimmersDto = GetSwimmerDto.entityToDto(swimmers);
        return new ResponseEntity<>(swimmersDto, OK);
    }

    @GetMapping("{id}")
    ResponseEntity<GetSwimmerDto> getSwimmer(@PathVariable Long id) {
        Optional<Swimmer> swimmer = swimmerService.find(id);
        if (swimmer.isEmpty()) return new ResponseEntity<>(NOT_FOUND);
        return new ResponseEntity<>(GetSwimmerDto.entityToDto(swimmer.get()), OK);
    }

    @GetMapping("{id}/coach")
    ResponseEntity<String> getSwimmerCoach(@PathVariable Long id) {
        Optional<Swimmer> swimmer = swimmerService.find(id);
        if (swimmer.isEmpty()) return new ResponseEntity<>(NOT_FOUND);
        return swimmerService.getSwimmerCoach(swimmer.get());
    }

    @PostMapping
    ResponseEntity<String> createSwimmer(@RequestBody PostSwimmerDto swimmerDto) {
        Swimmer swimmer = PostSwimmerDto.dtoToEntity(swimmerDto);
        List<Swimmer> swimmers = swimmerService.findAll();
        if (swimmers.contains(swimmer)) return new ResponseEntity<>("This swimmer was already created!", BAD_REQUEST);
        swimmerService.create(swimmer);
        return new ResponseEntity<>("A swimmer was added to the database!", OK);
    }

    @PostMapping("with_coach")
    ResponseEntity<String> createSwimmerWithCoach(@RequestBody PostSwimmerWithCoachDto swimmerDto) {
        Optional<Coach> coach = coachService.find(swimmerDto.getCoachId());
        if (coach.isEmpty()) return new ResponseEntity<>("This coach does not exist!", NOT_FOUND);
        Swimmer swimmer = PostSwimmerWithCoachDto.dtoToEntity(swimmerDto, coach.get());
        List<Swimmer> swimmers = swimmerService.findAll();
        if (swimmers.contains(swimmer)) return new ResponseEntity<>("This swimmer was already created!", BAD_REQUEST);
        swimmerService.create(swimmer);
        return new ResponseEntity<>("A swimmer was added to the database!", OK);
    }

    @PutMapping("{id}")
    ResponseEntity<String> changeSwimmerSpecialization(@PathVariable Long id,
                                                       @RequestParam String specialization) {
        Optional<Swimmer> swimmer = swimmerService.find(id);
        if (swimmer.isEmpty()) return new ResponseEntity<>("This swimmer does not exist", NOT_FOUND);
        swimmer.get().updateSwimmerSpecialization(SwimmingStyle.of(specialization));
        swimmerService.create(swimmer.get());
        return new ResponseEntity<>("A swimmer specialization was updated!", OK);
    }

    @DeleteMapping("{id}")
    ResponseEntity<String> deleteSwimmer(@PathVariable Long id) {
        Optional<Swimmer> swimmer = swimmerService.find(id);
        if (swimmer.isEmpty()) return new ResponseEntity<>("This swimmer does not exist!", NOT_FOUND);
        swimmerService.delete(swimmer.get());
        return new ResponseEntity<>("This swimmer was successfully deleted!", OK);
    }
}
