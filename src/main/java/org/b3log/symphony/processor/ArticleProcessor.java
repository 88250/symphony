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
package org.b3log.symphony.processor;

import com.qiniu.util.Auth;
import java.awt.Graphics;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jodd.util.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.Latkes;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.Role;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.freemarker.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.model.Article;
import org.b3log.symphony.model.Client;
import org.b3log.symphony.model.Comment;
import org.b3log.symphony.model.Common;
import org.b3log.symphony.model.Liveness;
import org.b3log.symphony.model.Pointtransfer;
import org.b3log.symphony.model.Referral;
import org.b3log.symphony.model.Revision;
import org.b3log.symphony.model.Reward;
import org.b3log.symphony.model.Tag;
import org.b3log.symphony.model.UserExt;
import org.b3log.symphony.model.Vote;
import org.b3log.symphony.processor.advice.CSRFCheck;
import org.b3log.symphony.processor.advice.CSRFToken;
import org.b3log.symphony.processor.advice.LoginCheck;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.ArticleAddValidation;
import org.b3log.symphony.processor.advice.validate.ArticleUpdateValidation;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.service.ArticleMgmtService;
import org.b3log.symphony.service.ArticleQueryService;
import org.b3log.symphony.service.CharacterQueryService;
import org.b3log.symphony.service.ClientMgmtService;
import org.b3log.symphony.service.ClientQueryService;
import org.b3log.symphony.service.CommentQueryService;
import org.b3log.symphony.service.FollowQueryService;
import org.b3log.symphony.service.LivenessMgmtService;
import org.b3log.symphony.service.ReferralMgmtService;
import org.b3log.symphony.service.RewardQueryService;
import org.b3log.symphony.service.ShortLinkQueryService;
import org.b3log.symphony.service.UserMgmtService;
import org.b3log.symphony.service.UserQueryService;
import org.b3log.symphony.service.VoteQueryService;
import org.b3log.symphony.util.Emotions;
import org.b3log.symphony.util.Filler;
import org.b3log.symphony.util.Markdowns;
import org.b3log.symphony.util.Sessions;
import org.b3log.symphony.util.Symphonys;
import org.json.JSONObject;

/**
 * Article processor.
 *
 * <ul>
 * <li>Shows an article (/article/{articleId}), GET</li>
 * <li>Shows article pre adding form page (/pre-post), GET</li>
 * <li>Shows article adding form page (/post), GET</li>
 * <li>Adds an article (/post) <em>locally</em>, POST</li>
 * <li>Shows an article updating form page (/update) <em>locally</em>, GET</li>
 * <li>Updates an article (/article/{id}) <em>locally</em>, PUT</li>
 * <li>Adds an article (/rhythm/article) <em>remotely</em>, POST</li>
 * <li>Updates an article (/rhythm/article) <em>remotely</em>, PUT</li>
 * <li>Markdowns text (/markdown), POST</li>
 * <li>Rewards an article (/article/reward), POST</li>
 * <li>Gets an article preview content (/article/{articleId}/preview), GET</li>
 * <li>Sticks an article (/article/stick), POST</li>
 * <li>Gets article revisions (/article/{articleId}/revisions), GET</li>
 * <li>Gets article image (/article/{articleId}/image), GET</li>
 * </ul>
 *
 * <p>
 * The '<em>locally</em>' means user post an article on Symphony directly rather than receiving an article from
 * externally (for example Rhythm).
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.19.15.30, Jun 29, 2016
 * @since 0.2.0
 */
@RequestProcessor
public class ArticleProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleProcessor.class.getName());

    /**
     * Short link query service.
     */
    @Inject
    private ShortLinkQueryService shortLinkQueryService;

    /**
     * Article management service.
     */
    @Inject
    private ArticleMgmtService articleMgmtService;

    /**
     * Article query service.
     */
    @Inject
    private ArticleQueryService articleQueryService;

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
     * User management service.
     */
    @Inject
    private UserMgmtService userMgmtService;

    /**
     * Client management service.
     */
    @Inject
    private ClientMgmtService clientMgmtService;

    /**
     * Client query service.
     */
    @Inject
    private ClientQueryService clientQueryService;

    /**
     * Language service.
     */
    @Inject
    private LangPropsService langPropsService;

    /**
     * Follow query service.
     */
    @Inject
    private FollowQueryService followQueryService;

    /**
     * Reward query service.
     */
    @Inject
    private RewardQueryService rewardQueryService;

    /**
     * Vote query service.
     */
    @Inject
    private VoteQueryService voteQueryService;

    /**
     * Liveness management service.
     */
    @Inject
    private LivenessMgmtService livenessMgmtService;

    /**
     * Referral management service.
     */
    @Inject
    private ReferralMgmtService referralMgmtService;

    /**
     * Character query service.
     */
    @Inject
    private CharacterQueryService characterQueryService;

    /**
     * Filler.
     */
    @Inject
    private Filler filler;

    /**
     * Gets article image.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{articleId}/image", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void getArticleImage(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String articleId) throws Exception {
        final JSONObject article = articleQueryService.getArticle(articleId);
        final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);

        final Set<JSONObject> characters = characterQueryService.getWrittenCharacters();
        final String articleContent = article.optString(Article.ARTICLE_CONTENT);

        final List<BufferedImage> images = new ArrayList<BufferedImage>();
        for (int i = 0; i < articleContent.length(); i++) {
            final String ch = articleContent.substring(i, i + 1);
            final JSONObject chRecord = org.b3log.symphony.model.Character.getCharacter(ch, characters);
            if (null == chRecord) {
                images.add(org.b3log.symphony.model.Character.createImage(ch));

                continue;
            }

            final String imgData = chRecord.optString(org.b3log.symphony.model.Character.CHARACTER_IMG);
            final byte[] data = Base64.decode(imgData.getBytes());
            final BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
            final BufferedImage newImage = new BufferedImage(50, 50, img.getType());
            final Graphics g = newImage.getGraphics();
            g.setClip(0, 0, 50, 50);
            g.fillRect(0, 0, 50, 50);
            g.drawImage(img, 0, 0, 50, 50, null);
            g.dispose();

            images.add(newImage);
        }

        final int rowCharacterCount = 30;
        final int rows = (int) Math.ceil((double) images.size() / (double) rowCharacterCount);

        final BufferedImage combined = new BufferedImage(30 * 50, rows * 50, Transparency.TRANSLUCENT);
        int row = 0;
        for (int i = 0; i < images.size(); i++) {
            final BufferedImage image = images.get(i);

            final Graphics g = combined.getGraphics();
            g.drawImage(image, (i % rowCharacterCount) * 50, row * 50, null);

            if (0 == (i + 1) % rowCharacterCount) {
                row++;
            }
        }

        ImageIO.write(combined, "PNG", new File("./hp.png"));

        String url = "";

        final JSONObject ret = new JSONObject();
        ret.put(Keys.STATUS_CODE, true);
        ret.put(Common.URL, (Object) url);

        context.renderJSON(ret);
    }

    /**
     * Gets article revisions.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{articleId}/revisions", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void getArticleRevisions(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String articleId) throws Exception {
        final List<JSONObject> revisions = articleQueryService.getArticleRevisions(articleId);
        final JSONObject ret = new JSONObject();
        ret.put(Keys.STATUS_CODE, true);
        ret.put(Revision.REVISIONS, (Object) revisions);

        context.renderJSON(ret);
    }

    /**
     * Shows pre-add article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/pre-post", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {CSRFToken.class, StopwatchEndAdvice.class})
    public void showPreAddArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("/home/pre-post.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put(Common.BROADCAST_POINT, Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST);

        filler.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Shows add article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/post", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {CSRFToken.class, StopwatchEndAdvice.class})
    public void showAddArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("/home/post.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        final String uploadToken = auth.uploadToken(Symphonys.get("qiniu.bucket"));
        dataModel.put("qiniuUploadToken", uploadToken);
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        if (!Symphonys.getBoolean("qiniu.enabled")) {
            dataModel.put("qiniuUploadToken", "");
        }

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        String tags = request.getParameter(Tag.TAGS);
        if (StringUtils.isBlank(tags)) {
            tags = "";

            dataModel.put(Tag.TAGS, tags);
        } else {
            tags = Tag.formatTags(tags);
            final String[] tagTitles = tags.split(",");

            final StringBuilder tagBuilder = new StringBuilder();
            for (final String title : tagTitles) {
                final String tagTitle = title.trim();

                if (Strings.isEmptyOrNull(tagTitle)) {
                    continue;
                }

                if (!Tag.TAG_TITLE_PATTERN.matcher(tagTitle).matches()) {
                    continue;
                }

                if (Strings.isEmptyOrNull(tagTitle) || tagTitle.length() > Tag.MAX_TAG_TITLE_LENGTH || tagTitle.length() < 1) {
                    continue;
                }

                final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
                if (!Role.ADMIN_ROLE.equals(currentUser.optString(User.USER_ROLE))
                        && ArrayUtils.contains(Symphonys.RESERVED_TAGS, tagTitle)) {
                    continue;
                }

                tagBuilder.append(tagTitle).append(",");
            }
            if (tagBuilder.length() > 0) {
                tagBuilder.deleteCharAt(tagBuilder.length() - 1);
            }

            dataModel.put(Tag.TAGS, tagBuilder.toString());
        }

        final String type = request.getParameter(Common.TYPE);
        if (StringUtils.isBlank(type)) {
            dataModel.put(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
        } else {
            int articleType = Article.ARTICLE_TYPE_C_NORMAL;

            try {
                articleType = Integer.valueOf(type);
            } catch (final Exception e) {
                LOGGER.log(Level.WARN, "Gets article type error [" + type + "]", e);
            }

            if (Article.isInvalidArticleType(articleType)) {
                articleType = Article.ARTICLE_TYPE_C_NORMAL;
            }

            dataModel.put(Article.ARTICLE_TYPE, articleType);
        }

        final String at = request.getParameter(Common.AT);
        if (StringUtils.isNotBlank(at)) {
            dataModel.put(Common.AT, at);
        }

        filler.fillHeaderAndFooter(request, response, dataModel);

        String rewardEditorPlaceholderLabel = langPropsService.get("rewardEditorPlaceholderLabel");
        rewardEditorPlaceholderLabel = rewardEditorPlaceholderLabel.replace("{point}",
                String.valueOf(Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_REWARD));
        dataModel.put("rewardEditorPlaceholderLabel", rewardEditorPlaceholderLabel);
        dataModel.put(Common.BROADCAST_POINT, Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST);
    }

    /**
     * Shows article with the specified article id.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{articleId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = {CSRFToken.class, StopwatchEndAdvice.class})
    public void showArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String articleId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("/article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final JSONObject article = articleQueryService.getArticleById(articleId);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final HttpSession session = request.getSession(false);
        if (null != session) {
            session.setAttribute(Article.ARTICLE_T_ID, articleId);
        }

        filler.fillHeaderAndFooter(request, response, dataModel);

        final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
        final JSONObject author = userQueryService.getUser(authorId);
        article.put(Article.ARTICLE_T_AUTHOR_NAME, author.optString(User.USER_NAME));
        article.put(Article.ARTICLE_T_AUTHOR_URL, author.optString(User.USER_URL));
        article.put(Article.ARTICLE_T_AUTHOR_INTRO, author.optString(UserExt.USER_INTRO));
        dataModel.put(Article.ARTICLE, article);

        article.put(Common.IS_MY_ARTICLE, false);
        article.put(Article.ARTICLE_T_AUTHOR, author);
        article.put(Common.REWARDED, false);
        if (!article.has(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK)) { // for legacy data
            article.put(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK, "");
        }

        articleQueryService.processArticleContent(article, request);

        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);

        String cmtViewModeStr = request.getParameter("m");

        JSONObject currentUser;
        String currentUserId = null;
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            currentUserId = currentUser.optString(Keys.OBJECT_ID);

            article.put(Common.IS_MY_ARTICLE, currentUserId.equals(article.optString(Article.ARTICLE_AUTHOR_ID)));

            final boolean isFollowing = followQueryService.isFollowing(currentUserId, articleId);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            final int vote = voteQueryService.isVoted(currentUserId, articleId);
            dataModel.put(Vote.VOTE, vote);

            if (currentUserId.equals(author.optString(Keys.OBJECT_ID))) {
                article.put(Common.REWARDED, true);
            } else {
                article.put(Common.REWARDED,
                        rewardQueryService.isRewarded(currentUserId, articleId, Reward.TYPE_C_ARTICLE));
            }

            if (Strings.isEmptyOrNull(cmtViewModeStr) || !Strings.isNumeric(cmtViewModeStr)) {
                cmtViewModeStr = currentUser.optString(UserExt.USER_COMMENT_VIEW_MODE);
            }
        } else if (Strings.isEmptyOrNull(cmtViewModeStr) || !Strings.isNumeric(cmtViewModeStr)) {
            cmtViewModeStr = "0";
        }

        int cmtViewMode = Integer.valueOf(cmtViewModeStr);

        dataModel.put(UserExt.USER_COMMENT_VIEW_MODE, cmtViewMode);

        if (!(Boolean) request.getAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT)) {
            articleMgmtService.incArticleViewCount(articleId);
        }

        final JSONObject viewer = (JSONObject) request.getAttribute(User.USER);
        if (null != viewer) {
            livenessMgmtService.incLiveness(viewer.optString(Keys.OBJECT_ID), Liveness.LIVENESS_PV);
        }

        filler.fillRelevantArticles(dataModel, article);
        filler.fillRandomArticles(dataModel);
        filler.fillHotArticles(dataModel);

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        final String uploadToken = auth.uploadToken(Symphonys.get("qiniu.bucket"));
        dataModel.put("qiniuUploadToken", uploadToken);
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        if (!Symphonys.getBoolean("qiniu.enabled")) {
            dataModel.put("qiniuUploadToken", "");
        }

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        // Fill article thank
        article.put(Common.THANKED, rewardQueryService.isRewarded(currentUserId, articleId, Reward.TYPE_C_THANK_ARTICLE));

        String stickConfirmLabel = langPropsService.get("stickConfirmLabel");
        stickConfirmLabel = stickConfirmLabel.replace("{point}", Symphonys.get("pointStickArticle"));
        dataModel.put("stickConfirmLabel", stickConfirmLabel);
        dataModel.put("pointThankArticle", Symphonys.get("pointThankArticle"));

        dataModel.put(Common.DISCUSSION_VIEWABLE, article.optBoolean(Common.DISCUSSION_VIEWABLE));
        if (!article.optBoolean(Common.DISCUSSION_VIEWABLE)) {
            article.put(Article.ARTICLE_T_COMMENTS, (Object) Collections.emptyList());

            return;
        }

        String pageNumStr = request.getParameter("p");
        if (Strings.isEmptyOrNull(pageNumStr) || !Strings.isNumeric(pageNumStr)) {
            pageNumStr = "1";
        }

        final int pageNum = Integer.valueOf(pageNumStr);
        final int pageSize = Symphonys.getInt("articleCommentsPageSize");
        final int windowSize = Symphonys.getInt("articleCommentsWindowSize");

        final List<JSONObject> articleComments = commentQueryService.getArticleComments(articleId, pageNum, pageSize,
                cmtViewMode);
        article.put(Article.ARTICLE_T_COMMENTS, (Object) articleComments);

        // Fill comment thank
        for (final JSONObject comment : articleComments) {
            String thankTemplate = langPropsService.get("thankConfirmLabel");
            thankTemplate = thankTemplate.replace("{point}", String.valueOf(Symphonys.getInt("pointThankComment")))
                    .replace("{user}", comment.optJSONObject(Comment.COMMENT_T_COMMENTER).optString(User.USER_NAME));
            comment.put(Comment.COMMENT_T_THANK_LABEL, thankTemplate);

            final String commentId = comment.optString(Keys.OBJECT_ID);
            if (isLoggedIn) {
                comment.put(Common.REWARDED,
                        rewardQueryService.isRewarded(currentUserId, commentId, Reward.TYPE_C_COMMENT));
            }

            comment.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(commentId, Reward.TYPE_C_COMMENT));
        }

        final int commentCnt = article.getInt(Article.ARTICLE_COMMENT_CNT);
        final int pageCount = (int) Math.ceil((double) commentCnt / (double) pageSize);

        final List<Integer> pageNums = Paginator.paginate(pageNum, pageSize, pageCount, windowSize);
        if (!pageNums.isEmpty()) {
            dataModel.put(Pagination.PAGINATION_FIRST_PAGE_NUM, pageNums.get(0));
            dataModel.put(Pagination.PAGINATION_LAST_PAGE_NUM, pageNums.get(pageNums.size() - 1));
        }

        dataModel.put(Pagination.PAGINATION_CURRENT_PAGE_NUM, pageNum);
        dataModel.put(Pagination.PAGINATION_PAGE_COUNT, pageCount);
        dataModel.put(Pagination.PAGINATION_PAGE_NUMS, pageNums);
        dataModel.put(Common.ARTICLE_COMMENTS_PAGE_SIZE, pageSize);

        // Referral statistic
        final String referralUserName = request.getParameter("r");
        if (!UserRegisterValidation.invalidUserName(referralUserName)) {
            final JSONObject referralUser = userQueryService.getUserByName(referralUserName);
            if (null == referralUser) {
                return;
            }

            final String viewerIP = Requests.getRemoteAddr(request);

            final JSONObject referral = new JSONObject();
            referral.put(Referral.REFERRAL_CLICK, 1);
            referral.put(Referral.REFERRAL_DATA_ID, articleId);
            referral.put(Referral.REFERRAL_IP, viewerIP);
            referral.put(Referral.REFERRAL_TYPE, Referral.REFERRAL_TYPE_C_ARTICLE);
            referral.put(Referral.REFERRAL_USER, referralUserName);

            referralMgmtService.updateReferral(referral);
        }
    }

    /**
     * Adds an article locally.
     *
     * <p>
     * The request json object (an article):
     * <pre>
     * {
     *   "articleTitle": "",
     *   "articleTags": "", // Tags spliting by ','
     *   "articleContent": "",
     *   "articleCommentable": boolean,
     *   "articleType": int,
     *   "articleRewardContent": "",
     *   "articleRewardPoint": int
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws IOException io exception
     * @throws ServletException servlet exception
     */
    @RequestProcessing(value = "/article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, ArticleAddValidation.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws IOException, ServletException {
        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        //final boolean articleCommentable = requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE);
        final boolean articleCommentable = true;
        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
        final String articleRewardContent = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
        final int articleRewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = request.getHeader("User-Agent");

        final JSONObject article = new JSONObject();
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_COMMENTABLE, articleCommentable);
        article.put(Article.ARTICLE_TYPE, articleType);
        article.put(Article.ARTICLE_REWARD_CONTENT, articleRewardContent);
        article.put(Article.ARTICLE_REWARD_POINT, articleRewardPoint);
        article.put(Article.ARTICLE_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            article.put(Article.ARTICLE_IP, ip);
        }
        article.put(Article.ARTICLE_UA, "");
        if (StringUtils.isNotBlank(ua)) {
            article.put(Article.ARTICLE_UA, ua);
        }

        try {
            final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);

            article.put(Article.ARTICLE_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

            final String authorEmail = currentUser.optString(User.USER_EMAIL);
            article.put(Article.ARTICLE_AUTHOR_EMAIL, authorEmail);

            if (!Role.ADMIN_ROLE.equals(currentUser.optString(User.USER_ROLE))) {
                articleTags = articleMgmtService.filterReservedTags(articleTags);
            }

            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType && StringUtils.isBlank(articleTags)) {
                articleTags = "小黑屋";
            }

            if (Article.ARTICLE_TYPE_C_THOUGHT == articleType && StringUtils.isBlank(articleTags)) {
                articleTags = "思绪";
            }

            article.put(Article.ARTICLE_TAGS, articleTags);
            article.put(Article.ARTICLE_T_IS_BROADCAST, false);

            articleMgmtService.addArticle(article);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            final String msg = e.getMessage();
            LOGGER.log(Level.ERROR, "Adds article[title=" + articleTitle + "] failed: {0}", e.getMessage());

            context.renderMsg(msg);
        }
    }

    /**
     * Shows update article.
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/update", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {CSRFToken.class, StopwatchEndAdvice.class})
    public void showUpdateArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String articleId = request.getParameter("id");
        if (Strings.isEmptyOrNull(articleId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject article = articleQueryService.getArticleById(articleId);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject currentUser = Sessions.currentUser(request);
        if (null == currentUser
                || !currentUser.optString(Keys.OBJECT_ID).equals(article.optString(Article.ARTICLE_AUTHOR_ID))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer();
        context.setRenderer(renderer);

        renderer.setTemplateName("/home/post.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put(Article.ARTICLE, article);

        filler.fillHeaderAndFooter(request, response, dataModel);

        // Qiniu file upload authenticate
        final Auth auth = Auth.create(Symphonys.get("qiniu.accessKey"), Symphonys.get("qiniu.secretKey"));
        final String uploadToken = auth.uploadToken(Symphonys.get("qiniu.bucket"));
        dataModel.put("qiniuUploadToken", uploadToken);
        dataModel.put("qiniuDomain", Symphonys.get("qiniu.domain"));

        if (!Symphonys.getBoolean("qiniu.enabled")) {
            dataModel.put("qiniuUploadToken", "");
        }

        final long imgMaxSize = Symphonys.getLong("upload.img.maxSize");
        dataModel.put("imgMaxSize", imgMaxSize);
        final long fileMaxSize = Symphonys.getLong("upload.file.maxSize");
        dataModel.put("fileMaxSize", fileMaxSize);

        String rewardEditorPlaceholderLabel = langPropsService.get("rewardEditorPlaceholderLabel");
        rewardEditorPlaceholderLabel = rewardEditorPlaceholderLabel.replace("{point}",
                String.valueOf(Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_REWARD));
        dataModel.put("rewardEditorPlaceholderLabel", rewardEditorPlaceholderLabel);
    }

    /**
     * Updates an article locally.
     *
     * <p>
     * The request json object (an article):
     * <pre>
     * {
     *   "articleTitle": "",
     *   "articleTags": "", // Tags spliting by ','
     *   "articleContent": "",
     *   "articleCommentable": boolean,
     *   "articleType": int,
     *   "articleRewardContent": "",
     *   "articleRewardPoint": int
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @param id the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{id}", method = HTTPRequestMethod.PUT)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, ArticleUpdateValidation.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
            final String id) throws Exception {
        if (Strings.isEmptyOrNull(id)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject oldArticle = articleQueryService.getArticleById(id);
        if (null == oldArticle) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        context.renderJSON();

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        //final boolean articleCommentable = requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE);
        final boolean articleCommentable = true;
        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
        final String articleRewardContent = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
        final int articleRewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = request.getHeader("User-Agent");

        final JSONObject article = new JSONObject();
        article.put(Keys.OBJECT_ID, id);
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_COMMENTABLE, articleCommentable);
        article.put(Article.ARTICLE_TYPE, articleType);
        article.put(Article.ARTICLE_REWARD_CONTENT, articleRewardContent);
        article.put(Article.ARTICLE_REWARD_POINT, articleRewardPoint);
        article.put(Article.ARTICLE_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            article.put(Article.ARTICLE_IP, ip);
        }
        article.put(Article.ARTICLE_UA, "");
        if (StringUtils.isNotBlank(ua)) {
            article.put(Article.ARTICLE_UA, ua);
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(User.USER);
        if (null == currentUser
                || !currentUser.optString(Keys.OBJECT_ID).equals(oldArticle.optString(Article.ARTICLE_AUTHOR_ID))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        article.put(Article.ARTICLE_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

        final String authorEmail = currentUser.optString(User.USER_EMAIL);
        article.put(Article.ARTICLE_AUTHOR_EMAIL, authorEmail);

        if (!Role.ADMIN_ROLE.equals(currentUser.optString(User.USER_ROLE))) {
            articleTags = articleMgmtService.filterReservedTags(articleTags);
        }

        if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType && StringUtils.isBlank(articleTags)) {
            articleTags = "小黑屋";
        }

        if (Article.ARTICLE_TYPE_C_THOUGHT == articleType && StringUtils.isBlank(articleTags)) {
            articleTags = "思绪";
        }

        article.put(Article.ARTICLE_TAGS, articleTags);

        try {

            articleMgmtService.updateArticle(article);
            context.renderTrueResult();
        } catch (final ServiceException e) {
            final String msg = e.getMessage();
            LOGGER.log(Level.ERROR, "Adds article[title=" + articleTitle + "] failed: {0}", e.getMessage());

            context.renderMsg(msg);
        }
    }

    /**
     * Adds an article remotely.
     *
     * <p>
     * This interface will be called by Rhythm, so here is no article data validation, just only validate the B3
     * key.</p>
     *
     * <p>
     * The request json object, for example,
     * <pre>
     * {
     *     "article": {
     *         "articleAuthorEmail": "DL88250@gmail.com",
     *         "articleContent": "&lt;p&gt;test&lt;\/p&gt;",
     *         "articleCreateDate": 1350635469922,
     *         "articlePermalink": "/articles/2012/10/19/1350635469866.html",
     *         "articleTags": "test",
     *         "articleTitle": "test",
     *         "clientArticleId": "1350635469866",
     *         "oId": "1350635469866"
     *     },
     *     "clientAdminEmail": "DL88250@gmail.com",
     *     "clientHost": "http://localhost:11099",
     *     "clientName": "B3log Solo",
     *     "clientTitle": "简约设计の艺术",
     *     "clientRuntimeEnv": "LOCAL",
     *     "clientVersion": "0.5.0",
     *     "symphonyKey": "....",
     *     "userB3Key": "Your key"
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/rhythm/article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addArticleFromRhythm(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);

        final String clientB3Key = requestJSONObject.getString(UserExt.USER_B3_KEY);
        final String symphonyKey = requestJSONObject.getString(Common.SYMPHONY_KEY);
        final String clientAdminEmail = requestJSONObject.getString(Client.CLIENT_ADMIN_EMAIL);
        final String clientName = requestJSONObject.getString(Client.CLIENT_NAME);
        final String clientTitle = requestJSONObject.getString(Client.CLIENT_T_TITLE);
        final String clientVersion = requestJSONObject.getString(Client.CLIENT_VERSION);
        final String clientHost = requestJSONObject.getString(Client.CLIENT_HOST);
        final String clientRuntimeEnv = requestJSONObject.getString(Client.CLIENT_RUNTIME_ENV);

//        final String maybeIP = StringUtils.substringBetween(clientHost, "://", ":");
//        if (Networks.isIPv4(maybeIP)) {
//            LOGGER.log(Level.WARN, "Sync add article error, caused by the client host [{0}] is invalid", clientHost);
//
//            return;
//        }
        final JSONObject user = userQueryService.getUserByEmail(clientAdminEmail);
        if (null == user) {
            LOGGER.log(Level.WARN, "The user[email={0}, host={1}] not found in community", clientAdminEmail, clientHost);

            return;
        }

        final String userName = user.optString(User.USER_NAME);

        String userKey = user.optString(UserExt.USER_B3_KEY);
        if (StringUtils.isBlank(userKey) || (Strings.isNumeric(userKey) && userKey.length() == clientB3Key.length())) {
            userKey = clientB3Key;

            user.put(UserExt.USER_B3_KEY, userKey);
            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);
        }

        if (!Symphonys.get("keyOfSymphony").equals(symphonyKey) || !userKey.equals(clientB3Key)) {
            LOGGER.log(Level.WARN, "B3 key not match, ignored add article [name={0}, email={1}, host={2}, userSymKey={3}, userClientKey={4}]",
                    userName, clientAdminEmail, clientHost, user.optString(UserExt.USER_B3_KEY), clientB3Key);

            return;
        }

        if (UserExt.USER_STATUS_C_VALID != user.optInt(UserExt.USER_STATUS)) {
            LOGGER.log(Level.WARN, "The user[name={0}, email={1}, host={2}] has been forbidden", userName, clientAdminEmail, clientHost);

            return;
        }

        final JSONObject originalArticle = requestJSONObject.getJSONObject(Article.ARTICLE);
        final String authorId = user.optString(Keys.OBJECT_ID);
        final String clientArticleId = originalArticle.optString(Keys.OBJECT_ID);

        final String articleTitle = originalArticle.optString(Article.ARTICLE_TITLE);
        String articleTags = Tag.formatTags(originalArticle.optString(Article.ARTICLE_TAGS));
        String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);

        final JSONObject article = new JSONObject();
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_SYNC_TO_CLIENT, false);
        article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, clientArticleId);
        article.put(Article.ARTICLE_AUTHOR_ID, authorId);
        article.put(Article.ARTICLE_AUTHOR_EMAIL, clientAdminEmail.toLowerCase().trim());

        final String permalink = originalArticle.optString(Article.ARTICLE_PERMALINK);

        final JSONObject articleExisted = articleQueryService.getArticleByClientArticleId(authorId, clientArticleId);
        final boolean toAdd = null == articleExisted;
        if (!toAdd) { // Client requests to add an article, but the article already exist in server
            article.put(Keys.OBJECT_ID, articleExisted.optString(Keys.OBJECT_ID));
            article.put(Article.ARTICLE_T_IS_BROADCAST, false);

//            articleContent += "\n\n<p class='fn-clear'><span class='fn-right'><span class='ft-small'>该文章同步自</span> "
//                    + "<i style='margin-right:5px;'><a target='_blank' href='"
//                    + clientHost + permalink + "'>" + clientTitle + "</a></i></span></p>";
        } else { // Add
            final boolean isBroadcast = "aBroadcast".equals(permalink);
            if (isBroadcast) {
                articleContent += "\n\n<p class='fn-clear'><span class='fn-right'><span class='ft-small'>该广播来自</span> "
                        + "<i style='margin-right:5px;'><a target='_blank' href='"
                        + clientHost + "'>" + clientTitle + "</a></i></span></p>";
            } else {
//                articleContent += "\n\n<p class='fn-clear'><span class='fn-right'><span class='ft-small'>该文章同步自</span> "
//                        + "<i style='margin-right:5px;'><a target='_blank' href='"
//                        + clientHost + permalink + "'>" + clientTitle + "</a></i></span></p>";
            }

            article.put(Article.ARTICLE_T_IS_BROADCAST, isBroadcast);
        }

        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK, clientHost + permalink);

        if (!Role.ADMIN_ROLE.equals(user.optString(User.USER_ROLE))) {
            articleTags = articleMgmtService.filterReservedTags(articleTags);
        }

        try {
            articleTags = "B3log," + articleTags;
            articleTags = Tag.formatTags(articleTags);
            article.put(Article.ARTICLE_TAGS, articleTags);

            if (toAdd) {
                articleMgmtService.addArticle(article);
            } else {
                articleMgmtService.updateArticle(article);
            }

            context.renderTrueResult();
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            context.renderMsg(msg);
        }

        // Updates client record
        JSONObject client = clientQueryService.getClientByAdminEmail(clientAdminEmail);
        if (null == client) {
            client = new JSONObject();
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_COMMENT_TIME, 0L);
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, System.currentTimeMillis());

            clientMgmtService.addClient(client);
        } else {
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, System.currentTimeMillis());

            clientMgmtService.updateClient(client);
        }
    }

    /**
     * Updates an article remotely.
     *
     * <p>
     * This interface will be called by Rhythm, so here is no article data validation, just only validate the B3
     * key.</p>
     *
     * <p>
     * The request json object, for example,
     * <pre>
     * {
     *     "article": {
     *         "articleAuthorEmail": "DL88250@gmail.com",
     *         "articleContent": "&lt;p&gt;test&lt;\/p&gt;",
     *         "articleCreateDate": 1350635469922,
     *         "articlePermalink": "/articles/2012/10/19/1350635469866.html",
     *         "articleTags": "test",
     *         "articleTitle": "test",
     *         "clientArticleId": "1350635469866",
     *         "oId": "1350635469866"
     *     },
     *     "clientAdminEmail": "DL88250@gmail.com",
     *     "clientHost": "http://localhost:11099",
     *     "clientName": "B3log Solo",
     *     "clientTitle": "简约设计の艺术",
     *     "clientRuntimeEnv": "LOCAL",
     *     "clientVersion": "0.5.0",
     *     "symphonyKey": "....",
     *     "userB3Key": "Your key"
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/rhythm/article", method = HTTPRequestMethod.PUT)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateArticleFromRhythm(final HTTPRequestContext context,
            final HttpServletRequest request, final HttpServletResponse response) throws Exception {
        context.renderJSON();

        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, response);

        final String clientB3Key = requestJSONObject.getString(UserExt.USER_B3_KEY);
        final String symphonyKey = requestJSONObject.getString(Common.SYMPHONY_KEY);
        final String clientAdminEmail = requestJSONObject.getString(Client.CLIENT_ADMIN_EMAIL);
        final String clientName = requestJSONObject.getString(Client.CLIENT_NAME);
        final String clientTitle = requestJSONObject.getString(Client.CLIENT_T_TITLE);
        final String clientVersion = requestJSONObject.getString(Client.CLIENT_VERSION);
        final String clientHost = requestJSONObject.getString(Client.CLIENT_HOST);
        final String clientRuntimeEnv = requestJSONObject.getString(Client.CLIENT_RUNTIME_ENV);

//        final String maybeIP = StringUtils.substringBetween(clientHost, "://", ":");
//        if (Networks.isIPv4(maybeIP)) {
//            LOGGER.log(Level.WARN, "Sync update article error, caused by the client host [{0}] is invalid", clientHost);
//
//            return;
//        }
        final JSONObject user = userQueryService.getUserByEmail(clientAdminEmail);
        if (null == user) {
            LOGGER.log(Level.WARN, "The user[email={0}, host={1}] not found in community", clientAdminEmail, clientHost);

            return;
        }

        final String userName = user.optString(User.USER_NAME);

        String userKey = user.optString(UserExt.USER_B3_KEY);
        if (StringUtils.isBlank(userKey) || (Strings.isNumeric(userKey) && userKey.length() == clientB3Key.length())) {
            userKey = clientB3Key;

            user.put(UserExt.USER_B3_KEY, userKey);
            userMgmtService.updateUser(user.optString(Keys.OBJECT_ID), user);
        }

        if (!Symphonys.get("keyOfSymphony").equals(symphonyKey) || !userKey.equals(clientB3Key)) {
            LOGGER.log(Level.WARN, "B3 key not match, ignored update article [name={0}, email={1}, host={2}, userSymKey={3}, userClientKey={4}]",
                    userName, clientAdminEmail, clientHost, user.optString(UserExt.USER_B3_KEY), clientB3Key);

            return;
        }

        if (UserExt.USER_STATUS_C_VALID != user.optInt(UserExt.USER_STATUS)) {
            LOGGER.log(Level.WARN, "The user[name={0}, email={1}, host={2}] has been forbidden", userName, clientAdminEmail, clientHost);

            return;
        }

        final JSONObject originalArticle = requestJSONObject.getJSONObject(Article.ARTICLE);

        final String articleTitle = originalArticle.optString(Article.ARTICLE_TITLE);
        String articleTags = Tag.formatTags(originalArticle.optString(Article.ARTICLE_TAGS));
        String articleContent = originalArticle.optString(Article.ARTICLE_CONTENT);

        final String permalink = originalArticle.optString(Article.ARTICLE_PERMALINK);
//        articleContent += "\n\n<p class='fn-clear'><span class='fn-right'><span class='ft-small'>该文章同步自</span> "
//                + "<i style='margin-right:5px;'><a target='_blank' href='"
//                + clientHost + permalink + "'>" + clientTitle + "</a></i></span></p>";

        final String authorId = user.optString(Keys.OBJECT_ID);
        final String clientArticleId = originalArticle.optString(Keys.OBJECT_ID);
        final JSONObject oldArticle = articleQueryService.getArticleByClientArticleId(authorId, clientArticleId);
        if (null == oldArticle) {
            LOGGER.log(Level.WARN, "Not found article [clientHost={0}, clientArticleId={1}] to update", clientHost, clientArticleId);

            return;
        }

        final JSONObject article = new JSONObject();
        article.put(Keys.OBJECT_ID, oldArticle.optString(Keys.OBJECT_ID));
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_SYNC_TO_CLIENT, false);
        article.put(Article.ARTICLE_CLIENT_ARTICLE_ID, clientArticleId);
        article.put(Article.ARTICLE_AUTHOR_ID, authorId);
        article.put(Article.ARTICLE_AUTHOR_EMAIL, clientAdminEmail.toLowerCase().trim());
        article.put(Article.ARTICLE_T_IS_BROADCAST, false);
        article.put(Article.ARTICLE_CLIENT_ARTICLE_PERMALINK, clientHost + permalink);

        if (!Role.ADMIN_ROLE.equals(user.optString(User.USER_ROLE))) {
            articleTags = articleMgmtService.filterReservedTags(articleTags);
        }

        try {
            articleTags = "B3log," + articleTags;
            articleTags = Tag.formatTags(articleTags);
            article.put(Article.ARTICLE_TAGS, articleTags);

            articleMgmtService.updateArticle(article);

            context.renderTrueResult();
        } catch (final ServiceException e) {
            final String msg = langPropsService.get("updateFailLabel") + " - " + e.getMessage();
            LOGGER.log(Level.ERROR, msg, e);

            context.renderMsg(msg);
        }

        // Updates client record
        JSONObject client = clientQueryService.getClientByAdminEmail(clientAdminEmail);
        if (null == client) {
            client = new JSONObject();
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_COMMENT_TIME, 0L);
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, System.currentTimeMillis());

            clientMgmtService.addClient(client);
        } else {
            client.put(Client.CLIENT_ADMIN_EMAIL, clientAdminEmail);
            client.put(Client.CLIENT_HOST, clientHost);
            client.put(Client.CLIENT_NAME, clientName);
            client.put(Client.CLIENT_RUNTIME_ENV, clientRuntimeEnv);
            client.put(Client.CLIENT_VERSION, clientVersion);
            client.put(Client.CLIENT_LATEST_ADD_ARTICLE_TIME, System.currentTimeMillis());

            clientMgmtService.updateClient(client);
        }
    }

    /**
     * Markdowns.
     *
     * <p>
     * Renders the response with a json object, for example,
     * <pre>
     * {
     *     "html": ""
     * }
     * </pre>
     * </p>
     *
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @param context the specified http request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/markdown", method = HTTPRequestMethod.POST)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void markdown2HTML(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        context.renderJSON(true);

        String markdownText = request.getParameter("markdownText");
        if (Strings.isEmptyOrNull(markdownText)) {
            context.renderJSONValue("html", "");

            return;
        }

        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final Set<String> userNames = userQueryService.getUserNames(markdownText);
        for (final String userName : userNames) {
            markdownText = markdownText.replace('@' + userName, "@<a href='" + Latkes.getServePath()
                    + "/member/" + userName + "'>" + userName + "</a>");
        }
        markdownText = shortLinkQueryService.linkArticle(markdownText);
        markdownText = shortLinkQueryService.linkTag(markdownText);
        markdownText = Emotions.convert(markdownText);
        markdownText = Markdowns.toHTML(markdownText);
        markdownText = Markdowns.clean(markdownText, "");

        context.renderJSONValue("html", markdownText);
    }

    /**
     * Gets article preview content.
     *
     * <p>
     * Renders the response with a json object, for example,
     * <pre>
     * {
     *     "html": ""
     * }
     * </pre>
     * </p>
     *
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @param context the specified http request context
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{articleId}/preview", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void getArticlePreviewContent(final HttpServletRequest request, final HttpServletResponse response,
            final HTTPRequestContext context, final String articleId) throws Exception {
        final String content = articleQueryService.getArticlePreviewContent(articleId, request);
        if (StringUtils.isBlank(content)) {
            context.renderJSON().renderFalseResult();

            return;
        }

        context.renderJSON(true).renderJSONValue("html", content);
    }

    /**
     * Article rewards.
     *
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @param context the specified http request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/reward", method = HTTPRequestMethod.POST)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void reward(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String articleId = request.getParameter(Article.ARTICLE_T_ID);
        if (Strings.isEmptyOrNull(articleId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

            return;
        }

        context.renderJSON();

        try {
            articleMgmtService.reward(articleId, currentUser.optString(Keys.OBJECT_ID));
        } catch (final ServiceException e) {
            context.renderMsg(langPropsService.get("transferFailLabel"));

            return;
        }

        final JSONObject article = articleQueryService.getArticle(articleId);
        articleQueryService.processArticleContent(article, request);

        context.renderTrueResult().
                renderJSONValue(Article.ARTICLE_REWARD_CONTENT, article.optString(Article.ARTICLE_REWARD_CONTENT));
    }

    /**
     * Article thanks.
     *
     * @param request the specified http servlet request
     * @param response the specified http servlet response
     * @param context the specified http request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/thank", method = HTTPRequestMethod.POST)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void thank(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String articleId = request.getParameter(Article.ARTICLE_T_ID);
        if (Strings.isEmptyOrNull(articleId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

            return;
        }

        context.renderJSON();

        try {
            articleMgmtService.thank(articleId, currentUser.optString(Keys.OBJECT_ID));
        } catch (final ServiceException e) {
            context.renderMsg(langPropsService.get("transferFailLabel"));

            return;
        }

        final JSONObject article = articleQueryService.getArticle(articleId);
        articleQueryService.processArticleContent(article, request);

        context.renderTrueResult();
    }

    /**
     * Sticks an article.
     *
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @param context the specified HTTP request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/stick", method = HTTPRequestMethod.POST)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void stickArticle(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final JSONObject currentUser = userQueryService.getCurrentUser(request);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String articleId = request.getParameter(Article.ARTICLE_T_ID);
        if (Strings.isEmptyOrNull(articleId)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);

            return;
        }

        final JSONObject article = articleQueryService.getArticle(articleId);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        if (!currentUser.optString(Keys.OBJECT_ID).equals(article.optString(Article.ARTICLE_AUTHOR_ID))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        context.renderJSON();

        try {
            articleMgmtService.stick(articleId);
        } catch (final ServiceException e) {
            context.renderMsg(e.getMessage());

            return;
        }

        context.renderTrueResult().renderMsg(langPropsService.get("stickSuccLabel"));
    }

    /**
     * Expires a sticked article.
     *
     * @param request the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @param context the specified HTTP request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/cron/article/stick-expire", method = HTTPRequestMethod.GET)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void expireStickArticle(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final String key = Symphonys.get("keyOfSymphony");
        if (!key.equals(request.getParameter("key"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        articleMgmtService.expireStick();

        context.renderJSON().renderTrueResult();
    }
}
