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
                    <div class="article-list list">
                        <ul>
                            <#list latestCmtArticles as latestCmtArticle>
                            <li>
                                <div>
                                    <div class="fn-clear">
                                        <img class="avatar fn-left" src="https://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140" />
                                        <div class="fn-left" style="width: 550px">
                                            <h2><a href="/article/ss">${latestCmtArticle.articleTitle}</a></h2>
                                            <span class="ft-small">
                                                <a href="">${latestCmtArticle.articleTags}</a> 
                                                2012-02-10
                                            </span>
                                        </div>
                                    </div>
                                    <div class="count ft-small">
                                        评论<a href="">123</a><br/>
                                        访问<a href="">123</a>
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
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <@footer/>
    </body>
</html>
