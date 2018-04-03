package cn.org.hentai.server;

import cn.org.hentai.common.Configs;
import cn.org.hentai.common.Log;

/**
 * Created by matrixy on 2018/4/3.
 */
public class ServerApp
{
    public static void main(String[] args) throws Exception
    {
        Configs.init("/server.properties");
        new TranscodeServer().start();
        Log.debug("Middleman server started on: " + Configs.getInt("server.listen.port", 64035));
    }
}
