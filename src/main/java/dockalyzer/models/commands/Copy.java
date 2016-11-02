package dockalyzer.models.commands;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class Copy extends Instruction{
    String source;
    String destination;
    public Copy(String source, String destinatation) {
        super();
        this.source = source;
        this.destination=destinatation;
    }
}
