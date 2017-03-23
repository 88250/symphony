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
                                 aria-label="${gobangLabel}" style="background-image:url('${staticServePath}/images/activities/snak.png')"></div>
                            ${gobangLabel}
                            <span class="ft-13 ft-gray">${activityGobangTitleLabel}</span>
                        </h2>
                        <br>
                        <div class="fn-clear fn-m10">
                            <button class="green fn-right" onclick="Activity.initGobang('${csrfToken}')">${gameStartLabel}</button>
                        </div>
                        <div id="yard"><canvas id="gobangCanvas" height="600px" width="600px"></canvas></div>
                        <input  type="hidden" id="player"/>
                        <input  type="text" id="chatInput" style="display: none;"/>
                        <input type="submit" id="chatSubmit" value="发送" onclick="Gobang.chatSend()" style="display: none;"/>
                        <div id="chatArea">
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
        <script src="${staticServePath}/js/gobang${miniPostfix}.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/lib/reconnecting-websocket.min.js"></script>
        <script src="${staticServePath}/js/lib/jquery/jquery.min.js"></script>
        <script>
            Label.activityStartGobnagTipLabel = '${activityStartGobangTipLabel}';
            Gobang.initCanvas('oMark', 'gobangCanvas');
            document.getElementById("gobangCanvas").addEventListener("click",Gobang.moveChess, false);
        </script>
    </body>
</html>
