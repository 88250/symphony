<#include "macro-head.ftl">
<#include "macro-pagination-query.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${article.articleTitle} - ${symphonyLabel}">
        <meta name="keywords" content="${article.articleTags}" />
        <meta name="description" content="${article.articlePreviewContent}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/js/lib/highlight.js-8.6/styles/github.css">
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/mobile-index${miniPostfix}.css?${staticResourceVersion}" />
        <link rel="stylesheet" href="${staticServePath}/js/lib/editor/codemirror.min.css">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="article-action fn-clear">
                    <div class="fn-right">
                        <#if isLoggedIn>
                        <span id="voteUp" class="fn-pointer<#if 0==vote> ft-red</#if>" title="${upLabel} ${article.articleGoodCnt}" onclick="Util.voteUp('${article.oId}', 'article')">
                            <span class="icon-thumbs-up"></span></span>&nbsp;
                        <span id="voteDown" class="fn-pointer<#if 1==vote> ft-red</#if>" title="${downLabel} ${article.articleBadCnt}" onclick="Util.voteDown('${article.oId}', 'article')">
                            <span class="icon-thumbs-down"></span></span>
                        </#if>

                        <#if isLoggedIn>
                        <#if isFollowing>
                        <span class="ft-red fn-pointer" title="${uncollectLabel}" onclick="Util.unfollow(this, '${article.oId}', 'article')">
                            <span class="icon-star"></span>
                            ${article.articleCollectCnt}
                        </span>
                        <#else>
                        <span class="fn-pointer" title="${collectLabel}" onclick="Util.follow(this, '${article.oId}', 'article')">
                            <span class="icon-star"></span>
                            ${article.articleCollectCnt}
                        </span>
                        </#if>
                        <#else>
                        <span title="${collectLabel}" class="fn-pointer">
                            <span class="icon-star"></span>
                            ${article.articleCollectCnt}
                        </span>
                        </#if>

                        <#if article.isMyArticle && 3 != article.articleType>
                        <a href="/update?id=${article.oId}" title="${editLabel}" class="icon-edit"></a>
                        </#if>
                        <#if article.isMyArticle>
                        <a class="icon-chevron-up" title="${stickLabel}" 
                           href="javascript:Article.stick('${article.oId}')"></a>
                        </#if>

                        <#if isAdminLoggedIn>
                        <a class="icon-setting" href="/admin/article/${article.oId}" title="${adminLabel}"></a>
                        </#if>
                    </div>
                </div>
                <h2 class="article-title">
                    <#if 1 == article.articleType>
                    <span class="icon-locked" title="${discussionLabel}"></span>
                    <#elseif 2 == article.articleType>
                    <span class="icon-feed" title="${cityBroadcastLabel}"></span>
                    <#elseif 3 == article.articleType>
                    <span class="icon-video" title="${thoughtLabel}"></span>
                    </#if>
                    <a href="${article.articlePermalink}" rel="bookmark">
                        ${article.articleTitleEmoj}
                    </a>
                </h2> 
                <div class="article-info">
                    <a rel="author" href="/member/${article.articleAuthorName}"
                       title="${article.articleAuthorName}"><div class="avatar" style="background-image:url('${article.articleAuthorThumbnailURL}-64.jpg?${article.articleAuthor.userUpdateTime?c}')"></div></a>
                    <div>
                        <a rel="author" href="/member/${article.articleAuthorName}" class="ft-black"
                           title="${article.articleAuthorName}"><strong>${article.articleAuthorName}</strong></a>
                        <span class="ft-gray"> 
                            <#if article.clientArticlePermalink?? && 0 < article.clientArticlePermalink?length>
                            • <a href="${article.clientArticlePermalink}" target="_blank" rel="nofollow"><span class="ft-green">${sourceLabel}</span></a>
                            </#if>
                            •
                            ${article.timeAgo}
                            •
                            ${viewLabel}
                            <#if article.articleViewCount < 1000>
                            ${article.articleViewCount}
                            <#else>
                            ${article.articleViewCntDisplayFormat}
                            </#if>
                            •
                        </span>
                        <a title="${cmtLabel}" rel="nofollow" class="ft-gray" href="#comments">
                            ${cmtLabel} ${article.articleCommentCount}
                        </a> 
                        <br/>
                        <span id="articltVia" class="ft-fade" data-ua="${article.articleUA}"></span>
                    </div>
                </div>

                <#if 3 != article.articleType>
                <div class="content-reset article-content">${article.articleContent}</div>
                <#else>
                <div id="thoughtProgress"><span class="bar"></span><span class="icon-video"></span><div data-text="" class="content-reset" id="thoughtProgressPreview"></div></div>
                <div class="content-reset article-content"></div>
                </#if>

                <div>
                    <#list article.articleTags?split(",") as articleTag>
                    <a rel="tag" class="tag" href="/tag/${articleTag?url('UTF-8')}">
                        ${articleTag}
                    </a>&nbsp;
                    </#list>
                </div>

                <div class="fn-clear">
                    <div class="share fn-right">
                        <div id="qrCode" class="fn-none"></div>
                        <span class="icon-wechat" data-type="wechat"></span>
                        <span class="icon-tencent" data-type="tencent"></span>
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
                <#if 1 == userCommentViewMode>
                <#if isLoggedIn>
                <#if discussionViewable && article.articleCommentable>
                <div class="form fn-clear comment-wrap">
                    <br/>
                    <textarea id="commentContent" placeholder="${commentEditorPlaceholderLabel}"></textarea>
                    <div class="tip" id="addCommentTip"></div>

                    <div class="fn-clear comment-submit">
                        <div class="fn-right">
                            <button class="red" onclick="Comment.add('${article.oId}', '${csrfToken}')">${replayLabel}</button>
                        </div>
                    </div>

                </div>
                <div class="grammar fn-none fn-clear">
                    ${markdwonGrammarLabel}
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
                    <div class="list" id="comments">
                        <div class="fn-clear comment-header">
                            <h2 class="fn-left">${article.articleCommentCount} ${cmtLabel}</h2>
                            <span<#if article.articleComments?size == 0> class="fn-none"</#if>>
                                <a class="icon-chevron-down fn-right" href="#bottomComment" title="${jumpToBottomCommentLabel}"></a>
                                <a class="icon-<#if 0 == userCommentViewMode>sortasc<#else>time</#if> fn-right" href="javascript:Comment.exchangeCmtSort(${userCommentViewMode})"
                                   title="<#if 0 == userCommentViewMode>${changeToLabel}${realTimeLabel}${cmtViewModeLabel}<#else>${changeToLabel}${traditionLabel}${cmtViewModeLabel}</#if>"></a>
                            </span>
                        </div>
                        <ul>
                            <#list article.articleComments as comment>
                            <li id="${comment.oId}"<#if comment.commentStatus == 1>class="shield"</#if>>
                                <#if !comment?has_next><div id="bottomComment"></div></#if>
                                <div class="fn-flex">
                                    <a rel="nofollow" href="/member/${comment.commentAuthorName}">
                                        <div class="avatar" 
                                             title="${comment.commentAuthorName}" style="background-image:url('${comment.commentAuthorThumbnailURL}-64.jpg?${comment.commenter.userUpdateTime?c}')"></div>
                                    </a>
                                    <div class="fn-flex-1 comment-content">
                                        <div class="fn-clear comment-info">
                                            <span class="fn-left">
                                                <a rel="nofollow" href="/member/${comment.commentAuthorName}"
                                                   title="${comment.commentAuthorName}">${comment.commentAuthorName}</a><span class="ft-fade ft-smaller">&nbsp;•&nbsp;${comment.timeAgo} 
                                                    <span class="cmt-via" data-ua="${comment.commentUA}"></span>
                                                </span>
                                                <#if comment.rewardedCnt gt 0>
                                                <#assign hasRewarded = isLoggedIn && comment.commentAuthorId != currentUser.oId && comment.rewarded>
                                                <#if hasRewarded>
                                                <span title="${thankedLabel}">
                                                    </#if>   
                                                    <span class="icon-heart ft-smaller <#if hasRewarded>ft-red<#else>ft-fade</#if>"></span><span
                                                        class="ft-smaller <#if hasRewarded>ft-red<#else>ft-fade</#if>" 
                                                        id='${comment.oId}RewardedCnt'> ${comment.rewardedCnt}</span> 
                                                    <#if hasRewarded>
                                                </span>
                                                </#if>
                                                </#if>
                                            </span>
                                            <span class="fn-right">
                                                <#if isLoggedIn>
                                                <#if comment.commentAuthorId != currentUser.oId>
                                                <#if !comment.rewarded>
                                                <span class='fn-none thx fn-pointer ft-smaller ft-fade' id='${comment.oId}Thx'
                                                      onclick="Comment.thank('${comment.oId}', '${csrfToken}', '${comment.commentThankLabel}', '${thankedLabel}')">${thankLabel}</span>
                                                </#if>
                                                </#if>
                                                <span class="icon-reply fn-pointer" onclick="Comment.replay('@${comment.commentAuthorName} ')"></span>
                                                </#if>
                                                <#if isAdminLoggedIn>
                                                <a class="icon-setting" href="/admin/comment/${comment.oId}" title="${adminLabel}"></a>
                                                </#if>
                                                #<i><#if 0 == userCommentViewMode>${(paginationCurrentPageNum - 1) * articleCommentsPageSize + comment_index + 1}<#else>${article.articleCommentCount - ((paginationCurrentPageNum - 1) * articleCommentsPageSize + comment_index)}</#if></i>
                                            </span>    
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
                    <@pagination url=article.articlePermalink query="m=${userCommentViewMode}" />
                </div>
                <#if 0 == userCommentViewMode>
                <#if isLoggedIn>
                <#if discussionViewable && article.articleCommentable>
                <div class="form fn-clear comment-wrap">
                    <textarea id="commentContent" placeholder="${commentEditorPlaceholderLabel}"></textarea>
                    <div class="tip" id="addCommentTip"></div>

                    <div class="fn-clear comment-submit">
                        <div class="fn-right">
                            <button class="red" onclick="Comment.add('${article.oId}', '${csrfToken}')">${replayLabel}</button>
                        </div>
                    </div>

                </div>
                <div class="grammar fn-none fn-clear">
                    ${markdwonGrammarLabel}
                </div>
                </#if>
                <#else>
                <div class="comment-login">
                    <a rel="nofollow" href="javascript:window.scrollTo(0,0);Util.showLogin();">${loginDiscussLabel}</a>
                </div>
                </#if>
                </#if>
            </div>
            <div class="side wrapper">
                <#if ADLabel!="">
                ${ADLabel}
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
                                <a class="avatar-small slogan" rel="nofollow" 
                               title="${relevantArticle.articleAuthorName}"
                               style="background-image:url('${relevantArticle.articleAuthorThumbnailURL}-64.jpg?${relevantArticle.articleAuthor.userUpdateTime?c}')"
                               href="/member/${relevantArticle.articleAuthorName}"></a>
                                <a rel="nofollow" class="title" href="${relevantArticle.articlePermalink}">${relevantArticle.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
                </#if>

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
                                <a class="avatar-small slogan" rel="nofollow"
                               href="/member/${randomArticle.articleAuthorName}"
                               title="${randomArticle.articleAuthorName}"
                               style="background-image:url('${randomArticle.articleAuthorThumbnailURL}-64.jpg?${randomArticle.articleAuthor.userUpdateTime?c}')"></a>
                                <a class="title" rel="nofollow" href="${randomArticle.articlePermalink}">${randomArticle.articleTitleEmoj}</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <div id="heatBar">
            <i class="heat" style="width:${article.articleHeat*3}px"></i>
        </div>
        <#include "footer.ftl">
        <script>
            Label.commentErrorLabel = "${commentErrorLabel}";
            Label.symphonyLabel = "${symphonyLabel}";
            Label.rewardConfirmLabel = "${rewardConfirmLabel?replace('{point}', article.articleRewardPoint)}";
            Label.articleOId = "${article.oId}";
            Label.articleTitle = "${article.articleTitle}";
            Label.articlePermalink = "${article.articlePermalink}";
            Label.recordDeniedLabel = "${recordDeniedLabel}";
            Label.recordDeviceNotFoundLabel = "${recordDeviceNotFoundLabel}";
            Label.csrfToken = "${csrfToken}";
            Label.upLabel = "${upLabel}";
            Label.downLabel = "${downLabel}";
            Label.uploadLabel = "${uploadLabel}";
            Label.userCommentViewMode = ${userCommentViewMode};
            Label.stickConfirmLabel = "${stickConfirmLabel}";
            Label.audioRecordingLabel = '${audioRecordingLabel}';
        </script>
        <script src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js"></script>
        <script src="${staticServePath}/js/lib/editor/codemirror.min.js?5120"></script>
        <script src="${staticServePath}/js/lib/editor/editor.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/highlight.js-8.6/highlight.pack.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/ws-flash/swfobject.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/ws-flash/web_socket.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/reconnecting-websocket.min.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/sound-recorder/SoundRecorder.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/jquery.qrcode.min.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/article${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/audio${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            var WEB_SOCKET_SWF_LOCATION = "${staticServePath}/js/lib/ws-flash/WebSocketMain.swf";
            // Init [Article] channel
            ArticleChannel.init("${wsScheme}://${serverHost}:${serverPort}/article-channel?articleId=${article.oId}&articleType=${article.articleType}");
            // jQuery File Upload
            Util.uploadFile({
            "type": "img",
                    "id": "fileUpload",
                    "pasteZone": $(".CodeMirror"),
                    "qiniuUploadToken": "${qiniuUploadToken}",
                    "editor": Comment.editor,
                    "uploadingLabel": "${uploadingLabel}",
                    "qiniuDomain": "${qiniuDomain}"
            });
            <#if 3 == article.articleType>
                    Article.playThought('${article.articleContent}');
            </#if>
        </script>
    </body>
</html>
