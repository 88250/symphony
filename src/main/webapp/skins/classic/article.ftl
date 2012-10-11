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
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <div>
                        <div class="ft-small fn-clear">
                            <div class="fn-left">
                                ${article.articleCreateTime?string('yyyy-MM-dd HH:mm:ss')}
                            </div>
                            <div class="fn-right">
                                &nbsp;${viewLabel} <a href="/">${article.articleViewCount}</a> 
                                ${cmtLabel} <a href="/">${article.articleCommentCount}</a>   
                            </div>
                            <div class="fn-right">
                                <#list article.articleTags?split(",") as articleTag>
                                <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                                    ${articleTag}</a><#if articleTag_has_next>, </#if>
                                </#list>
                            </div>
                        </div>
                        <h2 class="article-title">
                            <a href="${article.articlePermalink}" rel="bookmark">
                                ${article.articleTitle}
                            </a>
                        </h2>
                        <div>
                            ${article.articleContent}
                        </div>
                    </div>
                    <div class="comment-list list fn-clear">
                        <h2>${article.articleCommentCount} ${cmtLabel}</h2>
                        <ul>
                            <#list article.articleComments as comment>
                            <li>
                                <div class="fn-clear">
                                    <div class="fn-left avatar">
                                        <img title="${comment.commentAuthorName}" src="${comment.commentAuthorThumbnailURL}" />
                                    </div>
                                    <div class="fn-left comment-main">
                                        <span class="fn-clear">
                                            <span class="fn-left">
                                                <a href="/${comment.commentAuthorName}" title="${comment.commentAuthorName}">${comment.commentAuthorName}</a>
                                                @ <a href="/">Daniel</a>
                                            </span>
                                            <span class="fn-right ft-small">
                                                ${comment.commentCreateTime?string('yyyy-MM-dd HH:mm:ss')}
                                            </span>    
                                        </span>
                                        <div>
                                            ${comment.commentContent}
                                        </div>
                                    </div>
                                </div>
                            </li>
                            </#list>  
                        </ul>
                        <@pagination url=article.articlePermalink/>
                    </div>
                    <div class="form fn-clear">
                        <textarea id="commentContent"></textarea>
                        <span style="right:0;top:11px;"></span>
                        <button class="green fn-right" onclick="Comment.add('${article.oId}')">${submitLabel}</button>
                    </div>

                </div>
                <div class="side">
                    <div class="index-module">
                        <div class="fn-clear">
                            <div class="fn-left avatar">
                                <img src="${article.articleAuthorThumbnailURL}" />
                            </div>
                            <div class="fn-left">
                                <a href="/${article.articleAuthorName?url('utf-8')}" title="${article.articleAuthorName}">${article.articleAuthorName}</a>
                                <#if article.articleAuthorURL!="">
                                <br/>
                                <a href="${article.articleAuthorURL}">${article.articleAuthorURL}</a>
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
                        <ul>
                            <#list 1..10 as i>
                            <li>
                                <a href="">Recent Post</a> 
                                <div class="ft-small">
                                    <span>
                                        <a href="">vanesaa</a>
                                        2011-1-1
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
                        <ul>
                            <#list 1..10 as i>
                            <li>
                                <a href="">Recent Post</a> 
                                <div class="ft-small">
                                    <span>
                                        <a href="">vanesaa</a>
                                        2011-1-1
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
        </script>
        <script type="text/javascript" src="/js/article.js"></script>
    </body>
</html>
