<#include "macro-head.ftl">
<#include "macro-list.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symphonyLabel}${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="wrapper">
            <div class="fn-flex ads">
                <div class="fn-flex-1 ad">
                    <span class="title"></span>
                    <img src="${staticServePath}/skins/hacpai/static/images/ad1.png"/>
                </div>
                <div>
                    <div class="fn-clear">
                        <div></div>
                        <div></div>
                    </div>
                    <div></div>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
    </body>
</html>
