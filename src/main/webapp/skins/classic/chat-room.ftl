<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${communityDynamicLabel} - ${symphonyLabel}">
        <meta name="description" content="${timelineLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="stylesheet" href="${staticServePath}/js/lib/editor/codemirror.min.css?${staticResourceVersion}">
        <link rel="canonical" href="${servePath}/community">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content chat-room">
                    <div class="module article-module">
                        <div class="sub-head">
                            <h2 class="fn-inline">${communityDynamicLabel}</h2><span class="ft-gray">${communityDynamicSubLabel}</span>
                        </div>
                        <br>
                        <div class="form fn-m10">
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
                            <br>
                        </div>
                    </div>
                    <div class="list module" id="comments">
                        <ul>
                            <#list messages as msg>
                            <li>
                                <div class="fn-flex">
                                    <#if !msg.userAvatarURL?contains("user-thumbnail.png")>
                                    <a rel="nofollow" href="${servePath}/member/${msg.userName}">
                                        <div class="avatar tooltipped tooltipped-se" 
                                             aria-label="${msg.userName}" style="background-image:url('${msg.userAvatarURL}')"></div>
                                    </a>
                                    <#else>
                                    <div class="avatar tooltipped tooltipped-se" 
                                         aria-label="${msg.userName}" style="background-image:url('${msg.userAvatarURL}')"></div>
                                    </#if>
                                    <div class="fn-flex-1">
                                        <div class="fn-clear">
                                            <span class="fn-left">
                                                <#if !msg.userAvatarURL?contains("user-thumbnail.png")>
                                                <a rel="nofollow" href="${servePath}/member/${msg.userName}">${msg.userName}</a>
                                                <#else>
                                                ${msg.userName}
                                                </#if>
                                            </span>
                                        </div>
                                        <div class="content-reset comment">
                                            ${msg.content}
                                        </div>
                                    </div>
                                </div>
                            </li>
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
        <script>
            Label.uploadLabel = "${uploadLabel}";
        </script>
        <script src="${staticServePath}/js/lib/editor/codemirror.min.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/lib/highlight.js-9.6.0/highlight.pack.js"></script>
        <script src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js"></script>
        <script src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/chat-room${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            // Init [ChatRoom] channel
            ChatRoomChannel.init("${wsScheme}://${serverHost}:${serverPort}${contextPath}/chat-room-channel");
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
