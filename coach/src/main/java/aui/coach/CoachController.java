package aui.coach;

import aui.coach.dto.GetCoachDto;
import aui.coach.dto.PostCoachDto;
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
@RequestMapping("coaches")
@RequiredArgsConstructor
class CoachController {

    private final CoachService coachService;

    @GetMapping
    ResponseEntity<List<Coach>> getCoaches() {
        List<Coach> coaches = coachService.findAll();
        return new ResponseEntity<>(coaches, OK);
    }

    @GetMapping("{id}")
    ResponseEntity<GetCoachDto> getCoach(@PathVariable Long id) {
        Optional<Coach> coach = coachService.find(id);
        if (coach.isEmpty()) return new ResponseEntity<>(NOT_FOUND);
        return new ResponseEntity<>(GetCoachDto.entityToDto(coach.get()), OK);
    }

    @GetMapping("{id}/swimmers")
    ResponseEntity<String> getCoachSwimmers(@PathVariable Long id) {
        return coachService.getCoachSwimmers(id);
    }

    @PostMapping
    ResponseEntity<String> createCoach(@RequestBody PostCoachDto coachDTO) {
        Coach coach = PostCoachDto.dtoToEntity(coachDTO);
        List<Coach> coaches = coachService.findAll();
        if (coaches.contains(coach)) return new ResponseEntity<>("This coach was already created!", BAD_REQUEST);
        coachService.create(coach);
        return new ResponseEntity<>("A coach was added to the database!", OK);
    }

    @PutMapping("{id}")
    ResponseEntity<String> changeCoachLevel(@PathVariable Long id,
                                            @RequestParam int level) {
        Optional<Coach> coach = coachService.find(id);
        if (coach.isEmpty()) return new ResponseEntity<>("This coach does not exist", NOT_FOUND);
        coach.get().updateCoachLevel(level);
        coachService.create(coach.get());
        return new ResponseEntity<>("A coach level was updated!", OK);
    }

    @DeleteMapping("{id}")
    ResponseEntity<String> deleteCoach(@PathVariable Long id) {
        Optional<Coach> coach = coachService.find(id);
        if (coach.isEmpty()) return new ResponseEntity<>("This coach does not exist!", NOT_FOUND);
        coachService.delete(coach.get());
        return new ResponseEntity<>("This coach was successfully deleted!", OK);
    }
}
