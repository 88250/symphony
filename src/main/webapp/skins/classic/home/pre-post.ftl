<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${selectAddTypeLabel} - ${symphonyLabel}">
        <meta name="robots" content="none" />
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/home.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main pre-post-wrap">
            <div class="wrapper pre-post">
                <div>
                    <a href="${servePath}/post?type=0">
                        <div class="icon-article"></div>
                        ${articleLabel}
                    </a>
                    <div class="ft-gray">${addNormalArticleTipLabel}</div>
                </div>
                <div>
                    <a href="${servePath}/post?type=3">
                        <div class="icon-video"></div>
                        ${thoughtLabel}
                    </a>
                    <div class="ft-gray">
                        ${addThoughtArticleTipLabel}
                        <a href="https://hacpai.com/article/1441942422856" target="_blank">(?)</a>
                    </div>
                </div>
                <div>
                    <a href="${servePath}/post?type=1">
                        <div class="icon-locked"></div>
                        ${discussionLabel}
                    </a>
                    <div class="ft-gray">${addDiscussionArticleTipLabel}</div>
                </div>
                <div>
                    <a href="${servePath}/post?type=2">
                        <div class="icon-feed"></div>
                        ${cityBroadcastLabel}
                    </a>
                    <div class="ft-gray">${addCityArticleTipLabel} <i>${broadcastPoint}</i> ${pointLabel}</div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script>
            (function () {
                var h = $(window).height() - $('.nav').outerHeight() - $('.footer').outerHeight();
                if (h > 451) {
                    $('.main').outerHeight(h).css({
                        display: 'flex',
                        'align-items': 'center'
                    });
                }
            })();
        </script>
    </body>
</html>
