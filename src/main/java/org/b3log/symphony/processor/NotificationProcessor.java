/*
 * Copyright (c) 2009, 2010, 2011, 2012, 2013, B3log Team
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.servlet.renderer.freemarker.FreeMarkerRenderer;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.NotificationQueryService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Notification processor.
 * 
 * <ul> 
 *   <li>Displays comments of an article (/notifications/commented), GET</li> 
 * </ul> 
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 1, 2013
 * @since 0.2.5
 */
@RequestProcessor
public class NotificationProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(NotificationProcessor.class.getName());

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Notification query service.
     */
    @Inject
    private NotificationQueryService notificationQueryService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Shows comments of an article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param userName the specified user name
     * @throws Exception exception
     */
    @RequestProcessing(value = "/notifications/commented", method = HTTPRequestMethod.GET)
    public void showCommentedNotifications(final HTTPRequestContext context, final HttpServletRequest request,
            final HttpServletResponse response,
            final String userName) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new FreeMarkerRenderer();
        context.setRenderer(renderer);
        renderer.setTemplateName("/home/notifications/commented.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);

        final int pageSize = Symphonys.getInt("userHomeCmtsCnt");
        final int windowSize = Symphonys.getInt("userHomeCmtsWindowSize");

        final List<JSONObject> commentedNotifications = new ArrayList<JSONObject>();
        for (int i = 0; i < Integer.valueOf("15"); i++) {
            final JSONObject commented = new JSONObject();
            commented.put("content", "xxx 评论");
            commented.put(Comment.COMMENT_T_AUTHOR_THUMBNAIL_URL, "commentAuthorThumbnailURL");
            commented.put(Comment.COMMENT_T_ARTICLE_TITLE, "xxx 文章");
            commented.put(Comment.COMMENT_T_ARTICLE_PERMALINK, "xxx 文章链接");
            commented.put(Comment.COMMENT_CREATE_TIME, new Date());

            commentedNotifications.add(commented);
        }

        dataModel.put("commenteds", commentedNotifications);

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, Integer.valueOf("2"));
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, Integer.valueOf("5"));

        filler.fillHeader(request, response, dataModel);
        filler.fillFooter(dataModel);
    }
}
