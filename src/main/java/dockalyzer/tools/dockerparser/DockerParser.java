package dockalyzer.tools.dockerparser;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dockalyzer.models.DockerFile;
import dockalyzer.models.commands.*;
import jdk.nashorn.internal.ir.Labels;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Created by salizumberi-laptop on 22.10.2016.
 */
public class DockerParser {
    DockerFile dockerfile;
    public String localDockerfilePath;
    public String localPath;
    public List<From> froms = new ArrayList<>();
    public List<Maintainer> maintainers = new ArrayList<>();
    public List<Run> runs = new ArrayList<>();
    public List<Cmd> cmds = new ArrayList<>();
    public List<Label> labels = new ArrayList<>();
    public List<Env> envs = new ArrayList<>();
    public List<Expose> exposes = new ArrayList<>();
    public List<Add> adds = new ArrayList<>();
    public List<Copy> copies = new ArrayList<>();
    public List<EntryPoint> entryPoints = new ArrayList<>();
    public List<Volume> volumes = new ArrayList<>();
    public List<User> users = new ArrayList<>();
    public List<WorkDir> workDirs = new ArrayList<>();
    public List<Arg> args = new ArrayList<>();
    public List<OnBuild> onBuilds = new ArrayList<>();
    public List<StopSignal> stopSignals = new ArrayList<>();
    public List<Healthcheck> healthchecks = new ArrayList<>();

    public DockerParser(String localpath, String localDockerfilePath) {
        String[] pathTokens = localDockerfilePath.split("/");
        if (pathTokens.length == 1) {
            this.localDockerfilePath = "";
        } else {
            String newPath = "";
            for (int i = 0; i < pathTokens.length - 1; i++) {
                newPath += pathTokens[i] + "/";
            }
            this.localDockerfilePath = newPath;
        }
        this.localPath = localpath;
    }

    public DockerFile getDockerfileObject() throws IOException {
        dockerfile = new DockerFile();
        File flatDockerfile = getFlatDockerFile();
        doClassificationOfLines(flatDockerfile);
        assignToDockerObject();
        return dockerfile;
    }

    public void printLines(File file) throws IOException {
        try (Stream<String> lines = Files.lines(file.toPath(), Charset.defaultCharset())) {
            lines.forEachOrdered(System.out::println);
        }
    }

    public File getFlatDockerFile(File dockerFile) throws IOException {
        System.out.println("**Parent path og this Dockerfile:" + dockerFile.getParentFile().getPath());
        File flatDockerfile = new File(dockerFile.getParentFile().getPath() + "\\DockerFileFlat");
        BufferedWriter writer = new BufferedWriter(new FileWriter(flatDockerfile));
        FileInputStream fis = new FileInputStream(dockerFile);

        //Construct BufferedReader from InputStreamReader
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line = null;
        String newLine = null;
        boolean concatFlag = false;

        while ((line = reader.readLine()) != null) {
            if (doesLineHaveAnInstruction(line) && line.contains(" \\")) {
                newLine = "";
                newLine += line;
                concatFlag = true;
            } else if (line.contains(" \\") && concatFlag) {
                newLine += line;
            } else if (!doesLineHaveAnInstruction(line) && !line.contains(" \\") && concatFlag) {
                newLine += line;
                newLine = newLine.replace(" \\", "");
                newLine = newLine.trim().replaceAll(" +", " ");
                writer.write(newLine);
                writer.newLine();
                concatFlag = false;
            } else if (doesLineHaveAnInstruction(line) && !line.contains(" \\")) {
                line = line.trim().replaceAll(" +", " ");
                writer.write(line);
                writer.newLine();
            }
        }
        writer.close();
        reader.close();
        return flatDockerfile;
    }

    public File getFlatDockerFile(String localRepoPath, String localDockerfilePath) throws IOException {
        String localPathtoDockerfile = localRepoPath.concat("/").concat(localDockerfilePath);
        File dockerfile = findFile("Dockerfile", new File(localPathtoDockerfile));
        return getFlatDockerFile(dockerfile);
    }

    public File getFlatDockerFile() throws IOException {
        String localPathtoDockerfile = localPath.concat("/").concat("/").concat(localDockerfilePath);
        File dockerfile = findFile("Dockerfile", new File(localPathtoDockerfile));
        return getFlatDockerFile(dockerfile);
    }

    private static boolean doesLineHaveAnInstruction(String line) {
        if (line.contains("ADD")) {
            return true;
        } else if (line.contains("FROM")) {
            return true;
        } else if (line.contains("CMD")) {
            return true;
        } else if (line.contains("COPY")) {
            return true;
        } else if (line.contains("ENTRYPOINT")) {
            return true;
        } else if (line.contains("ENV")) {
            return true;
        } else if (line.contains("EXPOSE")) {
            return true;
        } else if (line.contains("FROM")) {
            return true;
        } else if (line.contains("HEALTHCHECK")) {
            return true;
        } else if (line.contains("INSTRUCTION")) {
            return true;
        } else if (line.contains("LABEL")) {
            return true;
        } else if (line.contains("MAINTAINER")) {
            return true;
        } else if (line.contains("ONBUILD")) {
            return true;
        } else if (line.contains("RUN")) {
            return true;
        } else if (line.contains("STOPSIGNAL")) {
            return true;
        } else if (line.contains("USER")) {
            return true;
        } else if (line.contains("VOLUME")) {
            return true;
        } else if (line.contains("WORKDIR")) {
            return true;
        } else {
            return false;
        }
    }

    public void doClassificationOfLines(File file) throws IOException {
        try (Stream<String> lines = Files.lines(file.toPath(), Charset.defaultCharset())) {
            lines.forEachOrdered(line -> fillDockerFileObject(getClassification(line)));
        }
    }

    public void fillDockerFileObject(Instruction instructions) {
        if (instructions instanceof From) {
            System.out.println("ITS A FROM");
        }
    }

    public Instruction getClassification(String line) {
        String arr[] = line.split(" ", 2);

        String instruction = arr[0];   //Instruction
        String command;
        if (arr.length > 1) {
            command = arr[1];
        } else {
            command = "";
        }

        switch (instruction) {
            case "FROM":
                return parseFromInstruction(command);
            case "ENV":
                return (parseEnvInstruction(command));
            case "MAINTAINER":
                return (parseMaintainerInstruction(command));
            case "RUN":
                return (parseRunInstruction(command));
            case "CMD":
                return (parseCMDInstruction(command));
            case "ADD":
                return (parseAddInstruction(command));
            case "COPY":
                return (parseCopyInstruction(command));
            case "EXPOSE":
                return (parseExposeInstruction(command));
            case "LABEL":
                return (parseLabelInstruction(command));
            case "ENTRYPOINT":
                return (parseEntryPointInstruction(command));
            case "VOLUME":
                return (parseVolumeInstruction(command));
            case "WORKDIR":
                return (parseWorkDirInstruction(command));
            case "ARG":
                return (parseArgInstruction(command));
            case "ONBUILD":
                return (parseOnBuildInstruction(command));
            case "STOPSIGNAL":
                return (parseStopSignalInstruction(command));
            case "HEALTHCHECK":
                return (parseHealthCheckInstruction(command));
            case "SHELL":
                return (parseShellInstruction(command));
            case "USER":
                return (parseUserInstruction(command));
        }
        return null;
    }

    private Instruction parseExposeInstruction(String ports) {
        String[] parts = ports.split(" ");
        for (int i = 0; i < parts.length; i++) {
            exposes.add(new Expose(parts[i]));
        }
        return null;
    }

    private Instruction parseEntryPointInstruction(String command) {
        String executable = null;
        List<String> params = new ArrayList<>();
        if (command.contains("[")) {
            Pattern p = Pattern.compile("\"(.*?)\"");
            Matcher m = p.matcher(command);

            List<String> matches = new ArrayList<String>();
            while (m.find()) {
                matches.add(m.group(1));
            }

            for (int i = 0; i < matches.size(); i++) {
                if (i == 0) {
                    executable = matches.get(i);
                } else {
                    params.add(matches.get(i));
                }
            }
        } else {
            String[] parts = command.split(" ");

            for (int i = 0; i < parts.length; i++) {
                if (i == 0) {
                    executable = parts[i];

                } else {
                    params.add(parts[i]);
                }
            }

        }
        entryPoints.add(new EntryPoint(executable, params));
        return null;

    }

    public Instruction copyAndAdd(String command, String instruction) {
        String source = null;
        String destinatation = null;
        if (command.contains("[")) {
            System.out.println("copyAndAdd YES IT CONTAINS [");
            Pattern p = Pattern.compile("\"(.*?)\"");
            Matcher m = p.matcher(command);

            List<String> matches = new ArrayList<String>();
            while (m.find()) {
                matches.add(m.group(1));
            }
            for (int i = 0; i < matches.size(); i++) {
                if (i == 0) {
                    source = matches.get(i);
                    System.out.println("this is exec:[ + " + source);
                } else {
                    source = matches.get(i);
                }
            }
        } else {
            System.out.println("copyAndAdd No it doesnt contain[");
            String[] parts = command.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (i == 0) {
                    source = parts[i];
                    System.out.println("this is source:" + source);

                } else {
                    System.out.println("this is dest: " + parts[i]);
                    source = parts[i];
                }
            }
        }

        if (instruction.equals("ADD")) {
            adds.add(new Add(source, destinatation));
        } else if (instruction.equals("COPY")) {
            copies.add(new Copy(source, destinatation));
        }
        return null;
    }

    private Instruction parseCopyInstruction(String command) {
        return copyAndAdd(command, "COPY");

    }

    private Instruction parseAddInstruction(String command) {
        return copyAndAdd(command, "ADD");
    }

    private Instruction parseWorkDirInstruction(String path) {
        workDirs.add(new WorkDir(path));
        return new WorkDir(path);
    }

    private Instruction parseLabelInstruction(String command) {
        String[] parts = command.split(" ");
        for (int i = 0; i < parts.length; i++) {
            String[] split = command.split("=");
            String key = split[0];
            String value = split[0];

            Pattern p = Pattern.compile("\"(.*?)\"");
            Matcher m = p.matcher(value);

            while (m.find()) {
                value = m.group(1);
            }

            labels.add(new Label(key, value));
            System.out.println("Label key: " + key + " and value: " + value);

        }
        return new Label("", "");
    }

    private Instruction parseHealthCheckInstruction(String command) {
        if (command.equals("NONE")) {
        } else {
            healthchecks.add(new Healthcheck(getClassification(command)));
        }
        return new Healthcheck(getClassification(command));
    }

    private Instruction parseCMDInstruction(String command) {
        String executable = null;
        List<String> params = new ArrayList<>();
        if (command.contains("[")) {
            Pattern p = Pattern.compile("\"(.*?)\"");
            Matcher m = p.matcher(command);

            List<String> matches = new ArrayList<String>();
            while (m.find()) {
                matches.add(m.group(1));
            }

            for (int i = 0; i < matches.size(); i++) {
                if (i == 0) {
                    executable = matches.get(i);
                } else {
                    params.add(matches.get(i));
                }
            }
        } else {
            String[] parts = command.split(" ");

            for (int i = 0; i < parts.length; i++) {
                if (i == 0) {
                    executable = parts[i];

                } else {
                    params.add(parts[i]);
                }
            }

        }
        cmds.add(new Cmd(executable, params));
        return null;
    }

    private Instruction parseStopSignalInstruction(String signal) {
        stopSignals.add(new StopSignal(signal));
        return new StopSignal(signal);


    }

    private Instruction parseArgInstruction(String arg) {
        args.add(new Arg(arg));
        return new Arg(arg);

    }

    private Instruction parseRunInstruction(String command) {
        String[] runentry = command.split(" && ");

        for (String run : runentry) {
            String executable = null;
            List<String> params = new ArrayList<>();

            if (command.contains("[")) {
                Pattern p = Pattern.compile("\"(.*?)\"");
                Matcher m = p.matcher(command);

                List<String> matches = new ArrayList<String>();
                while (m.find()) {
                    matches.add(m.group(1));
                }

                for (int i = 0; i < matches.size(); i++) {
                    if (i == 0) {
                        executable = matches.get(i);
                    } else {
                        params.add(matches.get(i));
                    }
                }
            } else {
                String[] parts = run.split(" ");

                for (int i = 0; i < parts.length; i++) {
                    if (i == 0) {
                        executable = parts[i];

                    } else {
                        params.add(parts[i]);
                    }
                }

            }
            runs.add(new Run(executable,params));

        }
        return null;
    }


    private Instruction parseVolumeInstruction(String command) {
        if (command.contains("[")) {
            System.out.print("YES IT CONTAINS [");
            Pattern p = Pattern.compile("\"(.*?)\"");
            Matcher m = p.matcher(command);

            List<String> matches = new ArrayList<String>();
            while (m.find()) {
                matches.add(m.group(1));
            }

            for (String match :
                    matches) {
                volumes.add(new Volume(match));
            }
        } else {
            System.out.println("No it doesnt contain[");
            String[] parts = command.split(" ");
            for (int i = 0; i < parts.length; i++) {
                System.out.println("No it doesnt contain[ + " + parts[i]);
                volumes.add(new Volume(parts[i]));
            }
        }

        return new Volume("test");
    }

    private Instruction parseOnBuildInstruction(String command) {
        onBuilds.add(new OnBuild(getClassification(command)));
        return new OnBuild(getClassification(command));
    }

    private Instruction parseUserInstruction(String command) {
        users.add(new User(command));
        return new User(command);


    }

    private Instruction parseShellInstruction(String command) {
        return null;
    }

    private Instruction parseMaintainerInstruction(String user) {
        maintainers.add(new Maintainer(user));
        return new Maintainer(user);


    }

    private Instruction parseEnvInstruction(String command) {
        String[] parts = command.split(" ");
        String key = parts[0];
        String value = "";
        if (parts.length > 1) {
            value = parts[1];
        }
        envs.add(new Env(key, value));
        return new Env(key, value);
    }

    private Instruction parseFromInstruction(String command) {
        From from = null;
        if (command.contains("/")) {
            from = new From(command);
        } else if (command.contains("@")) {
            String[] parts = command.split("@");
            from = new From(command, parts[0], "digest");
        } else if (command.split("\\w+").length == 0) {
            from = new From(command);
        } else if (command.matches(".*\\d+.*")) {
            Double imageVersion = Double.parseDouble(getStringFromRegexPattern("\\d+\\.?\\d+", command));
            String[] parts = command.split(":");
            String image = parts[0];
            from = new From(image, imageVersion);
        }
        froms.add(from);
        return from;
    }


    public DockerFile parseFile(File file) {
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                // process the line.
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getStringFromRegexPattern(String patternInput, String data) {
        Pattern pattern = Pattern.compile(patternInput);
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            System.out.println("getStringFromRegexPattern" + matcher.group(0));
        }
        return matcher.group(0);
    }

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

    public void assignToDockerObject() {
        dockerfile.froms = this.froms;
        dockerfile.maintainers = this.maintainers;
        dockerfile.runs = this.runs;
        dockerfile.cmds = this.cmds;
        dockerfile.labels = this.labels;
        dockerfile.envs = this.envs;
        dockerfile.exposes = this.exposes;
        dockerfile.adds = this.adds;
        dockerfile.copies = this.copies;
        dockerfile.entryPoints = this.entryPoints;
        dockerfile.volumes = this.volumes;
        dockerfile.users = this.users;
        dockerfile.workDirs = this.workDirs;
        dockerfile.args = this.args;
        dockerfile.onBuilds = this.onBuilds;
        dockerfile.stopSignals = this.stopSignals;
        dockerfile.healthchecks = this.healthchecks;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        String json = null;
        try {
            //Convert object to JSON string and save into file directly
            mapper.writeValue(new File("\\dockerfile.json"), dockerfile);

            //Convert object to JSON string
            json = mapper.writeValueAsString(dockerfile);

        } catch (JsonGenerationException e) {
            e.printStackTrace();
        } catch (JsonMappingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonParser jp = new JsonParser();
        JsonElement je = jp.parse(json);
        String prettyJsonString = gson.toJson(je);

        String header = "*************************************************** \n";

        String footer = "*************************************************** \n";
        return header + prettyJsonString + footer;
    }
}
