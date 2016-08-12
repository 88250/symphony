<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${hotLabel} - ${symphonyLabel}">
        <meta name="description" content="${recentArticleLabel}"/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <@subNav 'hot' ''/>
            <div class="content fn-clear">
                <@list listData=indexArticles/>
                <a href="${servePath}/recent" class="ft-gray more-article">${moreRecentArticleLabel}</a>
            </div>
            <div class="side wrapper">
                <#include "side.ftl">
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
