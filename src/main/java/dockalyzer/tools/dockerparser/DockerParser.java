package dockalyzer.tools.dockerparser;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import dockalyzer.models.DockerFile;
import dockalyzer.models.DockerfileSnapshot;
import dockalyzer.models.SQL.Snapshot;
import dockalyzer.models.commands.*;
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
    Snapshot dockerfile = new Snapshot();
    public String localDockerfilePath;
    public String localPath;

    public From from;
    public Maintainer maintainer;
    public Cmd cmd;
    public EntryPoint entryPoint;
    public StopSignal stopSignal;
    public Healthcheck healthcheck;

    public List<Run> runs = new ArrayList<>();
    public List<Label> labels = new ArrayList<>();
    public List<Env> envs = new ArrayList<>();
    public List<Expose> exposes = new ArrayList<>();
    public List<Add> adds = new ArrayList<>();
    public List<Copy> copies = new ArrayList<>();
    public List<Volume> volumes = new ArrayList<>();
    public List<User> users = new ArrayList<>();
    public List<WorkDir> workDirs = new ArrayList<>();
    public List<Arg> args = new ArrayList<>();
    public List<OnBuild> onBuilds = new ArrayList<>();

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

    public DockerParser(String localpath) {
        this.localDockerfilePath = "";
        this.localPath = localpath;
    }

    public Snapshot getDockerfileObject(String fileName) throws IOException {
        dockerfile = new Snapshot();
        File flatDockerfile = getFlatDockerFile(new File(fileName));
        doClassificationOfLines(flatDockerfile);
        assignToDockerObject();
        return dockerfile;
    }

    public Snapshot getDockerfileObject() throws IOException {
        dockerfile = new Snapshot();
        File flatDockerfile = getFlatDockerFile(new File(this.localPath + "/" + this.localDockerfilePath + "/" + "Dockerfile"));
        doClassificationOfLines(flatDockerfile);
        assignToDockerObject();
        return dockerfile;
    }

    public void printLines(File file) throws IOException {
        try (Stream<String> lines = Files.lines(file.toPath(), Charset.defaultCharset())) {
            lines.forEachOrdered(System.out::println);
        }
    }

    public List<Comment> getCommentsFromDockerfile(File flatDockerFile) throws IOException {
        List<Comment> comments = new ArrayList<>();
        FileInputStream fis = new FileInputStream(flatDockerFile);
        BufferedReader reader = new BufferedReader(new InputStreamReader(fis));
        String line = null;
        String instruction = null;
        String newLine = null;

        String comment = null;
        boolean commentFlag = false;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("#") && !commentFlag) {
                newLine += line;
            }else if (line.startsWith("#") && commentFlag) {
                newLine += line;
            }else if (doesLineHaveAnInstruction(line) && commentFlag) {
                if(line.contains("\t")){
                    String uname = " ";
                    line = line.replaceAll("\t", uname);
                }

                String arr[] = line.split(" ", 2);

                String foundInstruction = arr[0];   //Instruction
                String command;
                if (arr.length > 1) {
                    command = arr[1];
                } else {
                    command = "";
                }
                newLine = "";
                newLine += line;

                Comment c = new Comment(null, foundInstruction,comment);
                comments.add(c);
            } else if (line.contains(" \\") && concatFlag) {
                newLine += line;
            } else if (!doesLineHaveAnInstruction(line) && !line.contains(" \\") && concatFlag) {
                newLine += line;
                newLine = newLine.replace(" \\", "");
                newLine = newLine.trim().replaceAll(" +", " ");
                concatFlag = false;
            } else if (doesLineHaveAnInstruction(line) && !line.contains(" \\")) {
                line = line.trim().replaceAll(" +", " ");
            }
        }
        reader.close();

        return null;
    }
    public File getFlatDockerFile(File dockerFile) throws IOException {
        File flatDockerfile = new File(dockerFile.getParentFile().getPath() + "\\DockerFileFlat");
        if(!flatDockerfile.exists()){
            flatDockerfile = this.findFile("Dockerfile",new File(this.localPath + "/" + this.localDockerfilePath) );
        }
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

    public File getFlatDockerFile(String fileName) throws IOException {
        String localPathtoDockerfile = localPath.concat("/").concat("/").concat(localDockerfilePath);
        File dockerfile = findFile(fileName, new File(localPathtoDockerfile));
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

        } else if (line.startsWith("#")) {
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
    }

    public Instruction getClassification(String newLine) {
        String line = newLine;
        if (line.startsWith("#")) {
            return null;
        }

        if(line.contains("\t")){
            String uname = " ";
            line = line.replaceAll("\t", uname);
        }

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
            exposes.add(new Expose(dockerfile,parts[i]));
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
        if(executable.length()>0){
            entryPoint = new EntryPoint(dockerfile, executable, params);
        }
        return null;

    }

    public Instruction copyAndAdd(String command, String instruction) {
        String source = null;
        String destinatation = null;
        if (command.contains("[")) {
            Pattern p = Pattern.compile("\"(.*?)\"");
            Matcher m = p.matcher(command);

            List<String> matches = new ArrayList<String>();
            while (m.find()) {
                matches.add(m.group(1));
            }
            for (int i = 0; i < matches.size(); i++) {
                if (i == 0) {
                    source = matches.get(i);
                } else {
                    source = matches.get(i);
                }
            }
        } else {
            String[] parts = command.split(" ");
            for (int i = 0; i < parts.length; i++) {
                if (i == 0) {
                    source = parts[i];

                } else {
                    destinatation = parts[i];
                }
            }
        }

        if (instruction.equals("ADD")) {
            adds.add(new Add(dockerfile, source, destinatation));
        } else if (instruction.equals("COPY")) {
            copies.add(new Copy(dockerfile, source, destinatation));
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
        workDirs.add(new WorkDir(dockerfile, path));
        return new WorkDir(dockerfile, path);
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

            labels.add(new Label(dockerfile, key, value));

        }
        return new Label(dockerfile, "", "");
    }

    private Instruction parseHealthCheckInstruction(String command) {
        if (command.equals("NONE")) {
        } else {
            healthcheck = new Healthcheck(dockerfile, getClassification(command));
        }
        return new Healthcheck(dockerfile, getClassification(command));
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
        // cmds.add(new Cmd(dockerfile,executable, params));
        if(executable.length()>0){
            cmd = new Cmd(dockerfile, executable, params);
        }
        return null;
    }

    private Instruction parseStopSignalInstruction(String signal) {
        //stopSignals.add(new StopSignal(dockerfile,signal));
        stopSignal = new StopSignal(dockerfile, signal);

        return new StopSignal(dockerfile, signal);


    }

    private Instruction parseArgInstruction(String arg) {
        args.add(new Arg(arg));
        return new Arg(arg);

    }

    private Instruction parseRunInstruction(String commandx) {
        String command = commandx;
        if(commandx.contains("(")){
            Matcher m = Pattern.compile("\\(([^)]+)\\)").matcher(commandx);
            while(m.find()) {
                command  =m.group(1);
            }
        }

        String[] runentry = command.split(" && ");

        for (String run : runentry) {
            String executable = "";
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
            }else {
                String[] parts = run.split(" ");

                for (int i = 0; i < parts.length; i++) {
                    if (i == 0) {
                        executable = parts[i];

                    } else {
                        params.add(parts[i]);
                    }
                }

            }
            if(executable.length()>0){
                runs.add(new Run(dockerfile, executable, params));
            }
        }
        return null;
    }


    private Instruction parseVolumeInstruction(String command) {
        if (command.contains("[")) {
            Pattern p = Pattern.compile("\"(.*?)\"");
            Matcher m = p.matcher(command);

            List<String> matches = new ArrayList<String>();
            while (m.find()) {
                matches.add(m.group(1));
            }

            for (String match :
                    matches) {
                volumes.add(new Volume(dockerfile, match));
            }
        } else {
            String[] parts = command.split(" ");
            for (int i = 0; i < parts.length; i++) {
                volumes.add(new Volume(dockerfile, parts[i]));
            }
        }
        return new Volume(dockerfile, "test");
    }

    private Instruction parseOnBuildInstruction(String command) {
        onBuilds.add(new OnBuild(dockerfile, getClassification(command)));
        return new OnBuild(dockerfile, getClassification(command));
    }

    private Instruction parseUserInstruction(String command) {
        users.add(new User(dockerfile, command));
        return new User(dockerfile, command);


    }

    private Instruction parseShellInstruction(String command) {
        return null;
    }

    private Instruction parseMaintainerInstruction(String user) {
        maintainer = new Maintainer(dockerfile, user);
        return new Maintainer(dockerfile, user);


    }

    private Env parseEnvInstruction(String command) {
        String[] parts = command.split(" ");
        String key = parts[0];
        String value = "";
        if (parts.length > 1) {
            value = parts[1];
        }
        envs.add(new Env(dockerfile, key, value));
        return new Env(dockerfile, key, value);
    }

    private Instruction parseFromInstruction(String command) {
        if (command.contains("/")) {
            from = new From(dockerfile,command);
            return from;
        } else if (command.contains("@")) {
            String[] parts = command.split("@");
            String image = parts[0];
            String imageVersion = parts[1];
            from = new From(dockerfile,image, imageVersion, "digest");
            return from;
        } else if (command.split("\\w+").length == 0) {
            from = new From(dockerfile,command);
            return from;
        } else if (command.matches(".*\\d+.*")) {
            Double imageVersion = Double.parseDouble(getStringFromRegexPattern("\\d+\\.?\\d+", command));
            String[] parts = command.split(":");
            String image = parts[0];
            from = new From(dockerfile,image, imageVersion);
            return from;
        } else if (command.matches("\\w*(:)\\w+")){
            String[] parts = command.split(":");
            String image = parts[0];
            String imageVersion = parts[1];
            from = new From(dockerfile,image, imageVersion, "imageVersion");
            return from;
        }
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
       try {
           if (matcher.find()) {
             //  System.out.println("getStringFromRegexPattern" + matcher.group(0));
           }
           return matcher.group(0);
       }catch (Exception e){
           e.printStackTrace();
           return "";
       }
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
        dockerfile.from = from;
        dockerfile.maintainer = maintainer;
        dockerfile.cmd = cmd;
        dockerfile.entryPoint = entryPoint;
        dockerfile.stopSignals = stopSignal;
        dockerfile.healthCheck = healthcheck;


        dockerfile.runs = this.runs;
        dockerfile.labels = this.labels;
        dockerfile.envs = this.envs;
        dockerfile.exposes = this.exposes;
        dockerfile.adds = this.adds;
        dockerfile.copies = this.copies;
        dockerfile.volumes = this.volumes;
        dockerfile.users = this.users;
        dockerfile.workDirs = this.workDirs;
        dockerfile.args = this.args;
        dockerfile.onBuilds = this.onBuilds;
    }

   /* @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setVisibility(JsonMethod.FIELD, JsonAutoDetect.Visibility.ANY);
        String json = null;
        try {
            //Convert object to JSON string and save into file directly
            mapper.writeValue(new File(localPath + "/" + "dockerfile.json"), dockerfile);

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

    public void printDockerfile(DockerfileSnapshot dockerfileSnapshot) {
        System.out.println(this.toString());

    }*/
}
