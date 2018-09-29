package com.example.tsnt.action;

import com.example.tsnt.ui.CreateActivityDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;

import javax.swing.*;
import java.io.File;
import java.io.IOException;

public class CreateActivityAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        showInputText(event);
    }

    private void showInputText(AnActionEvent event) {
        new CreateActivityDialog.Builder()
                .setOnConfirmClickListener(new CreateActivityDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirm() {
                        createActivity(event);
                    }
                })
                .build()
                .showDialog();
    }

    private void createActivity(AnActionEvent event) {
        VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        String fullPath = file.getPath() + "/Test.java";
        File newFile = new File(fullPath);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
