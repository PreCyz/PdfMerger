package pg.merger;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

class ControllerTest {

    @Test
    void name() {
        System.out.println(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-h24mmss")));
    }
}