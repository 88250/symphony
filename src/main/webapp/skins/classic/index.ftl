<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="B3log ${symphonyLabel} - ${visionLabel}">
        <meta name="keywords" content="${visionLabel},B3log ${symphonyLabel}"/>
        <meta name="description" content="B3log ${symphonyLabel}${b3logDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="/css/index.css" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper fn-clear">
                <div class="content">
                    <@list listData=latestCmtArticles/>
                    <@pagination url=""/>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
