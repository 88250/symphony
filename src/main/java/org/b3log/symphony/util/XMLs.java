/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
package org.b3log.symphony.util;

import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringReader;
import java.io.StringWriter;

/**
 * XML utilities.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Jul 5, 2018
 * @since 3.1.0
 */
public final class XMLs {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(XMLs.class);

    /**
     * Returns pretty print of the specified xml string.
     *
     * @param xml the specified xml string
     * @return the pretty print of the specified xml string
     */
    public static String format(final String xml) {
        try {
            final DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            final Document doc = db.parse(new InputSource(new StringReader(xml)));
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            final StreamResult result = new StreamResult(new StringWriter());
            final DOMSource source = new DOMSource(doc);
            transformer.transform(source, result);

            return result.getWriter().toString();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "format pretty XML failed", e);

            return xml;
        }
    }

    /**
     * Private constructor.
     */
    private XMLs() {
    }
}
