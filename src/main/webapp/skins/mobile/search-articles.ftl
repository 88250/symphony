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
                        <a href="${servePath}/domain/${domain.domainURI}">${domain.domainTitle}</a>
                        </#list>
                        <a href="${servePath}/">${latestLabel}</a>
                        <a href="${servePath}/hot">${hotLabel}</a>
                        <#if isLoggedIn && "" != currentUser.userCity>
                        <a href="${servePath}/city/my">${currentUser.userCity}</a>
                        </#if>
                        <a href="${servePath}/timeline">${timelineLabel}</a>
                        <a href="${servePath}/community">${communityGroupLabel}</a>
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
        <@listScript/>
    </body>
</html>
