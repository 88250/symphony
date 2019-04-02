/*
 * Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
 * Copyright (C) 2012-present, b3log.org
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
 * @fileoverview 聊天室
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 1.3.0.0, Feb 11, 2019
 */

/**
 * @description Add comment function.
 * @static
 */
var ChatRoom = {
  init: function () {
    // 聊天窗口高度设置
    if ($.ua.device.type !== 'mobile') {
      $('.list').
        height($('.side').height() -
          $('.chat-room .module:first').outerHeight() - 20)
    } else {
      $('.list').height($(window).height() - 173)
    }

    // 没用登陆就不需要编辑起初始化了
    if ($('#chatContent').length === 0) {
      return false
    }

    ChatRoom.editor = Util.newVditor({
      id: 'chatContent',
      cache: true,
      preview: {
        show: false,
      },
      resize: {
        enable: true,
        position: 'bottom',
      },
      height: 160,
      counter: 4096,
      placeholder: 'Say sth...',
      ctrlEnter: function () {
        ChatRoom.send()
      },
    })
  },
  /**
   * 发送聊天内容
   * @returns {undefined}
   */
  send: function () {
    var content = ChatRoom.editor.getValue()
    var requestJSONObject = {
      content: content,
    }

    $.ajax({
      url: Label.servePath + '/chat-room/send',
      type: 'POST',
      cache: false,
      data: JSON.stringify(requestJSONObject),
      beforeSend: function () {
        $('.form button.red').
          attr('disabled', 'disabled').
          css('opacity', '0.3')
      },
      success: function (result) {
        if (result.sc) {
          $('#chatContentTip').removeClass('error succ').html('')

          ChatRoom.editor.setValue('')

        } else {
          $('#chatContentTip').
            addClass('error').
            html('<ul><li>' + result.msg + '</li></ul>')
        }
      },
      error: function (result) {
        $('#chatContentTip').
          addClass('error').
          html('<ul><li>' + result.statusText + '</li></ul>')
      },
      complete: function (jqXHR, textStatus) {
        $('.form button.red').removeAttr('disabled').css('opacity', '1')
      },
    })
  },
}

