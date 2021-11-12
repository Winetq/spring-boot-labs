package aui.coach;

import aui.coach.dto.GETCoachDTO;
import aui.coach.dto.POSTCoachDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("coaches")
class CoachController {
    private final CoachService coachService;

    @Autowired
    CoachController(CoachService coachService) {
        this.coachService = coachService;
    }

    @GetMapping
    ResponseEntity<List<GETCoachDTO>> getCoaches() {
        List<Coach> coaches = coachService.findAll();
        List<GETCoachDTO> coachesDTO = GETCoachDTO.entityToDTO(coaches);
        return new ResponseEntity<>(coachesDTO, HttpStatus.OK);
    }

    @GetMapping("{id}")
    ResponseEntity<GETCoachDTO> getCoach(@PathVariable Long id) {
        Optional<Coach> coach = coachService.find(id);
        if (coach.isEmpty()) return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        return new ResponseEntity<>(GETCoachDTO.entityToDTO(coach.get()), HttpStatus.OK);
    }

    @PostMapping
    ResponseEntity<String> createCoach(@RequestBody POSTCoachDTO coachDTO) {
        Coach coach = POSTCoachDTO.dtoToEntity(coachDTO);
        List<Coach> coaches = coachService.findAll();
        if (coaches.contains(coach)) return new ResponseEntity<>("This coach was already created!", HttpStatus.BAD_REQUEST);
        coachService.create(coach);
        return new ResponseEntity<>("A coach was added to the database!", HttpStatus.OK);
    }

    @DeleteMapping("{id}")
    ResponseEntity<String> deleteCoach(@PathVariable Long id) {
        Optional<Coach> coach = coachService.find(id);
        if (coach.isEmpty()) return new ResponseEntity<>("This coach does not exist!", HttpStatus.NOT_FOUND);
        coachService.delete(coach.get());
        return new ResponseEntity<>("This coach was successfully deleted!", HttpStatus.OK);
    }
}

