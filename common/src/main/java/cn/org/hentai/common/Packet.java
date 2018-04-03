package cn.org.hentai.common;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by matrixy on 2018/4/3.
 */
public final class Packet
{
    private static final byte[] HEADER = new byte[] { (byte)0xfa, (byte)0xfa, (byte)0xfa };

    // 认证数据包
    // fa fa fa 00 00 00 04 00 01 02 03 04 05 06 07 08 09 10 0a 0b 0c 0d 0e 0f 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00 00
    //    协议头
    //                包长度
    //                                                                    明文
    //                                                                                                                       密文

    /**
     * 创建一个验证数据包
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] createAuthenticationPacket(String key) throws Exception
    {
        String text = NonceStr.generate(16);
        byte[] encryptedData = DES.encrypt(text.getBytes(), key);
        return create(ByteUtils.concat(text.getBytes(), encryptedData));
    }

    /**
     * 创建一个加密后的数据包，以便于传输
     * @param data
     * @param key
     * @return
     * @throws Exception
     */
    public static byte[] encryptPacket(byte[] data, String key) throws Exception
    {
        return create(DES.encrypt(data, key));
    }

    private static byte[] create(byte[] data) throws Exception
    {
        return ByteUtils.concat(HEADER, ByteUtils.toBytes(data.length), data);
    }

    // 回应
    // fa fa fa 00 00 00 04 00 00 00 00
    //                      O  J  B  K
    // 开始转发吧

    // 转发数据包
    // fa fa fa 00 00 00 04 00 00 00 00
    //                      加密后的数据体

    // 读取一整个数据包
    public static byte[] readEncryptedPacket(InputStream inputStream) throws IOException
    {
        if (inputStream.available() < 7) return null;
        byte[] head = new byte[7];
        inputStream.read(head);
        if ((head[0] & 0xff) != 0xfa || (head[1] & 0xff) != 0xfa || (head[2] & 0xff) != 0xfa) throw new RuntimeException("错误的协议头");
        int len = 0, byteCount = ByteUtils.getInt(head, 3, 4);
        byte[] buf = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream(40960);
        for (int i = 0; i < byteCount; i += len)
        {
            len = inputStream.read(buf, 0, Math.min(4096, byteCount - i));
            baos.write(buf, 0, len);
        }
        return baos.toByteArray();
    }
}
