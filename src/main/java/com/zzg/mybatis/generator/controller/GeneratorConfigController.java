package com.zzg.mybatis.generator.controller;

import com.zzg.mybatis.generator.model.GeneratorConfig;
import com.zzg.mybatis.generator.util.ConfigHelper;
import com.zzg.mybatis.generator.view.AlertUtil;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import org.dromara.hutool.core.collection.CollUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

/**
 * 管理GeneratorConfig的Controller
 *
 * Created by Owen on 8/21/16.
 */
@SuppressWarnings("all")
public class GeneratorConfigController extends BaseFXController {

    private static final Logger _LOG = LoggerFactory.getLogger(GeneratorConfigController.class);

    @FXML
    private TableView<GeneratorConfig> configTable;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn opsColumn;

    private MainUIController mainUIController;

    private GeneratorConfigController controller;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        controller = this;
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        // 自定义操作列
        opsColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
        opsColumn.setCellFactory(cell -> new TableCell() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setText(null);
                    setGraphic(null);
                } else {
                    Button btn1 = new Button("应用");
                    Button btn2 = new Button("删除");
                    HBox hBox = new HBox();
                    hBox.setSpacing(10);
                    hBox.getChildren().add(btn1);
                    hBox.getChildren().add(btn2);
                    btn1.setOnAction(event -> {
                        try {
                            // 应用配置
                            mainUIController.checkDatabaseConfig();
                            GeneratorConfig generatorConfig = ConfigHelper.loadGeneratorConfig(item.toString());
                            mainUIController.setGeneratorConfigIntoUI(generatorConfig);
                            controller.closeDialogStage();
                        } catch (Exception e) {
                            AlertUtil.showErrorAlert(e.getMessage());
                        }
                    });
                    btn2.setOnAction(event -> {
                        try {
                            // 删除配置
                            _LOG.debug("item: {}", item);
                            ConfigHelper.deleteGeneratorConfig(item.toString());
                            refreshTableView();
                        } catch (Exception e) {
                            AlertUtil.showErrorAlert(e.getMessage());
                        }
                    });
                    setGraphic(hBox);
                }
            }
        });
        refreshTableView();
    }

    public void refreshTableView() {
        try {
            List<GeneratorConfig> configs = ConfigHelper.loadGeneratorConfigs();

            // 按名称排序, 配置名格式为dbName.tableName时, 可以将同一db下的放在一起
            if (CollUtil.isNotEmpty(configs)) {
                configs = configs.stream().sorted(Comparator.comparing(GeneratorConfig::getName))
                        .collect(Collectors.toList());
            }

            configTable.setItems(FXCollections.observableList(configs));
        } catch (Exception e) {
            AlertUtil.showErrorAlert(e.getMessage());
        }
    }

    void setMainUIController(MainUIController mainUIController) {
        this.mainUIController = mainUIController;
    }

}
