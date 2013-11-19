<#include "macro-head.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${article.articleTitle} - B3log ${symphonyLabel}">
        <meta name="keywords" content="${article.articleTags}"/>
        <meta name="description" content="${article.articleTitle}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css?${staticResourceVersion}" />
        <link type="text/css" rel="stylesheet" href="/js/lib/google-code-prettify/prettify.css?${staticResourceVersion}">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <div>
                        <div class="ft-small fn-clear">
                            <div class="fn-left">
                                <#list article.articleTags?split(",") as articleTag>
                                <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                                    ${articleTag}</a><#if articleTag_has_next>, </#if>
                                </#list>
                            </div>
                            <div class="fn-right">
                                <span class="ico-date">
                                    ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
                                </span>
                                <a class="ico-cmt" title="${cmtLabel}" rel="nofollow" href="#comments">${article.articleCommentCount}</a>   
                                <a class="ico-view" title="${viewLabel}" rel="nofollow" href="#">${article.articleViewCount}</a> 
                            </div>

                        </div>
                        <h2 class="article-title">
                            <a href="${article.articlePermalink}" rel="bookmark">
                                ${article.articleTitle}
                            </a>
                        </h2>
                        <div class="content-reset">
                            ${article.articleContent}
                        </div>
                        <div class="fn-clear">
                            <div class="share fn-right">
                                <span class="tencent-ico"></span>
                                <span class="sina-ico"></span>
                                <span class="twitter-ico"></span>
                                <span class="google-ico"></span>
                                <span class="clear"></span>
                            </div>    
                        </div>
                    </div>
                    <div class="fn-clear">
                        <div class="list" id="comments">
                            <h2>${article.articleCommentCount} ${cmtLabel}</h2>
                            <ul>
                                <#list article.articleComments as comment>
                                <li id="${comment.oId}">
                                    <div class="fn-clear">
                                        <div class="fn-left avatar-wrapper">
                                            <img class="avatar" 
                                                 title="${comment.commentAuthorName}" src="${comment.commentAuthorThumbnailURL}" />
                                        </div>
                                        <div class="fn-left" style="width: 612px;">
                                            <div class="fn-clear comment-info">
                                                <span class="fn-left">
                                                    <a rel="nofollow" href="/member/${comment.commentAuthorName}"
                                                       title="${comment.commentAuthorName}">${comment.commentAuthorName}</a>
                                                    &nbsp;<span class="ico-date fn-right ft-small">
                                                        ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm')} 
                                                    </span>
                                                </span>
                                                <span class="fn-right">
                                                    <#if isLoggedIn> 
                                                    <span class="ico-replay" onclick="Comment.replay('@${comment.commentAuthorName} ')"></span>
                                                    </#if>
                                                    <i>#${(paginationCurrentPageNum - 1) * articleCommentsPageSize + comment_index + 1}</i>
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
                        <@pagination url=article.articlePermalink/>
                    </div>
                    <#if isLoggedIn>
                    <div class="form fn-clear">
                        <textarea id="commentContent" rows="3"></textarea>
                        <span style="bottom: 4px; right: 75px;"></span>
                        <a href="javascript:void(0)" onclick="$('.grammar').slideToggle()">${baseGrammarLabel}</a>
                        <a target="_blank" href="http://daringfireball.net/projects/markdown/syntax">${allGrammarLabel}</a>
                        <button class="green fn-right" onclick="Comment.add('${article.oId}')">${submitLabel}</button>
                    </div>
                    <div class="grammar fn-none">
                        ${markdwonGrammarLabel}
                    </div>
                    <#else>
                    <div class="comment-login">
                        <a rel="nofollow" href="javascript:window.scrollTo(0,0);Util.showLogin();">${pleaseLoginLabel}</a>
                    </div>
                    </#if>
                </div>
                <div class="side">
                    <div class="module">
                        <div class="module-header ad">
                            <div class="fn-clear">
                                <div class="fn-left avatar-wrapper">
                                    <img class="avatar" src="${article.articleAuthorThumbnailURL}" />
                                </div>
                                <div class="fn-left">
                                    <a rel="author" href="/member/${article.articleAuthorName}" 
                                       title="${article.articleAuthorName}">${article.articleAuthorName}</a>
                                    <#if article.articleAuthorURL!="">
                                    <br/>
                                    <a target="_blank" rel="nofollow" href="${article.articleAuthorURL}">${article.articleAuthorURL}</a>
                                    </#if>
                                </div>
                            </div>
                            <div>
                                ${article.articleAuthorIntro}
                            </div>
                        </div> 
                    </div>

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
                                    <a rel="nofollow" href="${relevantArticle.articlePermalink}">${relevantArticle.articleTitle}</a>
                                    <a class="ft-small" rel="nofollow" 
                                       href="/member/${relevantArticle.articleAuthorName}">${relevantArticle.articleAuthorName}</a>
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
                                    <a rel="nofollow" href="${randomArticle.articlePermalink}">${randomArticle.articleTitle}</a>
                                    <a class="ft-small" rel="nofollow"
                                       href="/member/${randomArticle.articleAuthorName}">${randomArticle.articleAuthorName}</a>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>

                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/lib/google-code-prettify/prettify.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/article.js?${staticResourceVersion}"></script>
        <script>
            var Label = {
                commentErrorLabel: "${commentErrorLabel}",
                symphonyLabel: "${symphonyLabel}",
                articleOId: "${article.oId}",
                articleTitle: "${article.articleTitle}",
                articlePermalink: "${article.articlePermalink}"
            };
        </script>
    </body>
</html>
