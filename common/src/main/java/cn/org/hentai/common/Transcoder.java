package cn.org.hentai.common;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by matrixy on 2018/4/3.
 */
public final class Transcoder
{
    // 数据包的转发，解密后转发
    public static void decryptAndTransfer(InputStream from, OutputStream to, int byteCount, String key) throws Exception
    {
        int len = 4096;
        byte[] buf = new byte[4096];
        byteCount = Math.min(1024 * 64, byteCount);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(byteCount + 64);
        // 先读4字节，确定内容长度
        from.read(buf, 0, 3);
        if ((buf[0] & 0xff) != 0xfa || (buf[1] & 0xff) != 0xfa || (buf[2] & 0xff) != 0xfa) throw new RuntimeException("错误的协议头");
        len = from.read(buf, 0, 4);
        if (len != 4) throw new RuntimeException("读取数据包长度失败");
        byteCount = ByteUtils.toInt(buf);
        for (int i = 0; i < byteCount; i += len)
        {
            len = from.read(buf, 0, Math.min(4096, byteCount - i));
            baos.write(buf, 0, len);
        }
        buf = null;
        buf = DES.decrypt(baos.toByteArray(), key);
        to.write(buf);
        to.flush();
    }

    // 数据包的转发：加密后转发
    public static void encryptAndTransfer(InputStream from, OutputStream to, int byteCount, String key) throws Exception
    {
        int len = 4096;
        byte[] buf = new byte[4096];
        byteCount = Math.min(1024 * 64, byteCount);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(byteCount + 64);
        for (int i = 0; i < byteCount; i += len)
        {
            len = from.read(buf, 0, Math.min(4096, byteCount - i));
            baos.write(buf, 0, len);
            // to.write(buf, 0, len);
        }
        buf = null;
        buf = DES.encrypt(baos.toByteArray(), key);
        to.write((byte)0xfa);
        to.write((byte)0xfa);
        to.write((byte)0xfa);
        to.write(ByteUtils.toBytes(buf.length));
        to.write(buf);
        to.flush();
    }
}
