module dse.explorer {
    requires static java.annotation;

    requires transitive javafx.graphics;
    requires javafx.controls;
    requires javafx.fxml;
    requires atlantafx.base;

    requires com.fazecast.jSerialComm;
    requires dse.libods;
    requires org.slf4j;
    requires io.fair_acc.chartfx;
    requires io.fair_acc.dataset;
    requires org.jetbrains.annotations;

    requires org.kordamp.ikonli.core;
    requires org.kordamp.ikonli.javafx;

    opens dse.explorer to javafx.fxml;
    exports dse.explorer;
}