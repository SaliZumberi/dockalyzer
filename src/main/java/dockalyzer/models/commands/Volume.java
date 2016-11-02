package dockalyzer.models.commands;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class Volume extends Instruction{
    String value;

    public Volume(String match) {
        super();
        this.value = match;
    }
}
