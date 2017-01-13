<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination-query.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${searchLabel} - ${articleLabel} - ${symphonyLabel}">
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <#if 0 == articles?size>
                        <div class="module article-module ft-center">
                        ${searchEmptyLabel}
                        </div>
                    <#else>
                        <div class="module">
                        <@list listData=articles/>
                        <@pagination url="${servePath}/search" query="key=${key}" />
                        </div>
                    </#if>
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
