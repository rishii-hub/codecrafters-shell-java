import java.util.Scanner;
import java.io.File;

public class Main {

    public static void main(String[] args) throws Exception {

        Scanner scanner = new Scanner(System.in);

        while (true) {

            System.out.print("$ ");

            String command = scanner.nextLine();

            if (command.equals("exit")) {

                break;

            } else if (command.startsWith("echo ")) {

                System.out.println(command.substring(5));

            } else if (command.equals("pwd")) {
                System.out.println(System.getProperty("user.dir"));
            } else if (command.startsWith("type ")) {

                String cmd = command.substring(5);

                if (cmd.equals("echo") ||
                        cmd.equals("exit") ||
                        cmd.equals("type") ||
                        cmd.equals("pwd")) {

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

            } else {

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