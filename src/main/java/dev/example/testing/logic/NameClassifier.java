package dev.example.testing.logic;

/**
 * Classifies the name a user typed. Pure logic, free of any Vaadin or Spring
 * dependency, and therefore the cheapest layer of the test pyramid to cover.
 */
public final class NameClassifier {

    private NameClassifier() {
    }

    /**
     * Returns {@code true} if the value consists only of letters and starts with
     * an upper-case one. Letters of any script count.
     */
    public static boolean isCapitalizedWord(String value) {
        return !value.isEmpty()
                && value.chars().allMatch(Character::isLetter)
                && Character.isUpperCase(value.charAt(0));
    }

    /**
     * Returns {@code true} if the value consists only of digits. Stricter than
     * parsing a number: no signs, separators or decimal points.
     */
    public static boolean isNumeric(String value) {
        return !value.isEmpty() && value.chars().allMatch(Character::isDigit);
    }
}
