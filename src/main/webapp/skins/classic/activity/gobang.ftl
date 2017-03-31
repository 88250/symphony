<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${gobangLabel} - ${activityLabel} - ${symphonyLabel}">
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/activity/gobang">
    </head>
    <body onbeforeunload="closing();">
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <div class="module">
                        <h2 class="sub-head">
                            <div class="avatar-small tooltipped tooltipped-ne"
                                 aria-label="${gobangLabel}" style="background-image:url('${staticServePath}/images/activities/gobang.png')"></div>
                            ${gobangLabel}
                            <span class="ft-13 ft-gray">${activityGobangTitleLabel}</span>
                        </h2>
                        <div class="fn-content">
                            <div class="fn-clear">
                                <input  type="text" id="chatInput" class="fn-none"/>
                                <button class="green fn-right" id="chatSubmit" value="Chat" onclick="Gobang.chatSend()" style="display: none;">发送消息</button>
                                <#--<button class="green fn-right" id="quitSubmit" value="Quit" onclick="Gobang.quit()" style="display: none;">放弃游戏</button>-->
                                <button class="green fn-right" id="gameStart" onclick="Activity.initGobang('${wsScheme}://${serverHost}:${serverPort}${contextPath}')">${gameStartLabel}</button>
                            </div>
                            <div id="yard">
                                <canvas id="gobangCanvas" height="600px" width="600px"></canvas>
                                <span id="tip" class="tip-succ" style="top: 307px;">${msg}</span>
                                <div id="chatArea" style="text-align: center">
                                    <textarea rows="10" cols="100"></textarea>
                                </div>
                            </div>

                            <input  type="hidden" id="player"/>
                            <input  type="hidden" id="playerName"/>
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
        <script>
            Label.activityStartGobangTipLabel = '${activityStartGobangTipLabel}';
            Gobang.initCanvas('oMark', 'gobangCanvas');
            document.getElementById("gobangCanvas").addEventListener("click",Gobang.moveChess, false);
//            window.onunload=function(){
//                alert("bye");
//            }
//            function closing(){
//                alert('hello');
//            }
        </script>
    </body>
</html>
