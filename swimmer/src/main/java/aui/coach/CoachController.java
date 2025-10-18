package aui.coach;

import aui.coach.dto.CreateCoachRequest;
import aui.swimmer.Swimmer;
import aui.swimmer.SwimmerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@RestController
@RequestMapping("coaches")
@RequiredArgsConstructor
class CoachController {

    private final CoachService coachService;
    private final SwimmerService swimmerService;

    @GetMapping("{id}/swimmers")
    ResponseEntity<List<Swimmer>> getCoachSwimmers(@PathVariable Long id) {
        Optional<Coach> coach = coachService.find(id);
        List<Swimmer> swimmers = swimmerService.findAll().stream()
                .filter(swimmer -> swimmer.getCoach() != null && swimmer.getCoach().getId().equals(coach.get().getId()))
                .collect(toList());
        return new ResponseEntity<>(swimmers, OK);
    }

    @PostMapping
    ResponseEntity<Void> createCoach(@RequestBody CreateCoachRequest request) {
        Coach coach = CreateCoachRequest.dtoToEntity(request);
        coachService.create(coach);
        return new ResponseEntity<>(OK);
    }

    @DeleteMapping("{id}")
    ResponseEntity<Void> deleteCoach(@PathVariable Long id) {
        Optional<Coach> coach = coachService.find(id);
        if (coach.isEmpty()) {
            return new ResponseEntity<>(NOT_FOUND);
        } else {
            coachService.delete(coach.get());
            return new ResponseEntity<>(OK);
        }
    }
}
