import java.util.Scanner;
import java.util.function.Function;
import java.io.Console;
import java.util.regex.Pattern;

/**
 * A utility class that provides helper methods for handling user input and displaying menus.
 */
@SuppressWarnings("unused")
public final class IOHelper {
    private IOHelper() {} // Prevent instantiation

    /**
     * Gets a string input from the user.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed to the user.
     * @param allowEmpty Whether empty input is allowed.
     * @return The user input string.
     */
    public static String getStringInput(Scanner scanner, String prompt, boolean allowEmpty) {
        System.out.print(prompt);
        String input = scanner.nextLine().trim();

        if (!allowEmpty && input.isEmpty()) {
            System.out.println("This field cannot be empty!");
            return getStringInput(scanner, prompt, false);
        }
        return input;
    }

    private static <T extends Number> T getNumericInput(Scanner scanner, String prompt, T min, T max, boolean allowEmpty, T defaultValue, Function<String, T> parser) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (allowEmpty && input.isEmpty()) return defaultValue;

            try {
                T value = parser.apply(input);
                if (value.doubleValue() >= min.doubleValue() && value.doubleValue() <= max.doubleValue()) {
                    return value;
                }
                System.out.printf("Please enter a value between %s and %s\n", min, max);
            } catch (NumberFormatException e) {
                System.out.println("Invalid input. Please enter a valid number.");
            }
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
        return getNumericInput(scanner, prompt, min, max, allowEmpty, defaultValue, Integer::parseInt);
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
        return getNumericInput(scanner, prompt, min, max, allowEmpty, defaultValue, Double::parseDouble);
    }

    /**
     * Prompts the user for a Yes/No confirmation.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The confirmation message displayed to the user.
     * @return true if the user enters 'Y' or 'y', false if 'N' or 'n'.
     */
    public static boolean getYesNoInput(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (Y/N): ");
            String input = scanner.nextLine().trim().toLowerCase();
            if (input.equals("y")) return true;
            if (input.equals("n")) return false;
            System.out.println("Invalid input. Please enter 'Y' for Yes or 'N' for No.");
        }
    }

    /**
     * Reads multi-line input from the user until they enter a specific stop keyword.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed before collecting input.
     * @param stopWord The keyword that signals the end of input.
     * @return The concatenated multi-line input.
     */
    public static String getMultiLineInput(Scanner scanner, String prompt, String stopWord) {
        System.out.println(prompt + " (Type '" + stopWord + "' on a new line to finish):");
        StringBuilder input = new StringBuilder();
        while (true) {
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase(stopWord)) break;
            input.append(line).append("\n");
        }
        return input.toString().trim();
    }

    /**
     * Reads a password securely from the console (without echoing input).
     *
     * @param prompt The message displayed before collecting input.
     * @return The entered password as a String.
     */
    public static String getPasswordInput(Scanner scanner, String prompt) {
        Console console = System.console();
        if (console == null) {
            System.out.print(prompt);
            return scanner.nextLine();
        }
        char[] passwordChars = console.readPassword(prompt);
        return new String(passwordChars);
    }

    /**
     * Prompts the user for an email address and validates its format.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed to the user.
     * @return A valid email address.
     */
    public static String getEmailInput(Scanner scanner, String prompt) {
        Pattern emailPattern = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
        while (true) {
            System.out.print(prompt);
            String email = scanner.nextLine().trim();
            if (emailPattern.matcher(email).matches()) return email;
            System.out.println("Invalid email format. Please enter a valid email.");
        }
    }

    /**
     * Prompts the user to select an option from a list.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed to the user.
     * @param options The list of selectable options.
     * @return The index of the selected option (0-based).
     */
    public static int getOptionInput(Scanner scanner, String prompt, String[] options) {
        while (true) {
            System.out.println(prompt);
            for (int i = 0; i < options.length; i++) {
                System.out.println((i + 1) + ". " + options[i]);
            }
            int choice = getIntInput(scanner, "Enter your choice: ", 1, options.length - 1);

            try {
                if (choice >= 1 && choice <= options.length) return choice - 1;
            } catch (NumberFormatException ignored) {}

            System.out.println("Invalid selection. Please choose a number between 1 and " + options.length);
        }
    }

    /**
     * Prompts the user for a 10-digit phone number, validates it, and formats it as (###) ###-####.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed to the user.
     * @return The formatted phone number as a string.
     */
    public static String getPhoneNumber(Scanner scanner, String prompt) {
        Pattern phonePattern = Pattern.compile("^\\d{10}$"); // Ensures exactly 10 digits

        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().replaceAll("[^0-9]", ""); // Remove non-numeric characters

            if (phonePattern.matcher(input).matches()) {
                return String.format("(%s) %s-%s",
                        input.substring(0, 3),
                        input.substring(3, 6),
                        input.substring(6));
            }

            System.out.println("Invalid phone number. Please enter a 10-digit number.");
        }
    }

    /**
     * Prompts the user for a valid US ZIP code or Canadian postal code, validates it, and formats it properly.
     *
     * @param scanner The scanner object for user input.
     * @param prompt The message displayed to the user.
     * @return The formatted ZIP or postal code.
     */
    public static String getPostalCode(Scanner scanner, String prompt) {
        Pattern usZipPattern = Pattern.compile("^\\d{5}(-\\d{4})?$"); // 5-digit ZIP or ZIP+4
        Pattern caPostalPattern = Pattern.compile("^[A-Za-z]\\d[A-Za-z][ -]?\\d[A-Za-z]\\d$"); // Canadian postal code

        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim().toUpperCase().replaceAll("\\s+", ""); // Normalize input

            if (usZipPattern.matcher(input).matches()) {
                return input; // US ZIP codes are already correctly formatted
            }

            if (caPostalPattern.matcher(input).matches()) {
                return input.substring(0, 3) + " " + input.substring(3); // Format as A1A 1A1
            }

            System.out.println("Invalid postal code. Please enter a valid US ZIP code (##### or #####-####) or Canadian postal code (A1A 1A1).");
        }
    }

    /**
     * Clears the console screen. Supports Windows, macOS, and Linux.
     */
    public static void clearScreen() {
        String os = System.getProperty("os.name").toLowerCase();
        if (os.contains("win")) {
            System.out.print("\033[H\033[2J"); // ANSI escape code for Windows terminals
        } else {
            System.out.print("\033[H\033[2J");
            System.out.flush();
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
            System.out.println("Thread was interrupted while waiting.");
        }
    }

    /**
     * Displays a menu with a given title and a list of options.
     *
     * @param title The menu title.
     * @param options The menu options.
     */
    public static void printMenu(String title, String[] options, boolean clearScreen, String exitText) {
        if (clearScreen) clearScreen();
        System.out.println("\n==== " + title + " ====\n");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.println((options.length + 1) + ". " + exitText + "\n");
    }
}