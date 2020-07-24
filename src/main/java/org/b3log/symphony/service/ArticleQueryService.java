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
package org.b3log.symphony.service;

import com.vdurmont.emoji.EmojiParser;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.http.RequestContext;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Locales;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Times;
import org.b3log.symphony.cache.ArticleCache;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.channel.ArticleChannel;
import org.b3log.symphony.processor.middleware.validate.UserRegisterValidationMidware;
import org.b3log.symphony.repository.*;
import org.b3log.symphony.util.*;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.TimeUnit;

/**
 * Article query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="https://qiankunpingtai.cn">qiankunpingtai</a>
 * @version 2.28.2.3, Jul 22, 2020
 * @since 0.2.0
 */
@Service
public class ArticleQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(ArticleQueryService.class);

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
     * Reward query service.
     */
    @Inject
    private RewardQueryService rewardQueryService;

    /**
     * Tag query service.
     */
    @Inject
    private TagQueryService tagQueryService;

    /**
     * Gets the question articles with the specified fetch size.
     *
     * @param sortMode       the specified sort mode, 0: default, 1: unanswered, 2: reward, 3: hot
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
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
     */
    public JSONObject getQuestionArticles(final int sortMode, final int currentPageNum, final int pageSize) {
        final JSONObject ret = new JSONObject();

        Query query;
        switch (sortMode) {
            case 0:
                query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                        setPage(currentPageNum, pageSize).
                        setFilter(makeQuestionArticleShowingFilter());
                break;
            case 1:
                query = new Query().
                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                        setPage(currentPageNum, pageSize);
                final CompositeFilter compositeFilter1 = makeQuestionArticleShowingFilter();
                final List<Filter> filters1 = new ArrayList<>();
                filters1.add(new PropertyFilter(Article.ARTICLE_COMMENT_CNT, FilterOperator.EQUAL, 0));
                filters1.addAll(compositeFilter1.getSubFilters());
                query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters1));
                break;
            case 2:
                final String id = String.valueOf(DateUtils.addMonths(new Date(), -1).getTime());
                query = new Query().
                        addSort(Article.ARTICLE_QNA_OFFER_POINT, SortDirection.DESCENDING).
                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                        setPage(currentPageNum, pageSize);
                final CompositeFilter compositeFilter2 = makeQuestionArticleShowingFilter();
                final List<Filter> filters2 = new ArrayList<>();
                filters2.add(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, id));
                filters2.addAll(compositeFilter2.getSubFilters());
                query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters2));
                break;
            case 3:
                query = new Query().
                        addSort(Article.REDDIT_SCORE, SortDirection.DESCENDING).
                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                        setPage(currentPageNum, pageSize).
                        setFilter(makeQuestionArticleShowingFilter());
                break;
            default:
                query = new Query().
                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                        setPage(currentPageNum, pageSize).
                        setFilter(makeQuestionArticleShowingFilter());
        }

        JSONObject result;
        try {
            Stopwatchs.start("Query question articles");

            result = articleRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles failed", e);

            return null;
        } finally {
            Stopwatchs.end();
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);

        final int windowSize = Symphonys.ARTICLE_LIST_WIN_SIZE;

        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) pageNums);

        final List<JSONObject> articles = (List<JSONObject>) result.opt(Keys.RESULTS);
        organizeArticles(articles);
        for (final JSONObject article : articles) {
            final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final String articleId = article.optString(Keys.OBJECT_ID);
            article.put(Common.OFFERED, rewardQueryService.isRewarded(articleAuthorId, articleId, Reward.TYPE_C_ACCEPT_COMMENT));
        }

        //final Integer participantsCnt = Symphonys.getInt("latestArticleParticipantsCnt");
        //genParticipants(articles, participantsCnt);
        ret.put(Article.ARTICLES, (Object) articles);

        return ret;
    }

    /**
     * Gets following user articles.
     *
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return following tag articles, returns an empty list if not found
     */
    public List<JSONObject> getFollowingUserArticles(final String userId, final int currentPageNum, final int pageSize) {
        final List<JSONObject> users = (List<JSONObject>) followQueryService.getFollowingUsers(userId, 1, Integer.MAX_VALUE).opt(Keys.RESULTS);
        if (users.isEmpty()) {
            return Collections.emptyList();
        }

        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).setPage(currentPageNum, pageSize);

        final List<String> followingUserIds = new ArrayList<>();
        for (final JSONObject user : users) {
            followingUserIds.add(user.optString(Keys.OBJECT_ID));
        }

        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID));
        filters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION));
        filters.add(new PropertyFilter(Article.ARTICLE_AUTHOR_ID, FilterOperator.IN, followingUserIds));
        query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
        addListProjections(query);

        JSONObject result;
        try {
            Stopwatchs.start("Query following user articles");

            result = articleRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets following user articles failed", e);

            return Collections.emptyList();
        } finally {
            Stopwatchs.end();
        }

        final List<JSONObject> ret = (List<JSONObject>) result.opt(Keys.RESULTS);
        organizeArticles(ret);

        return ret;
    }

    /**
     * Gets following tag articles.
     *
     * @param userId         the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return following tag articles, returns an empty list if not found
     */
    public List<JSONObject> getFollowingTagArticles(final String userId, final int currentPageNum, final int pageSize) {
        final List<JSONObject> tags = (List<JSONObject>) followQueryService.getFollowingTags(
                userId, 1, Integer.MAX_VALUE).opt(Keys.RESULTS);
        if (tags.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> articleFields = new ArrayList<>();
        articleFields.add(Keys.OBJECT_ID);
        articleFields.add(Article.ARTICLE_STICK);
        articleFields.add(Article.ARTICLE_CREATE_TIME);
        articleFields.add(Article.ARTICLE_UPDATE_TIME);
        articleFields.add(Article.ARTICLE_LATEST_CMT_TIME);
        articleFields.add(Article.ARTICLE_AUTHOR_ID);
        articleFields.add(Article.ARTICLE_TITLE);
        articleFields.add(Article.ARTICLE_STATUS);
        articleFields.add(Article.ARTICLE_VIEW_CNT);
        articleFields.add(Article.ARTICLE_TYPE);
        articleFields.add(Article.ARTICLE_PERMALINK);
        articleFields.add(Article.ARTICLE_TAGS);
        articleFields.add(Article.ARTICLE_LATEST_CMTER_NAME);
        articleFields.add(Article.ARTICLE_COMMENT_CNT);
        articleFields.add(Article.ARTICLE_ANONYMOUS);
        articleFields.add(Article.ARTICLE_PERFECT);
        articleFields.add(Article.ARTICLE_CONTENT);
        articleFields.add(Article.ARTICLE_QNA_OFFER_POINT);
        articleFields.add(Article.ARTICLE_SHOW_IN_LIST);

        return getArticlesByTags(currentPageNum, pageSize, articleFields, tags.toArray(new JSONObject[0]));
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
                    select(Article.ARTICLE_PERMALINK, Article.ARTICLE_TITLE).
                    setPage(1, 1).setPageCount(1);
            final JSONObject ret = articleRepository.getFirst(query);
            if (null == ret) {
                return null;
            }
            final String title = Escapes.escapeHTML(ret.optString(Article.ARTICLE_TITLE));
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
                    select(Article.ARTICLE_PERMALINK, Article.ARTICLE_TITLE).
                    setPage(1, 1).setPageCount(1);
            final JSONObject ret = articleRepository.getFirst(query);
            if (null == ret) {
                return null;
            }
            final String title = Escapes.escapeHTML(ret.optString(Article.ARTICLE_TITLE));
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
                new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID)
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
                new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID)
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
            final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).setPageCount(1).
                    setPage(currentPageNum, pageSize);
            if (null != types && types.length > 0) {
                final List<Filter> typeFilters = new ArrayList<>();
                for (int i = 0; i < types.length; i++) {
                    final int type = types[i];
                    typeFilters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.EQUAL, type));
                }
                final CompositeFilter typeFilter = new CompositeFilter(CompositeFilterOperator.OR, typeFilters);
                final List<Filter> filters = new ArrayList<>();
                filters.add(typeFilter);
                filters.add(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID));
                filters.add(new PropertyFilter(Article.ARTICLE_SHOW_IN_LIST, FilterOperator.NOT_EQUAL, Article.ARTICLE_SHOW_IN_LIST_C_NOT));
                query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
            } else {
                query.setFilter(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID));
            }

            return articleRepository.getList(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Gets domain articles.
     *
     * @param domainId       the specified domain id
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
     * @return result
     */
    public JSONObject getDomainArticles(final String domainId, final int currentPageNum, final int pageSize) {
        final JSONObject ret = new JSONObject();
        ret.put(Article.ARTICLES, (Object) Collections.emptyList());

        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, 0);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) Collections.emptyList());

        try {
            final List<JSONObject> domainTags = (List<JSONObject>) domainTagRepository.getByDomainId(domainId, 1, Integer.MAX_VALUE).opt(Keys.RESULTS);
            if (domainTags.isEmpty()) {
                return ret;
            }

            final List<String> tagIds = new ArrayList<>();
            for (final JSONObject domainTag : domainTags) {
                tagIds.add(domainTag.optString(Tag.TAG + "_" + Keys.OBJECT_ID));
            }

            final StringBuilder queryCount = new StringBuilder("select count(0) ").append(" from ");
            final StringBuilder queryList = new StringBuilder("select symphony_article.oId ").append(" from ");
            final StringBuilder queryStr = new StringBuilder(articleRepository.getName() + " symphony_article, ").
                    append(tagArticleRepository.getName() + " symphony_tag_article ").
                    append(" where symphony_article.oId=symphony_tag_article.article_oId and symphony_article.articleShowInList != ? ").
                    append(" and symphony_article.").append(Article.ARTICLE_STATUS).append("!=?").
                    append(" and symphony_article.").append(Article.ARTICLE_TYPE).append("!=?").
                    append(" and ").append("symphony_tag_article." + Tag.TAG + '_' + Keys.OBJECT_ID).append(" in ( ");
            for (int i = 0; i < tagIds.size(); i++) {
                queryStr.append(" ").append(tagIds.get(i));
                if (i < (tagIds.size() - 1)) {
                    queryStr.append(",");
                }
            }
            queryStr.append(")");
            queryStr.append(" order by ").append("symphony_tag_article." + Keys.OBJECT_ID + " ").append(" desc ");

            final List<JSONObject> tagArticlesCount = articleRepository.
                    select(queryCount.append(queryStr.toString()).toString(), Article.ARTICLE_SHOW_IN_LIST_C_NOT, Article.ARTICLE_STATUS_C_INVALID, Article.ARTICLE_TYPE_C_DISCUSSION);
            queryStr.append(" limit ").append((currentPageNum - 1) * pageSize).append(",").append(pageSize);
            final List<JSONObject> tagArticles = articleRepository.
                    select(queryList.append(queryStr.toString()).toString(), Article.ARTICLE_SHOW_IN_LIST_C_NOT, Article.ARTICLE_STATUS_C_INVALID, Article.ARTICLE_TYPE_C_DISCUSSION);
            if (tagArticles.size() <= 0) {
                return ret;
            }
            final int windowSize = Symphonys.ARTICLE_LIST_WIN_SIZE;
            final int pageCount = (int) Math.ceil((tagArticlesCount == null ? 0 : tagArticlesCount.get(0).optInt("count(0)")) / (double) pageSize);
            final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
            pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) pageNums);

            final Set<String> articleIds = new HashSet<>();
            for (final JSONObject tagArticle : tagArticles) {
                articleIds.add(tagArticle.optString(Keys.OBJECT_ID));
            }
            final Query query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds)).
                    setPageCount(1).addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
            final List<JSONObject> articles = (List<JSONObject>) articleRepository.get(query).opt(Keys.RESULTS);
            organizeArticles(articles);
            final Integer participantsCnt = Symphonys.ARTICLE_LIST_PARTICIPANTS_CNT;
            genParticipants(articles, participantsCnt);
            ret.put(Article.ARTICLES, (Object) articles);
            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets domain articles failed", e);
            return null;
        }
    }

    /**
     * Gets the relevant articles of the specified article with the specified fetch size.
     * <p>
     * The relevant articles exist the same tag with the specified article.
     * </p>
     *
     * @param article   the specified article
     * @param fetchSize the specified fetch size
     * @return relevant articles, returns an empty list if not found
     */
    public List<JSONObject> getRelevantArticles(final JSONObject article, final int fetchSize) {
        final String tagsString = article.optString(Article.ARTICLE_TAGS);
        String[] tagTitles = tagsString.split(",");
        final List<String> excludedB3logTitles = new ArrayList<>();
        for (int i = 0; i < tagTitles.length; i++) {
            if (!"B3log".equalsIgnoreCase(tagTitles[i])) {
                excludedB3logTitles.add(tagTitles[i]);
            }
        }
        if (excludedB3logTitles.size() < 1) {
            excludedB3logTitles.add("B3log");
        }
        tagTitles = excludedB3logTitles.toArray(new String[0]);
        final Set<String> fetchedArticleIds = new HashSet<>();
        try {
            List<JSONObject> ret = new ArrayList<>();
            final List<JSONObject> tags = new ArrayList<>();
            for (int i = 0; i < tagTitles.length; i++) {
                final String tagTitle = tagTitles[i];
                final JSONObject tag = tagRepository.getByTitle(tagTitle);
                tags.add(tag);
            }
            Collections.sort(tags, Comparator.comparingInt(t -> t.optInt(Tag.TAG_REFERENCE_CNT)));

            for (final JSONObject tag : tags) {
                final String tagId = tag.optString(Keys.OBJECT_ID);
                JSONObject result = tagArticleRepository.getByTagId(tagId, 1, fetchSize);
                final List<JSONObject> tagArticleRelations = (List<JSONObject>) result.opt(Keys.RESULTS);
                final Set<String> articleIds = new HashSet<>();
                for (final JSONObject tagArticleRelation : tagArticleRelations) {
                    final String articleId = tagArticleRelation.optString(Article.ARTICLE + '_' + Keys.OBJECT_ID);
                    if (fetchedArticleIds.contains(articleId)) {
                        continue;
                    }

                    articleIds.add(articleId);
                    fetchedArticleIds.add(articleId);
                }

                articleIds.remove(article.optString(Keys.OBJECT_ID));
                final Query query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds)).
                        select(Article.ARTICLE_TITLE, Article.ARTICLE_PERMALINK, Article.ARTICLE_AUTHOR_ID);
                ret.addAll(articleRepository.getList(query));
                if (ret.size() >= fetchSize) {
                    break;
                }
            }

            final int size = ret.size() > fetchSize ? fetchSize : ret.size();
            ret = ret.subList(0, size);
            if (ret.size() < fetchSize) {
                final List<JSONObject> hotArticles = getHotArticles(fetchSize - ret.size());
                ret.addAll(0, hotArticles);
            }

            organizeArticles(ret);
            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets relevant articles failed", e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets articles by the specified tags (order by article create date desc).
     *
     * @param tags           the specified tags
     * @param currentPageNum the specified page number
     * @param articleFields  the specified article fields to return
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     */
    public List<JSONObject> getArticlesByTags(final int currentPageNum, final int pageSize, final List<String> articleFields, final JSONObject... tags) {
        try {
//            final StringBuilder queryCount = new StringBuilder("select count(0) ").append(" from ");
            final StringBuilder queryList = new StringBuilder("select symphony_article.oId ").append(" from ");
            final StringBuilder queryStr = new StringBuilder(articleRepository.getName() + " symphony_article, ").append(tagArticleRepository.getName() + " symphony_tag_article ");
            queryStr.append(" where symphony_article.oId=symphony_tag_article.article_oId and symphony_article.articleShowInList != ? ");
            if (tags != null && tags.length > 0) {
                queryStr.append(" and ").append("symphony_tag_article." + Tag.TAG + '_' + Keys.OBJECT_ID).append(" in ( ");
                for (int i = 0; i < tags.length; i++) {
                    queryStr.append(" ").append(tags[i].optString(Keys.OBJECT_ID));
                    if (i < (tags.length - 1)) {
                        queryStr.append(",");
                    }
                }
                queryStr.append(")");
            }
            queryStr.append(" order by ").append("symphony_tag_article." + Keys.OBJECT_ID + " ").append(" desc ");
//            final List<JSONObject> tagArticlesCount = articleRepository.
//                    select(queryCount.append(queryStr.toString()).toString(), Article.ARTICLE_SHOW_IN_LIST_C_NOT);
            queryStr.append(" limit ").append((currentPageNum - 1) * pageSize).append(",").append(pageSize);
            final List<JSONObject> tagArticles = articleRepository.
                    select(queryList.append(queryStr.toString()).toString(), Article.ARTICLE_SHOW_IN_LIST_C_NOT);

            final Set<String> articleIds = new HashSet<>();
            for (int i = 0; i < tagArticles.size(); i++) {
                articleIds.add(tagArticles.get(i).optString(Keys.OBJECT_ID));
            }

            final Query query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds)).
                    addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
            for (final String articleField : articleFields) {
                query.select(articleField);
            }

            final List<JSONObject> ret = articleRepository.getList(query);
            organizeArticles(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles by tags [tagLength=" + tags.length + "] failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets articles by the specified city (order by article create date desc).
     *
     * @param city           the specified city
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     */
    public List<JSONObject> getArticlesByCity(final String city, final int currentPageNum, final int pageSize) {
        try {
            final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                    setFilter(CompositeFilterOperator.and(new PropertyFilter(Article.ARTICLE_CITY, FilterOperator.EQUAL, city),
                            new PropertyFilter(Article.ARTICLE_SHOW_IN_LIST, FilterOperator.NOT_EQUAL, Article.ARTICLE_SHOW_IN_LIST_C_NOT))).
                    setPageCount(1).setPage(currentPageNum, pageSize);
            final List<JSONObject> ret = articleRepository.getList(query);
            organizeArticles(ret);
            final Integer participantsCnt = Symphonys.ARTICLE_LIST_PARTICIPANTS_CNT;
            genParticipants(ret, participantsCnt);
            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles by city [" + city + "] failed", e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets articles by the specified tag (order by article create date desc).
     *
     * @param sortMode       the specified sort mode, 0: default, 1: hot, 2: score, 3: reply, 4: perfect
     * @param tag            the specified tag
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return articles, return an empty list if not found
     */
    public List<JSONObject> getArticlesByTag(final int sortMode, final JSONObject tag, final int currentPageNum, final int pageSize) {
        try {
            final StringBuilder queryCount = new StringBuilder("select count(0) ").append(" from ");
            final StringBuilder queryList = new StringBuilder("select symphony_article.oId").append(" from ");
            final StringBuilder queryStr = new StringBuilder(articleRepository.getName() + " symphony_article, ").append(tagArticleRepository.getName() + " symphony_tag_article ");
            queryStr.append("where symphony_article.oId=symphony_tag_article.article_oId and symphony_article.articleShowInList != ? ");
            switch (sortMode) {
                case 0:
                    queryStr.append(" and ").append("symphony_tag_article." + Tag.TAG + '_' + Keys.OBJECT_ID).append("=").append(tag.optString(Keys.OBJECT_ID)).
                            append(" order by ").append("symphony_tag_article." + Keys.OBJECT_ID + " ").append(" desc ");
                    break;
                case 1:
                    queryStr.append(" and ").append("symphony_tag_article." + Tag.TAG + '_' + Keys.OBJECT_ID).append("=").append(tag.optString(Keys.OBJECT_ID)).
                            append(" order by ").append("symphony_tag_article." + Article.ARTICLE_COMMENT_CNT + " ").append(" desc ").
                            append(",").append("symphony_tag_article." + Keys.OBJECT_ID + " ").append(" desc ");
                    break;
                case 2:
                    queryStr.append(" and ").append("symphony_tag_article." + Tag.TAG + '_' + Keys.OBJECT_ID).append("=").append(tag.optString(Keys.OBJECT_ID)).
                            append(" order by ").append("symphony_tag_article." + Article.REDDIT_SCORE + " ").append(" desc ").
                            append(",").append("symphony_tag_article." + Keys.OBJECT_ID + " ").append(" desc ");
                    break;
                case 3:
                    queryStr.append(" and ").append("symphony_tag_article." + Tag.TAG + '_' + Keys.OBJECT_ID).append("=").append(tag.optString(Keys.OBJECT_ID)).
                            append(" order by ").append("symphony_tag_article." + Article.ARTICLE_LATEST_CMT_TIME + " ").append(" desc ").
                            append(",").append("symphony_tag_article." + Keys.OBJECT_ID + " ").append(" desc ");
                    break;
                case 4:
                    queryStr.append(" and ").append("symphony_tag_article." + Tag.TAG + '_' + Keys.OBJECT_ID).append("=").append(tag.optString(Keys.OBJECT_ID)).
                            append(" order by ").append("symphony_tag_article." + Article.ARTICLE_PERFECT + " ").append(" desc ").
                            append(",").append("symphony_tag_article." + Keys.OBJECT_ID + " ").append(" desc ");
                    break;
                default:
                    LOGGER.warn("Unknown sort mode [" + sortMode + "]");
                    queryStr.append(" and ").append("symphony_tag_article." + Tag.TAG + '_' + Keys.OBJECT_ID).append("=").append(tag.optString(Keys.OBJECT_ID)).
                            append(" order by ").append(",").append("symphony_tag_article." + Keys.OBJECT_ID + " ").append(" desc ");

            }
            final List<JSONObject> tagArticleTotalCount = articleRepository.select(queryCount.append(queryStr.toString()).toString(), Article.ARTICLE_SHOW_IN_LIST_C_NOT);
            tag.put(Tag.TAG_REFERENCE_CNT, tagArticleTotalCount == null ? 0 : tagArticleTotalCount.get(0).optInt("count(0)"));
            queryStr.append(" limit ").append((currentPageNum - 1) * pageSize).append(",").append(pageSize);
            final List<JSONObject> tagArticleRelations = articleRepository.select(queryList.append(queryStr.toString()).toString(), Article.ARTICLE_SHOW_IN_LIST_C_NOT);
            final List<String> articleIds = new ArrayList<>();
            for (int i = 0; i < tagArticleRelations.size(); i++) {
                articleIds.add(tagArticleRelations.get(i).optString(Keys.OBJECT_ID));
            }
            Query query = new Query().setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.IN, articleIds));
            addListProjections(query);

            final List<JSONObject> ret = articleRepository.getList(query);
            switch (sortMode) {
                default:
                    LOGGER.warn("Unknown sort mode [" + sortMode + "]");
                case 0:
                    ret.sort((o1, o2) -> o2.optString(Keys.OBJECT_ID).compareTo(o1.optString(Keys.OBJECT_ID)));
                    break;
                case 1:
                    ret.sort((o1, o2) -> {
                        final int v = o2.optInt(Article.ARTICLE_COMMENT_CNT) - o1.optInt(Article.ARTICLE_COMMENT_CNT);
                        if (0 == v) {
                            return o2.optString(Keys.OBJECT_ID).compareTo(o1.optString(Keys.OBJECT_ID));
                        }
                        return v > 0 ? 1 : -1;
                    });
                    break;
                case 2:
                    ret.sort((o1, o2) -> {
                        final double v = o2.optDouble(Article.REDDIT_SCORE) - o1.optDouble(Article.REDDIT_SCORE);
                        if (0 == v) {
                            return o2.optString(Keys.OBJECT_ID).compareTo(o1.optString(Keys.OBJECT_ID));
                        }
                        return v > 0 ? 1 : -1;
                    });
                    break;
                case 3:
                    ret.sort((o1, o2) -> {
                        final long v = (o2.optLong(Article.ARTICLE_LATEST_CMT_TIME)
                                - o1.optLong(Article.ARTICLE_LATEST_CMT_TIME));
                        if (0 == v) {
                            return o2.optString(Keys.OBJECT_ID).compareTo(o1.optString(Keys.OBJECT_ID));
                        }
                        return v > 0 ? 1 : -1;
                    });
                    break;
                case 4:
                    ret.sort((o1, o2) -> {
                        final long v = (o2.optLong(Article.ARTICLE_PERFECT) - o1.optLong(Article.ARTICLE_PERFECT));
                        if (0 == v) {
                            return o2.optString(Keys.OBJECT_ID).compareTo(o1.optString(Keys.OBJECT_ID));
                        }
                        return v > 0 ? 1 : -1;
                    });
                    break;
            }

            organizeArticles(ret);

            final Integer participantsCnt = Symphonys.ARTICLE_LIST_PARTICIPANTS_CNT;
            genParticipants(ret, participantsCnt);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles by tag [tagTitle=" + tag.optString(Tag.TAG_TITLE) + "] failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets an article with {@link #organizeArticle(JSONObject)} by the specified id.
     * <p>
     * Saves thumbnail if it updated.
     * </p>
     *
     * @param articleId the specified id
     * @return article, return {@code null} if not found
     */
    public JSONObject getArticleById(final String articleId) {
        Stopwatchs.start("Get article by id");
        try {
            final JSONObject ret = articleRepository.get(articleId);
            if (null == ret) {
                return null;
            }

            final JSONObject articleDO = JSONs.clone(ret);

            organizeArticle(ret);

            final String generatedThumb = ret.optString(Article.ARTICLE_T_THUMBNAIL_URL);
            final String articleImg1 = ret.optString(Article.ARTICLE_IMG1_URL);
            if (StringUtils.isNotBlank(generatedThumb) && !StringUtils.equals(generatedThumb, articleImg1)) {
                try {
                    final Transaction transaction = articleRepository.beginTransaction();
                    articleDO.put(Article.ARTICLE_IMG1_URL, generatedThumb);
                    articleRepository.update(articleId, articleDO, Article.ARTICLE_IMG1_URL);
                    transaction.commit();
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Saves article img1 URL failed", e);
                }
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets an article [articleId=" + articleId + "] failed", e);

            return null;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets an article by the specified id.
     *
     * @param articleId the specified id
     * @return article, return {@code null} if not found
     */
    public JSONObject getArticle(final String articleId) {
        try {
            final JSONObject ret = articleRepository.get(articleId);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets an article [articleId=" + articleId + "] failed", e);

            return null;
        }
    }

    /**
     * Gets preview content of the article specified with the given article id.
     *
     * @param articleId the given article id
     * @param context   the specified request context
     * @return preview content
     */
    public String getArticlePreviewContent(final String articleId, final RequestContext context) {
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
                return Jsoup.clean(langPropsService.get("articleContentBlockLabel"), Whitelist.none());
            }

            final Set<String> userNames = userQueryService.getUserNames(ret);
            final JSONObject currentUser = Sessions.getUser();
            final String currentUserName = null == currentUser ? "" : currentUser.optString(User.USER_NAME);
            final String authorName = author.optString(User.USER_NAME);
            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType && !authorName.equals(currentUserName)) {
                boolean invited = false;
                for (final String userName : userNames) {
                    if (userName.equals(currentUserName)) {
                        invited = true;
                        break;
                    }
                }

                if (!invited) {
                    String blockContent = langPropsService.get("articleDiscussionLabel");
                    blockContent = blockContent.replace("{user}", UserExt.getUserLink(authorName));

                    return blockContent;
                }
            }

            ret = Emotions.convert(ret);
            ret = Markdowns.toHTML(ret);

            ret = Jsoup.clean(ret, Whitelist.none());
            if (ret.length() >= length) {
                ret = StringUtils.substring(ret, 0, length) + " ....";
            }

            return ret;
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the user articles with the specified user id, page number and page size.
     *
     * @param userId         the specified user id
     * @param anonymous      the specified article anonymous
     * @param currentPageNum the specified page number
     * @param pageSize       the specified page size
     * @return user articles, return an empty list if not found
     */
    public List<JSONObject> getUserArticles(final String userId, final int anonymous, final int currentPageNum, final int pageSize) {
        final Query query = new Query().addSort(Article.ARTICLE_CREATE_TIME, SortDirection.DESCENDING).
                setPage(currentPageNum, pageSize).
                setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Article.ARTICLE_AUTHOR_ID, FilterOperator.EQUAL, userId),
                        new PropertyFilter(Article.ARTICLE_ANONYMOUS, FilterOperator.EQUAL, anonymous),
                        new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID)));
        try {
            final JSONObject result = articleRepository.get(query);
            final List<JSONObject> ret = (List<JSONObject>) result.opt(Keys.RESULTS);
            if (ret.isEmpty()) {
                return ret;
            }
            final JSONObject pagination = result.optJSONObject(Pagination.PAGINATION);
            final int recordCount = pagination.optInt(Pagination.PAGINATION_RECORD_COUNT);
            final int pageCount = pagination.optInt(Pagination.PAGINATION_PAGE_COUNT);
            final JSONObject first = ret.get(0);
            first.put(Pagination.PAGINATION_RECORD_COUNT, recordCount);
            first.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
            organizeArticles(ret);
            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets user articles failed", e);
            return Collections.emptyList();
        }
    }

    /**
     * Gets side hot articles.
     *
     * @return side hot articles, returns an empty list if not found
     */
    public List<JSONObject> getSideHotArticles() {
        return articleCache.getSideHotArticles();
    }

    /**
     * Gets side random articles.
     *
     * @return recent articles, returns an empty list if not found
     */
    public List<JSONObject> getSideRandomArticles() {
        return articleCache.getSideRandomArticles();
    }

    /**
     * Makes article showing filters.
     *
     * @return filter the article showing to user
     */
    private CompositeFilter makeArticleShowingFilter() {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID));
        filters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION));
        return new CompositeFilter(CompositeFilterOperator.AND, filters);
    }

    /**
     * Makes recent articles showing filter.
     *
     * @return filter the article showing to user
     */
    private CompositeFilter makeRecentArticleShowingFilter() {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID));
        filters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION));
        filters.add(new PropertyFilter(Article.ARTICLE_TAGS, FilterOperator.NOT_EQUAL, Tag.TAG_TITLE_C_SANDBOX));
        filters.add(new PropertyFilter(Article.ARTICLE_TAGS, FilterOperator.NOT_LIKE, "B3log%"));
        filters.add(new PropertyFilter(Article.ARTICLE_SHOW_IN_LIST, FilterOperator.NOT_EQUAL, Article.ARTICLE_SHOW_IN_LIST_C_NOT));

        return new CompositeFilter(CompositeFilterOperator.AND, filters);
    }

    /**
     * Makes question articles showing filter.
     *
     * @return filter the article showing to user
     */
    private CompositeFilter makeQuestionArticleShowingFilter() {
        final List<Filter> filters = new ArrayList<>();
        filters.add(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.EQUAL, Article.ARTICLE_TYPE_C_QNA));
        filters.add(new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.NOT_EQUAL, Article.ARTICLE_STATUS_C_INVALID));
        filters.add(new PropertyFilter(Article.ARTICLE_TAGS, FilterOperator.NOT_EQUAL, Tag.TAG_TITLE_C_SANDBOX));
        filters.add(new PropertyFilter(Article.ARTICLE_SHOW_IN_LIST, FilterOperator.NOT_EQUAL, Article.ARTICLE_SHOW_IN_LIST_C_NOT));

        return new CompositeFilter(CompositeFilterOperator.AND, filters);
    }

    /**
     * Makes the top articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
     * @return top articles query
     */
    private Query makeTopQuery(final int currentPageNum, final int pageSize) {
        final Query query = new Query().
                addSort(Article.REDDIT_SCORE, SortDirection.DESCENDING).
                addSort(Article.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING).
                setPageCount(1).setPage(currentPageNum, pageSize);
        query.setFilter(makeArticleShowingFilter());
        return query;
    }

    /**
     * Gets the recent articles with the specified fetch size.
     *
     * @param sortMode       the specified sort mode, 0: default, 1: hot, 2: score, 3: reply
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
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
     */
    public JSONObject getRecentArticles(final int sortMode, final int currentPageNum, final int pageSize) {
        final JSONObject ret = new JSONObject();

        Query query;
        switch (sortMode) {
            case 0:
                query = new Query().
                        addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING).
                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                        setFilter(makeRecentArticleShowingFilter());
                break;
            case 1:
                final String id = String.valueOf(DateUtils.addMonths(new Date(), -1).getTime());
                query = new Query().
                        addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING).
                        addSort(Article.ARTICLE_COMMENT_CNT, SortDirection.DESCENDING).
                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
                final CompositeFilter compositeFilter = makeRecentArticleShowingFilter();
                final List<Filter> filters = new ArrayList<>();
                filters.add(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.GREATER_THAN_OR_EQUAL, id));
                filters.addAll(compositeFilter.getSubFilters());
                query.setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));
                break;
            case 2:
                query = new Query().
                        addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING).
                        addSort(Article.REDDIT_SCORE, SortDirection.DESCENDING).
                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                        setFilter(makeRecentArticleShowingFilter());
                break;
            case 3:
                query = new Query().
                        addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING).
                        addSort(Article.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING).
                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                        setFilter(makeRecentArticleShowingFilter());
                break;
            default:
                query = new Query().
                        addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING).
                        addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                        setFilter(makeRecentArticleShowingFilter());
        }
        query.setPage(currentPageNum, pageSize);
        addListProjections(query);

        JSONObject result = null;
        try {
            Stopwatchs.start("Query recent articles");

            result = articleRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles failed", e);
        } finally {
            Stopwatchs.end();
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final int windowSize = Symphonys.ARTICLE_LIST_WIN_SIZE;
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) pageNums);

        final List<JSONObject> articles = (List<JSONObject>) result.opt(Keys.RESULTS);
        organizeArticles(articles);

        //final Integer participantsCnt = Symphonys.getInt("latestArticleParticipantsCnt");
        //genParticipants(articles, participantsCnt);
        ret.put(Article.ARTICLES, (Object) articles);
        return ret;
    }

    /**
     * Gets the index recent articles.
     *
     * @return recent articles, returns an empty list if not found
     */
    public List<JSONObject> getIndexRecentArticles() {
        List<JSONObject> ret;
        try {
            Stopwatchs.start("Query index recent articles");
            try {
                final int fetchSize = 18;
                Query query = new Query().
                        setFilter(CompositeFilterOperator.and(
                                new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION),
                                new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID),
                                new PropertyFilter(Article.ARTICLE_SHOW_IN_LIST, FilterOperator.NOT_EQUAL, Article.ARTICLE_SHOW_IN_LIST_C_NOT))).
                        setPageCount(1).setPage(1, fetchSize).
                        addSort(Article.ARTICLE_LATEST_CMT_TIME, SortDirection.DESCENDING);
                ret = articleRepository.getList(query);

                final List<JSONObject> stickArticles = getStickArticles();
                if (!stickArticles.isEmpty()) {
                    final Iterator<JSONObject> i = ret.iterator();
                    while (i.hasNext()) {
                        final JSONObject article = i.next();
                        for (final JSONObject stickArticle : stickArticles) {
                            if (article.optString(Keys.OBJECT_ID).equals(stickArticle.optString(Keys.OBJECT_ID))) {
                                i.remove();
                            }
                        }
                    }

                    ret.addAll(0, stickArticles);
                    final int size = ret.size() < fetchSize ? ret.size() : fetchSize;
                    ret = ret.subList(0, size);
                }
            } finally {
                Stopwatchs.end();
            }

            organizeArticles(ret);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets index recent articles failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Gets the hot articles with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return hot articles, returns an empty list if not found
     */
    public List<JSONObject> getHotArticles(final int fetchSize) {
        final Query query = makeTopQuery(1, fetchSize);

        try {
            List<JSONObject> ret;
            Stopwatchs.start("Query hot articles");
            try {
                ret = articleRepository.getList(query);
            } finally {
                Stopwatchs.end();
            }

            organizeArticles(ret);

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

            return Collections.emptyList();
        }
    }

    /**
     * Gets the perfect articles with the specified fetch size.
     *
     * @param currentPageNum the specified current page number
     * @param pageSize       the specified page size
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
     */
    public JSONObject getPerfectArticles(final int currentPageNum, final int pageSize) {
        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setPage(currentPageNum, pageSize).
                setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Article.ARTICLE_PERFECT, FilterOperator.EQUAL, Article.ARTICLE_PERFECT_C_PERFECT),
                        new PropertyFilter(Article.ARTICLE_SHOW_IN_LIST, FilterOperator.NOT_EQUAL, Article.ARTICLE_SHOW_IN_LIST_C_NOT)));
        final JSONObject ret = new JSONObject();
        JSONObject result;
        try {
            Stopwatchs.start("Query perfect articles");

            result = articleRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles failed", e);

            return null;
        } finally {
            Stopwatchs.end();
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final int windowSize = Symphonys.ARTICLE_LIST_WIN_SIZE;
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, (Object) pageNums);

        final List<JSONObject> articles = (List<JSONObject>) result.opt(Keys.RESULTS);
        organizeArticles(articles);

        //final Integer participantsCnt = Symphonys.getInt("latestArticleParticipantsCnt");
        //genParticipants(articles, participantsCnt);
        ret.put(Article.ARTICLES, (Object) articles);
        return ret;
    }

    /**
     * Gets the index perfect articles.
     *
     * @return hot articles, returns an empty list if not found
     */
    public List<JSONObject> getIndexPerfectArticles() {
        return articleCache.getPerfectArticles();
    }

    /**
     * Organizes the specified articles.
     *
     * @param articles the specified articles
     * @see #organizeArticle(org.json.JSONObject)
     */
    public void organizeArticles(final List<JSONObject> articles) {
        Stopwatchs.start("Organize articles");
        try {
            final ForkJoinPool pool = new ForkJoinPool(Symphonys.PROCESSORS);
            pool.submit(() -> articles.parallelStream().forEach(article -> {
                try {
                    organizeArticle(article);
                } catch (final Exception e) {
                    LOGGER.log(Level.ERROR, "Organizes article [" + article.optString(Keys.OBJECT_ID) + "] failed", e);
                } finally {
                    // LOGGER.log(Level.INFO, "Stopwatch: {}{}", Strings.LINE_SEPARATOR, Stopwatchs.getTimingStat());
                    Stopwatchs.release();
                }
            }));
            pool.shutdown();
            pool.awaitTermination(10, TimeUnit.SECONDS);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Organizes articles failed", e);
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Organizes the specified article.
     * <ul>
     * <li>converts create/update/latest comment time (long) to date type and format string</li>
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
     *
     * @param article the specified article
     * @throws RepositoryException repository exception
     */
    public void organizeArticle(final JSONObject article) throws RepositoryException {
        article.put(Article.ARTICLE_T_ORIGINAL_CONTENT, article.optString(Article.ARTICLE_CONTENT));
        article.put(Common.OFFERED, false);
        toArticleDate(article);
        genArticleAuthor(article);

        final String previewContent = getArticleMetaDesc(article);
        article.put(Article.ARTICLE_T_PREVIEW_CONTENT, previewContent);
        article.put(Article.ARTICLE_T_THUMBNAIL_URL, getArticleThumbnail(article));

        final int articleType = article.optInt(Article.ARTICLE_TYPE);
        if (Article.ARTICLE_TYPE_C_THOUGHT != articleType) {
            String content = article.optString(Article.ARTICLE_CONTENT);
            content = Images.qiniuImgProcessing(content);
            article.put(Article.ARTICLE_CONTENT, content);
        }

        final String title = Escapes.escapeHTML(article.optString(Article.ARTICLE_TITLE));
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
            expired = stick + Symphonys.STICK_ARTICLE_TIME;
            final long remainsMills = Math.abs(System.currentTimeMillis() - expired);

            article.put(Article.ARTICLE_T_STICK_REMAINS, (int) Math.floor((double) remainsMills / 1000 / 60));
        } else {
            article.put(Article.ARTICLE_T_STICK_REMAINS, 0);
        }

        String articleLatestCmterName = article.optString(Article.ARTICLE_LATEST_CMTER_NAME);
        if (StringUtils.isNotBlank(articleLatestCmterName)
                && UserRegisterValidationMidware.invalidUserName(articleLatestCmterName)) {
            articleLatestCmterName = UserExt.ANONYMOUS_USER_NAME;
            article.put(Article.ARTICLE_LATEST_CMTER_NAME, articleLatestCmterName);
        }

        // builds tag objects
        final String tagsStr = article.optString(Article.ARTICLE_TAGS);
        final List<JSONObject> tags = tagQueryService.buildTagObjs(tagsStr);
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
        if (StringUtils.isBlank(content)) {
            return "";
        }

        final String html = Markdowns.toHTML(content);
        final String[] imgs = StringUtils.substringsBetween(html, "<img", ">");
        if (null == imgs || 0 == imgs.length) {
            return "";
        }

        String ret = null;
        for (int i = 0; i < imgs.length; i++) {
            ret = StringUtils.substringBetween(imgs[i], "data-src=\"", "\"");
            if (StringUtils.isBlank(ret)) {
                ret = StringUtils.substringBetween(imgs[i], "src=\"", "\"");
            }

            if (!StringUtils.containsIgnoreCase(ret, ".ico")) {
                break;
            }
        }
        if (StringUtils.isBlank(ret)) {
            return "";
        }

        if (Symphonys.QN_ENABLED) {
            final String qiniuDomain = Symphonys.UPLOAD_QINIU_DOMAIN;
            if (StringUtils.startsWith(ret, qiniuDomain)) {
                ret = StringUtils.substringBefore(ret, "?");
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
     * Converts the specified article create/update/latest comment time (long) to date type and format str.
     *
     * @param article the specified article
     */
    private void toArticleDate(final JSONObject article) {
        article.put(Common.TIME_AGO, Times.getTimeAgo(article.optLong(Article.ARTICLE_CREATE_TIME), Locales.getLocale()));
        article.put(Common.CMT_TIME_AGO, Times.getTimeAgo(article.optLong(Article.ARTICLE_LATEST_CMT_TIME), Locales.getLocale()));
        final Date createDate = new Date(article.optLong(Article.ARTICLE_CREATE_TIME));
        article.put(Article.ARTICLE_CREATE_TIME, createDate);
        article.put(Article.ARTICLE_CREATE_TIME_STR, DateFormatUtils.format(createDate, "yyyy-MM-dd HH:mm:ss"));
        final Date updateDate = new Date(article.optLong(Article.ARTICLE_UPDATE_TIME));
        article.put(Article.ARTICLE_UPDATE_TIME, updateDate);
        article.put(Article.ARTICLE_UPDATE_TIME_STR, DateFormatUtils.format(updateDate, "yyyy-MM-dd HH:mm:ss"));
        final Date latestCmtDate = new Date(article.optLong(Article.ARTICLE_LATEST_CMT_TIME));
        article.put(Article.ARTICLE_LATEST_CMT_TIME, latestCmtDate);
        article.put(Article.ARTICLE_LATEST_CMT_TIME_STR, DateFormatUtils.format(latestCmtDate, "yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Generates the specified article author name and thumbnail URL.
     *
     * @param article the specified article
     * @throws RepositoryException repository exception
     */
    private void genArticleAuthor(final JSONObject article) throws RepositoryException {
        final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);

        JSONObject author;
        if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == article.optInt(Article.ARTICLE_ANONYMOUS)) {
            author = userRepository.get(authorId);
        } else {
            author = userRepository.getAnonymousUser();
        }
        article.put(Article.ARTICLE_T_AUTHOR, author);
        article.put(Article.ARTICLE_T_AUTHOR_NAME, author.optString(User.USER_NAME));
        article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "210", avatarQueryService.getAvatarURLByUser(author, "210"));
        article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "48", avatarQueryService.getAvatarURLByUser(author, "48"));
        article.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "20", avatarQueryService.getAvatarURLByUser(author, "20"));
    }

    /**
     * Generates participants for the specified articles.
     *
     * @param articles        the specified articles
     * @param participantsCnt the specified generate size
     */
    public void genParticipants(final List<JSONObject> articles, final Integer participantsCnt) {
        Stopwatchs.start("Generates participants");
        try {
            for (final JSONObject article : articles) {
                article.put(Article.ARTICLE_T_PARTICIPANTS, (Object) Collections.emptyList());

                if (article.optInt(Article.ARTICLE_COMMENT_CNT) < 1) {
                    continue;
                }

                final List<JSONObject> articleParticipants = getArticleLatestParticipants(article.optString(Keys.OBJECT_ID), participantsCnt);
                article.put(Article.ARTICLE_T_PARTICIPANTS, (Object) articleParticipants);
            }
        } finally {
            Stopwatchs.end();
        }
    }

    /**
     * Gets the article participants (commenters) with the specified article article id and fetch size.
     *
     * @param articleId the specified article id
     * @param fetchSize the specified fetch size
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
     */
    public List<JSONObject> getArticleLatestParticipants(final String articleId, final int fetchSize) {
        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setFilter(new PropertyFilter(Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId)).
                select(Keys.OBJECT_ID, Comment.COMMENT_AUTHOR_ID).
                setPageCount(1).setPage(1, fetchSize);
        final List<JSONObject> ret = new ArrayList<>();

        try {
            final JSONObject result = commentRepository.get(query);

            final List<JSONObject> comments = new ArrayList<>();
            final List<JSONObject> records = (List<JSONObject>) result.opt(Keys.RESULTS);
            for (final JSONObject comment : records) {
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

                String thumbnailURL = AvatarQueryService.DEFAULT_AVATAR_URL;
                if (!UserExt.COM_BOT_EMAIL.equals(email)) {
                    thumbnailURL = avatarQueryService.getAvatarURLByUser(commenter, "48");
                }

                final JSONObject participant = new JSONObject();
                participant.put(Article.ARTICLE_T_PARTICIPANT_NAME, commenter.optString(User.USER_NAME));
                participant.put(Article.ARTICLE_T_PARTICIPANT_THUMBNAIL_URL, thumbnailURL);
                participant.put(Article.ARTICLE_T_PARTICIPANT_URL, commenter.optString(User.USER_URL));
                participant.put(Keys.OBJECT_ID, commenter.optString(Keys.OBJECT_ID));
                participant.put(Comment.COMMENT_T_ID, comment.optString(Keys.OBJECT_ID));

                ret.add(participant);
            }

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets article [" + articleId + "] participants failed", e);

            return Collections.emptyList();
        }
    }

    /**
     * Processes the specified article content.
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
     */
    public void processArticleContent(final JSONObject article) {
        Stopwatchs.start("Process content");

        try {
            final JSONObject author = article.optJSONObject(Article.ARTICLE_T_AUTHOR);
            if (null != author && UserExt.USER_STATUS_C_INVALID == author.optInt(UserExt.USER_STATUS)
                    || Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
                article.put(Article.ARTICLE_TITLE, langPropsService.get("articleTitleBlockLabel"));
                article.put(Article.ARTICLE_T_TITLE_EMOJI, langPropsService.get("articleTitleBlockLabel"));
                article.put(Article.ARTICLE_T_TITLE_EMOJI_UNICODE, langPropsService.get("articleTitleBlockLabel"));
                article.put(Article.ARTICLE_CONTENT, langPropsService.get("articleContentBlockLabel"));
                article.put(Article.ARTICLE_T_PREVIEW_CONTENT, Jsoup.clean(langPropsService.get("articleContentBlockLabel"), Whitelist.none()));
                article.put(Article.ARTICLE_T_TOC, "");
                article.put(Article.ARTICLE_REWARD_CONTENT, "");
                article.put(Article.ARTICLE_REWARD_POINT, 0);
                article.put(Article.ARTICLE_QNA_OFFER_POINT, 0);
                return;
            }

            article.put(Article.ARTICLE_T_PREVIEW_CONTENT, article.optString(Article.ARTICLE_TITLE));

            String articleContent = article.optString(Article.ARTICLE_CONTENT);
            article.put(Common.DISCUSSION_VIEWABLE, true);

            final JSONObject currentUser = Sessions.getUser();
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
                    blockContent = blockContent.replace("{user}", UserExt.getUserLink(authorName));

                    article.put(Article.ARTICLE_CONTENT, blockContent);
                    article.put(Common.DISCUSSION_VIEWABLE, false);
                    article.put(Article.ARTICLE_REWARD_CONTENT, "");
                    article.put(Article.ARTICLE_REWARD_POINT, 0);
                    article.put(Article.ARTICLE_QNA_OFFER_POINT, 0);
                    article.put(Article.ARTICLE_T_TOC, "");
                    article.put(Article.ARTICLE_AUDIO_URL, "");
                    return;
                }
            }

            if (Article.ARTICLE_TYPE_C_THOUGHT != articleType) {
                articleContent = shortLinkQueryService.linkArticle(articleContent);
                articleContent = Emotions.convert(articleContent);
                article.put(Article.ARTICLE_CONTENT, articleContent);
            }

            if (article.optInt(Article.ARTICLE_REWARD_POINT) > 0) {
                String rewardContent = article.optString(Article.ARTICLE_REWARD_CONTENT);
                rewardContent = shortLinkQueryService.linkArticle(rewardContent);
                rewardContent = Emotions.convert(rewardContent);
                article.put(Article.ARTICLE_REWARD_CONTENT, rewardContent);
            }

            markdown(article);
            articleContent = article.optString(Article.ARTICLE_CONTENT);

            if (Article.ARTICLE_TYPE_C_THOUGHT != articleType) {
                articleContent = MediaPlayers.renderAudio(articleContent);
                articleContent = MediaPlayers.renderVideo(articleContent);
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
     * @see Pagination
     */
    public JSONObject getArticles(final JSONObject requestJSONObject, final List<String> articleFields) {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setPage(currentPageNum, pageSize).
                addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);
        for (final String articleField : articleFields) {
            query.select(articleField);
        }

        if (requestJSONObject.has(Keys.OBJECT_ID)) {
            query.setFilter(new PropertyFilter(Keys.OBJECT_ID, FilterOperator.EQUAL, requestJSONObject.optString(Keys.OBJECT_ID)));
        }

        JSONObject result;
        try {
            result = articleRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets articles failed", e);
            return null;
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final List<JSONObject> articles = (List<JSONObject>) result.opt(Keys.RESULTS);
        organizeArticles(articles);
        ret.put(Article.ARTICLES, (Object) articles);
        return ret;
    }

    /**
     * Markdowns the specified article content.
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
    String getArticleMetaDesc(final JSONObject article) {
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
            final Elements hs = doc.select("body>h1, body>h2, body>h3, body>h4, body>h5, body>h6");
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
                element.attr("id", id);
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

    private void addListProjections(final Query query) {
        /**
         * 添加帖子是否展示字段
         */
        query.select(Keys.OBJECT_ID,
                Article.ARTICLE_STICK,
                Article.ARTICLE_CREATE_TIME,
                Article.ARTICLE_UPDATE_TIME,
                Article.ARTICLE_LATEST_CMT_TIME,
                Article.ARTICLE_AUTHOR_ID,
                Article.ARTICLE_TITLE,
                Article.ARTICLE_STATUS,
                Article.ARTICLE_VIEW_CNT,
                Article.ARTICLE_THANK_CNT,
                Article.ARTICLE_TYPE,
                Article.ARTICLE_PERMALINK,
                Article.ARTICLE_TAGS,
                Article.ARTICLE_LATEST_CMTER_NAME,
                Article.ARTICLE_COMMENT_CNT,
                Article.ARTICLE_ANONYMOUS,
                Article.ARTICLE_PERFECT,
                Article.ARTICLE_BAD_CNT,
                Article.ARTICLE_GOOD_CNT,
                Article.ARTICLE_COLLECT_CNT,
                Article.ARTICLE_WATCH_CNT,
                Article.ARTICLE_UA,
                Article.ARTICLE_CONTENT,
                Article.ARTICLE_QNA_OFFER_POINT,
                Article.ARTICLE_SHOW_IN_LIST
        );
    }

    private List<JSONObject> getStickArticles() {
        final Query query = new Query().
                setFilter(CompositeFilterOperator.and(
                        new PropertyFilter(Article.ARTICLE_STICK, FilterOperator.NOT_EQUAL, 0L),
                        new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.NOT_EQUAL, Article.ARTICLE_TYPE_C_DISCUSSION),
                        new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID))).
                setPageCount(1).setPage(1, 2).
                addSort(Article.ARTICLE_STICK, SortDirection.DESCENDING);
        try {
            return articleRepository.getList(query);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get stick articles failed", e);

            return Collections.emptyList();
        }
    }
}
