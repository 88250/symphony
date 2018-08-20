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
package org.b3log.symphony.model.feed;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.time.DateFormatUtils;

import java.util.Date;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Item.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.1, Aug 20, 2018
 * @since 3.1.0
 */
public final class RSSItem {

    /**
     * Start title element.
     */
    private static final String START_TITLE_ELEMENT = "<title>";

    /**
     * End title element.
     */
    private static final String END_TITLE_ELEMENT = "</title>";

    /**
     * Start link element.
     */
    private static final String START_LINK_ELEMENT = "<link>";

    /**
     * End link element.
     */
    private static final String END_LINK_ELEMENT = "</link>";

    /**
     * Start description element.
     */
    private static final String START_DESCRIPTION_ELEMENT = "<description>";

    /**
     * End summary element.
     */
    private static final String END_DESCRIPTION_ELEMENT = "</description>";

    /**
     * Start author element.
     */
    private static final String START_AUTHOR_ELEMENT = "<author>";

    /**
     * End author element.
     */
    private static final String END_AUTHOR_ELEMENT = "</author>";

    /**
     * Categories.
     */
    private Set<RSSCategory> categories = new HashSet<>();

    /**
     * Start guid element.
     */
    private static final String START_GUID_ELEMENT = "<guid>";

    /**
     * End guid element.
     */
    private static final String END_GUID_ELEMENT = "</guid>";

    /**
     * Start pubDate element.
     */
    private static final String START_PUB_DATE_ELEMENT = "<pubDate>";

    /**
     * End pubDate element.
     */
    private static final String END_PUB_DATE_ELEMENT = "</pubDate>";

    /**
     * Guid.
     */
    private String guid;

    /**
     * Publish date.
     */
    private Date pubDate;

    /**
     * Title.
     */
    private String title;

    /**
     * Description.
     */
    private String description;

    /**
     * Link.
     */
    private String link;

    /**
     * Author.
     */
    private String author;

    /**
     * Gets the GUID.
     *
     * @return GUID
     */
    public String getGUID() {
        return guid;
    }

    /**
     * Sets the GUID with the specified GUID.
     *
     * @param guid the specified GUID
     */
    public void setGUID(final String guid) {
        this.guid = guid;
    }

    /**
     * Gets the author.
     *
     * @return author
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author with the specified author.
     *
     * @param author the specified author
     */
    public void setAuthor(final String author) {
        this.author = author;
    }

    /**
     * Gets the link.
     *
     * @return link
     */
    public String getLink() {
        return link;
    }

    /**
     * Sets the link with the specified link.
     *
     * @param link the specified link
     */
    public void setLink(final String link) {
        this.link = link;
    }

    /**
     * Gets the title.
     *
     * @return title
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title with the specified title.
     *
     * @param title the specified title
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * Gets publish date.
     *
     * @return publish date
     */
    public Date getPubDate() {
        return pubDate;
    }

    /**
     * Sets the publish date with the specified publish date.
     *
     * @param pubDate the specified publish date
     */
    public void setPubDate(final Date pubDate) {
        this.pubDate = pubDate;
    }

    /**
     * Gets the description.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description with the specified description.
     *
     * @param description the specified description
     */
    public void setDescription(final String description) {
        this.description = description;
    }

    /**
     * Adds the specified category.
     *
     * @param category the specified category
     */
    public void addCatetory(final RSSCategory category) {
        categories.add(category);
    }

    @Override
    public String toString() {
        final StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("<item>").append(START_TITLE_ELEMENT);
        stringBuilder.append(StringEscapeUtils.escapeXml(title));
        stringBuilder.append(END_TITLE_ELEMENT);

        stringBuilder.append(START_LINK_ELEMENT);
        stringBuilder.append(StringEscapeUtils.escapeXml(link));
        stringBuilder.append(END_LINK_ELEMENT);

        stringBuilder.append(START_DESCRIPTION_ELEMENT);
        stringBuilder.append("<![CDATA[" + description + "]]>");
        stringBuilder.append(END_DESCRIPTION_ELEMENT);

        stringBuilder.append(START_AUTHOR_ELEMENT);
        stringBuilder.append(StringEscapeUtils.escapeXml(author));
        stringBuilder.append(END_AUTHOR_ELEMENT);

        stringBuilder.append(START_GUID_ELEMENT);
        stringBuilder.append(StringEscapeUtils.escapeXml(guid));
        stringBuilder.append(END_GUID_ELEMENT);

        for (final RSSCategory category : categories) {
            stringBuilder.append(category.toString());
        }

        stringBuilder.append(START_PUB_DATE_ELEMENT);
        stringBuilder.append(DateFormatUtils.format(pubDate, "EEE, dd MMM yyyy HH:mm:ss Z", Locale.US));
        stringBuilder.append(END_PUB_DATE_ELEMENT).append("</item>");

        return stringBuilder.toString();
    }
}
