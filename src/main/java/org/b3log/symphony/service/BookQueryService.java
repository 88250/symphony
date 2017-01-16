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

import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.urlfetch.*;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Book;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.BookRepository;
import org.b3log.symphony.repository.UserBookArticleRepository;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.inject.Inject;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Book query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.2, Jan 16, 2017
 * @since 1.9.0
 */
@Service
public class BookQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(AvatarQueryService.class);

    /**
     * Book repository.
     */
    @Inject
    private BookRepository bookRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * User-Book-Article repository.
     */
    @Inject
    private UserBookArticleRepository userBookArticleRepository;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

    /**
     * Get shared books by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example
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
    public JSONObject getSharedBooks(final JSONObject requestJSONObject, final Map<String, Class<?>> articleFields)
            throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).
                setFilter(new PropertyFilter(Article.ARTICLE_TYPE, FilterOperator.EQUAL, Article.ARTICLE_TYPE_C_BOOK));
        for (final Map.Entry<String, Class<?>> articleField : articleFields.entrySet()) {
            query.addProjection(articleField.getKey(), articleField.getValue());
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
            articleQueryService.organizeArticles(UserExt.USER_AVATAR_VIEW_MODE_C_STATIC, articles);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Organizes articles failed", e);

            throw new ServiceException(e);
        }

        final List<JSONObject> retArticles = new ArrayList<>();
        for (final JSONObject article : articles) {
            final String articleId = article.optString(Keys.OBJECT_ID);

            final JSONObject retArticle = new JSONObject();
            retArticle.put(Article.ARTICLE_T_ID, articleId);
            retArticle.put(Article.ARTICLE_TITLE, StringUtils.substringBetween(article.optString(Article.ARTICLE_TITLE), "《", "》"));
            retArticle.put(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL, article.optString(Article.ARTICLE_T_AUTHOR_THUMBNAIL_URL + "48"));

            try {
                final JSONObject userBookArticleRel = userBookArticleRepository.getByArticleId(articleId);
                final String bookId = userBookArticleRel.optString(Book.BOOK_T_ID);
                final JSONObject book = bookRepository.get(bookId);
                retArticle.put(Book.BOOK_ISBN13, book.optString(Book.BOOK_ISBN13));
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Get user book article rel failed [articleId=" + articleId + "]", e);

                continue;
            }

            retArticles.add(retArticle);
        }

        ret.put(Article.ARTICLES, retArticles);

        return ret;
    }

    /**
     * Gets a book's information with the specified ISBN.
     *
     * @param isbn the specified ISBN
     * @return book info, returns {@code null} if not found
     */
    @Transactional
    public JSONObject getBookByISBN(final String isbn) {
        final String url = "https://api.douban.com/v2/book/isbn/" + isbn;

        final URLFetchService urlFetchService = URLFetchServiceFactory.getURLFetchService();
        final HTTPRequest request = new HTTPRequest();
        request.addHeader(new HTTPHeader("User-Agent", Symphonys.USER_AGENT_BOT));

        try {
            request.setURL(new URL(url));

            final HTTPResponse response = urlFetchService.fetch(request);
            final String content = new String(response.getContent(), "UTF-8");
            final JSONObject result = new JSONObject(content);

            if (result.has("code")) {
                return null;
            }

            JSONObject ret = bookRepository.getByISBN(isbn);
            boolean add = false;
            if (null == ret) {
                ret = new JSONObject();
                add = true;
            }

            ret.put(Book.BOOK_ALT_TITLE, result.optString("alt_title"));
            ret.put(Book.BOOK_AUTHOR, result.optJSONArray("author").toString());
            ret.put(Book.BOOK_AUTHOR_INTRO, result.optString("author_intro").replace("\n", "\n\n"));
            ret.put(Book.BOOK_BINDING, result.optString("binding"));
            ret.put(Book.BOOK_CATALOG, result.optString("catalog"));

            final JSONObject series = result.optJSONObject("series");
            if (null != series) {
                ret.put(Book.BOOK_SERIES, series.optString("title"));
            } else {
                ret.put(Book.BOOK_SERIES, "");
            }
            ret.put(Book.BOOK_DOUBAN_URL, result.optString("alt"));
            ret.put(Book.BOOK_IMG_URL, result.optString("image"));
            ret.put(Book.BOOK_ISBN10, result.optString("isbn10"));
            ret.put(Book.BOOK_ISBN13, result.optString("isbn13"));
            ret.put(Book.BOOK_ORIGINAL_TITLE, result.optString("origin_title"));
            ret.put(Book.BOOK_PAGES, result.optString("pages"));
            ret.put(Book.BOOK_PRICE, result.optString("price"));
            ret.put(Book.BOOK_PUBLISH_DATE, result.optString("pubdate"));
            ret.put(Book.BOOK_PUBLISHER, result.optString("publisher"));
            ret.put(Book.BOOK_SUB_TITLE, result.optString("subtitle"));
            ret.put(Book.BOOK_SUMMARY, result.optString("summary").replace("\n", "\n\n"));

            final StringBuilder tagBuilder = new StringBuilder();
            final JSONArray tags = result.optJSONArray("tags");
            for (int i = 0; i < tags.length(); i++) {
                final JSONObject tag = tags.optJSONObject(i);

                tagBuilder.append(tag.optString("name")).append(",");
            }
            if (tagBuilder.length() > 0) {
                tagBuilder.deleteCharAt(tagBuilder.length() - 1);
            }
            ret.put(Book.BOOK_TAGS, tagBuilder.toString());

            ret.put(Book.BOOK_TITLE, result.optString("title"));
            ret.put(Book.BOOK_TRANSLATOR, result.optJSONArray("translator").toString());

            if (add) {
                bookRepository.add(ret);
            } else {
                bookRepository.update(ret.optString(Keys.OBJECT_ID), ret);
            }

            ret.put(Book.BOOK_TRANSLATOR, result.optJSONArray("translator"));
            ret.put(Book.BOOK_AUTHOR, result.optJSONArray("author"));

            return ret;
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Query book by ISBN [" + isbn + "] failed", e);

            return null;
        }
    }
}
