<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination-query.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${searchLabel} - ${articleLabel} - ${symphonyLabel}">
        </@head>
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <@subNav '' ''/>
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
