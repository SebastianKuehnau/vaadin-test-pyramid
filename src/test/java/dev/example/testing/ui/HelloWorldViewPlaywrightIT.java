package dev.example.testing.ui;

import static com.microsoft.playwright.assertions.PlaywrightAssertions.assertThat;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.assertions.LocatorAssertions;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

/**
 * Level 5: an end-to-end test with Playwright. Same scenario as the TestBench
 * test, but with a generic library that knows nothing about Vaadin, so it talks
 * to the web components directly.
 *
 * <p>
 * Playwright downloads a browser on first run. Run it with
 * {@code ./mvnw verify -Pe2e}.
 */
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT,
        properties = "vaadin.launch-browser=false")
@Tag("playwright")
class HelloWorldViewPlaywrightIT {

    private static final double TIMEOUT_MS = 3_000;

    private static Playwright playwright;
    private static Browser browser;

    @LocalServerPort
    private int port;

    private Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch();
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void openApplication() {
        page = browser.newPage();
        page.navigate("http://localhost:" + port + "/");
    }

    @AfterEach
    void closePage() {
        page.close();
    }

    @ParameterizedTest(name = "\"{0}\" -> {2}")
    @CsvSource({
            "Sebastian, Hello Sebastian, success",
            "'123', Hello 123, error",
            "'hello world', Hello hello world, warning",
    })
    void greeting_usesMatchingThemeVariant(String name, String expectedText,
            String expectedTheme) {
        // A Vaadin TextField renders a slotted <input> in its light DOM. The
        // Tab key commits the value: the field only sends it to the server on
        // "change", and fill() alone fires "input".
        Locator input = page.locator("[data-testid='name-field'] input");
        input.fill(name);
        input.press("Tab");

        page.locator("[data-testid='show-notification-button']").click();

        // These assertions retry until they hold, which is what makes them safe
        // against the asynchronous client-server round trip.
        Locator card = page.locator("vaadin-notification-card");
        assertThat(card).hasText(expectedText,
                new LocatorAssertions.HasTextOptions().setTimeout(TIMEOUT_MS));
        assertThat(card).hasAttribute("theme", expectedTheme,
                new LocatorAssertions.HasAttributeOptions()
                        .setTimeout(TIMEOUT_MS));
    }
}
