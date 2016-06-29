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
package org.b3log.symphony.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
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
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.Reward;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.CommentRepository;
import org.b3log.symphony.repository.PointtransferRepository;
import org.b3log.symphony.repository.RewardRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Pointtransfer query service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.15.2.1, Jun 29, 2016
 * @since 1.3.0
 */
@Service
public class PointtransferQueryService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(PointtransferQueryService.class.getName());

    /**
     * Pointtransfer repository.
     */
    @Inject
    private PointtransferRepository pointtransferRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Reward repository.
     */
    @Inject
    private RewardRepository rewardRepository;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Avatar query service.
     */
    @Inject
    private AvatarQueryService avatarQueryService;

    /**
     * Gets the latest pointtransfers with the specified user id, type and fetch size.
     *
     * @param userId the specified user id
     * @param type the specified type
     * @param fetchSize the specified fetch size
     * @return pointtransfers, returns an empty list if not found
     */
    public List<JSONObject> getLatestPointtransfers(final String userId, final int type, final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<JSONObject>();

        final List<Filter> userFilters = new ArrayList<Filter>();
        userFilters.add(new PropertyFilter(Pointtransfer.FROM_ID, FilterOperator.EQUAL, userId));
        userFilters.add(new PropertyFilter(Pointtransfer.TO_ID, FilterOperator.EQUAL, userId));

        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new CompositeFilter(CompositeFilterOperator.OR, userFilters));
        filters.add(new PropertyFilter(Pointtransfer.TYPE, FilterOperator.EQUAL, type));

        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING).setCurrentPageNum(1)
                .setPageSize(fetchSize).setFilter(new CompositeFilter(CompositeFilterOperator.AND, filters));

        try {
            final JSONObject result = pointtransferRepository.get(query);

            return CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets latest pointtransfers error", e);
        }

        return ret;
    }

    /**
     * Gets the top balance users with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopBalanceUsers(final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<JSONObject>();

        final Query query = new Query().addSort(UserExt.USER_POINT, SortDirection.DESCENDING).setCurrentPageNum(1)
                .setPageSize(fetchSize).
                setFilter(new PropertyFilter(UserExt.USER_JOIN_POINT_RANK,
                        FilterOperator.EQUAL, UserExt.USER_JOIN_POINT_RANK_C_JOIN));

        final int moneyUnit = Symphonys.getInt("pointExchangeUnit");
        try {
            final JSONObject result = userRepository.get(query);
            final List<JSONObject> users = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final JSONObject user : users) {
                if (UserExt.USER_APP_ROLE_C_HACKER == user.optInt(UserExt.USER_APP_ROLE)) {
                    user.put(UserExt.USER_T_POINT_HEX, Integer.toHexString(user.optInt(UserExt.USER_POINT)));
                } else {
                    user.put(UserExt.USER_T_POINT_CC, UserExt.toCCString(user.optInt(UserExt.USER_POINT)));
                }

                user.put(Common.MONEY, (int) Math.floor(user.optInt(UserExt.USER_POINT) / moneyUnit));

                avatarQueryService.fillUserAvatarURL(user);

                ret.add(user);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets top balance users error", e);
        }

        return ret;
    }

    /**
     * Gets the top consumption users with the specified fetch size.
     *
     * @param fetchSize the specified fetch size
     * @return users, returns an empty list if not found
     */
    public List<JSONObject> getTopConsumptionUsers(final int fetchSize) {
        final List<JSONObject> ret = new ArrayList<JSONObject>();

        final Query query = new Query().addSort(UserExt.USER_USED_POINT, SortDirection.DESCENDING).setCurrentPageNum(1)
                .setPageSize(fetchSize).
                setFilter(new PropertyFilter(UserExt.USER_JOIN_USED_POINT_RANK,
                        FilterOperator.EQUAL, UserExt.USER_JOIN_USED_POINT_RANK_C_JOIN));

        final int moneyUnit = Symphonys.getInt("pointExchangeUnit");
        try {
            final JSONObject result = userRepository.get(query);
            final List<JSONObject> users = CollectionUtils.jsonArrayToList(result.optJSONArray(Keys.RESULTS));

            for (final JSONObject user : users) {
                if (UserExt.USER_APP_ROLE_C_HACKER == user.optInt(UserExt.USER_APP_ROLE)) {
                    user.put(UserExt.USER_T_POINT_HEX, Integer.toHexString(user.optInt(UserExt.USER_POINT)));
                } else {
                    user.put(UserExt.USER_T_POINT_CC, UserExt.toCCString(user.optInt(UserExt.USER_POINT)));
                }

                user.put(Common.MONEY, (int) Math.floor(user.optInt(UserExt.USER_USED_POINT) / moneyUnit));

                avatarQueryService.fillUserAvatarURL(user);

                ret.add(user);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets top consumption users error", e);
        }

        return ret;
    }

    /**
     * Gets the user points with the specified user id, page number and page size.
     *
     * @param userId the specified user id
     * @param currentPageNum the specified page number
     * @param pageSize the specified page size
     * @return result json object, for example,      <pre>
     * {
     *     "paginationRecordCount": int,
     *     "rslts": java.util.List[{
     *         Pointtransfer
     *     }, ....]
     * }
     * </pre>
     *
     * @throws ServiceException service exception
     */
    public JSONObject getUserPoints(final String userId, final int currentPageNum, final int pageSize) throws ServiceException {
        final Query query = new Query().addSort(Keys.OBJECT_ID, SortDirection.DESCENDING)
                .setCurrentPageNum(currentPageNum).setPageSize(pageSize);
        final List<Filter> filters = new ArrayList<Filter>();
        filters.add(new PropertyFilter(Pointtransfer.FROM_ID, FilterOperator.EQUAL, userId));
        filters.add(new PropertyFilter(Pointtransfer.TO_ID, FilterOperator.EQUAL, userId));
        query.setFilter(new CompositeFilter(CompositeFilterOperator.OR, filters));

        try {
            final JSONObject ret = pointtransferRepository.get(query);
            final JSONArray records = ret.optJSONArray(Keys.RESULTS);
            for (int i = 0; i < records.length(); i++) {
                final JSONObject record = records.optJSONObject(i);

                record.put(Common.CREATE_TIME, new Date(record.optLong(Pointtransfer.TIME)));

                final String toId = record.optString(Pointtransfer.TO_ID);
                final String fromId = record.optString(Pointtransfer.FROM_ID);

                String typeStr = record.optString(Pointtransfer.TYPE);
                if (("3".equals(typeStr) && userId.equals(toId))
                        || ("5".equals(typeStr) && userId.equals(fromId))
                        || ("9".equals(typeStr) && userId.equals(toId))
                        || ("14".equals(typeStr) && userId.equals(toId))
                        || ("22".equals(typeStr) && userId.equals(toId))) {
                    typeStr += "In";
                }

                if (fromId.equals(userId)) {
                    record.put(Common.BALANCE, record.optInt(Pointtransfer.FROM_BALANCE));
                    record.put(Common.OPERATION, "-");
                } else {
                    record.put(Common.BALANCE, record.optInt(Pointtransfer.TO_BALANCE));
                    record.put(Common.OPERATION, "+");
                }

                record.put(Common.DISPLAY_TYPE, langPropsService.get("pointType" + typeStr + "Label"));

                final int type = record.optInt(Pointtransfer.TYPE);
                final String dataId = record.optString(Pointtransfer.DATA_ID);
                String desTemplate = langPropsService.get("pointType" + typeStr + "DesLabel");

                switch (type) {
                    case Pointtransfer.TRANSFER_TYPE_C_INIT:
                        desTemplate = desTemplate.replace("{point}", record.optString(Pointtransfer.SUM));

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE:
                        final JSONObject addArticle = articleRepository.get(dataId);
                        if (null == addArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String addArticleLink = "<a href=\""
                                + addArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + addArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", addArticleLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_UPDATE_ARTICLE:
                        final JSONObject updateArticle = articleRepository.get(dataId);
                        if (null == updateArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String updateArticleLink = "<a href=\""
                                + updateArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + updateArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", updateArticleLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ADD_COMMENT:
                        final JSONObject comment = commentRepository.get(dataId);

                        if (null == comment) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleId = comment.optString(Comment.COMMENT_ON_ARTICLE_ID);
                        final JSONObject commentArticle = articleRepository.get(articleId);

                        final String commentArticleLink = "<a href=\""
                                + commentArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + commentArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", commentArticleLink);

                        if ("3In".equals(typeStr)) {
                            final JSONObject commenter = userRepository.get(fromId);
                            final String commenterLink = "<a href=\"/member/" + commenter.optString(User.USER_NAME) + "\">"
                                    + commenter.optString(User.USER_NAME) + "</a>";

                            desTemplate = desTemplate.replace("{user}", commenterLink);
                        }

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD:
                        final JSONObject addArticleReword = articleRepository.get(dataId);
                        if (null == addArticleReword) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String addArticleRewordLink = "<a href=\""
                                + addArticleReword.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + addArticleReword.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", addArticleRewordLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ARTICLE_REWARD:
                        final JSONObject reward = rewardRepository.get(dataId);
                        String senderId = reward.optString(Reward.SENDER_ID);
                        if ("5In".equals(typeStr)) {
                            senderId = toId;
                        }
                        final String rewardArticleId = reward.optString(Reward.DATA_ID);

                        final JSONObject sender = userRepository.get(senderId);
                        final String senderLink = "<a href=\"/member/" + sender.optString(User.USER_NAME) + "\">"
                                + sender.optString(User.USER_NAME) + "</a>";
                        desTemplate = desTemplate.replace("{user}", senderLink);

                        final JSONObject articleReward = articleRepository.get(rewardArticleId);
                        if (null == articleReward) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleRewardLink = "<a href=\""
                                + articleReward.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + articleReward.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleRewardLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_COMMENT_REWARD:
                        final JSONObject reward14 = rewardRepository.get(dataId);
                        JSONObject user14;
                        if ("14In".equals(typeStr)) {
                            user14 = userRepository.get(fromId);
                        } else {
                            user14 = userRepository.get(toId);
                        }
                        final String commentId14 = reward14.optString(Reward.DATA_ID);
                        final JSONObject comment14 = commentRepository.get(commentId14);
                        if (null == comment14) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleId14 = comment14.optString(Comment.COMMENT_ON_ARTICLE_ID);

                        final String userLink14 = "<a href=\"/member/" + user14.optString(User.USER_NAME) + "\">"
                                + user14.optString(User.USER_NAME) + "</a>";
                        desTemplate = desTemplate.replace("{user}", userLink14);

                        final JSONObject article14 = articleRepository.get(articleId14);
                        final String articleLink = "<a href=\""
                                + article14.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article14.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ARTICLE_THANK:
                        final JSONObject thank22 = rewardRepository.get(dataId);
                        JSONObject user22;
                        if ("22In".equals(typeStr)) {
                            user22 = userRepository.get(fromId);
                        } else {
                            user22 = userRepository.get(toId);
                        }
                        final String articleId22 = thank22.optString(Reward.DATA_ID);
                        final JSONObject article22 = articleRepository.get(articleId22);
                        if (null == article22) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String userLink22 = "<a href=\"/member/" + user22.optString(User.USER_NAME) + "\">"
                                + user22.optString(User.USER_NAME) + "</a>";
                        desTemplate = desTemplate.replace("{user}", userLink22);

                        final String articleLink22 = "<a href=\""
                                + article22.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + article22.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", articleLink22);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_INVITE_REGISTER:
                        final JSONObject newUser = userRepository.get(dataId);
                        final String newUserLink = "<a href=\"/member/" + newUser.optString(User.USER_NAME) + "\">"
                                + newUser.optString(User.USER_NAME) + "</a>";
                        desTemplate = desTemplate.replace("{user}", newUserLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_INVITED_REGISTER:
                        final JSONObject referralUser = userRepository.get(dataId);
                        final String referralUserLink = "<a href=\"/member/" + referralUser.optString(User.USER_NAME) + "\">"
                                + referralUser.optString(User.USER_NAME) + "</a>";
                        desTemplate = desTemplate.replace("{user}", referralUserLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_YESTERDAY_LIVENESS_REWARD:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_1A0001_COLLECT:
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHARACTER:
                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_AT_PARTICIPANTS:
                        final JSONObject comment20 = commentRepository.get(dataId);
                        if (null == comment20) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String articleId20 = comment20.optString(Comment.COMMENT_ON_ARTICLE_ID);
                        final JSONObject atParticipantsArticle = articleRepository.get(articleId20);
                        if (null == atParticipantsArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String ArticleLink20 = "<a href=\""
                                + atParticipantsArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + atParticipantsArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", ArticleLink20);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_STICK_ARTICLE:
                        final JSONObject stickArticle = articleRepository.get(dataId);
                        if (null == stickArticle) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String stickArticleLink = "<a href=\""
                                + stickArticle.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + stickArticle.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", stickArticleLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ACCOUNT2ACCOUNT:
                        JSONObject user9;
                        if ("9In".equals(typeStr)) {
                            user9 = userRepository.get(fromId);
                        } else {
                            user9 = userRepository.get(toId);
                        }

                        final String userLink = "<a href=\"/member/" + user9.optString(User.USER_NAME) + "\">"
                                + user9.optString(User.USER_NAME) + "</a>";
                        desTemplate = desTemplate.replace("{user}", userLink);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ACTIVITY_CHECKIN_STREAK:
                        desTemplate = desTemplate.replace("{point}",
                                String.valueOf(Pointtransfer.TRANSFER_SUM_C_ACTIVITY_CHECKINT_STREAK));
                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_CHARGE:
                        final String yuan = dataId.split("-")[0];
                        desTemplate = desTemplate.replace("{yuan}", yuan);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_EXCHANGE:
                        final String exYuan = dataId;
                        desTemplate = desTemplate.replace("{yuan}", exYuan);

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ABUSE_DEDUCT:
                        desTemplate = desTemplate.replace("{action}", dataId);
                        desTemplate = desTemplate.replace("{point}", record.optString(Pointtransfer.SUM));

                        break;
                    case Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_BROADCAST:
                        final JSONObject addArticleBroadcast = articleRepository.get(dataId);
                        if (null == addArticleBroadcast) {
                            desTemplate = langPropsService.get("removedLabel");

                            break;
                        }

                        final String addArticleBroadcastLink = "<a href=\""
                                + addArticleBroadcast.optString(Article.ARTICLE_PERMALINK) + "\">"
                                + addArticleBroadcast.optString(Article.ARTICLE_TITLE) + "</a>";
                        desTemplate = desTemplate.replace("{article}", addArticleBroadcastLink);

                        break;
                    default:
                        LOGGER.warn("Invalid point type [" + type + "]");
                }

                desTemplate = Emotions.convert(desTemplate);

                record.put(Common.DESCRIPTION, desTemplate);
            }

            final int recordCnt = ret.optJSONObject(Pagination.PAGINATION).optInt(Pagination.PAGINATION_RECORD_COUNT);
            ret.remove(Pagination.PAGINATION);
            ret.put(Pagination.PAGINATION_RECORD_COUNT, recordCnt);

            return ret;
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Gets user points failed", e);
            throw new ServiceException(e);
        }
    }
}
