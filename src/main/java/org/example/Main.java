import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Main {

    private static final String MINECRAFT_SERVER_JAR = "./pufferfish-paperclip-1.19.4-R0.1-SNAPSHOT-reobf.jar";
    private static final int MINECRAFT_SERVER_PORT = 25512;
    private static final String TEMP_FILE = "/tmp/dialog_output";

    private static Process minecraftServerProcess;

    public static void main(String[] args) {
        System.out.println("Minecraft server is: Offline.");
        System.out.println("Starting the server...");
        startServer();

        waitForServer();

        updateStatus("Online");

        while (true) {
            String choice = showMenu();

            if (choice.equals("End")) {
                updateStatus("Updating...");
                // Add your code to handle the End command here
                // ...
            } else if (choice.equals("Stop")) {
                updateStatus("Updating...");
                shutdownServer();
                // Add your code to handle the Stop command here
                // ...
            } else if (choice.equals("Start")) {
                updateStatus("Updating...");
                startServer();
                // Add your code to handle the Start command here
                // ...
            } else if (choice.equals("Stopall")) {
                updateStatus("Updating...");
                shutdownServer();
                // Add your code to handle the Stopall command here
                // ...
            } else {
                // Handle dialog cancellation or exit
                break;
            }
        }

        cleanup();
    }

    private static void startServer() {
        String command = String.format("java -Xms4G -Xmx8G -jar %s", MINECRAFT_SERVER_JAR);
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(command.split(" "));
            processBuilder.directory(new File(System.getProperty("user.dir")));
            minecraftServerProcess = processBuilder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void shutdownServer() {
        if (minecraftServerProcess != null) {
            System.out.println("Shutting down Minecraft server...");
            sendCommandToServer("stop");
            try {
                minecraftServerProcess.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static void sendCommandToServer(String command) {
        if (minecraftServerProcess != null && minecraftServerProcess.isAlive()) {
            try {
                minecraftServerProcess.getOutputStream().write((command + System.lineSeparator()).getBytes());
                minecraftServerProcess.getOutputStream().flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static void waitForServer() {
        while (!isServerRunning()) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean isServerRunning() {
        try {
            Process process = Runtime.getRuntime().exec(String.format("lsof -Pi :%d -sTCP:LISTEN -t", MINECRAFT_SERVER_PORT));
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String output = reader.readLine();
            return output != null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static void updateStatus(String status) {
        LocalDateTime now = LocalDateTime.now();
        String timestamp = now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
        try {
            BufferedReader fileReader = new BufferedReader(new InputStreamReader(new File(TEMP_FILE).toURI().toURL().openStream()));
            StringBuilder content = new StringBuilder();
            String line;
            int lineCounter = 0;
            while ((line = fileReader.readLine()) != null) {
                if (lineCounter == 1) {
                    line = String.format("Server is: %s | Last Updated: %s", status, timestamp);
                }
                content.append(line).append(System.lineSeparator());
                lineCounter++;
            }
            fileReader.close();
            File tempFile = new File(TEMP_FILE);
            tempFile.delete();
            File newTempFile = new File(TEMP_FILE);
            newTempFile.createNewFile();
            newTempFile.setReadable(true);
            newTempFile.setWritable(true);
            FileUtil.writeToFile(newTempFile, content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String showMenu() {
        System.out.println("Available Options:");
        System.out.println("1. End - Stop the port listener");
        System.out.println("2. Stop - Shut down the Minecraft server");
        System.out.println("3. Start - Manually start the Minecraft server");
        System.out.println("4. Stopall - Stop both the server and the listener");

        // Add your code to display a dialog-based menu here
        // ...

        return null; // Replace with the selected choice
    }

    private static void cleanup() {
        File tempFile = new File(TEMP_FILE);
        if (tempFile.exists()) {
            tempFile.delete();
        }
    }
}
