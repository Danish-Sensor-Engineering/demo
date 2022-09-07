module dse.explorer {
    requires static java.annotation;

    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;

    requires org.slf4j;
    requires libsensor;
    requires com.fazecast.jSerialComm;

    opens dse.explorer to javafx.fxml;
    exports dse.explorer;
}