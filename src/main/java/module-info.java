module com.example {
    requires transitive javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires java.logging;

    opens com.example to javafx.fxml;
    exports com.example;
}
