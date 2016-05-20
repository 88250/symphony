<#include "macro-head.ftl">
<#include "macro-list.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${recentArticleLabel} - ${symphonyLabel}">
        <meta name="description" content="${recentArticleLabel}"/>
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="content fn-clear">
                <div class="domains fn-clear">
                    <#list domains as domain>
                    <a href='/domain/${domain.domainURI}'>${domain.domainTitle}</a>
                    </#list>
                    <a href="/">${latestLabel}</a>
                    <a href="/hot" class="selected">${hotLabel}</a>
                    <#if isLoggedIn && "" != currentUser.userCity>
                    <a href="/city/my">${currentUser.userCity}</a>
                    </#if>
                    <a href="/timeline">${timelineLabel}</a>
                    <a href="/community">${communityGroupLabel}</a>
                </div>
                <@list listData=indexArticles/>
                <a href="/" class="ft-gray more-article">${moreRecentArticleLabel}</a>
            </div>
            <div class="side wrapper">
                <#include "side.ftl">
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
