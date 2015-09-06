<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <h2>${timelineLabel}</h2>
                    <#if timelines?size <= 0>
                    <div id="emptyTimeline">${emptyTimelineLabel}</div>
                    </#if>
                    <div class="list">
                        <ul id="ul">
                            <#list timelines as timeline>
                            <li>${timeline.content}</li>
                            </#list>
                        </ul>
                    </div>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">

        <script type="text/javascript" src="${staticServePath}/js/lib/ws-flash/swfobject.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/ws-flash/web_socket.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/reconnecting-websocket.min.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            WEB_SOCKET_SWF_LOCATION = "${staticServePath}/js/lib/ws-flash/WebSocketMain.swf";

            // Init [Timeline] channel
            TimelineChannel.init("ws://${serverHost}:${serverPort}/timeline-channel");
            
            var timelineCnt = ${timelineCnt};
        </script>
    </body>
</html>
