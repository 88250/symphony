<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${city} - ${symphonyLabel}">
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <@subNav 'city/my' ''/>
            <div class="content fn-clear">
                <#if articles?size gt 0>
                <div class="fn-clear">
                    <@list listData=articles/>
                    <@pagination url="${servePath}/city/${city?url('utf-8')}"/>
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
    <@listScript/>
</body>
</html>
