<#include "macro-head.ftl">
<#include "macro-pagination-query.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${article.articleTitle} - ${symphonyLabel}">
        <meta name="keywords" content="<#list article.articleTagObjs as articleTag>${articleTag.tagTitle}<#if articleTag?has_next>,</#if></#list>" />
        <meta name="description" content="${article.articlePreviewContent}"/>
        <#if 1 == article.articleStatus || 1 == article.articleAuthor.userStatus || 1 == article.articleType>
        <meta name="robots" content="NOINDEX,NOFOLLOW" />
        </#if>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/highlight.js-9.6.0/styles/github.css">
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/editor/codemirror.min.css">
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/aplayer/APlayer.min.css">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="article-action fn-clear"> 
                    <span class="fn-right">
                        <span id="thankArticle" aria-label="${thankLabel}"
                              class="tooltipped tooltipped-n has-cnt<#if article.thanked> ft-red</#if>"
                              <#if !article.thanked>onclick="Article.thankArticle('${article.oId}', ${article.articleAnonymous})"</#if>><span class="icon-heart"></span> ${article.thankedCnt}</span> 
                        <span class="tooltipped tooltipped-n has-cnt<#if isLoggedIn && 0 == article.articleVote> ft-red</#if>" aria-label="${upLabel}" onclick="Article.voteUp('${article.oId}', 'article', this)">
                            <span class="icon-thumbs-up"></span> ${article.articleGoodCnt}</span>
                        <span  class="tooltipped tooltipped-n has-cnt<#if isLoggedIn && 1 == article.articleVote> ft-red</#if>" aria-label="${downLabel}" onclick="Article.voteDown('${article.oId}', 'article', this)"><span class="icon-thumbs-down"></span> ${article.articleBadCnt}</span> 
                        <#if isLoggedIn && isFollowing>
                        <span class="tooltipped tooltipped-n has-cnt ft-red" aria-label="${uncollectLabel}" onclick="Util.unfollow(this, '${article.oId}', 'article', ${article.articleCollectCnt})"><span class="icon-star"></span> ${article.articleCollectCnt}</span>
                        <#else>
                        <span class="tooltipped tooltipped-n has-cnt" aria-label="${collectLabel}" onclick="Util.follow(this, '${article.oId}', 'article', ${article.articleCollectCnt})"><span class="icon-star"></span> ${article.articleCollectCnt}</span>
                        </#if>
                        
                        <#if article.isMyArticle && 3 != article.articleType>
                        <a href="${servePath}/update?id=${article.oId}" aria-label="${editLabel}" 
                           class="tooltipped tooltipped-n"><span class="icon-edit"></span></a>
                        </#if>
                        <#if article.isMyArticle>
                        <a class="tooltipped tooltipped-n" aria-label="${stickLabel}" 
                           href="javascript:Article.stick('${article.oId}')"><span class="icon-chevron-up"></span></a>
                        </#if>
                        <#if isAdminLoggedIn>
                        <a class="tooltipped tooltipped-n" href="${servePath}/admin/article/${article.oId}" aria-label="${adminLabel}"><span class="icon-setting"></span></a>
                        </#if>
                    </span>
                </div>
                <h2 class="article-title">
                    <#if 1 == article.articlePerfect>
                    <svg height="20" viewBox="3 3 11 12" width="14">${perfectIcon}</svg>
                    </#if>
                    <#if 1 == article.articleType>
                    <span class="icon-locked" title="${discussionLabel}"></span>
                    <#elseif 2 == article.articleType>
                    <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                    <#elseif 3 == article.articleType>
                    <span class="icon-video" title="${thoughtLabel}"></span>
                    </#if>
                    <a class="ft-a-title" href="${servePath}${article.articlePermalink}" rel="bookmark">
                        ${article.articleTitleEmoj}
                    </a>
                </h2> 
                <div class="article-info">
                    <#if article.articleAnonymous == 0>
                    <a rel="author" href="${servePath}/member/${article.articleAuthorName}"
                       title="${article.articleAuthorName}"></#if><div class="avatar" style="background-image:url('${article.articleAuthorThumbnailURL48}')"></div><#if article.articleAnonymous == 0></a></#if>
                    <div class="article-params">
                        <#if article.articleAnonymous == 0>
                        <a rel="author" href="${servePath}/member/${article.articleAuthorName}" class="ft-gray"
                           title="${article.articleAuthorName}"></#if><strong>${article.articleAuthorName}</strong><#if article.articleAnonymous == 0></a></#if>
                        <span class="ft-gray">
                        &nbsp;•&nbsp;
                        <a rel="nofollow" class="ft-gray" href="#comments">
                            <b class="article-level<#if article.articleCommentCount lt 40>${(article.articleCommentCount/10)?int}<#else>4</#if>">${article.articleCommentCount}</b> ${cmtLabel}</a>
                        &nbsp;•&nbsp;
                        <span class="article-level<#if article.articleViewCount lt 400>${(article.articleViewCount/100)?int}<#else>4</#if>">
                        <#if article.articleViewCount < 1000>
                        ${article.articleViewCount}
                        <#else>
                        ${article.articleViewCntDisplayFormat}
                        </#if>
                        </span>
                        ${viewLabel}
                        &nbsp;•&nbsp;
                        ${article.timeAgo}
                        <#if article.clientArticlePermalink?? && 0 < article.clientArticlePermalink?length>
                        &nbsp;•&nbsp; <a href="${article.clientArticlePermalink}" target="_blank" rel="nofollow"><span class="ft-green">${sourceLabel}</span></a>
                        </#if>
                    </span>
                        <#if 0 == article.articleAuthor.userUAStatus>
                        <span id="articltVia" class="ft-fade" data-ua="${article.articleUA}"></span>
                        </#if>
                        <div class="article-tags">
                        <#list article.articleTagObjs as articleTag>
                        <a rel="tag" class="tag" href="${servePath}/tag/${articleTag.tagURI}">${articleTag.tagTitle}</a>&nbsp;
                        </#list>
                        </div>
                    </div>
                </div>

                <#if 3 != article.articleType>
                <div class="content-reset article-content">${article.articleContent}</div>
                <#else>
                <div id="thoughtProgress"><span class="bar"></span><span class="icon-video"></span><div data-text="" class="content-reset" id="thoughtProgressPreview"></div></div>
                <div class="content-reset article-content"></div>
                </#if>

                <div class="fn-clear">
                    <div class="share fn-right">
                        <div id="qrCode" class="fn-none"
                             data-shareurl="${servePath}${article.articlePermalink}<#if isLoggedIn>?r=${currentUser.userName}</#if>"></div>
                        <span class="icon-wechat" data-type="wechat"></span>
                        <span class="icon-weibo" data-type="weibo"></span>
                        <span class="icon-twitter" data-type="twitter"></span>
                        <span class="icon-google" data-type="google"></span>
                    </div>
                </div>
                
                <#if 0 < article.articleRewardPoint>
                <div class="content-reset" id="articleRewardContent"<#if !article.rewarded> class="reward"</#if>>
                     <#if !article.rewarded>
                     <span>
                        ${rewardTipLabel?replace("{articleId}", article.oId)?replace("{point}", article.articleRewardPoint)}
                    </span>
                    <#else>
                    ${article.articleRewardContent}
                    </#if>
                </div>
                </#if>
                
                <#if article.articleNiceComments?size != 0>
                <div class="fn-hr10"></div>
                <div class="list comments nice">
                    <span class="ft-smaller"> ${niceCommentsLabel}</span>
                    <ul>                
                        <#list article.articleNiceComments as comment>
                        <li>
                                <#if !comment?has_next><div id="bottomComment"></div></#if>
                                <div class="fn-flex">
                                    <#if !comment.fromClient>
                                    <#if comment.commentAnonymous == 0>
                                    <a rel="nofollow" href="${servePath}/member/${comment.commentAuthorName}"></#if>
                                        <div class="avatar tooltipped tooltipped-se" 
                                             aria-label="${comment.commentAuthorName}" style="background-image:url('${comment.commentAuthorThumbnailURL}')"></div>
                                    <#if comment.commentAnonymous == 0></a></#if>
                                    <#else>
                                    <div class="avatar tooltipped tooltipped-se" 
                                         aria-label="${comment.commentAuthorName}" style="background-image:url('${comment.commentAuthorThumbnailURL}')"></div>
                                    </#if>
                                    <div class="fn-flex-1">
                                        <div class="fn-clear comment-info ft-smaller">
                                            <span class="fn-left">
                                                <#if !comment.fromClient>
                                                    <#if comment.commentAnonymous == 0><a rel="nofollow" href="${servePath}/member/${comment.commentAuthorName}" class="ft-gray"></#if><span class="ft-gray">${comment.commentAuthorName}</span><#if comment.commentAnonymous == 0></a></#if>
                                                <#else><span class="ft-gray">${comment.commentAuthorName}</span>
                                                <span class="ft-fade"> • </span>
                                                <a rel="nofollow" class="ft-green" href="https://hacpai.com/article/1457158841475">API</a>
                                                </#if>
                                                <span class="ft-fade">• ${comment.timeAgo}</span>

                                                <#if comment.rewardedCnt gt 0>
                                                <#assign hasRewarded = isLoggedIn && comment.commentAuthorId != currentUser.oId && comment.rewarded>
                                                <span aria-label="<#if hasRewarded>${thankedLabel}<#else>${thankLabel} ${comment.rewardedCnt}</#if>" 
                                                      class="tooltipped tooltipped-n rewarded-cnt <#if hasRewarded>ft-red<#else>ft-fade</#if>">
                                                    <span class="icon-heart"></span>${comment.rewardedCnt}
                                                </span>
                                                </#if>
                                                <#if 0 == comment.commenter.userUAStatus><span class="cmt-via ft-fade" data-ua="${comment.commentUA}"></span></#if>
                                            </span>
                                            <a class="ft-a-icon fn-right tooltipped tooltipped-nw" aria-label="${goCommentLabel}"
                                               href="javascript:Comment.goComment('${servePath}/article/${article.oId}?p=${comment.paginationCurrentPageNum}&m=${userCommentViewMode}#${comment.oId}')"><span class="icon-down"></span></a>
                                        </div>
                                        <div class="content-reset comment">
                                            ${comment.commentContent}
                                        </div>
                                    </div>
                                </div>
                            </li>
                        </#list>  
                    </ul>
                </div>
                </#if>
                    
                <#if 1 == userCommentViewMode>
                <#if isLoggedIn>
                <#if discussionViewable && article.articleCommentable>
                <div class="form fn-clear comment-wrap">
                    <br/>
                    <div id="replyUseName"> </div>
                    <textarea id="commentContent" placeholder="${commentEditorPlaceholderLabel}"></textarea>
                    <div class="tip" id="addCommentTip"></div>

                    <div class="fn-clear comment-submit">
                        <label class="anonymous-check">${anonymousLabel}<input type="checkbox" id="commentAnonymous"></label>
                        <button class="red fn-right" onclick="Comment.add('${article.oId}', '${csrfToken}')">${replyLabel}</button>
                    </div>
                </div>
                </#if>
                <#else>
                <div class="comment-login">
                    <a rel="nofollow" href="javascript:window.scrollTo(0,0);Util.showLogin();">${loginDiscussLabel}</a>
                </div>
                </#if>
                </#if>
            </div>
            <div>
                <div class="fn-clear">
                    <div class="list comments" id="comments">
                            <div class="fn-clear comment-header">
                                <span class="fn-left ft-smaller">${article.articleCommentCount} ${cmtLabel}</span>
                                <span<#if article.articleComments?size == 0> class="fn-none"</#if>>
                                    <a class="tooltipped tooltipped-nw fn-right" href="#bottomComment" aria-label="${jumpToBottomCommentLabel}"><span class="icon-chevron-down"></span></a>
                                    <a class="tooltipped tooltipped-nw fn-right" href="javascript:Comment.exchangeCmtSort(${userCommentViewMode})"
                                       aria-label="<#if 0 == userCommentViewMode>${changeToLabel}${realTimeLabel}${cmtViewModeLabel}<#else>${changeToLabel}${traditionLabel}${cmtViewModeLabel}</#if>"><span class="icon-<#if 0 == userCommentViewMode>sortasc<#else>time</#if>"></span></a>
                                </span>
                            </div>
                            <ul>
                                <#assign notificationCmtIds = "">
                                <#list article.articleComments as comment>
                                <#assign notificationCmtIds = notificationCmtIds + comment.oId>
                                <#if comment_has_next><#assign notificationCmtIds = notificationCmtIds + ","></#if>
                                <li id="${comment.oId}" 
                                    class="<#if comment.commentStatus == 1>shield</#if><#if comment.commentNice> perfect</#if><#if comment.commentReplyCnt != 0> selected</#if>">
                                    <#if !comment?has_next><div id="bottomComment"></div></#if>
                                    <div class="fn-flex">
                                        <#if !comment.fromClient>
                                        <#if comment.commentAnonymous == 0>
                                        <a rel="nofollow" href="${servePath}/member/${comment.commentAuthorName}"></#if>
                                            <div class="avatar tooltipped tooltipped-se" 
                                                 aria-label="${comment.commentAuthorName}" style="background-image:url('${comment.commentAuthorThumbnailURL}')"></div>
                                        <#if comment.commentAnonymous == 0></a></#if>
                                        <#else>
                                        <div class="avatar tooltipped tooltipped-se" 
                                             aria-label="${comment.commentAuthorName}" style="background-image:url('${comment.commentAuthorThumbnailURL}')"></div>
                                        </#if>
                                        <div class="fn-flex-1">
                                            <div class="comment-get-comment list"></div>
                                            <div class="fn-clear comment-info ft-smaller">
                                                <span class="fn-left">
                                                    <#if !comment.fromClient>
                                                      <#if comment.commentAnonymous == 0><a rel="nofollow" href="${servePath}/member/${comment.commentAuthorName}" class="ft-gray"></#if><span class="ft-gray">${comment.commentAuthorName}</span><#if comment.commentAnonymous == 0></a></#if>
                                                    <#else><span class="ft-gray">${comment.commentAuthorName}</span>
                                                    <span class="ft-fade"> • </span>
                                                    <a rel="nofollow" class="ft-green" href="https://hacpai.com/article/1457158841475">API</a>
                                                    </#if>
                                                    <span class="ft-fade">• ${comment.timeAgo}</span>
                                                    
                                                    <span class="comment-reward">
                                                    <#if comment.rewardedCnt gt 0>
                                                    <#assign hasRewarded = isLoggedIn && comment.commentAuthorId != currentUser.oId && comment.rewarded>
                                                    <span aria-label="<#if hasRewarded>${thankedLabel}<#else>${thankLabel} ${comment.rewardedCnt}</#if>" 
                                                          class="tooltipped tooltipped-n rewarded-cnt fn-hidden hover-show <#if hasRewarded>ft-red<#else>ft-fade</#if>">
                                                        <span class="icon-heart"></span>${comment.rewardedCnt}
                                                    </span>
                                                    </#if>
                                                    </span>
                                                    <#if 0 == comment.commenter.userUAStatus><span class="cmt-via ft-fade hover-show fn-hidden" data-ua="${comment.commentUA}"></span></#if>
                                                </span>
                                                <span class="fn-right">
                                                    <#if comment.commentOriginalCommentId != ''>
                                                    <span class="fn-pointer ft-fade tooltipped tooltipped-nw" aria-label="${goCommentLabel}" 
                                                       onclick="Comment.showReply('${comment.commentOriginalCommentId}', this, 'comment-get-comment')"><span class="icon-reply-to"></span>
                                                        <div class="avatar-small" style="background-image:url('${comment.commentOriginalAuthorThumbnailURL}')"></div>
                                                    </span> 
                                                    </#if>
                                                    <#if isAdminLoggedIn>
                                                    <a class="tooltipped tooltipped-n ft-a-icon hover-show fn-hidden" href="${servePath}/admin/comment/${comment.oId}" 
                                                       aria-label="${adminLabel}"><span class="icon-setting"></span></a>
                                                    </#if>
                                                </span>
                                            </div>
                                            <div class="content-reset comment">
                                                ${comment.commentContent}
                                            </div>
                                            <div class="comment-action">
                                                <div class="ft-fade fn-clear">
                                                    <#if comment.commentReplyCnt != 0>
                                                    <span class="fn-pointer ft-smaller" onclick="Comment.showReply('${comment.oId}', this, 'comment-replies')">
                                                        ${comment.commentReplyCnt} ${replyLabel} <span class="icon-chevron-down"></span>
                                                    </span>
                                                    </#if>
                                                     <span class="fn-right fn-hidden hover-show">
                                                        <#if (isLoggedIn && !comment.rewarded) || !isLoggedIn>
                                                        <span class="fn-pointer tooltipped tooltipped-n"
                                                              aria-label="${thankLabel}"
                                                              onclick="Comment.thank('${comment.oId}', '${csrfToken}', '${comment.commentThankLabel}', ${comment.commentAnonymous}, this)"><span class="icon-heart"></span></span>
                                                        </#if>
                                                        <span class="tooltipped tooltipped-n fn-pointer" 
                                                              aria-label="${upLabel} ${comment.commentGoodCnt}"
                                                              onclick="Article.voteUp('${comment.oId}', 'comment', this)">
                                                            <span class="icon-thumbs-up<#if isLoggedIn && 0 == comment.commentVote> ft-red</#if>"></span></span>
                                                        <span class="tooltipped tooltipped-n fn-pointer"
                                                              aria-label="${downLabel} ${comment.commentBadCnt}" 
                                                              onclick="Article.voteDown('${comment.oId}', 'comment', this)">
                                                            <span class="icon-thumbs-down<#if isLoggedIn && 1 == comment.commentVote> ft-red</#if>"></span></span>

                                                        <#if (isLoggedIn && comment.commentAuthorName != currentUser.userName) || !isLoggedIn>
                                                        <span aria-label="${replyLabel}" class="fn-pointer tooltipped tooltipped-n" 
                                                              onclick="Comment.reply('${comment.commentAuthorName}', '${comment.oId}')"><span class="icon-reply"></span></span>
                                                        </#if>
                                                    </span>
                                                </div>
                                                <div class="comment-replies list"></div>
                                            </div>
                                        </div>
                                    </div>
                                </li>
                                </#list>  
                            </ul>
                        </div>
                    <@pagination url=article.articlePermalink query="m=${userCommentViewMode}" />
                </div>
                <#if 0 == userCommentViewMode>
                <#if isLoggedIn>
                <#if discussionViewable && article.articleCommentable>
                <div class="form fn-clear wrapper">
                    <div id="replyUseName"> </div>
                    <textarea id="commentContent" placeholder="${commentEditorPlaceholderLabel}"></textarea>
                    <div class="tip" id="addCommentTip"></div>

                    <div class="fn-clear comment-submit">
                        <label class="anonymous-check">${anonymousLabel}<input type="checkbox" id="commentAnonymous"></label>
                        <button class="red fn-right" onclick="Comment.add('${article.oId}', '${csrfToken}')">${replyLabel}</button>
                    </div>
                    <div class="fn-hr10"></div>
                    <div class="fn-hr10"></div>
                </div>
                </#if>
                <#else>
                <div class="comment-login wrapper">
                    <a rel="nofollow" href="javascript:window.scrollTo(0,0);Util.showLogin();">${loginDiscussLabel}</a>
                </div>
                <div class="fn-hr10"></div>
                </#if>
                </#if>
            </div>
            <div class="side wrapper">
                <#if ADLabel!="">
                <div class="module">
                    <div class="module-header">
                        <h2>
                            ${sponsorLabel} 
                            <a href="https://hacpai.com/article/1460083956075" class="fn-right ft-13 ft-gray" target="_blank">${wantPutOnLabel}</a>
                        </h2>
                    </div>
                    <div class="module-panel ad fn-clear">
                        ${ADLabel}
                    </div>
                </div>
                </#if>
                <#if sideRelevantArticles?size != 0>
                <div class="module">
                    <div class="module-header">
                        <h2>
                            ${relativeArticleLabel}
                        </h2>
                    </div>
                    <div class="module-panel">
                        <ul class="module-list">
                            <#list sideRelevantArticles as relevantArticle>
                            <li<#if !relevantArticle_has_next> class="last"</#if>>
                                <#if "someone" != relevantArticle.articleAuthorName>
                                <a rel="nofollow" 
                               href="${servePath}/member/${relevantArticle.articleAuthorName}"></#if>
                                    <span class="avatar-small slogan"
                                          style="background-image:url('${relevantArticle.articleAuthorThumbnailURL20}')"></span>
                                    <#if "someone" != relevantArticle.articleAuthorName></a></#if>
                                <a rel="nofollow" class="title" href="${relevantArticle.articlePermalink}">${relevantArticle.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                </#if>
                <#if sideRandomArticles?size != 0>
                <div class="module">
                    <div class="module-header">
                        <h2>
                            ${randomArticleLabel}
                        </h2>
                    </div>
                    <div class="module-panel">
                        <ul class="module-list">
                            <#list sideRandomArticles as randomArticle>
                            <li<#if !randomArticle_has_next> class="last"</#if>>
                                <#if "someone" != randomArticle.articleAuthorName>
                                <a  rel="nofollow"
                                href="${servePath}/member/${randomArticle.articleAuthorName}"></#if>
                                    <span class="avatar-small slogan"
                                          style="background-image:url('${randomArticle.articleAuthorThumbnailURL20}')"></span>
                                    <#if "someone" != randomArticle.articleAuthorName></a></#if>
                                <a class="title" rel="nofollow" href="${randomArticle.articlePermalink}">${randomArticle.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                </#if>
            </div>
        </div>
        <div id="heatBar">
            <i class="heat" style="width:${article.articleHeat*3}px"></i>
        </div>
        <#include "footer.ftl">
        <script src="${staticServePath}/js/lib/compress/article-libs.min.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/article${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Label.commentErrorLabel = "${commentErrorLabel}";
            Label.symphonyLabel = "${symphonyLabel}";
            Label.rewardConfirmLabel = "${rewardConfirmLabel?replace('{point}', article.articleRewardPoint)}";
            Label.thankArticleConfirmLabel = "${thankArticleConfirmLabel?replace('{point}', pointThankArticle)}";
            Label.thankSentLabel = "${thankSentLabel}";
            Label.articleOId = "${article.oId}";
            Label.articleTitle = "${article.articleTitle}";
            Label.recordDeniedLabel = "${recordDeniedLabel}";
            Label.recordDeviceNotFoundLabel = "${recordDeviceNotFoundLabel}";
            Label.csrfToken = "${csrfToken}";
            Label.upLabel = "${upLabel}";
            Label.downLabel = "${downLabel}";
            Label.uploadLabel = "${uploadLabel}";
            Label.userCommentViewMode = ${userCommentViewMode};
            Label.stickConfirmLabel = "${stickConfirmLabel}";
            Label.audioRecordingLabel = '${audioRecordingLabel}';
            Label.uploadingLabel = '${uploadingLabel}';
            Label.copiedLabel = '${copiedLabel}';
            Label.copyLabel = '${copyLabel}';
            Label.noRevisionLabel = "${noRevisionLabel}";
            Label.thankedLabel = "${thankedLabel}";
            Label.thankLabel = "${thankLabel}";
            Label.isAdminLoggedIn = ${isAdminLoggedIn?c};
            Label.adminLabel = '${adminLabel}';
            Label.thankSelfLabel = '${thankSelfLabel}';
            Label.articleAuthorName = '${article.articleAuthorName}';
            Label.replyLabel = '${replyLabel}';
            Label.referenceLabel = '${referenceLabel}';
            Label.goCommentLabel = '${goCommentLabel}';
            qiniuToken = "${qiniuUploadToken}";
            qiniuDomain = "${qiniuDomain}";
            <#if isLoggedIn>
                Article.makeNotificationRead('${article.oId}', '${notificationCmtIds}');
                setTimeout(function() {
                    Util.setUnreadNotificationCount();
                }, 1000);
                Label.currentUserName = '${currentUser.userName}';
            </#if>            
            // Init [Article] channel
            ArticleChannel.init("${wsScheme}://${serverHost}:${serverPort}${contextPath}/article-channel?articleId=${article.oId}&articleType=${article.articleType}");
            
            $(document).ready(function () {
                Comment.init();
                
                // jQuery File Upload
                Util.uploadFile({
                    "type": "img",
                    "id": "fileUpload",
                    "pasteZone": $(".CodeMirror"),
                    "qiniuUploadToken": "${qiniuUploadToken}",
                    "editor": Comment.editor,
                    "uploadingLabel": "${uploadingLabel}",
                    "qiniuDomain": "${qiniuDomain}",
                    "imgMaxSize": ${imgMaxSize?c},
                    "fileMaxSize": ${fileMaxSize?c}
                });
            });
            <#if 3 == article.articleType>
                Article.playThought('${article.articleContent}');
            </#if>           
        </script>
    </body>
</html>
