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
import com.scienjus.smartqq.model.Group;
import com.scienjus.smartqq.model.GroupMessage;
import com.scienjus.smartqq.model.Message;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.RandomUtils;
import org.b3log.latke.Latkes;
import org.b3log.latke.event.AbstractEventListener;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.cache.TagCache;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.processor.ChatRoomProcessor;
import org.b3log.symphony.processor.channel.ChatRoomChannel;
import org.b3log.symphony.service.TuringQueryService;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Sends an article to QQ qun.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.3.0.1, May 20, 2016
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
     * QQ groups.
     *
     * &lt;groupId, group&gt;
     */
    private static final Map<Long, Group> QQ_GROUPS = new HashMap<Long, Group>();

    /**
     * QQ client.
     */
    private static SmartQQClient qqClient;

    /**
     * Tag cache.
     */
    @Inject
    private TagCache tagCache;

    /**
     * Turing query service.
     */
    @Inject
    private TuringQueryService turingQueryService;

    /**
     * Initializes QQ client.
     */
    public void initQQClient() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                qqClient = new SmartQQClient(new MessageCallback() {
                    @Override
                    public void onMessage(final Message message) {
                        final String content = message.getContent();

                        final String key = Symphonys.get("qq.robotKey");
                        if (!StringUtils.startsWith(content, key)) {
                            return;
                        }

                        final String msg = StringUtils.substringAfter(content, key);
                        LOGGER.info("Received admin message: " + msg);
                        sendToQQGroups(msg);
                    }

                    @Override
                    public void onGroupMessage(final GroupMessage message) {
                        final long groupId = message.getGroupId();

                        if (QQ_GROUPS.isEmpty()) {
                            return;
                        }

                        final String content = message.getContent();

                        // Push to chat room
                        final String defaultAvatarURL = Symphonys.get("defaultThumbnailURL");
                        final JSONObject chatroomMsg = new JSONObject();
                        chatroomMsg.put(User.USER_NAME, Long.toHexString(message.getUserId()));
                        chatroomMsg.put(UserExt.USER_AVATAR_URL, defaultAvatarURL);
                        chatroomMsg.put(Common.CONTENT, "<p>" + content + "</p>");

                        ChatRoomChannel.notifyChat(chatroomMsg);
                        ChatRoomProcessor.messages.addFirst(chatroomMsg);

                        String msg = "";
                        if (StringUtils.contains(content, Symphonys.get("qq.robotName"))
                                || (StringUtils.length(content) > 6
                                && (StringUtils.contains(content, "?") || StringUtils.contains(content, "？") || StringUtils.contains(content, "问")))) {
                            msg = answer(content);

                            LOGGER.info(content + ": " + msg);
                        }

                        if (StringUtils.isNotBlank(msg)) {
                            sendMessageToGroup(groupId, msg);
                        }
                    }

                    private String answer(final String content) {
                        String keyword = "";
                        final int pageSize = Symphonys.getInt("latestArticlesCnt");
                        final List<JSONObject> tags = tagCache.getTags();
                        for (final JSONObject tag : tags) {
                            if (tag.optInt(Tag.TAG_REFERENCE_CNT) < pageSize) {
                                continue;
                            }

                            final String tagTitle = tag.optString(Tag.TAG_TITLE);

                            if (StringUtils.containsIgnoreCase(content, tagTitle)) {
                                keyword = tagTitle;

                                break;
                            }
                        }

                        String ret = "";
                        if (StringUtils.isNotBlank(keyword)) {
                            try {
                                ret = "这里可能有该问题的答案： " + Latkes.getServePath() + "/search?key="
                                        + URLEncoder.encode(keyword, "UTF-8");
                            } catch (final UnsupportedEncodingException e) {
                                LOGGER.log(Level.ERROR, "Search key encoding failed", e);
                            }
                        } else if (StringUtils.contains(content, Symphonys.get("qq.robotName"))) {
                            ret = turingQueryService.chat("Vanessa", content);
                        }

                        return ret;
                    }

                    @Override
                    public void onDiscussMessage(final DiscussMessage message) {
                    }
                });

                // Load groups
                final List<Group> groups = qqClient.getGroupList();
                for (final Group group : groups) {
                    QQ_GROUPS.put(group.getId(), group);

                    LOGGER.info(group.getName() + ": " + group.getId());
                }
            }
        }).start();
    }

    /**
     * Closes QQ client.
     */
    public void closeQQClient() {
        if (null == qqClient) {
            return;
        }

        try {
            qqClient.close();
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Closes QQ client failed", e);
        }
    }

    @Override
    public void action(final Event<JSONObject> event) throws EventException {
        final JSONObject data = event.getData();
        LOGGER.log(Level.DEBUG, "Processing an event[type={0}, data={1}] in listener[className={2}]",
                new Object[]{event.getType(), data, ArticleQQSender.class.getName()});

        if (null == qqClient) {
            return;
        }

        try {
            final JSONObject article = data.getJSONObject(Article.ARTICLE);
            final int articleType = article.optInt(Article.ARTICLE_TYPE);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType || Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
                return;
            }

            final String title = article.optString(Article.ARTICLE_TITLE);
            final String permalink = article.optString(Article.ARTICLE_PERMALINK);
            final String msg = title + " " + Latkes.getServePath() + permalink;
            sendToDefaultPushQQGroups(msg);

        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends the article to QQ group error", e);
        }
    }

    /**
     * Sends the specified article to QQ groups.
     *
     * @param msg the specified message
     */
    public static void sendToDefaultPushQQGroups(final String msg) {
        final String defaultGroupsConf = Symphonys.get("qq.robotDefaultPushGroups");
        if (StringUtils.isBlank(defaultGroupsConf)) {
            return;
        }

        final String[] groups = defaultGroupsConf.split(",");
        for (final Entry<Long, Group> entry : QQ_GROUPS.entrySet()) {
            final Group group = entry.getValue();
            final String name = group.getName();

            if (Strings.contains(name, groups)) {
                sendMessageToGroup(group.getId(), msg);
            }
        }
    }

    /**
     * Sends the specified message to QQ groups.
     *
     * @param msg the specified message
     */
    private void sendToQQGroups(final String msg) {
        for (final Entry<Long, Group> entry : QQ_GROUPS.entrySet()) {
            final Group group = entry.getValue();
            sendMessageToGroup(group.getId(), msg);
        }
    }

    private static void sendMessageToGroup(final Long groupId, final String msg) {
        final Group group = QQ_GROUPS.get(groupId);

        if (null == group) {
            // Reload groups

            final List<Group> groups = qqClient.getGroupList();
            QQ_GROUPS.clear();

            for (final Group g : groups) {
                QQ_GROUPS.put(g.getId(), g);

                LOGGER.info(g.getName() + ": " + g.getId());
            }
        }

        LOGGER.info("Pushing [msg=" + msg + "] to QQ qun [" + group.getName() + "]");
        qqClient.sendMessageToGroup(groupId, msg);
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
