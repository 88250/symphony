<#include "macro-head.ftl">
<#include "macro-footer.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
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
                    <div class="comment-list list">
                        <h2>10 comments</h2>
                        <ul>
                            <#list 1..5 as i>
                            <li>
                                <div class="fn-clear">
                                    <div class="fn-left avatar">
                                        <img src="/images/user-thumbnail.png" />
                                    </div>
                                    <div class="fn-left comment-main">
                                        <span class="fn-clear">
                                            <span class="fn-left">
                                                <a href="/" title="VanessaLiliYuan">VanessaLiliYuan</a>
                                                @ <a href="/">Daniel</a>
                                            </span>
                                            <span class="fn-right ft-small">
                                                2012-01-21
                                            </span>    
                                        </span>
                                        <div>
                                            我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                                            我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                                            我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要我是摘要
                                        </div>
                                    </div>
                                </div>
                            </li>
                            </#list>  
                        </ul>
                        <div style="text-align: center">
                            <a href="/"><<</a>
                            <a href="/">1</a>
                            <a href="/">2</a>
                            <a href="/">3</a>
                            <a href="/">4</a>
                            <a href="/">...</a>
                            <a href="/">>></a>
                        </div>
                    </div>
                    <div class="form fn-clear">
                        <textarea></textarea>
                        <button class="green fn-right">submit</button>
                    </div>

                </div>
                <div class="side">
                    <div class="index-module">
                        <div class="fn-clear">
                            <div class="fn-left avatar">
                                <img src="${article.articleAuthorThumbnailURL}" />
                            </div>
                            <div class="fn-left">
                                <a href="/home/{article.userName?url('utf-8')}" title="{article.userName}">{article.userName}</a><br/>
                                <a href="{article.userURL}">{article.userURL}</a>
                            </div>
                        </div>
                        <div>
                            {article.userIntro}
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
        <@footer/>
    </body>
</html>
