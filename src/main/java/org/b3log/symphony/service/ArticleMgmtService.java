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
package org.b3log.symphony.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.b3log.latke.Keys;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
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
import org.b3log.symphony.util.Symphonys;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Article management service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.1.5, Dec 26, 2012
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
     * Increments the view count of the specified article by the given article id.
     * 
     * @param articleId the given article id
     * @throws ServiceException service exception 
     */
    public void incArticleViewCount(final String articleId) throws ServiceException {
        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final JSONObject article = articleRepository.get(articleId);
            if (null == article) {
                return;
            }

            final int viewCnt = article.optInt(Article.ARTICLE_VIEW_CNT);
            article.put(Article.ARTICLE_VIEW_CNT, viewCnt + 1);

            articleRepository.update(articleId, article);

            transaction.commit();
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Incs an article view count failed", e);
            throw new ServiceException(e);
        }
    }

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
     *     "isBroadcast": boolean
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
            final JSONObject author = userRepository.get(authorId);
            final long currentTimeMillis = System.currentTimeMillis();
            if (currentTimeMillis - author.optLong(UserExt.USER_LATEST_ARTICLE_TIME) < Symphonys.getLong("minStepArticleTime")) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                LOGGER.log(Level.WARN, "Adds article too frequent [userName={0}]", author.optString(User.USER_NAME));
                throw new ServiceException(langPropsService.get("tooFrequentArticleLabel"));
            }

            final String clientArticleId = requestJSONObject.optString(Article.ARTICLE_CLIENT_ARTICLE_ID, ret);
            final boolean fromClient = requestJSONObject.has(Article.ARTICLE_CLIENT_ARTICLE_ID);
            final boolean isBroadcast = requestJSONObject.optBoolean(Article.ARTICLE_T_IS_BROADCAST);

            article.put(Article.ARTICLE_TITLE, requestJSONObject.optString(Article.ARTICLE_TITLE));
            article.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));
            if (fromClient) {
                // The article content security has been processed by Rhythm
                article.put(Article.ARTICLE_CONTENT, requestJSONObject.optString(Article.ARTICLE_CONTENT));
            } else {
                article.put(Article.ARTICLE_CONTENT, requestJSONObject.optString(Article.ARTICLE_CONTENT).
                        replace("<", "&lt;").replace(">", "&gt;")
                        .replace("&lt;pre&gt;", "<pre>").replace("&lt;/pre&gt;", "</pre>"));

            }
            article.put(Article.ARTICLE_EDITOR_TYPE, requestJSONObject.optString(Article.ARTICLE_EDITOR_TYPE));
            article.put(Article.ARTICLE_AUTHOR_EMAIL, requestJSONObject.optString(Article.ARTICLE_AUTHOR_EMAIL));
            article.put(Article.ARTICLE_SYNC_TO_CLIENT, fromClient ? true : requestJSONObject.optBoolean(Article.ARTICLE_SYNC_TO_CLIENT));
            article.put(Article.ARTICLE_AUTHOR_ID, authorId);
            article.put(Article.ARTICLE_COMMENT_CNT, 0);
            article.put(Article.ARTICLE_VIEW_CNT, 0);
            article.put(Article.ARTICLE_GOOD_CNT, 0);
            article.put(Article.ARTICLE_BAD_CNT, 0);
            article.put(Article.ARTICLE_COMMENTABLE, true);
            article.put(Article.ARTICLE_CREATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_UPDATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_LATEST_CMT_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_PERMALINK, "/article/" + ret);
            if (isBroadcast) {
                article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, "aBroadcast");
            } else {
                article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, clientArticleId);
            }
            article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());
            article.put(Article.ARTICLE_STATUS, 0);
            

            tag(article.optString(Article.ARTICLE_TAGS).split(","), article, author);

            final JSONObject articleCntOption = optionRepository.get(Option.ID_C_STATISTIC_ARTICLE_COUNT);
            final int articleCnt = articleCntOption.optInt(Option.OPTION_VALUE);
            articleCntOption.put(Option.OPTION_VALUE, articleCnt + 1);
            optionRepository.update(Option.ID_C_STATISTIC_ARTICLE_COUNT, articleCntOption);

            author.put(UserExt.USER_ARTICLE_COUNT, author.optInt(UserExt.USER_ARTICLE_COUNT) + 1);
            author.put(UserExt.USER_LATEST_ARTICLE_TIME, currentTimeMillis);
            // Updates user article count (and new tag count), latest article time
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            articleRepository.add(article);

            transaction.commit();

            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(Article.ARTICLE, article);
            try {
                eventManager.fireEventSynchronously(new Event<JSONObject>(EventTypes.ADD_ARTICLE, eventData));
            } catch (final EventException e) {
                LOGGER.log(Level.ERROR, e.getMessage(), e);
            }

            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Adds an article failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Updates an article with the specified request json object.
     * 
     * @param requestJSONObject the specified request json object, for example,
     * <pre>
     * {
     *     "oId": "",
     *     "articleTitle": "",
     *     "articleTags": "",
     *     "articleContent": "",
     *     "articleEditorType": ""
     * }
     * </pre>, see {@link Article} for more details
     * @throws ServiceException service exception
     */
    public void updateArticle(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final String articleId = requestJSONObject.getString(Keys.OBJECT_ID);
            final JSONObject oldArticle = articleRepository.get(articleId);
            final String authorId = oldArticle.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userRepository.get(authorId);

            processTagsForArticleUpdate(oldArticle, requestJSONObject, author);
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            final boolean fromClient = requestJSONObject.has(Article.ARTICLE_CLIENT_ARTICLE_ID);

            oldArticle.put(Article.ARTICLE_TITLE, requestJSONObject.optString(Article.ARTICLE_TITLE));
            oldArticle.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));
            if (fromClient) {
                // The article content security has been processed by Rhythm
                oldArticle.put(Article.ARTICLE_CONTENT, requestJSONObject.optString(Article.ARTICLE_CONTENT));
            } else {
                oldArticle.put(Article.ARTICLE_CONTENT, requestJSONObject.optString(Article.ARTICLE_CONTENT).
                        replace("<", "&lt;").replace(">", "&gt;")
                        .replace("&lt;pre&gt;", "<pre>").replace("&lt;/pre&gt;", "</pre>"));
            }

            oldArticle.put(Article.ARTICLE_UPDATE_TIME, System.currentTimeMillis());

            articleRepository.update(articleId, oldArticle);

            transaction.commit();

            final JSONObject eventData = new JSONObject();
            eventData.put(Common.FROM_CLIENT, fromClient);
            eventData.put(Article.ARTICLE, oldArticle);
            try {
                eventManager.fireEventSynchronously(new Event<JSONObject>(EventTypes.UPDATE_ARTICLE, eventData));
            } catch (final EventException e) {
                LOGGER.log(Level.ERROR, e.getMessage(), e);
            }
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates an article failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Processes tags for article update.
     *
     * <ul> 
     *   <li>Un-tags old article, decrements tag reference count</li>
     *   <li>Removes old article-tag relations</li> 
     *   <li>Saves new article-tag relations with tag reference count</li>
     * </ul>
     *
     * @param oldArticle the specified old article
     * @param newArticle the specified new article
     * @param author the specified author
     * @throws Exception exception
     */
    private void processTagsForArticleUpdate(final JSONObject oldArticle, final JSONObject newArticle, final JSONObject author)
            throws Exception {
        final String oldArticleId = oldArticle.getString(Keys.OBJECT_ID);
        final List<JSONObject> oldTags = tagRepository.getByArticleId(oldArticleId);
        final String tagsString = newArticle.getString(Article.ARTICLE_TAGS);
        String[] tagStrings = tagsString.split(",");
        final List<JSONObject> newTags = new ArrayList<JSONObject>();

        for (int i = 0; i < tagStrings.length; i++) {
            final String tagTitle = tagStrings[i].trim();
            JSONObject newTag = tagRepository.getByTitle(tagTitle);

            if (null == newTag) {
                newTag = new JSONObject();
                newTag.put(Tag.TAG_TITLE, tagTitle);
            }
            newTags.add(newTag);
        }

        final List<JSONObject> tagsDropped = new ArrayList<JSONObject>();
        final List<JSONObject> tagsNeedToAdd = new ArrayList<JSONObject>();

        for (final JSONObject newTag : newTags) {
            final String newTagTitle = newTag.getString(Tag.TAG_TITLE);

            if (!tagExists(newTagTitle, oldTags)) {
                LOGGER.log(Level.DEBUG, "Tag need to add[title={0}]", newTagTitle);
                tagsNeedToAdd.add(newTag);
            }
        }
        for (final JSONObject oldTag : oldTags) {
            final String oldTagTitle = oldTag.getString(Tag.TAG_TITLE);

            if (!tagExists(oldTagTitle, newTags)) {
                LOGGER.log(Level.DEBUG, "Tag dropped[title={0}]", oldTag);
                tagsDropped.add(oldTag);
            }
        }

        final int articleCmtCnt = oldArticle.getInt(Article.ARTICLE_COMMENT_CNT);

        for (final JSONObject tagDropped : tagsDropped) {
            final String tagId = tagDropped.getString(Keys.OBJECT_ID);
            final int refCnt = tagDropped.getInt(Tag.TAG_REFERENCE_CNT);

            tagDropped.put(Tag.TAG_REFERENCE_CNT, refCnt - 1);
            final int tagCmtCnt = tagDropped.getInt(Tag.TAG_COMMENT_CNT);
            tagDropped.put(Tag.TAG_COMMENT_CNT, tagCmtCnt - articleCmtCnt);

            tagRepository.update(tagId, tagDropped);
        }

        final String[] tagIdsDropped = new String[tagsDropped.size()];

        for (int i = 0; i < tagIdsDropped.length; i++) {
            final JSONObject tag = tagsDropped.get(i);
            final String id = tag.getString(Keys.OBJECT_ID);

            tagIdsDropped[i] = id;
        }

        if (0 != tagIdsDropped.length) {
            removeTagArticleRelations(oldArticleId, tagIdsDropped);
        }

        tagStrings = new String[tagsNeedToAdd.size()];
        for (int i = 0; i < tagStrings.length; i++) {
            final JSONObject tag = tagsNeedToAdd.get(i);
            final String tagTitle = tag.getString(Tag.TAG_TITLE);

            tagStrings[i] = tagTitle;
        }

        newArticle.put(Article.ARTICLE_COMMENT_CNT, articleCmtCnt);
        tag(tagStrings, newArticle, author);
    }

    /**
     * Removes tag-article relations by the specified article id and tag ids of the relations to be removed.
     *
     * <p>
     * Removes all relations if not specified the tag ids.
     * </p>
     *
     * @param articleId the specified article id
     * @param tagIds the specified tag ids of the relations to be removed
     * @throws JSONException json exception
     * @throws RepositoryException repository exception
     */
    private void removeTagArticleRelations(final String articleId, final String... tagIds)
            throws JSONException, RepositoryException {
        final List<String> tagIdList = Arrays.asList(tagIds);
        final List<JSONObject> tagArticleRelations = tagArticleRepository.getByArticleId(articleId);

        for (int i = 0; i < tagArticleRelations.size(); i++) {
            final JSONObject tagArticleRelation = tagArticleRelations.get(i);
            String relationId;

            if (tagIdList.isEmpty()) { // Removes all if un-specified
                relationId = tagArticleRelation.getString(Keys.OBJECT_ID);
                tagArticleRepository.remove(relationId);
            } else {
                if (tagIdList.contains(tagArticleRelation.getString(Tag.TAG + "_" + Keys.OBJECT_ID))) {
                    relationId = tagArticleRelation.getString(Keys.OBJECT_ID);
                    tagArticleRepository.remove(relationId);
                }
            }
        }
    }

    /**
     * Determines whether the specified tag title exists in the specified tags.
     *
     * @param tagTitle the specified tag title
     * @param tags the specified tags
     * @return {@code true} if it exists, {@code false} otherwise
     * @throws JSONException json exception
     */
    private static boolean tagExists(final String tagTitle, final List<JSONObject> tags) throws JSONException {
        for (final JSONObject tag : tags) {
            if (tag.getString(Tag.TAG_TITLE).equals(tagTitle)) {
                return true;
            }
        }

        return false;
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
            final int articleCmtCnt = article.optInt(Article.ARTICLE_COMMENT_CNT);

            if (null == tag) {
                LOGGER.log(Level.TRACE, "Found a new tag[title={0}] in article[title={1}]",
                        new Object[]{tagTitle, article.optString(Article.ARTICLE_TITLE)});
                tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, tagTitle);
                tag.put(Tag.TAG_REFERENCE_CNT, 1);
                tag.put(Tag.TAG_COMMENT_CNT, articleCmtCnt);
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
                LOGGER.log(Level.TRACE, "Found a existing tag[title={0}, id={1}] in article[title={2}]",
                        new Object[]{tag.optString(Tag.TAG_TITLE), tag.optString(Keys.OBJECT_ID),
                    article.optString(Article.ARTICLE_TITLE)});
                final JSONObject tagTmp = new JSONObject();
                tagTmp.put(Keys.OBJECT_ID, tagId);
                tagTmp.put(Tag.TAG_TITLE, tag.optString(Tag.TAG_TITLE));
                tagTmp.put(Tag.TAG_COMMENT_CNT, tag.optInt(Tag.TAG_COMMENT_CNT) + articleCmtCnt);
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
