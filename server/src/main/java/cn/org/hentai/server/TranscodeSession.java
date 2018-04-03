package cn.org.hentai.server;

import cn.org.hentai.common.*;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by matrixy on 2018/4/3.
 */
public class TranscodeSession extends Thread
{
    private Socket clientConnection;
    private Socket localConnection;
    public TranscodeSession(Socket conn)
    {
        this.clientConnection = conn;
        this.setName("trans-" + conn.getInetAddress());
    }

    private void converse() throws Exception
    {
        int idleTimeout = Configs.getInt("socket.idletimeout", 60000);
        long lastActiveTime = System.currentTimeMillis();

        clientConnection.setSoTimeout(Configs.getInt("socket.iotimeout", 30000));
        InputStream clientIS = clientConnection.getInputStream();
        OutputStream clientOS = clientConnection.getOutputStream();
        Log.debug("[" + this.getName() + "]己连接...");

        // 读取认证包
        byte[] auth = Packet.readPacket(clientIS);
        String text = new String(auth, 0, 16);
        byte[] encryptedData = new byte[auth.length - 16];
        System.arraycopy(auth, 16, encryptedData, 0, auth.length - 16);
        if (!ByteUtils.compare(DES.encrypt(text.getBytes(), Configs.get("server.authentication.password")), encryptedData))
        {
            Log.debug("客户端[" + this.getName() + "]认证失败...");
            clientOS.write(Packet.create("FAIL".getBytes()));
            clientOS.flush();
            return;
        }

        // 下发反馈
        byte[] resp = new byte[20];
        resp[0] = 'O';
        resp[1] = 'J';
        resp[2] = 'B';
        resp[3] = 'K';
        String key = NonceStr.generate(16);
        System.arraycopy(key.getBytes(), 0, resp, 4, 16);
        clientOS.write(Packet.create(resp));
        clientOS.flush();
        Log.debug("客户端[" + getName() + "]认证成功，会话密码：" + key);

        // 连接到本地
        localConnection = new Socket(Configs.get("server.forward.host"), Configs.getInt("server.forward.port", 80));
        localConnection.setSoTimeout(Configs.getInt("socket.iotimeout", 30000));
        InputStream localIS = localConnection.getInputStream();
        OutputStream localOS = localConnection.getOutputStream();

        // 开始转发
        while (true)
        {
            int clientBufLength = clientIS.available();
            if (clientBufLength >= 7)
            {
                // 客户端到本地，需要解密后转发
                Transcoder.decryptAndTransfer(clientIS, localOS, clientBufLength, key);
            }
            int localBufLength = localIS.available();
            if (localBufLength > 0)
            {
                // 本地到客户端，需要加密后转发
                Transcoder.encryptAndTransfer(localIS, clientOS, localBufLength, key);
            }
            if (localBufLength + clientBufLength == 0) Thread.sleep(1);
            else lastActiveTime = System.currentTimeMillis();
            if (System.currentTimeMillis() - lastActiveTime > idleTimeout)
            {
                Log.debug("会话[" + this.getName() + "]闲置超时...");
                break;
            }
        }
    }

    public void run()
    {
        try
        {
            converse();
        }
        catch(Exception e)
        {
            Log.error(e);
        }
        finally
        {
            try { clientConnection.close(); } catch(Exception e) { }
            try { localConnection.close(); } catch(Exception e) { }
        }
    }
}
