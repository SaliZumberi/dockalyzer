package dockalyzer.models.commands;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class Expose extends Instruction{
    long port;
    public Expose(String port) {
        super();
        this.port = Long.parseLong(port);
    }
}
