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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateFormatUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.event.Event;
import org.b3log.latke.event.EventException;
import org.b3log.latke.event.EventManager;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.User;
import org.b3log.latke.repository.*;
import org.b3log.latke.repository.annotation.Transactional;
import org.b3log.latke.repository.jdbc.JdbcRepository;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.service.annotation.Service;
import org.b3log.latke.util.Ids;
import org.b3log.latke.util.Strings;
import org.b3log.latke.util.URLs;
import org.b3log.symphony.event.EventTypes;
import org.b3log.symphony.model.*;
import org.b3log.symphony.repository.*;
import org.b3log.symphony.util.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Article management service.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 2.18.4.2, Oct 14, 2018
 * @since 0.2.0
 */
@Service
public class ArticleMgmtService {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleMgmtService.class);

    /**
     * Tag max count.
     */
    private static final int TAG_MAX_CNT = 4;

    /**
     * Comment repository.
     */
    @Inject
    private CommentRepository commentRepository;

    /**
     * Article repository.
     */
    @Inject
    private ArticleRepository articleRepository;

    /**
     * Tag repository.
     */
    @Inject
    private TagRepository tagRepository;

    /**
     * Tag-Article repository.
     */
    @Inject
    private TagArticleRepository tagArticleRepository;

    /**
     * User repository.
     */
    @Inject
    private UserRepository userRepository;

    /**
     * User-Tag repository.
     */
    @Inject
    private UserTagRepository userTagRepository;

    /**
     * Option repository.
     */
    @Inject
    private OptionRepository optionRepository;

    /**
     * Notification repository.
     */
    @Inject
    private NotificationRepository notificationRepository;

    /**
     * Revision repository.
     */
    @Inject
    private RevisionRepository revisionRepository;

    /**
     * Reward repository.
     */
    @Inject
    private RewardRepository rewardRepository;

    /**
     * Vote repository.
     */
    @Inject
    private VoteRepository voteRepository;

    /**
     * Tag management service.
     */
    @Inject
    private TagMgmtService tagMgmtService;

    /**
     * Event manager.
     */
    @Inject
    private EventManager eventManager;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Pointtransfer management service.
     */
    @Inject
    private PointtransferMgmtService pointtransferMgmtService;

    /**
     * Reward management service.
     */
    @Inject
    private RewardMgmtService rewardMgmtService;

    /**
     * Reward query service.
     */
    @Inject
    private RewardQueryService rewardQueryService;

    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Tag query service.
     */
    @Inject
    private TagQueryService tagQueryService;
    /**
     * Notification management service.
     */
    @Inject
    private NotificationMgmtService notificationMgmtService;

    /**
     * Liveness management service.
     */
    @Inject
    private LivenessMgmtService livenessMgmtService;

    /**
     * Search management service.
     */
    @Inject
    private SearchMgmtService searchMgmtService;

    /**
     * Audio management service.
     */
    @Inject
    private AudioMgmtService audioMgmtService;

    /**
     * Visit management service.
     */
    @Inject
    private VisitMgmtService visitMgmtService;

    /**
     * Determines whether the specified tag title exists in the specified tags.
     *
     * @param tagTitle the specified tag title
     * @param tags     the specified tags
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
     * Removes an article specified with the given article id. An article is removable if:
     * <ul>
     * <li>No comments</li>
     * <li>No watches, collects, ups, downs</li>
     * <li>No rewards</li>
     * <li>No thanks</li>
     * <li>In valid status</li>
     * </ul>
     * Sees https://github.com/b3log/symphony/issues/450 for more details.
     *
     * @param articleId the given article id
     * @throws ServiceException service exception
     */
    public void removeArticle(final String articleId) throws ServiceException {
        JSONObject article = null;

        try {
            article = articleRepository.get(articleId);
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets article [id=" + articleId + "] failed", e);
        }

        if (null == article) {
            return;
        }

        if (Article.ARTICLE_STATUS_C_VALID != article.optInt(Article.ARTICLE_STATUS)) {
            throw new ServiceException(langPropsService.get("articleLockedLabel"));
        }

        final int commentCnt = article.optInt(Article.ARTICLE_COMMENT_CNT);
        if (commentCnt > 0) {
            throw new ServiceException(langPropsService.get("removeArticleFoundCmtLabel"));
        }

        final int watchCnt = article.optInt(Article.ARTICLE_WATCH_CNT);
        final int collectCnt = article.optInt(Article.ARTICLE_COLLECT_CNT);
        final int ups = article.optInt(Article.ARTICLE_GOOD_CNT);
        final int downs = article.optInt(Article.ARTICLE_BAD_CNT);
        if (watchCnt > 0 || collectCnt > 0 || ups > 0 || downs > 0) {
            throw new ServiceException(langPropsService.get("removeArticleFoundWatchEtcLabel"));
        }

        final int rewardCnt = (int) rewardQueryService.rewardedCount(articleId, Reward.TYPE_C_ARTICLE);
        if (rewardCnt > 0) {
            throw new ServiceException(langPropsService.get("removeArticleFoundRewardLabel"));
        }

        final int thankCnt = (int) rewardQueryService.rewardedCount(articleId, Reward.TYPE_C_THANK_ARTICLE);
        if (thankCnt > 0) {
            throw new ServiceException(langPropsService.get("removeArticleFoundThankLabel"));
        }

        // Perform removal
        removeArticleByAdmin(articleId);
    }

    /**
     * Generates article's audio.
     *
     * @param article the specified article
     * @param userId  the specified user id
     */
    public void genArticleAudio(final JSONObject article, final String userId) {
        if (Article.ARTICLE_TYPE_C_THOUGHT == article.optInt(Article.ARTICLE_TYPE)
                || Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)) {
            return;
        }

        final String tags = article.optString(Article.ARTICLE_TAGS);
        if (StringUtils.containsIgnoreCase(tags, Tag.TAG_TITLE_C_SANDBOX)) {
            return;
        }

        final String articleId = article.optString(Keys.OBJECT_ID);
        String previewContent = article.optString(Article.ARTICLE_CONTENT);
        previewContent = Markdowns.toHTML(previewContent);
        final Document doc = Jsoup.parse(previewContent);
        final Elements elements = doc.select("a, img, iframe, object, video");
        for (final Element element : elements) {
            element.remove();
        }
        previewContent = Emotions.clear(doc.text());
        previewContent = StringUtils.substring(previewContent, 0, 512);
        final String contentToTTS = previewContent;

        new Thread(() -> {
            final Transaction transaction = articleRepository.beginTransaction();

            try {
                String audioURL = "";
                if (StringUtils.length(contentToTTS) < 96 || Runes.getChinesePercent(contentToTTS) < 40) {
                    LOGGER.trace("Content is too short to TTS [contentToTTS=" + contentToTTS + "]");
                } else {
                    audioURL = audioMgmtService.tts(contentToTTS, Article.ARTICLE, articleId, userId);
                }
                if (StringUtils.isBlank(audioURL)) {
                    return;
                }

                article.put(Article.ARTICLE_AUDIO_URL, audioURL);

                final JSONObject toUpdate = articleRepository.get(articleId);
                toUpdate.put(Article.ARTICLE_AUDIO_URL, audioURL);

                articleRepository.update(articleId, toUpdate);
                transaction.commit();

                if (StringUtils.isNotBlank(audioURL)) {
                    LOGGER.debug("Generated article [id=" + articleId + "] audio");
                }
            } catch (final Exception e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                LOGGER.log(Level.ERROR, "Updates article's audio URL failed", e);
            } finally {
                JdbcRepository.dispose();
            }
        }).start();
    }

    /**
     * Removes an article specified with the given article id. Calls this method will remove all existed data related
     * with the specified article forcibly.
     *
     * @param articleId the given article id
     */
    @Transactional
    public void removeArticleByAdmin(final String articleId) {
        try {
            final JSONObject article = articleRepository.get(articleId);
            if (null == article) {
                return;
            }

            Query query = new Query().setFilter(new PropertyFilter(
                    Comment.COMMENT_ON_ARTICLE_ID, FilterOperator.EQUAL, articleId)).setPageCount(1);
            final JSONArray comments = commentRepository.get(query).optJSONArray(Keys.RESULTS);
            final int commentCnt = comments.length();
            for (int i = 0; i < commentCnt; i++) {
                final JSONObject comment = comments.optJSONObject(i);
                final String commentId = comment.optString(Keys.OBJECT_ID);

                commentRepository.removeComment(commentId);
            }

            final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userRepository.get(authorId);
            author.put(UserExt.USER_ARTICLE_COUNT, author.optInt(UserExt.USER_ARTICLE_COUNT) - 1);
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            final String city = article.optString(Article.ARTICLE_CITY);
            final String cityStatId = city + "-ArticleCount";
            final JSONObject cityArticleCntOption = optionRepository.get(cityStatId);
            if (null != cityArticleCntOption) {
                cityArticleCntOption.put(Option.OPTION_VALUE, cityArticleCntOption.optInt(Option.OPTION_VALUE) - 1);
                optionRepository.update(cityStatId, cityArticleCntOption);
            }

            final JSONObject articleCntOption = optionRepository.get(Option.ID_C_STATISTIC_ARTICLE_COUNT);
            articleCntOption.put(Option.OPTION_VALUE, articleCntOption.optInt(Option.OPTION_VALUE) - 1);
            optionRepository.update(Option.ID_C_STATISTIC_ARTICLE_COUNT, articleCntOption);

            articleRepository.remove(articleId);

            // Remove article revisions
            query = new Query().setFilter(CompositeFilterOperator.and(
                    new PropertyFilter(Revision.REVISION_DATA_ID, FilterOperator.EQUAL, articleId),
                    new PropertyFilter(Revision.REVISION_DATA_TYPE, FilterOperator.EQUAL, Revision.DATA_TYPE_C_ARTICLE)
            ));
            final JSONArray articleRevisions = revisionRepository.get(query).optJSONArray(Keys.RESULTS);
            for (int i = 0; i < articleRevisions.length(); i++) {
                final JSONObject articleRevision = articleRevisions.optJSONObject(i);
                revisionRepository.remove(articleRevision.optString(Keys.OBJECT_ID));
            }

            final List<JSONObject> tagArticleRels = tagArticleRepository.getByArticleId(articleId);
            for (final JSONObject tagArticleRel : tagArticleRels) {
                final String tagId = tagArticleRel.optString(Tag.TAG + "_" + Keys.OBJECT_ID);
                final JSONObject tag = tagRepository.get(tagId);
                int cnt = tag.optInt(Tag.TAG_REFERENCE_CNT) - 1;
                cnt = cnt < 0 ? 0 : cnt;
                tag.put(Tag.TAG_REFERENCE_CNT, cnt);
                tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

                tagRepository.update(tagId, tag);
            }

            tagArticleRepository.removeByArticleId(articleId);
            notificationRepository.removeByDataId(articleId);
            rewardRepository.removeByDataId(articleId);
            voteRepository.removeByDataId(articleId);

            if (Symphonys.getBoolean("algolia.enabled")) {
                searchMgmtService.removeAlgoliaDocument(article);
            }

            if (Symphonys.getBoolean("es.enabled")) {
                searchMgmtService.removeESDocument(article, Article.ARTICLE);
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Removes an article error [id=" + articleId + "]", e);
        }
    }

    /**
     * Increments the view count of the specified article by the given visit.
     *
     * @param visit the given visit
     */
    public void incArticleViewCount(final JSONObject visit) {
        Symphonys.EXECUTOR_SERVICE.submit(() -> {
            final String visitURL = visit.optString(Visit.VISIT_URL);
            final String articleId = StringUtils.substringAfter(visitURL, "/article/");
            boolean visitedB4 = false;
            try {
                if ("1".equals(optionRepository.get(Option.ID_C_MISC_ARTICLE_VISIT_COUNT_MODE).optString(Option.OPTION_VALUE))) {
                    visitedB4 = visitMgmtService.add(visit);
                }
            } catch (final Exception e) {
                LOGGER.log(Level.ERROR, "Gets visit count mode failed", e);
            } finally {
                JdbcRepository.dispose();
            }

            if (visitedB4) {
                return;
            }

            final Transaction transaction = articleRepository.beginTransaction();
            try {
                final JSONObject article = articleRepository.get(articleId);
                if (null == article) {
                    if (transaction.isActive()) {
                        transaction.rollback();
                    }

                    return;
                }

                final int viewCnt = article.optInt(Article.ARTICLE_VIEW_CNT);
                article.put(Article.ARTICLE_VIEW_CNT, viewCnt + 1);
                article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());

                articleRepository.update(articleId, article);

                transaction.commit();
            } catch (final RepositoryException e) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }

                LOGGER.log(Level.ERROR, "Incs an article view count failed", e);
            }
        });
    }

    /**
     * Adds an article with the specified request json object.
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "articleTitle": "",
     *                          "articleTags": "",
     *                          "articleContent": "",
     *                          "articleEditorType": int, // optional, default to 0: markdown
     *                          "articleAuthorId": "",
     *                          "articleCommentable": boolean, // optional, default to true
     *                          "articleType": int, // optional, default to 0
     *                          "articleRewardContent": "", // optional, default to ""
     *                          "articleRewardPoint": int, // optional, default to 0
     *                          "articleQnAOfferPoint": int, // optional, default to 0
     *                          "articleIP": "", // optional, default to ""
     *                          "articleUA": "", // optional, default to ""
     *                          "articleAnonymous": int, // optional, default to 0 (public)
     *                          "articleAnonymousView": int // optional, default to 0 (use global)
     *                          , see {@link Article} for more details
     * @return generated article id
     * @throws ServiceException service exception
     */
    public synchronized String addArticle(final JSONObject requestJSONObject) throws ServiceException {
        final long currentTimeMillis = System.currentTimeMillis();
        final String authorId = requestJSONObject.optString(Article.ARTICLE_AUTHOR_ID);
        JSONObject author;

        final int rewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT, 0);
        if (rewardPoint < 0) {
            throw new ServiceException(langPropsService.get("invalidRewardPointLabel"));
        }

        final int articleAnonymous = requestJSONObject.optInt(Article.ARTICLE_ANONYMOUS);

        String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        articleTitle = Emotions.toAliases(articleTitle);
        articleTitle = Pangu.spacingText(articleTitle);
        articleTitle = StringUtils.trim(articleTitle);

        final int qnaOfferPoint = requestJSONObject.optInt(Article.ARTICLE_QNA_OFFER_POINT, 0);

        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
        if (Article.ARTICLE_TYPE_C_QNA == articleType && 20 > qnaOfferPoint) { // https://github.com/b3log/symphony/issues/672
            throw new ServiceException(langPropsService.get("invalidQnAOfferPointLabel"));
        }

        try {
            // check if admin allow to add article
            final JSONObject option = optionRepository.get(Option.ID_C_MISC_ALLOW_ADD_ARTICLE);

            if (!"0".equals(option.optString(Option.OPTION_VALUE))) {
                throw new ServiceException(langPropsService.get("notAllowAddArticleLabel"));
            }

            author = userRepository.get(authorId);
            if (UserExt.USER_STATUS_C_VALID != author.optInt(UserExt.USER_STATUS)) {
                throw new ServiceException(langPropsService.get("userStatusInvalidLabel"));
            }

            if (currentTimeMillis - author.optLong(Keys.OBJECT_ID) < Symphonys.getLong("newbieFirstArticle")) {
                String tip = langPropsService.get("newbieFirstArticleLabel");
                final long time = author.optLong(Keys.OBJECT_ID) + Symphonys.getLong("newbieFirstArticle");
                final String timeStr = DateFormatUtils.format(time, "yyyy-MM-dd HH:mm:ss");
                tip = tip.replace("${time}", timeStr);

                throw new ServiceException(tip);
            }

            if (currentTimeMillis - author.optLong(UserExt.USER_LATEST_ARTICLE_TIME) < Symphonys.getLong("minStepArticleTime")
                    && !Role.ROLE_ID_C_ADMIN.equals(author.optString(User.USER_ROLE))) {
                LOGGER.log(Level.WARN, "Adds article too frequent [userName={0}]", author.optString(User.USER_NAME));
                throw new ServiceException(langPropsService.get("tooFrequentArticleLabel"));
            }

            final int balance = author.optInt(UserExt.USER_POINT);
            if (Article.ARTICLE_ANONYMOUS_C_ANONYMOUS == articleAnonymous) {
                final int anonymousPoint = Symphonys.getInt("anonymous.point");
                if (balance < anonymousPoint) {
                    String anonymousEnabelPointLabel = langPropsService.get("anonymousEnabelPointLabel");
                    anonymousEnabelPointLabel
                            = anonymousEnabelPointLabel.replace("${point}", String.valueOf(anonymousPoint));
                    throw new ServiceException(anonymousEnabelPointLabel);
                }
            }

            if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                // Point
                final long followerCnt = followQueryService.getFollowerCount(authorId, Follow.FOLLOWING_TYPE_C_USER);
                final int addition = (int) Math.round(Math.sqrt(followerCnt));
                final int broadcast = Article.ARTICLE_TYPE_C_CITY_BROADCAST == articleType ?
                        Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST : 0;

                final int sum = Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE + addition + rewardPoint + qnaOfferPoint + broadcast;

                if (balance - sum < 0) {
                    throw new ServiceException(langPropsService.get("insufficientBalanceLabel"));
                }
            }

            if (Article.ARTICLE_TYPE_C_DISCUSSION != articleType) {
                final JSONObject maybeExist = articleRepository.getByTitle(articleTitle);
                if (null != maybeExist) {
                    final String existArticleAuthorId = maybeExist.optString(Article.ARTICLE_AUTHOR_ID);
                    String msg;
                    if (existArticleAuthorId.equals(authorId)) {
                        msg = langPropsService.get("duplicatedArticleTitleSelfLabel");
                        msg = msg.replace("{article}", "<a target='_blank' href='/article/" + maybeExist.optString(Keys.OBJECT_ID)
                                + "'>" + articleTitle + "</a>");
                    } else {
                        final JSONObject existArticleAuthor = userRepository.get(existArticleAuthorId);
                        final String userName = existArticleAuthor.optString(User.USER_NAME);
                        msg = langPropsService.get("duplicatedArticleTitleLabel");
                        msg = msg.replace("{user}", "<a target='_blank' href='/member/" + userName + "'>" + userName + "</a>");
                        msg = msg.replace("{article}", "<a target='_blank' href='/article/" + maybeExist.optString(Keys.OBJECT_ID)
                                + "'>" + articleTitle + "</a>");
                    }

                    throw new ServiceException(msg);
                }
            }
        } catch (final RepositoryException e) {
            throw new ServiceException(e);
        }

        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final String ret = Ids.genTimeMillisId();
            final JSONObject article = new JSONObject();
            article.put(Keys.OBJECT_ID, ret);

            article.put(Article.ARTICLE_TITLE, articleTitle);
            article.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));

            String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
            articleContent = Emotions.toAliases(articleContent);
            //articleContent = StringUtils.trim(articleContent) + " "; https://github.com/b3log/symphony/issues/389
            articleContent = StringUtils.replace(articleContent, langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            articleContent = StringUtils.replace(articleContent, langPropsService.get("uploadingLabel", Locale.US), "");
            article.put(Article.ARTICLE_CONTENT, articleContent);

            String rewardContent = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
            rewardContent = Emotions.toAliases(rewardContent);
            article.put(Article.ARTICLE_REWARD_CONTENT, rewardContent);

            article.put(Article.ARTICLE_EDITOR_TYPE, requestJSONObject.optInt(Article.ARTICLE_EDITOR_TYPE));
            article.put(Article.ARTICLE_AUTHOR_ID, authorId);
            article.put(Article.ARTICLE_COMMENT_CNT, 0);
            article.put(Article.ARTICLE_VIEW_CNT, 0);
            article.put(Article.ARTICLE_GOOD_CNT, 0);
            article.put(Article.ARTICLE_BAD_CNT, 0);
            article.put(Article.ARTICLE_COLLECT_CNT, 0);
            article.put(Article.ARTICLE_WATCH_CNT, 0);
            article.put(Article.ARTICLE_COMMENTABLE, requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE, true));
            article.put(Article.ARTICLE_CREATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_UPDATE_TIME, currentTimeMillis);
            article.put(Article.ARTICLE_LATEST_CMT_TIME, 0);
            article.put(Article.ARTICLE_LATEST_CMTER_NAME, "");
            article.put(Article.ARTICLE_PERMALINK, "/article/" + ret);
            article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());
            article.put(Article.REDDIT_SCORE, 0);
            article.put(Article.ARTICLE_STATUS, Article.ARTICLE_STATUS_C_VALID);
            article.put(Article.ARTICLE_TYPE, articleType);
            article.put(Article.ARTICLE_REWARD_POINT, rewardPoint);
            article.put(Article.ARTICLE_QNA_OFFER_POINT, qnaOfferPoint);
            article.put(Article.ARTICLE_PUSH_ORDER, 0);
            article.put(Article.ARTICLE_IMG1_URL, "");
            String city = "";
            if (UserExt.USER_GEO_STATUS_C_PUBLIC == author.optInt(UserExt.USER_GEO_STATUS)) {
                city = author.optString(UserExt.USER_CITY);
            }
            article.put(Article.ARTICLE_CITY, city);
            article.put(Article.ARTICLE_ANONYMOUS, articleAnonymous);
            article.put(Article.ARTICLE_PERFECT, Article.ARTICLE_PERFECT_C_NOT_PERFECT);
            article.put(Article.ARTICLE_ANONYMOUS_VIEW,
                    requestJSONObject.optInt(Article.ARTICLE_ANONYMOUS_VIEW, Article.ARTICLE_ANONYMOUS_VIEW_C_USE_GLOBAL));
            article.put(Article.ARTICLE_AUDIO_URL, "");

            String articleTags = article.optString(Article.ARTICLE_TAGS);
            articleTags = Tag.formatTags(articleTags);
            boolean sandboxEnv = false;
            if (StringUtils.containsIgnoreCase(articleTags, Tag.TAG_TITLE_C_SANDBOX)) {
                articleTags = Tag.TAG_TITLE_C_SANDBOX;
                sandboxEnv = true;
            }

            String[] tagTitles = articleTags.split(",");
            if (!sandboxEnv && tagTitles.length < TAG_MAX_CNT && tagTitles.length < 3
                    && Article.ARTICLE_TYPE_C_DISCUSSION != articleType
                    && Article.ARTICLE_TYPE_C_THOUGHT != articleType && !Tag.containsReservedTags(articleTags)) {
                final String content = article.optString(Article.ARTICLE_TITLE)
                        + " " + Jsoup.parse("<p>" + article.optString(Article.ARTICLE_CONTENT) + "</p>").text();

                final List<String> genTags = tagQueryService.generateTags(content, 1);
                if (!genTags.isEmpty()) {
                    articleTags = articleTags + "," + StringUtils.join(genTags, ",");
                    articleTags = Tag.formatTags(articleTags);
                    articleTags = Tag.useHead(articleTags, TAG_MAX_CNT);
                }
            }

            if (StringUtils.isBlank(articleTags)) {
                articleTags = "B3log";
            }

            articleTags = Tag.formatTags(articleTags);
            if (Article.ARTICLE_TYPE_C_QNA == articleType && !StringUtils.contains(articleTags, "Q&A")) {
                articleTags += ",Q&A";
            }
            article.put(Article.ARTICLE_TAGS, articleTags);
            tagTitles = articleTags.split(",");

            tag(tagTitles, article, author);

            final String ip = requestJSONObject.optString(Article.ARTICLE_IP);
            article.put(Article.ARTICLE_IP, ip);

            String ua = requestJSONObject.optString(Article.ARTICLE_UA);
            if (StringUtils.length(ua) > Common.MAX_LENGTH_UA) {
                ua = StringUtils.substring(ua, 0, Common.MAX_LENGTH_UA);
            }
            article.put(Article.ARTICLE_UA, ua);

            article.put(Article.ARTICLE_STICK, 0L);

            final JSONObject articleCntOption = optionRepository.get(Option.ID_C_STATISTIC_ARTICLE_COUNT);
            final int articleCnt = articleCntOption.optInt(Option.OPTION_VALUE);
            articleCntOption.put(Option.OPTION_VALUE, articleCnt + 1);
            optionRepository.update(Option.ID_C_STATISTIC_ARTICLE_COUNT, articleCntOption);

            if (!StringUtils.isBlank(city)) {
                final String cityStatId = city + "-ArticleCount";
                JSONObject cityArticleCntOption = optionRepository.get(cityStatId);

                if (null == cityArticleCntOption) {
                    cityArticleCntOption = new JSONObject();
                    cityArticleCntOption.put(Keys.OBJECT_ID, cityStatId);
                    cityArticleCntOption.put(Option.OPTION_VALUE, 1);
                    cityArticleCntOption.put(Option.OPTION_CATEGORY, city + "-statistic");

                    optionRepository.add(cityArticleCntOption);
                } else {
                    final int cityArticleCnt = cityArticleCntOption.optInt(Option.OPTION_VALUE);
                    cityArticleCntOption.put(Option.OPTION_VALUE, cityArticleCnt + 1);

                    optionRepository.update(cityStatId, cityArticleCntOption);
                }
            }

            author.put(UserExt.USER_ARTICLE_COUNT, author.optInt(UserExt.USER_ARTICLE_COUNT) + 1);
            author.put(UserExt.USER_LATEST_ARTICLE_TIME, currentTimeMillis);
            // Updates user article count (and new tag count), latest article time
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            final String articleId = articleRepository.add(article);

            if (Article.ARTICLE_TYPE_C_THOUGHT != articleType) {
                // Revision
                final JSONObject revision = new JSONObject();
                revision.put(Revision.REVISION_AUTHOR_ID, authorId);

                final JSONObject revisionData = new JSONObject();
                revisionData.put(Article.ARTICLE_TITLE, articleTitle);
                revisionData.put(Article.ARTICLE_CONTENT, articleContent);

                revision.put(Revision.REVISION_DATA, revisionData.toString());
                revision.put(Revision.REVISION_DATA_ID, articleId);
                revision.put(Revision.REVISION_DATA_TYPE, Revision.DATA_TYPE_C_ARTICLE);

                revisionRepository.add(revision);
            }

            transaction.commit();

            try {
                Thread.sleep(50); // wait for db write to avoid article duplication
            } catch (final Exception e) {
            }

            // Grows the tag graph
            tagMgmtService.relateTags(article.optString(Article.ARTICLE_TAGS));

            if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                // Point
                final long followerCnt = followQueryService.getFollowerCount(authorId, Follow.FOLLOWING_TYPE_C_USER);
                final int addition = (int) Math.round(Math.sqrt(followerCnt));

                pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                        Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE,
                        Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE + addition, articleId, System.currentTimeMillis(), "");

                if (rewardPoint > 0) { // Enable reward
                    pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD,
                            Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_REWARD, articleId, System.currentTimeMillis(), "");
                }

                if (Article.ARTICLE_TYPE_C_CITY_BROADCAST == articleType) {
                    pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_BROADCAST,
                            Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST, articleId, System.currentTimeMillis(), "");
                }

                // Liveness
                livenessMgmtService.incLiveness(authorId, Liveness.LIVENESS_ARTICLE);
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Article.ARTICLE, article);
            try {
                eventManager.fireEventAsynchronously(new Event<>(EventTypes.ADD_ARTICLE, eventData));
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
     *                          "oId": "",
     *                          "articleTitle": "",
     *                          "articleTags": "",
     *                          "articleContent": "",
     *                          "articleEditorType": "",
     *                          "articleCommentable": boolean, // optional, default to true
     *                          "articleType": int // optional, default to 0
     *                          "articleRewardContent": "", // optional, default to ""
     *                          "articleRewardPoint": int, // optional, default to 0
     *                          "articleQnAOfferPoint": int, // optional, default to 0
     *                          "articleIP": "", // optional, default to ""
     *                          "articleUA": "", // optional default to ""
     *                          , see {@link Article} for more details
     * @throws ServiceException service exception
     */
    public synchronized void updateArticle(final JSONObject requestJSONObject) throws ServiceException {
        String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);

        String articleId;
        JSONObject oldArticle;
        String authorId;
        JSONObject author;
        int updatePointSum;
        int articleAnonymous = 0;

        try {
            // check if admin allow to add article
            final JSONObject option = optionRepository.get(Option.ID_C_MISC_ALLOW_ADD_ARTICLE);

            if (!"0".equals(option.optString(Option.OPTION_VALUE))) {
                throw new ServiceException(langPropsService.get("notAllowAddArticleLabel"));
            }

            articleId = requestJSONObject.optString(Keys.OBJECT_ID);
            oldArticle = articleRepository.get(articleId);
            authorId = oldArticle.optString(Article.ARTICLE_AUTHOR_ID);
            author = userRepository.get(authorId);
            if (UserExt.USER_STATUS_C_VALID != author.optInt(UserExt.USER_STATUS)) {
                throw new ServiceException(langPropsService.get("userStatusInvalidLabel"));
            }

            final long followerCnt = followQueryService.getFollowerCount(authorId, Follow.FOLLOWING_TYPE_C_USER);
            int addition = (int) Math.round(Math.sqrt(followerCnt));
            final long collectCnt = followQueryService.getFollowerCount(articleId, Follow.FOLLOWING_TYPE_C_ARTICLE);
            final long watchCnt = followQueryService.getFollowerCount(articleId, Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH);
            addition += (collectCnt + watchCnt) * 2;
            updatePointSum = Pointtransfer.TRANSFER_SUM_C_UPDATE_ARTICLE + addition;

            articleAnonymous = oldArticle.optInt(Article.ARTICLE_ANONYMOUS);

            if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                // Point
                final int balance = author.optInt(UserExt.USER_POINT);
                if (balance - updatePointSum < 0) {
                    throw new ServiceException(langPropsService.get("insufficientBalanceLabel"));
                }
            }

            final JSONObject maybeExist = articleRepository.getByTitle(articleTitle);
            if (null != maybeExist) {
                if (!oldArticle.optString(Article.ARTICLE_TITLE).equals(articleTitle)) {
                    final String existArticleAuthorId = maybeExist.optString(Article.ARTICLE_AUTHOR_ID);
                    String msg;
                    if (existArticleAuthorId.equals(authorId)) {
                        msg = langPropsService.get("duplicatedArticleTitleSelfLabel");
                        msg = msg.replace("{article}", "<a target='_blank' href='/article/" + maybeExist.optString(Keys.OBJECT_ID)
                                + "'>" + articleTitle + "</a>");
                    } else {
                        final JSONObject existArticleAuthor = userRepository.get(existArticleAuthorId);
                        final String userName = existArticleAuthor.optString(User.USER_NAME);
                        msg = langPropsService.get("duplicatedArticleTitleLabel");
                        msg = msg.replace("{user}", "<a target='_blank' href='/member/" + userName + "'>" + userName + "</a>");
                        msg = msg.replace("{article}", "<a target='_blank' href='/article/" + maybeExist.optString(Keys.OBJECT_ID)
                                + "'>" + articleTitle + "</a>");
                    }

                    throw new ServiceException(msg);
                }
            }
        } catch (final RepositoryException e) {
            throw new ServiceException(e);
        }

        final int qnaOfferPoint = requestJSONObject.optInt(Article.ARTICLE_QNA_OFFER_POINT, 0);
        if (qnaOfferPoint < oldArticle.optInt(Article.ARTICLE_QNA_OFFER_POINT)) { // Increase only to prevent lowering points when adopting answer
            throw new ServiceException(langPropsService.get("qnaOfferPointMustMoreThanOldLabel"));
        }
        oldArticle.put(Article.ARTICLE_QNA_OFFER_POINT, qnaOfferPoint);

        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);

        final Transaction transaction = articleRepository.beginTransaction();

        try {
            requestJSONObject.put(Article.ARTICLE_ANONYMOUS, articleAnonymous);
            processTagsForArticleUpdate(oldArticle, requestJSONObject, author);
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            articleTitle = Emotions.toAliases(articleTitle);
            articleTitle = Pangu.spacingText(articleTitle);

            final String oldTitle = oldArticle.optString(Article.ARTICLE_TITLE);
            oldArticle.put(Article.ARTICLE_TITLE, articleTitle);

            oldArticle.put(Article.ARTICLE_TAGS, requestJSONObject.optString(Article.ARTICLE_TAGS));
            oldArticle.put(Article.ARTICLE_COMMENTABLE, requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE, true));
            oldArticle.put(Article.ARTICLE_TYPE, articleType);

            String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
            articleContent = Emotions.toAliases(articleContent);
            //articleContent = StringUtils.trim(articleContent) + " "; https://github.com/b3log/symphony/issues/389
            articleContent = articleContent.replace(langPropsService.get("uploadingLabel", Locale.SIMPLIFIED_CHINESE), "");
            articleContent = articleContent.replace(langPropsService.get("uploadingLabel", Locale.US), "");

            final String oldContent = oldArticle.optString(Article.ARTICLE_CONTENT);
            oldArticle.put(Article.ARTICLE_CONTENT, articleContent);

            final long currentTimeMillis = System.currentTimeMillis();
            final long createTime = oldArticle.optLong(Keys.OBJECT_ID);
            oldArticle.put(Article.ARTICLE_UPDATE_TIME, currentTimeMillis);

            final int rewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT, 0);
            boolean enableReward = false;
            if (0 < rewardPoint) {
                if (1 > oldArticle.optInt(Article.ARTICLE_REWARD_POINT)) {
                    enableReward = true;
                }

                String rewardContent = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
                rewardContent = Emotions.toAliases(rewardContent);
                oldArticle.put(Article.ARTICLE_REWARD_CONTENT, rewardContent);
                oldArticle.put(Article.ARTICLE_REWARD_POINT, rewardPoint);
            }

            final String ip = requestJSONObject.optString(Article.ARTICLE_IP);
            oldArticle.put(Article.ARTICLE_IP, ip);

            String ua = requestJSONObject.optString(Article.ARTICLE_UA);
            if (StringUtils.length(ua) > Common.MAX_LENGTH_UA) {
                ua = StringUtils.substring(ua, 0, Common.MAX_LENGTH_UA);
            }
            oldArticle.put(Article.ARTICLE_UA, ua);

            articleRepository.update(articleId, oldArticle);

            if (Article.ARTICLE_TYPE_C_THOUGHT != articleType
                    && (!oldContent.equals(articleContent) || !oldTitle.equals(articleTitle))) {
                // Revision
                final JSONObject revision = new JSONObject();
                revision.put(Revision.REVISION_AUTHOR_ID, authorId);

                final JSONObject revisionData = new JSONObject();
                revisionData.put(Article.ARTICLE_TITLE, articleTitle);
                revisionData.put(Article.ARTICLE_CONTENT, articleContent);

                revision.put(Revision.REVISION_DATA, revisionData.toString());
                revision.put(Revision.REVISION_DATA_ID, articleId);
                revision.put(Revision.REVISION_DATA_TYPE, Revision.DATA_TYPE_C_ARTICLE);

                revisionRepository.add(revision);
            }

            transaction.commit();

            try {
                Thread.sleep(50); // wait for db write to avoid artitle duplication
            } catch (final Exception e) {
            }

            if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == articleAnonymous) {
                if (currentTimeMillis - createTime > 1000 * 60 * 5) {
                    pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_UPDATE_ARTICLE,
                            updatePointSum, articleId, System.currentTimeMillis(), "");
                }

                if (enableReward) {
                    pointtransferMgmtService.transfer(authorId, Pointtransfer.ID_C_SYS,
                            Pointtransfer.TRANSFER_TYPE_C_ADD_ARTICLE_REWARD,
                            Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_REWARD, articleId, System.currentTimeMillis(), "");
                }
            }

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Article.ARTICLE, oldArticle);
            try {
                eventManager.fireEventAsynchronously(new Event<>(EventTypes.UPDATE_ARTICLE, eventData));
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
     * Updates the specified article by the given article id.
     * <p>
     * <b>Note</b>: This method just for admin console.
     * </p>
     *
     * @param articleId the given article id
     * @param article   the specified article
     * @throws ServiceException service exception
     */
    public void updateArticleByAdmin(final String articleId, final JSONObject article) throws ServiceException {
        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userRepository.get(authorId);

            article.put(Article.ARTICLE_COMMENTABLE, Boolean.valueOf(article.optBoolean(Article.ARTICLE_COMMENTABLE)));

            final JSONObject oldArticle = articleRepository.get(articleId);

            if (Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
                article.put(Article.ARTICLE_TAGS, "回收站");
            }

            processTagsForArticleUpdate(oldArticle, article, author);

            String articleTitle = article.optString(Article.ARTICLE_TITLE);
            articleTitle = Emotions.toAliases(articleTitle);
            article.put(Article.ARTICLE_TITLE, articleTitle);

            if (Article.ARTICLE_TYPE_C_THOUGHT == article.optInt(Article.ARTICLE_TYPE)) {
                article.put(Article.ARTICLE_CONTENT, oldArticle.optString(Article.ARTICLE_CONTENT));
            } else {
                String articleContent = article.optString(Article.ARTICLE_CONTENT);
                articleContent = Emotions.toAliases(articleContent);
                article.put(Article.ARTICLE_CONTENT, articleContent);
            }

            final int perfect = article.optInt(Article.ARTICLE_PERFECT);
            if (Article.ARTICLE_PERFECT_C_PERFECT == perfect) {
                // if it is perfect, allow anonymous view
                article.put(Article.ARTICLE_ANONYMOUS_VIEW, Article.ARTICLE_ANONYMOUS_VIEW_C_ALLOW);

                // updates tag-article perfect
                final List<JSONObject> tagArticleRels = tagArticleRepository.getByArticleId(articleId);
                for (final JSONObject tagArticleRel : tagArticleRels) {
                    tagArticleRel.put(Article.ARTICLE_PERFECT, Article.ARTICLE_PERFECT_C_PERFECT);

                    tagArticleRepository.update(tagArticleRel.optString(Keys.OBJECT_ID), tagArticleRel);
                }
            }

            userRepository.update(authorId, author);
            articleRepository.update(articleId, article);

            transaction.commit();

            if (Article.ARTICLE_PERFECT_C_NOT_PERFECT == oldArticle.optInt(Article.ARTICLE_PERFECT)
                    && Article.ARTICLE_PERFECT_C_PERFECT == perfect) {
                final JSONObject notification = new JSONObject();
                notification.put(Notification.NOTIFICATION_USER_ID, authorId);
                notification.put(Notification.NOTIFICATION_DATA_ID, articleId);

                notificationMgmtService.addPerfectArticleNotification(notification);

                pointtransferMgmtService.transfer(Pointtransfer.ID_C_SYS, authorId,
                        Pointtransfer.TRANSFER_TYPE_C_PERFECT_ARTICLE, Pointtransfer.TRANSFER_SUM_C_PERFECT_ARTICLE,
                        articleId, System.currentTimeMillis(), "");
            }
        } catch (final Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Updates an article[id=" + articleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * A user specified by the given sender id rewards the author of an article specified by the given article id.
     *
     * @param articleId the given article id
     * @param senderId  the given sender id
     * @throws ServiceException service exception
     */
    public void reward(final String articleId, final String senderId) throws ServiceException {
        try {
            final JSONObject article = articleRepository.get(articleId);

            if (null == article) {
                return;
            }

            if (Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
                return;
            }

            final JSONObject sender = userRepository.get(senderId);
            if (null == sender) {
                return;
            }

            if (UserExt.USER_STATUS_C_VALID != sender.optInt(UserExt.USER_STATUS)) {
                return;
            }

            final String receiverId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject receiver = userRepository.get(receiverId);
            if (null == receiver) {
                return;
            }

            if (UserExt.USER_STATUS_C_VALID != receiver.optInt(UserExt.USER_STATUS)) {
                return;
            }

            if (receiverId.equals(senderId)) {
                return;
            }

            final int rewardPoint = article.optInt(Article.ARTICLE_REWARD_POINT);
            if (rewardPoint < 1) {
                return;
            }

            if (rewardQueryService.isRewarded(senderId, articleId, Reward.TYPE_C_ARTICLE)) {
                return;
            }

            final String rewardId = Ids.genTimeMillisId();

            if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == article.optInt(Article.ARTICLE_ANONYMOUS)) {
                final boolean succ = null != pointtransferMgmtService.transfer(senderId, receiverId,
                        Pointtransfer.TRANSFER_TYPE_C_ARTICLE_REWARD, rewardPoint, rewardId, System.currentTimeMillis(), "");

                if (!succ) {
                    throw new ServiceException();
                }
            }

            final JSONObject reward = new JSONObject();
            reward.put(Keys.OBJECT_ID, rewardId);
            reward.put(Reward.SENDER_ID, senderId);
            reward.put(Reward.DATA_ID, articleId);
            reward.put(Reward.TYPE, Reward.TYPE_C_ARTICLE);

            rewardMgmtService.addReward(reward);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, receiverId);
            notification.put(Notification.NOTIFICATION_DATA_ID, rewardId);

            notificationMgmtService.addArticleRewardNotification(notification);

            livenessMgmtService.incLiveness(senderId, Liveness.LIVENESS_REWARD);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Rewards an article[id=" + articleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * A user specified by the given sender id thanks the author of an article specified by the given article id.
     *
     * @param articleId the given article id
     * @param senderId  the given sender id
     * @throws ServiceException service exception
     */
    public void thank(final String articleId, final String senderId) throws ServiceException {
        try {
            final JSONObject article = articleRepository.get(articleId);

            if (null == article) {
                return;
            }

            if (Article.ARTICLE_STATUS_C_INVALID == article.optInt(Article.ARTICLE_STATUS)) {
                return;
            }

            final JSONObject sender = userRepository.get(senderId);
            if (null == sender) {
                return;
            }

            if (UserExt.USER_STATUS_C_VALID != sender.optInt(UserExt.USER_STATUS)) {
                return;
            }

            final String receiverId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject receiver = userRepository.get(receiverId);
            if (null == receiver) {
                return;
            }

            if (UserExt.USER_STATUS_C_VALID != receiver.optInt(UserExt.USER_STATUS)) {
                return;
            }

            if (receiverId.equals(senderId)) {
                return;
            }

            if (rewardQueryService.isRewarded(senderId, articleId, Reward.TYPE_C_THANK_ARTICLE)) {
                return;
            }

            final String thankId = Ids.genTimeMillisId();

            if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == article.optInt(Article.ARTICLE_ANONYMOUS)) {
                final boolean succ = null != pointtransferMgmtService.transfer(senderId, receiverId,
                        Pointtransfer.TRANSFER_TYPE_C_ARTICLE_THANK,
                        Pointtransfer.TRANSFER_SUM_C_ARTICLE_THANK, thankId, System.currentTimeMillis(), "");

                if (!succ) {
                    throw new ServiceException();
                }
            }

            final JSONObject reward = new JSONObject();
            reward.put(Keys.OBJECT_ID, thankId);
            reward.put(Reward.SENDER_ID, senderId);
            reward.put(Reward.DATA_ID, articleId);
            reward.put(Reward.TYPE, Reward.TYPE_C_THANK_ARTICLE);

            rewardMgmtService.addReward(reward);

            final JSONObject notification = new JSONObject();
            notification.put(Notification.NOTIFICATION_USER_ID, receiverId);
            notification.put(Notification.NOTIFICATION_DATA_ID, thankId);

            notificationMgmtService.addArticleThankNotification(notification);

            livenessMgmtService.incLiveness(senderId, Liveness.LIVENESS_REWARD);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Thanks an article[id=" + articleId + "] failed", e);
            throw new ServiceException(e);
        }
    }

    /**
     * Sticks an article specified by the given article id.
     *
     * @param articleId the given article id
     * @throws ServiceException service exception
     */
    public synchronized void stick(final String articleId) throws ServiceException {
        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final JSONObject article = articleRepository.get(articleId);
            if (null == article) {
                return;
            }

            final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            final JSONObject author = userRepository.get(authorId);
            final int balance = author.optInt(UserExt.USER_POINT);

            if (balance - Pointtransfer.TRANSFER_SUM_C_STICK_ARTICLE < 0) {
                throw new ServiceException(langPropsService.get("insufficientBalanceLabel"));
            }

            final Query query = new Query().
                    setFilter(new PropertyFilter(Article.ARTICLE_STICK, FilterOperator.GREATER_THAN, 0L));
            final JSONArray articles = articleRepository.get(query).optJSONArray(Keys.RESULTS);
            if (articles.length() > 1) {
                final Set<String> ids = new HashSet<>();
                for (int i = 0; i < articles.length(); i++) {
                    ids.add(articles.optJSONObject(i).optString(Keys.OBJECT_ID));
                }

                if (!ids.contains(articleId)) {
                    throw new ServiceException(langPropsService.get("stickExistLabel"));
                }
            }

            article.put(Article.ARTICLE_STICK, System.currentTimeMillis());

            articleRepository.update(articleId, article);

            transaction.commit();

            final boolean succ = null != pointtransferMgmtService.transfer(article.optString(Article.ARTICLE_AUTHOR_ID),
                    Pointtransfer.ID_C_SYS, Pointtransfer.TRANSFER_TYPE_C_STICK_ARTICLE,
                    Pointtransfer.TRANSFER_SUM_C_STICK_ARTICLE, articleId, System.currentTimeMillis(), "");
            if (!succ) {
                throw new ServiceException(langPropsService.get("stickFailedLabel"));
            }
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Sticks an article[id=" + articleId + "] failed", e);

            throw new ServiceException(langPropsService.get("stickFailedLabel"));
        }
    }

    /**
     * Admin sticks an article specified by the given article id.
     *
     * @param articleId the given article id
     * @throws ServiceException service exception
     */
    @Transactional
    public synchronized void adminStick(final String articleId) throws ServiceException {
        try {
            final JSONObject article = articleRepository.get(articleId);
            if (null == article) {
                return;
            }

            article.put(Article.ARTICLE_STICK, Long.MAX_VALUE);

            articleRepository.update(articleId, article);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Admin sticks an article[id=" + articleId + "] failed", e);

            throw new ServiceException(langPropsService.get("stickFailedLabel"));
        }
    }

    /**
     * Admin cancels stick an article specified by the given article id.
     *
     * @param articleId the given article id
     * @throws ServiceException service exception
     */
    @Transactional
    public synchronized void adminCancelStick(final String articleId) throws ServiceException {
        try {
            final JSONObject article = articleRepository.get(articleId);
            if (null == article) {
                return;
            }

            article.put(Article.ARTICLE_STICK, 0L);

            articleRepository.update(articleId, article);
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Admin cancel sticks an article[id=" + articleId + "] failed", e);

            throw new ServiceException(langPropsService.get("operationFailedLabel"));
        }
    }

    /**
     * Expires sticked articles.
     *
     * @throws ServiceException service exception
     */
    @Transactional
    public void expireStick() throws ServiceException {
        try {
            final Query query = new Query().
                    setFilter(new PropertyFilter(Article.ARTICLE_STICK, FilterOperator.GREATER_THAN, 0L));
            final JSONArray articles = articleRepository.get(query).optJSONArray(Keys.RESULTS);
            if (articles.length() < 1) {
                return;
            }

            final long stepTime = Symphonys.getLong("stickArticleTime");
            final long now = System.currentTimeMillis();

            for (int i = 0; i < articles.length(); i++) {
                final JSONObject article = articles.optJSONObject(i);
                final long stick = article.optLong(Article.ARTICLE_STICK);
                if (stick >= Long.MAX_VALUE) {
                    continue; // Skip admin stick
                }

                final long expired = stick + stepTime;

                if (expired < now) {
                    article.put(Article.ARTICLE_STICK, 0L);
                    articleRepository.update(article.optString(Keys.OBJECT_ID), article);
                }
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.ERROR, "Expires sticked articles failed", e);

            throw new ServiceException();
        }
    }

    /**
     * Processes tags for article update.
     * <p>
     * <ul>
     * <li>Un-tags old article, decrements tag reference count</li>
     * <li>Removes old article-tag relations</li>
     * <li>Saves new article-tag relations with tag reference count</li>
     * </ul>
     * </p>
     *
     * @param oldArticle the specified old article
     * @param newArticle the specified new article
     * @param author     the specified author
     * @throws Exception exception
     */
    private synchronized void processTagsForArticleUpdate(final JSONObject oldArticle, final JSONObject newArticle,
                                                          final JSONObject author) throws Exception {
        final String oldArticleId = oldArticle.getString(Keys.OBJECT_ID);
        final List<JSONObject> oldTags = tagRepository.getByArticleId(oldArticleId);
        String tagsString = newArticle.getString(Article.ARTICLE_TAGS);
        tagsString = Tag.formatTags(tagsString);
        boolean sandboxEnv = false;
        if (StringUtils.containsIgnoreCase(tagsString, Tag.TAG_TITLE_C_SANDBOX)) {
            tagsString = Tag.TAG_TITLE_C_SANDBOX;
            sandboxEnv = true;
        }

        String[] tagStrings = tagsString.split(",");
        final int articleType = newArticle.optInt(Article.ARTICLE_TYPE);
        if (!sandboxEnv && tagStrings.length < TAG_MAX_CNT && tagStrings.length < 3
                && Article.ARTICLE_TYPE_C_DISCUSSION != articleType
                && Article.ARTICLE_TYPE_C_THOUGHT != articleType && !Tag.containsReservedTags(tagsString)) {
            final String content = newArticle.optString(Article.ARTICLE_TITLE)
                    + " " + Jsoup.parse("<p>" + newArticle.optString(Article.ARTICLE_CONTENT) + "</p>").text();
            final List<String> genTags = tagQueryService.generateTags(content, 1);
            if (!genTags.isEmpty()) {
                tagsString = tagsString + "," + StringUtils.join(genTags, ",");
                tagsString = Tag.formatTags(tagsString);
                tagsString = Tag.useHead(tagsString, TAG_MAX_CNT);
            }
        }

        if (StringUtils.isBlank(tagsString)) {
            tagsString = "B3log";
        }

        tagsString = Tag.formatTags(tagsString);
        if (Article.ARTICLE_TYPE_C_QNA == articleType && !StringUtils.contains(tagsString, "Q&A")) {
            tagsString += ",Q&A";
        }
        newArticle.put(Article.ARTICLE_TAGS, tagsString);
        tagStrings = tagsString.split(",");

        final List<JSONObject> newTags = new ArrayList<>();

        for (final String tagString : tagStrings) {
            final String tagTitle = tagString.trim();
            JSONObject newTag = tagRepository.getByTitle(tagTitle);
            if (null == newTag) {
                newTag = new JSONObject();
                newTag.put(Tag.TAG_TITLE, tagTitle);
            }

            newTags.add(newTag);
        }

        final List<JSONObject> tagsDropped = new ArrayList<>();
        final List<JSONObject> tagsNeedToAdd = new ArrayList<>();

        for (final JSONObject newTag : newTags) {
            final String newTagTitle = newTag.getString(Tag.TAG_TITLE);

            if (!tagExists(newTagTitle, oldTags)) {
                LOGGER.log(Level.DEBUG, "Tag need to add [title={0}]", newTagTitle);
                tagsNeedToAdd.add(newTag);
            }
        }
        for (final JSONObject oldTag : oldTags) {
            final String oldTagTitle = oldTag.getString(Tag.TAG_TITLE);

            if (!tagExists(oldTagTitle, newTags)) {
                LOGGER.log(Level.DEBUG, "Tag dropped [title={0}]", oldTag);
                tagsDropped.add(oldTag);
            }
        }

        final int articleCmtCnt = oldArticle.getInt(Article.ARTICLE_COMMENT_CNT);

        for (final JSONObject tagDropped : tagsDropped) {
            final String tagId = tagDropped.getString(Keys.OBJECT_ID);
            int refCnt = tagDropped.getInt(Tag.TAG_REFERENCE_CNT) - 1;
            refCnt = refCnt < 0 ? 0 : refCnt;
            tagDropped.put(Tag.TAG_REFERENCE_CNT, refCnt);
            final int tagCmtCnt = tagDropped.getInt(Tag.TAG_COMMENT_CNT);
            tagDropped.put(Tag.TAG_COMMENT_CNT, tagCmtCnt - articleCmtCnt);
            tagDropped.put(Tag.TAG_RANDOM_DOUBLE, Math.random());

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
            removeUserTagRelations(oldArticle.optString(Article.ARTICLE_AUTHOR_ID), Tag.TAG_TYPE_C_ARTICLE, tagIdsDropped);
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
     * <p>
     * Removes all relations if not specified the tag ids.
     * </p>
     *
     * @param articleId the specified article id
     * @param tagIds    the specified tag ids of the relations to be removed
     * @throws JSONException       json exception
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
            } else if (tagIdList.contains(tagArticleRelation.getString(Tag.TAG + "_" + Keys.OBJECT_ID))) {
                relationId = tagArticleRelation.getString(Keys.OBJECT_ID);
                tagArticleRepository.remove(relationId);
            }
        }
    }

    /**
     * Removes User-Tag relations by the specified user id, type and tag ids of the relations to be removed.
     *
     * @param userId the specified article id
     * @param type   the specified type
     * @param tagIds the specified tag ids of the relations to be removed
     * @throws RepositoryException repository exception
     */
    private void removeUserTagRelations(final String userId, final int type, final String... tagIds) throws RepositoryException {
        for (final String tagId : tagIds) {
            userTagRepository.removeByUserIdAndTagId(userId, tagId, type);
        }
    }

    /**
     * Tags the specified article with the specified tag titles.
     *
     * @param tagTitles the specified (new) tag titles
     * @param article   the specified article
     * @param author    the specified author
     * @throws RepositoryException repository exception
     */
    private synchronized void tag(final String[] tagTitles, final JSONObject article, final JSONObject author)
            throws RepositoryException {
        String articleTags = article.optString(Article.ARTICLE_TAGS);

        for (final String t : tagTitles) {
            final String tagTitle = t.trim();
            JSONObject tag = tagRepository.getByTitle(tagTitle);
            String tagId;
            int userTagType;
            final int articleCmtCnt = article.optInt(Article.ARTICLE_COMMENT_CNT);
            if (null == tag) {
                LOGGER.log(Level.TRACE, "Found a new tag [title={0}] in article [title={1}]",
                        tagTitle, article.optString(Article.ARTICLE_TITLE));
                tag = new JSONObject();
                tag.put(Tag.TAG_TITLE, tagTitle);
                final String tagURI = URLs.encode(tagTitle);
                tag.put(Tag.TAG_URI, tagURI);
                tag.put(Tag.TAG_CSS, "");
                tag.put(Tag.TAG_REFERENCE_CNT, 1);
                tag.put(Tag.TAG_COMMENT_CNT, articleCmtCnt);
                tag.put(Tag.TAG_FOLLOWER_CNT, 0);
                tag.put(Tag.TAG_LINK_CNT, 0);
                tag.put(Tag.TAG_DESCRIPTION, "");
                tag.put(Tag.TAG_ICON_PATH, "");
                tag.put(Tag.TAG_STATUS, 0);
                tag.put(Tag.TAG_GOOD_CNT, 0);
                tag.put(Tag.TAG_BAD_CNT, 0);
                tag.put(Tag.TAG_SEO_TITLE, tagTitle);
                tag.put(Tag.TAG_SEO_KEYWORDS, tagTitle);
                tag.put(Tag.TAG_SEO_DESC, "");
                tag.put(Tag.TAG_RANDOM_DOUBLE, Math.random());
                tag.put(Tag.TAG_AD, "");
                tag.put(Tag.TAG_SHOW_SIDE_AD, 0);

                tagId = tagRepository.add(tag);
                tag.put(Keys.OBJECT_ID, tagId);
                userTagType = Tag.TAG_TYPE_C_CREATOR;

                final JSONObject tagCntOption = optionRepository.get(Option.ID_C_STATISTIC_TAG_COUNT);
                final int tagCnt = tagCntOption.optInt(Option.OPTION_VALUE);
                tagCntOption.put(Option.OPTION_VALUE, tagCnt + 1);
                optionRepository.update(Option.ID_C_STATISTIC_TAG_COUNT, tagCntOption);

                author.put(UserExt.USER_TAG_COUNT, author.optInt(UserExt.USER_TAG_COUNT) + 1);
            } else {
                tagId = tag.optString(Keys.OBJECT_ID);
                LOGGER.log(Level.TRACE, "Found a existing tag[title={0}, id={1}] in article[title={2}]",
                        tag.optString(Tag.TAG_TITLE), tag.optString(Keys.OBJECT_ID), article.optString(Article.ARTICLE_TITLE));
                final JSONObject tagTmp = new JSONObject();
                tagTmp.put(Keys.OBJECT_ID, tagId);
                final String title = tag.optString(Tag.TAG_TITLE);

                tagTmp.put(Tag.TAG_TITLE, title);
                tagTmp.put(Tag.TAG_COMMENT_CNT, tag.optInt(Tag.TAG_COMMENT_CNT) + articleCmtCnt);
                tagTmp.put(Tag.TAG_STATUS, tag.optInt(Tag.TAG_STATUS));
                tagTmp.put(Tag.TAG_REFERENCE_CNT, tag.optInt(Tag.TAG_REFERENCE_CNT) + 1);
                tagTmp.put(Tag.TAG_FOLLOWER_CNT, tag.optInt(Tag.TAG_FOLLOWER_CNT));
                tagTmp.put(Tag.TAG_LINK_CNT, tag.optInt(Tag.TAG_LINK_CNT));
                tagTmp.put(Tag.TAG_DESCRIPTION, tag.optString(Tag.TAG_DESCRIPTION));
                tagTmp.put(Tag.TAG_ICON_PATH, tag.optString(Tag.TAG_ICON_PATH));
                tagTmp.put(Tag.TAG_GOOD_CNT, tag.optInt(Tag.TAG_GOOD_CNT));
                tagTmp.put(Tag.TAG_BAD_CNT, tag.optInt(Tag.TAG_BAD_CNT));
                tagTmp.put(Tag.TAG_SEO_DESC, tag.optString(Tag.TAG_SEO_DESC));
                tagTmp.put(Tag.TAG_SEO_KEYWORDS, tag.optString(Tag.TAG_SEO_KEYWORDS));
                tagTmp.put(Tag.TAG_SEO_TITLE, tag.optString(Tag.TAG_SEO_TITLE));
                tagTmp.put(Tag.TAG_RANDOM_DOUBLE, Math.random());
                tagTmp.put(Tag.TAG_URI, tag.optString(Tag.TAG_URI));
                tagTmp.put(Tag.TAG_CSS, tag.optString(Tag.TAG_CSS));
                tagTmp.put(Tag.TAG_AD, tag.optString(Tag.TAG_AD));
                tagTmp.put(Tag.TAG_SHOW_SIDE_AD, tag.optInt(Tag.TAG_SHOW_SIDE_AD));

                tagRepository.update(tagId, tagTmp);

                userTagType = Tag.TAG_TYPE_C_ARTICLE;
            }

            // Tag-Article relation
            final JSONObject tagArticleRelation = new JSONObject();
            tagArticleRelation.put(Tag.TAG + "_" + Keys.OBJECT_ID, tagId);
            tagArticleRelation.put(Article.ARTICLE + "_" + Keys.OBJECT_ID, article.optString(Keys.OBJECT_ID));
            tagArticleRelation.put(Article.ARTICLE_LATEST_CMT_TIME, article.optLong(Article.ARTICLE_LATEST_CMT_TIME));
            tagArticleRelation.put(Article.ARTICLE_COMMENT_CNT, article.optInt(Article.ARTICLE_COMMENT_CNT));
            tagArticleRelation.put(Article.REDDIT_SCORE, article.optDouble(Article.REDDIT_SCORE, 0D));
            tagArticleRelation.put(Article.ARTICLE_PERFECT, article.optInt(Article.ARTICLE_PERFECT));
            tagArticleRepository.add(tagArticleRelation);

            final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);

            // User-Tag relation
            if (Tag.TAG_TYPE_C_ARTICLE == userTagType) {
                userTagRepository.removeByUserIdAndTagId(authorId, tagId, Tag.TAG_TYPE_C_ARTICLE);
            }

            final JSONObject userTagRelation = new JSONObject();
            userTagRelation.put(Tag.TAG + '_' + Keys.OBJECT_ID, tagId);
            if (Article.ARTICLE_ANONYMOUS_C_ANONYMOUS == article.optInt(Article.ARTICLE_ANONYMOUS)) {
                userTagRelation.put(User.USER + '_' + Keys.OBJECT_ID, "0");
            } else {
                userTagRelation.put(User.USER + '_' + Keys.OBJECT_ID, authorId);
            }
            userTagRelation.put(Common.TYPE, userTagType);
            userTagRepository.add(userTagRelation);
        }

        final String[] tags = articleTags.split(",");
        final StringBuilder builder = new StringBuilder();
        for (final String tagTitle : tags) {
            final JSONObject tag = tagRepository.getByTitle(tagTitle);

            builder.append(tag.optString(Tag.TAG_TITLE)).append(",");
        }
        if (builder.length() > 0) {
            builder.deleteCharAt(builder.length() - 1);
        }

        article.put(Article.ARTICLE_TAGS, builder.toString());
    }

    /**
     * Filters the specified article tags.
     *
     * @param articleTags the specified article tags
     * @return filtered tags string
     */
    public String filterReservedTags(final String articleTags) {
        final String[] tags = articleTags.split(",");

        final StringBuilder retBuilder = new StringBuilder();

        for (final String tag : tags) {
            if (!ArrayUtils.contains(Symphonys.RESERVED_TAGS, tag)) {
                retBuilder.append(tag).append(",");
            }
        }
        if (retBuilder.length() > 0) {
            retBuilder.deleteCharAt(retBuilder.length() - 1);
        }

        return retBuilder.toString();
    }

    /**
     * Adds an article with the specified request json object.
     * <p>
     * <b>Note</b>: This method just for admin console.
     * </p>
     *
     * @param requestJSONObject the specified request json object, for example,
     *                          "articleTitle": "",
     *                          "articleTags": "",
     *                          "articleContent": "",
     *                          "articleRewardContent": "",
     *                          "articleRewardPoint": int,
     *                          "userName": "",
     *                          "time": long
     *                          , see {@link Article} for more details
     * @return generated article id
     * @throws ServiceException service exception
     */
    public synchronized String addArticleByAdmin(final JSONObject requestJSONObject) throws ServiceException {
        JSONObject author;

        try {
            author = userRepository.getByName(requestJSONObject.optString(User.USER_NAME));
            if (null == author) {
                throw new ServiceException(langPropsService.get("notFoundUserLabel"));
            }
        } catch (final RepositoryException e) {
            LOGGER.log(Level.DEBUG, "Admin adds article failed", e);

            throw new ServiceException(e.getMessage());
        }

        final Transaction transaction = articleRepository.beginTransaction();

        try {
            final long time = requestJSONObject.optLong(Common.TIME);
            final String ret = String.valueOf(time);
            final JSONObject article = new JSONObject();
            article.put(Keys.OBJECT_ID, ret);
            article.put(Article.ARTICLE_AUTHOR_ID, author.optString(Keys.OBJECT_ID));
            article.put(Article.ARTICLE_TITLE, Emotions.toAliases(requestJSONObject.optString(Article.ARTICLE_TITLE)));
            article.put(Article.ARTICLE_CONTENT, Emotions.toAliases(requestJSONObject.optString(Article.ARTICLE_CONTENT)));
            article.put(Article.ARTICLE_REWARD_CONTENT, requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT));
            article.put(Article.ARTICLE_EDITOR_TYPE, 0);
            article.put(Article.ARTICLE_COMMENT_CNT, 0);
            article.put(Article.ARTICLE_VIEW_CNT, 0);
            article.put(Article.ARTICLE_GOOD_CNT, 0);
            article.put(Article.ARTICLE_BAD_CNT, 0);
            article.put(Article.ARTICLE_COLLECT_CNT, 0);
            article.put(Article.ARTICLE_WATCH_CNT, 0);
            article.put(Article.ARTICLE_COMMENTABLE, true);
            article.put(Article.ARTICLE_CREATE_TIME, time);
            article.put(Article.ARTICLE_UPDATE_TIME, time);
            article.put(Article.ARTICLE_LATEST_CMT_TIME, 0);
            article.put(Article.ARTICLE_LATEST_CMTER_NAME, "");
            article.put(Article.ARTICLE_PERMALINK, "/article/" + ret);
            article.put(Article.ARTICLE_RANDOM_DOUBLE, Math.random());
            article.put(Article.REDDIT_SCORE, 0);
            article.put(Article.ARTICLE_STATUS, Article.ARTICLE_STATUS_C_VALID);
            article.put(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
            article.put(Article.ARTICLE_REWARD_POINT, requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT));
            article.put(Article.ARTICLE_QNA_OFFER_POINT, 0);
            article.put(Article.ARTICLE_PUSH_ORDER, 0);
            article.put(Article.ARTICLE_IMG1_URL, "");
            article.put(Article.ARTICLE_CITY, "");
            String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
            articleTags = Tag.formatTags(articleTags);
            boolean sandboxEnv = false;
            if (StringUtils.containsIgnoreCase(articleTags, Tag.TAG_TITLE_C_SANDBOX)) {
                articleTags = Tag.TAG_TITLE_C_SANDBOX;
                sandboxEnv = true;
            }

            String[] tagTitles = articleTags.split(",");
            if (!sandboxEnv && tagTitles.length < TAG_MAX_CNT && tagTitles.length < 3
                    && !Tag.containsReservedTags(articleTags)) {
                final String content = article.optString(Article.ARTICLE_TITLE)
                        + " " + Jsoup.parse("<p>" + article.optString(Article.ARTICLE_CONTENT) + "</p>").text();
                final List<String> genTags = tagQueryService.generateTags(content, TAG_MAX_CNT);
                if (!genTags.isEmpty()) {
                    articleTags = articleTags + "," + StringUtils.join(genTags, ",");
                    articleTags = Tag.formatTags(articleTags);
                    articleTags = Tag.useHead(articleTags, TAG_MAX_CNT);
                }
            }

            if (StringUtils.isBlank(articleTags)) {
                articleTags = "B3log";
            }

            articleTags = Tag.formatTags(articleTags);
            article.put(Article.ARTICLE_TAGS, articleTags);
            tagTitles = articleTags.split(",");

            tag(tagTitles, article, author);

            final String ip = requestJSONObject.optString(Article.ARTICLE_IP);
            article.put(Article.ARTICLE_IP, ip);

            String ua = requestJSONObject.optString(Article.ARTICLE_UA);
            if (StringUtils.length(ua) > Common.MAX_LENGTH_UA) {
                ua = StringUtils.substring(ua, 0, Common.MAX_LENGTH_UA);
            }
            article.put(Article.ARTICLE_UA, ua);

            article.put(Article.ARTICLE_STICK, 0L);
            article.put(Article.ARTICLE_ANONYMOUS, Article.ARTICLE_ANONYMOUS_C_PUBLIC);
            article.put(Article.ARTICLE_PERFECT, Article.ARTICLE_PERFECT_C_NOT_PERFECT);
            article.put(Article.ARTICLE_ANONYMOUS_VIEW, Article.ARTICLE_ANONYMOUS_VIEW_C_USE_GLOBAL);
            article.put(Article.ARTICLE_AUDIO_URL, "");

            final JSONObject articleCntOption = optionRepository.get(Option.ID_C_STATISTIC_ARTICLE_COUNT);
            final int articleCnt = articleCntOption.optInt(Option.OPTION_VALUE);
            articleCntOption.put(Option.OPTION_VALUE, articleCnt + 1);
            optionRepository.update(Option.ID_C_STATISTIC_ARTICLE_COUNT, articleCntOption);

            author.put(UserExt.USER_ARTICLE_COUNT, author.optInt(UserExt.USER_ARTICLE_COUNT) + 1);
            author.put(UserExt.USER_LATEST_ARTICLE_TIME, time);
            // Updates user article count (and new tag count), latest article time
            userRepository.update(author.optString(Keys.OBJECT_ID), author);

            articleRepository.add(article);

            transaction.commit();

            // Grows the tag graph
            tagMgmtService.relateTags(article.optString(Article.ARTICLE_TAGS));

            // Event
            final JSONObject eventData = new JSONObject();
            eventData.put(Article.ARTICLE, article);
            try {
                eventManager.fireEventAsynchronously(new Event<>(EventTypes.ADD_ARTICLE, eventData));
            } catch (final EventException e) {
                LOGGER.log(Level.ERROR, e.getMessage(), e);
            }

            return ret;
        } catch (final RepositoryException e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }

            LOGGER.log(Level.ERROR, "Admin adds an article failed", e);
            throw new ServiceException(e.getMessage());
        }
    }

    /**
     * Saves markdown file for the specified article.
     *
     * @param article the specified article
     */
    public void saveMarkdown(final JSONObject article) {
        if (Article.ARTICLE_TYPE_C_THOUGHT == article.optInt(Article.ARTICLE_TYPE)
                || Article.ARTICLE_TYPE_C_DISCUSSION == article.optInt(Article.ARTICLE_TYPE)
                || StringUtils.containsIgnoreCase(article.optString(Article.ARTICLE_TAGS), Tag.TAG_TITLE_C_SANDBOX)) {
            return;
        }

        final String dir = Symphonys.get("ipfs.dir");
        if (StringUtils.isBlank(dir)) {
            return;
        }

        final Path dirPath = Paths.get(dir);
        try {
            FileUtils.forceMkdir(dirPath.toFile());
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Creates dir [" + dirPath.toString() + "] for save markdown files failed", e);

            return;
        }

        final String id = article.optString(Keys.OBJECT_ID);
        final String authorName = article.optJSONObject(Article.ARTICLE_T_AUTHOR).optString(User.USER_NAME);
        final Path mdPath = Paths.get(dir, "hacpai", authorName, id + ".md");
        try {
            if (mdPath.toFile().exists()) {
                final FileTime lastModifiedTime = Files.getLastModifiedTime(mdPath);
                if (lastModifiedTime.toMillis() + 1000 * 60 * 60 >= System.currentTimeMillis()) {
                    return;
                }
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Gets last modified time of file [" + mdPath.toString() + "] failed", e);

            return;
        }

        try {
            final Map<String, Object> hexoFront = new LinkedHashMap<>();
            hexoFront.put("title", article.optString(Article.ARTICLE_TITLE));
            hexoFront.put("date", DateFormatUtils.format((Date) article.opt(Article.ARTICLE_CREATE_TIME), "yyyy-MM-dd HH:mm:ss"));
            hexoFront.put("updated", DateFormatUtils.format((Date) article.opt(Article.ARTICLE_UPDATE_TIME), "yyyy-MM-dd HH:mm:ss"));
            final List<String> tags = Arrays.stream(article.optString(Article.ARTICLE_TAGS).split(",")).
                    filter(StringUtils::isNotBlank).map(String::trim).collect(Collectors.toList());
            if (tags.isEmpty()) {
                tags.add("Sym");
            }
            hexoFront.put("tags", tags);

            final String text = new Yaml().dump(hexoFront).replaceAll("\n", Strings.LINE_SEPARATOR) + "---" + Strings.LINE_SEPARATOR + article.optString(Article.ARTICLE_T_ORIGINAL_CONTENT);
            FileUtils.writeStringToFile(new File(mdPath.toString()), text, "UTF-8");
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Writes article to markdown file [" + mdPath.toString() + "] failed", e);
        }
    }
}
