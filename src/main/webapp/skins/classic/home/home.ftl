<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/home.css" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <ul class="tab fn-clear">
                        <li class="current">
                            ${articleLabel}
                        </li>
                        <li>
                            ${cmtLabel}
                        </li>
                    </ul>
                    <div>
                        <div class="article-list list">
                            <ul>
                                <#list userHomeArticles as userHomeArticle>
                                <li>
                                    <div>
                                        <h2><a href="">${userHomeArticle.articleTitle}</a></h2>
                                        <span class="ft-small">
                                            <#list userHomeArticle.articleTags?split(",") as articleTag>
                                            <a rel="tag" href="/tags/${articleTag?url('UTF-8')}">
                                                ${articleTag}</a><#if articleTag_has_next>, </#if>
                                            </#list>
                                            ${userHomeArticle.articleCreateTime}
                                        </span>
                                        <div class="count ft-small">
                                            ${viewLabel} <a href="">${userHomeArticle.articleViewCount}</a><br/>
                                            ${cmtLabel} <a href="">${userHomeArticle.articleCommentCount}</a>
                                        </div>
                                        <div class="commenters">
                                            <#list 1..10 as i>
                                            <img class="avatar-small" src="https://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140" />
                                            </#list>
                                        </div>
                                    </div>
                                </li>
                                </#list>
                            </ul>
                        </div>
                        <div class="comment-list list fn-none">
                            <ul>
                                <#list 1..10 as i>
                                <li>
                                    <div class="fn-clear">
                                        <div class="fn-left avatar">
                                            <img src="/images/user-thumbnail.png" />
                                        </div>
                                        <div class="fn-left comment-main" style="width:612px">
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
                        </div>
                    </div>
                </div>
                <div class="side">
                    <div>
                        <img class="avatar-big" src="https://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140" />
                        <div>
                            online 
                            <br/>
                            <a href="">${user.userName}</a> 
                            <br/>
                            URL: <a href="">${user.userURL}</a>
                            <br/>
                            B3log Symphony 第 ${user.userNo} 号会员
                            <br/>
                            加入于 ${user.created}
                        </div>
                        <div>
                            ${user.userIntro}
                        </div>
                    </div>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script type="text/javascript" src="/js/home.js"></script>
    </body>
</html>
