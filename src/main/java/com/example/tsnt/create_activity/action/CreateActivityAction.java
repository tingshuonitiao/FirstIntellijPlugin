package com.example.tsnt.create_activity.action;

import com.example.tsnt.create_activity.template.ActivityNodeTemplate;
import com.example.tsnt.create_activity.template.ActivityTemplate;
import com.example.tsnt.create_activity.template.XmlTemplate;
import com.example.tsnt.create_activity.ui.CreateActivityDialog;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.http.util.TextUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.*;

public class CreateActivityAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent event) {
        showInputText(event);
    }

    /**
     * 展示输入Activity名字的文本框
     *
     * @param event
     */
    private void showInputText(AnActionEvent event) {
        new CreateActivityDialog.Builder()
                .setOnConfirmClickListener(new CreateActivityDialog.OnConfirmClickListener() {
                    @Override
                    public void onConfirm(String activityName) {
                        createActivity(event, activityName);
                    }
                })
                .build()
                .showDialog();
    }

    /**
     * 创建Activity
     *
     * @param event
     * @param activityName
     */
    private void createActivity(AnActionEvent event, String activityName) {
        if (TextUtils.isEmpty(activityName)) {
            return;
        }
        VirtualFile file = event.getData(PlatformDataKeys.VIRTUAL_FILE);
        // 生成layout文件
        String layoutName = generateLayout(file, activityName);
        // 生成Java文件
        String fullClassName = generateJavaFile(file, activityName, layoutName);
        // 将Activity注册到AndroidManifest
        registerToManifest(file, fullClassName);
    }

    /**
     * 生成layout文件
     *
     * @param file
     * @param activityName
     * @return layout的名字
     */
    private String generateLayout(VirtualFile file, String activityName) {
        String filePath = file.getPath();
        StringBuilder sb = new StringBuilder("activity");
        int rightBorder = activityName.length() - "activity".length();
        for (int i = 0; i < rightBorder; i++) {
            if (Character.isUpperCase(activityName.charAt(i))) {
                // 如果是大写
                sb.append("_");
            }
            sb.append(Character.toLowerCase(activityName.charAt(i)));
        }
        String layoutName = sb.toString();
        String src_main = "src/main/";
        int index = filePath.indexOf(src_main) + src_main.length();
        while (true) {
            String layoutPath = filePath.substring(0, index) +
                    "res/layout/" +
                    layoutName +
                    ".xml";
            File layoutFile = new File(layoutPath);
            if (!layoutFile.exists()) {
                // 如果layout文件不存在, 去创建
                try {
                    layoutFile.createNewFile();
                    writeFile(layoutPath, generateXmlContent());
                } catch (IOException e) {
                    layoutName = null;
                }
                break;
            } else {
                // 如果layout文件存在, 修改文件名
                layoutName += "_1";
            }
        }
        return layoutName;
    }

    /**
     * 生成Java文件
     *
     * @param file
     * @param activityName
     * @param layoutName
     * @return Activity的全类名
     */
    private String generateJavaFile(VirtualFile file, String activityName, String layoutName) {
        // 生成Java文件内容
        String importPath = getImportPath(file);
        String rPath = getRPath(file);
        String content = generateJavaContent(importPath, rPath, activityName, layoutName);
        // 生成Java文件
        String javaPath = file.getPath()
                + "/"
                + activityName
                + ".java";
        File newFile = new File(javaPath);
        try {
            newFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 写入Java文件内容
        writeFile(javaPath, content);
        String fullClassName = importPath + "." + activityName;
        return fullClassName;
    }

    /**
     * 将Activity注册到AndroidManifest
     *
     * @param file
     * @param fullClassName
     */
    private void registerToManifest(VirtualFile file, String fullClassName) {
        FileReader reader = null;
        FileWriter writer = null;
        BufferedReader bufferedReader = null;
        try {
            String manifestPath = getManifestPath(file.getPath());
            reader = new FileReader(manifestPath);
            bufferedReader = new BufferedReader(reader);
            StringBuilder sb = new StringBuilder();
            // 每一行的内容
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                // 找到application节点的末尾
                if (line.contains("</application>")) {
                    // 在application节点最后插入新创建的activity节点
                    String activityNode = generateActivityNodeContent(fullClassName);
                    sb.append(activityNode + "\n");
                }
                sb.append(line + "\n");
            }
            String content = sb.toString();
            // 删除最后多出的一行
            content = content.substring(0, content.length() - 1);
            writer = new FileWriter(manifestPath);
            writer.write(content);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 获取当前的导包路径
     *
     * @param file
     * @return
     */
    private String getImportPath(VirtualFile file) {
        String path = file.getPath();
        String str = "src/main/java/";
        int index = path.indexOf(str) + str.length();
        String importPath = path.substring(index, path.length());
        importPath = importPath.replace("/", ".");
        return importPath;
    }

    /**
     * 获取AndroidManifest文件的路径
     *
     * @param filePath
     * @return
     */
    private String getManifestPath(String filePath) {
        String str = "/src/main";
        int index = filePath.indexOf(str) + str.length();
        return filePath.substring(0, index) + "/AndroidManifest.xml";
    }

    /**
     * 通过解析AndroidManifest文件
     * 获取R文件的路径
     *
     * @param file
     * @return
     */
    private String getRPath(VirtualFile file) {
        String manifestPath = getManifestPath(file.getPath());
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder documentBuilder = factory.newDocumentBuilder();
            File manifestFile = new File(manifestPath);
            Document doc = documentBuilder.parse(manifestFile);
            Element root = doc.getDocumentElement();
            // 获取manifest节点下的package属性
            String packageName = root.getAttribute("package");
            return packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 生成xml内容
     *
     * @return
     */
    private String generateXmlContent() {
        return XmlTemplate.template;
    }

    /**
     * 生成Java文件内容
     *
     * @param importPath
     * @param rPath
     * @param activityName
     * @param layoutName
     * @return
     */
    private String generateJavaContent(String importPath, String rPath, String activityName, String layoutName) {
        String content = String.format(ActivityTemplate.template, importPath, rPath, activityName, layoutName);
        return content;
    }

    /**
     * 生成Activity节点内容
     *
     * @param fullClassName
     * @return
     */
    private String generateActivityNodeContent(String fullClassName) {
        String content = String.format(ActivityNodeTemplate.template, fullClassName);
        return content;
    }

    /**
     * 向文件写入内容
     *
     * @param file
     * @param content
     * @throws IOException
     */
    private void writeFile(String file, String content) {
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "rw");
            if (randomAccessFile.length() > 2) {
                randomAccessFile.seek(randomAccessFile.length() - 2);
            }
            randomAccessFile.write(content.getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
