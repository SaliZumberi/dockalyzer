package dockalyzer.tools.dockerparser;

import dockalyzer.models.DockerFile;
import dockalyzer.models.commands.*;

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
    public static List<Instruction> flatMapOfInstructions = new ArrayList<>();


    public static void printLines(File file) throws IOException {
        try (Stream<String> lines = Files.lines(file.toPath(), Charset.defaultCharset())) {
            lines.forEachOrdered(System.out::println);
        }
    }

    public static void readLines(File file) throws IOException {
        try (Stream<String> lines = Files.lines(file.toPath(), Charset.defaultCharset())) {
            lines.forEachOrdered(line -> getClassification(line));
        }
    }

    public static Instruction  getClassification(String line) {
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
                return (parseMaintanerInstruction(command));
            case "RUN":
                return(parseRunInstruction(command));
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

    private static Instruction parseExposeInstruction(String ports) {
        String[] parts = ports.split(" ");
        for (int i = 0; i < parts.length; i++) {
            return new Expose(parts[i]);
        }
        return null;
    }

    private static Instruction parseEntryPointInstruction(String command) {
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

        return null;

    }

    public static Instruction copyAndAdd(String command, String instruction){
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

        if (instruction.equals("ADD")){
            return new Add(source,destinatation);
        }else if (instruction.equals("COPY")){
            return new Copy(source,destinatation);
        }
        return null;
    }

    private static Instruction parseCopyInstruction(String command) {
        return copyAndAdd(command, "COPY");

    }

    private static Instruction parseAddInstruction(String command) {
        return copyAndAdd(command, "ADD");



    }

    private static Instruction parseWorkDirInstruction(String path) {
        return new WorkDir(path);


    }

    private static Instruction parseLabelInstruction(String command) {
        List<Label> labels =new ArrayList<>();
        String[] parts = command.split(" ");
        for (int i = 0; i < parts.length; i++) {
            String[] split = command.split("=");
            String key =split[0];
            String value =split[0];

            Pattern p = Pattern.compile("\"(.*?)\"");
            Matcher m = p.matcher(value);

            while (m.find()) {
                value = m.group(1);
            }

            labels.add(new Label(split[0],split[1]));
            System.out.println("Label key: " + key + " and value: " +value);
            new Volume(parts[i]);
        }
        return null;
    }

    private static Instruction parseHealthCheckInstruction(String command) {
        return null;
    }

    private static Instruction parseCMDInstruction(String command) {
        return null;
    }

    private static Instruction parseStopSignalInstruction(String command) {
        return null;
    }

    private static Instruction parseArgInstruction(String arg) {
        return new Arg(arg);


    }

    private static Instruction parseRunInstruction(String command) {
        return null;
    }

    private static Instruction parseVolumeInstruction(String command) {
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
                new Volume(match);
            }
        } else {
            System.out.println("No it doesnt contain[");
            String[] parts = command.split(" ");
            for (int i = 0; i < parts.length; i++) {
                System.out.println("No it doesnt contain[ + " + parts[i]);
                new Volume(parts[i]);
            }
        }

        return null;
    }

    private static Instruction parseOnBuildInstruction(String command) {
        return new OnBuild(getClassification(command));
    }

    private static Instruction parseUserInstruction(String command) {
        return new User(command);


    }

    private static Instruction parseShellInstruction(String command) {
        return null;
    }

    private static Instruction parseMaintanerInstruction(String user) {
        return new Maintainer(user);


    }

    private static Instruction parseEnvInstruction(String command) {
        String[] parts = command.split(" ");
        String key = parts[0];
        String value = "";
        if (parts.length > 1) {
            value = parts[1];
        }
        return new Env(key, value);
    }

    private static Instruction parseFromInstruction(String command) {
        if (command.contains("/")) {
            return new From(command);
        } else if (command.contains("@")) {
            String[] parts = command.split("@");
            return new From(command, parts[0], "digest");

        } else if (command.split("\\w+").length == 0) {
            return new From(command);

        } else if (command.matches(".*\\d+.*")) {
            Double imageVersion = Double.parseDouble(getStringFromRegexPattern("\\d+\\.?\\d+", command));
            return new From(command, imageVersion);
        }
        return null;
    }


    public static DockerFile parseFile(File file) {
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

    public static String getStringFromRegexPattern(String patternInput, String data) {
        Pattern pattern = Pattern.compile(patternInput);
        Matcher matcher = pattern.matcher(data);
        if (matcher.find()) {
            System.out.println("getStringFromRegexPattern" + matcher.group(0));
        }
        return matcher.group(0);
    }

    public static File findFile(String name, File path) {
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
