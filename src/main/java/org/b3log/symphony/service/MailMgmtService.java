package org.b3log.symphony.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.inject.Inject;
import org.b3log.latke.Keys;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.CompositeFilterOperator;
import org.b3log.latke.repository.FilterOperator;
import org.b3log.latke.repository.PropertyFilter;
import org.b3log.latke.repository.Query;
import org.b3log.latke.repository.SortDirection;
import org.b3log.latke.repository.Transaction;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.CollectionUtils;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Option;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.repository.ArticleRepository;
import org.b3log.symphony.repository.LivenessRepository;
import org.b3log.symphony.repository.OptionRepository;
import org.b3log.symphony.repository.UserRepository;
import org.b3log.symphony.util.Mails;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * Mail management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.0.0.0, Sep 14, 2016
 * @since 1.6.0
 */
@Service
public class MailMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(MailMgmtService.class.getName());

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
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Liveness repository.
     */
    @Inject
    private LivenessRepository livenessRepository;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Send weekly mails.
     */
    public void sendWeekly() {
        LOGGER.info("Sending weekly mails....");

        final long now = System.currentTimeMillis();
        final long sevenDaysAgo = now - 1000 * 60 * 60 * 24 * 7;

        try {
            final int memberCount = optionRepository.get(Option.ID_C_STATISTIC_MEMBER_COUNT).optInt(Option.OPTION_VALUE);
            final int userSize = memberCount / 7;

            // select receivers 
            final Query toUserQuery = new Query();
            toUserQuery.setCurrentPageNum(1).setPageCount(1).setPageSize(userSize).
                    setFilter(CompositeFilterOperator.and(
                            new PropertyFilter(UserExt.USER_SUB_MAIL_SEND_TIME, FilterOperator.LESS_THAN_OR_EQUAL,
                                    sevenDaysAgo),
                            new PropertyFilter(UserExt.USER_LATEST_LOGIN_TIME, FilterOperator.LESS_THAN_OR_EQUAL,
                                    sevenDaysAgo),
                            new PropertyFilter(UserExt.USER_SUB_MAIL_STATUS, FilterOperator.EQUAL,
                                    UserExt.USER_SUB_MAIL_STATUS_ENABLED),
                            new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_VALID)
                    )).addSort(Keys.OBJECT_ID, SortDirection.ASCENDING);
            final JSONArray receivers = userRepository.get(toUserQuery).optJSONArray(Keys.RESULTS);
            final Set<String> receiverMails = new HashSet<>();

            final Transaction transaction = userRepository.beginTransaction();
            for (int i = 0; i < receivers.length(); i++) {
                final JSONObject user = receivers.optJSONObject(i);
                final String email = user.optString(User.USER_EMAIL);
                if (Strings.isEmail(email)) {
                    receiverMails.add(email);

                    user.put(UserExt.USER_SUB_MAIL_SEND_TIME, now);
                    userRepository.update(user.optString(Keys.OBJECT_ID), user);
                }
            }
            transaction.commit();

            // select nice articles
            final Query articleQuery = new Query();
            articleQuery.setCurrentPageNum(1).setPageCount(1).setPageSize(Symphonys.getInt("sendcloud.batch.articleSize")).
                    setFilter(CompositeFilterOperator.and(
                            new PropertyFilter(Article.ARTICLE_CREATE_TIME, FilterOperator.GREATER_THAN_OR_EQUAL, sevenDaysAgo),
                            new PropertyFilter(Article.ARTICLE_STATUS, FilterOperator.EQUAL, Article.ARTICLE_STATUS_C_VALID)
                    )).addSort(Article.ARTICLE_COMMENT_CNT, SortDirection.DESCENDING).
                    addSort(Article.REDDIT_SCORE, SortDirection.DESCENDING);
            final List<JSONObject> articles = CollectionUtils.jsonArrayToList(
                    articleRepository.get(articleQuery).optJSONArray(Keys.RESULTS));

            // select nice users
            final int RANGE_SIZE = 64;
            final int SELECT_SIZE = 6;
            final Query userQuery = new Query();
            userQuery.setCurrentPageNum(1).setPageCount(1).setPageSize(RANGE_SIZE).
                    setFilter(new PropertyFilter(UserExt.USER_STATUS, FilterOperator.EQUAL, UserExt.USER_STATUS_C_VALID)).
                    addSort(UserExt.USER_ARTICLE_COUNT, SortDirection.DESCENDING).
                    addSort(UserExt.USER_COMMENT_COUNT, SortDirection.DESCENDING);
            final JSONArray rangeUsers = userRepository.get(userQuery).optJSONArray(Keys.RESULTS);
            final List<Integer> indices = CollectionUtils.getRandomIntegers(0, RANGE_SIZE, SELECT_SIZE);
            final List<JSONObject> selectUsers = new ArrayList<>();
            for (final Integer index : indices) {
                selectUsers.add(rangeUsers.getJSONObject(index));
            }

            final Map<String, List<String>> vars = new HashMap<>();

            String articlesTemplate = "";
            articlesTemplate = articlesTemplate.replace(articlesTemplate, articlesTemplate);

            final List<String> articlesValue = new ArrayList<>();
            final List<String> users1Value = new ArrayList<>();
            final List<String> users2Value = new ArrayList<>();
            for (int i = 0; i < receiverMails.size(); i++) {
                articlesValue.add(articlesTemplate);

            }

            vars.put(Article.ARTICLES, articlesValue);

            Mails.batchSend(langPropsService.get("weeklyEmailSubjectLabel"), Mails.TEMPLATE_NAME_WEEKLY,
                    new ArrayList<>(receiverMails), vars);
            LOGGER.info("Sent weekly mails [" + receiverMails.size() + "]");
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Sends weekly mails failed", e);
        }
    }
}
