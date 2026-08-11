module LoopsFirstYearProject.LoopsFirstYearProject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
	requires javafx.graphics;
	requires com.google.zxing;
	requires javafx.base;
	requires webcam.capture;
	requires java.desktop;
	requires com.google.zxing.javase;
	requires javafx.swing;
	requires com.github.librepdf.openpdf;

    opens model to javafx.base;
    opens LoopsFirstYearProject.LoopsFirstYearProject.controllers to javafx.fxml;
    exports LoopsFirstYearProject.LoopsFirstYearProject;
    exports services;
}
