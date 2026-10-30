package sn.sunagri.shared.adapter.out.time;

import java.time.LocalDate;
import org.springframework.stereotype.Component;
import sn.sunagri.shared.domain.ClockPort;

@Component
public class HorlogeSysteme implements ClockPort {

    @Override
    public LocalDate aujourdHui() {
        return LocalDate.now();
    }
}
