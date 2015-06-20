/*
 * Copyright (c) 2012-2015, b3log.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * @fileoverview Message channel via WebSocket.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 1.1.0.0, Jun 20, 2015
 */

/**
 * @description Channel
 * @static
 */
var Channel = {
    /**
     * WebSocket instance.
     * 
     * @type WebSocket
     */
    ws: undefined,
    /**
     * @description Initializes message channel
     */
    init: function (channelServer) {
        Channel.ws = new ReconnectingWebSocket(channelServer);
        Channel.ws.reconnectInterval = 10000;

        Channel.ws.onopen = function () {
        };

        Channel.ws.onmessage = function (evt) {
            var data = JSON.parse(evt.data);
            if (Label.articleOId !== data.articleId) { // It's not the current article
                return;
            }

            // Append comment
            
            var preIndex = $("#commentList li:last .icon-cmt").next().text();
            var preIndex = preIndex.indexOf("#") > -1 ? preIndex = preIndex.substring(1) : 0;

            var template = "<li class=\"fn-none\" id=\"${comment.oId}\">" +
                    "<div class=\"fn-clear\">" +
                    "<div class=\"fn-left\">" +
                    "<img class=\"avatar\"" +
                    "title=\"${comment.commentAuthorName}\" src=\"${comment.commentAuthorThumbnailURL}\" />" +
                    "</div>" +
                    "<div class=\"fn-left comment-content\">" +
                    "<div class=\"fn-clear comment-info\">" +
                    "<span class=\"fn-left\">" +
                    "<a rel=\"nofollow\" href=\"/member/${comment.commentAuthorName}\"" +
                    "title=\"${comment.commentAuthorName}\">${comment.commentAuthorName}</a>" +
                    " &nbsp;<span class=\"icon icon-date ft-small\"></span>" +
                    "<span class=\"ft-small\">&nbsp;${comment.commentCreateTime}</span>" +
                    "</span>" +
                    "<span class=\"fn-right\">" +
                    "<span class=\"icon icon-cmt\" onclick=\"Comment.replay('@${comment.commentAuthorName} ')\"></span>" +
                    " <i>#" + (parseInt(preIndex) + 1) + "</i>" +
                    "</span>" +
                    "</div>" +
                    "<div class=\"content-reset comment\">" +
                    "${comment.commentContent}" +
                    "</div>" +
                    "</div>" +
                    "</div>" +
                    "</li>";

            template = replaceAll(template, "${comment.oId}", data.commentId);
            template = replaceAll(template, "${comment.commentAuthorName}", data.commentAuthorName);
            template = replaceAll(template, "${comment.commentAuthorThumbnailURL}", data.commentAuthorThumbnailURL);
            template = replaceAll(template, "${comment.commentContent}", data.commentContent);
            template = replaceAll(template, "${comment.commentCreateTime}", data.commentCreateTime);

            $("#commentList").append(template);
            
            Article.parseLanguage();
            
            $("#" + data.commentId).fadeIn(2000);
        };

        Channel.ws.onclose = function () {
            Channel.ws.close();
        };

        Channel.ws.onerror = function (err) {
            console.log("ERROR", err)
        };
    }
};

function escapeRegExp(string) {
    return string.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1");
}

function replaceAll(string, find, replace) {
    return string.replace(new RegExp(escapeRegExp(find), 'g'), replace);
}