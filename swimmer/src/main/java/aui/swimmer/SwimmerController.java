package aui.swimmer;

import aui.swimmer.dto.GetSwimmerDto;
import aui.swimmer.dto.PostSwimmerDto;
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
@RequestMapping("swimmers")
@RequiredArgsConstructor
public class SwimmerController {

    private final SwimmerService swimmerService;

    @GetMapping
    public ResponseEntity<List<GetSwimmerDto>> getSwimmers() {
        List<Swimmer> swimmers = swimmerService.findAll();
        List<GetSwimmerDto> swimmersDto = GetSwimmerDto.entityToDto(swimmers);
        return new ResponseEntity<>(swimmersDto, OK);
    }

    @GetMapping("{id}")
    public ResponseEntity<GetSwimmerDto> getSwimmer(@PathVariable Long id) {
        return swimmerService.find(id)
                .map(swimmer -> new ResponseEntity<>(GetSwimmerDto.entityToDto(swimmer), OK))
                .orElseGet(() -> new ResponseEntity<>(NOT_FOUND));
    }

    @PostMapping
    public ResponseEntity<String> createSwimmer(@RequestBody PostSwimmerDto swimmerDto) {
        Swimmer swimmer = PostSwimmerDto.dtoToEntity(swimmerDto);
        Swimmer savedSwimmer = swimmerService.save(swimmer);
        return new ResponseEntity<>("Swimmer %s was added to the database!".formatted(savedSwimmer.getName()), CREATED);
    }

    @PutMapping("{id}")
    public ResponseEntity<String> updateSpecialization(@PathVariable Long id, @RequestParam String specialization) {
        Swimmer updatedSwimmer = swimmerService.updateSpecialization(id, specialization);
        return updatedSwimmer != null
                ? new ResponseEntity<>("A swimmer specialization was updated to %s!".formatted(updatedSwimmer.getSpecialization()), CREATED)
                : new ResponseEntity<>("This swimmer does not exist!", NO_CONTENT);
    }

    @DeleteMapping("{id}")
    @PreAuthorize("hasAuthority('admin')")
    public ResponseEntity<String> deleteSwimmer(@PathVariable Long id, Authentication authentication) {
        log.info("User authorities: {}", authentication.getAuthorities());
        return swimmerService.delete(id)
                ? new ResponseEntity<>("This swimmer was successfully deleted!", OK)
                : new ResponseEntity<>("This swimmer does not exist!", NOT_FOUND);
    }
}
