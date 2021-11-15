package aui.coach;

import aui.coach.dto.CreateCoachRequest;
import aui.swimmer.Swimmer;
import aui.swimmer.SwimmerService;
import aui.swimmer.dto.GETSwimmerDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("coaches")
class CoachController {
    private final CoachService coachService;
    private final SwimmerService swimmerService;

    @Autowired
    CoachController(CoachService coachService, SwimmerService swimmerService) {
        this.coachService = coachService;
        this.swimmerService = swimmerService;
    }

    @GetMapping("{id}/swimmers")
    ResponseEntity<List<GETSwimmerDTO>> getCoachSwimmers(@PathVariable Long id) {
        Optional<Coach> coach = coachService.find(id);
        List<Swimmer> swimmers = swimmerService.findAll().stream()
                .filter(swimmer -> swimmer.getCoach()!= null && swimmer.getCoach().getId().equals(coach.get().getId()))
                .collect(Collectors.toList());
        return new ResponseEntity<>(GETSwimmerDTO.entityToDTO(swimmers), HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity<Void> createCoach(@RequestBody CreateCoachRequest request) {
        Coach coach = CreateCoachRequest.dtoToEntity(request);
        coachService.create(coach);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    ResponseEntity<Void> deleteCoach(@PathVariable Long id) {
        Optional<Coach> coach = coachService.find(id);
        if (coach.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } else {
            coachService.delete(coach.get());
            return new ResponseEntity<>(HttpStatus.OK);
        }
    }
}

