import java.util.Scanner;

public class IOHelper {
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

    public static int getIntInput(Scanner scanner, String prompt, int min, int max) {
        return getIntInput(scanner, prompt, min, max, false, min);
    }

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

    public static double getDoubleInput(Scanner scanner, String prompt, double min, double max) {
        return getDoubleInput(scanner, prompt, min, max, false, min);
    }

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

    public static void clearScreen() {
        for (int i = 0; i < 50; i++) System.out.println();
    }

    public static void wait(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void printMenu(String title, String[] options) {
        System.out.println(title + "\n");
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.println((options.length + 1) + ". Exit\n");
    }
}