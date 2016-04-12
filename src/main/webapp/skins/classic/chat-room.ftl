<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${chatRoomLabel} - ${symphonyLabel}">
        <meta name="description" content="${timelineLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content">
                    <h2>${chatRoomLabel}</h2><br/>

                    <textarea id="chatRoom" rows="40" placeholder=""></textarea>
                    <textarea id="chatMsgEditor" rows="10" placeholder="reply...."></textarea>
                    <button id="sendBtn" onclick="sendMsg()">${postLabel}</button>

                    <script>
                        function sendMsg() {
                            var content = $("#chatMsgEditor").val();
                            var requestJSONObject = {
                                content: content
                            };

                            $.ajax({
                                url: "/chat-room/send",
                                type: "POST",
                                cache: false,
                                data: JSON.stringify(requestJSONObject),
                                beforeSend: function () {
                                },
                                success: function (result, textStatus) {
                                    if (result.sc) {
                                        $("#chatMsgEditor").val('');
                                    } else {
                                        alert(result.msg);
                                    }
                                },
                                complete: function (jqXHR, textStatus) {
                                }
                            });
                        }
                    </script>

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
                        // Init [ChatRoom] channel
                        ChatRoomChannel.init("${wsScheme}://${serverHost}:${serverPort}/chat-room-channel");
        </script>
    </body>
</html>
