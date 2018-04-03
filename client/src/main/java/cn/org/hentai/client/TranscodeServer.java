package cn.org.hentai.client;

import cn.org.hentai.common.Configs;
import cn.org.hentai.common.Log;

import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by matrixy on 2018/4/3.
 */
public class TranscodeServer extends Thread
{
    private void listen() throws Exception
    {
        ServerSocket server = new ServerSocket(Configs.getInt("local.listen.port", 64161), 10, InetAddress.getByName("0.0.0.0"));
        while (true)
        {
            Socket conn = server.accept();
            new TranscodeSession(conn).start();
        }
    }

    public void run()
    {
        try
        {
            listen();
        }
        catch(Exception e)
        {
            Log.error(e);
        }
    }
}
