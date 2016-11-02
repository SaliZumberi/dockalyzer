package dockalyzer.models.commands;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class Add extends Instruction{
    String source;
    String destination;
    public Add(String source, String destinatation) {
        super();
        this.source = source;
        this.destination=destinatation;
    }
}
