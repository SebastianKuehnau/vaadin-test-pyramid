package dev.example.testing.ui;

import com.vaadin.browserless.SpringBrowserlessTest;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.textfield.TextField;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Level 3: a UI unit test using Vaadin's browserless testing. Drives the real
 * server-side components without a browser or servlet container, so it is fast
 * enough to cover every branch of the click listener.
 */
@SpringBootTest(properties = "vaadin.launch-browser=false")
class HelloWorldViewBrowserlessTest extends SpringBrowserlessTest {

    @ParameterizedTest(name = "\"{0}\" -> {2}")
    @CsvSource({
            "Sebastian, Hello Sebastian, success",
            "'123', Hello 123, error",
            "'hello world', Hello hello world, warning",
    })
    void greeting_usesMatchingThemeVariant(String name, String expectedText,
            String expectedTheme) {
        navigate(HelloWorldView.class);

        // test(...) simulates the user action and asserts on the way that the
        // component is visible, enabled and attached.
        test(find(TextField.class).testId("name-field")).setValue(name);
        test(find(Button.class).testId("show-notification-button")).click();

        Notification notification = find(Notification.class).single();
        Assertions.assertTrue(test(notification).isUsable());
        Assertions.assertEquals(expectedText, test(notification).getText());
        Assertions.assertTrue(
                notification.getThemeNames().contains(expectedTheme),
                () -> "Expected theme '" + expectedTheme + "' but found "
                        + notification.getThemeNames());
    }
}
