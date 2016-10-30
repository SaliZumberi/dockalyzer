package dockalyzer.tools.dockerparser;

import java.io.File;

/**
 * Created by salizumberi-laptop on 22.10.2016.
 */
public class DockerParser {

    public File findFile(String name, File path) {
        File[] list = path.listFiles();
        if (list != null)
            for (File file : list) {
                if (file.isDirectory()) {
                    findFile(name, file);
                } else if (name.equalsIgnoreCase(file.getName())) {
                    return file;
                }
            }
            return null;
    }
}
