package aui.coach;

import aui.coach.dto.CreateCoachRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("coaches")
class CoachController {
    private final CoachService coachService;

    @Autowired
    CoachController(CoachService coachService) {
        this.coachService = coachService;
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

