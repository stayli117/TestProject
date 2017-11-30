package com.yh.urltest.zlib.net.util;


import android.text.TextUtils;
import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * <pre>
 *     author: gyh
 *     time  : 2016/11/17
 *     desc  : 流 及 关闭相关工具类
 * </pre>
 */
public class StreamUtils {
    private StreamUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    public static String loadFileAsString(String filePath) throws Exception {
        if (isFileExists(filePath))
            return streamToStr(new FileInputStream(filePath));
        return null;
    }

    public static String loadFileAsString(File fileName) throws Exception {
        return streamToStr(new FileInputStream(fileName));
    }

    public static String streamToStr(InputStream is) throws IOException {
        return readInputStream(is).toString();
    }

    public static byte[] streamToByteArray(InputStream ImStream) throws IOException {
        return readInputStream(ImStream).toByteArray();
    }

    /**
     * 加载文件获取数组
     *
     * @param fileName 文件名
     * @return 数组
     */
    public static String[] loadFileToSArr(String fileName) {
        try {
            if (isFileExists(fileName)) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)), 1000);
                String load = reader.readLine();
                reader.close();
                if (!TextUtils.isEmpty(load)) {
                    return load.split(" ");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 加载文件获取数组
     *
     * @param fileName 文件名
     * @return 内容数组
     */
    public static String[] loadFileToSArr(File fileName) {
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream(fileName)), 1000);
            String load = reader.readLine();
            reader.close();
            if (!TextUtils.isEmpty(load)) {
                return load.split(" ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据文件路径判断文件是否存在
     *
     * @param filePath 文件路径
     * @return true
     */
    private static boolean isFileExists(String filePath) {
        File file = new File(filePath);
        return file.exists() && file.isFile();
    }


    public static String errorPrintStackTrace(Throwable e) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        e.printStackTrace(pw);
        pw.flush();
        pw.close();
        sw.flush();
        return sw.toString();
    }

    /**
     * gzip 压缩字符串
     *
     * @param str 元数据
     * @return 压缩结果
     */
    public static synchronized String comGzip(String str) {
        if (PaUtil.isNullOrEmpty(str)) {
            return null;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            GZIPOutputStream gzip = new GZIPOutputStream(out);
            gzip.write(str.getBytes());
            gzip.close();
            return Base64.encodeToString(out.toByteArray(), Base64.DEFAULT);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Gzip解压数据 Base
     *
     * @param gzipStr 待解压数据源
     * @return 解压结果
     */
    public static synchronized String dComForGzip(String gzipStr) {
        if (PaUtil.isNullOrEmpty(gzipStr)) {
            return null;
        }
        byte[] t = Base64.decode(gzipStr.getBytes(), Base64.DEFAULT);
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ByteArrayInputStream in = new ByteArrayInputStream(t);
            GZIPInputStream gzip = new GZIPInputStream(in);
            byte[] buffer = new byte[1024];
            int n = 0;
            while ((n = gzip.read(buffer, 0, buffer.length)) > 0) {
                out.write(buffer, 0, n);
            }
            gzip.close();
            in.close();
            out.close();
            return out.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static synchronized ByteArrayOutputStream readInputStream(final InputStream ImStream) throws IOException {
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = ImStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, len);
        }
        closeIO(outStream, ImStream);
        return outStream;
    }

    /**
     * 关闭IO
     *
     * @param closeables closeable
     */
    public static void closeIO(Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 安静关闭IO
     *
     * @param closeables closeable
     */
    public static void closeIOQuietly(Closeable... closeables) {
        if (closeables == null) return;
        for (Closeable closeable : closeables) {
            if (closeable != null) {
                try {
                    closeable.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    public static void writeStr2File(String value, File logFile) {
        try {
            if (logFile.exists() && logFile.isFile()) {
                FileOutputStream fos = new FileOutputStream(logFile, true);
                fos.write(value.getBytes("Utf-8"));
                fos.flush();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
