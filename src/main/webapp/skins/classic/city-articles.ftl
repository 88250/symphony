<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${city}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="fn-clear title">
                        <#if userGeoStatus && cityFound>
                        <h1 class="fn-inline">
                            <a title="${city?url('UTF-8')}" href="/city/${city?url('utf-8')}">${city}</a>
                        </h1>
                        </#if>
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
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
    </div>
    <#include "footer.ftl">
    <script>
        Util.initArticlePreview();
    </script>
</body>
</html>
