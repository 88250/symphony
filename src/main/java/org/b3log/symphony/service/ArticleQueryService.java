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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.CompositeFilter;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.Filter;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.MD5;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.TagArticleRepository;
import org.b3log.symphony.repository.TagRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Article query service.
 *
 * @author <a href="mailto:DL88250@gmail.com">Liang Ding</a>
 * @version 1.0.0.3, Oct 18, 2012
 * @since 0.2.0
 */
public final class ArticleQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleQueryService.class.getName());
    /**
     * Singleton.
     */
    private static final ArticleQueryService SINGLETON = new ArticleQueryService();
    /**
     * Article repository.
     */
    private ArticleRepository articleRepository = ArticleRepository.getInstance();
    /**
     * Tag-Article repository.
     */
    private TagArticleRepository tagArticleRepository = TagArticleRepository.getInstance();
    /**
     * Tag repository.
     */
    private TagRepository tagRepository = TagRepository.getInstance();
    /**
     * User repository.
     */
    private UserRepository userRepository = UserRepository.getInstance();
    /**
     * Comment query service.
     */
    private CommentQueryService commentQueryService = CommentQueryService.getInstance();
    /**
     * User query service.
     */
    private UserQueryService userQueryService = UserQueryService.getInstance();
    /**
     * Count to fetch article tags for relevant articles.
     */
    private static final int RELEVANT_ARTICLE_RANDOM_FETCH_TAG_CNT = 3;
    /**
     * Language service.
     */
    private LangPropsService langPropsService = LangPropsService.getInstance();

    /**
     * Gets the relevant articles of the specified article with the specified fetch size.
     * 
     * <p>
     * The relevant articles exist the same tag with the specified article.
     * </p>
     * 
     * @param article the specified article
     * @param fetchSize the specified fetch size
     * @return relevant articles, returns an empty list if not found
     * @throws ServiceException service exception 
     */
    public List<JSONObject> getRelevantArticles(final JSONObject article, final int fetchSize) throws ServiceException {
        final String tagsString = article.optString(Article.ARTICLE_TAGS);
        final String[] tagTitles = tagsString.split(",");
        final int tagTitlesLength = tagTitles.length;
        final int subCnt = tagTitlesLength > RELEVANT_ARTICLE_RANDOM_FETCH_TAG_CNT
                ? RELEVANT_ARTICLE_RANDOM_FETCH_TAG_CNT : tagTitlesLength;

        final List<Integer> tagIdx = CollectionUtils.getRandomIntegers(0, tagTitlesLength, subCnt);
        final int subFetchSize = fetchSize / subCnt;
        final Set<String> fetchedArticleIds = new HashSet<String>();

        final List<JSONObject> ret = new ArrayList<JSONObject>();
        try {
            for (int i = 0; i < tagIdx.size(); i++) {
                final String tagTitle = tagTitles[tagIdx.get(i)].trim();

                final JSONObject tag = tagRepository.getByTitle(tagTitle);
                final String tagId = tag.optString(Keys.OBJECT_ID);
                JSONObject result = tagArticleRepository.getByTagId(tagId, 1, subFetchSize);

                final JSONArray tagArticleRelations = result.optJSONArray(Keys.RESULTS);

                final Set<String> articleIds = new HashSet<String>();
                for (int j = 0; j < tagArticleRelations.length(); j++) {
                    final String articleId = tagArticleRelations.optJSONObject(j).optString(Article.ARTICLE + '_' + Keys.OBJECT_ID);

                    if (fetchedArticleIds.contains(articleId)) {
                        continue;
                    }

                    articleIds.add(articleId);
                    fetchedArticleIds.add(articleId);
                }

                final Query query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds));
                result = articleRepository.get(query);

                ret.addAll(CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS)));
            }

            organizeArticles(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets relevant articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets news (articles tags contains "B3log Announcement").
     * 
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return articles, return an empty list if not found
     * @throws ServiceException service exception 
     */
    public List<JSONObject> getNews(final int currentPageNum, final int pageSize) throws ServiceException {
        JSONObject tag = null;

        try {
            tag = tagRepository.getByTitle("B3log Announcement");
            if (null == tag) {
                return Collections.emptyList();
            }

            Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                    setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
                    .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

            JSONObject result = tagArticleRepository.get(query);
            final JSONArray tagArticleRelations = result.optJSONArray(Keys.RESULTS);

            final Set<String> articleIds = new HashSet<String>();
            for (int i = 0; i < tagArticleRelations.length(); i++) {
                articleIds.add(tagArticleRelations.optJSONObject(i).optString(Article.ARTICLE + '_' + Keys.OBJECT_ID));
            }

            final List<Filter> subFilters = new ArrayList<Filter>();
            subFilters.add(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds));
            subFilters.add(new PropertyFilter(Article.ARTICLE_AUTHOR_EMAIL, FilterOperator.EQUAL, UserExt.DEFAULT_ADMIN_EMAIL));
            query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, subFilters))
                    .addProjection(Article.ARTICLE_TITLE, String.class).addProjection(Article.ARTICLE_PERMALINK, String.class)
                    .addProjection(Article.ARTICLE_CREATE_TIME, Long.class);
            result = articleRepository.get(query);

            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            for (final JSONObject article : ret) {
                article.put(Article.ARTICLE_PERMALINK, Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK));
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets articles by tag [tagTitle=" + tag.optString(Tag.TAG_TITLE) + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets articles by the specified tag.
     * 
     * @param tag the specified tag
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return articles, return an empty list if not found
     * @throws ServiceException service exception 
     */
    public List<JSONObject> getArticlesByTag(final JSONObject tag, final int currentPageNum, final int pageSize) throws ServiceException {
        try {
            Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                    setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
                    .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

            JSONObject result = tagArticleRepository.get(query);
            final JSONArray tagArticleRelations = result.optJSONArray(Keys.RESULTS);

            final Set<String> articleIds = new HashSet<String>();
            for (int i = 0; i < tagArticleRelations.length(); i++) {
                articleIds.add(tagArticleRelations.optJSONObject(i).optString(Article.ARTICLE + '_' + Keys.OBJECT_ID));
            }

            query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds));
            result = articleRepository.get(query);

            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(ret);

            final Integer participantsCnt = Symphonys.getInt("tagArticleParticipantsCnt");
            genParticipants(ret, participantsCnt);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets articles by tag [tagTitle=" + tag.optString(Tag.TAG_TITLE) + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets an article by the specified client article id.
     * 
     * @param clientArticleId the specified client article id
     * @return article, return {@code null} if not found
     * @throws ServiceException service exception 
     */
    public JSONObject getArticleByClientArticleId(final String clientArticleId) throws ServiceException {
        final Query query = new Query().
                setFilter(new PropertyFilter(Article.ARTICLE_CLIENT_ARTICLE_ID, FilterOperator.EQUAL, clientArticleId));
        try {
            final JSONObject result = articleRepository.get(query);
            final JSONArray array = result.optJSONArray(Keys.RESULTS);

            if (0 == array.length()) {
                return null;
            }

            return array.optJSONObject(0);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets article [clientArticleId=" + clientArticleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets an article by the specified id.
     * 
     * @param articleId the specified id
     * @return article, return {@code null} if not found
     * @throws ServiceException service exception 
     */
    public JSONObject getArticleById(final String articleId) throws ServiceException {
        try {
            final JSONObject ret = articleRepository.get(articleId);

            if (null == ret) {
                return null;
            }

            organizeArticle(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets article [articleId=" + articleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the user articles with the specified user id, page number and page size.
     * 
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return user articles, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getUserArticles(final String userId, final int currentPageNum, final int pageSize) throws ServiceException {
        final Query query = new Query().addSort(Article.ARTICLE_CREATE_TIME, SortDirection.DESCENDING)
                .setPageCount(currentPageNum).setPageSize(pageSize).
                setFilter(new PropertyFilter(Article.ARTICLE_AUTHOR_ID, FilterOperator.EQUAL, userId));
        try {
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets user articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the random articles with the specified fetch size.
     * 
     * @param fetchSize the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getRandomArticles(final int fetchSize) throws ServiceException {
        try {
            final List<JSONObject> ret = articleRepository.getRandomly(fetchSize);
            organizeArticles(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets random articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the recent (sort by create time) articles with the specified fetch size.
     * 
     * @param fetchSize the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getRecentArticles(final int fetchSize) throws ServiceException {
        final Query query = new Query().addSort(Article.ARTICLE_CREATE_TIME, SortDirection.DESCENDING)
                .setPageCount(1).setPageSize(fetchSize);

        try {
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets recent articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the latest comment articles with the specified fetch size.
     * 
     * @param currentPageNum the specified current page number
     * @param fetchSize the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getLatestCmtArticles(final int currentPageNum, final int fetchSize) throws ServiceException {
        final Query query = new Query().addSort(Article.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING)
                .setPageCount(1).setPageSize(fetchSize).setCurrentPageNum(currentPageNum);

        try {
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(ret);

            for (final JSONObject article : ret) {
                final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                final JSONObject author = userRepository.get(authorId);
                if (UserExt.USER_STATUS_C_INVALID == author.optInt(UserExt.USER_STATUS)) {
                    article.put(Article.ARTICLE_TITLE, langPropsService.get("articleTitleBlockLabel"));
                }
            }

            final Integer participantsCnt = Symphonys.getInt("latestCmtArticleParticipantsCnt");
            genParticipants(ret, participantsCnt);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.SEVERE, "Gets latest comment articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Organizes the specified articles.
     * 
     * <ul>
     *   <li>converts create/update/latest comment time (long) to date type</li>
     *   <li>generates author thumbnail URL</li>
     *   <li>generates author name</li>
     *   <li>escapes article  title &lt; and &gt;</li>
     * </ul>
     * 
     * @param articles the specified articles
     * @throws RepositoryException repository exception 
     */
    private void organizeArticles(final List<JSONObject> articles) throws RepositoryException {
        for (final JSONObject article : articles) {
            organizeArticle(article);
        }
    }

    /**
     * Organizes the specified article.
     * 
     * <ul>
     *   <li>converts create/update/latest comment time (long) to date type</li>
     *   <li>generates author thumbnail URL</li>
     *   <li>generates author name</li>
     *   <li>escapes article  title &lt; and &gt;</li>
     * </ul>
     * 
     * @param article the specified article
     * @throws RepositoryException repository exception 
     */
    private void organizeArticle(final JSONObject article) throws RepositoryException {
        toArticleDate(article);
        genArticleAuthor(article);

        final String title = article.optString(Article.ARTICLE_TITLE).replace("<", "&lt;").replace(">", "&gt;");
        article.put(Article.ARTICLE_TITLE, title);
    }

    /**
     * Converts the specified article create/update/latest comment time (long) to date type.
     * 
     * @param article the specified article
     */
    private void toArticleDate(final JSONObject article) {
        article.put(Article.ARTICLE_CREATE_TIME, new Date(article.optLong(Article.ARTICLE_CREATE_TIME)));
        article.put(Article.ARTICLE_UPDATE_TIME, new Date(article.optLong(Article.ARTICLE_UPDATE_TIME)));
        article.put(Article.ARTICLE_LATEST_CMT_TIME, new Date(article.optLong(Article.ARTICLE_LATEST_CMT_TIME)));
    }

    /**
     * Generates the specified article author name and thumbnail URL.
     * 
     * @param article the specified article
     * @throws RepositoryException repository exception 
     */
    private void genArticleAuthor(final JSONObject article) throws RepositoryException {
        final String authorEmail = article.optString(Article.ARTICLE_AUTHOR_EMAIL);
        final String thumbnailURL = "http://secure.gravatar.com/avatar/" + MD5.hash(authorEmail) + "?s=140&d="
                + Latkes.getStaticServePath() + "/images/user-thumbnail.png";

        article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL, thumbnailURL);

        final JSONObject author = userRepository.getByEmail(authorEmail);
        article.put(Article.ARTICLE_T_AUTHOR_NAME, author.optString(User.USER_NAME));
    }

    /**
     * Gets the {@link ArticleQueryService} singleton.
     *
     * @return the singleton
     */
    public static ArticleQueryService getInstance() {
        return SINGLETON;
    }

    /**
     * Private constructor.
     */
    private ArticleQueryService() {
    }

    /**
     * Generates participants for the specified articles.
     * 
     * @param articles the specified articles
     * @param participantsCnt the specified generate size
     * @throws ServiceException service exception
     */
    private void genParticipants(final List<JSONObject> articles, final Integer participantsCnt) throws ServiceException {
        for (final JSONObject article : articles) {
            final String participantName = "";
            final String participantThumbnailURL = "";

            final List<JSONObject> articleParticipants =
                    commentQueryService.getArticleLatestParticipants(article.optString(Keys.OBJECT_ID), participantsCnt);
            article.put(Article.ARTICLE_T_PARTICIPANTS, (Object) articleParticipants);

            article.put(Article.ARTICLE_T_PARTICIPANT_NAME, participantName);
            article.put(Article.ARTICLE_T_PARTICIPANT_THUMBNAIL_URL, participantThumbnailURL);
        }
    }

    /**
     * Processes the specified article content.
     * 
     * <ul>
     *   <li>Generates &#64;username home URL</li>
     *   <li>Markdowns</li>
     *   <li>Generates secured article content</li>
     *   <li>Blocks the article if need</li>
     * </ul>
     * 
     * @param article the specified article, for example,
     * <pre>
     * {
     *     "articleTitle": "",
     *     ...., 
     *     "author": {}
     * }
     * </pre>
     * @throws ServiceException service exception 
     */
    public void processArticleContent(final JSONObject article) throws ServiceException {
        final JSONObject author = article.optJSONObject(Article.ARTICLE_T_AUTHOR);
        if (UserExt.USER_STATUS_C_INVALID == author.optInt(UserExt.USER_STATUS)
                || Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
            article.put(Article.ARTICLE_TITLE, langPropsService.get("articleTitleBlockLabel"));
            article.put(Article.ARTICLE_CONTENT, langPropsService.get("articleContentBlockLabel"));

            return;
        }

        String articleContent = article.optString(Article.ARTICLE_CONTENT);
        try {
            final Set<String> userNames = userQueryService.getUserNames(articleContent);
            for (final String userName : userNames) {
                articleContent = articleContent.replace('@' + userName,
                        "@<a href='/member/" + userName + "'>" + userName + "</a>");
            }
        } catch (final ServiceException e) {
            final String errMsg = "Generates @username home URL for comment content failed";
            LOGGER.log(Level.SEVERE, errMsg, e);
            throw new ServiceException(errMsg);
        }

        article.put(Article.ARTICLE_CONTENT, articleContent);

        markdown(article);
    }

    /**
     * Markdowns the specified article content.
     * 
     * <ul>
     *   <li>Markdowns article content</li>
     *   <li>Generates secured article content</li>
     * </ul>
     * 
     * @param article the specified article content
     */
    private void markdown(final JSONObject article) {
        String content = "Markdown Error";

        try {
            content = Markdowns.toHTML(article.optString(Article.ARTICLE_CONTENT));
            content = Markdowns.clean(content, Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK));
        } catch (final Exception e) {
            LOGGER.log(Level.SEVERE, "Markdown failed", e);
        }

        article.put(Article.ARTICLE_CONTENT, content);
    }
}
