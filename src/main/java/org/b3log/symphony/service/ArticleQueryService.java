/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.b3log.symphony.service;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.inject.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.*;
import org.b3log.symphony.cache.ArticleCache;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.processor.channel.ArticleChannel;
import org.b3log.symphony.repository.*;
import org.b3log.symphony.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.*;

/**
 * Article query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 2.27.32.58, May 20, 2017
 * @since 0.2.0
 */
@Service
public class ArticleQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleQueryService.class);

    /**
     * Count to fetch article tags for relevant articles.
     */
    private static final int RELEVANT_ARTICLE_RANDOM_FETCH_TAG_CNT = 3;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Tag-Article repository.
     */
    @Inject
    private TagArticleRepository tagArticleRepository;

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Domain tag repository.
     */
    @Inject
    private DomainTagRepository domainTagRepository;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * User query service.
     */
    @Inject
    private UserQueryService userQueryService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Article cache.
     */
    @Inject
    private ArticleCache articleCache;

    /**
     * Gets following user articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return following tag articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getFollowingUserArticles(final int avatarViewMode, final String userId,
                                                     final int currentPageNum, final int pageSize) throws ServiceException {
        final List<JSONObject> users = (List<JSONObject>) followQueryService.getFollowingUsers(
                avatarViewMode, userId, 1, Integer.MAX_VALUE).opt(Keys.RESULTS);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        final Query query = new Query()
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(pageSize).setCurrentPageNum(currentPageNum);

        final List<String> followingUserIds = new ArrayList<>();
        for (final JSONObject user : users) {
            followingUserIds.add(user.optString(Keys.OBJECT_ID));
        }

        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID));
        filters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION));
        filters.add(new PropertyFilter(Article.ARTICLE_AUTHOR_ID, FilterOperator.IN, followingUserIds));
        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        query.addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_STICK, Long.class).
                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                addProjection(Article.ARTICLE_TITLE, String.class).
                addProjection(Article.ARTICLE_STATUS, Integer.class).
                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
                addProjection(Article.ARTICLE_TYPE, Integer.class).
                addProjection(Article.ARTICLE_PERMALINK, String.class).
                addProjection(Article.ARTICLE_TAGS, String.class).
                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
                addProjection(Article.ARTICLE_PERFECT, Integer.class).
                addProjection(Article.ARTICLE_CONTENT, String.class);

        JSONObject result = null;
        try {
            Stopwatchs.start("Query following user articles");

            result = articleRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets following user articles failed", e);

            throw new ServiceException(e);
        } finally {
            Stopwatchs.end();
        }

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(data);

        try {
            organizeArticles(avatarViewMode, ret);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Organizes articles failed", e);

            throw new ServiceException(e);
        }

        return ret;
    }

    /**
     * Gets following tag articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return following tag articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getFollowingTagArticles(final int avatarViewMode, final String userId,
                                                    final int currentPageNum, final int pageSize) throws ServiceException {
        final List<JSONObject> tags = (List<JSONObject>) followQueryService.getFollowingTags(
                userId, 1, Integer.MAX_VALUE).opt(Keys.RESULTS);
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }

        final Map<String, Class<?>> articleFields = new HashMap<>();
        articleFields.put(Keys.OBJECT_ID, String.class);
        articleFields.put(Article.ARTICLE_STICK, Long.class);
        articleFields.put(Article.ARTICLE_CREATE_TIME, Long.class);
        articleFields.put(Article.ARTICLE_UPDATE_TIME, Long.class);
        articleFields.put(Article.ARTICLE_LATEST_CMT_TIME, Long.class);
        articleFields.put(Article.ARTICLE_AUTHOR_ID, String.class);
        articleFields.put(Article.ARTICLE_TITLE, String.class);
        articleFields.put(Article.ARTICLE_STATUS, Integer.class);
        articleFields.put(Article.ARTICLE_VIEW_CNT, Integer.class);
        articleFields.put(Article.ARTICLE_TYPE, Integer.class);
        articleFields.put(Article.ARTICLE_PERMALINK, String.class);
        articleFields.put(Article.ARTICLE_TAGS, String.class);
        articleFields.put(Article.ARTICLE_LATEST_CMTER_NAME, String.class);
        articleFields.put(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class);
        articleFields.put(Article.ARTICLE_COMMENT_CNT, Integer.class);
        articleFields.put(Article.ARTICLE_ANONYMOUS, Integer.class);
        articleFields.put(Article.ARTICLE_PERFECT, Integer.class);
        articleFields.put(Article.ARTICLE_CONTENT, String.class);

        return getArticlesByTags(avatarViewMode, currentPageNum, pageSize, articleFields, tags.toArray(new JSONObject[0]));
    }

    /**
     * Gets the next article.
     *
     * @param articleId the specified article id
     * @return permalink and title, <pre>
     * {
     *     "articlePermalink": "",
     *     "articleTitle": "",
     *     "articleTitleEmoj": "",
     *     "articleTitleEmojUnicode": ""
     * }
     * </pre>, returns {@code null} if not found
     */
    public JSONObject getNextPermalink(final String articleId) {
        Stopwatchs.start("Get next");

        try {
            final Query query = new Query().setFilter(
                    new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN, articleId)).
                    addSort(Keys.OBJECT_ID, SortDirection.ASCENDING).
                    addProjection(Article.ARTICLE_PERMALINK, String.class).
                    addProjection(Article.ARTICLE_TITLE, String.class).
                    setCurrentPageNum(1).setPageCount(1).setPageSize(1);

            final JSONArray result = articleRepository.get(query).optJSONArray(Keys.RESULTS);
            if (0 == result.length()) {
                return null;
            }

            final JSONObject ret = result.optJSONObject(0);
            if (null == ret) {
                return null;
            }

            String title = ret.optString(Article.ARTICLE_TITLE);
            ret.put(Article.ARTICLE_T_TITLE_EMOJI, Emotions.convert(title));
            ret.put(Article.ARTICLE_T_TITLE_EMOJI_UNICODE, EmojiParser.parseToUnicode(title));

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets next article permalink failed", e);

            return null;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the previous article.
     *
     * @param articleId the specified article id
     * @return permalink and title, <pre>
     * {
     *     "articlePermalink": "",
     *     "articleTitle": "",
     *     "articleTitleEmoj": "",
     *     "articleTitleEmojUnicode": ""
     * }
     * </pre>, returns {@code null} if not found
     */
    public JSONObject getPreviousPermalink(final String articleId) {
        Stopwatchs.start("Get previous");

        try {
            final Query query = new Query().setFilter(
                    new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, articleId)).
                    addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                    addProjection(Article.ARTICLE_PERMALINK, String.class).
                    addProjection(Article.ARTICLE_TITLE, String.class).
                    setCurrentPageNum(1).setPageCount(1).setPageSize(1);

            final JSONArray result = articleRepository.get(query).optJSONArray(Keys.RESULTS);
            if (0 == result.length()) {
                return null;
            }

            final JSONObject ret = result.optJSONObject(0);
            if (null == ret) {
                return null;
            }

            String title = ret.optString(Article.ARTICLE_TITLE);
            ret.put(Article.ARTICLE_T_TITLE_EMOJI, Emotions.convert(title));
            ret.put(Article.ARTICLE_T_TITLE_EMOJI_UNICODE, EmojiParser.parseToUnicode(title));

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets previous article permalink failed", e);

            return null;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Get an articles by the specified title.
     *
     * @param title the specified title
     * @return article, returns {@code null} if not found
     */
    public JSONObject getArticleByTitle(final String title) {
        try {
            return articleRepository.getByTitle(title);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets article by title [" + title + "] failed", e);

            return null;
        }
    }

    /**
     * Gets article count of the specified day.
     *
     * @param day the specified day
     * @return article count
     */
    public int getArticleCntInDay(final Date day) {
        final long time = day.getTime();
        final long start = Times.getDayStartTime(time);
        final long end = Times.getDayEndTime(time);

        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
                new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID)
        ));

        try {
            return (int) articleRepository.count(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Count day article failed", e);

            return 1;
        }
    }

    /**
     * Gets article count of the specified month.
     *
     * @param day the specified month
     * @return article count
     */
    public int getArticleCntInMonth(final Date day) {
        final long time = day.getTime();
        final long start = Times.getMonthStartTime(time);
        final long end = Times.getMonthEndTime(time);

        final Query query = new Query().setFilter(CompositeFilterOperator.and(
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, start),
                new PropertyFilter(Keys.OBJECT_ID, FilterOperator.LESS_THAN, end),
                new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID)
        ));

        try {
            return (int) articleRepository.count(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Count month article failed", e);

            return 1;
        }
    }

    /**
     * Gets articles by the specified page number and page size.
     *
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @param types          the specified types
     * @return articles, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getValidArticles(final int currentPageNum, final int pageSize, final int... types) throws ServiceException {
        try {
            final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                    .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

            if (null != types && types.length > 0) {
                final List<Filter> typeFilters = new ArrayList<>();
                for (int i = 0; i < types.length; i++) {
                    final int type = types[i];

                    typeFilters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.EQUAL, type));
                }

                final CompositeFilter typeFilter = new CompositeFilter(CompositeFilterOperator.OR, typeFilters);
                final List<Filter> filters = new ArrayList<>();
                filters.add(typeFilter);
                filters.add(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID));

                query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
            } else {
                query.setFilter(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID));
            }

            final JSONObject result = articleRepository.get(query);

            return CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets domain articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param domainId       the specified domain id
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
     * @return result
     * @throws ServiceException service exception
     */
    public JSONObject getDomainArticles(final int avatarViewMode, final String domainId,
                                        final int currentPageNum, final int pageSize) throws ServiceException {
        final JSONObject ret = new JSONObject();
        ret.put(Article.ARTICLES, (Object) Collections.emptyList());

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, 0);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) Collections.emptyList());

        try {
            final JSONArray domainTags = domainTagRepository.getByDomainId(domainId, 1, Integer.MAX_VALUE)
                    .optJSONArray(Keys.RESULTS);

            if (domainTags.length() <= 0) {
                return ret;
            }

            final List<String> tagIds = new ArrayList<>();
            for (int i = 0; i < domainTags.length(); i++) {
                tagIds.add(domainTags.optJSONObject(i).optString(Tag.TAG + "_" + Keys.OBJECT_ID));
            }

            Query query = new Query().setFilter(
                    new PropertyFilter(Tag.TAG + "_" + Keys.OBJECT_ID, FilterOperator.IN, tagIds)).
                    setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                    addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
            JSONObject result = tagArticleRepository.get(query);
            final JSONArray tagArticles = result.optJSONArray(Keys.RESULTS);
            if (tagArticles.length() <= 0) {
                return ret;
            }

            final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

            final int windowSize = Symphonys.getInt("latestArticlesWindowSize");

            final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) pageNums);

            final Set<String> articleIds = new HashSet<>();
            for (int i = 0; i < tagArticles.length(); i++) {
                articleIds.add(tagArticles.optJSONObject(i).optString(Article.ARTICLE + "_" + Keys.OBJECT_ID));
            }

            query = new Query().setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds),
                    new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID))).
                    setPageCount(1).addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

            final List<JSONObject> articles
                    = CollectionUtils.<JSONObject>jsonArrayToList(articleRepository.get(query).optJSONArray(Keys.RESULTS));

            try {
                organizeArticles(avatarViewMode, articles);
            } catch (final RepositoryException e) {
                LOGGER.log(Level.ERROR, "Organizes articles failed", e);

                throw new ServiceException(e);
            }

            final Integer participantsCnt = Symphonys.getInt("latestArticleParticipantsCnt");
            genParticipants(avatarViewMode, articles, participantsCnt);

            ret.put(Article.ARTICLES, (Object) articles);

            return ret;
        } catch (final RepositoryException | ServiceException e) {
            LOGGER.log(Level.ERROR, "Gets domain articles error", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets the relevant articles of the specified article with the specified fetch size.
     * <p>
     * The relevant articles exist the same tag with the specified article.
     * </p>
     *
     * @param avatarViewMode the specified avatar view mode
     * @param article        the specified article
     * @param fetchSize      the specified fetch size
     * @return relevant articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getRelevantArticles(final int avatarViewMode, final JSONObject article, final int fetchSize)
            throws ServiceException {
        final String tagsString = article.optString(Article.ARTICLE_TAGS);
        final String[] tagTitles = tagsString.split(",");
        final int tagTitlesLength = tagTitles.length;
        final int subCnt = tagTitlesLength > RELEVANT_ARTICLE_RANDOM_FETCH_TAG_CNT
                ? RELEVANT_ARTICLE_RANDOM_FETCH_TAG_CNT : tagTitlesLength;

        final List<Integer> tagIdx = CollectionUtils.getRandomIntegers(0, tagTitlesLength, subCnt);
        final int subFetchSize = fetchSize / subCnt;
        final Set<String> fetchedArticleIds = new HashSet<>();

        final List<JSONObject> ret = new ArrayList<>();
        try {
            for (int i = 0; i < tagIdx.size(); i++) {
                final String tagTitle = tagTitles[tagIdx.get(i)].trim();

                final JSONObject tag = tagRepository.getByTitle(tagTitle);
                final String tagId = tag.optString(Keys.OBJECT_ID);
                JSONObject result = tagArticleRepository.getByTagId(tagId, 1, subFetchSize);

                final JSONArray tagArticleRelations = result.optJSONArray(Keys.RESULTS);

                final Set<String> articleIds = new HashSet<>();
                for (int j = 0; j < tagArticleRelations.length(); j++) {
                    final String articleId = tagArticleRelations.optJSONObject(j).optString(Article.ARTICLE + '_' + Keys.OBJECT_ID);

                    if (fetchedArticleIds.contains(articleId)) {
                        continue;
                    }

                    articleIds.add(articleId);
                    fetchedArticleIds.add(articleId);
                }

                articleIds.remove(article.optString(Keys.OBJECT_ID));

                final Query query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds));
                result = articleRepository.get(query);

                ret.addAll(CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS)));
            }

            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets relevant articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets broadcasts (articles permalink equals to "aBroadcast").
     *
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getBroadcasts(final int currentPageNum, final int pageSize) throws ServiceException {
        try {
            final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).setFilter(
                    new PropertyFilter(Article.ARTICLE_CLIENT_ARTICLE_ID, FilterOperator.EQUAL, "aBroadcast")).
                    addSort(Article.ARTICLE_CREATE_TIME, SortDirection.DESCENDING);

            final JSONObject result = articleRepository.get(query);
            final JSONArray articles = result.optJSONArray(Keys.RESULTS);

            if (0 == articles.length()) {
                return Collections.emptyList();
            }

            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(articles);
            for (final JSONObject article : ret) {
                article.put(Article.ARTICLE_PERMALINK, Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK));
                article.remove(Article.ARTICLE_CONTENT);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets broadcasts [currentPageNum=" + currentPageNum + ", pageSize=" + pageSize + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets interest articles.
     *
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified fetch size
     * @param tagTitles      the specified tag titles
     * @return articles, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getInterests(final int currentPageNum, final int pageSize, final String... tagTitles)
            throws ServiceException {
        try {
            final List<JSONObject> tagList = new ArrayList<>();
            for (final String tagTitle : tagTitles) {
                final JSONObject tag = tagRepository.getByTitle(tagTitle);
                if (null == tag) {
                    continue;
                }

                tagList.add(tag);
            }

            final Map<String, Class<?>> articleFields = new HashMap<>();
            articleFields.put(Article.ARTICLE_TITLE, String.class);
            articleFields.put(Article.ARTICLE_PERMALINK, String.class);
            articleFields.put(Article.ARTICLE_CREATE_TIME, Long.class);
            articleFields.put(Article.ARTICLE_AUTHOR_ID, String.class);

            final List<JSONObject> ret = new ArrayList<>();

            if (!tagList.isEmpty()) {
                final List<JSONObject> tagArticles
                        = getArticlesByTags(UserExt.USER_AVATAR_VIEW_MODE_C_STATIC,
                        currentPageNum, pageSize, articleFields, tagList.toArray(new JSONObject[0]));

                ret.addAll(tagArticles);
            }

            if (ret.size() < pageSize) {
                final List<Filter> filters = new ArrayList<>();
                filters.add(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID));
                filters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION));

                final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                        .setPageCount(currentPageNum).setPageSize(pageSize).setCurrentPageNum(1);
                query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
                for (final Map.Entry<String, Class<?>> articleField : articleFields.entrySet()) {
                    query.addProjection(articleField.getKey(), articleField.getValue());
                }

                final JSONObject result = articleRepository.get(query);

                final List<JSONObject> recentArticles = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
                ret.addAll(recentArticles);
            }

            final Iterator<JSONObject> iterator = ret.iterator();
            int i = 0;
            while (iterator.hasNext()) {
                final JSONObject article = iterator.next();
                article.put(Article.ARTICLE_PERMALINK, Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK));

                article.remove(Article.ARTICLE_T_AUTHOR);
                article.remove(Article.ARTICLE_AUTHOR_ID);
                article.remove(Article.ARTICLE_T_PARTICIPANTS);
                article.remove(Article.ARTICLE_T_PARTICIPANT_NAME);
                article.remove(Article.ARTICLE_T_PARTICIPANT_THUMBNAIL_URL);
                article.remove(Article.ARTICLE_LATEST_CMT_TIME);
                article.remove(Article.ARTICLE_LATEST_CMTER_NAME);
                article.remove(Article.ARTICLE_UPDATE_TIME);
                article.remove(Article.ARTICLE_T_HEAT);
                article.remove(Article.ARTICLE_T_TITLE_EMOJI);
                article.remove(Article.ARTICLE_T_TITLE_EMOJI_UNICODE);
                article.remove(Common.TIME_AGO);
                article.remove(Common.CMT_TIME_AGO);
                article.remove(Article.ARTICLE_T_TAG_OBJS);
                article.remove(Article.ARTICLE_STICK);
                article.remove(Article.ARTICLE_T_PREVIEW_CONTENT);
                article.remove(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "20");
                article.remove(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "48");
                article.remove(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "210");
                article.remove(Article.ARTICLE_T_STICK_REMAINS);

                long createTime = 0;
                final Object time = article.get(Article.ARTICLE_CREATE_TIME);
                if (time instanceof Date) {
                    createTime = ((Date) time).getTime();
                } else {
                    createTime = (Long) time;
                }
                article.put(Article.ARTICLE_CREATE_TIME, createTime);

                i++;
                if (i > pageSize) {
                    iterator.remove();
                }
            }

            return ret;
        } catch (final RepositoryException | ServiceException | JSONException e) {
            LOGGER.log(Level.ERROR, "Gets interests failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets news (perfect articles).
     *
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getNews(final int currentPageNum, final int pageSize) throws ServiceException {
        try {
            final Query query = new Query().
                    setFilter(new PropertyFilter(Article.ARTICLE_PERFECT, FilterOperator.EQUAL, Article.ARTICLE_PERFECT_C_PERFECT)).
                    addProjection(Article.ARTICLE_TITLE, String.class).
                    addProjection(Article.ARTICLE_PERMALINK, String.class).
                    addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                    addSort(Article.ARTICLE_CREATE_TIME, SortDirection.DESCENDING);
            final JSONObject result = articleRepository.get(query);

            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            for (final JSONObject article : ret) {
                article.put(Article.ARTICLE_PERMALINK, Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK));
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets news failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets articles by the specified tags (order by article create date desc).
     *
     * @param avatarViewMode the specified avatar view mode
     * @param tags           the specified tags
     * @param currentPageNum the specified page number
     * @param articleFields  the specified article fields to return
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getArticlesByTags(final int avatarViewMode, final int currentPageNum, final int pageSize,
                                              final Map<String, Class<?>> articleFields, final JSONObject... tags) throws ServiceException {
        try {
            final List<Filter> filters = new ArrayList<>();
            for (final JSONObject tag : tags) {
                filters.add(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)));
            }

            Filter filter;
            if (filters.size() >= 2) {
                filter = new CompositeFilter(CompositeFilterOperator.OR, filters);
            } else {
                filter = filters.get(0);
            }

            // XXX: 这里的分页是有问题的，后面取文章的时候会少（因为一篇文章可以有多个标签，但是文章 id 一样）
            Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                    setFilter(filter).setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

            JSONObject result = tagArticleRepository.get(query);
            final JSONArray tagArticleRelations = result.optJSONArray(Keys.RESULTS);

            final Set<String> articleIds = new HashSet<>();
            for (int i = 0; i < tagArticleRelations.length(); i++) {
                articleIds.add(tagArticleRelations.optJSONObject(i).optString(Article.ARTICLE + '_' + Keys.OBJECT_ID));
            }

            query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds)).
                    addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
            for (final Map.Entry<String, Class<?>> articleField : articleFields.entrySet()) {
                query.addProjection(articleField.getKey(), articleField.getValue());
            }

            result = articleRepository.get(query);

            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles by tags [tagLength=" + tags.length + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets articles by the specified city (order by article create date desc).
     *
     * @param avatarViewMode the specified avatar view mode
     * @param city           the specified city
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getArticlesByCity(final int avatarViewMode, final String city,
                                              final int currentPageNum, final int pageSize) throws ServiceException {
        try {
            final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                    setFilter(new PropertyFilter(Article.ARTICLE_CITY, FilterOperator.EQUAL, city))
                    .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

            final JSONObject result = articleRepository.get(query);

            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(avatarViewMode, ret);

            final Integer participantsCnt = Symphonys.getInt("cityArticleParticipantsCnt");
            genParticipants(avatarViewMode, ret, participantsCnt);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles by city [" + city + "] failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets articles by the specified tag (order by article create date desc).
     *
     * @param avatarViewMode the specified avatar view mode
     * @param sortMode       the specified sort mode, 0: default, 1: hot, 2: score, 3: reply, 4: perfect
     * @param tag            the specified tag
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getArticlesByTag(final int avatarViewMode, final int sortMode, final JSONObject tag,
                                             final int currentPageNum, final int pageSize) throws ServiceException {
        try {
            Query query = new Query();
            switch (sortMode) {
                case 0:
                    query.addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

                    break;
                case 1:
                    query.addSort(Article.ARTICLE_COMMENT_CNT, SortDirection.DESCENDING).
                            addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

                    break;
                case 2:
                    query.addSort(Article.REDDIT_SCORE, SortDirection.DESCENDING).
                            addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

                    break;
                case 3:
                    query.addSort(Article.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING).
                            addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

                    break;
                case 4:
                    query.addSort(Article.ARTICLE_PERFECT, SortDirection.DESCENDING).
                            addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);

                    break;
                default:
                    LOGGER.warn("Unknown sort mode [" + sortMode + "]");
                    query.addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                            setFilter(new PropertyFilter(Tag.TAG + '_' + Keys.OBJECT_ID, FilterOperator.EQUAL, tag.optString(Keys.OBJECT_ID)))
                            .setPageCount(1).setPageSize(pageSize).setCurrentPageNum(currentPageNum);
            }

            JSONObject result = tagArticleRepository.get(query);
            final JSONArray tagArticleRelations = result.optJSONArray(Keys.RESULTS);

            final List<String> articleIds = new ArrayList<>();
            for (int i = 0; i < tagArticleRelations.length(); i++) {
                articleIds.add(tagArticleRelations.optJSONObject(i).optString(Article.ARTICLE + '_' + Keys.OBJECT_ID));
            }

            query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds)).
                    addProjection(Keys.OBJECT_ID, String.class).
                    addProjection(Article.ARTICLE_STICK, Long.class).
                    addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                    addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                    addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
                    addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                    addProjection(Article.ARTICLE_TITLE, String.class).
                    addProjection(Article.ARTICLE_STATUS, Integer.class).
                    addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
                    addProjection(Article.ARTICLE_TYPE, Integer.class).
                    addProjection(Article.ARTICLE_PERMALINK, String.class).
                    addProjection(Article.ARTICLE_TAGS, String.class).
                    addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
                    addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
                    addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                    addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
                    addProjection(Article.ARTICLE_PERFECT, Integer.class).
                    addProjection(Article.ARTICLE_CONTENT, String.class);

            result = articleRepository.get(query);

            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            switch (sortMode) {
                default:
                    LOGGER.warn("Unknown sort mode [" + sortMode + "]");
                case 0:
                    Collections.sort(ret, (o1, o2) -> o2.optString(Keys.OBJECT_ID).compareTo(o1.optString(Keys.OBJECT_ID)));

                    break;
                case 1:
                    Collections.sort(ret, (o1, o2) -> {
                        final int v = o2.optInt(Article.ARTICLE_COMMENT_CNT) - o1.optInt(Article.ARTICLE_COMMENT_CNT);
                        if (0 == v) {
                            return o2.optString(Keys.OBJECT_ID).compareTo(o1.optString(Keys.OBJECT_ID));
                        }

                        return v > 0 ? 1 : -1;
                    });

                    break;
                case 2:
                    Collections.sort(ret, (o1, o2) -> {
                        final double v = o2.optDouble(Article.REDDIT_SCORE) - o1.optDouble(Article.REDDIT_SCORE);
                        if (0 == v) {
                            return o2.optString(Keys.OBJECT_ID).compareTo(o1.optString(Keys.OBJECT_ID));
                        }

                        return v > 0 ? 1 : -1;
                    });

                    break;
                case 3:
                    Collections.sort(ret, (o1, o2) -> {
                        final long v = (o2.optLong(Article.ARTICLE_LATEST_CMT_TIME)
                                - o1.optLong(Article.ARTICLE_LATEST_CMT_TIME));
                        if (0 == v) {
                            return o2.optString(Keys.OBJECT_ID).compareTo(o1.optString(Keys.OBJECT_ID));
                        }

                        return v > 0 ? 1 : -1;
                    });

                    break;
                case 4:
                    Collections.sort(ret, (o1, o2) -> {
                        final long v = (o2.optLong(Article.ARTICLE_PERFECT) - o1.optLong(Article.ARTICLE_PERFECT));
                        if (0 == v) {
                            return o2.optString(Keys.OBJECT_ID).compareTo(o1.optString(Keys.OBJECT_ID));
                        }

                        return v > 0 ? 1 : -1;
                    });

                    break;
            }

            organizeArticles(avatarViewMode, ret);

            final Integer participantsCnt = Symphonys.getInt("tagArticleParticipantsCnt");
            genParticipants(avatarViewMode, ret, participantsCnt);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles by tag [tagTitle=" + tag.optString(Tag.TAG_TITLE) + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets an article by the specified client article id.
     *
     * @param authorId        the specified author id
     * @param clientArticleId the specified client article id
     * @return article, return {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getArticleByClientArticleId(final String authorId, final String clientArticleId) throws ServiceException {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Article.ARTICLE_CLIENT_ARTICLE_ID, FilterOperator.EQUAL, clientArticleId));
        filters.add(new PropertyFilter(Article.ARTICLE_AUTHOR_ID, FilterOperator.EQUAL, authorId));

        final Query query = new Query().setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
        try {
            final JSONObject result = articleRepository.get(query);
            final JSONArray array = result.optJSONArray(Keys.RESULTS);

            if (0 == array.length()) {
                return null;
            }

            return array.optJSONObject(0);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets article [clientArticleId=" + clientArticleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets an article with {@link #organizeArticle(int, JSONObject)} by the specified id.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param articleId      the specified id
     * @return article, return {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getArticleById(final int avatarViewMode, final String articleId) throws ServiceException {
        Stopwatchs.start("Get article by id");
        try {
            final JSONObject ret = articleRepository.get(articleId);

            if (null == ret) {
                return null;
            }

            organizeArticle(avatarViewMode, ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets an article [articleId=" + articleId + "] failed", e);
            throw new ServiceException(e);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets an article by the specified id.
     *
     * @param articleId the specified id
     * @return article, return {@code null} if not found
     * @throws ServiceException service exception
     */
    public JSONObject getArticle(final String articleId) throws ServiceException {
        try {
            final JSONObject ret = articleRepository.get(articleId);

            if (null == ret) {
                return null;
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets an article [articleId=" + articleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets preview content of the article specified with the given article id.
     *
     * @param articleId the given article id
     * @param request   the specified request
     * @return preview content
     * @throws ServiceException service exception
     */
    public String getArticlePreviewContent(final String articleId, final HttpServletRequest request) throws ServiceException {
        final JSONObject article = getArticle(articleId);
        if (null == article) {
            return null;
        }

        final int articleType = article.optInt(Article.ARTICLE_TYPE);
        if (Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
            return null;
        }

        Stopwatchs.start("Get preview content");

        try {
            final int length = Integer.valueOf("150");
            String ret = article.optString(Article.ARTICLE_CONTENT);
            final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userQueryService.getUser(authorId);

            if (null != author && UserExt.USER_STATUS_C_INVALID == author.optInt(UserExt.USER_STATUS)
                    || Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
                return langPropsService.get("articleContentBlockLabel");
            }

            final Set<String> userNames = userQueryService.getUserNames(ret);
            final JSONObject currentUser = userQueryService.getCurrentUser(request);
            final String currentUserName = null == currentUser ? "" : currentUser.optString(User.USER_NAME);
            final String authorName = author.optString(User.USER_NAME);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType
                    && !authorName.equals(currentUserName)) {
                boolean invited = false;
                for (final String userName : userNames) {
                    if (userName.equals(currentUserName)) {
                        invited = true;

                        break;
                    }
                }

                if (!invited) {
                    String blockContent = langPropsService.get("articleDiscussionLabel");
                    blockContent = blockContent.replace("{user}", "<a href='" + Latkes.getServePath()
                            + "/member/" + authorName + "'>" + authorName + "</a>");

                    return blockContent;
                }
            }

            ret = Emotions.convert(ret);
            ret = Markdowns.toHTML(ret);

            ret = Jsoup.clean(ret, Whitelist.none());
            if (ret.length() >= length) {
                ret = StringUtils.substring(ret, 0, length)
                        + " ....";
            }

            return ret;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the user articles with the specified user id, page number and page size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param userId         the specified user id
     * @param anonymous      the specified article anonymous
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return user articles, return an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getUserArticles(final int avatarViewMode, final String userId, final int anonymous,
                                            final int currentPageNum, final int pageSize) throws ServiceException {
        final Query query = new Query().addSort(Article.ARTICLE_CREATE_TIME, SortDirection.DESCENDING)
                .setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                        setFilter(CompositeFilterOperator.and(
                                new PropertyFilter(Article.ARTICLE_AUTHOR_ID, FilterOperator.EQUAL, userId),
                                new PropertyFilter(Article.ARTICLE_ANONYMOUS, FilterOperator.EQUAL, anonymous),
                                new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID)));
        try {
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            if (ret.isEmpty()) {
                return ret;
            }

            final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
            final int recordCount = pagination.optInt(Pagination.PAGINATION_RECORD_COUNT);
            final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);

            final JSONObject first = ret.get(0);
            first.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);
            first.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);

            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets user articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets side hot articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getSideHotArticles(final int avatarViewMode, final int fetchSize) throws ServiceException {
        final String id = String.valueOf(DateUtils.addDays(new Date(), -7).getTime());

        try {
            final Query query = new Query().addSort(Article.ARTICLE_COMMENT_CNT, SortDirection.DESCENDING).
                    addSort(Keys.OBJECT_ID, SortDirection.ASCENDING).setCurrentPageNum(1).setPageSize(fetchSize);

            final List<Filter> filters = new ArrayList<>();
            filters.add(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, id));
            filters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION));
            filters.add(new PropertyFilter(Article.ARTICLE_TAGS, FilterOperator.NOT_EQUAL, Tag.TAG_TITLE_C_SANDBOX));

            query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets hot articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the random articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return random articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getRandomArticles(final int avatarViewMode, final int fetchSize) throws ServiceException {
        try {
            final List<JSONObject> ret = articleRepository.getRandomly(fetchSize);
            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets random articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Makes article showing filters.
     *
     * @return filter the article showing to user
     */
    private CompositeFilter makeArticleShowingFilter() {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID));
        filters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION));
        return new CompositeFilter(CompositeFilterOperator.AND, filters);
    }

    /**
     * Makes recent article showing filters.
     *
     * @return filter the article showing to user
     */
    private CompositeFilter makeRecentArticleShowingFilter() {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID));
        filters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION));
        filters.add(new PropertyFilter(Article.ARTICLE_TAGS, FilterOperator.NOT_LIKE, "B3log%"));
        filters.add(new PropertyFilter(Article.ARTICLE_TAGS, FilterOperator.NOT_LIKE, Tag.TAG_TITLE_C_SANDBOX + "%"));
        return new CompositeFilter(CompositeFilterOperator.AND, filters);
    }

    /**
     * Makes the recent (sort by create time desc) articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles query
     */
    private Query makeRecentDefaultQuery(final int currentPageNum, final int fetchSize) {
        final Query ret = new Query()
                .addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING)
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(fetchSize).setCurrentPageNum(currentPageNum);
        ret.setFilter(makeRecentArticleShowingFilter());
        ret.addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_STICK, Long.class).
                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                addProjection(Article.ARTICLE_TITLE, String.class).
                addProjection(Article.ARTICLE_STATUS, Integer.class).
                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
                addProjection(Article.ARTICLE_TYPE, Integer.class).
                addProjection(Article.ARTICLE_PERMALINK, String.class).
                addProjection(Article.ARTICLE_TAGS, String.class).
                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
                addProjection(Article.ARTICLE_PERFECT, Integer.class).
                addProjection(Article.ARTICLE_BAD_CNT, Integer.class).
                addProjection(Article.ARTICLE_GOOD_CNT, Integer.class).
                addProjection(Article.ARTICLE_COLLECT_CNT, Integer.class).
                addProjection(Article.ARTICLE_WATCH_CNT, Integer.class).
                addProjection(Article.ARTICLE_UA, String.class).
                addProjection(Article.ARTICLE_CONTENT, String.class);


        return ret;
    }

    /**
     * Makes the recent (sort by comment count desc) articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles query
     */
    private Query makeRecentHotQuery(final int currentPageNum, final int fetchSize) {
        final String id = String.valueOf(DateUtils.addMonths(new Date(), -1).getTime());

        final Query ret = new Query()
                .addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING)
                .addSort(Article.ARTICLE_COMMENT_CNT, SortDirection.DESCENDING)
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(fetchSize).setCurrentPageNum(currentPageNum);

        final CompositeFilter compositeFilter = makeRecentArticleShowingFilter();
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, id));
        filters.addAll(compositeFilter.getSubFilters());

        ret.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
        ret.addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_STICK, Long.class).
                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                addProjection(Article.ARTICLE_TITLE, String.class).
                addProjection(Article.ARTICLE_STATUS, Integer.class).
                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
                addProjection(Article.ARTICLE_TYPE, Integer.class).
                addProjection(Article.ARTICLE_PERMALINK, String.class).
                addProjection(Article.ARTICLE_TAGS, String.class).
                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
                addProjection(Article.ARTICLE_PERFECT, Integer.class).
                addProjection(Article.ARTICLE_BAD_CNT, Integer.class).
                addProjection(Article.ARTICLE_GOOD_CNT, Integer.class).
                addProjection(Article.ARTICLE_COLLECT_CNT, Integer.class).
                addProjection(Article.ARTICLE_WATCH_CNT, Integer.class).
                addProjection(Article.ARTICLE_UA, String.class).
                addProjection(Article.ARTICLE_CONTENT, String.class);

        return ret;
    }

    /**
     * Makes the recent (sort by score desc) articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles query
     */
    private Query makeRecentGoodQuery(final int currentPageNum, final int fetchSize) {
        final Query ret = new Query()
                .addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING)
                .addSort(Article.REDDIT_SCORE, SortDirection.DESCENDING)
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(fetchSize).setCurrentPageNum(currentPageNum);
        ret.setFilter(makeRecentArticleShowingFilter());
        ret.addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_STICK, Long.class).
                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                addProjection(Article.ARTICLE_TITLE, String.class).
                addProjection(Article.ARTICLE_STATUS, Integer.class).
                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
                addProjection(Article.ARTICLE_TYPE, Integer.class).
                addProjection(Article.ARTICLE_PERMALINK, String.class).
                addProjection(Article.ARTICLE_TAGS, String.class).
                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
                addProjection(Article.ARTICLE_PERFECT, Integer.class).
                addProjection(Article.ARTICLE_BAD_CNT, Integer.class).
                addProjection(Article.ARTICLE_GOOD_CNT, Integer.class).
                addProjection(Article.ARTICLE_COLLECT_CNT, Integer.class).
                addProjection(Article.ARTICLE_WATCH_CNT, Integer.class).
                addProjection(Article.ARTICLE_UA, String.class).
                addProjection(Article.ARTICLE_CONTENT, String.class);

        return ret;
    }

    /**
     * Makes the recent (sort by latest comment time desc) articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles query
     */
    private Query makeRecentReplyQuery(final int currentPageNum, final int fetchSize) {
        final Query ret = new Query()
                .addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING)
                .addSort(Article.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING)
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageSize(fetchSize).setCurrentPageNum(currentPageNum);
        ret.setFilter(makeRecentArticleShowingFilter());
        ret.addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_STICK, Long.class).
                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                addProjection(Article.ARTICLE_TITLE, String.class).
                addProjection(Article.ARTICLE_STATUS, Integer.class).
                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
                addProjection(Article.ARTICLE_TYPE, Integer.class).
                addProjection(Article.ARTICLE_PERMALINK, String.class).
                addProjection(Article.ARTICLE_TAGS, String.class).
                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
                addProjection(Article.ARTICLE_PERFECT, Integer.class).
                addProjection(Article.ARTICLE_BAD_CNT, Integer.class).
                addProjection(Article.ARTICLE_GOOD_CNT, Integer.class).
                addProjection(Article.ARTICLE_COLLECT_CNT, Integer.class).
                addProjection(Article.ARTICLE_WATCH_CNT, Integer.class).
                addProjection(Article.ARTICLE_UA, String.class).
                addProjection(Article.ARTICLE_CONTENT, String.class);

        return ret;
    }

    /**
     * Makes the top articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return top articles query
     */
    private Query makeTopQuery(final int currentPageNum, final int fetchSize) {
        final Query query = new Query()
                .addSort(Article.REDDIT_SCORE, SortDirection.DESCENDING)
                .addSort(Article.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING)
                .setPageCount(1).setPageSize(fetchSize).setCurrentPageNum(currentPageNum);

        query.setFilter(makeArticleShowingFilter());
        return query;
    }

    /**
     * Gets the recent (sort by create time) articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param sortMode       the specified sort mode, 0: default, 1: hot, 2: score, 3: reply
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "articles": [{
     *         "oId": "",
     *         "articleTitle": "",
     *         "articleContent": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getRecentArticles(final int avatarViewMode, final int sortMode,
                                        final int currentPageNum, final int fetchSize)
            throws ServiceException {
        final JSONObject ret = new JSONObject();

        Query query;
        switch (sortMode) {
            case 0:
                query = makeRecentDefaultQuery(currentPageNum, fetchSize);

                break;
            case 1:
                query = makeRecentHotQuery(currentPageNum, fetchSize);

                break;
            case 2:
                query = makeRecentGoodQuery(currentPageNum, fetchSize);

                break;
            case 3:
                query = makeRecentReplyQuery(currentPageNum, fetchSize);

                break;
            default:
                LOGGER.warn("Unknown sort mode [" + sortMode + "]");
                query = makeRecentDefaultQuery(currentPageNum, fetchSize);
        }

        JSONObject result = null;

        try {
            Stopwatchs.start("Query recent articles");

            result = articleRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles failed", e);

            throw new ServiceException(e);
        } finally {
            Stopwatchs.end();
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);

        final int windowSize = Symphonys.getInt("latestArticlesWindowSize");

        final List<Integer> pageNums = Paginator.paginate(currentPageNum, fetchSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> articles = CollectionUtils.<JSONObject>jsonArrayToList(data);

        try {
            organizeArticles(avatarViewMode, articles);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Organizes articles failed", e);

            throw new ServiceException(e);
        }

        //final Integer participantsCnt = Symphonys.getInt("latestArticleParticipantsCnt");
        //genParticipants(articles, participantsCnt);
        ret.put(Article.ARTICLES, (Object) articles);

        return ret;
    }

    /**
     * Gets the index recent (sort by create time) articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @return recent articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getIndexRecentArticles(final int avatarViewMode) throws ServiceException {
        try {
            List<JSONObject> ret;
            Stopwatchs.start("Query index recent articles");
            try {
                ret = articleRepository.select("SELECT\n"
                        + "	oId,\n"
                        + "	articleStick,\n"
                        + "	articleCreateTime,\n"
                        + "	articleUpdateTime,\n"
                        + "	articleLatestCmtTime,\n"
                        + "	articleAuthorId,\n"
                        + "	articleTitle,\n"
                        + "	articleStatus,\n"
                        + "	articleViewCount,\n"
                        + "	articleType,\n"
                        + "	articlePermalink,\n"
                        + "	articleTags,\n"
                        + "	articleLatestCmterName,\n"
                        + "	syncWithSymphonyClient,\n"
                        + "	articleCommentCount,\n"
                        + "	articleAnonymous,\n"
                        + "	articlePerfect,\n"
                        + "	articleContent,\n"
                        + "	CASE\n"
                        + "WHEN articleLatestCmtTime = 0 THEN\n"
                        + "	oId\n"
                        + "ELSE\n"
                        + "	articleLatestCmtTime\n"
                        + "END AS flag\n"
                        + "FROM\n"
                        + "	`" + articleRepository.getName() + "`\n"
                        + " WHERE `articleType` != 1 AND `articleStatus` = 0 AND `articleTags` != '" + Tag.TAG_TITLE_C_SANDBOX + "'\n"
                        + " ORDER BY\n"
                        + "	articleStick DESC,\n"
                        + "	flag DESC\n"
                        + "LIMIT ?", Symphonys.getInt("indexListCnt"));
            } finally {
                Stopwatchs.end();
            }

            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets index recent articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the hot articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param fetchSize      the specified fetch size
     * @return hot articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getHotArticles(final int avatarViewMode, final int fetchSize) throws ServiceException {
        final Query query = makeTopQuery(1, fetchSize);

        try {
            List<JSONObject> ret;
            Stopwatchs.start("Query hot articles");
            try {
                final JSONObject result = articleRepository.get(query);
                ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            } finally {
                Stopwatchs.end();
            }

            organizeArticles(avatarViewMode, ret);

            Stopwatchs.start("Checks author status");
            try {
                for (final JSONObject article : ret) {
                    final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);

                    final JSONObject author = userRepository.get(authorId);

                    if (UserExt.USER_STATUS_C_INVALID == author.optInt(UserExt.USER_STATUS)) {
                        article.put(Article.ARTICLE_TITLE, langPropsService.get("articleTitleBlockLabel"));
                        article.put(Article.ARTICLE_T_TITLE_EMOJI, langPropsService.get("articleTitleBlockLabel"));
                        article.put(Article.ARTICLE_T_TITLE_EMOJI_UNICODE, langPropsService.get("articleTitleBlockLabel"));
                    }
                }
            } finally {
                Stopwatchs.end();
            }

//            final Integer participantsCnt = Symphonys.getInt("indexArticleParticipantsCnt");
//            genParticipants(ret, participantsCnt);
            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets index articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the perfect articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "articles": [{
     *         "oId": "",
     *         "articleTitle": "",
     *         "articleContent": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     */
    public JSONObject getPerfectArticles(final int avatarViewMode, final int currentPageNum, final int fetchSize)
            throws ServiceException {
        final Query query = new Query()
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setCurrentPageNum(currentPageNum).setPageSize(fetchSize);
        query.setFilter(new PropertyFilter(Article.ARTICLE_PERFECT, FilterOperator.EQUAL, Article.ARTICLE_PERFECT_C_PERFECT));

        final JSONObject ret = new JSONObject();

        JSONObject result = null;

        try {
            Stopwatchs.start("Query recent articles");

            result = articleRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles failed", e);

            throw new ServiceException(e);
        } finally {
            Stopwatchs.end();
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);

        final int windowSize = Symphonys.getInt("latestArticlesWindowSize");

        final List<Integer> pageNums = Paginator.paginate(currentPageNum, fetchSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> articles = CollectionUtils.<JSONObject>jsonArrayToList(data);

        try {
            organizeArticles(avatarViewMode, articles);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Organizes articles failed", e);

            throw new ServiceException(e);
        }

        //final Integer participantsCnt = Symphonys.getInt("latestArticleParticipantsCnt");
        //genParticipants(articles, participantsCnt);
        ret.put(Article.ARTICLES, (Object) articles);

        return ret;
    }

    /**
     * Gets the index hot articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @return hot articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getIndexHotArticles(final int avatarViewMode) throws ServiceException {
        final Query query = new Query()
                .addSort(Article.REDDIT_SCORE, SortDirection.DESCENDING)
                .addSort(Article.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING)
                .setPageCount(1).setPageSize(Symphonys.getInt("indexListCnt")).setCurrentPageNum(1);
        query.setFilter(makeArticleShowingFilter());
        query.addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_STICK, Long.class).
                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                addProjection(Article.ARTICLE_TITLE, String.class).
                addProjection(Article.ARTICLE_STATUS, Integer.class).
                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
                addProjection(Article.ARTICLE_TYPE, Integer.class).
                addProjection(Article.ARTICLE_PERMALINK, String.class).
                addProjection(Article.ARTICLE_TAGS, String.class).
                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
                addProjection(Article.ARTICLE_PERFECT, Integer.class);

        try {
            List<JSONObject> ret;
            Stopwatchs.start("Query index hot articles");
            try {
                final JSONObject result = articleRepository.get(query);
                ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            } finally {
                Stopwatchs.end();
            }

            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets index hot articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the index perfect articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @return hot articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getIndexPerfectArticles(final int avatarViewMode) throws ServiceException {
        final Query query = new Query()
                .addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setPageCount(1).setPageSize(36).setCurrentPageNum(1);
        query.setFilter(new PropertyFilter(Article.ARTICLE_PERFECT, FilterOperator.EQUAL, Article.ARTICLE_PERFECT_C_PERFECT));
        query.addProjection(Keys.OBJECT_ID, String.class).
                addProjection(Article.ARTICLE_STICK, Long.class).
                addProjection(Article.ARTICLE_CREATE_TIME, Long.class).
                addProjection(Article.ARTICLE_UPDATE_TIME, Long.class).
                addProjection(Article.ARTICLE_LATEST_CMT_TIME, Long.class).
                addProjection(Article.ARTICLE_AUTHOR_ID, String.class).
                addProjection(Article.ARTICLE_TITLE, String.class).
                addProjection(Article.ARTICLE_STATUS, Integer.class).
                addProjection(Article.ARTICLE_VIEW_CNT, Integer.class).
                addProjection(Article.ARTICLE_TYPE, Integer.class).
                addProjection(Article.ARTICLE_PERMALINK, String.class).
                addProjection(Article.ARTICLE_TAGS, String.class).
                addProjection(Article.ARTICLE_LATEST_CMTER_NAME, String.class).
                addProjection(Article.ARTICLE_SYNC_TO_CLIENT, Boolean.class).
                addProjection(Article.ARTICLE_COMMENT_CNT, Integer.class).
                addProjection(Article.ARTICLE_ANONYMOUS, Integer.class).
                addProjection(Article.ARTICLE_PERFECT, Integer.class);

        try {
            List<JSONObject> ret;
            Stopwatchs.start("Query index perfect articles");
            try {
                final JSONObject result = articleRepository.get(query);
                ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            } finally {
                Stopwatchs.end();
            }

            organizeArticles(avatarViewMode, ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets index perfect articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets the recent articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getRecentArticlesWithComments(final int avatarViewMode,
                                                          final int currentPageNum, final int fetchSize) throws ServiceException {
        return getArticles(avatarViewMode, makeRecentDefaultQuery(currentPageNum, fetchSize));
    }

    /**
     * Gets the index articles with the specified fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param currentPageNum the specified current page number
     * @param fetchSize      the specified fetch size
     * @return recent articles, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getTopArticlesWithComments(final int avatarViewMode,
                                                       final int currentPageNum, final int fetchSize) throws ServiceException {
        return getArticles(avatarViewMode, makeTopQuery(currentPageNum, fetchSize));
    }

    /**
     * The specific articles.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param query          conditions
     * @return articles
     * @throws ServiceException service exception
     */
    private List<JSONObject> getArticles(final int avatarViewMode, final Query query) throws ServiceException {
        try {
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> ret = CollectionUtils.<JSONObject>jsonArrayToList(result.optJSONArray(Keys.RESULTS));
            organizeArticles(avatarViewMode, ret);
            final List<JSONObject> stories = new ArrayList<>();

            for (final JSONObject article : ret) {
                final JSONObject story = new JSONObject();
                final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
                final JSONObject author = userRepository.get(authorId);
                if (UserExt.USER_STATUS_C_INVALID == author.optInt(UserExt.USER_STATUS)) {
                    story.put("title", langPropsService.get("articleTitleBlockLabel"));
                } else {
                    story.put("title", article.optString(Article.ARTICLE_TITLE));
                }
                story.put("id", article.optLong("oId"));
                story.put("url", Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK));
                story.put("user_display_name", article.optString(Article.ARTICLE_T_AUTHOR_NAME));
                story.put("user_job", author.optString(UserExt.USER_INTRO));
                story.put("comment_html", article.optString(Article.ARTICLE_CONTENT));
                story.put("comment_count", article.optInt(Article.ARTICLE_COMMENT_CNT));
                story.put("vote_count", article.optInt(Article.ARTICLE_GOOD_CNT));
                story.put("created_at", formatDate(article.get(Article.ARTICLE_CREATE_TIME)));
                story.put("user_portrait_url", article.optString(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL));
                story.put("comments", getAllComments(avatarViewMode, article.optString("oId")));
                final String tagsString = article.optString(Article.ARTICLE_TAGS);
                String[] tags = null;
                if (!Strings.isEmptyOrNull(tagsString)) {
                    tags = tagsString.split(",");
                }
                story.put("badge", tags == null ? "" : tags[0]);
                stories.add(story);
            }
            final Integer participantsCnt = Symphonys.getInt("indexArticleParticipantsCnt");
            genParticipants(avatarViewMode, stories, participantsCnt);
            return stories;
        } catch (final RepositoryException | JSONException e) {
            LOGGER.log(Level.ERROR, "Gets index articles failed", e);

            throw new ServiceException(e);
        }
    }

    /**
     * Gets the article comments with the specified article id.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param articleId      the specified article id
     * @return comments, return an empty list if not found
     * @throws ServiceException    service exception
     * @throws JSONException       json exception
     * @throws RepositoryException repository exception
     */
    private List<JSONObject> getAllComments(final int avatarViewMode, final String articleId)
            throws ServiceException, JSONException, RepositoryException {
        final List<JSONObject> commments = new ArrayList<>();
        final List<JSONObject> articleComments = commentQueryService.getArticleComments(
                avatarViewMode, articleId, 1, Integer.MAX_VALUE, UserExt.USER_COMMENT_VIEW_MODE_C_TRADITIONAL);
        for (final JSONObject ac : articleComments) {
            final JSONObject comment = new JSONObject();
            final JSONObject author = userRepository.get(ac.optString(Comment.COMMENT_AUTHOR_ID));
            comment.put("id", ac.optLong("oId"));
            comment.put("body_html", ac.optString(Comment.COMMENT_CONTENT));
            comment.put("depth", 0);
            comment.put("user_display_name", ac.optString(Comment.COMMENT_T_AUTHOR_NAME));
            comment.put("user_job", author.optString(UserExt.USER_INTRO));
            comment.put("vote_count", 0);
            comment.put("created_at", formatDate(ac.get(Comment.COMMENT_CREATE_TIME)));
            comment.put("user_portrait_url", ac.optString(Comment.COMMENT_T_ARTICLE_AUTHOR_THUMBNAIL_URL));
            commments.add(comment);
        }
        return commments;
    }

    /**
     * The demand format date.
     *
     * @param date the original date
     * @return the format date like "2015-08-03T07:26:57Z"
     */
    private String formatDate(final Object date) {
        return DateFormatUtils.format(((Date) date).getTime(), "yyyy-MM-dd")
                + "T" + DateFormatUtils.format(((Date) date).getTime(), "HH:mm:ss") + "Z";
    }

    /**
     * Organizes the specified articles.
     *
     * @param avatarViewMode the specified avatarViewMode
     * @param articles       the specified articles
     * @throws RepositoryException repository exception
     * @see #organizeArticle(int, org.json.JSONObject)
     */
    public void organizeArticles(final int avatarViewMode, final List<JSONObject> articles) throws RepositoryException {
        Stopwatchs.start("Organize articles");
        try {
            for (final JSONObject article : articles) {
                organizeArticle(avatarViewMode, article);
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Organizes the specified article.
     * <p>
     * <ul>
     * <li>converts create/update/latest comment time (long) to date type</li>
     * <li>generates author thumbnail URL</li>
     * <li>generates author name</li>
     * <li>escapes article title &lt; and &gt;</li>
     * <li>generates article heat</li>
     * <li>generates article view count display format(1k+/1.5k+...)</li>
     * <li>generates time ago text</li>
     * <li>generates comment time ago text</li>
     * <li>generates stick remains minutes</li>
     * <li>anonymous process</li>
     * <li>builds tag objects</li>
     * <li>generates article preview content</li>
     * <li>extracts the first image URL</li>
     * <li>image processing if using Qiniu</li>
     * </ul>
     * </p>
     *
     * @param avatarViewMode the specified avatar view mode
     * @param article        the specified article
     * @throws RepositoryException repository exception
     */
    public void organizeArticle(final int avatarViewMode, final JSONObject article) throws RepositoryException {
        toArticleDate(article);
        genArticleAuthor(avatarViewMode, article);

        final String previewContent = getArticleMetaDesc(article);
        article.put(Article.ARTICLE_T_PREVIEW_CONTENT, previewContent);

        if (StringUtils.length(previewContent) > 100) {
            article.put(Article.ARTICLE_T_THUMBNAIL_URL, getArticleThumbnail(article));
        } else {
            article.put(Article.ARTICLE_T_THUMBNAIL_URL, "");
        }

        qiniuImgProcessing(article);

        String title = article.optString(Article.ARTICLE_TITLE).replace("<", "&lt;").replace(">", "&gt;");
        title = Markdowns.clean(title, "");
        article.put(Article.ARTICLE_TITLE, title);

        article.put(Article.ARTICLE_T_TITLE_EMOJI, Emotions.convert(title));
        article.put(Article.ARTICLE_T_TITLE_EMOJI_UNICODE, EmojiParser.parseToUnicode(title));

        if (Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
            article.put(Article.ARTICLE_TITLE, langPropsService.get("articleTitleBlockLabel"));
            article.put(Article.ARTICLE_T_TITLE_EMOJI, langPropsService.get("articleTitleBlockLabel"));
            article.put(Article.ARTICLE_T_TITLE_EMOJI_UNICODE, langPropsService.get("articleTitleBlockLabel"));
            article.put(Article.ARTICLE_CONTENT, langPropsService.get("articleContentBlockLabel"));
        }

        final String articleId = article.optString(Keys.OBJECT_ID);
        Integer viewingCnt = ArticleChannel.ARTICLE_VIEWS.get(articleId);
        if (null == viewingCnt) {
            viewingCnt = 0;
        }

        article.put(Article.ARTICLE_T_HEAT, viewingCnt);

        final int viewCnt = article.optInt(Article.ARTICLE_VIEW_CNT);
        final double views = (double) viewCnt / 1000;
        if (views >= 1) {
            final DecimalFormat df = new DecimalFormat("#.#");
            article.put(Article.ARTICLE_T_VIEW_CNT_DISPLAY_FORMAT, df.format(views) + "K");
        }

        final long stick = article.optLong(Article.ARTICLE_STICK);
        long expired;
        if (stick > 0) {
            expired = stick + Symphonys.getLong("stickArticleTime");
            final long remainsMills = Math.abs(System.currentTimeMillis() - expired);

            article.put(Article.ARTICLE_T_STICK_REMAINS, (int) Math.floor((double) remainsMills / 1000 / 60));
        } else {
            article.put(Article.ARTICLE_T_STICK_REMAINS, 0);
        }

        String articleLatestCmterName = article.optString(Article.ARTICLE_LATEST_CMTER_NAME);
        if (StringUtils.isNotBlank(articleLatestCmterName)
                && UserRegisterValidation.invalidUserName(articleLatestCmterName)) {
            articleLatestCmterName = UserExt.ANONYMOUS_USER_NAME;
            article.put(Article.ARTICLE_LATEST_CMTER_NAME, articleLatestCmterName);
        }

        final Query query = new Query()
                .setPageCount(1).setCurrentPageNum(1).setPageSize(1)
                .setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId)).
                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        final JSONArray cmts = commentRepository.get(query).optJSONArray(Keys.RESULTS);
        if (cmts.length() > 0) {
            final JSONObject latestCmt = cmts.optJSONObject(0);
            latestCmt.put(Comment.COMMENT_CLIENT_COMMENT_ID, latestCmt.optString(Comment.COMMENT_CLIENT_COMMENT_ID));
            article.put(Article.ARTICLE_T_LATEST_CMT, latestCmt);
        }

        // builds tag objects
        final String tagsStr = article.optString(Article.ARTICLE_TAGS);
        final String[] tagTitles = tagsStr.split(",");

        final List<JSONObject> tags = new ArrayList<>();
        for (final String tagTitle : tagTitles) {
            final JSONObject tag = new JSONObject();
            tag.put(Tag.TAG_TITLE, tagTitle);

            final String uri = tagRepository.getURIByTitle(tagTitle);
            if (null != uri) {
                tag.put(Tag.TAG_URI, uri);
            } else {
                tag.put(Tag.TAG_URI, tagTitle);

                tagRepository.getURIByTitle(tagTitle);
            }

            tags.add(tag);
        }
        article.put(Article.ARTICLE_T_TAG_OBJS, (Object) tags);
    }

    /**
     * Gets the first image URL of the specified article.
     *
     * @param article the specified article
     * @return the first image URL, returns {@code ""} if not found
     */
    private String getArticleThumbnail(final JSONObject article) {
        final int articleType = article.optInt(Article.ARTICLE_TYPE);
        if (Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
            return "";
        }

        final String content = article.optString(Article.ARTICLE_CONTENT);
        final String html = Markdowns.toHTML(content);
        String ret = StringUtils.substringBetween(html, "<img src=\"", "\"");

        final boolean qiniuEnabled = Symphonys.getBoolean("qiniu.enabled");
        if (qiniuEnabled) {
            final String qiniuDomain = Symphonys.get("qiniu.domain");
            if (StringUtils.startsWith(ret, qiniuDomain)) {
                ret += "?imageView2/1/w/" + 180 + "/h/" + 135 + "/format/jpg/interlace/1/q";
            } else {
                ret = "";
            }
        } else {
            if (!StringUtils.startsWith(ret, Latkes.getServePath())) {
                ret = "";
            }
        }

        if (StringUtils.isBlank(ret)) {
            ret = "";
        }

        return ret;
    }

    /**
     * Qiniu image processing.
     *
     * @param article the specified article
     * @return the first image URL, returns {@code ""} if not found
     */
    private void qiniuImgProcessing(final JSONObject article) {
        final boolean qiniuEnabled = Symphonys.getBoolean("qiniu.enabled");
        if (!qiniuEnabled) {
            return;
        }

        final int articleType = article.optInt(Article.ARTICLE_TYPE);
        if (Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
            return;
        }

        final String qiniuDomain = Symphonys.get("qiniu.domain");
        String content = article.optString(Article.ARTICLE_CONTENT);
        final String html = Markdowns.toHTML(content);

        final String[] imgSrcs = StringUtils.substringsBetween(html, "<img src=\"", "\"");
        if (null == imgSrcs) {
            return;
        }

        for (final String imgSrc : imgSrcs) {
            if (!StringUtils.startsWith(imgSrc, qiniuDomain) || StringUtils.contains(imgSrc, ".gif")) {
                continue;
            }

            content = StringUtils.replaceOnce(content, imgSrc, imgSrc + "?imageView2/2/w/768/format/jpg/interlace/0/q");
        }

        article.put(Article.ARTICLE_CONTENT, content);
    }

    /**
     * Converts the specified article create/update/latest comment time (long) to date type.
     *
     * @param article the specified article
     */
    private void toArticleDate(final JSONObject article) {
        article.put(Common.TIME_AGO,
                Times.getTimeAgo(article.optLong(Article.ARTICLE_CREATE_TIME), Locales.getLocale()));
        article.put(Common.CMT_TIME_AGO,
                Times.getTimeAgo(article.optLong(Article.ARTICLE_LATEST_CMT_TIME), Locales.getLocale()));

        article.put(Article.ARTICLE_CREATE_TIME, new Date(article.optLong(Article.ARTICLE_CREATE_TIME)));
        article.put(Article.ARTICLE_UPDATE_TIME, new Date(article.optLong(Article.ARTICLE_UPDATE_TIME)));
        article.put(Article.ARTICLE_LATEST_CMT_TIME, new Date(article.optLong(Article.ARTICLE_LATEST_CMT_TIME)));
    }

    /**
     * Generates the specified article author name and thumbnail URL.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param article        the specified article
     * @throws RepositoryException repository exception
     */
    private void genArticleAuthor(final int avatarViewMode, final JSONObject article) throws RepositoryException {
        final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);

        final JSONObject author = userRepository.get(authorId);
        article.put(Article.ARTICLE_T_AUTHOR, author);

        if (Article.ARTICLE_ANONYMOUS_C_ANONYMOUS == article.optInt(Article.ARTICLE_ANONYMOUS)) {
            article.put(Article.ARTICLE_T_AUTHOR_NAME, UserExt.ANONYMOUS_USER_NAME);
            article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "210", avatarQueryService.getDefaultAvatarURL("210"));
            article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "48", avatarQueryService.getDefaultAvatarURL("48"));
            article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "20", avatarQueryService.getDefaultAvatarURL("20"));
        } else {
            article.put(Article.ARTICLE_T_AUTHOR_NAME, author.optString(User.USER_NAME));
            article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "210",
                    avatarQueryService.getAvatarURLByUser(avatarViewMode, author, "210"));
            article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "48",
                    avatarQueryService.getAvatarURLByUser(avatarViewMode, author, "48"));
            article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "20",
                    avatarQueryService.getAvatarURLByUser(avatarViewMode, author, "20"));
        }
    }

    /**
     * Generates participants for the specified articles.
     *
     * @param avatarViewMode  the specified avatar view mode
     * @param articles        the specified articles
     * @param participantsCnt the specified generate size
     * @throws ServiceException service exception
     */
    public void genParticipants(final int avatarViewMode,
                                final List<JSONObject> articles, final Integer participantsCnt) throws ServiceException {
        Stopwatchs.start("Generates participants");
        try {
            for (final JSONObject article : articles) {
                article.put(Article.ARTICLE_T_PARTICIPANTS, (Object) Collections.emptyList());

                if (article.optInt(Article.ARTICLE_COMMENT_CNT) < 1) {
                    continue;
                }

                final List<JSONObject> articleParticipants = getArticleLatestParticipants(
                        avatarViewMode, article.optString(Keys.OBJECT_ID), participantsCnt);
                article.put(Article.ARTICLE_T_PARTICIPANTS, (Object) articleParticipants);
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the article participants (commenters) with the specified article article id and fetch size.
     *
     * @param avatarViewMode the specified avatar view mode
     * @param articleId      the specified article id
     * @param fetchSize      the specified fetch size
     * @return article participants, for example,      <pre>
     * [
     *     {
     *         "oId": "",
     *         "articleParticipantName": "",
     *         "articleParticipantThumbnailURL": "",
     *         "articleParticipantThumbnailUpdateTime": long,
     *         "commentId": ""
     *     }, ....
     * ]
     * </pre>, returns an empty list if not found
     * @throws ServiceException service exception
     */
    public List<JSONObject> getArticleLatestParticipants(final int avatarViewMode,
                                                         final String articleId, final int fetchSize) throws ServiceException {
        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId))
                .addProjection(Keys.OBJECT_ID, String.class)
                .addProjection(Comment.COMMENT_AUTHOR_ID, String.class)
                .setPageCount(1).setCurrentPageNum(1).setPageSize(fetchSize);
        final List<JSONObject> ret = new ArrayList<>();

        try {
            final JSONObject result = commentRepository.get(query);

            final List<JSONObject> comments = new ArrayList<>();
            final JSONArray records = result.optJSONArray(Keys.RESULTS);
            for (int i = 0; i < records.length(); i++) {
                final JSONObject comment = records.optJSONObject(i);

                boolean exist = false;
                // deduplicate
                for (final JSONObject c : comments) {
                    if (comment.optString(Comment.COMMENT_AUTHOR_ID).equals(
                            c.optString(Comment.COMMENT_AUTHOR_ID))) {
                        exist = true;

                        break;
                    }
                }

                if (!exist) {
                    comments.add(comment);
                }
            }

            for (final JSONObject comment : comments) {
                final String userId = comment.optString(Comment.COMMENT_AUTHOR_ID);

                final JSONObject commenter = userRepository.get(userId);
                final String email = commenter.optString(User.USER_EMAIL);

                String thumbnailURL = Symphonys.get("defaultThumbnailURL");
                if (!UserExt.DEFAULT_CMTER_EMAIL.equals(email)) {
                    thumbnailURL = avatarQueryService.getAvatarURLByUser(avatarViewMode, commenter, "48");
                }

                final JSONObject participant = new JSONObject();
                participant.put(Article.ARTICLE_T_PARTICIPANT_NAME, commenter.optString(User.USER_NAME));
                participant.put(Article.ARTICLE_T_PARTICIPANT_THUMBNAIL_URL, thumbnailURL);
                participant.put(Article.ARTICLE_T_PARTICIPANT_THUMBNAIL_UPDATE_TIME,
                        commenter.optLong(UserExt.USER_UPDATE_TIME));
                participant.put(Article.ARTICLE_T_PARTICIPANT_URL, commenter.optString(User.USER_URL));
                participant.put(Keys.OBJECT_ID, commenter.optString(Keys.OBJECT_ID));
                participant.put(Comment.COMMENT_T_ID, comment.optString(Keys.OBJECT_ID));

                ret.add(participant);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets article [" + articleId + "] participants failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Processes the specified article content.
     * <p>
     * <ul>
     * <li>Generates &#64;username home URL</li>
     * <li>Markdowns</li>
     * <li>Generates secured article content</li>
     * <li>Blocks the article if need</li>
     * <li>Generates emotion images</li>
     * <li>Generates article link with article id</li>
     * <li>Generates article abstract (preview content)</li>
     * <li>Generates article ToC</li>
     * </ul>
     *
     * @param article the specified article, for example,
     *                "articleTitle": "",
     *                ....,
     *                "author": {}
     * @param request the specified request
     * @throws ServiceException service exception
     */
    public void processArticleContent(final JSONObject article, final HttpServletRequest request)
            throws ServiceException {
        Stopwatchs.start("Process content");

        try {
            final JSONObject author = article.optJSONObject(Article.ARTICLE_T_AUTHOR);
            if (null != author && UserExt.USER_STATUS_C_INVALID == author.optInt(UserExt.USER_STATUS)
                    || Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
                article.put(Article.ARTICLE_TITLE, langPropsService.get("articleTitleBlockLabel"));
                article.put(Article.ARTICLE_T_TITLE_EMOJI, langPropsService.get("articleTitleBlockLabel"));
                article.put(Article.ARTICLE_T_TITLE_EMOJI_UNICODE, langPropsService.get("articleTitleBlockLabel"));
                article.put(Article.ARTICLE_CONTENT, langPropsService.get("articleContentBlockLabel"));
                article.put(Article.ARTICLE_T_PREVIEW_CONTENT, langPropsService.get("articleContentBlockLabel"));
                article.put(Article.ARTICLE_T_TOC, "");
                article.put(Article.ARTICLE_REWARD_CONTENT, "");
                article.put(Article.ARTICLE_REWARD_POINT, 0);

                return;
            }

            article.put(Article.ARTICLE_T_PREVIEW_CONTENT, article.optString(Article.ARTICLE_TITLE));

            String articleContent = article.optString(Article.ARTICLE_CONTENT);
            article.put(Common.DISCUSSION_VIEWABLE, true);

            final JSONObject currentUser = userQueryService.getCurrentUser(request);
            final String currentUserName = null == currentUser ? "" : currentUser.optString(User.USER_NAME);
            final String currentRole = null == currentUser ? "" : currentUser.optString(User.USER_ROLE);
            final String authorName = article.optString(Article.ARTICLE_T_AUTHOR_NAME);

            final int articleType = article.optInt(Article.ARTICLE_TYPE);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType
                    && !authorName.equals(currentUserName) && !Role.ROLE_ID_C_ADMIN.equals(currentRole)) {
                boolean invited = false;

                final Set<String> userNames = userQueryService.getUserNames(articleContent);
                for (final String userName : userNames) {
                    if (userName.equals(currentUserName)) {
                        invited = true;

                        break;
                    }
                }

                if (!invited) {
                    String blockContent = langPropsService.get("articleDiscussionLabel");
                    blockContent = blockContent.replace("{user}", "<a href='" + Latkes.getServePath()
                            + "/member/" + authorName + "'>" + authorName + "</a>");

                    article.put(Article.ARTICLE_CONTENT, blockContent);
                    article.put(Common.DISCUSSION_VIEWABLE, false);
                    article.put(Article.ARTICLE_REWARD_CONTENT, "");
                    article.put(Article.ARTICLE_REWARD_POINT, 0);
                    article.put(Article.ARTICLE_T_TOC, "");
                    article.put(Article.ARTICLE_AUDIO_URL, "");

                    return;
                }
            }

            if (Article.ARTICLE_TYPE_C_THOUGHT != articleType) {
                articleContent = shortLinkQueryService.linkArticle(articleContent);
                articleContent = shortLinkQueryService.linkTag(articleContent);
                articleContent = Emotions.convert(articleContent);
                article.put(Article.ARTICLE_CONTENT, articleContent);
            }

            if (article.optInt(Article.ARTICLE_REWARD_POINT) > 0) {
                String rewardContent = article.optString(Article.ARTICLE_REWARD_CONTENT);
                rewardContent = shortLinkQueryService.linkArticle(rewardContent);
                rewardContent = shortLinkQueryService.linkTag(rewardContent);
                rewardContent = Emotions.convert(rewardContent);
                article.put(Article.ARTICLE_REWARD_CONTENT, rewardContent);
            }

            markdown(article);
            articleContent = article.optString(Article.ARTICLE_CONTENT);

            if (Article.ARTICLE_TYPE_C_THOUGHT != articleType) {
                articleContent = MP3Players.render(articleContent);
            }

            article.put(Article.ARTICLE_CONTENT, articleContent);
            article.put(Article.ARTICLE_T_PREVIEW_CONTENT, getArticleMetaDesc(article));
            article.put(Article.ARTICLE_T_TOC, getArticleToC(article));
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets articles by the specified request json object.
     *
     * @param avatarViewMode    the specified avatar view mode
     * @param requestJSONObject the specified request json object, for example
     *                          "oId": "", // optional
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10
     * @param articleFields     the specified article fields to return
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "articles": [{
     *         "oId": "",
     *         "articleTitle": "",
     *         "articleContent": "",
     *         ....
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getArticles(final int avatarViewMode,
                                  final JSONObject requestJSONObject, final Map<String, Class<?>> articleFields) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        for (final Map.Entry<String, Class<?>> articleField : articleFields.entrySet()) {
            query.addProjection(articleField.getKey(), articleField.getValue());
        }

        if (requestJSONObject.has(Keys.OBJECT_ID)) {
            query.setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, requestJSONObject.optString(Keys.OBJECT_ID)));
        }

        JSONObject result = null;

        try {
            result = articleRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> articles = CollectionUtils.<JSONObject>jsonArrayToList(data);

        try {
            organizeArticles(avatarViewMode, articles);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Organizes articles failed", e);

            throw new ServiceException(e);
        }

        ret.put(Article.ARTICLES, articles);

        return ret;
    }

    /**
     * Markdowns the specified article content.
     * <p>
     * <ul>
     * <li>Markdowns article content/reward content</li>
     * <li>Generates secured article content/reward content</li>
     * </ul>
     *
     * @param article the specified article content
     */
    private void markdown(final JSONObject article) {
        String content = article.optString(Article.ARTICLE_CONTENT);

        final int articleType = article.optInt(Article.ARTICLE_TYPE);
        if (Article.ARTICLE_TYPE_C_THOUGHT != articleType) {
            content = Markdowns.toHTML(content);
            content = Markdowns.clean(content, Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK));
        } else {
            final Document.OutputSettings outputSettings = new Document.OutputSettings();
            outputSettings.prettyPrint(false);

            content = Jsoup.clean(content, Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK),
                    Whitelist.relaxed().addAttributes(":all", "id", "target", "class").
                            addTags("span", "hr").addAttributes("iframe", "src", "width", "height")
                            .addAttributes("audio", "controls", "src"), outputSettings);

            content = content.replace("\n", "\\n").replace("'", "\\'")
                    .replace("\"", "\\\"");
        }

        article.put(Article.ARTICLE_CONTENT, content);

        if (article.optInt(Article.ARTICLE_REWARD_POINT) > 0) {
            String rewardContent = article.optString(Article.ARTICLE_REWARD_CONTENT);
            rewardContent = Markdowns.toHTML(rewardContent);
            rewardContent = Markdowns.clean(rewardContent,
                    Latkes.getServePath() + article.optString(Article.ARTICLE_PERMALINK));
            article.put(Article.ARTICLE_REWARD_CONTENT, rewardContent);
        }
    }

    /**
     * Gets meta description content of the specified article.
     *
     * @param article the specified article
     * @return meta description
     */
    public String getArticleMetaDesc(final JSONObject article) {
        final String articleId = article.optString(Keys.OBJECT_ID);
        String articleAbstract = articleCache.getArticleAbstract(articleId);
        if (StringUtils.isNotBlank(articleAbstract)) {
            return articleAbstract;
        }

        Stopwatchs.start("Meta Desc");
        try {
            final int articleType = article.optInt(Article.ARTICLE_TYPE);
            if (Article.ARTICLE_TYPE_C_THOUGHT == articleType) {
                return "....";
            }

            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType) {
                return langPropsService.get("articleAbstractDiscussionLabel", Latkes.getLocale());
            }

            final int length = Integer.valueOf("150");

            String ret = article.optString(Article.ARTICLE_CONTENT);
            ret = Emotions.clear(ret);
            try {
                ret = Markdowns.toHTML(ret);
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Parses article abstract failed [id=" + articleId + ", md=" + ret + "]");
                throw e;
            }

            final Whitelist whitelist = Whitelist.basicWithImages();
            whitelist.addTags("object", "video");
            ret = Jsoup.clean(ret, whitelist);

            final int threshold = 20;
            String[] pics = StringUtils.substringsBetween(ret, "<img", ">");
            if (null != pics) {
                if (pics.length > threshold) {
                    pics = Arrays.copyOf(pics, threshold);
                }

                final String[] picsRepl = new String[pics.length];
                for (int i = 0; i < picsRepl.length; i++) {
                    picsRepl[i] = langPropsService.get("picTagLabel", Latkes.getLocale());
                    pics[i] = "<img" + pics[i] + ">";

                    if (i > threshold) {
                        break;
                    }
                }

                ret = StringUtils.replaceEach(ret, pics, picsRepl);
            }

            String[] objs = StringUtils.substringsBetween(ret, "<object>", "</object>");
            if (null != objs) {
                if (objs.length > threshold) {
                    objs = Arrays.copyOf(objs, threshold);
                }

                final String[] objsRepl = new String[objs.length];
                for (int i = 0; i < objsRepl.length; i++) {
                    objsRepl[i] = langPropsService.get("objTagLabel", Latkes.getLocale());
                    objs[i] = "<object>" + objs[i] + "</object>";

                    if (i > threshold) {
                        break;
                    }
                }

                ret = StringUtils.replaceEach(ret, objs, objsRepl);
            }

            objs = StringUtils.substringsBetween(ret, "<video", "</video>");
            if (null != objs) {
                if (objs.length > threshold) {
                    objs = Arrays.copyOf(objs, threshold);
                }

                final String[] objsRepl = new String[objs.length];
                for (int i = 0; i < objsRepl.length; i++) {
                    objsRepl[i] = langPropsService.get("objTagLabel", Latkes.getLocale());
                    objs[i] = "<video" + objs[i] + "</video>";

                    if (i > threshold) {
                        break;
                    }
                }

                ret = StringUtils.replaceEach(ret, objs, objsRepl);
            }

            String tmp = Jsoup.clean(Jsoup.parse(ret).text(), Whitelist.none());
            if (tmp.length() >= length && null != pics) {
                tmp = StringUtils.substring(tmp, 0, length) + " ....";
                ret = tmp.replaceAll("\"", "'");

                articleCache.putArticleAbstract(articleId, ret);

                return ret;
            }

            String[] urls = StringUtils.substringsBetween(ret, "<a", "</a>");
            if (null != urls) {
                if (urls.length > threshold) {
                    urls = Arrays.copyOf(urls, threshold);
                }

                final String[] urlsRepl = new String[urls.length];
                for (int i = 0; i < urlsRepl.length; i++) {
                    urlsRepl[i] = langPropsService.get("urlTagLabel", Latkes.getLocale());
                    urls[i] = "<a" + urls[i] + "</a>";
                }

                ret = StringUtils.replaceEach(ret, urls, urlsRepl);
            }

            tmp = Jsoup.clean(Jsoup.parse(ret).text(), Whitelist.none());
            if (tmp.length() >= length) {
                tmp = StringUtils.substring(tmp, 0, length) + " ....";
            }

            ret = tmp.replaceAll("\"", "'");

            articleCache.putArticleAbstract(articleId, ret);

            return ret;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets ToC of the specified article.
     *
     * @param article the specified article
     * @return ToC
     */
    private String getArticleToC(final JSONObject article) {
        Stopwatchs.start("ToC");

        if (Article.ARTICLE_TYPE_C_THOUGHT == article.optInt(Article.ARTICLE_TYPE)) {
            return "";
        }

        try {
            final String content = article.optString(Article.ARTICLE_CONTENT);
            final Document doc = Jsoup.parse(content, StringUtils.EMPTY, Parser.htmlParser());
            doc.outputSettings().prettyPrint(false);
            final Elements hs = doc.select("h1, h2, h3, h4, h5");
            if (hs.size() < 3) {
                return "";
            }

            final StringBuilder listBuilder = new StringBuilder();
            listBuilder.append("<ul class=\"article-toc\">");
            for (int i = 0; i < hs.size(); i++) {
                final Element element = hs.get(i);
                final String tagName = element.tagName().toLowerCase();
                final String text = element.text();
                final String id = "toc_" + tagName + "_" + i;

                element.before("<span id='" + id + "'></span>");
                listBuilder.append("<li class='toc-").append(tagName).append("'><a data-id=\"").append(id).append("\" href=\"javascript:Comment._bgFade($('#").append(id).append("'))\">").append(text).append(
                        "</a></li>");
            }
            listBuilder.append("</ul>");

            article.put(Article.ARTICLE_CONTENT, doc.select("body").html());

            return listBuilder.toString();
        } finally {
            Stopwatchs.end();
        }
    }
}
