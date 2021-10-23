package aui.coach;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("coaches")
public class CoachController {
    private final CoachService service;

    @Autowired
    public CoachController(CoachService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<Coach>> getCoaches() {
        return new ResponseEntity<>(service.findAll(), HttpStatus.OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<Optional<Coach>> getCoach(@PathVariable Long id) {
        return new ResponseEntity<>(service.find(id), HttpStatus.OK);
    }

    @PostMapping("{name}/{level}")
    public ResponseEntity<String> createCoach(@PathVariable String name, @PathVariable int level) {
        Coach coach = new Coach(name, new ArrayList<>(), level);
        service.create(coach);
        return new ResponseEntity<>("A coach was added to database!", HttpStatus.OK);
    }
}

