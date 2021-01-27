package nil.nadph.qnotified.script;

import android.widget.*;

import java.io.*;
import java.util.*;

import bsh.*;
import nil.nadph.qnotified.config.*;
import nil.nadph.qnotified.util.*;

import static nil.nadph.qnotified.util.Utils.*;

public class QNScriptManager {
    
    public static int enables = 0;
    public static boolean enableall = false;
    public static String scriptsPath;
    public static String error = "啥也没";
    private static final List<QNScript> scripts = new ArrayList<>();
    private static boolean init = false;
    
    /**
     * 添加一个脚本
     *
     * @param file 文件
     * @return
     */
    public static String addScript(String file) throws Exception {
        if (isNullOrEmpty(file)) {
            return "file is null";
        }
        if (hasScript(file)) {
            return "脚本已存在";
        }
        // to do
        // 操作: 将文件移动到软件数据文件夹下
        File s = new File(file);
        File dir = new File(scriptsPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(dir, s.getName());
        Utils.copy(s, f);
        String code = readByReader(new FileReader(f));
        if (!isNullOrEmpty(code)) {
            scripts.add(execute(code));
        }
        return "";
    }
    
    public static String addScriptFD(FileDescriptor fileDescriptor, String scriptName) throws Throwable {
        File dir = new File(scriptsPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            fileInputStream = new FileInputStream(fileDescriptor);
            StringBuffer stringBuffer = new StringBuffer();
            byte[] buf = new byte[1024];
            int len;
            while ((len = fileInputStream.read(buf)) > 0) {
                stringBuffer.append(new String(buf, 0, len));
            }
            if (hasScriptStr(stringBuffer.toString())) {
                return "脚本已存在";
            }
            fileOutputStream = new FileOutputStream(scriptsPath + scriptName);
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
            outputStreamWriter.write(stringBuffer.toString());
            outputStreamWriter.close();
            fileOutputStream.flush();
        } finally {
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
        }
        String code = readByReader(new FileReader(scriptsPath + scriptName));
        if (!isNullOrEmpty(code)) {
            scripts.add(execute(code));
        }
        return "";
    }
    
    public static void addEnable() {
        enables++;
        if (enables > scripts.size() - 1) {
            enables = scripts.size();
        }
    }
    
    public static void delEnable() {
        enables--;
        if (enables < 0) {
            enables = 0;
        }
    }
    
    /**
     * 判断脚本是否存在
     *
     * @param file 文件
     * @return 是否存在
     */
    public static boolean hasScript(String file) throws Exception {
        if (Utils.isNullOrEmpty(file)) {
            return false;
        }
        // to do
        // 判断文件
        QNScriptInfo info = QNScriptInfo.getInfo(Utils.readByReader(new FileReader(new File(file))));
        if (info == null) {
            throw new RuntimeException("不是有效的脚本文件");
        }
        for (QNScript q : getScripts()) {
            if (info.label.equalsIgnoreCase(q.getLabel())) {
                return true;
            }
        }
        return false;
    }
    
    public static boolean hasScriptStr(String code) throws Exception {
        QNScriptInfo info = QNScriptInfo.getInfo(code);
        if (info == null) {
            throw new RuntimeException("不是有效的脚本文件");
        }
        for (QNScript q : getScripts()) {
            if (info.label.equalsIgnoreCase(q.getLabel())) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * 删除脚本
     *
     * @param script
     * @return
     */
    public static boolean delScript(QNScript script) {
        // to do
        // 删除文件
        File dir = new File(scriptsPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.isDirectory()) {
            log(new RuntimeException("脚本文件夹不应为一个文件"));
            return false;
        }
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                continue;
            }
            try {
                QNScriptInfo info = QNScriptInfo.getInfo(Utils.readByReader(new FileReader(f)));
                if (info.label.equalsIgnoreCase(script.getLabel())) {
                    f.delete();
                    return true;
                }
            } catch (Exception e) {
                log(e);
            }
        }
        for (QNScript q : scripts) {
            if (q.getLabel().equalsIgnoreCase(script.getLabel())) {
                scripts.remove(q);
            }
        }
        return false;
    }
    
    /**
     * 获取所有的脚本代码
     *
     * @return
     */
    public static List<String> getScriptCodes() {
        // to do
        // 返回全部脚本代码
        List<String> codes = new ArrayList<String>() {{
            try {
                add(Utils.readByReader(new InputStreamReader(Utils.toInputStream("demo.java"))));
            } catch (IOException e) {
                log(e);
            }
        }};
        File dir = new File(scriptsPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        if (!dir.isDirectory()) {
            log(new RuntimeException("脚本文件夹不应为一个文件"));
            return codes;
        }
        for (File f : dir.listFiles()) {
            if (f.isDirectory()) {
                continue;
            }
            try {
                String code = Utils.readByReader(new FileReader(f));
                if (!Utils.isNullOrEmpty(code)) {
                    codes.add(code);
                }
            } catch (Exception e) {
                log(e);
            }
        }
        return codes;
    }
    
    /**
     * 获取所有的脚本
     *
     * @return
     */
    public static List<QNScript> getScripts() {
        return scripts;
    }
    
    public static void init() {
        if (init) {
            return;
        }
        scriptsPath = getApplication().getFilesDir().getAbsolutePath() + "/qn_script/";
        for (String code : getScriptCodes()) {
            try {
                QNScript qs = execute(code);
                scripts.add(qs);
                if (qs.isEnable()) {
                    qs.onLoad();
                }
            } catch (EvalError e) {
                log(e);
            }
        }
        init = true;
    }
    
    public static QNScript execute(String code) throws EvalError {
        Interpreter lp = new Interpreter();
        lp.setClassLoader(Initiator.class.getClassLoader());
        return QNScript.create(lp, code);
    }
    
    
    public static void changeGlobal(CompoundButton compoundButton, boolean b) {
        ConfigManager cfg = ConfigManager.getDefaultConfig();
        cfg.putBoolean(ConfigItems.qn_script_global, b);
        try {
            cfg.save();
        } catch (IOException e) {
            log(e);
        }
    }
    
    public static void enableAll() {
        enableall = true;
        for (QNScript qs : QNScriptManager.getScripts()) {
            if (!qs.isEnable()) {
                qs.setEnable(true);
                addEnable();
            }
        }
        
    }
    
    public static void disableAll() {
        enableall = false;
        for (QNScript qs : QNScriptManager.getScripts()) {
            if (qs.isEnable()) {
                qs.setEnable(false);
                delEnable();
            }
        }
        
    }
    
    public static int getAllCount() {
        return scripts.size();
    }
    
    public static int getEnableCount() {
        return enables;
    }
    
    public static void enableAll(CompoundButton compoundButton, boolean b) {
        if (b) {
            enableAll();
        } else {
            disableAll();
        }
        Utils.showToast(compoundButton.getContext(), Utils.TOAST_TYPE_ERROR, "重启" + Utils.getHostAppName() + "生效", Toast.LENGTH_SHORT);
    }
    
    public static boolean isEnableAll() {
        return enableall;
    }
}
