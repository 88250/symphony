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
package org.b3log.symphony.service;

import java.util.LinkedList;
import java.util.List;
import javax.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.channel.TimelineChannel;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Timeline management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jul 22, 2016
 * @since 1.3.0
 */
@Service
public class TimelineMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(TimelineMgmtService.class.getName());

    /**
     * Timelines.
     */
    private LinkedList<JSONObject> timelines = new LinkedList<JSONObject>();

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Adds the specified timeline.
     *
     * @param timeline the specified timeline, for example,      <pre>
     * {
     *     "userId": "",
     *     "type": "article",
     *     "content": timelineArticleLabel
     * }
     * </pre>
     */
    public void addTimeline(final JSONObject timeline) {
        String userId = timeline.optString(Common.USER_ID);
        try {
            final JSONObject user = userRepository.get(userId);

            if (UserExt.USER_XXX_STATUS_C_PUBLIC != user.optInt(UserExt.USER_TIMELINE_STATUS)) {
                return;
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets user [userId=" + userId + "] failed", e);
        }

        TimelineChannel.notifyTimeline(timeline);

        timelines.addFirst(timeline);

        final int maxCnt = Symphonys.getInt("timelineCnt");

        if (timelines.size() > maxCnt) {
            timelines.remove(maxCnt);
        }
    }

    /**
     * Gets timelines.
     *
     * @return timelines
     */
    public List<JSONObject> getTimelines() {
        return timelines;
    }
}
