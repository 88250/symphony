<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${gobangLabel} - ${activityLabel} - ${symphonyLabel}">
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/activity/gobang">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module article-module">
                        <h2 class="sub-head">
                            <div class="avatar-small tooltipped tooltipped-ne"
                                 aria-label="${gobangLabel}" style="background-image:url('${staticServePath}/images/activities/gobang.png')"></div>
                            ${gobangLabel}
                            <span class="ft-13 ft-gray">${activityGobangTitleLabel}</span>
                        </h2>
                        <br>
                        <div class="fn-clear fn-m10">
                            <div id="chatArea"></div>
                            <button class="green fn-right" onclick="Activity.initGobang('${wsScheme}://${serverHost}:${serverPort}${contextPath}')">${gameStartLabel}</button>
                        </div>
                        <div id="yard"><canvas id="gobangCanvas" height="600px" width="600px"></canvas></div>
                        <br>
                        <input  type="hidden" id="player"/>
                        <input  type="text" id="chatInput" style="display: none;"/>
                        <input type="submit" id="chatSubmit" value="Chat" onclick="Gobang.chatSend()" style="display: none;"/>
                    </div>
                </div>
                <div class="side">
                    <#include "../side.ftl">
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script src="${staticServePath}/js/activity${miniPostfix}.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/gobang${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Label.activityStartGobangTipLabel = '${activityStartGobangTipLabel}';
            Gobang.initCanvas('oMark', 'gobangCanvas');
            document.getElementById("gobangCanvas").addEventListener("click",Gobang.moveChess, false);
        </script>
    </body>
</html>
