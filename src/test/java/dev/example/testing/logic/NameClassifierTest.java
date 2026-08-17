package dev.example.testing.logic;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Level 1: a plain unit test. No Spring context, no Vaadin session, no browser.
 * Each method is checked once per possible answer; a real suite would add the
 * edge cases here, because this is the cheapest layer to add them at.
 */
class NameClassifierTest {

    @Test
    void capitalizedWord_isRecognized() {
        Assertions.assertTrue(NameClassifier.isCapitalizedWord("Sebastian"));
    }

    @Test
    void lowercaseWord_isNotCapitalized() {
        Assertions.assertFalse(NameClassifier.isCapitalizedWord("sebastian"));
    }

    @Test
    void digitsOnly_areNumeric() {
        Assertions.assertTrue(NameClassifier.isNumeric("123"));
    }

    @Test
    void valueWithLetters_isNotNumeric() {
        Assertions.assertFalse(NameClassifier.isNumeric("12a"));
    }
}
