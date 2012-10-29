/*
 * Copyright (c) 2012, B3log Team
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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.util.Ids;
import org.b3log.symphony.event.EventTypes;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.OptionRepository;
import org.b3log.symphony.repository.TagArticleRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.repository.UserTagRepository;
import org.json.JSONObject;

/**
 * Article management service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.7, Oct 29, 2012
 * @since 0.2.0
 */
public final class ArticleMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleMgmtService.class.getName());
    /**
     * Singleton.
     */
    private static final ArticleMgmtService SINGLETON = new ArticleMgmtService();
    /**
     * Article repository.
     */
    private ArticleRepository articleRepository = ArticleRepository.getInstance();
    /**
     * Tag repository.
     */
    private TagRepository tagRepository = TagRepository.getInstance();
    /**
     * Tag-Article repository.
     */
    private TagArticleRepository tagArticleRepository = TagArticleRepository.getInstance();
    /**
     * User repository.
     */
    private UserRepository userRepository = UserRepository.getInstance();
    /**
     * User-Tag repository.
     */
    private UserTagRepository userTagRepository = UserTagRepository.getInstance();
    /**
     * Option repository.
     */
    private OptionRepository optionRepository = OptionRepository.getInstance();
    /**
     * Event manager.
     */
    private EventManager eventManager = EventManager.getInstance();
    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();

    /**
     * Adds an article with the specified request json object.
     * 
     * @param requestJSONObject the specified request json object, for example,
     * <pre>
     * {
     *     "articleTitle": "",
     *     "articleTags": "",
     *     "articleContent": "",
     *     "articleEditorType": "",
     *     "articleAuthorEmail": "",
     *     "articleAuthorId": "",
     *     "syncWithSymphonyClient": boolean, // optional
     *     "clientArticleId": "" // optional
     * }
     * </pre>, see {@link Article} for more details
     * @return generated article id
     * @throws ServiceException service exception
     */
    public String addArticle(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final String ret = Ids.genTimeMillisId();
            final JSONObject article = new JSONObject();
            article.put(Keys.OBJECT_ID, ret);

            final String authorId = requestJSONObject.optString(Article.ARTICLE_AUTHOR_ID);
            final String clientArticleId = requestJSONObject.optString(Article.ARTICLE_CLIENT_ARTICLE_ID, ret);
            final boolean fromClient = requestJSONObject.has(Article.ARTICLE_CLIENT_ARTICLE_ID);

            article.put(Article.ARTICLE_TITLE, requestJSONObject.optString(Article.ARTICLE_TITLE));
            article.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));
            if (fromClient) {
                // The article content security has been processed by Rhythm
                article.put(Article.ARTICLE_CONTENT, requestJSONObject.optString(Article.ARTICLE_CONTENT));
            } else {
                article.put(Article.ARTICLE_CONTENT, requestJSONObject.optString(Article.ARTICLE_CONTENT)
                        .replace("<", "&lt;").replace(ret, ret).replace(">", "&gt;"));
            }
            article.put(Article.ARTICLE_EDITOR_TYPE, requestJSONObject.optString(Article.ARTICLE_EDITOR_TYPE));
            article.put(Article.ARTICLE_AUTHOR_EMAIL, requestJSONObject.optString(Article.ARTICLE_AUTHOR_EMAIL));
            article.put(Article.ARTICLE_SYNC_TO_CLIENT, fromClient ? true : requestJSONObject.optBoolean(Article.ARTICLE_SYNC_TO_CLIENT));
            article.put(Article.ARTICLE_AUTHOR_ID, authorId);

            final long currentTimeMillis = System.currentTimeMillis();

            article.put(Article.ARTICLE_COMMENT_CNT, 0);
            article.put(Article.ARTICLE_VIEW_CNT, 0);
            article.put(Article.ARTICLE_GOOD_CNT, 0);
            article.put(Article.ARTICLE_BAD_CNT, 0);
            article.put(Article.ARTICLE_COMMENTABLE, true);
            article.put(Article.ARTICLE_CREATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_UPDATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_LATEST_CMT_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_PERMALINK, "/article/" + ret);
            article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());
            article.put(Article.ARTICLE_STATUS, 0);
            article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, clientArticleId);

            final JSONObject author = userRepository.get(authorId);

            tag(article.optString(Article.ARTICLE_TAGS).split(","), article, author);

            final JSONObject articleCntOption = optionRepository.get(Option.ID_C_STATISTIC_ARTICLE_COUNT);
            final int articleCnt = articleCntOption.optInt(Option.OPTION_VALUE);
            articleCntOption.put(Option.OPTION_VALUE, articleCnt + 1);
            optionRepository.update(Option.ID_C_STATISTIC_ARTICLE_COUNT, articleCntOption); // Updates global tag/article count

            author.put(UserExt.USER_ARTICLE_COUNT, author.optInt(UserExt.USER_ARTICLE_COUNT) + 1);
            userRepository.update(author.optString(Keys.OBJECT_ID), author); // Updates user article count (and new tag count) 

            articleRepository.add(article);

            transaction.commit();

            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(Article.ARTICLE, article);
            try {
                eventManager.fireEventSynchronously(new Event<JSONObject>(EventTypes.ADD_ARTICLE, eventData));
            } catch (final EventException e) {
                LOGGER.log(Level.SEVERE, e.getMessage(), e);
            }

            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.SEVERE, "Adds an article failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Tags the specified article with the specified tag titles.
     *
     * @param tagTitles the specified tag titles
     * @param article the specified article
     * @param author the specified author
     * @throws RepositoryException repository exception
     */
    private void tag(final String[] tagTitles, final JSONObject article, final JSONObject author) throws RepositoryException {
        for (int i = 0; i < tagTitles.length; i++) {
            final String tagTitle = tagTitles[i].trim();
            JSONObject tag = tagRepository.getByTitle(tagTitle);
            String tagId;
            int userTagType;

            if (null == tag) {
                LOGGER.log(Level.FINEST, "Found a new tag[title={0}] in article[title={1}]",
                           new Object[]{tagTitle, article.optString(Article.ARTICLE_TITLE)});
                tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, tagTitle);
                tag.put(Tag.TAG_REFERENCE_CNT, 1);
                tag.put(Tag.TAG_COMMENT_CNT, 0);
                tag.put(Tag.TAG_DESCRIPTION, "");
                tag.put(Tag.TAG_ICON_PATH, "");
                tag.put(Tag.TAG_STATUS, 0);

                tagId = tagRepository.add(tag);
                tag.put(Keys.OBJECT_ID, tagId);
                userTagType = 0; // Creator

                final JSONObject tagCntOption = optionRepository.get(Option.ID_C_STATISTIC_TAG_COUNT);
                final int tagCnt = tagCntOption.optInt(Option.OPTION_VALUE);
                tagCntOption.put(Option.OPTION_VALUE, tagCnt + 1);
                optionRepository.update(Option.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

                author.put(UserExt.USER_TAG_COUNT, author.optInt(UserExt.USER_TAG_COUNT) + 1);
            } else {
                tagId = tag.optString(Keys.OBJECT_ID);
                LOGGER.log(Level.FINEST, "Found a existing tag[title={0}, id={1}] in article[title={2}]",
                           new Object[]{tag.optString(Tag.TAG_TITLE), tag.optString(Keys.OBJECT_ID),
                                        article.optString(Article.ARTICLE_TITLE)});
                final JSONObject tagTmp = new JSONObject();
                tagTmp.put(Keys.OBJECT_ID, tagId);
                tagTmp.put(Tag.TAG_TITLE, tagTitle);
                tagTmp.put(Tag.TAG_COMMENT_CNT, tag.optInt(Tag.TAG_COMMENT_CNT));
                tagTmp.put(Tag.TAG_STATUS, tag.optInt(Tag.TAG_STATUS));
                tagTmp.put(Tag.TAG_REFERENCE_CNT, tag.optInt(Tag.TAG_REFERENCE_CNT) + 1);
                tagTmp.put(Tag.TAG_DESCRIPTION, tag.optString(Tag.TAG_DESCRIPTION));
                tagTmp.put(Tag.TAG_ICON_PATH, tag.optString(Tag.TAG_ICON_PATH));

                tagRepository.update(tagId, tagTmp);

                userTagType = 1; // Article
            }

            // Tag-Article relation
            final JSONObject tagArticleRelation = new JSONObject();
            tagArticleRelation.put(Tag.TAG + "_" + Keys.OBJECT_ID, tagId);
            tagArticleRelation.put(Article.ARTICLE + "_" + Keys.OBJECT_ID, article.optString(Keys.OBJECT_ID));
            tagArticleRepository.add(tagArticleRelation);

            // User-Tag relation
            final JSONObject userTagRelation = new JSONObject();
            userTagRelation.put(Tag.TAG + '_' + Keys.OBJECT_ID, tagId);
            userTagRelation.put(User.USER + '_' + Keys.OBJECT_ID, article.optString(Article.ARTICLE_AUTHOR_ID));
            userTagRelation.put(Common.TYPE, userTagType);
            userTagRepository.add(userTagRelation);
        }
    }

    /**
     * Gets the {@link UserMgmtService} singleton.
     *
     * @return the singleton
     */
    public static ArticleMgmtService getInstance() {
        return SINGLETON;
    }

    /**
     * Private constructor.
     */
    private ArticleMgmtService() {
    }
}
