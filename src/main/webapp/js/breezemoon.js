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
 * @fileoverview Breezemoon.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @version 0.1.0.0, May 21, 2018
 */

/**
 * @description Breezemoon
 * @static
 */
var Breezemoon = {
  csrfToken: '',
  init: function () {
    $('#breezemoonInput').keyup(function (event) {
      if (event.keyCode === 13) {
        Breezemoon.add()
      }
    })

    this.csrfToken =  $('#breezemoonBtn').data('csrftoken')

    var $breezemoonList = $('#breezemoonList')
    $breezemoonList.on('click', '.rm', function () {
      var $it = $(this)
      Breezemoon.rm($it, $it.closest('li').attr('id'))
    })

    $breezemoonList.on('click', '.edit', function () {
      var $it = $(this)
      Breezemoon.toggleUpdate($it, $it.closest('li').attr('id'))
    })
    $breezemoonList.find('.copy').each(function () {
      var $it = $(this)
      Util.clipboard($it, $it.next())
    })

    $breezemoonList.find('.ua').each(function () {
      var ua = $(this).data('ua'),
        name = Util.getDeviceByUa(ua);
      if (name !== '') {
        $(this).text('via ' + name);
      }
    })
  },
  add: function () {
    if (!Label.isLoggedIn) {
      alert(Label.reloginLabel)
      return
    }

    var $breezemoonBtn = $('#breezemoonBtn')

    $breezemoonBtn.css('opacity', 0.3).attr('disabled')

    $.ajax({
      url: Label.servePath + '/breezemoon',
      type: 'POST',
      cache: false,
      headers: {'csrfToken': this.csrfToken},
      data: JSON.stringify({
        breezemoonContent: $('#breezemoonInput').val(),
      }),
      success: function (result) {
        if (result.sc === 0) {
          window.location.reload()
        } else {
          alert(result.msg)
        }
      },
      complete: function () {
        $breezemoonBtn.css('opacity', 1).removeAttr('disabled')
      },
    })
  },
  rm: function ($it, id) {
    if (confirm(Label.confirmRemoveLabel)) {
      $it.css('opacity', 0.3).attr('disabled')
      $.ajax({
        url: Label.servePath + '/breezemoon/' + id,
        type: 'DELETE',
        headers: {'csrfToken': this.csrfToken},
        cache: false,
        success: function (result) {
          if (result.sc === 0) {
            $it.closest('li').remove()
          } else {
            alert(result.msg)
          }
        },
        complete: function () {
          $it.css('opacity', 1).removeAttr('disabled')
        },
      })
    }
  },
  toggleUpdate: function ($it, id) {
    var $li = $it.closest('li')
    var $content = $li.find('.content-reset')
    if ($li.find('.form').length === 1) {
      $content.show()
      $li.find('.form').remove()
      return
    }
    $content.hide()
    $content.after('<div class="form" style="margin-top: 15px"><input type="text" value="' +
      $content.text() + '"  style="padding-right: 89px;"><button class="absolute">' +
      Label.breezemoonLabel + '</button></div>')
    $content.next().find('input').keyup(function (event) {
      if (event.keyCode === 13) {
        Breezemoon.update($content.next().find('button'), $li.attr('id'),
          $(this).val())
      }
    })
    $content.next().find('button').click(function () {
      Breezemoon.update($(this), $li.attr('id'),
        $content.next().find('input').val())
    })
  },
  update: function ($btn, id, val) {
    $btn.css('opacity', 0.3).attr('disabled')
    $.ajax({
      url: Label.servePath + '/breezemoon/' + id,
      type: 'PUT',
      cache: false,
      headers: {'csrfToken': this.csrfToken},
      data: JSON.stringify({
        breezemoonContent: val,
      }),
      success: function (result) {
        if (result.sc === 0) {
          $btn.closest('.form').prev().text(val).show()
          $btn.closest('.form').remove()
        } else {
          alert(result.msg)
        }
      },
      complete: function () {
        $btn.css('opacity', 1).removeAttr('disabled')
      },
    })
  },
}

$(document).ready(function () {
  Breezemoon.init()
})