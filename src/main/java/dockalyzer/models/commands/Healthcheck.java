package dockalyzer.models.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class Healthcheck extends Instruction{
    Instruction healthInstruction = new Instruction();
    List<String> optionsBeforeInstructions = new ArrayList<>();

    public Healthcheck(Instruction instruction) {
        super();
        this.healthInstruction =instruction;
    }

    public Healthcheck(List<String> optionsBeforeInstructions) {
        super();
        this.optionsBeforeInstructions =optionsBeforeInstructions;
    }

    public Healthcheck(Instruction instruction, List<String> optionsBeforeInstructions) {
        super();
        this.healthInstruction =instruction;
        this.optionsBeforeInstructions =optionsBeforeInstructions;
    }
}
