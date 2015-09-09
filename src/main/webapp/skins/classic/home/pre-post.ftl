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
        <div class="main">
            <div class="wrapper pre-post">
                <a href="/post?type=0">
                    <div class="icon-article"></div>
                    ${articleLabel}
                </a>
                <a href="/post?type=1">
                    <div class="icon-locked"></div>
                    ${discussionLabel}
                </a>
                <a href="/post?type=2">
                    <div class="icon-feed"></div>
                    ${cityBroadcastLabel}
                </a>
            </div>
        </div>
        <#include "../footer.ftl">
    </body>
</html>
