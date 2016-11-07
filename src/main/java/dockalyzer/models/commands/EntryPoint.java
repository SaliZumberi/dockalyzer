package dockalyzer.models.commands;

import java.util.List;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class EntryPoint extends Instruction{
    String executable;
    List<String> params;
    public EntryPoint(String executable, List<String> params) {
        super();
        this.executable=executable;
        this.params = params;
    }
}
