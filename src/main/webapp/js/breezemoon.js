/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2018, b3log.org & hacpai.com
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
  init: function () {
    $('#breezemoonInput').keyup(function (event) {
      if (event.keyCode === 13) {
        Breezemoon.add()
      }
    })

    var $breezemoonList = $('#breezemoonList')
    $breezemoonList.on('click', '.rm', function () {
      var $it = $(this)
      Breezemoon.rm($it, $it.closest('li').attr('id'))
    })

    $breezemoonList.on('click', '.edit', function () {
      var $it = $(this)
      Breezemoon.toggleUpdate($it, $it.closest('li').attr('id'))
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
      url: Label.servePath + '/bm',
      type: 'POST',
      cache: false,
      data: JSON.stringify({
        breezemoonContent: $('#breezemoonInput').val(),
      }),
      success: function (result) {
        if (result.sc) {
          $('#breezemoonList').
            append('<li> <a class="tooltipped tooltipped-n avatar"\n' +
              '                               style="background-image:url(\'https://img.hacpai.com/avatar/1353745196544_1501644090048.png\')"\n' +
              '                               rel="nofollow" href="http://localhost:8080/member/Vanessa" aria-label="Vanessa">\n' +
              '                            </a>\n' +
              '                            <div class="fn-flex-1">\n' +
              '                                <div class="ft-fade">\n' +
              '                                    <a href="">Vanessa</a>\n' +
              '                                    •\n' +
              '                                    <span class="ft-smaller">\n' +
              '                                        1分钟钱\n' +
              '                                    </span>\n' +
              '                                    <span class="ft-smaller"\n' +
              '                                          data-ua="Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Mobile Safari/537.36">via Android</span>\n' +
              '                                    <div class="fn-right">\n' +
              '                                        <span class="tooltipped tooltipped-n ft-red" aria-label="${removeLabel}">\n' +
              '                                            <svg><use xlink:href="#remove"></use></svg>\n' +
              '                                        </span>\n' +
              '                                        &nbsp;&nbsp;\n' +
              '                                        <span class="tooltipped tooltipped-n ft-a-title" aria-label="${editLabel}">\n' +
              '                                            <svg><use xlink:href="#edit"></use></svg>\n' +
              '                                        </span>\n' +
              '                                    </div>\n' +
              '                                </div>\n' +
              '                                <div class="content-reset">\n' +
              '                                    af\n' +
              '                                </div>\n' +
              '                            </div></li>')
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
        url: Label.servePath + '/bm/' + id,
        type: 'DELETE',
        cache: false,
        success: function (result) {
          if (result.sc) {
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
    $content.after('<div class="form" style="margin-top: 15px"><input type="text" value="' + $content.text() + '"><button class="absolute">' +
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
      url: Label.servePath + '/bm/' + id,
      type: 'PUT',
      cache: false,
      data: JSON.stringify({
        breezemoonContent: val,
      }),
      success: function (result) {
        if (result.sc) {
          $btn.closest('form').prev().text(val).show()
          $btn.closest('form').remove()
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