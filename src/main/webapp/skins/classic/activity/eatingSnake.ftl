<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${eatingSnakeLabel} - ${activityLabel} - ${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/activity/eatingSnake">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content activity">
                ${activityEatingSnakeTitleLabel}
                            
                    <div class="content-reset">
                    <div class="fn-right">
                                <button class="green" onclick="Activity.startSnake()">${gameStartLabel}</button>
                            </div>
                       <div id="page">
        <div id="yard"><canvas id="snakeCanvas" height="600px" width="600px"></canvas></div>
        <div id="help">
            <div id="mark">得分：<span id="mark_con"></span></div>
            
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
        <script type="text/javascript" src="${staticServePath}/js/eatingSnake${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
                                    Activity.initSnake();
        </script>
    </body>
</html>