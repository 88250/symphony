<#include "macro-head.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${article.articleTitle} - B3log ${symphonyLabel}">
        <meta name="keywords" content="${article.articleTags}"/>
        <meta name="description" content="${article.articleTitle}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css" />
        <link type="text/css" rel="stylesheet" href="/js/lib/google-code-prettify/prettify.css">
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
                                &nbsp;${viewLabel} <a rel="nofollow" href="${article.articlePermalink}">${article.articleViewCount}</a> 
                                ${cmtLabel} <a rel="nofollow" href="${article.articlePermalink}#comments">${article.articleCommentCount}</a>   
                            </div>
                            <div class="fn-right">
                                ${article.articleCreateTime?string('yyyy-MM-dd HH:mm')}
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
                                                    <span class="ico-replay" onclick="Comment.replay('@${comment.commentAuthorName}')"></span>
                                                    </#if>
                                                    <i>#${(paginationCurrentPageNum - 1) * articleCommentsPageSize + comment_index + 1}</i>
                                                </span>    
                                            </div>
                                            <div class="content-reset">
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
                        <span style="right:0;top:11px;"></span>
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
                    <div class="index-module">
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
                    <div class="index-module">
                        <h2>
                            ${relativeArticleLabel}
                        </h2>
                        <ul class="index-module-list module-line">
                            <#list sideRelevantArticles as relevantArticle>
                            <li<#if !relevantArticle_has_next> class="last"</#if>>
                                <a rel="nofollow" href="${relevantArticle.articlePermalink}">${relevantArticle.articleTitle}</a>
                                <div class="ft-small">
                                    <span>
                                        <a rel="nofollow" 
                                           href="/member/${relevantArticle.articleAuthorName}">${relevantArticle.articleAuthorName}</a>
                                        <span class="ico-date"> ${relevantArticle.articleCreateTime?string('yyyy-MM-dd')}</span>
                                    </span>
                                </div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                    <div class="index-module">
                        <h2>
                            ${randomArticleLabel}
                        </h2>
                        <ul class="index-module-list module-line">
                            <#list sideRandomArticles as randomArticle>
                            <li<#if !randomArticle_has_next> class="last"</#if>>
                                <a rel="nofollow" href="${randomArticle.articlePermalink}">${randomArticle.articleTitle}</a>
                                <div class="ft-small">
                                    <span>
                                        <a rel="nofollow"
                                           href="/member/${randomArticle.articleAuthorName}">${randomArticle.articleAuthorName}</a>
                                        <span class="ico-date"> ${randomArticle.articleCreateTime?string('yyyy-MM-dd')}</span>
                                    </span>
                                </div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script>
            Label.commentErrorLabel = "${commentErrorLabel}";
            Label.symphonyLabel = "${symphonyLabel}";
            Label.articleOId = "${article.oId}";
            Label.articleTitle = "${article.articleTitle}";
            Label.articlePermalink = "${article.articlePermalink}"
        </script>
        <script type="text/javascript" src="/js/lib/google-code-prettify/prettify.js"></script>
        <script type="text/javascript" src="/js/lib/jquery/jquery.linkify-1.0-min.js"></script>
        <script type="text/javascript" src="/js/article.js"></script>
    </body>
</html>
