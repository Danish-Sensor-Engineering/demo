module dse.explorer {
    requires static java.annotation;

    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;

    requires com.fazecast.jSerialComm;
    requires dse.libods;
    requires org.slf4j;

    opens dse.explorer to javafx.fxml;
    exports dse.explorer;
}