package com.example.tsnt.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class CreateActivityDialog extends JFrame {

    private OnConfirmClickListener onConfirmClickListener;

    private CreateActivityDialog() throws HeadlessException {

    }

    /**
     * 展示弹窗
     */
    public void showDialog() {
        setSize(320, 120);
        // 设置点击左上角x按钮, 仅退出JFrame界面
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        // 创建面板, 类似Android的RelativeLayout
        JPanel panel = new JPanel();
        // 添加面板
        add(panel);
        // 调用用户定义的方法并添加组件到面板
        placeComponents(panel);
        // 让JFrame位于屏幕中央
        setLocationRelativeTo(null);
        // 设置界面可见
        setVisible(true);
    }

    /**
     * 调用用户定义的方法并添加组件到面板
     *
     * @param panel
     */
    private void placeComponents(JPanel panel) {
        // 这边设置布局为null
        panel.setLayout(null);

        // JLabel类似Android的TextView
        JLabel userLabel = new JLabel("ActivityName:");
        // 这个方法定义了组件的位置
        userLabel.setBounds(10, 20, 110, 25);
        panel.add(userLabel);

        // JTextField类似Android的EditText
        JTextField userText = new JTextField(20);
        userText.setBounds(110, 20, 165, 25);
        panel.add(userText);

        // 创建按钮, 类似Android的Button
        JButton createButton = new JButton("create Activity");
        createButton.setBounds(90, 60, 140, 25);
        createButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // 界面消失, 释放JFrame资源
                dispose();
                if (onConfirmClickListener != null) {
                    onConfirmClickListener.onConfirm();
                }
            }
        });
        panel.add(createButton);
    }

    public static class Builder {

        private CreateActivityDialog dialog;

        public Builder() {
            dialog = new CreateActivityDialog();
        }

        public Builder setOnConfirmClickListener(OnConfirmClickListener onConfirmClickListener) {
            dialog.setOnConfirmClickListener(onConfirmClickListener);
            return this;
        }

        public CreateActivityDialog build() {
            return dialog;
        }
    }

    public CreateActivityDialog setOnConfirmClickListener(OnConfirmClickListener onConfirmClickListener) {
        this.onConfirmClickListener = onConfirmClickListener;
        return this;
    }

    public interface OnConfirmClickListener {
        void onConfirm();
    }
}
