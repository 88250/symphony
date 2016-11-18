/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2016,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @fileoverview Message channel via WebSocket.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.10.15.19, Nov 18, 2016
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

            if (Label.articleOId !== data.articleId) { // It's not the current article
                return;
            }

            switch (data.type) {
                case "comment":
                    var cmtCount = parseInt($(".comments-header .article-cmt-cnt").text()) + 1;
                    // 总帖数更新
                    $(".comments-header .article-cmt-cnt").text(cmtCount + ' ' + Label.cmtLabel);

                    // 新增第一条评论时到底部的锚点
                    var bottomCmt = '';
                    if ($('#comments .list > ul > li.ft-center').length === 1) {
                        $('.comment-header > .fn-none').show();
                        bottomCmt = '<div id="bottomComment"></div>';
                        // 显示预览模式 & 回到底部
                        $('.comments-header > .fn-none').show();
                        // 移除没有评论的提示
                        $('#comments .list > ul > li.ft-center').remove();
                        // 显示评论大图
                        $('#comments').next().show();
                    }

                    // ua
                    var UAName = Util.getDeviceByUa(data.commentUA);
                    if (UAName !== '') {
                        UAName = ' <span class="cmt-via ft-fade fn-hidden hover-show">via ' + UAName + '</span>';
                    }

                    var template = '<li id="' + data.commentId + '">'
                            + bottomCmt + '<div class="fn-flex">';

                    if (!data.fromClient) {
                        if (data.commentAuthorName !== 'someone') {
                            template += '<a rel="nofollow" href="/member/' + data.commentAuthorName + '">';
                        }
                        template += '<div class="avatar tooltipped tooltipped-se" aria-label="' + data.commentAuthorName + '" style="background-image:url('
                                + data.commentAuthorThumbnailURL + ')"></div>';
                        if (data.commentAuthorName !== 'someone') {
                            template += '</a>';
                        }
                    } else {
                        template += '<div class="avatar tooltipped tooltipped-se" aria-label="' + data.commentAuthorName + '" style="background-image:url('
                                + data.commentAuthorThumbnailURL + ')"></div>';
                    }

                    template += '<div class="fn-flex-1">'
                            + '<div class="comment-get-comment list"></div><div class="fn-clear comment-info ft-smaller">'
                            + '<span class="fn-left">';

                    if (!data.fromClient) {
                        if (data.commentAuthorName !== 'someone') {
                            template += '<a rel="nofollow" class="ft-gray" href="/member/' + data.commentAuthorName + '">';
                        }
                        template += '<span class="ft-gray">' + data.commentAuthorName + '</span>';
                        if (data.commentAuthorName !== 'someone') {
                            template += '</a>';
                        }
                    } else {
                        template += '<span class="ft-gray">' + data.commentAuthorName + '</span> <span class="ft-fade"> • </span> <a rel="nofollow" class="ft-green" href="https://hacpai.com/article/1457158841475">API</a>';
                    }
                    template += ' <span class="ft-fade"> • ' + data.timeAgo + '</span> <span class="comment-reward"></span>';

                    if (data.userUAStatus === 0) {
                        template += ' ' + UAName;
                    }

                    template += '</span><span class="fn-right">';
                    if (data.commentOriginalCommentId !== '') {
                        template += '<span class="ft-fade tooltipped tooltipped-nw" aria-label="'
                                + Label.goCommentLabel + '" onclick="Comment.showReply(\'' + data.commentOriginalCommentId + '\', this, \'comment-get-comment\')"><span class="icon-reply-to"></span>  <div class="avatar-small" style="background-image:url(\''
                                + data.commentOriginalAuthorThumbnailURL + '\')"></div></span> ';
                    }
                    if (Label.isAdminLoggedIn) {
                        template += '<a class="hover-show fn-hidden tooltipped tooltipped-n ft-a-title" href="/admin/comment/' + data.commentId
                                + '" aria-label="' + Label.adminLabel + '"><span class="icon-setting"></span></a> ';
                    }
                    template += '</span></div><div class="content-reset comment">'
                            + data.commentContent + '</div><div class="comment-action"><div class="ft-fade fn-clear">'
                            + '<span class="fn-right fn-hidden hover-show action-btns">'
                            + '<span class="tooltipped tooltipped-n" '
                            + ' aria-label="' + Label.thankLabel + '" onclick="Comment.thank(\'' + data.commentId + '\', \'' + Label.csrfToken
                            + '\', \'' + data.commentThankLabel + '\','
                            + (data.commentAuthorName === 'someone' ? 1 : 0) + ', this)"><span class="icon-heart"></span> 0</span> &nbsp; '
                            + '<span class="tooltipped tooltipped-n" '
                            + 'aria-label="' + Label.upLabel + '" '
                            + 'onclick="Article.voteUp(\'' + data.commentId + '\', \'comment\', this)">'
                            + '<span class="icon-thumbs-up"></span> 0</span> &nbsp; '
                            + '<span class="tooltipped tooltipped-n"'
                            + 'aria-label="' + Label.downLabel + '" '
                            + 'onclick="Article.voteDown(\'' + data.commentId + '\', \'comment\', this)">'
                            + '<span class="icon-thumbs-down"></span> 0</span>';

                    if ((Label.isLoggedIn && data.commentAuthorName !== Label.currentUserName) || !Label.isLoggedIn) {
                        template += ' &nbsp; <span aria-label="' + Label.replyLabel + '" class="tooltipped tooltipped-n icon-reply-btn" onclick="Comment.reply(\''
                                + data.commentAuthorName + '\', \''
                                + data.commentId + '\')"><span class="icon-reply"></span></span> ';
                    }
                    template += '</span></div><div class="comment-replies list"></div></div></li>';

                    if (0 === Label.userCommentViewMode) { // tranditional view mode
                        $("#comments > .list > ul").append(template);
                    } else {
                        $("#comments > .list > ul").prepend(template);
                    }

                    // 代码高亮
                    Article.parseLanguage();
                    Comment._bgFade($("#" + data.commentId));

                    // 更新回复的帖子
                    if (data.commentOriginalCommentId !== '') {
                        var $originalComment = $('#' + data.commentOriginalCommentId),
                                $replyBtn = $originalComment.find('.comment-action > .ft-fade > .fn-pointer');
                        if ($replyBtn.length === 1) {
                            $replyBtn.html(' ' + (parseInt($.trim($replyBtn.text())) + 1)
                                    + ' ' + Label.replyLabel + ' <span class="'
                                    + $replyBtn.find('span').attr('class') + '"></span>');

                            if ($replyBtn.find('span').attr('class') === "icon-chevron-up") {
                                $replyBtn.find('span').removeClass('icon-chevron-up').addClass('icon-chevron-down');
                                $replyBtn.click();
                            }
                        } else {
                            $originalComment.find('.comment-action > .ft-fade').prepend('<span class="fn-pointer ft-smaller fn-left" onclick="Comment.showReply(\''
                                    + data.commentOriginalCommentId + '\', this, \'comment-replies\')" style="opacity: 1;"> 1 '
                                    + Label.replyLabel + ' <span class="icon-chevron-down"></span>');
                        }
                    }
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
            $(".article-list h2 > a[rel=bookmark]").each(function () {
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
                    $(".timeline > ul").prepend(template);
                    $(".timeline > ul > li:first").fadeIn(2000);

                    var length = $(".timeline > ul > li").length;
                    if (length > timelineCnt) {
                        $(".timeline > ul  > li:last").remove();
                    }

                    break;
            }
        };

        TimelineChannel.ws.onclose = function () {
            TimelineChannel.ws.close();
        };

        TimelineChannel.ws.onerror = function (err) {
            console.log("ERROR", err);
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
                            + '<div class="avatar tooltipped tooltipped-se" aria-label="' + data.userName
                            + '" style="background-image:url(' + data.userAvatarURL + ')"></div>'
                            + '</a>';
                    if (!enableUserLink) {
                        avatarPart = '<div class="avatar tooltipped tooltipped-se" aria-label="' + data.userName
                                + '" style="background-image:url(' + data.userAvatarURL + ')"></div>';
                    }

                    var namePart = '<a rel="nofollow" href="/member/' + data.userName + '">' + data.userName + '</a>';
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
                            + '<div class="content-reset comment">'
                            + data.content
                            + '</div>'
                            + '</div>'
                            + '</div>'
                            + '</li>';
                    if ($('.list ul li').length === 0) {
                        $('.list ul').html(liHTML);
                    } else {
                        $('.list ul li:first').before(liHTML);
                    }

                    if ($('.list').scrollTop() < $('li').outerHeight() * 2) {
                        $('.list').animate({'scrollTop': 0}, 500);
                    }
                    $(".list li:first").fadeIn(2000);
                    break;
            }
        };

        ChatRoomChannel.ws.onclose = function () {
            ChatRoomChannel.ws.close();
        };

        ChatRoomChannel.ws.onerror = function (err) {
            console.log("ERROR", err);
        };
    }
};