package nil.nadph.qnotified.hook;

import java.io.*;
import java.lang.reflect.*;
import java.net.*;

import de.robv.android.xposed.*;
import nil.nadph.qnotified.*;
import nil.nadph.qnotified.step.*;
import nil.nadph.qnotified.util.*;

public class BlockFluxThief extends CommonDelayableHook {
    public static final BlockFluxThief INSTANCE = new BlockFluxThief();
    
    private BlockFluxThief() {
        super("bug_block_flux_thief", SyncUtils.PROC_ANY, true, new DexDeobfStep(DexKit.C_ZipUtils_biz));
    }
    
    static long requestUrlSizeBlocked(String url) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
        conn.setRequestMethod("GET");
        int code = conn.getResponseCode();
        String lenStr = conn.getHeaderField("Content-Length");
        conn.getInputStream().close();
        conn.disconnect();
        if (lenStr == null) {
            return -1L;
        } else {
            try {
                return Long.parseLong(lenStr);
            } catch (Throwable th) {
                Utils.logd(String.format("BlockFluxThief/W [%d] %s %s", code, lenStr, url));
                return -1;
            }
        }
    }
    
    @Override
    protected boolean initOnce() {
        try {
            Method downloadImage = null;
            for (Method m : DexKit.doFindClass(DexKit.C_HttpDownloader).getDeclaredMethods()) {
                if (m.getReturnType() != File.class || Modifier.isStatic(m.getModifiers())) {
                    continue;
                }
                Class<?>[] argt = m.getParameterTypes();
                if (argt.length != 5 || argt[0] != OutputStream.class || argt[3] != int.class || argt[4] != URL.class) {
                    continue;
                }
                downloadImage = m;
                break;
            }
            XposedBridge.hookMethod(downloadImage, new XC_MethodHook(51) {
                @Override
                protected void afterHookedMethod(MethodHookParam param) throws Throwable {
                    if (LicenseStatus.sDisableCommonHooks || !isEnabled()) {
                        return;
                    }
                    long maxSize = 32 * 1024 * 1024;//32MiB
                    String url = (String) Utils.iget_object_or_null(param.args[1], "urlStr");
                    Class<?> cHttpDownloader = param.method.getDeclaringClass();
                    Method mGetFilePath = cHttpDownloader.getMethod("getFilePath", String.class);
                    if (mGetFilePath == null) {
                        mGetFilePath = Utils.hasMethod(mGetFilePath, "d", null, String.class, String.class);
                    }
                    String savePath = (String) mGetFilePath.invoke(null, url);
                    if (!new File(savePath).exists()) {
                        try {
                            long size = requestUrlSizeBlocked(url);
                            if (size != -1) {
                                if (size > maxSize) {
                                    param.setResult(null);
                                    Toasts.show(null, String.format("已拦截异常图片加载, 大小: %s", BugUtils.getSizeString(size)));
                                }
                            } else {
                                // TODO: 2021-1-9 Unknown size, do nothing?
                            }
                        } catch (IOException e) {
                            Utils.logd("BlockFluxThief/Req " + e.toString() + " URL=" + url);
                        }
                    }
                }
            });
            return true;
        } catch (Throwable e) {
            Utils.log(e);
            return false;
        }
    }
}
