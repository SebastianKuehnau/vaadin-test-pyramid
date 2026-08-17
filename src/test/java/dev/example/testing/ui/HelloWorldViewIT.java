package dev.example.testing.ui;

import com.vaadin.flow.component.button.testbench.ButtonElement;
import com.vaadin.flow.component.notification.testbench.NotificationElement;
import com.vaadin.flow.component.textfield.testbench.TextFieldElement;
import com.vaadin.testbench.BrowserTestBase;
import com.vaadin.testbench.ParameterizedBrowserTest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Level 4: an end-to-end test with Vaadin TestBench. A real browser drives a
 * real server, using an API that knows the Vaadin components and waits for
 * client-server round trips by itself.
 *
 * <p>
 * Needs a commercial Vaadin subscription and a local Chrome. Run it with
 * {@code ./mvnw verify -Pe2e}.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = "vaadin.launch-browser=false")
class HelloWorldViewIT extends BrowserTestBase {

    @LocalServerPort
    private int port;

    @BeforeEach
    void openApplication() {
        getDriver().get("http://localhost:" + port + "/");
    }

    @ParameterizedBrowserTest
    @CsvSource({
            "Sebastian, Hello Sebastian, success",
            "'123', Hello 123, error",
            "'hello world', Hello hello world, warning",
    })
    void greeting_usesMatchingThemeVariant(String name, String expectedText,
            String expectedTheme) {
        // TestBench has no dedicated test-ID lookup, so match the attribute.
        $(TextFieldElement.class).withAttribute("data-testid", "name-field")
                .first().setValue(name);
        $(ButtonElement.class)
                .withAttribute("data-testid", "show-notification-button")
                .first().click();

        NotificationElement notification = $(NotificationElement.class)
                .waitForFirst();
        Assertions.assertEquals(expectedText, notification.getText());
        Assertions.assertEquals(expectedTheme,
                notification.getDomAttribute("theme"));
    }
}
