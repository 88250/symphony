<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${communityDynamicLabel} - ${symphonyLabel}">
        <meta name="description" content="${timelineLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/js/lib/editor/codemirror.min.css">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content chat-room">
                    <div class="content-reset">
                        <h1>${communityDynamicLabel}</h1>
                        <i class="ft-gray">${communityDynamicSubLabel}</i>
                    </div>
                    <!--
                    <div class="fn-clear">
                        <span class="fn-right ft-smaller online-cnt">
                            <span class="ft-red" id="onlineCnt">${onlineChatCnt}</span>
                            <span class="ft-gray">${onlineLabel}</span>
                        </span>
                    </div>
                    -->
                    <div class="form">
                        <div class="reply">
                            <#if isLoggedIn>
                            <textarea id="chatContent" rows="10" placeholder="Say sth...."></textarea>
                            <div class="tip" id="chatContentTip"></div>
                            <div class="fn-clear comment-submit">
                                <div class="fn-right">
                                    <button class="red" onclick="ChatRoom.send()">${postLabel}</button>
                                </div>
                            </div>
                            <#else>
                            <div class="comment-login">
                                <a rel="nofollow" href="javascript:window.scrollTo(0,0);Util.showLogin();">${loginDiscussLabel}</a>
                            </div>
                            </#if>
                        </div>
                        <br/>
                        <div class="list">
                            <ul>
                                <#list messages as msg>
                                <li>
                                    <div class="fn-flex">
                                        <a rel="nofollow" href="/member/${msg.userName}">
                                            <div class="avatar" 
                                                 title="${msg.userName}" style="background-image:url('${msg.userAvatarURL}-64.jpg')"></div>
                                        </a>
                                        <div class="fn-flex-1">
                                            <div class="fn-clear">
                                                <span class="fn-left">
                                                    <a rel="nofollow" href="/member/${msg.userName}"
                                                       title="${msg.userName}">${msg.userName}</a>
                                                </span>
                                            </div>
                                            <div class="content-reset">
                                                ${msg.content}
                                            </div>
                                        </div>
                                    </div>
                                </li>
                                </#list>  
                            </ul>
                        </div>
                    </div>
                </div>
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script>
            Label.uploadLabel = "${uploadLabel}";
        </script>
        <script type="text/javascript" src="${staticServePath}/js/lib/reconnecting-websocket.min.js"></script>
        <script src="${staticServePath}/js/lib/editor/codemirror.min.js?5120"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/highlight.js-8.6/highlight.pack.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js"></script>
        <script type="text/javascript" src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/chat-room${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            // Init [ChatRoom] channel
            ChatRoomChannel.init("${wsScheme}://${serverHost}:${serverPort}/chat-room-channel");
            var chatRoomMsgCnt = ${chatRoomMsgCnt};
            Util.uploadFile({
            "type": "img",
                    "id": "fileUpload",
                    "pasteZone": $(".CodeMirror"),
                    "editor": ChatRoom.editor,
                    "qiniuUploadToken": "${qiniuUploadToken}",
                    "uploadingLabel": "${uploadingLabel}",
                    "qiniuDomain": "${qiniuDomain}",
                    "imgMaxSize": ${imgMaxSize?c},
                    "fileMaxSize": ${fileMaxSize?c}
            });
        </script>
    </body>
</html>
