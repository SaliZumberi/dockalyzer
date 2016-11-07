package dockalyzer.models.commands;

import java.util.List;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class Cmd extends Instruction{
    String executable;
    List<String> params;
    public Cmd(String executable, List<String> params) {
this.executable=executable;
    this.params=params;
    }
}
