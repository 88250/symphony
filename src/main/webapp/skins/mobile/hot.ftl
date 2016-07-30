<#include "macro-head.ftl">
<#include "macro-list.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${hotTopicLabel} - ${symphonyLabel}">
        <meta name="description" content="${recentArticleLabel}"/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="content fn-clear">
                <div class="domains fn-clear">
                    <#list domains as domain>
                    <a href="${servePath}/domain/${domain.domainURI}">${domain.domainTitle}</a>
                    </#list>
                    <a href="${servePath}/">${latestLabel}</a>
                    <a href="${servePath}/hot" class="selected">${hotLabel}</a>
                    <#if isLoggedIn && "" != currentUser.userCity>
                    <a href="${servePath}/city/my">${currentUser.userCity}</a>
                    </#if>
                    <a href="${servePath}/timeline">${timelineLabel}</a>
                    <a href="${servePath}/community">${communityGroupLabel}</a>
                </div>
                <@list listData=indexArticles/>
                <a href="${servePath}/" class="ft-gray more-article">${moreRecentArticleLabel}</a>
            </div>
            <div class="side wrapper">
                <#include "side.ftl">
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
