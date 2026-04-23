package be.cytomine.service.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class Validation {
    private boolean ok = true;
    private ResponseEntity<?> responseEntity;
}
