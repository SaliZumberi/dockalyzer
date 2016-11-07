package dockalyzer.models.commands;

import java.util.List;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class Run extends Instruction{
    String executable;
    List<String> params;
    public Run(String executable, List<String> params) {
        this.executable=executable;
        this.params=params;
    }
}
