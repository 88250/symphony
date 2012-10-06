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
import org.b3log.latke.model.User;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Statistic;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.StatisticRepository;
import org.b3log.symphony.repository.TagArticleRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.repository.UserTagRepository;
import org.json.JSONObject;

/**
 * Article management service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.2, Oct 6, 2012
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
     * User-Tag repository.
     */
    private UserTagRepository userTagRepository = UserTagRepository.getInstance();
    /**
     * Statistic repository.
     */
    private StatisticRepository statisticRepository = StatisticRepository.getInstance();
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
     *     "articleAuthorId": ""
     * }
     * </pre>,see {@link Article} for more details
     * @return generated article id
     * @throws ServiceException service exception
     */
    public String addArticle(final JSONObject requestJSONObject) throws ServiceException {
        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final JSONObject article = new JSONObject();

            article.put(Article.ARTICLE_TITLE, requestJSONObject.optString(Article.ARTICLE_TITLE));
            article.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));
            article.put(Article.ARTICLE_CONTENT, requestJSONObject.optString(Article.ARTICLE_CONTENT));
            article.put(Article.ARTICLE_EDITOR_TYPE, requestJSONObject.optString(Article.ARTICLE_EDITOR_TYPE));
            article.put(Article.ARTICLE_AUTHOR_EMAIL, requestJSONObject.optString(Article.ARTICLE_AUTHOR_EMAIL));
            article.put(Article.ARTICLE_AUTHOR_ID, requestJSONObject.optString(Article.ARTICLE_AUTHOR_ID));

            final long currentTimeMillis = System.currentTimeMillis();

            article.put(Article.ARTICLE_COMMENT_CNT, 0);
            article.put(Article.ARTICLE_VIEW_CNT, 0);
            article.put(Article.ARTICLE_COMMENTABLE, true);
            article.put(Article.ARTICLE_CREATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_UPDATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_LATEST_CMT_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_PERMALINK, "TODO permalink");
            article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());
            article.put(Article.ARTICLE_STATUS, 0);

            final JSONObject statistic = statisticRepository.get(Statistic.STATISTIC);
            tag(article.optString(Article.ARTICLE_TAGS).split(","), article, statistic);

            statistic.put(Statistic.STATISTIC_ARTICLE_COUNT, statistic.optInt(Statistic.STATISTIC_ARTICLE_COUNT) + 1);

            statisticRepository.update(Statistic.STATISTIC, statistic);
            articleRepository.add(article);

            transaction.commit();

            return article.optString(Keys.OBJECT_ID);
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.SEVERE, "Adds a user failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Tags the specified article with the specified tag titles.
     *
     * @param tagTitles the specified tag titles
     * @param article the specified article
     * @param statistic the specified statistic
     * @throws RepositoryException repository exception
     */
    private void tag(final String[] tagTitles, final JSONObject article, final JSONObject statistic) throws RepositoryException {
        for (int i = 0; i < tagTitles.length; i++) {
            final String tagTitle = tagTitles[i].trim();
            JSONObject tag = tagRepository.getByTitle(tagTitle);
            String tagId;
            String userTagType;

            if (null == tag) {
                LOGGER.log(Level.FINEST, "Found a new tag[title={0}] in article[title={1}]",
                        new Object[]{tagTitle, article.optString(Article.ARTICLE_TITLE)});
                tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, tagTitle);
                tag.put(Tag.TAG_REFERENCE_CNT, 1);
                tag.put(Tag.TAG_COMMENT_CNT, 0);
                tag.put(Tag.TAG_STATUS, 0);

                tagId = tagRepository.add(tag);
                tag.put(Keys.OBJECT_ID, tagId);
                userTagType = "creator";

                statistic.put(Statistic.STATISTIC_TAG_COUNT, statistic.optInt(Statistic.STATISTIC_TAG_COUNT) + 1);
            } else {
                tagId = tag.optString(Keys.OBJECT_ID);
                LOGGER.log(Level.FINEST, "Found a existing tag[title={0}, id={1}] in article[title={2}]",
                        new Object[]{tag.optString(Tag.TAG_TITLE), tag.optString(Keys.OBJECT_ID), article.optString(Article.ARTICLE_TITLE)});
                final JSONObject tagTmp = new JSONObject();
                tagTmp.put(Keys.OBJECT_ID, tagId);
                tagTmp.put(Tag.TAG_TITLE, tagTitle);
                tagTmp.put(Tag.TAG_COMMENT_CNT, tag.optInt(Tag.TAG_COMMENT_CNT));
                tagTmp.put(Tag.TAG_STATUS, tag.optInt(Tag.TAG_STATUS));
                tagTmp.put(Tag.TAG_REFERENCE_CNT, tag.optInt(Tag.TAG_REFERENCE_CNT) + 1);

                tagRepository.update(tagId, tagTmp);

                userTagType = "article";
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
