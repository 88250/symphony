/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
/**
 * @fileoverview Message channel via WebSocket.
 *
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.14.0.1, Jan 30, 2018
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
          if ($('#comments .list > ul > li').length === 0) {
            $('.comment-header > .fn-none').show();
            // 显示预览模式 & 回到底部
            $('.comments-header > .fn-none').show();
            // 显示评论
            $("#articleCommentsPanel").parent().show();
          }

          if (0 === Label.userCommentViewMode) { // tranditional view mode
            $("#comments > .list > ul").append(data.cmtTpl);
          } else {
            $("#comments > .list > ul").prepend(data.cmtTpl);
          }

          // ua
          $("#" + data.commentId + ' .cmt-via').text(Util.getDeviceByUa(data.commentUA));

          // 回帖高亮，他人回帖不定位，只有自己回帖才定位
          if (Label.currentUserName === data.commentAuthorName) {
            Comment._bgFade($("#" + data.commentId));
          }

          // 代码高亮
          hljs.initHighlighting.called = false;
          hljs.initHighlighting();

          // 更新回复的回帖
          if (data.commentOriginalCommentId !== '') {
            var $originalComment = $('#' + data.commentOriginalCommentId),
              $replyBtn = $originalComment.find('.comment-action > .ft-fade > .fn-pointer');
            if ($replyBtn.length === 1) {
              $replyBtn.html(' ' + (parseInt($.trim($replyBtn.text())) + 1)
                + ' ' + Label.replyLabel + ' <span class="'
                + $replyBtn.find('span').attr('class') + '"></span>');

              if ($replyBtn.find('svg').attr('class') === "icon-chevron-up") {
                $replyBtn.find('svg').removeClass('icon-chevron-up').addClass('icon-chevron-down').find('use').attr('xlink:href', '#chevron-down');
                $replyBtn.click();
              }
            } else {
              $originalComment.find('.comment-action > .ft-fade').prepend('<span class="fn-pointer ft-smaller fn-left" onclick="Comment.showReply(\''
                + data.commentOriginalCommentId + '\', this, \'comment-replies\')" style="opacity: 1;"> 1 '
                + Label.replyLabel + ' <svg class="icon-chevron-down"><use xlink:href="#chevron-down"></use></svg>');
            }
          }
          Util.parseMarkdown();
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

/**
 * @description gobang game channel.
 * @static
 */
var GobangChannel = {
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
    GobangChannel.ws = new ReconnectingWebSocket(channelServer);
    GobangChannel.ws.reconnectInterval = 10000;

    GobangChannel.ws.onopen = function () {
      setInterval(function () {
        GobangChannel.ws.send('zephyr test');
      }, 1000 * 60 * 3);
    };

    GobangChannel.ws.onmessage = function (evt) {
      var data = JSON.parse(evt.data);

      switch (data.type) {
        case "gobangPlayer":
          console.log("data.type:>gobangPlayer");
          break;
        case "msg":
          console.log("data.type:>msg");
          break;
      }
    };

    GobangChannel.ws.onclose = function () {
      GobangChannel.ws.close();
    };

    GobangChannel.ws.onerror = function (err) {
      console.log("ERROR", err);
    };
  }
};