<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel} - ${selectAddTypeLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
            <div class="wrapper post">
                <a href="/post?type=0">${articleLabel}</a>
                <a href="/post?type=1">${discussionLabel}</a>
                <a href="/post?type=2">${cityBroadcastLabel}</a>
            </div>
        <div id="preview" class="content-reset"></div>
        <#include "../footer.ftl">
    </body>
</html>
