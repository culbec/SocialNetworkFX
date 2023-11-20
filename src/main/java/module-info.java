module ro.ubbcluj.map.socialnetworkfx {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;

    exports ro.ubbcluj.map.socialnetworkfx;
    opens ro.ubbcluj.map.socialnetworkfx to javafx.fxml;

    exports ro.ubbcluj.map.socialnetworkfx.controllers;
    opens ro.ubbcluj.map.socialnetworkfx.controllers to javafx.fxml;

    exports ro.ubbcluj.map.socialnetworkfx.exception;
    opens ro.ubbcluj.map.socialnetworkfx.exception to javafx.fxml;

    exports ro.ubbcluj.map.socialnetworkfx.entity;
    opens ro.ubbcluj.map.socialnetworkfx.entity to javafx.fxml;

    exports ro.ubbcluj.map.socialnetworkfx.repository;
    opens ro.ubbcluj.map.socialnetworkfx.repository to javafx.fxml;

    exports ro.ubbcluj.map.socialnetworkfx.service;
    opens ro.ubbcluj.map.socialnetworkfx.service to javafx.fxml;

    exports ro.ubbcluj.map.socialnetworkfx.utility;
    opens ro.ubbcluj.map.socialnetworkfx.utility to javafx.fxml;

    exports ro.ubbcluj.map.socialnetworkfx.events;
    opens ro.ubbcluj.map.socialnetworkfx.events to javafx.fxml;

    exports ro.ubbcluj.map.socialnetworkfx.utility.observer;
    opens ro.ubbcluj.map.socialnetworkfx.utility.observer to javafx.fxml;
}