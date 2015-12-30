/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.b3log.symphony.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;

/**
 * Data channel via Flash WebSocket.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jun 20, 2015
 * @since 1.3.0
 */
@WebServlet(urlPatterns = "/policy-server", loadOnStartup = 2)
public class PolicyServerServlet extends HttpServlet {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PolicyServerServlet.class);

    /**
     * Server socket.
     */
    private static ServerSocket serverSock;

    /**
     * Is listening.
     */
    private static boolean listening = true;

    /**
     * Server thread.
     */
    private static Thread serverThread;

    static {
        try {
            serverThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        serverSock = new ServerSocket(843, 50);

                        while (listening) {
                            final Socket sock = serverSock.accept();

                            final Thread t = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        sock.setSoTimeout(10000);
                                        final InputStream in = sock.getInputStream();
                                        final byte[] buffer = new byte[23];

                                        if (in.read(buffer) != -1 && (new String(buffer)).startsWith("<policy-file-request/>")) {
                                            final File policyFile = new File(PolicyServerServlet.class.
                                                    getResource("/socketpolicy.xml").toURI());
                                            final BufferedReader fin = new BufferedReader(new FileReader(policyFile));
                                            final OutputStream out = sock.getOutputStream();

                                            String line;
                                            while ((line = fin.readLine()) != null) {
                                                out.write(line.getBytes());
                                            }

                                            fin.close();

                                            out.write(0x00);

                                            out.flush();
                                            out.close();
                                        } else {
                                            LOGGER.error("PolicyServerServlet: Ignoring Invalid Request");
                                        }
                                    } catch (final Exception ex) {
                                        LOGGER.log(Level.ERROR, "PolicyServerServlet error", ex);
                                    } finally {
                                        try {
                                            sock.close();
                                        } catch (final Exception ex2) {
                                            LOGGER.log(Level.ERROR, "PolicyServerServlet error" + ex2);
                                        }
                                    }
                                }
                            });
                            t.start();
                        }
                    } catch (final Exception ex) {
                        LOGGER.log(Level.ERROR, "PolicyServerServlet error" + ex);
                    }
                }
            });

            serverThread.start();

        } catch (final Exception ex) {
            LOGGER.log(Level.ERROR, "PolicyServerServlet error" + ex);
        }
    }

    @Override
    public void destroy() {
        if (listening) {
            listening = false;
        }

        if (!serverSock.isClosed()) {
            try {
                serverSock.close();
            } catch (final Exception ex) {
                LOGGER.log(Level.ERROR, "PolicyServerServlet error" + ex);
            }
        }
    }
}
