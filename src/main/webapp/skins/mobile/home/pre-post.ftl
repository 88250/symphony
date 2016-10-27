<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${selectAddTypeLabel} - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="pre-post">
                <div>
                    <a href="${servePath}/post?type=0">
                        <div class="icon-article"> ${articleLabel}</div>
                    </a>
                    <div class="ft-fade">${addNormalArticleTipLabel}</div>
                </div>
                <div>
                    <a href="${servePath}/post?type=3">
                        <div class="icon-video"> ${thoughtLabel}</div>
                    </a>
                    <div class="ft-fade">
                        ${addThoughtArticleTipLabel}
                        <a href="https://hacpai.com/article/1441942422856" target="_blank">(?)</a>
                    </div>
                </div>
                <div>
                    <a href="${servePath}/post?type=1">
                        <div class="icon-locked"> ${discussionLabel}</div>
                    </a>
                    <div class="ft-fade">${addDiscussionArticleTipLabel}</div>
                </div>
                <div>
                    <a href="${servePath}/post?type=2">
                        <div class="icon-feed"> ${cityBroadcastLabel}</div>
                    </a>
                    <div class="ft-fade">${addCityArticleTipLabel} <i>${broadcastPoint}</i> ${pointLabel}</div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script>
            if ($.ua.device.type === 'mobile') {
                $('.pre-post > div:eq(1)').hide();
            }   
        </script>
    </body>
</html>
