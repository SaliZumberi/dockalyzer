package dockalyzer.models.commands;

/**
 * Created by salizumberi-laptop on 01.11.2016.
 */
public class From extends Instruction{
    String imagename;
    double imageVersionNumber;
    String imageVersionString;
    String digest;

    boolean isAutomated;
    boolean isOfficial;

    public From(String imagename){
        this.imagename = imagename;
    }

    public  From(String imagename, double imageVersionNumber){
        this.imagename = imagename;
        this.imageVersionNumber = imageVersionNumber;
    }

    public From(String imagename, String digest, String what){
        this.imagename = imageVersionString;

        if(what.equals("diggest")){
            this.digest = digest;
        }else {
            this.imageVersionString = imageVersionString;
        }

    }
}
