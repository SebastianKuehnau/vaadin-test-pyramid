package dev.example.testing.it;

import dev.example.testing.ui.HelloWorldView;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/**
 * Level 2: a Spring integration test. Boots the real application context, but no
 * web server and no browser, and checks that the view can be created the way
 * Vaadin's Spring integration creates it at runtime. This starts failing the
 * moment a constructor dependency of the view cannot be satisfied.
 */
@SpringBootTest(properties = "vaadin.launch-browser=false")
class HelloWorldViewIntegrationTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("The view can be instantiated through Spring, with its "
            + "components wired up")
    void view_isCreatableThroughSpring() {
        HelloWorldView view = applicationContext.getAutowireCapableBeanFactory()
                .createBean(HelloWorldView.class);

        Assertions.assertNotNull(view, "Spring could not create the view");
        Assertions.assertEquals(2, view.getComponentCount(),
                "Expected the name field and the button to be added to the view");
    }
}
