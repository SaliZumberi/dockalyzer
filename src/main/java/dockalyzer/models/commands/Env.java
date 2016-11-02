package dockalyzer.models.commands;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class Env extends Instruction{
    String key;
    String value;
    public Env(String key, String value) {
        super();
        this.key=key;
        this.value=value;
    }
}
