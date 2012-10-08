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
                        <li>
                            <a href="/${user.userName}">${articleLabel}</a>
                        </li>
                        <li class="current">
                            <a href="/comments/${user.userName}">${cmtLabel}</a>
                        </li>
                    </ul>
                    <div>
                        <div class="comment-list list">
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
                    <#include "home-side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
