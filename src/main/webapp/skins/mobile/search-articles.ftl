<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination-query.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${searchLabel} - ${articleLabel} - ${symphonyLabel}">
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="domains fn-clear">
                        <#list domains as domain>
                        <a href='/domain/${domain.domainURI}'>${domain.domainTitle}</a>
                        </#list>
                        <a href="/">${latestLabel}</a>
                        <a href="/hot">${hotLabel}</a>
                        <#if isLoggedIn && "" != currentUser.userCity>
                        <a href="/city/my">${currentUser.userCity}</a>
                        </#if>
                        <a href="/timeline">${timelineLabel}</a>
                        <a href="/community">${communityGroupLabel}</a>
                    </div>
                    <@list listData=articles/>
                    <@pagination url="/search" query="key=${key}" />
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script>
            Util.initArticlePreview();
        </script>
    </body>
</html>
