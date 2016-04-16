<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination-query.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${searchLabel} - ${articleLabel} - ${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="domains fn-clear">
                        <#list domains as domain>
                        <a href='/domain/${domain.domainURI}'>${domain.domainTitle}</a>
                        <#if 10 < domain?counter>
                        <#break>
                        </#if>
                        </#list>
                        <a href="/">${hotLabel}</a>
                        <a href="/recent">${latestLabel}</a>
                        <#if isLoggedIn && "" != currentUser.userCity>
                        <a href="/city/my">${currentUser.userCity}</a>
                        </#if>
                        <a href="/timeline">${timelineLabel}</a>
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
