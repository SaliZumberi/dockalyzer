package dockalyzer.models.commands;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class OnBuild extends Instruction{
    Instruction instruction;
    public OnBuild(Instruction instruction) {
        super();
        this.instruction = instruction;
    }
}
