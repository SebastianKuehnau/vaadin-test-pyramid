package dev.example.testing.ui;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

import dev.example.testing.logic.NameClassifier;

@Route("")
public class HelloWorldView extends HorizontalLayout {

    TextField name;
    Button sayHello;

    public HelloWorldView() {
        name = new TextField("Name");
        // Renders as data-testid, which the tests locate the component by.
        name.setTestId("name-field");

        sayHello = new Button("Show Notification");
        sayHello.setTestId("show-notification-button");
        sayHello.addClickListener(e -> {
            String value = name.getValue();
            Notification notification = Notification.show("Hello " + value);
            if (NameClassifier.isCapitalizedWord(value)) {
                notification.addThemeVariants(NotificationVariant.SUCCESS);
            } else if (NameClassifier.isNumeric(value)) {
                notification.addThemeVariants(NotificationVariant.ERROR);
            } else {
                notification.addThemeVariants(NotificationVariant.WARNING);
            }
        });

        add(name, sayHello);

        setAlignItems(Alignment.BASELINE);
        setPadding(true);
    }
}
