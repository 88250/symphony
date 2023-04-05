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
package org.b3log.symphony.processor;

import org.b3log.latke.Keys;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Domain;
import org.b3log.symphony.util.Sessions;
import org.json.JSONObject;

import java.util.List;
import java.util.Map;


public class ArticleProcessorHelper {
    ArticleProcessor articleProcessorr =new ArticleProcessor();
    /**
     * Fills the domains with tags.
     *
     * @param dataModel the specified data model
     */
    public void fillDomainsWithTags(final Map<String, Object> dataModel) {
        final List<JSONObject> domains = articleProcessorr.domainQueryService.getAllDomains();
        dataModel.put(Common.ADD_ARTICLE_DOMAINS, domains);
        for (final JSONObject domain : domains) {
            final List<JSONObject> tags = articleProcessorr.domainQueryService.getTags(domain.optString(Keys.OBJECT_ID));

            domain.put(Domain.DOMAIN_T_TAGS, (Object) tags);
        }

        final JSONObject user = Sessions.getUser();
        if (null == user) {
            return;
        }

        try {
            final JSONObject followingTagsResult = articleProcessorr.followQueryService.getFollowingTags(
                    user.optString(Keys.OBJECT_ID), 1, 28);
            final List<JSONObject> followingTags = (List<JSONObject>) followingTagsResult.opt(Keys.RESULTS);
            if (!followingTags.isEmpty()) {
                final JSONObject userWatched = new JSONObject();
                userWatched.put(Keys.OBJECT_ID, String.valueOf(System.currentTimeMillis()));
                userWatched.put(Domain.DOMAIN_TITLE, articleProcessorr.langPropsService.get("notificationFollowingLabel"));
                userWatched.put(Domain.DOMAIN_T_TAGS, (Object) followingTags);

                domains.add(0, userWatched);
            }
        } catch (final Exception e) {
           e.printStackTrace();
        }
    }


}
