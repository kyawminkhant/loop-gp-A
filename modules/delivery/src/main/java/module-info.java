module LoopProject.loopproject {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires javafx.graphics;

    exports main;

    opens main to javafx.fxml;
    opens model to javafx.base;
    opens LoopProject.loopproject.controllers to javafx.fxml;

    exports LoopProject.loopproject.controllers;
}