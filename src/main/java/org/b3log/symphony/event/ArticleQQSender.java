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
package org.b3log.symphony.event;

import com.scienjus.smartqq.callback.MessageCallback;
import com.scienjus.smartqq.client.SmartQQClient;
import com.scienjus.smartqq.model.DiscussMessage;
import com.scienjus.smartqq.model.GroupMessage;
import com.scienjus.smartqq.model.Message;
import javax.inject.Named;
import javax.inject.Singleton;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Sends an article to QQ qun.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, May 16, 2016
 * @since 1.4.0
 */
@Named
@Singleton
public class ArticleQQSender extends AbstractEventListener<JSONObject> {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleQQSender.class.getName());

    /**
     * QQ group id.
     */
    private static final Long QQ_GROUP_ID = Symphonys.getLong("qq.groupId");

    /**
     * QQ client.
     */
    private static SmartQQClient QQ_CLIENT = null;

    /**
     * Initializes QQ client.
     */
    public static void initQQClient() {
        if (null == QQ_GROUP_ID) {
            return;
        }

        QQ_CLIENT = new SmartQQClient(new MessageCallback() {
            @Override
            public void onMessage(final Message message) {
            }

            @Override
            public void onGroupMessage(GroupMessage message) {
            }

            @Override
            public void onDiscussMessage(DiscussMessage message) {
            }
        });
    }

    /**
     * Closes QQ client.
     */
    public static void closeQQClient() {
        if (null == QQ_CLIENT) {
            return;
        }

        try {
            QQ_CLIENT.close();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Closes QQ client failed", e);
        }
    }

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER
                .log(Level.DEBUG, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                        new Object[]{event.getType(), data, ArticleQQSender.class
                            .getName()});

        if (null == QQ_CLIENT) {
            return;
        }

        try {
            final JSONObject article = data.getJSONObject(Article.ARTICLE);
            final int articleType = article.optInt(Article.ARTICLE_TYPE);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType || Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
                return;
            }

            sendToQQGroup(article);

        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the article to QQ group error", e);
        }
    }

    /**
     * Sends the specified article to QQ group.
     *
     * @param article the specified article
     */
    public static void sendToQQGroup(final JSONObject article) {
        final String title = article.optString(Article.ARTICLE_TITLE);
        final String permalink = article.optString(Article.ARTICLE_PERMALINK);

        QQ_CLIENT.sendMessageToGroup(QQ_GROUP_ID, title + " " + permalink);
    }

    /**
     * Gets the event type {@linkplain EventTypes#ADD_ARTICLE}.
     *
     * @return event type
     */
    @Override
    public String getEventType() {
        return EventTypes.ADD_ARTICLE;
    }
}
