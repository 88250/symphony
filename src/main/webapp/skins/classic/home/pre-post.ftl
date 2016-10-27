<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${selectAddTypeLabel} - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/home.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper pre-post">
                <div>
                    <a href="${servePath}/post?type=0">
                        <div class="icon-article"></div>
                        ${articleLabel}
                    </a>
                    <div class="ft-fade">${addNormalArticleTipLabel}</div>
                </div>
                <div>
                    <a href="${servePath}/post?type=3">
                        <div class="icon-video"></div>
                        ${thoughtLabel}
                    </a>
                    <div class="ft-fade">
                        ${addThoughtArticleTipLabel}
                        <a href="https://hacpai.com/article/1441942422856" target="_blank">(?)</a>
                    </div>
                </div>
                <div>
                    <a href="${servePath}/post?type=1">
                        <div class="icon-locked"></div>
                        ${discussionLabel}
                    </a>
                    <div class="ft-fade">${addDiscussionArticleTipLabel}</div>
                </div>
                <div>
                    <a href="${servePath}/post?type=2">
                        <div class="icon-feed"></div>
                        ${cityBroadcastLabel}
                    </a>
                    <div class="ft-fade">${addCityArticleTipLabel} <i>${broadcastPoint}</i> ${pointLabel}</div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script>
            if ($.ua.device.type === 'mobile') {
                $('.pre-post > div:last').hide();
            }   
        </script>
    </body>
</html>
