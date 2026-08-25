import java.io.File;
import java.util.*;

public class Main {

    static final String[] BUILTINS = { "echo", "exit", "type", "pwd", "cd" };

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        File currentDirectory = new File(System.getProperty("user.dir"));

        while (true) {

            System.out.print("$ ");
            System.out.flush();

            if (!scanner.hasNextLine()) {
                break;
            }

            String command = scanner.nextLine().trim();

            if (command.isEmpty()) {
                continue;
            }

            if (command.equals("exit") || command.startsWith("exit ")) {
                break;

            } else if (command.equals("echo") || command.startsWith("echo ")) {
                String[] arguments = parseCommand(command);

                for (int i = 1; i < arguments.length; i++) {
                    if (i > 1) {
                        System.out.print(" ");
                    }
                    System.out.print(arguments[i]);

                }
                System.out.println();
            } else if (command.equals("pwd")) {
                System.out.println(currentDirectory.getCanonicalPath());

            } else if (command.equals("type") || command.startsWith("type ")) {
                handleType(command);

            } else if (command.equals("cd") || command.startsWith("cd ")) {
                currentDirectory = handleCd(command, currentDirectory);

            } else {
                runExternal(command);
            }
        }

        scanner.close();
    }

    static boolean isBuiltin(String cmd) {
        for (String b : BUILTINS) {
            if (b.equals(cmd)) {
                return true;
            }
        }
        return false;
    }

    static File findExecutable(String program) {
        String path = System.getenv("PATH");
        if (path == null) {
            return null;
        }
        for (String directory : path.split(File.pathSeparator)) {
            File file = new File(directory, program);
            if (file.isFile() && file.canExecute()) {
                return file;
            }
        }
        return null;
    }

    static void handleType(String command) {
        String cmd = command.equals("type") ? "" : command.substring(5).trim();

        if (cmd.isEmpty()) {
            return;
        }

        if (isBuiltin(cmd)) {
            System.out.println(cmd + " is a shell builtin");
            return;
        }

        File executable = findExecutable(cmd);
        if (executable != null) {
            System.out.println(cmd + " is " + executable.getAbsolutePath());
        } else {
            System.out.println(cmd + ": not found");
        }
    }

    static File handleCd(String command, File currentDirectory) throws Exception {
        String path = command.equals("cd") ? "~" : command.substring(3).trim();

        if (path.isEmpty() || path.equals("~")) {
            path = System.getenv("HOME");
        } else if (path.startsWith("~/")) {
            path = System.getenv("HOME") + path.substring(1);
        }

        File directory = new File(path);
        if (!directory.isAbsolute()) {
            directory = new File(currentDirectory, path);
        }

        directory = directory.getCanonicalFile();

        if (directory.isDirectory()) {
            return directory;
        } else {
            System.out.println("cd: " + path + ": No such file or directory");
            return currentDirectory;
        }
    }

    static void runExternal(String command) throws Exception {
        String[] arguments = parseCommand(command);
        String program = arguments[0];

        File executable = findExecutable(program);

        if (executable != null) {
            ProcessBuilder processBuilder = new ProcessBuilder(arguments);
            processBuilder.inheritIO();
            Process process = processBuilder.start();
            process.waitFor();
        } else {
            System.out.println(program + ": command not found");
        }
    }

    static String[] parseCommand(String command) {
        List<String> arguments = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;

        for (char c : command.toCharArray()) {
            if (c == '\'') {
                inSingleQuote = !inSingleQuote;
            } else if (Character.isWhitespace(c) && !inSingleQuote) {

                if (current.length() > 0) {
                    arguments.add(current.toString());
                    current.setLength(0);
                }

            } else {
                current.append(c);
            }
        }
        if (current.length() > 0) {
            arguments.add(current.toString());
        }

        return arguments.toArray(new String[0]);
    }
}