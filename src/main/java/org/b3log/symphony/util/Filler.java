package org.b3log.symphony.util;

import java.util.Map;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.symphony.model.Common;

/**
 * Filler utilities.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.0, Aug 10, 2012
 * @since 0.2.0
 */
public final class Filler {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(Filler.class.getName());

    /**
     * Fills header.ftl.
     * 
     * @param datModel the specified data model
     */
    public static void fillHeader(final Map<String, Object> datModel) {
        fillMinified(datModel);
        Keys.fillServer(datModel);
    }

    /**
     * Fills minified directory and file postfix for static JavaScript, CSS.
     * 
     * @param dataModel the specified data model
     */
    private static void fillMinified(final Map<String, Object> dataModel) {
        switch (Latkes.getRuntimeMode()) {
            case DEVELOPMENT:
                dataModel.put(Common.MINI_POSTFIX, "");
                break;
            case PRODUCTION:
                dataModel.put(Common.MINI_POSTFIX, Common.MINI_POSTFIX_VALUE);
                break;
            default:
                throw new AssertionError();
        }
    }
}
