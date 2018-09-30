/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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

import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.RepositoryException;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Paginator;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Report;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.ReportRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.encoder.Encode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Report management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.2, Jun 27, 2018
 * @since 3.1.0
 */
@Service
public class ReportQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ReportQueryService.class);

    /**
     * Report repository.
     */
    @Inject
    private ReportRepository reportRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

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
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Comment query service.
     */
    @Inject
    private CommentQueryService commentQueryService;

    /**
     * Gets report by the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          {
     *                          "paginationCurrentPageNum": 1,
     *                          "paginationPageSize": 20,
     *                          "paginationWindowSize": 10
     *                          }, see {@link Pagination} for more details
     * @return for example,      <pre>
     * {
     *     "pagination": {
     *         "paginationPageCount": 100,
     *         "paginationPageNums": [1, 2, 3, 4, 5]
     *     },
     *     "reports": [{
     *         "oId": "",
     *         "reportUserName": "<a>/member/username</a>",
     *         "reportData": "<a>Article or user</a>",
     *         "reportDataType": int,
     *         "reportDataTypeStr": "",
     *         "reportType": int,
     *         "reportTypeStr": "",
     *         "reportMemo": "",
     *         "reportHandled": int,
     *
     *      }, ....]
     * }
     * </pre>
     * @throws ServiceException service exception
     * @see Pagination
     */
    public JSONObject getReports(final JSONObject requestJSONObject) throws ServiceException {
        final JSONObject ret = new JSONObject();

        final int currentPageNum = requestJSONObject.optInt(Pagination.PAGINATION_CURRENT_PAGE_NUM);
        final int pageSize = requestJSONObject.optInt(Pagination.PAGINATION_PAGE_SIZE);
        final int windowSize = requestJSONObject.optInt(Pagination.PAGINATION_WINDOW_SIZE);
        final Query query = new Query().setCurrentPageNum(currentPageNum).setPageSize(pageSize).
                addSort(Report.REPORT_HANDLED, SortDirection.ASCENDING).
                addSort(Keys.OBJECT_ID, SortDirection.DESCENDING);

        JSONObject result;
        try {
            result = reportRepository.get(query);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Get reports failed", e);

            throw new ServiceException(e);
        }

        final int pageCount = result.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_PAGE_COUNT);
        final JSONObject pagination = new JSONObject();
        ret.put(Pagination.PAGINATION, pagination);
        final List<Integer> pageNums = Paginator.paginate(currentPageNum, pageSize, pageCount, windowSize);
        pagination.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        pagination.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);

        final JSONArray data = result.optJSONArray(Keys.RESULTS);
        final List<JSONObject> records = CollectionUtils.jsonArrayToList(data);
        final List<JSONObject> reports = new ArrayList<>();
        for (final JSONObject record : records) {
            final JSONObject report = new JSONObject();
            report.put(Keys.OBJECT_ID, record.optString(Keys.OBJECT_ID));
            try {
                final String reportUserId = record.optString(Report.REPORT_USER_ID);
                final JSONObject reporter = userRepository.get(reportUserId);
                report.put(Report.REPORT_T_USERNAME, UserExt.getUserLink(reporter));
                report.put(Report.REPORT_T_TIME, new Date(record.optLong(Keys.OBJECT_ID)));

                final String dataId = record.optString(Report.REPORT_DATA_ID);
                final int dataType = record.optInt(Report.REPORT_DATA_TYPE);
                report.put(Report.REPORT_DATA_TYPE, dataType);
                String reportData = langPropsService.get("removedLabel");
                switch (dataType) {
                    case Report.REPORT_DATA_TYPE_C_ARTICLE:
                        report.put(Report.REPORT_T_DATA_TYPE_STR, langPropsService.get("articleLabel"));
                        final JSONObject article = articleRepository.get(dataId);
                        if (null != article) {
                            final String title = Encode.forHtml(article.optString(Article.ARTICLE_TITLE));
                            reportData = "<a href=\"" + Latkes.getServePath() + "/article/" + article.optString(Keys.OBJECT_ID) +
                                    "\" target=\"_blank\">" + Emotions.convert(title) + "</a>";
                        }

                        break;
                    case Report.REPORT_DATA_TYPE_C_COMMENT:
                        report.put(Report.REPORT_T_DATA_TYPE_STR, langPropsService.get("cmtLabel"));
                        final JSONObject comment = commentRepository.get(dataId);
                        if (null != comment) {
                            final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                            final JSONObject cmtArticle = articleRepository.get(articleId);
                            final String title = Encode.forHtml(cmtArticle.optString(Article.ARTICLE_TITLE));
                            final String commentId = comment.optString(Keys.OBJECT_ID);
                            final int cmtViewMode = UserExt.USER_COMMENT_VIEW_MODE_C_REALTIME;
                            reportData = commentQueryService.getCommentURL(commentId, cmtViewMode, Symphonys.getInt("articleCommentsPageSize"));
                        }

                        break;
                    case Report.REPORT_DATA_TYPE_C_USER:
                        report.put(Report.REPORT_T_DATA_TYPE_STR, langPropsService.get("accountLabel"));
                        final JSONObject reported = userRepository.get(dataId);
                        reportData = UserExt.getUserLink(reported);

                        break;
                    default:
                        LOGGER.log(Level.ERROR, "Unknown report data type [" + dataType + "]");

                        continue;
                }
                report.put(Report.REPORT_T_DATA, reportData);
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Builds report data failed", e);

                continue;
            }

            final int type = record.optInt(Report.REPORT_TYPE);
            report.put(Report.REPORT_TYPE, type);
            switch (type) {
                case Report.REPORT_TYPE_C_SPAM_AD:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("spamADLabel"));

                    break;
                case Report.REPORT_TYPE_C_PORNOGRAPHIC:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("pornographicLabel"));

                    break;
                case Report.REPORT_TYPE_C_VIOLATION_OF_REGULATIONS:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("violationOfRegulationsLabel"));

                    break;
                case Report.REPORT_TYPE_C_ALLEGEDLY_INFRINGING:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("allegedlyInfringingLabel"));

                    break;
                case Report.REPORT_TYPE_C_PERSONAL_ATTACKS:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("personalAttacksLabel"));

                    break;
                case Report.REPORT_TYPE_C_POSING_ACCOUNT:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("posingAccountLabel"));

                    break;
                case Report.REPORT_TYPE_C_SPAM_AD_ACCOUNT:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("spamADAccountLabel"));

                    break;
                case Report.REPORT_TYPE_C_PERSONAL_INFO_VIOLATION:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("personalInfoViolationLabel"));

                    break;
                case Report.REPORT_TYPE_C_OTHER:
                    report.put(Report.REPORT_T_TYPE_STR, langPropsService.get("miscLabel"));

                    break;
                default:
                    LOGGER.log(Level.ERROR, "Unknown report type [" + type + "]");

                    continue;
            }

            String memo = record.optString(Report.REPORT_MEMO);
            memo = Markdowns.toHTML(memo);
            memo = Markdowns.clean(memo, "");
            report.put(Report.REPORT_MEMO, memo);
            report.put(Report.REPORT_HANDLED, record.optInt(Report.REPORT_HANDLED));

            reports.add(report);
        }
        ret.put(Report.REPORTS, reports);

        return ret;
    }
}
