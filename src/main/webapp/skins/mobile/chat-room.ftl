<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
<#include "macro-head.ftl">
<#include "common/sub-nav.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${chatRoomLabel} - ${symphonyLabel}">
        <meta name="description" content="${chatRoomLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/js/lib/editor/codemirror.min.css">
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <@subNav 'community' ''/>
            <div class="wrapper">
                <div class="content chat-room">
                    <div class="form">
                        <div class="reply">
                            <#if isLoggedIn>
                            <textarea id="chatContent" rows="10" placeholder="Say sth...."></textarea>
                                <div class="fn-clear comment-submit">
                                    <div class="fn-left online-cnt">${onlineVisitorCountLabel} <span id="onlineCnt"></span></div>
                                    <div class="tip fn-left" id="chatContentTip"></div>
                                    <div class="fn-right">
                                        <button class="green" onclick="ChatRoom.send()">${postLabel}</button>
                                    </div>
                                </div>
                            <#else>
                            <div class="comment-login">
                                <a rel="nofollow" href="javascript:window.scrollTo(0,0);Util.goLogin();">${loginDiscussLabel}</a>
                            </div>
                            </#if>
                        </div>
                        <br/>
                        <div class="list">
                            <ul>
                                <#list messages as msg>
                                    <li class="fn-flex">
                                        <a rel="nofollow" href="${servePath}/member/${msg.userName}">
                                            <div class="avatar tooltipped tooltipped-n"
                                                 aria-label="${msg.userName}" style="background-image:url('${msg.userAvatarURL}')"></div>
                                        </a>
                                        <div class="fn-flex-1">
                                            <div class="ft-smaller">
                                                <a rel="nofollow" href="${servePath}/member/${msg.userName}">
                                                    <span class="ft-gray">${msg.userName}</span>
                                                </a>
                                                <span class="ft-fade">
                                             • ${msg.time}
                                        </span>
                                            </div>
                                            <div class="content-reset comment">
                                                ${msg.content}
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
        <script src="${staticServePath}/js/lib/editor/codemirror.min.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/lib/highlight/highlight.pack.js"></script>
        <script src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js"></script>
        <script src="${staticServePath}/js/channel${miniPostfix}.js?${staticResourceVersion}"></script>
        <script src="${staticServePath}/js/chat-room${miniPostfix}.js?${staticResourceVersion}"></script>
        <script>
            Label.addBoldLabel = '${addBoldLabel}';
            Label.addItalicLabel = '${addItalicLabel}';
            Label.insertQuoteLabel = '${insertQuoteLabel}';
            Label.addBulletedLabel = '${addBulletedLabel}';
            Label.addNumberedListLabel = '${addNumberedListLabel}';
            Label.addLinkLabel = '${addLinkLabel}';
            Label.undoLabel = '${undoLabel}';
            Label.redoLabel = '${redoLabel}';
            Label.previewLabel = '${previewLabel}';
            Label.helpLabel = '${helpLabel}';
            Label.fullscreenLabel = '${fullscreenLabel}';
            Label.uploadFileLabel = '${uploadFileLabel}';
            Label.insertEmojiLabel = '${insertEmojiLabel}';
            ChatRoom.init();
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
