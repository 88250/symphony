/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.b3log.symphony;

import org.apache.commons.cli.*;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.processor.channel.*;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.log.Log;
import org.eclipse.jetty.util.log.Slf4jLog;
import org.eclipse.jetty.webapp.WebAppContext;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;

import java.io.File;

/**
 * Sym with embedded Jetty.
 * <ul>
 * <li>Windows: java -cp "WEB-INF/lib/*;WEB-INF/classes" org.b3log.symphony.Starter</li>
 * <li>Unix-like: java -cp "WEB-INF/lib/*:WEB-INF/classes" org.b3log.symphony.Starter</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Mar 26, 2019
 * @since 3.4.8
 */
public final class Starter {

    static {
        try {
            Log.setLog(new Slf4jLog());
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Private constructor.
     */
    private Starter() {
    }

    /**
     * Main.
     *
     * @param args the specified arguments
     * @throws Exception if start failed
     */
    public static void main(final String[] args) throws Exception {
        final Logger logger = Logger.getLogger(Starter.class);

        final Options options = new Options();
        final Option listenPortOpt = Option.builder("lp").longOpt("listen_port").argName("LISTEN_PORT")
                .hasArg().desc("listen port, default is 8080").build();
        options.addOption(listenPortOpt);

        final Option serverSchemeOpt = Option.builder("ss").longOpt("server_scheme").argName("SERVER_SCHEME")
                .hasArg().desc("browser visit protocol, default is http").build();
        options.addOption(serverSchemeOpt);

        final Option serverHostOpt = Option.builder("sh").longOpt("server_host").argName("SERVER_HOST")
                .hasArg().desc("browser visit domain name, default is localhost").build();
        options.addOption(serverHostOpt);

        final Option serverPortOpt = Option.builder("sp").longOpt("server_port").argName("SERVER_PORT")
                .hasArg().desc("browser visit port, default is 8080").build();
        options.addOption(serverPortOpt);

        final Option staticServerSchemeOpt = Option.builder("sss").longOpt("static_server_scheme").argName("STATIC_SERVER_SCHEME")
                .hasArg().desc("browser visit static resource protocol, default is http").build();
        options.addOption(staticServerSchemeOpt);

        final Option staticServerHostOpt = Option.builder("ssh").longOpt("static_server_host").argName("STATIC_SERVER_HOST")
                .hasArg().desc("browser visit static resource domain name, default is localhost").build();
        options.addOption(staticServerHostOpt);

        final Option staticServerPortOpt = Option.builder("ssp").longOpt("static_server_port").argName("STATIC_SERVER_PORT")
                .hasArg().desc("browser visit static resource port, default is 8080").build();
        options.addOption(staticServerPortOpt);

        final Option runtimeModeOpt = Option.builder("rm").longOpt("runtime_mode").argName("RUNTIME_MODE")
                .hasArg().desc("runtime mode (DEVELOPMENT/PRODUCTION), default is DEVELOPMENT").build();
        options.addOption(runtimeModeOpt);

        options.addOption("h", "help", false, "print help for the command");

        final HelpFormatter helpFormatter = new HelpFormatter();
        final CommandLineParser commandLineParser = new DefaultParser();
        CommandLine commandLine;

        final boolean isWindows = System.getProperty("os.name").toLowerCase().contains("windows");
        final String cmdSyntax = isWindows ? "java -cp \"WEB-INF/lib/*;WEB-INF/classes\" org.b3log.symphony.Starter"
                : "java -cp \"WEB-INF/lib/*:WEB-INF/classes\" org.b3log.symphony.Starter";
        final String header = "\nSym 是一款用 Java 实现的现代化社区（论坛/BBS/社交网络/博客）平台。\n\n";
        final String footer = "\n提需求或报告缺陷请到项目网站: https://github.com/b3log/symphony\n\n";
        try {
            commandLine = commandLineParser.parse(options, args);
        } catch (final ParseException e) {
            helpFormatter.printHelp(cmdSyntax, header, options, footer, true);

            return;
        }

        if (commandLine.hasOption("h")) {
            helpFormatter.printHelp(cmdSyntax, header, options, footer, true);

            return;
        }

        String portArg = commandLine.getOptionValue("listen_port");
        if (!Strings.isNumeric(portArg)) {
            portArg = "8080";
        }

        try {
            Latkes.init();
        } catch (final Exception e) {
            logger.log(Level.ERROR, "Latke init failed, please configure latke.props or run with args, visit https://hacpai.com/article/1492881378588 for more details");

            System.exit(-1);
        }

        String serverScheme = commandLine.getOptionValue("server_scheme");
        if (null != serverScheme) {
            Latkes.setLatkeProperty("serveScheme", serverScheme);
        }
        String serverHost = commandLine.getOptionValue("server_host");
        if (null != serverHost) {
            Latkes.setLatkeProperty("serverHost", serverHost);
        }
        String serverPort = commandLine.getOptionValue("server_port");
        if (null != serverPort) {
            Latkes.setLatkeProperty("serverPort", serverPort);
        }
        String staticServerScheme = commandLine.getOptionValue("static_server_scheme");
        if (null != staticServerScheme) {
            Latkes.setLatkeProperty("staticServerScheme", staticServerScheme);
        }
        String staticServerHost = commandLine.getOptionValue("static_server_host");
        if (null != staticServerHost) {
            Latkes.setLatkeProperty("staticServerHost", staticServerHost);
        }
        String staticServerPort = commandLine.getOptionValue("static_server_port");
        if (null != staticServerPort) {
            Latkes.setLatkeProperty("staticServerPort", staticServerPort);
        }
        String runtimeMode = commandLine.getOptionValue("runtime_mode");
        if (null != runtimeMode) {
            Latkes.setRuntimeMode(Latkes.RuntimeMode.valueOf(runtimeMode));
        }

        String webappDirLocation = "src/main/webapp/"; // POM structure in dev env
        final File file = new File(webappDirLocation);
        if (!file.exists()) {
            webappDirLocation = "."; // production environment
        }

        final int port = Integer.valueOf(portArg);
        final Server server = new Server(port);
        final WebAppContext root = new WebAppContext();
        root.setParentLoaderPriority(true); // Use parent class loader
        root.setContextPath("/");
        root.setDescriptor(webappDirLocation + "/WEB-INF/web.xml");
        root.setResourceBase(webappDirLocation);
        server.setHandler(root);
        try {
            final ServerContainer container = WebSocketServerContainerInitializer.configureContext(root);
            container.addEndpoint(ArticleChannel.class);
            container.addEndpoint(ArticleListChannel.class);
            container.addEndpoint(ChatroomChannel.class);
            container.addEndpoint(GobangChannel.class);
            container.addEndpoint(UserChannel.class);

            server.start();
        } catch (final Exception e) {
            logger.log(Level.ERROR, "Server start failed", e);

            System.exit(-1);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                server.stop();
            } catch (final Exception e) {
                logger.log(Level.ERROR, "Server stop failed", e);

                System.exit(-1);
            }
        }));

        server.join();
    }
}
