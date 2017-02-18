<#include "macro-head.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${article.articleTitle} - ${symphonyLabel}">
        <meta name="description" content="${article.articleTitle}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/js/lib/highlight.js-9.6.0/styles/github.css">
        <link rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
        <link rel="stylesheet" href="${staticServePath}/js/lib/codemirror-5.3/codemirror.css" />
        <link rel="stylesheet" href="${staticServePath}/js/lib/codemirror-5.3/addon/hint/show-hint.css" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div>
                        <div class="ft-small fn-clear article-info">
                            <div class="fn-left">
                                <span class="icon icon-tags"></span>
                                <#list article.articleTags?split(",") as articleTag>
                                <a rel="tag" href="/tag/${articleTag?url('UTF-8')}">
                                    ${articleTag}</a><#if articleTag_has_next>, </#if>
                                </#list>
                            </div>
                            <br class="responsive-show">
                            <div class="fn-right">
                                <span class="icon icon-date"></span>
                                ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')} &nbsp;
                                <a title="${cmtLabel}" rel="nofollow" href="#comments">
                                    <span class="icon icon-cmts"></span>
                                    ${article.articleCommentCount}
                                </a> &nbsp;
                                <a title="${viewLabel}" rel="nofollow" href="#"> 
                                    <span class="icon icon-view"></span>
                                    <#if article.articleViewCount < 1000>
                                    ${article.articleViewCount}
                                    <#else>
                                    ${article.articleViewCntDisplayFormat}
                                    </#if>
                                </a>
                            </div>
                        </div>
                        <div class="article-title fn-flex">
                            <h2 class="fn-flex-1">
                                <a rel="author" href="/member/${article.articleAuthorName}" class="ft-small"
                                   title="${article.articleAuthorName}">
                                    <img class="avatar-small" src="${article.articleAuthorThumbnailURL}" />
                                </a> &nbsp;
                                <a href="${servePath}${article.articlePermalink}" rel="bookmark">
                                    ${article.articleTitleEmoj}
                                </a> &nbsp;
                            </h2> 
                            <span>
                                <#if isLoggedIn> 
                                <#if isFollowing>
                                <button class="red small" onclick="Util.unfollow(this, '${article.oId}', 'article')"> 
                                    ${uncollectLabel}
                                </button>
                                <#else>
                                <button class="green small" onclick="Util.follow(this, '${article.oId}', 'article')"> 
                                    ${collectLabel}
                                </button>
                                </#if>
                                &nbsp;
                                </#if>

                                <#if article.isMyArticle>
                                <a href="/update?id=${article.oId}" title="${editLabel}" class="icon icon-edit"></a>
                                &nbsp;
                                </#if>
                                <#if isAdminLoggedIn>
                                <a class="icon icon-setting" href="/admin/article/${article.oId}" title="${adminLabel}"></a>
                                </#if>
                            </span>
                        </div>
                        <div class="content-reset article-content">
                            ${article.articleContent}
                        </div>
                        <div class="fn-clear">
                            <div class="share fn-right">
                                <span class="icon icon-tencent" data-type="tencent"></span>
                                <span class="icon icon-weibo" data-type="weibo"></span>
                                <span class="icon icon-twitter" data-type="twitter"></span>
                                <span class="icon icon-google" data-type="google"></span>
                            </div>
                        </div>
                    </div>
                    <#if 0 < article.articleRewardPoint>
                    <div id="articleRewardContent"<#if !article.rewarded> class="reward"</#if>>
                         <#if !article.rewarded>
                         <span>
                            ${rewardTipLabel?replace("{articleId}", article.oId)?replace("{point}", article.articleRewardPoint)}
                        </span>
                        <#else>
                        ${article.articleRewardContent}
                        </#if>
                    </div>
                    </#if>
                    <#if isLoggedIn>
                    <#if discussionViewable && article.articleCommentable>
                    <div class="form fn-clear comment-wrap">
                        <form class="fn-none" id="fileupload" method="POST" enctype="multipart/form-data">
                            <input type="file" name="file" accept="image/*">
                        </form>
                        <textarea id="commentContent" placeholder="${commentEditorPlaceholderLabel}"></textarea>
                        <span style="bottom: 4px; right: 137px;"></span>
                        Markdown
                        <a href="javascript:void(0)" onclick="$('.grammar').slideToggle()">${baseGrammarLabel}</a>
                        <a target="_blank" href="http://daringfireball.net/projects/markdown/syntax">${allGrammarLabel}</a>
                        |
                        <a target="_blank" href="${servePath}/emoji/index.html">Emoji</a>
                        <div class="fn-right">
                            <button class="green" onclick="Comment.preview()">${previewLabel}</button> &nbsp; &nbsp; 
                            <button class="red" onclick="Comment.add('${article.oId}', '${csrfToken}')">${submitLabel}</button>
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
                    <div class="fn-clear">
                        <div class="list" id="comments">
                            <h2>${article.articleCommentCount} ${cmtLabel}</h2>
                            <ul>
                                <#list article.articleComments as comment>
                                    <#include 'common/comment.ftl' />
                                </#list>  
                            </ul>
                        </div>
                        <@pagination url=article.articlePermalink/>
                    </div>
                </div>
                <div class="side">

                    <#include 'common/person-info.ftl'/>

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
                                       style="background-image:url('${relevantArticle.articleAuthorThumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80')"
                                       href="${servePath}/member/${relevantArticle.articleAuthorName}"></a>
                                    <a rel="nofollow" class="title" href="${servePath}${relevantArticle.articlePermalink}">${relevantArticle.articleTitleEmoj}</a>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>

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
                                       href="${servePath}/member/${randomArticle.articleAuthorName}"
                                       title="${randomArticle.articleAuthorName}"
                                       style="background-image:url('${randomArticle.articleAuthorThumbnailURL}?imageView2/1/w/64/h/64/interlace/0/q/80')"></a>
                                    <a class="title" rel="nofollow" href="${servePath}${randomArticle.articlePermalink}">${randomArticle.articleTitle}</a>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <div id="preview" class="content-reset"></div>
        <script>
                    Label.commentErrorLabel = "${commentErrorLabel}";
                    Label.symphonyLabel = "${symphonyLabel}";
                    Label.rewardConfirmLabel = "${rewardConfirmLabel?replace("{point}", article.articleRewardPoint)}"
                    Label.articleOId = "${article.oId}";
                    Label.articleTitle = "${article.articleTitle}";
                    Label.articlePermalink = "${servePath}${article.articlePermalink}";
                    Label.csrfToken = "${csrfToken}";                            
        </script>
        <script src="${staticServePath}/js/lib/jquery/jquery.bowknot.min.js"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/codemirror.js"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/mode/markdown/markdown.js"></script>
        <script src="${staticServePath}/js/lib/codemirror-5.3/addon/display/placeholder.js"></script>
        <script src="${staticServePath}/js/overwrite/codemirror/addon/hint/show-hint.js"></script>
        <script src="${staticServePath}/js/lib/highlight.js-9.6.0/highlight.pack.js"></script>
        <script src="${staticServePath}/js/lib/ws-flash/swfobject.js"></script>
        <script src="${staticServePath}/js/lib/ws-flash/web_socket.js"></script>
        <script src="${staticServePath}/js/lib/reconnecting-websocket.min.js"></script>
        <script src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/vendor/jquery.ui.widget.js"></script>
        <script src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.iframe-transport.js"></script>
        <script src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.js"></script>
        <script src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-process.js"></script>
        <script src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-validate.js"></script>
        <script src="${staticServePath}/js/article${miniPostfix}.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
                    WEB_SOCKET_SWF_LOCATION = "${staticServePath}/js/lib/ws-flash/WebSocketMain.swf";
                    // Init [Article] channel
                    ArticleChannel.init("${wsScheme}://${serverHost}:${serverPort}/article-channel?articleId=${article.oId}&articleType=${article.articleType}");
                    // jQuery File Upload
                    Util.uploadFile({
                    "id": "fileupload",
                            "pasteZone": $(".CodeMirror"),
                            "qiniuUploadToken": "${qiniuUploadToken}",
                            "editor": Comment.editor,
                            "uploadingLabel": "${uploadingLabel}",
                            "qiniuDomain": "${qiniuDomain}"
                    });
        </script>
    </body>
</html>
