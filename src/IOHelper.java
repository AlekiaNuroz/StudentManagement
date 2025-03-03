import java.io.IOException;
import java.util.Scanner;

/**
 * A utility class that provides helper methods for handling user input and displaying menus.
 */
public class IOHelper {
    
    /**
     * Gets a string input from the user.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed to the user.
     * @param allowEmpty Whether empty input is allowed.
     * @return The user input string.
     */
    public static String getStringInput(Scanner scanner, String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!allowEmpty && input.isEmpty()) {
                System.out.println("This field cannot be empty!");
                continue;
            }
            return input;
        }
    }

    /**
     * Gets an integer input from the user within a specified range.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed to the user.
     * @param min The minimum valid value.
     * @param max The maximum valid value.
     * @return The user input integer.
     */
    public static int getIntInput(Scanner scanner, String prompt, int min, int max) {
        return getIntInput(scanner, prompt, min, max, false, min);
    }

    /**
     * Gets an integer input from the user within a specified range, with an optional default value.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed to the user.
     * @param min The minimum valid value.
     * @param max The maximum valid value.
     * @param allowEmpty Whether empty input is allowed.
     * @param defaultValue The default value if empty input is allowed.
     * @return The user input integer.
     */
    public static int getIntInput(Scanner scanner, String prompt, int min, int max, boolean allowEmpty, int defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (allowEmpty && input.isEmpty()) return defaultValue;

            try {
                int value = Integer.parseInt(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Please enter a value between %d and %d\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid integer.");
            }
        }
    }

    /**
     * Gets a double input from the user within a specified range.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed to the user.
     * @param min The minimum valid value.
     * @param max The maximum valid value.
     * @return The user input double.
     */
    public static double getDoubleInput(Scanner scanner, String prompt, double min, double max) {
        return getDoubleInput(scanner, prompt, min, max, false, min);
    }

    /**
     * Gets a double input from the user within a specified range, with an optional default value.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed to the user.
     * @param min The minimum valid value.
     * @param max The maximum valid value.
     * @param allowEmpty Whether empty input is allowed.
     * @param defaultValue The default value if empty input is allowed.
     * @return The user input double.
     */
    public static double getDoubleInput(Scanner scanner, String prompt, double min, double max, boolean allowEmpty, double defaultValue) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (allowEmpty && input.isEmpty()) return defaultValue;

            try {
                double value = Double.parseDouble(input);
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.printf("Please enter a value between %.1f and %.1f\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
        }
    }

    /**
     * Clears the console screen. Supports Windows, macOS, and Linux.
     */
    public static void clearScreen() {
        String os = System.getProperty("os.name").toLowerCase();

        try {
            if (os.contains("win")) {
                // Windows
                new ProcessBuilder("cmd", "/c", "cls").inheritIO().start().waitFor();
            } else if (os.contains("mac") || os.contains("nix") || os.contains("nux")) {
                // macOS or Linux/Unix
                new ProcessBuilder("clear").inheritIO().start().waitFor();
            } else {
                System.out.println("Unsupported operating system.");
            }
        } catch (InterruptedException e) {
            System.out.println("Interrupted while clearing screen.");
        } catch (IOException e) {
            System.err.println("System Error: " + e.getMessage());
        }
    }

    /**
     * Pauses the execution for a given number of seconds.
     *
     * @param seconds The number of seconds to wait.
     */
    public static void wait(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Displays a menu with a given title and a list of options.
     *
     * @param title The menu title.
     * @param options The menu options.
     */
    public static void printMenu(String title, String[] options) {
        System.out.println(title + "\n");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.println((options.length + 1) + ". Exit\n");
    }
}
