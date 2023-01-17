package com.zzg.mybatis.generator.view;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * Created by Owen on 6/21/16.
 */
public class AlertUtil {

    public static void showInfoAlert(String message) {
        showAlert(AlertType.INFORMATION, message);
    }

    public static void showWarnAlert(String message) {
        showAlert(AlertType.WARNING, message);
    }

    public static void showErrorAlert(String message) {
        showAlert(AlertType.ERROR, message);
    }

    public static void showAlert(AlertType alertType, String message) {
        Alert alert = new Alert(alertType);
        alert.setContentText(message);
        alert.show();
    }

}
