<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${eatingSnakeLabel} - ${activityLabel} - ${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/activity/eating-snake">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <h2 class="sub-head">
                        <div class="avatar-small tooltipped tooltipped-ne"
                             aria-label="${eatingSnakeLabel}" style="background-image:url('${staticServePath}/images/activities/snak.png')"></div>
                       ${activityEatingSnakeTitleLabel}
                    </h2>

                    <div class="fn-clear">
                        <div class="fn-right">
                            <button class="green" onclick="Activity.startSnake()">${gameStartLabel}</button>
                        </div>
                        <div id="page">
                            <div id="yard"><canvas id="snakeCanvas" height="600px" width="600px"></canvas></div>
                            <div id="help">
                                <div id="mark">${scoreLabel}${colonLabel}<span id="mark_con"></span></div>
                            </div>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/eating-snake${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
                                Activity.initSnake();
        </script>
    </body>
</html>