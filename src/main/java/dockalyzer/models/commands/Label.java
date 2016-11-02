package dockalyzer.models.commands;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class Label extends Instruction{
    String key;
    String value;
    public Label(String key, String value) {
        super();
        this.key= key;
        this.value=value;
    }
}
