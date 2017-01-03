<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${userName} - Block - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/home.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper block">
                <h2 class="sub-head">${userName} - Block</h2>
                ${userBlockLabel}
            </div>
        </div>
    <#include "../footer.ftl">
</body>
</html>
