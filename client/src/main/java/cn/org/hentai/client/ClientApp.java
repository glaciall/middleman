package cn.org.hentai.client;

import cn.org.hentai.common.Configs;
import cn.org.hentai.common.Log;

/**
 * Created by matrixy on 2018/4/3.
 */
public class ClientApp
{
    public static void main(String[] args) throws Exception
    {
        Configs.init("/client.properties");
        new TranscodeServer().start();
        Log.debug("Middleman client started on: " + Configs.getInt("local.listen.port", 64161));
    }
}
