import java.util.Scanner;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);
        File currentDirectory = new File(System.getProperty("user.dir"));
        while (true) {

            System.out.print("$ ");

            String command = scanner.nextLine();

            if (command.equals("exit")) {

                break;

            } else if (command.startsWith("echo ")) {

                System.out.println(command.substring(5));

            } else if (command.equals("pwd")) {
                System.out.println(currentDirectory.getCanonicalPath());
            } else if (command.startsWith("type ")) {

                String cmd = command.substring(5);

                if (cmd.equals("echo") ||
                        cmd.equals("exit") ||
                        cmd.equals("type") ||
                        cmd.equals("pwd") ||
                        cmd.equals("cd")) {

                    System.out.println(cmd + " is a shell builtin");

                } else {

                    String path = System.getenv("PATH");
                    String[] directories = path.split(File.pathSeparator);

                    boolean found = false;

                    for (String directory : directories) {

                        File file = new File(directory, cmd);

                        if (file.exists() &&
                                file.isFile() &&
                                file.canExecute()) {

                            System.out.println(
                                    cmd + " is " + file.getAbsolutePath());

                            found = true;
                            break;
                        }
                    }

                    if (!found) {
                        System.out.println(cmd + ": not found");
                    }
                }

            } else if (command.startsWith("cd ")) {

                String path = command.substring(3);

                File directory = new File(path);

                if (!directory.isAbsolute()) {
                    directory = new File(currentDirectory, path);
                }

                if (directory.isDirectory() && directory.exists()) {

                    currentDirectory = directory.getCanonicalFile();

                }
            } else if (command.startsWith("cd ")) {

                String path = command.substring(3).trim();

                if (path.equals("~"))
                    path = System.getenv("HOME");

                File target = new File(path).isAbsolute()
                        ? new File(path)
                        : new File(currentDirectory, path);

                target = target.getCanonicalFile();

                if (target.exists() && target.isDirectory()) {
                    currentDirectory = target;
                } else {
                    System.out.println("cd " + path + ": No such file or directory");
                }
            }

            else {

                String[] arguments = command.split(" ");

                String program = arguments[0];

                String path = System.getenv("PATH");
                String[] directories = path.split(File.pathSeparator);

                File executable = null;

                for (String directory : directories) {

                    File file = new File(directory, program);

                    if (file.exists() &&
                            file.isFile() &&
                            file.canExecute()) {

                        executable = file;
                        break;
                    }
                }

                if (executable != null) {

                    ProcessBuilder processBuilder = new ProcessBuilder(arguments);

                    processBuilder.inheritIO();

                    Process process = processBuilder.start();

                    process.waitFor();

                } else {

                    System.out.println(command + ": not found");

                }
            }
        }

        scanner.close();
    }
}