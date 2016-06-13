/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
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
 * @version 1.7.7.7, Jun 13, 2016
 */

/**
 * @description Article channel.
 * @static
 */
var ArticleChannel = {
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
        ArticleChannel.ws = new ReconnectingWebSocket(channelServer);
        ArticleChannel.ws.reconnectInterval = 10000;

        ArticleChannel.ws.onopen = function () {
            setInterval(function () {
                ArticleChannel.ws.send('-hb-');
            }, 1000 * 60 * 3);
        };

        ArticleChannel.ws.onmessage = function (evt) {
            var data = JSON.parse(evt.data);
            // console.log(data);

            if (Label.articleOId !== data.articleId) { // It's not the current article
                return;
            }

            switch (data.type) {
                case "comment":
                    if (0 === Label.userCommentViewMode) { // tranditional view mode
                        return;
                    }

                    $("#comments .comment-header h2").text((parseInt($("#comments .comment-header h2").text()) + 1) + ' ' + Label.cmtLabel);
                    var bottomCmt = '';
                    if ($('#comments > ul li').length === 0) {
                        bottomCmt = '<div id="bottomComment"></div>';
                    }
                    var UAName = Util.getDeviceByUa(data.commentUA);
                    if (UAName !== '') {
                        UAName = ' <span class="cmt-via">via ' + UAName + '</span>';
                    }
                    // Append comment
                    var template = "<li class=\"fn-none\" id=\"${comment.oId}\">" + bottomCmt +
                            "<div class=\"fn-flex\">" +
                            "<a rel=\"nofollow\" href=\"/member/${comment.commentAuthorName}\">" +
                            "<img class=\"avatar\"" +
                            "title=\"${comment.commentAuthorName}\" src=\"${comment.commentAuthorThumbnailURL}-64.jpg?${comment.thumbnailUpdateTime}\" />" +
                            "</a>" +
                            "<div class=\"fn-flex-1 comment-content\">" +
                            "<div class=\"fn-clear comment-info\">" +
                            "<span class=\"fn-left\">" +
                            "<a rel=\"nofollow\" href=\"/member/${comment.commentAuthorName}\"" +
                            "title=\"${comment.commentAuthorName}\">${comment.commentAuthorName}</a>" +
                            "<span class=\"ft-fade ft-smaller\">&nbsp;•&nbsp;${comment.timeAgo}" + UAName + "</span>" +
                            "</span>" +
                            "<span class=\"fn-right\">" +
                            "<span class='fn-none thx fn-pointer ft-smaller ft-fade' id='${comment.oId}Thx'" +
                            "   onclick=\"Comment.thank('${comment.oId}', '" + Label.csrfToken + "', '${comment.commentThankLabel}', '${comment.thankedLabel}')\">${comment.thankLabel}</span> " +
                            "<span class=\"icon-reply fn-pointer\" onclick=\"Comment.replay('@${comment.commentAuthorName} ')\"></span> " +
                            "#<i>" + parseInt($("#comments .comment-header h2").text()) + "</i>" +
                            "</span>    " +
                            "</div>" +
                            "<div class=\"content-reset comment\">" +
                            "${comment.commentContent}" +
                            "</div>" +
                            "</div>" +
                            "</div>" +
                            "</li>";

                    var replaceAll = function (string, find, replace) {
                        return string.replace(new RegExp(find.replace(/([.*+?^=!:${}()|\[\]\/\\])/g, "\\$1"), 'g'), replace);
                    };
                    
                    template = replaceAll(template, "${comment.oId}", data.commentId);
                    template = replaceAll(template, "${comment.commentAuthorName}", data.commentAuthorName);
                    template = replaceAll(template, "${comment.commentAuthorThumbnailURL}", data.commentAuthorThumbnailURL);
                    template = replaceAll(template, "${comment.thumbnailUpdateTime}", data.thumbnailUpdateTime);
                    template = replaceAll(template, "${comment.commentContent}", data.commentContent);
                    template = replaceAll(template, "${comment.commentCreateTime}", data.commentCreateTime);
                    template = replaceAll(template, "${comment.timeAgo}", data.timeAgo);
                    template = replaceAll(template, "${comment.thankLabel}", data.thankLabel);
                    template = replaceAll(template, "${comment.thankedLabel}", data.thankedLabel);
                    template = replaceAll(template, "${comment.commentThankLabel}", data.commentThankLabel);

                    $("#comments > ul").prepend(template);

                    Article.parseLanguage();

                    $("#" + data.commentId).fadeIn(2000);

                    break;
                case "articleHeat":
                    var $heatBar = $("#heatBar"),
                            $heat = $(".heat");

                    if (data.operation === "+") {
                        $heatBar.append('<i class="point"></i>');
                        setTimeout(function () {
                            $heat.width($(".heat").width() + 1 * 3);
                            $heatBar.find(".point").remove();
                        }, 2000);
                    } else {
                        $heat.width($(".heat").width() - 1 * 3);
                        $heatBar.append('<i class="point-remove"></i>');
                        setTimeout(function () {
                            $heatBar.find(".point-remove").remove();
                        }, 2000);
                    }

                    break;
                default:
                    console.error("Wrong data [type=" + data.type + "]");
            }


        };

        ArticleChannel.ws.onclose = function () {
        };

        ArticleChannel.ws.onerror = function (err) {
            console.log(err);
        };
    }
};

/**
 * @description Article list channel.
 * @static
 */
var ArticleListChannel = {
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
        ArticleListChannel.ws = new ReconnectingWebSocket(channelServer);
        ArticleListChannel.ws.reconnectInterval = 10000;

        ArticleListChannel.ws.onopen = function () {
            setInterval(function () {
                ArticleListChannel.ws.send('-hb-');
            }, 1000 * 60 * 3);
        };

        ArticleListChannel.ws.onmessage = function (evt) {
            var data = JSON.parse(evt.data);
            $(".article-list .has-view h2 > a[rel=bookmark]").each(function () {
                var id = $(this).data('id').toString();

                if (data.articleId === id) {
                    var $li = $(this).closest("li"),
                            $heat = $li.find('.heat');

                    if (data.operation === "+") {
                        $li.append('<i class="point"></i>');
                        setTimeout(function () {
                            $heat.width($heat.width() + 1 * 3);
                            $li.find(".point").remove();
                        }, 2000);
                    } else {
                        $heat.width($heat.width() - 1 * 3);
                        $li.append('<i class="point-remove"></i>');
                        setTimeout(function () {
                            $li.find(".point-remove").remove();
                        }, 2000);
                    }
                }
            });
        };

        ArticleListChannel.ws.onclose = function () {
            ArticleListChannel.ws.close();
        };

        ArticleListChannel.ws.onerror = function (err) {
            console.log("ERROR", err);
        };
    }
};

/**
 * @description Timeline channel.
 * @static
 */
var TimelineChannel = {
    /**
     * WebSocket instance.
     * 
     * @type WebSocket
     */
    ws: undefined,
    /**
     * @description Initializes message channel
     */
    init: function (channelServer, timelineCnt) {
        TimelineChannel.ws = new ReconnectingWebSocket(channelServer);
        TimelineChannel.ws.reconnectInterval = 10000;

        TimelineChannel.ws.onopen = function () {
            setInterval(function () {
                TimelineChannel.ws.send('-hb-');
            }, 1000 * 60 * 3);
        };

        TimelineChannel.ws.onmessage = function (evt) {
            var data = JSON.parse(evt.data);

            $('#emptyTimeline').remove();

            switch (data.type) {
                case 'newUser':
                case 'article':
                case 'comment':
                case 'activity':
                    var time = new Date().getTime();
                    var template = "<li class=\"fn-none\" id=" + time + ">" + data.content + "</li>";
                    $("#ul").prepend(template);
                    $("#" + time).fadeIn(2000);

                    var length = $("#ul > li").length;
                    if (length > timelineCnt) {
                        $("#ul > li:last").remove();
                    }

                    break;
            }
        };

        TimelineChannel.ws.onclose = function () {
            TimelineChannel.ws.close();
        };

        TimelineChannel.ws.onerror = function (err) {
            console.log("ERROR", err)
        };
    }
};

/**
 * @description Char room channel.
 * @static
 */
var ChatRoomChannel = {
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
        ChatRoomChannel.ws = new ReconnectingWebSocket(channelServer);
        ChatRoomChannel.ws.reconnectInterval = 10000;

        ChatRoomChannel.ws.onopen = function () {
            setInterval(function () {
                ChatRoomChannel.ws.send('-hb-');
            }, 1000 * 60 * 3);
        };

        ChatRoomChannel.ws.onmessage = function (evt) {
            var data = JSON.parse(evt.data);

            switch (data.type) {
                case "online":
                    $("#onlineCnt").text(data.onlineChatCnt);
                    break;
                case "msg":
                    var enableUserLink = data.userAvatarURL.indexOf("user-thumbnail.png") < 0;
                    var avatarPart = '<a rel="nofollow" href="/member/' + data.userName + '">'
                            + '<div class="avatar" '
                            + 'title="' + data.userName + '" style="background-image:url(' + data.userAvatarURL + '-64.jpg)"></div>'
                            + '</a>';
                    if (!enableUserLink) {
                        avatarPart = '<div class="avatar" '
                                + 'title="' + data.userName + '" style="background-image:url(' + data.userAvatarURL + '-64.jpg)"></div>';
                    }

                    var namePart = '<a rel="nofollow" href="/member/' + data.userName + '" '
                            + 'title="' + data.userName + '">' + data.userName + '</a>';
                    if (!enableUserLink) {
                        namePart = data.userName;
                    }

                    var liHTML = '<li class="fn-none">'
                            + '<div class="fn-flex">'
                            + avatarPart
                            + '<div class="fn-flex-1">'
                            + '<div class="fn-clear">'
                            + '<span class="fn-left">'
                            + namePart
                            + '</span>'
                            + '</div>'
                            + '<div class="content-reset">'
                            + data.content
                            + '</div>'
                            + '</div>'
                            + '</div>'
                            + '</li>';
                    if ($('.form ul li').length === 0) {
                        $('.form ul').html(liHTML);
                    } else {
                        $('.form ul li:first').before(liHTML);
                    }

                    if ($('.form .list').scrollTop() < $('.form li').outerHeight() * 2) {
                        $('.form .list').animate({'scrollTop': 0}, 500);
                    }
                    $(".form ul li:first").fadeIn(2000);
                    break;
            }
        };

        ChatRoomChannel.ws.onclose = function () {
            ChatRoomChannel.ws.close();
        };

        ChatRoomChannel.ws.onerror = function (err) {
            console.log("ERROR", err)
        };
    }
};