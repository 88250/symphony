<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${city} - ${symphonyLabel}">
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="content fn-clear">
                <div class="domains fn-clear">
                    <#list domains as navDomain>
                    <a href="/domain/${navDomain.domainURI}">${navDomain.domainTitle}</a>
                    </#list>
                    <a href="/">${latestLabel}</a>
                    <a href="/hot">${hotLabel}</a>
                    <#if isLoggedIn && "" != currentUser.userCity>
                    <a href="/city/my" class="selected">${currentUser.userCity}</a>
                    </#if>
                    <a href="/timeline">${timelineLabel}</a>
                    <a href="/community">${communityGroupLabel}</a>
                </div>

                <#if articles?size gt 0>
                <div class="fn-clear">
                    <@list listData=articles/>
                    <@pagination url="/city/${city?url('utf-8')}"/>
                </div>
                <#else>
                <div class="content content-reset">
                    <#if !userGeoStatus>
                    ${cityArticlesTipLabel}
                    <#else>
                    <#if !cityFound>
                    ${geoInfoPlaceholderLabel}
                    </#if>
                    </#if>
                </div>
                </#if>
            </div>
            <div class="side wrapper">
                <#include "side.ftl">
            </div>
        </div>
    </div>
    <#include "footer.ftl">
</body>
</html>
