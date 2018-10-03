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
package org.b3log.symphony.processor;

import com.qiniu.util.Auth;
import jodd.util.Base64;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.b3log.latke.Keys;
import org.b3log.latke.ioc.Inject;
import org.b3log.latke.logging.Level;
import org.b3log.latke.logging.Logger;
import org.b3log.latke.model.Pagination;
import org.b3log.latke.model.User;
import org.b3log.latke.service.LangPropsService;
import org.b3log.latke.service.ServiceException;
import org.b3log.latke.servlet.HTTPRequestContext;
import org.b3log.latke.servlet.HTTPRequestMethod;
import org.b3log.latke.servlet.annotation.After;
import org.b3log.latke.servlet.annotation.Before;
import org.b3log.latke.servlet.annotation.RequestProcessing;
import org.b3log.latke.servlet.annotation.RequestProcessor;
import org.b3log.latke.servlet.renderer.AbstractFreeMarkerRenderer;
import org.b3log.latke.util.Paginator;
import org.b3log.latke.util.Requests;
import org.b3log.latke.util.Stopwatchs;
import org.b3log.latke.util.Strings;
import org.b3log.symphony.cache.DomainCache;
import org.b3log.symphony.model.*;
import org.b3log.symphony.processor.advice.*;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchEndAdvice;
import org.b3log.symphony.processor.advice.stopwatch.StopwatchStartAdvice;
import org.b3log.symphony.processor.advice.validate.ArticleAddValidation;
import org.b3log.symphony.processor.advice.validate.ArticleUpdateValidation;
import org.b3log.symphony.processor.advice.validate.UserRegisterValidation;
import org.b3log.symphony.service.*;
import org.b3log.symphony.util.*;
import org.json.JSONObject;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;
import java.util.List;

/**
 * Article processor.
 * <ul>
 * <li>Shows an article (/article/{articleId}), GET</li>
 * <li>Shows article pre adding form page (/pre-post), GET</li>
 * <li>Shows article adding form page (/post), GET</li>
 * <li>Adds an article (/post) <em>locally</em>, POST</li>
 * <li>Shows an article updating form page (/update) <em>locally</em>, GET</li>
 * <li>Updates an article (/article/{id}) <em>locally</em>, PUT</li>
 * <li>Markdowns text (/markdown), POST</li>
 * <li>Rewards an article (/article/reward), POST</li>
 * <li>Gets an article preview content (/article/{articleId}/preview), GET</li>
 * <li>Sticks an article (/article/stick), POST</li>
 * <li>Gets an article's revisions (/article/{id}/revisions), GET</li>
 * <li>Gets article image (/article/{articleId}/image), GET</li>
 * <li>Checks article title (/article/check-title), POST</li>
 * <li>Removes an article (/article/{id}/remove), POST</li>
 * </ul>
 * <p>
 * The '<em>locally</em>' means user post an article on Symphony directly rather than receiving an article from
 * externally (for example Rhythm).
 * </p>
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.27.2.5, Aug 10, 2018
 * @since 0.2.0
 */
@RequestProcessor
public class ArticleProcessor {

    /**
     * Logger.
     */
    private static final Logger LOGGER = Logger.getLogger(ArticleProcessor.class);

    /**
     * Revision query service.
     */
    @Inject
    private RevisionQueryService revisionQueryService;

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
     * Domain query service.
     */
    @Inject
    private DomainQueryService domainQueryService;

    /**
     * Domain cache.
     */
    @Inject
    private DomainCache domainCache;

    /**
     * Data model service.
     */
    @Inject
    private DataModelService dataModelService;

    /**
     * Removes an article.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{id}/remove", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, PermissionCheck.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void removeArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                              final String id) throws Exception {
        if (StringUtils.isBlank(id)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String currentUserId = currentUser.optString(Keys.OBJECT_ID);
        final JSONObject article = articleQueryService.getArticle(id);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
        if (!authorId.equals(currentUserId)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        context.renderJSON();
        try {
            articleMgmtService.removeArticle(id);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
            context.renderJSONValue(Article.ARTICLE_T_ID, id);
        } catch (final ServiceException e) {
            final String msg = e.getMessage();

            context.renderMsg(msg);
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Checks article title.
     *
     * @param context the specified context
     * @param request the specified request
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/check-title", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void checkArticleTitle(final HTTPRequestContext context, final HttpServletRequest request) throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        final String currentUserId = currentUser.optString(Keys.OBJECT_ID);
        final JSONObject requestJSONObject = Requests.parseRequestJSONObject(request, context.getResponse());
        String title = requestJSONObject.optString(Article.ARTICLE_TITLE);
        title = StringUtils.trim(title);
        String id = requestJSONObject.optString(Article.ARTICLE_T_ID);

        final JSONObject article = articleQueryService.getArticleByTitle(title);
        if (null == article) {
            context.renderJSON(true);

            return;
        }

        if (StringUtils.isBlank(id)) { // Add
            final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);

            String msg;
            if (authorId.equals(currentUserId)) {
                msg = langPropsService.get("duplicatedArticleTitleSelfLabel");
                msg = msg.replace("{article}", "<a target='_blank' href='/article/" + article.optString(Keys.OBJECT_ID)
                        + "'>" + title + "</a>");
            } else {
                final JSONObject author = userQueryService.getUser(authorId);
                final String userName = author.optString(User.USER_NAME);

                msg = langPropsService.get("duplicatedArticleTitleLabel");
                msg = msg.replace("{user}", "<a target='_blank' href='/member/" + userName + "'>" + userName + "</a>");
                msg = msg.replace("{article}", "<a target='_blank' href='/article/" + article.optString(Keys.OBJECT_ID)
                        + "'>" + title + "</a>");
            }

            final JSONObject ret = new JSONObject();
            ret.put(Keys.STATUS_CODE, false);
            ret.put(Keys.MSG, msg);

            context.renderJSON(ret);
        } else { // Update
            final JSONObject oldArticle = articleQueryService.getArticle(id);
            if (oldArticle.optString(Article.ARTICLE_TITLE).equals(title)) {
                context.renderJSON(true);

                return;
            }

            final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);

            String msg;
            if (authorId.equals(currentUserId)) {
                msg = langPropsService.get("duplicatedArticleTitleSelfLabel");
                msg = msg.replace("{article}", "<a target='_blank' href='/article/" + article.optString(Keys.OBJECT_ID)
                        + "'>" + title + "</a>");
            } else {
                final JSONObject author = userQueryService.getUser(authorId);
                final String userName = author.optString(User.USER_NAME);

                msg = langPropsService.get("duplicatedArticleTitleLabel");
                msg = msg.replace("{user}", "<a target='_blank' href='/member/" + userName + "'>" + userName + "</a>");
                msg = msg.replace("{article}", "<a target='_blank' href='/article/" + article.optString(Keys.OBJECT_ID)
                        + "'>" + title + "</a>");
            }

            final JSONObject ret = new JSONObject();
            ret.put(Keys.STATUS_CODE, false);
            ret.put(Keys.MSG, msg);

            context.renderJSON(ret);
        }
    }

    /**
     * Gets article image.
     *
     * @param context   the specified context
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{articleId}/image", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void getArticleImage(final HTTPRequestContext context, final String articleId) throws Exception {
        final JSONObject article = articleQueryService.getArticle(articleId);
        final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);

        final Set<JSONObject> characters = characterQueryService.getWrittenCharacters();
        final String articleContent = article.optString(Article.ARTICLE_CONTENT);

        final List<BufferedImage> images = new ArrayList<>();
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
        ret.put(Common.URL, url);

        context.renderJSON(ret);
    }

    /**
     * Gets an article's revisions.
     *
     * @param context the specified context
     * @param id      the specified article id
     */
    @RequestProcessing(value = "/article/{id}/revisions", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, PermissionCheck.class})
    @After(adviceClass = {StopwatchEndAdvice.class})
    public void getArticleRevisions(final HTTPRequestContext context, final String id) {
        final List<JSONObject> revisions = revisionQueryService.getArticleRevisions(id);
        final JSONObject ret = new JSONObject();
        ret.put(Keys.STATUS_CODE, true);
        ret.put(Revision.REVISIONS, (Object) revisions);

        context.renderJSON(ret);
    }

    /**
     * Shows pre-add article.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestProcessing(value = "/pre-post", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    public void showPreAddArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);

        renderer.setTemplateName("home/pre-post.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put(Common.BROADCAST_POINT, Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST);

        dataModelService.fillHeaderAndFooter(request, response, dataModel);
    }

    /**
     * Fills the domains with tags.
     *
     * @param dataModel the specified data model
     */
    private void fillDomainsWithTags(final Map<String, Object> dataModel) {
        final List<JSONObject> domains = domainQueryService.getAllDomains();
        dataModel.put(Common.ADD_ARTICLE_DOMAINS, domains);
        for (final JSONObject domain : domains) {
            final List<JSONObject> tags = domainQueryService.getTags(domain.optString(Keys.OBJECT_ID));

            domain.put(Domain.DOMAIN_T_TAGS, (Object) tags);
        }

        final JSONObject user = (JSONObject) dataModel.get(Common.CURRENT_USER);
        if (null == user) {
            return;
        }

        try {
            final JSONObject followingTagsResult = followQueryService.getFollowingTags(
                    user.optString(Keys.OBJECT_ID), 1, 28);
            final List<JSONObject> followingTags = (List<JSONObject>) followingTagsResult.opt(Keys.RESULTS);
            if (!followingTags.isEmpty()) {
                final JSONObject userWatched = new JSONObject();
                userWatched.put(Keys.OBJECT_ID, String.valueOf(System.currentTimeMillis()));
                userWatched.put(Domain.DOMAIN_TITLE, langPropsService.get("notificationFollowingLabel"));
                userWatched.put(Domain.DOMAIN_T_TAGS, (Object) followingTags);

                domains.add(0, userWatched);
            }
        } catch (final Exception e) {
            LOGGER.log(Level.ERROR, "Get user [name=" + user.optString(User.USER_NAME) + "] following tags failed", e);
        }
    }

    /**
     * Shows add article.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     */
    @RequestProcessing(value = "/post", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    public void showAddArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response) {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/post.ftl");
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
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);

        if (StringUtils.isBlank(tags)) {
            tags = "";

            dataModel.put(Tag.TAGS, tags);
        } else {
            tags = Tag.formatTags(tags);
            final String[] tagTitles = tags.split(",");

            final StringBuilder tagBuilder = new StringBuilder();
            for (final String title : tagTitles) {
                final String tagTitle = title.trim();

                if (StringUtils.isBlank(tagTitle)) {
                    continue;
                }

                if (Tag.containsWhiteListTags(tagTitle)) {
                    tagBuilder.append(tagTitle).append(",");

                    continue;
                }

                if (!Tag.TAG_TITLE_PATTERN.matcher(tagTitle).matches()) {
                    continue;
                }

                if (tagTitle.length() > Tag.MAX_TAG_TITLE_LENGTH) {
                    continue;
                }

                if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))
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

        String at = request.getParameter(Common.AT);
        at = StringUtils.trim(at);
        if (StringUtils.isNotBlank(at)) {
            dataModel.put(Common.AT, at + " ");
        }

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        String rewardEditorPlaceholderLabel = langPropsService.get("rewardEditorPlaceholderLabel");
        rewardEditorPlaceholderLabel = rewardEditorPlaceholderLabel.replace("{point}",
                String.valueOf(Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_REWARD));
        dataModel.put("rewardEditorPlaceholderLabel", rewardEditorPlaceholderLabel);
        dataModel.put(Common.BROADCAST_POINT, Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST);

        String articleContentErrorLabel = langPropsService.get("articleContentErrorLabel");
        articleContentErrorLabel = articleContentErrorLabel.replace("{maxArticleContentLength}",
                String.valueOf(ArticleAddValidation.MAX_ARTICLE_CONTENT_LENGTH));
        dataModel.put("articleContentErrorLabel", articleContentErrorLabel);

        fillPostArticleRequisite(dataModel, currentUser);
        fillDomainsWithTags(dataModel);
    }

    private void fillPostArticleRequisite(final Map<String, Object> dataModel, final JSONObject currentUser) {
        boolean requisite = false;
        String requisiteMsg = "";

//        if (!UserExt.updatedAvatar(currentUser)) {
//            requisite = true;
//            requisiteMsg = langPropsService.get("uploadAvatarThenPostLabel");
//        }

        dataModel.put(Common.REQUISITE, requisite);
        dataModel.put(Common.REQUISITE_MSG, requisiteMsg);
    }

    /**
     * Shows article with the specified article id.
     *
     * @param context   the specified context
     * @param request   the specified request
     * @param response  the specified response
     * @param articleId the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{articleId}", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, AnonymousViewCheck.class})
    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    public void showArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                            final String articleId) throws Exception {
        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("article.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject article = articleQueryService.getArticleById(avatarViewMode, articleId);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

        final String authorId = article.optString(Article.ARTICLE_AUTHOR_ID);
        final JSONObject author = userQueryService.getUser(authorId);
        Escapes.escapeHTML(author);

        if (Article.ARTICLE_ANONYMOUS_C_PUBLIC == article.optInt(Article.ARTICLE_ANONYMOUS)) {
            article.put(Article.ARTICLE_T_AUTHOR_NAME, author.optString(User.USER_NAME));
            article.put(Article.ARTICLE_T_AUTHOR_URL, author.optString(User.USER_URL));
            article.put(Article.ARTICLE_T_AUTHOR_INTRO, author.optString(UserExt.USER_INTRO));
        } else {
            article.put(Article.ARTICLE_T_AUTHOR_NAME, UserExt.ANONYMOUS_USER_NAME);
            article.put(Article.ARTICLE_T_AUTHOR_URL, "");
            article.put(Article.ARTICLE_T_AUTHOR_INTRO, "");
        }
        dataModel.put(Article.ARTICLE, article);

        article.put(Common.IS_MY_ARTICLE, false);
        article.put(Article.ARTICLE_T_AUTHOR, author);
        article.put(Common.REWARDED, false);
        article.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(articleId, Reward.TYPE_C_ARTICLE));
        article.put(Article.ARTICLE_REVISION_COUNT, revisionQueryService.count(articleId, Revision.DATA_TYPE_C_ARTICLE));

        articleQueryService.processArticleContent(article, request);

        String cmtViewModeStr = request.getParameter("m");
        JSONObject currentUser;
        String currentUserId = null;
        final boolean isLoggedIn = (Boolean) dataModel.get(Common.IS_LOGGED_IN);
        if (isLoggedIn) {
            currentUser = (JSONObject) dataModel.get(Common.CURRENT_USER);
            currentUserId = currentUser.optString(Keys.OBJECT_ID);
            article.put(Common.IS_MY_ARTICLE, currentUserId.equals(article.optString(Article.ARTICLE_AUTHOR_ID)));

            final boolean isFollowing = followQueryService.isFollowing(currentUserId, articleId, Follow.FOLLOWING_TYPE_C_ARTICLE);
            dataModel.put(Common.IS_FOLLOWING, isFollowing);

            final boolean isWatching = followQueryService.isFollowing(currentUserId, articleId, Follow.FOLLOWING_TYPE_C_ARTICLE_WATCH);
            dataModel.put(Common.IS_WATCHING, isWatching);

            final int articleVote = voteQueryService.isVoted(currentUserId, articleId);
            article.put(Article.ARTICLE_T_VOTE, articleVote);

            if (currentUserId.equals(author.optString(Keys.OBJECT_ID))) {
                article.put(Common.REWARDED, true);
            } else {
                article.put(Common.REWARDED, rewardQueryService.isRewarded(currentUserId, articleId, Reward.TYPE_C_ARTICLE));
            }

            if (StringUtils.isBlank(cmtViewModeStr) || !Strings.isNumeric(cmtViewModeStr)) {
                cmtViewModeStr = currentUser.optString(UserExt.USER_COMMENT_VIEW_MODE);
            }
        } else if (StringUtils.isBlank(cmtViewModeStr) || !Strings.isNumeric(cmtViewModeStr)) {
            cmtViewModeStr = "0";
        }

        final int cmtViewMode = Integer.valueOf(cmtViewModeStr);
        dataModel.put(UserExt.USER_COMMENT_VIEW_MODE, cmtViewMode);

        final JSONObject viewer = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null != viewer) {
            livenessMgmtService.incLiveness(viewer.optString(Keys.OBJECT_ID), Liveness.LIVENESS_PV);
        }

        if (!(Boolean) request.getAttribute(Keys.HttpRequest.IS_SEARCH_ENGINE_BOT)) {
            final long created = System.currentTimeMillis();
            final long expired = DateUtils.addMonths(new Date(created), 1).getTime();
            final String ip = Requests.getRemoteAddr(request);
            final String ua = Headers.getHeader(request, Common.USER_AGENT, "");
            final String referer = Headers.getHeader(request, "Referer", "");
            final JSONObject visit = new JSONObject();
            visit.put(Visit.VISIT_IP, ip);
            visit.put(Visit.VISIT_CITY, "");
            visit.put(Visit.VISIT_CREATED, created);
            visit.put(Visit.VISIT_DEVICE_ID, "");
            visit.put(Visit.VISIT_EXPIRED, expired);
            visit.put(Visit.VISIT_REFERER_URL, referer);
            visit.put(Visit.VISIT_UA, ua);
            visit.put(Visit.VISIT_URL, "/article/" + articleId);
            visit.put(Visit.VISIT_USER_ID, "");
            if (null != viewer) {
                visit.put(Visit.VISIT_USER_ID, viewer.optString(Keys.OBJECT_ID));
            }

            articleMgmtService.incArticleViewCount(visit);
        }

        dataModelService.fillRelevantArticles(avatarViewMode, dataModel, article);
        dataModelService.fillRandomArticles(dataModel);
        dataModelService.fillSideHotArticles(dataModel);

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
        Stopwatchs.start("Fills article thank");
        try {
            article.put(Common.THANKED, rewardQueryService.isRewarded(currentUserId, articleId, Reward.TYPE_C_THANK_ARTICLE));
            article.put(Common.THANKED_COUNT, rewardQueryService.rewardedCount(articleId, Reward.TYPE_C_THANK_ARTICLE));
            final String articleAuthorId = article.optString(Article.ARTICLE_AUTHOR_ID);
            if (Article.ARTICLE_TYPE_C_QNA == article.optInt(Article.ARTICLE_TYPE)) {
                article.put(Common.OFFERED, rewardQueryService.isRewarded(articleAuthorId, articleId, Reward.TYPE_C_ACCEPT_COMMENT));
                final JSONObject offeredComment = commentQueryService.getOfferedComment(avatarViewMode, cmtViewMode, articleId);
                article.put(Article.ARTICLE_T_OFFERED_COMMENT, offeredComment);
                if (null != offeredComment) {
                    final String offeredCmtId = offeredComment.optString(Keys.OBJECT_ID);
                    offeredComment.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(offeredCmtId, Reward.TYPE_C_COMMENT));
                    offeredComment.put(Common.REWARDED, rewardQueryService.isRewarded(currentUserId, offeredCmtId, Reward.TYPE_C_COMMENT));
                }
            }
        } finally {
            Stopwatchs.end();
        }

        // Fill previous/next article
        final JSONObject previous = articleQueryService.getPreviousPermalink(articleId);
        final JSONObject next = articleQueryService.getNextPermalink(articleId);
        dataModel.put(Article.ARTICLE_T_PREVIOUS, previous);
        dataModel.put(Article.ARTICLE_T_NEXT, next);

        String stickConfirmLabel = langPropsService.get("stickConfirmLabel");
        stickConfirmLabel = stickConfirmLabel.replace("{point}", Symphonys.get("pointStickArticle"));
        dataModel.put("stickConfirmLabel", stickConfirmLabel);
        dataModel.put("pointThankArticle", Symphonys.get("pointThankArticle"));
        final int pageNum = Paginator.getPage(request);
        final int pageSize = Symphonys.getInt("articleCommentsPageSize");
        final int windowSize = Symphonys.getInt("articleCommentsWindowSize");

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

        dataModel.put(Common.DISCUSSION_VIEWABLE, article.optBoolean(Common.DISCUSSION_VIEWABLE));
        if (!article.optBoolean(Common.DISCUSSION_VIEWABLE)) {
            article.put(Article.ARTICLE_T_COMMENTS, (Object) Collections.emptyList());
            article.put(Article.ARTICLE_T_NICE_COMMENTS, (Object) Collections.emptyList());

            return;
        }

        final List<JSONObject> niceComments = commentQueryService.getNiceComments(avatarViewMode, cmtViewMode, articleId, 3);
        article.put(Article.ARTICLE_T_NICE_COMMENTS, (Object) niceComments);

        double niceCmtScore = Double.MAX_VALUE;
        if (!niceComments.isEmpty()) {
            niceCmtScore = niceComments.get(niceComments.size() - 1).optDouble(Comment.COMMENT_SCORE, 0D);

            for (final JSONObject comment : niceComments) {
                String thankTemplate = langPropsService.get("thankConfirmLabel");
                thankTemplate = thankTemplate.replace("{point}", String.valueOf(Symphonys.getInt("pointThankComment")))
                        .replace("{user}", comment.optJSONObject(Comment.COMMENT_T_COMMENTER).optString(User.USER_NAME));
                comment.put(Comment.COMMENT_T_THANK_LABEL, thankTemplate);

                final String commentId = comment.optString(Keys.OBJECT_ID);
                if (isLoggedIn) {
                    comment.put(Common.REWARDED, rewardQueryService.isRewarded(currentUserId, commentId, Reward.TYPE_C_COMMENT));
                    final int commentVote = voteQueryService.isVoted(currentUserId, commentId);
                    comment.put(Comment.COMMENT_T_VOTE, commentVote);
                }

                comment.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(commentId, Reward.TYPE_C_COMMENT));

                // https://github.com/b3log/symphony/issues/682
                if (Comment.COMMENT_VISIBLE_C_AUTHOR == comment.optInt(Comment.COMMENT_VISIBLE)) {
                    final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
                    if (!isLoggedIn || (!StringUtils.equals(currentUserId, commentAuthorId) && !StringUtils.equals(currentUserId, authorId))) {
                        comment.put(Comment.COMMENT_CONTENT, langPropsService.get("onlySelfAndArticleAuthorVisibleLabel"));
                    }
                }
            }
        }

        // Load comments
        final List<JSONObject> articleComments =
                commentQueryService.getArticleComments(avatarViewMode, articleId, pageNum, pageSize, cmtViewMode);
        article.put(Article.ARTICLE_T_COMMENTS, (Object) articleComments);

        // Fill comment thank
        Stopwatchs.start("Fills comment thank");
        try {
            final String thankTemplate = langPropsService.get("thankConfirmLabel");
            for (final JSONObject comment : articleComments) {
                comment.put(Comment.COMMENT_T_NICE, comment.optDouble(Comment.COMMENT_SCORE, 0D) >= niceCmtScore);

                final String thankStr = thankTemplate.replace("{point}", String.valueOf(Symphonys.getInt("pointThankComment")))
                        .replace("{user}", comment.optJSONObject(Comment.COMMENT_T_COMMENTER).optString(User.USER_NAME));
                comment.put(Comment.COMMENT_T_THANK_LABEL, thankStr);

                final String commentId = comment.optString(Keys.OBJECT_ID);
                if (isLoggedIn) {
                    comment.put(Common.REWARDED,
                            rewardQueryService.isRewarded(currentUserId, commentId, Reward.TYPE_C_COMMENT));
                    final int commentVote = voteQueryService.isVoted(currentUserId, commentId);
                    comment.put(Comment.COMMENT_T_VOTE, commentVote);
                }

                comment.put(Common.REWARED_COUNT, rewardQueryService.rewardedCount(commentId, Reward.TYPE_C_COMMENT));

                // https://github.com/b3log/symphony/issues/682
                if (Comment.COMMENT_VISIBLE_C_AUTHOR == comment.optInt(Comment.COMMENT_VISIBLE)) {
                    final String commentAuthorId = comment.optString(Comment.COMMENT_AUTHOR_ID);
                    if (!isLoggedIn || (!StringUtils.equals(currentUserId, commentAuthorId) && !StringUtils.equals(currentUserId, authorId))) {
                        comment.put(Comment.COMMENT_CONTENT, langPropsService.get("onlySelfAndArticleAuthorVisibleLabel"));
                    }
                }
            }
        } finally {
            Stopwatchs.end();
        }

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

        if (StringUtils.isBlank(article.optString(Article.ARTICLE_AUDIO_URL))) {
            final String uid = StringUtils.isBlank(currentUserId) ? "visitor" : currentUserId;

            articleMgmtService.genArticleAudio(article, uid);
        }

        if (StringUtils.isNotBlank(Symphonys.get("ipfs.dir"))) {
            articleMgmtService.saveMarkdown(article);
        }
    }

    /**
     * Adds an article locally.
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
     *   "articleRewardPoint": int,
     *   "articleQnAOfferPoint": int,
     *   "articleAnonymous": boolean
     * }
     * </pre>
     * </p>
     *
     * @param context the specified context
     * @param request the specified request
     */
    @RequestProcessing(value = "/article", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, ArticleAddValidation.class, PermissionCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void addArticle(final HTTPRequestContext context, final HttpServletRequest request, final JSONObject requestJSONObject) {
        context.renderJSON();

        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        final boolean articleCommentable = requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE, true);
        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
        final String articleRewardContent = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
        final int articleRewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT);
        final int articleQnAOfferPoint = requestJSONObject.optInt(Article.ARTICLE_QNA_OFFER_POINT);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = Headers.getHeader(request, Common.USER_AGENT, "");
        final boolean isAnonymous = requestJSONObject.optBoolean(Article.ARTICLE_ANONYMOUS, false);
        final int articleAnonymous = isAnonymous
                ? Article.ARTICLE_ANONYMOUS_C_ANONYMOUS : Article.ARTICLE_ANONYMOUS_C_PUBLIC;

        final JSONObject article = new JSONObject();
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_COMMENTABLE, articleCommentable);
        article.put(Article.ARTICLE_TYPE, articleType);
        article.put(Article.ARTICLE_REWARD_CONTENT, articleRewardContent);
        article.put(Article.ARTICLE_REWARD_POINT, articleRewardPoint);
        article.put(Article.ARTICLE_QNA_OFFER_POINT, articleQnAOfferPoint);
        article.put(Article.ARTICLE_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            article.put(Article.ARTICLE_IP, ip);
        }
        article.put(Article.ARTICLE_UA, ua);
        article.put(Article.ARTICLE_ANONYMOUS, articleAnonymous);

        try {
            final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);

            article.put(Article.ARTICLE_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

            if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
                articleTags = articleMgmtService.filterReservedTags(articleTags);
            }

            if (Article.ARTICLE_TYPE_C_DISCUSSION == articleType && StringUtils.isBlank(articleTags)) {
                articleTags = "小黑屋";
            }

            if (Article.ARTICLE_TYPE_C_THOUGHT == articleType && StringUtils.isBlank(articleTags)) {
                articleTags = "思绪";
            }

            article.put(Article.ARTICLE_TAGS, articleTags);

            final String articleId = articleMgmtService.addArticle(article);

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
            context.renderJSONValue(Article.ARTICLE_T_ID, articleId);
        } catch (final ServiceException e) {
            final String msg = e.getMessage();
            LOGGER.log(Level.ERROR, "Adds article[title=" + articleTitle + "] failed: {0}", e.getMessage());

            context.renderMsg(msg);
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Shows update article.
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @throws Exception exception
     */
    @RequestProcessing(value = "/update", method = HTTPRequestMethod.GET)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class})
    @After(adviceClass = {CSRFToken.class, PermissionGrant.class, StopwatchEndAdvice.class})
    public void showUpdateArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response)
            throws Exception {
        final String articleId = request.getParameter("id");
        if (StringUtils.isBlank(articleId)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject article = articleQueryService.getArticle(articleId);
        if (null == article) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser
                || !currentUser.optString(Keys.OBJECT_ID).equals(article.optString(Article.ARTICLE_AUTHOR_ID))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final AbstractFreeMarkerRenderer renderer = new SkinRenderer(request);
        context.setRenderer(renderer);
        renderer.setTemplateName("home/post.ftl");
        final Map<String, Object> dataModel = renderer.getDataModel();

        dataModel.put(Article.ARTICLE, article);
        dataModel.put(Article.ARTICLE_TYPE, article.optInt(Article.ARTICLE_TYPE));

        dataModelService.fillHeaderAndFooter(request, response, dataModel);

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

        fillDomainsWithTags(dataModel);

        String rewardEditorPlaceholderLabel = langPropsService.get("rewardEditorPlaceholderLabel");
        rewardEditorPlaceholderLabel = rewardEditorPlaceholderLabel.replace("{point}",
                String.valueOf(Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_REWARD));
        dataModel.put("rewardEditorPlaceholderLabel", rewardEditorPlaceholderLabel);
        dataModel.put(Common.BROADCAST_POINT, Pointtransfer.TRANSFER_SUM_C_ADD_ARTICLE_BROADCAST);

        fillPostArticleRequisite(dataModel, currentUser);
    }

    /**
     * Updates an article locally.
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
     *   "articleRewardPoint": int,
     *    "articleQnAOfferPoint": int
     * }
     * </pre>
     * </p>
     *
     * @param context  the specified context
     * @param request  the specified request
     * @param response the specified response
     * @param id       the specified article id
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/{id}", method = HTTPRequestMethod.PUT)
    @Before(adviceClass = {StopwatchStartAdvice.class, LoginCheck.class, CSRFCheck.class, ArticleUpdateValidation.class,
            PermissionCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void updateArticle(final HTTPRequestContext context, final HttpServletRequest request, final HttpServletResponse response,
                              final String id) throws Exception {
        if (StringUtils.isBlank(id)) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        final int avatarViewMode = (int) request.getAttribute(UserExt.USER_AVATAR_VIEW_MODE);

        final JSONObject oldArticle = articleQueryService.getArticleById(avatarViewMode, id);
        if (null == oldArticle) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);

            return;
        }

        context.renderJSON();

        if (Article.ARTICLE_STATUS_C_VALID != oldArticle.optInt(Article.ARTICLE_STATUS)) {
            context.renderMsg(langPropsService.get("articleLockedLabel"));
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);

            return;
        }

        final JSONObject requestJSONObject = (JSONObject) request.getAttribute(Keys.REQUEST);

        final String articleTitle = requestJSONObject.optString(Article.ARTICLE_TITLE);
        String articleTags = requestJSONObject.optString(Article.ARTICLE_TAGS);
        final String articleContent = requestJSONObject.optString(Article.ARTICLE_CONTENT);
        final boolean articleCommentable = requestJSONObject.optBoolean(Article.ARTICLE_COMMENTABLE, true);
        final int articleType = requestJSONObject.optInt(Article.ARTICLE_TYPE, Article.ARTICLE_TYPE_C_NORMAL);
        final String articleRewardContent = requestJSONObject.optString(Article.ARTICLE_REWARD_CONTENT);
        final int articleRewardPoint = requestJSONObject.optInt(Article.ARTICLE_REWARD_POINT);
        final int articleQnAOfferPoint = requestJSONObject.optInt(Article.ARTICLE_QNA_OFFER_POINT);
        final String ip = Requests.getRemoteAddr(request);
        final String ua = Headers.getHeader(request, Common.USER_AGENT, "");

        final JSONObject article = new JSONObject();
        article.put(Keys.OBJECT_ID, id);
        article.put(Article.ARTICLE_TITLE, articleTitle);
        article.put(Article.ARTICLE_CONTENT, articleContent);
        article.put(Article.ARTICLE_EDITOR_TYPE, 0);
        article.put(Article.ARTICLE_COMMENTABLE, articleCommentable);
        article.put(Article.ARTICLE_TYPE, articleType);
        article.put(Article.ARTICLE_REWARD_CONTENT, articleRewardContent);
        article.put(Article.ARTICLE_REWARD_POINT, articleRewardPoint);
        article.put(Article.ARTICLE_QNA_OFFER_POINT, articleQnAOfferPoint);
        article.put(Article.ARTICLE_IP, "");
        if (StringUtils.isNotBlank(ip)) {
            article.put(Article.ARTICLE_IP, ip);
        }
        article.put(Article.ARTICLE_UA, ua);

        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser
                || !currentUser.optString(Keys.OBJECT_ID).equals(oldArticle.optString(Article.ARTICLE_AUTHOR_ID))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        article.put(Article.ARTICLE_AUTHOR_ID, currentUser.optString(Keys.OBJECT_ID));

        if (!Role.ROLE_ID_C_ADMIN.equals(currentUser.optString(User.USER_ROLE))) {
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

            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.SUCC);
            context.renderJSONValue(Article.ARTICLE_T_ID, id);
        } catch (final ServiceException e) {
            final String msg = e.getMessage();
            LOGGER.log(Level.ERROR, "Adds article[title=" + articleTitle + "] failed: {0}", e.getMessage());

            context.renderMsg(msg);
            context.renderJSONValue(Keys.STATUS_CODE, StatusCodes.ERR);
        }
    }

    /**
     * Markdowns.
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
     * @param context the specified http request context
     */
    @RequestProcessing(value = "/markdown", method = HTTPRequestMethod.POST)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void markdown2HTML(final HttpServletRequest request, final HTTPRequestContext context) {
        context.renderJSON(true);
        String markdownText = request.getParameter("markdownText");
        if (StringUtils.isBlank(markdownText)) {
            context.renderJSONValue("html", "");

            return;
        }

        markdownText = shortLinkQueryService.linkArticle(markdownText);
        markdownText = shortLinkQueryService.linkTag(markdownText);
        markdownText = Emotions.toAliases(markdownText);
        markdownText = Emotions.convert(markdownText);
        markdownText = Markdowns.toHTML(markdownText);
        markdownText = Markdowns.clean(markdownText, "");
        markdownText = MP3Players.render(markdownText);
        markdownText = VideoPlayers.render(markdownText);

        context.renderJSONValue("html", markdownText);
    }

    /**
     * Gets article preview content.
     * <p>
     * Renders the response with a json object, for example,
     * <pre>
     * {
     *     "html": ""
     * }
     * </pre>
     * </p>
     *
     * @param request   the specified http servlet request
     * @param response  the specified http servlet response
     * @param context   the specified http request context
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
     * @param request  the specified http servlet request
     * @param response the specified http servlet response
     * @param context  the specified http request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/reward", method = HTTPRequestMethod.POST)
    @Before(adviceClass = StopwatchStartAdvice.class)
    @After(adviceClass = StopwatchEndAdvice.class)
    public void reward(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String articleId = request.getParameter(Article.ARTICLE_T_ID);
        if (StringUtils.isBlank(articleId)) {
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

        final String rewardContent = article.optString(Article.ARTICLE_REWARD_CONTENT);
        context.renderTrueResult().renderJSONValue(Article.ARTICLE_REWARD_CONTENT, rewardContent);
    }

    /**
     * Article thanks.
     *
     * @param request  the specified http servlet request
     * @param response the specified http servlet response
     * @param context  the specified http request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/thank", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void thank(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String articleId = request.getParameter(Article.ARTICLE_T_ID);
        if (StringUtils.isBlank(articleId)) {
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

        context.renderTrueResult();
    }

    /**
     * Sticks an article.
     *
     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @param context  the specified HTTP request context
     * @throws Exception exception
     */
    @RequestProcessing(value = "/article/stick", method = HTTPRequestMethod.POST)
    @Before(adviceClass = {StopwatchStartAdvice.class, PermissionCheck.class})
    @After(adviceClass = StopwatchEndAdvice.class)
    public void stickArticle(final HttpServletRequest request, final HttpServletResponse response, final HTTPRequestContext context)
            throws Exception {
        final JSONObject currentUser = (JSONObject) request.getAttribute(Common.CURRENT_USER);
        if (null == currentUser) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);

            return;
        }

        final String articleId = request.getParameter(Article.ARTICLE_T_ID);
        if (StringUtils.isBlank(articleId)) {
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
     * @param request  the specified HTTP servlet request
     * @param response the specified HTTP servlet response
     * @param context  the specified HTTP request context
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
