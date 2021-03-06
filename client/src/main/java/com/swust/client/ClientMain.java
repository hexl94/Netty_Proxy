package com.swust.client;

import com.swust.client.handler.ClientHandler;
import com.swust.common.cmd.CmdOptions;
import com.swust.common.codec.MessageDecoder;
import com.swust.common.codec.MessageEncoder;
import com.swust.common.config.LogFormatter;
import com.swust.common.constant.Constant;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.apache.commons.cli.*;

import java.util.logging.Logger;

/**
 * @author : LiuMing
 * @date : 2019/11/4 14:15
 * @description :   内网的netty客户端，该客户端内部嵌了一个客户端，内部的客户端是访问本地的应用
 */
public class ClientMain {
    private static Logger logger = Logger.getGlobal();

    public static void main(String[] args) throws Exception {
        LogFormatter.init();
        Options options = new Options();
        options.addOption(CmdOptions.HELP.getOpt(), CmdOptions.HELP.getLongOpt(),
                CmdOptions.HELP.isHasArgs(), CmdOptions.HELP.getDescription());
        options.addOption(CmdOptions.HOST.getOpt(), CmdOptions.HOST.getLongOpt(),
                CmdOptions.HOST.isHasArgs(), CmdOptions.HOST.getDescription());
        options.addOption(CmdOptions.PORT.getOpt(), CmdOptions.PORT.getLongOpt(),
                CmdOptions.PORT.isHasArgs(), CmdOptions.PORT.getDescription());
        options.addOption(CmdOptions.PASSWORD.getOpt(), CmdOptions.PASSWORD.getLongOpt(),
                CmdOptions.PASSWORD.isHasArgs(), CmdOptions.PASSWORD.getDescription());
        options.addOption(CmdOptions.PROXY_HOST.getOpt(), CmdOptions.PROXY_HOST.getLongOpt(),
                CmdOptions.PROXY_HOST.isHasArgs(), CmdOptions.PROXY_HOST.getDescription());
        options.addOption(CmdOptions.PROXY_PORT.getOpt(), CmdOptions.PROXY_PORT.getLongOpt(),
                CmdOptions.PROXY_PORT.isHasArgs(), CmdOptions.PROXY_PORT.getDescription());
        options.addOption(CmdOptions.REMOTE_PORT.getOpt(), CmdOptions.REMOTE_PORT.getLongOpt(),
                CmdOptions.REMOTE_PORT.isHasArgs(), CmdOptions.REMOTE_PORT.getDescription());

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        if (cmd.hasOption(CmdOptions.HELP.getLongOpt()) || cmd.hasOption(CmdOptions.HELP.getOpt())) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp(Constant.OPTIONS, options);
        } else {

            //opt和longOpt都可以拿到命令对应的值
            serverAddress = cmd.getOptionValue(CmdOptions.HOST.getOpt());
            if (serverAddress == null) {
                logger.severe("server_addr cannot be null");
                return;
            }
            serverPort = cmd.getOptionValue(CmdOptions.PORT.getOpt());
            if (serverPort == null) {
                logger.severe("server_port cannot be null");
                return;
            }
            password = cmd.getOptionValue(CmdOptions.PASSWORD.getOpt());
            proxyAddress = cmd.getOptionValue(CmdOptions.PROXY_HOST.getOpt());
            if (proxyAddress == null) {
                logger.severe("proxy_addr cannot be null");
                return;
            }
            proxyPort = cmd.getOptionValue(CmdOptions.PROXY_PORT.getOpt());
            if (proxyPort == null) {
                logger.severe("proxy_port cannot be null");
                return;
            }
            remotePort = cmd.getOptionValue(CmdOptions.REMOTE_PORT.getOpt());
            if (remotePort == null) {
                logger.severe("remote_port cannot be null");
                return;
            }
            start();
        }
    }

    private static String serverAddress;
    private static String serverPort;
    private static String proxyAddress;
    private static String proxyPort;
    private static String password;
    private static String remotePort;

    public static void start() throws Exception {
        TcpClient.connect(serverAddress, Integer.parseInt(serverPort), new ChannelInitializer<SocketChannel>() {
            @Override
            public void initChannel(SocketChannel ch) {
                ClientHandler clientHandler = new ClientHandler(Integer.parseInt(remotePort), password,
                        proxyAddress, Integer.parseInt(proxyPort));
                ch.pipeline().addLast(
                        new MessageDecoder(), new MessageEncoder(),
                        new IdleStateHandler(60, 20, 0), clientHandler);
            }
        });
    }
}
