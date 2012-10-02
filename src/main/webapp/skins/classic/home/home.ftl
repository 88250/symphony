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
            <div class="wrapper">
                <div class="fn-clear">
                    <div class="fn-left">
                        <img class="avatar-big" src="https://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140" />
                        <br/>
                        online
                    </div>
                    <div class="fn-right" style="width: 926px">
                        <div>
                            <a href="">Vanessa</a> 
                            <a href="">http://vanessa.b3log.org</a>
                            <br/>
                            V2EX 第 4867 号会员，加入于 2011-01-07 14:41:12 PM
                        </div>
                        <div>
                            intro intro intro intro introintrointrointrointrointrointrointroi
                            ntrointrointrointrointrointrointrointrointro
                        </div>
                    </div>
                </div>
                <div>
                    <ul>
                        <li>
                            articles
                        </li>
                        <li>
                            comments
                        </li>
                    </ul>
                    <div class="article-list list">
                        <ul>
                            <#list 1..10 as i>
                            <li>
                                <div class="content">
                                    <h2><a href="">title</a></h2>
                                    <span class="ft-small">
                                        <a href="">tagss</a> 
                                        2012-02-10
                                    </span>
                                    <div class="count ft-small">
                                        评论数：<a href="">123</a>
                                        访问数：<a href="">123</a>
                                    </div>
                                </div>
                                <div class="abstract">abstractabstractabstractabstractabstractabstract</div>
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
                                    <div class="fn-left comment-main" style="width: 922px">
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
        </div>
        <#include "footer.ftl">
    </body>
</html>
