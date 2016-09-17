<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${chargePointLabel} - ${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/charge/point">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <h2 class="sub-head"><span class="ft-blue">♦</span> ${chargePointLabel}</h2>
                    <div class="content-reset">
                    ${chargePointContentLabel}
                    </div>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>