package dockalyzer.tools.dockerlinter;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by salizumberi-laptop on 26.10.2016.
 */
public class DockerLinter {

    public static String getReportOfLinting(File file) throws IOException, InterruptedException{
        File dockerfile =new File("Dockerfile");

        ProcessBuilder processBuilder = new ProcessBuilder("cmd",
                "/c", "docker run --rm -i lukasmartinelli/hadolint < C:\\Users\\salizumberi-laptop\\workspace\\dockerlinter\\Dockerfile");

        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();
        StringBuilder processOutput = new StringBuilder();

        try (BufferedReader processOutputReader = new BufferedReader(
                new InputStreamReader(process.getInputStream())))
        {
            String readLine;
            while ((readLine = processOutputReader.readLine()) != null){
                processOutput.append(readLine + System.lineSeparator());
            }
            process.waitFor();
        }

        return processOutput.toString().trim();
    }
}
