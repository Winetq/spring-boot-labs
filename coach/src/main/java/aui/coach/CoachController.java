package aui.coach;

import aui.coach.dto.GetCoachDto;
import aui.coach.dto.PostCoachDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.NO_CONTENT;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("coaches")
@RequiredArgsConstructor
public class CoachController {

    private final CoachService coachService;

    @GetMapping
    public ResponseEntity<List<GetCoachDto>> getCoaches() {
        List<Coach> coaches = coachService.findAll();
        List<GetCoachDto> coachesDto = GetCoachDto.entityToDto(coaches);
        return new ResponseEntity<>(coachesDto, OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<GetCoachDto> getCoach(@PathVariable Long id) {
        return coachService.find(id)
                .map(coach -> new ResponseEntity<>(GetCoachDto.entityToDto(coach), OK))
                .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @GetMapping("{id}/swimmers")
    public ResponseEntity<String> getCoachSwimmers(@PathVariable Long id) {
        return coachService.getCoachSwimmers(id);
    }

    @PostMapping
    public ResponseEntity<String> createCoach(@RequestBody PostCoachDto coachDto) {
        Coach coach = PostCoachDto.dtoToEntity(coachDto);
        Coach savedCoach = coachService.save(coach);
        return new ResponseEntity<>("Coach %s was added to the database!".formatted(savedCoach.getName()), CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<String> updateLevel(@PathVariable Long id, @RequestParam int level) {
        Coach updatedCoach = coachService.updateLevel(id, level);
        return updatedCoach != null
                ? new ResponseEntity<>("A coach level was updated to %d!".formatted(updatedCoach.getLevel()), CREATED)
                : new ResponseEntity<>("This coach does not exist!", NO_CONTENT);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<String> deleteCoach(@PathVariable Long id, Authentication authentication) {
        log.info("User authorities: {}", authentication.getAuthorities());
        ResponseEntity<String> response = coachService.delete(id);
        return response != null
                ? new ResponseEntity<>("This coach was successfully deleted! %s".formatted(response.getBody()), OK)
                : new ResponseEntity<>("This coach does not exist!", NOT_FOUND);
    }
}
