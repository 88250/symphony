<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${eatingSnakeLabel} - ${activityLabel} - ${symphonyLabel}">
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/activity/eating-snake">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module">
                        <h2 class="sub-head">
                            <div class="avatar-small tooltipped tooltipped-ne"
                                 aria-label="${eatingSnakeLabel}" style="background-image:url('${staticServePath}/images/activities/snak.png')"></div>
                            ${eatingSnakeLabel}
                            <span class="ft-13 ft-gray">${activityEatingSnakeTitleLabel}</span>
                        </h2>
                        <br>
                        <div class="fn-clear fn-content">
                            <button class="green fn-right" onclick="Activity.startSnake('${csrfToken}')">${gameStartLabel}</button>
                        </div>
                        <div id="yard">
                            <canvas id="snakeCanvas" height="600px" width="600px"></canvas>
                            <#--<span id="tip" class="tip-succ" style="top: 307px;">${msg}</span>-->
                        </div>
                        <div class="fn-flex snak-rank">
                            <div class="module">
                                <div class="module-header">${totalRankLabel}</div>
                                <div class="module-panel">
                                    <ul class="module-list">
                                        <#list sumUsers as user>
                                        <li>
                                            <div class="avatar-small tooltipped tooltipped-ne slogan"
                                                 aria-label="${activityDailyCheckinLabel}" style="background-image:url('${user.userAvatarURL48}')"></div>
                                            <a class="title" href="${servePath}/member/${user.userName}">${user.userName}</a>
                                            <span class="fn-right count ft-gray ft-smaller">${user.point}</span>
                                        </li>
                                        </#list>
                                    </ul>
                                </div>
                            </div>
                            <div class="module">
                                <div class="module-header">${eachRankLabel}</div>
                                <div class="module-panel">
                                    <ul class="module-list">
                                        <#list maxUsers as user>
                                        <li>
                                            <div class="avatar-small tooltipped tooltipped-ne slogan"
                                                 aria-label="${activityDailyCheckinLabel}" style="background-image:url('${user.userAvatarURL48}')"></div>
                                            <a class="title" href="${servePath}/member/${user.userName}">${user.userName}</a>
                                            <span class="fn-right count ft-gray ft-smaller">${user.point}</span>
                                        </li>
                                        </#list>
                                    </ul>
                                </div>
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
        <script src="${staticServePath}/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/eating-snake${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Label.activityStartEatingSnakeTipLabel = '${activityStartEatingSnakeTipLabel}';
            Activity.initSnake();
        </script>
    </body>
</html>
