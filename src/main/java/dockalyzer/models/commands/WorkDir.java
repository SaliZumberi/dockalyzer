package dockalyzer.models.commands;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class WorkDir extends Instruction{
    String path;
    public WorkDir(String path) {
        super();
        this.path= path;
    }
}
