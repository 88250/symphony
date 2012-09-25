<#include "macro-head.ftl">
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
                <div class="index-content">
                    <#list 1..10 as i>
                    <div>
                        <div class="fn-clear">
                            <div class="fn-left">
                                <h3>
                                    <a href="">tag</a>
                                </h3>
                                引用：12 
                                评论：14
                            </div>
                            <div class="fn-left">
                                <img class="index-user" src="https://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140" />
                            </div>
                            <div class="fn-right fn-box">
                                <#list 1..10 as i>
                                <img class="index-user" src="https://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140" />
                                </#list>
                            </div>
                        </div>
                        <ul>
                            <#list 1..5 as i>
                            <li>
                                article
                            </li>
                            </#list>
                        </ul>
                    </div>
                    </#list>
                </div>
                <div class="side">
                    <div class="index-module">
                        AD
                    </div>
                    <div class="index-module">
                        <h2>
                            Recent Post
                        </h2>
                        <ul>
                            <#list 1..10 as i>
                            <li class="fn-ellipsis">
                                <a href="">Recent PoPostPostPostPostPostPostPostPostst</a>
                            </li>
                            </#list>
                        </ul>
                    </div>
                    <div class="index-module">
                        <h2>
                            Tags
                        </h2>
                        <#list 1..10 as i>
                        <a href="">Recent Post</a>
                        </#list>
                    </div>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
