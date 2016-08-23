<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination-query.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${searchLabel} - ${articleLabel} - ${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <@subNav '' ''/>
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
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
