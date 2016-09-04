package org.b3log.symphony;

import org.b3log.latke.Latkes;

/**
 * Sym with embedded Jetty.
 *
 * <ul>
 * <li>Windows: java -cp WEB-INF/lib/*;WEB-INF/classes org.b3log.symphony.Starter</li>
 * <li>Unix-like: java -cp WEB-INF/lib/*:WEB-INF/classes org.b3log.symphony.Starter</li>
 * </ul>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 4, 2016
 * @since 1.6.0
 */
public final class Starter {

    /**
     * Main.
     *
     * @param args the specified arguments
     * @throws Exception if start failed
     */
    public static void main(final String[] args) throws Exception {
        Latkes.boot(Starter.class, args);
    }

    /**
     * Private constructor.
     */
    private Starter() {
    }
}
