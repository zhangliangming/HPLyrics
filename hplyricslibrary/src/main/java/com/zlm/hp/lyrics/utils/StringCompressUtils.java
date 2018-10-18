package com.zlm.hp.lyrics.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;

/**
 * 字符串解压和压缩
 *
 * @author zhangliangming
 */
public class StringCompressUtils {

    /**
     * 压缩
     *
     * @param text
     * @param charset
     * @return
     */
    public static byte[] compress(String text, Charset charset) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        OutputStream out = new DeflaterOutputStream(baos);
        out.write(text.getBytes(charset));
        out.close();

        return baos.toByteArray();
    }

    /**
     * @param input
     * @param charset
     * @return
     * @throws Exception
     */
    public static String decompress(InputStream input, Charset charset)
            throws Exception {
        return decompress(toByteArray(input), charset);
    }

    /**
     * 解压
     *
     * @param bytes
     * @param charset
     * @return
     */
    public static String decompress(byte[] bytes, Charset charset) throws Exception {
        InputStream in = new InflaterInputStream(
                new ByteArrayInputStream(bytes));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        byte[] buffer = new byte[8192];
        int len;
        while ((len = in.read(buffer)) > 0)
            baos.write(buffer, 0, len);
        return new String(baos.toByteArray(), charset);

    }

    /**
     * @param input
     * @return
     */
    private static byte[] toByteArray(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        copy(input, output);
        return output.toByteArray();
    }

    private static int copy(InputStream input, OutputStream output)
            throws Exception {
        long count = copyLarge(input, output);
        if (count > 2147483647L) {
            return -1;
        }
        return (int) count;
    }

    private static long copyLarge(InputStream input, OutputStream output)
            throws Exception {
        byte[] buffer = new byte[4096];
        long count = 0L;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }
}