<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${city} - ${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="module">
                        <#if articles?size gt 0>
                        <div class="fn-clear">
                            <@list listData=articles/>
                            <@pagination url="/city/${city?url('utf-8')}"/>
                        </div>
                        <#else>
                        <div class="no-list">
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
                    <#include "common/domains.ftl">
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
    </div>
    <#include "footer.ftl">
    <@listScript/>
</body>
</html>
