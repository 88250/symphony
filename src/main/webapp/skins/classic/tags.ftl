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
            <div class="wrapper fn-clear">
                <div class="content">
                    <#list trendTags as tag>
                    <a href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                    </#list>
                    <#list coldTags as tag>
                    <a href="/tags/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                    </#list>
                </div>  
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
