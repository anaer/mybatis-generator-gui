package com.zzg.mybatis.generator.view;

import javafx.scene.control.TextArea;
import org.dromara.hutool.core.date.DatePattern;
import org.dromara.hutool.core.date.DateUtil;
import org.mybatis.generator.api.ProgressCallback;

import java.util.Date;


/**
 * Created by Owen on 6/21/16.
 */
public class UIProgressCallback implements ProgressCallback {

  private TextArea consoleTextArea;

  public UIProgressCallback(TextArea textArea) {
    this.consoleTextArea = textArea;
  }

  private void show(String text) {
    String msg = String.format("%s %s\n", DateUtil.format(new Date(), DatePattern.NORM_TIME_FORMAT), text);
    consoleTextArea.appendText(msg);
  }

  @Override
  public void introspectionStarted(int totalTasks) {
    show("开始代码检查:" + totalTasks);
  }

  @Override
  public void generationStarted(int totalTasks) {
    show("开始代码生成:"+totalTasks);
  }

  @Override
  public void saveStarted(int totalTasks) {
    show("开始保存生成的文件:"+totalTasks);
  }

  @Override
  public void startTask(String taskName) {
    show(taskName);
  }

  @Override
  public void done() {
    show("代码生成完成");

    AlertUtil.showInfoAlert("代码生成完成");
  }

  @Override
  public void checkCancel() throws InterruptedException {
  }
}
