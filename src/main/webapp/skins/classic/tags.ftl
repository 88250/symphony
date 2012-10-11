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
            <div class="wrapper">
                <#list 1..20 as i>
                <a href="/">tag ${i}</a>
                </#list>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
