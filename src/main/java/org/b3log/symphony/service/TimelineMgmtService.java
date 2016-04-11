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
import org.b3log.latke.service.annotation.Service;
import org.b3log.symphony.processor.channel.TimelineChannel;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Timeline management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Aug 21, 2015
 * @since 1.3.0
 */
@Service
public class TimelineMgmtService {

    /**
     * Timelines.
     */
    private LinkedList<JSONObject> timelines = new LinkedList<JSONObject>();

    /**
     * Adds the specified timeline.
     *
     * @param timeline the specified timeline, for example,      <pre>
     * {
     *     "type": "article",
     *     "content": timelineArticleLabel
     * }
     * </pre>
     */
    public void addTimeline(final JSONObject timeline) {
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
