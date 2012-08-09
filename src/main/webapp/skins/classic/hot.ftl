<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log 社区">
        <meta name="keywords" content=""/>
        <meta name="description" content=""/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear hot">
                <div>
                    <h2>Hot articles</h2>
                    <ul>
                        <#list 1..10 as i>
                        <li>
                            我是标题
                        </li>
                        </#list>
                    </ul>
                </div> 
                <div class="hot-author">
                    <div class="fn-clear" style="margin-top: 20px;">
                        <#list 1..22 as i>
                        <div class="author fn-left">
                            <img src="/images/user-thumbnail.png" />
                            <span class="fn-ellipsis">
                                <a href="/" title="VanessaLiliYuan">VanessaLiliYuan</a>
                            </span>
                        </div>
                        <#if i_index=10>
                        <div class="center">
                            <h2>Hot Author</h2>
                        </div>
                        </#if>
                        </#list>
                    </div>
                </div>
                <div class="hot-tags">
                    <h2>Hot Tags</h2>
                    <#list 1..10 as i>
                    <a href="">Tag</a>
                    </#list>
                </div>
                <div>
                    <h2>Hot Comments</h2>
                    <ul>
                        <#list 1..10 as i>
                        <li>
                            我是标题
                        </li>
                        </#list>
                    </ul>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
