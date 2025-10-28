package aui.swimmer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static org.springframework.http.HttpStatus.OK;

@Slf4j
@RestController
@RequestMapping("coaches")
@RequiredArgsConstructor
public class CoachController {

    private final SwimmerService swimmerService;

    @GetMapping("{coachId}/swimmers")
    public ResponseEntity<List<Swimmer>> getCoachSwimmers(@PathVariable Long coachId) {
        List<Swimmer> swimmers = swimmerService.findByCoachId(coachId);
        return new ResponseEntity<>(swimmers, OK);
    }

    @DeleteMapping("{coachId}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<String> unassignCoach(@PathVariable Long coachId, Authentication authentication) {
        log.info("User authorities: {}", authentication.getAuthorities());
        int affectedSwimmers = swimmerService.unassignCoachFromSwimmers(coachId);
        return new ResponseEntity<>("%d swimmers were unassigned from coach %d".formatted(affectedSwimmers, coachId), OK);
    }
}
