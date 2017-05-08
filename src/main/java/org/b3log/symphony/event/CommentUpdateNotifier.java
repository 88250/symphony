/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.event;

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.ioc.inject.Named;
import org.b3log.latke.ioc.inject.Singleton;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.service.ShortLinkQueryService;
import org.b3log.symphony.service.TimelineMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Markdowns;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.safety.Whitelist;

/**
 * Sends comment update related notifications.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 6, 2017
 * @since 2.1.0
 */
@Named
@Singleton
public class CommentUpdateNotifier extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(CommentUpdateNotifier.class);

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Timeline management service.
     */
    @Inject
    private TimelineMgmtService timelineMgmtService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.TRACE, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                event.getType(), data, CommentUpdateNotifier.class.getName());

        try {
            final JSONObject originalArticle = data.getJSONObject(Article.ARTICLE);
            final JSONObject originalComment = data.getJSONObject(Comment.COMMENT);

            final boolean isDiscussion = originalArticle.optInt(Article.ARTICLE_TYPE) == Article.ARTICLE_TYPE_C_DISCUSSION;
            if (isDiscussion) {
                return;
            }

            if (Comment.COMMENT_ANONYMOUS_C_PUBLIC != originalComment.optInt(Comment.COMMENT_ANONYMOUS)) {
                return;
            }

            final String commenterId = originalComment.optString(Comment.COMMENT_AUTHOR_ID);
            final JSONObject commenter = userQueryService.getUser(commenterId);
            final String commenterName = commenter.optString(User.USER_NAME);
            final String commentContent = originalComment.optString(Comment.COMMENT_CONTENT);

            String cc = shortLinkQueryService.linkArticle(commentContent);
            cc = shortLinkQueryService.linkTag(cc);
            cc = Emotions.convert(cc);
            cc = Markdowns.toHTML(cc);
            cc = Markdowns.clean(cc, "");

            // Timeline
            String articleTitle = Jsoup.parse(originalArticle.optString(Article.ARTICLE_TITLE)).text();
            articleTitle = Emotions.convert(articleTitle);
            final String articlePermalink = Latkes.getServePath() + originalArticle.optString(Article.ARTICLE_PERMALINK);

            final JSONObject timeline = new JSONObject();
            timeline.put(Common.USER_ID, commenterId);
            timeline.put(Common.TYPE, Comment.COMMENT);
            String content = langPropsService.get("timelineCommentUpdateLabel");
            content = content.replace("{user}", "<a target='_blank' rel='nofollow' href='"
                    + Latkes.getServePath() + "/member/" + commenterName + "'>" + commenterName + "</a>");
            content = content.replace("{article}", "<a target='_blank' rel='nofollow' href='"
                    + articlePermalink + "'>" + articleTitle + "</a>").
                    replace("{comment}", cc.replaceAll("<p>", "").replaceAll("</p>", ""));

            content = Jsoup.clean(content, Whitelist.none().addAttributes("a", "href", "rel", "target"));
            timeline.put(Common.CONTENT, content);

            if (StringUtils.isNotBlank(content)) {
                timelineMgmtService.addTimeline(timeline);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the comment update notification failed", e);
        }
    }

    /**
     * Gets the event type {@linkplain EventTypes#UPDATE_COMMENT}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.UPDATE_COMMENT;
    }
}
