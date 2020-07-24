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
 * @fileoverview article page and add comment.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @version 0.5.0.3, Apr 30, 2020
 * @since 2.1.0
 */

/**
 * @description Add comment function.
 * @static
 */
var Comment = {
  editor: undefined,
  /**
   * 举报
   * @param it
   */
  report: function (it) {
    var $btn = $(it)
    $btn.attr('disabled', 'disabled').css('opacity', '0.3')
    $.ajax({
      url: Label.servePath + '/report',
      type: 'POST',
      cache: false,
      data: JSON.stringify({
        reportDataId: $('#reportDialog').data('id'),
        reportDataType: $('#reportDialog').data('type'),
        reportType: $('input[name=report]:checked').val(),
        reportMemo: $('#reportTextarea').val(),
      }),
      complete: function (result) {
        $btn.removeAttr('disabled').css('opacity', '1')
        if (result.responseJSON.code === 0) {
          Util.alert(Label.reportSuccLabel)
          $('#reportTextarea').val('')
          $('#reportDialog').dialog('close')
        } else {
          Util.alert(result.responseJSON.msg)
        }
      },
    })
  },
  /**
   * 采纳评论
   * @param tip
   * @param id
   */
  accept: function (tip, id, it) {
    if (!confirm(tip)) {
      return
    }
    $.ajax({
      url: Label.servePath + '/comment/accept',
      type: 'POST',
      headers: {'csrfToken': Label.csrfToken},
      cache: false,
      data: JSON.stringify({
        commentId: id,
      }),
      success: function (result) {
        if (0 !== result.code) {
          Util.alert(result.msg)
          return
        } else {
          $(it).closest('li').addClass('cmt-perfect')
          $(it).remove()
        }
      },
    })
  },
  /**
   * 删除评论
   * @param {integer} id 评论 id
   */
  remove: function (id) {
    if (!confirm(Label.confirmRemoveLabel)) {
      return false
    }
    $.ajax({
      url: Label.servePath + '/comment/' + id + '/remove',
      type: 'POST',
      cache: false,
      success: function (result, textStatus) {
        if (result.code === 0) {
          $('#' + id).remove()
        } else {
          Util.alert(result.msg)
        }
      },
    })
  },
  /**
   * 切换评论排序模式
   * @param {integer} mode 排序模式：0 传统模式，正序；1 实时模式，倒序
   * @returns {undefined}
   */
  exchangeCmtSort: function (mode) {
    mode = 0 === mode ? 1 : 0

    window.location.href = window.location.pathname + '?m=' + mode
  },
  /**
   * 编辑评论
   * @param {string} id 评论 id
   */
  edit: function (id) {
    $('.anonymous-check').hide()
    $.ajax({
      url: Label.servePath + '/comment/' + id + '/content',
      type: 'GET',
      cache: false,
      success: function (result, textStatus) {
        if (result.code === 0) {
          // doc.lineCount
          Comment.editor.setValue(result.commentContent)
        }
      },
    })

    $('#replyUseName').
      html('<a href="javascript:void(0)" onclick="Comment._bgFade($(\'#' +
        id +
        '\'))" class="ft-a-title"><svg><use xlink:href="#edit"></use></svg> ' +
        Label.commonUpdateCommentPermissionLabel + '</a>').
      data('commentId', id)
  },
  /**
   * 背景渐变
   * @param {jQuery} $obj 背景渐变对象
   * @returns {undefined}
   */
  _bgFade: function ($obj) {
    if ($obj.length === 0) {
      return false
    }

    $(window).scrollTop($obj[0].offsetTop - 48)

    if ($obj.attr('id') === 'comments') {
      return false
    }

    $obj.css({
      'background-color': '#9bbee0',
    })
    setTimeout(function () {
      $obj.css({
        'background-color': '#FFF',
        'transition': 'all 3s cubic-bezier(0.56, -0.36, 0.58, 1)',
      })
    }, 100)
    setTimeout(function () {
      $obj.removeAttr('style')
    }, 3100)
  },
  /**
   * 跳转到指定的评论处
   * @param {string} url 跳转的 url
   */
  goComment: function (url) {
    if ($(url.substr(url.length - 14, 14)).length === 0) {
      window.location = url
      return false
    }

    $('#comments .list > ul > li').removeAttr('style')
    Comment._bgFade($(url.substr(url.length - 14, 14)))
  },
  /**
   * 设置评论来源
   * @returns {Boolean}
   */
  _setCmtVia: function () {
    $('.cmt-via').each(function () {
      var ua = $(this).data('ua'),
        name = Util.getDeviceByUa(ua)
      if (name !== '') {
        $(this).html('via ' + name)
      }
    })
  },
  /**
   * 评论初始化
   * @returns {Boolean}
   */
  init: function () {
    if ($(window.location.hash).length === 1) {
      // if (!isNaN(parseInt(window.location.hash.substr(1)))) {
      Comment._bgFade($(window.location.hash))
      //}
    }

    this._setCmtVia()

    $.ua.set(navigator.userAgent)

    $.pjax({
      selector: '.pagination a',
      container: '#comments',
      show: '',
      cache: false,
      storage: true,
      titleSuffix: '',
      callback: function () {
        Util.parseMarkdown()
        Util.parseHljs()
      },
    })
    NProgress.configure({showSpinner: false})
    $('#comments').bind('pjax.start', function () {
      NProgress.start()
    })
    $('#comments').bind('pjax.end', function () {
      NProgress.done()
    })

    if (!Label.isLoggedIn) {
      return false
    }

    Comment.editor = Util.newVditor({
      id: 'commentContent',
      cache: true,
      preview: {
        mode: 'editor',
      },
      resize: {
        enable: true,
        position: 'top',
      },
      height: 200,
      counter: 4096,
      placeholder: Label.commentEditorPlaceholderLabel,
      ctrlEnter: function () {
        Comment.add(Label.articleOId, Label.csrfToken,
          document.getElementById('articleCommentBtn'))
      },
    })
  },
  /**
   * @description 感谢评论.
   * @param {String} id 评论 id
   * @param {String} csrfToken CSRF 令牌
   * @param {String} tip 确认提示
   * @param {Integer} 0：公开评论，1：匿名评论
   */
  thank:

    function (id, csrfToken, tip, commentAnonymous, it) {
      if (!Label.isLoggedIn) {
        Util.needLogin()
        return false
      }

      // 匿名回帖不需要进行 confirm
      if (0 === commentAnonymous && !confirm(tip)) {
        return false
      }

      var requestJSONObject = {
        commentId: id,
      }

      $.ajax({
        url: Label.servePath + '/comment/thank',
        type: 'POST',
        headers: {'csrfToken': csrfToken},
        cache: false,
        data: JSON.stringify(requestJSONObject),
        error: function (jqXHR, textStatus, errorThrown) {
          Util.alert(errorThrown)
        },
        success: function (result, textStatus) {
          if (0 === result.code) {
            $(it).removeAttr('onclick')
            var $heart = $(
              '<svg class="ft-red"><use xlink:href="#heart"></use></svg>'),
              y = $(it).offset().top,
              x = $(it).offset().left
            $heart.css({
              'z-index': 9999,
              'top': y,
              'left': x,
              'position': 'absolute',
              'font-size': 16,
              '-moz-user-select': 'none',
              '-webkit-user-select': 'none',
              '-ms-user-select': 'none',
            })
            $('body').append($heart)

            $heart.animate({'left': x - 150, 'top': y - 60, 'opacity': 0},
              1000,
              function () {
                var cnt = parseInt($(it).text())

                $(it).
                  html('<svg><use xlink:href="#heart"></use></svg> ' +
                    (cnt + 1)).
                  addClass('ft-red')

                $heart.remove()
              }
            )

          } else {
            Util.alert(result.msg)
          }
        },
      })
    }

  ,
  /**
   * @description 展现回帖回复列表
   * @param {type} id 回帖 id
   * @returns {Boolean}
   */
  showReply: function (id, it, className) {
    var $commentReplies = $(it).closest('li').find('.' + className)

    // 回复展现需要每次都异步获取。回复的回帖只需加载一次，后期不再加载
    if ('comment-get-comment' === className) {
      if ($commentReplies.find('li').length !== 0) {
        $commentReplies.html('')
        return false
      }
    } else {
      if ($(it).find('.icon-chevron-down').length === 0) {
        // 收起回复
        $(it).
          find('.icon-chevron-up').
          removeClass('icon-chevron-up').
          addClass('icon-chevron-down').
          find('use').
          attr('xlink:href', '#chevron-down')
        $commentReplies.html('')
        return false
      }
    }

    if ($(it).css('opacity') === '0.3') {
      return false
    }

    var url = '/comment/replies'
    if ('comment-get-comment' === className) {
      url = '/comment/original'
    }

    $.ajax({
      url: Label.servePath + url,
      type: 'POST',
      data: JSON.stringify({
        commentId: id,
        userCommentViewMode: Label.userCommentViewMode,
      }),
      beforeSend: function () {
        $(it).css('opacity', '0.3')
      },
      success: function (result, textStatus) {
        if (0 !== result.code) {
          Util.alert(result.msg)
          return false
        }

        var comments = result.commentReplies,
          template = ''
        if (!(comments instanceof Array)) {
          comments = [comments]
        }

        if (comments.length === 0) {
          template = '<li class="ft-red">' + Label.removedLabel + '</li>'
        }

        for (var i = 0; i < comments.length; i++) {
          var data = comments[i]

          template += '<li><div class="fn-flex">'
          template += '<a rel="nofollow" href="/member/' + data.commentAuthorName + '">'
          template += '<div class="avatar tooltipped tooltipped-se" aria-label="' +
            data.commentAuthorName + '" style="background-image:url('
            + data.commentAuthorThumbnailURL + ')"></div>'
          template += '</a>'

          template += '<div class="fn-flex-1">'
            + '<div class="comment-info ft-smaller">'
            template += '<a class="ft-gray" rel="nofollow" href="/member/' +
              data.commentAuthorName + '">'
          template += data.commentAuthorName
          template += '</a>'

          template += '<span class="ft-fade"> • ' + data.timeAgo
          if (data.rewardedCnt > 0) {
            template += '<span aria-label="'
              + (data.rewarded ? Label.thankedLabel : Label.thankLabel + ' ' +
                data.rewardedCnt)
              + '" class="tooltipped tooltipped-n '
              + (data.rewarded ? 'ft-red' : 'ft-fade') + '">'
              + ' <svg><use xlink:href="#heart"></use></svg> ' +
              data.rewardedCnt + '</span> '
          }

          template += ' ' + Util.getDeviceByUa(data.commentUA) + '</span>'

          template += '<a class="tooltipped tooltipped-nw ft-a-title fn-right" aria-label="' +
            Label.referenceLabel + '" href="javascript:Comment.goComment(\''
            + Label.servePath + '/article/' + Label.articleOId + '?p=' +
            data.paginationCurrentPageNum
            + '&m=' + Label.userCommentViewMode + '#' + data.oId
            +
            '\')"><svg><use xlink:href="#quote"></use></svg></a></div><div class="vditor-reset comment">'
            + data.commentContent + '</div></div></div></li>'
        }
        $commentReplies.html('<ul>' + template + '</ul>')
        Util.parseHljs()
        Util.parseMarkdown()

        // 如果是回帖的回复需要处理下样式
        $(it).
          find('.icon-chevron-down').
          removeClass('icon-chevron-down').
          addClass('icon-chevron-up').
          find('use').
          attr('xlink:href', '#chevron-up')
      },
      error: function (result) {
        Util.alert(result.statusText)
      },
      complete: function () {
        $(it).css('opacity', '1')
      },
    })
  }
  ,
  /**
   * @description 添加评论
   * @param {String} id 文章 id
   * @param {String} csrfToken CSRF 令牌
   */
  add: function (id, csrfToken) {

    var requestJSONObject = {
      articleId: id,
      commentAnonymous: $('#commentAnonymous').prop('checked'),
      commentVisible: $('#commentVisible').prop('checked'),
      commentContent: Comment.editor.getValue(), // 实际提交时不去除空格，因为直接贴代码时需要空格
      userCommentViewMode: Label.userCommentViewMode,
    }

    if ($('#replyUseName').data('commentOriginalCommentId')) {
      requestJSONObject.commentOriginalCommentId = $('#replyUseName').
        data('commentOriginalCommentId')
    }

    var url = Label.servePath + '/comment',
      type = 'POST',
      commentId = $('#replyUseName').data('commentId')
    if (commentId) {
      url = Label.servePath + '/comment/' + commentId
      type = 'PUT'
    }

    $.ajax({
      url: url,
      type: type,
      headers: {'csrfToken': csrfToken},
      cache: false,
      data: JSON.stringify(requestJSONObject),
      beforeSend: function () {
        $('.form button.red').attr('disabled', 'disabled').css('opacity', '0.3')
      },
      success: function (result, textStatus) {
        $('.form button.red').removeAttr('disabled').css('opacity', '1')

        if (0 === result.code) {
          // edit cmt
          if (commentId) {
            $('#' + commentId + ' > .fn-flex > .fn-flex-1 > .vditor-reset').
              html(result.commentContent)
          }

          // reset comment editor
          Comment.editor.setValue('')

          // clear reply comment
          $('#replyUseName').text('').removeData()

          // clear local storage
          if (window.localStorage) {
            var emptyContent = {
              commentContent: '',
            }

            window.localStorage[Label.articleOId] = JSON.stringify(emptyContent)
          }

          // 定为到回贴位置
          if (Label.userCommentViewMode === 1) {
            // 实时模式
            Comment._bgFade($('#comments'))
          } else {
            Comment._bgFade($('#bottomComment'))
          }
        } else {
          $('#addCommentTip').
            addClass('error').
            html('<ul><li>' + result.msg + '</li></ul>')
        }
      },
      error: function (result) {
        $('#addCommentTip').
          addClass('error').
          html('<ul><li>' + result.statusText + '</li></ul>')
      },
      complete: function () {
        $('.form button.red').removeAttr('disabled').css('opacity', '1')
      },
    })
  }
  ,
  /**
   * @description 点击回复评论时，把当楼层的用户名带到评论框中
   * @param {String} userName 用户名称
   */
  reply: function (userName, id) {
    $('#replyUseName').data('commentOriginalCommentId', id)
    Comment.editor.focus()
  },
}

var Article = {
  initAudio: function () {
    $('.content-audio').each(function () {
      var $it = $(this)
      new APlayer({
        element: this,
        narrow: false,
        autoplay: false,
        mutex: true,
        theme: '#4285f4',
        preload: 'none',
        mode: 'circulation',
        music: {
          title: $it.data('title'),
          author: '<a href="' + Label.servePath + '/about" target="_blank">音乐分享</a>',
          url: $it.data('url'),
          pic: Label.staticServePath + '/images/music.png',
        },
      })
    })

    var $articleAudio = $('#articleAudio')
    if ($articleAudio.length === 0) {
      return false
    }

    new APlayer({
      element: document.getElementById('articleAudio'),
      narrow: false,
      autoplay: false,
      mutex: true,
      theme: '#4285f4',
      mode: 'order',
      preload: 'none',
      music: {
        title: '语音预览',
        author: '<a href="' + Label.servePath + '/about" target="_blank">小薇</a>',
        url: $articleAudio.data('url'),
        pic: Label.staticServePath + '/images/blank.png',
      },
    })
  },
  /**
   * @description 没有权限的提示
   * @param {String} tip 提示内容
   */
  permissionTip: function (tip) {
    if (Label.isLoggedIn) {
      Util.alert(tip)
    } else {
      Util.needLogin()
    }
  },
  /**
   * @description 赞同
   * @param {String} id 赞同的实体数据 id
   * @param {String} type 赞同的实体类型
   */
  voteUp: function (id, type, it) {
    if (!Label.isLoggedIn) {
      Util.needLogin()
      return false
    }

    var $voteUp = $(it)
    var $voteDown = $voteUp.next()

    if ($voteUp.hasClass('disabled')) {
      return false
    }

    var requestJSONObject = {
      dataId: id,
    }

    $voteUp.addClass('disabled')

    $.ajax({
      url: Label.servePath + '/vote/up/' + type,
      type: 'POST',
      cache: false,
      data: JSON.stringify(requestJSONObject),
      success: function (result, textStatus) {
        $voteUp.removeClass('disabled')
        var upCnt = parseInt($voteUp.text()),
          downCnt = parseInt($voteDown.text())
        if (0 === result.code) {
          if (0 === result.type) { // cancel up
            $voteUp.html('<svg class="icon-thumbs-up"><use xlink:href="#thumbs-up"></use></svg> ' +
              (upCnt - 1)).removeClass('ft-red')
          } else {
            $voteUp.html('<svg class="icon-thumbs-up"><use xlink:href="#thumbs-up"></use></svg> ' +
              (upCnt + 1)).addClass('ft-red')
            if ($voteDown.hasClass('ft-red')) {
              $voteDown.html('<svg class="icon-thumbs-down"><use xlink:href="#thumbs-down"></use></svg> ' +
                (downCnt - 1)).removeClass('ft-red')
            }
          }

          return
        }

        Util.alert(result.msg)
      },
    })
  },
  /**
   * @description 反对
   * @param {String} id 反对的实体数据 id
   * @param {String} type 反对的实体类型
   */
  voteDown: function (id, type, it) {
    if (!Label.isLoggedIn) {
      Util.needLogin()
      return false
    }
    var $voteDown = $(it)
    var $voteUp = $voteDown.prev()

    if ($voteDown.hasClass('disabled')) {
      return false
    }

    var requestJSONObject = {
      dataId: id,
    }

    $voteDown.addClass('disabled')

    $.ajax({
      url: Label.servePath + '/vote/down/' + type,
      type: 'POST',
      cache: false,
      data: JSON.stringify(requestJSONObject),
      success: function (result, textStatus) {
        $voteDown.removeClass('disabled')
        var upCnt = parseInt($voteUp.text()),
          downCnt = parseInt($voteDown.text())
        if (0 === result.code) {
          if (1 === result.type) { // cancel down
            $voteDown.html('<svg class="icon-thumbs-down"><use xlink:href="#thumbs-down"></use></svg> ' +
              (downCnt - 1)).removeClass('ft-red')
          } else {
            $voteDown.html('<svg class="icon-thumbs-down"><use xlink:href="#thumbs-down"></use></svg> ' +
              (downCnt + 1)).addClass('ft-red')
            if ($voteUp.hasClass('ft-red')) {
              $voteUp.html('<svg class="icon-thumbs-up"><use xlink:href="#thumbs-up"></use></svg> ' +
                (upCnt - 1)).removeClass('ft-red')
            }
          }

          return false
        }

        Util.alert(result.msg)
      },
    })
  },
  /**
   * @description 大图预览等待获取大小后重制 translate
   */
  previewImgAfterLoading: function () {
    $('.img-preview img').css('transform', 'translate3d(' +
      (Math.max(0, $(window).width() - $('.img-preview img').width()) / 2) +
      'px, ' +
      (Math.max(0, $(window).height() - $('.img-preview img').height()) / 2) +
      'px, 0)')

    // fixed chrome render transform bug
    setTimeout(function () {
      $('.img-preview').width($(window).width())
    }, 300)
  },
  /**
   * @description 初始化文章
   */
  init: function () {
    this.share()
    Util.parseHljs()
    Util.parseMarkdown()

    // img preview
    var fixDblclick = null
    $('.article').on('dblclick', '.vditor-reset img', function () {
      clearTimeout(fixDblclick)
      if ($(this).hasClass('emoji') ||
        $(this).closest('.editor-panel').length === 1 ||
        $(this).closest('.ad').length === 1) {
        return
      }
      window.open($(this).attr('src'))
    }).on('click', '.vditor-reset img', function (event) {
      clearTimeout(fixDblclick)
      if ($(this).hasClass('emoji') ||
        $(this).closest('.editor-panel').length === 1 ||
        $(this).closest('.ad').length === 1) {
        return
      }
      var $it = $(this),
        it = this
      fixDblclick = setTimeout(function () {
        var top = it.offsetTop,
          left = it.offsetLeft
        if ($it.closest('.comments').length === 1) {
          top = top + $it.closest('li')[0].offsetTop
          left = left + $('.comments')[0].offsetLeft + 15
        }

        $('body').
          append('<div class="img-preview" onclick="$(this).remove()"><img style="transform: translate3d(' +
            Math.max(0, left) + 'px, ' +
            Math.max(0, (top - $(window).scrollTop())) + 'px, 0)" src="' +
            ($it.attr('src').split('?imageView2')[0]) +
            '" onload="Article.previewImgAfterLoading()"></div>')

        $('.img-preview').css({
          'background-color': '#fff',
          'position': 'fixed',
        })
      }, 100)
    })

    // UA
    var ua = $('#articltVia').data('ua'),
      name = Util.getDeviceByUa(ua)
    if (name !== '') {
      $('#articltVia').text('via ' + name)
    }

    // his
    $('#revision').dialog({
      'width': $(window).width() - 50,
      'height': $(window).height() - 50,
      'modal': true,
      'hideFooter': true,
    })

    // report
    $('#reportDialog').dialog({
      'width': $(window).width() > 500 ? 500 : $(window).width() - 50,
      'height': 450,
      'modal': true,
      'hideFooter': true,
    })

    this.initAudio()
  },
  /**
   * 历史版本对比
   * @param {string} id 文章/评论 id
   * @param {string} type 类型[comment, article]
   * @returns {undefined}
   */
  revision: function (id, type) {
    if (!Label.isLoggedIn) {
      Util.needLogin()
      return false
    }
    if (!type) {
      type = 'article'
    }

    $.ajax({
      url: Label.servePath + '/' + type + '/' + id + '/revisions',
      cache: false,
      success: function (result, textStatus) {
        if (0 === result.code) {
          if (0 === result.revisions.length // for legacy data
            || 1 === result.revisions.length) {
            $('#revision > .revisions').remove()
            $('#revisions').html('<b>' + Label.noRevisionLabel + '</b>')
            return false
          }

          // clear data
          $('#revisions').html('').prev().remove()

          $('#revisions').
            data('revisions', result.revisions).
            before('<div class="revisions">' +
              '<a href="javascript:void(0)" class="first"><svg><use xlink:href="#chevron-left"</svg></a><span>' +
              (result.revisions.length - 1) + '~' + result.revisions.length +
              '/' +
              result.revisions.length +
              '</span><a href="javascript:void(0)" class="disabled last"><svg><use xlink:href="#chevron-right"</svg></a>' +
              '</div>')
          if (result.revisions.length <= 2) {
            $('#revision a').first().addClass('disabled')
          }

          var diff = JsDiff.createPatch('',
            result.revisions[result.revisions.length -
            2].revisionData.articleContent ||
            result.revisions[result.revisions.length -
            2].revisionData.commentContent,
            result.revisions[result.revisions.length -
            1].revisionData.articleContent ||
            result.revisions[result.revisions.length -
            1].revisionData.commentContent,
            result.revisions[result.revisions.length -
            2].revisionData.articleTitle || '',
            result.revisions[result.revisions.length -
            1].revisionData.articleTitle || '')

          var diff2htmlUi = new Diff2HtmlUI({diff: diff})
          diff2htmlUi.draw('#revisions', {
            matching: 'lines',
            outputFormat: 'side-by-side',
            synchronisedScroll: true,
          })
          Article._revisionsControls(type)
          return false
        }

        Util.alert(result.msg)
      },
    })
    $('#revision').dialog('open')
  },
  /**
   * 上一版本，下一版本对比
   * @returns {undefined}
   */
  _revisionsControls: function (type) {
    var revisions = $('#revisions').data('revisions')
    $('#revision a.first').click(function () {
      if ($(this).hasClass('disabled')) {
        return
      }

      var prevVersion = parseInt(
        $('#revision .revisions').text().split('~')[0])
      if (prevVersion <= 2) {
        $(this).addClass('disabled')
      } else {
        $(this).removeClass('disabled')
      }

      if (revisions.length > 2) {
        $('#revision a.last').removeClass('disabled')
      }

      $('#revision .revisions > span').
        html((prevVersion - 1) + '~' + prevVersion + '/' + revisions.length)

      var diff = JsDiff.createPatch('',
        revisions[prevVersion - 2].revisionData.articleContent ||
        revisions[prevVersion - 2].revisionData.commentContent,
        revisions[prevVersion - 1].revisionData.articleContent ||
        revisions[prevVersion - 1].revisionData.commentContent,
        revisions[prevVersion - 2].revisionData.articleTitle || '',
        revisions[prevVersion - 1].revisionData.articleTitle || '')

      var diff2htmlUi = new Diff2HtmlUI({diff: diff})
      diff2htmlUi.draw('#revisions', {
        matching: 'lines',
        outputFormat: 'side-by-side',
        synchronisedScroll: true,
      })
    })

    $('#revision a.last').click(function () {
      if ($(this).hasClass('disabled')) {
        return
      }

      var prevVersion = parseInt(
        $('#revision .revisions span').text().split('~')[0])
      if (prevVersion > revisions.length - 3) {
        $(this).addClass('disabled')
      } else {
        $(this).removeClass('disabled')
      }

      if (revisions.length > 2) {
        $('#revision a.first').removeClass('disabled')
      }

      $('#revision .revisions > span').
        html((prevVersion + 1) + '~' + (prevVersion + 2) + '/' +
          revisions.length)

      var diff = JsDiff.createPatch('',
        revisions[prevVersion].revisionData.articleContent ||
        revisions[prevVersion].revisionData.commentContent,
        revisions[prevVersion + 1].revisionData.articleContent ||
        revisions[prevVersion + 1].revisionData.commentContent,
        revisions[prevVersion].revisionData.articleTitle || '',
        revisions[prevVersion + 1].revisionData.articleTitle || '')

      var diff2htmlUi = new Diff2HtmlUI({diff: diff})
      diff2htmlUi.draw('#revisions', {
        matching: 'lines',
        outputFormat: 'side-by-side',
        synchronisedScroll: true,
      })
    })
  },
  /**
   * @description 分享按钮
   */
  share: function () {
    if ($.ua.device.type !== 'mobile') {
      var shareL = $('.article-content').offset().left / 2 - 15
      $('.share').css('left', (shareL < 20 ? 20 : shareL) + 'px')
    }

    var shareURL = $('#qrCode').data('shareurl')
    $('#qrCode').qrcode({
      width: 90,
      height: 90,
      text: shareURL,
    })

    $('body').click(function () {
      $('#qrCode').slideUp()
    })

    $('.share > span').click(function () {
      var key = $(this).data('type')
      if (!key) return false
      if (key === 'wechat') {
        $('#qrCode').slideToggle()
        return false
      }

      if (key === 'copy') {
        return false
      }

      var title = encodeURIComponent(Label.articleTitle + ' - ' +
        Label.symphonyLabel),
        url = encodeURIComponent(shareURL),
        picCSS = $('.article-info .avatar').css('background-image')
      pic = picCSS.substring(5, picCSS.length - 2)

      var urls = {}
      urls.tencent = 'http://share.v.t.qq.com/index.php?c=share&a=index&title=' +
        title +
        '&url=' + url + '&pic=' + pic
      urls.weibo = 'http://v.t.sina.com.cn/share/share.php?title=' +
        title + '&url=' + url + '&pic=' + pic
      urls.google = 'https://plus.google.com/share?url=' + url
      urls.twitter = 'https://twitter.com/intent/tweet?status=' + title + ' ' +
        url
      window.open(urls[key], '_blank', 'top=100,left=200,width=648,height=618')
    })

    $('#qrCode').click(function () {
      $(this).hide()
    })

    $('#shareClipboard').mouseover(function () {
      $(this).attr('aria-label', Label.copyLabel)
    })
    Util.clipboard($('#shareClipboard'), $('#shareClipboard').next(),
      function () {
        $('#shareClipboard').attr('aria-label', Label.copiedLabel)
      })
  },
  /**
   * @description 打赏
   */
  reward: function (articleId) {
    var r = confirm(Label.rewardConfirmLabel)

    if (r) {
      $.ajax({
        url: Label.servePath + '/article/reward?articleId=' + articleId,
        type: 'POST',
        cache: false,
        success: function (result, textStatus) {
          if (0 === result.code) {
            $('#articleRewardContent').html(result.articleRewardContent)
            Util.parseHljs()
            Util.parseMarkdown()

            var cnt = parseInt(
              $('.article-actions .icon-points').parent().text())
            $('.article-actions .icon-points').
              parent().
              addClass('ft-red').
              html('<svg><use xlink:href="#points"></use></svg> ' + (cnt + 1)).
              removeAttr('onclick')
            return
          }

          Util.alert(result.msg)
        },
        error: function (result) {
          Util.needLogin()
        },
      })
    }
  },
  /**
   * @description 感谢文章
   */
  thankArticle: function (articleId, articleAnonymous) {
    if (!Label.isLoggedIn) {
      Util.needLogin()
      return false
    }

    // 匿名贴不需要 confirm
    if (0 === articleAnonymous && !confirm(Label.thankArticleConfirmLabel)) {
      return false
    }

    if (Label.currentUserName === Label.articleAuthorName) {
      Util.alert(Label.thankSelfLabel)
      return false
    }

    $.ajax({
      url: Label.servePath + '/article/thank?articleId=' + articleId,
      type: 'POST',
      cache: false,
      success: function (result, textStatus) {
        if (0 === result.code) {
          var thxCnt = parseInt($('#thankArticle').text())
          $('#thankArticle').
            removeAttr('onclick').
            html('<svg><use xlink:href="#heart"></use></svg> <span class="ft-13">' +
              (thxCnt + 1) + '</span>').
            addClass('ft-red').
            removeClass('ft-blue')

          var $heart = $(
            '<svg class="ft-red"><use xlink:href="#heart"></use></svg>'),
            y = $('#thankArticle').offset().top,
            x = $('#thankArticle').offset().left
          $heart.css({
            'z-index': 9999,
            'top': y - 20,
            'left': x,
            'position': 'absolute',
            'font-size': 16,
            '-moz-user-select': 'none',
            '-webkit-user-select': 'none',
            '-ms-user-select': 'none',
          })
          $('body').append($heart)

          $heart.animate({'top': y - 180, 'opacity': 0},
            1500,
            function () {
              $heart.remove()
            }
          )

          return false
        }

        Util.alert(result.msg)
      },
    })
  },
  /**
   * @description 置顶
   */
  stick: function (articleId) {
    var r = confirm(Label.stickConfirmLabel)

    if (r) {
      $.ajax({
        url: Label.servePath + '/article/stick?articleId=' + articleId,
        type: 'POST',
        cache: false,
        success: function (result, textStatus) {
          Util.alert(result.msg)

          window.location.href = Label.servePath + '/recent'
        },
      })
    }
  },
  /**
   * @description 播放思绪
   * @param {string} articleContent 记录过程
   */
  playThought: function (articleContent) {
    // + 0x1F: Unit Separator (单元分隔符) 31
    var UNIT_SEPARATOR = String.fromCharCode(31)
    // - 0x1E: Record Separator (记录分隔符) 30
    var RECORD_SEPARATOR = String.fromCharCode(30)
    var GROUP_SEPARATOR = String.fromCharCode(29)
    var NEWLINE = String.fromCharCode(10)
    var DELETE = String.fromCharCode(24)

    var $thoughtLine = $('#thoughtProgress > span')
    var $thoughtIcon = $('#thoughtProgress > svg')
    var thoughtContent = '#articleThought'

    var genThought = function (record, articleLinesList) {
      var units = record.split(UNIT_SEPARATOR)
      if (units.length === 3) {
        units.splice(0, 0, '')
      }
      var srcLinesContent = units[0],
        from = units[2].split('-'),
        to = units[3].split('-')
      from[0] = parseInt(from[0])    // from.ch
      from[1] = parseInt(from[1])    // from.line
      to[0] = parseInt(to[0])    // to.ch
      to[1] = parseInt(to[1])    // to.line

      if (srcLinesContent === DELETE) {
        // remove
        var removeLines = []
        for (var n = from[1], m = 0; n <= to[1], n <
        articleLinesList.length; n++, m++) {
          if (from[1] === to[1]) {
            articleLinesList[n] = articleLinesList[n].substring(0, from[0]) +
              articleLinesList[n].substr(to[0])
            break
          }

          if (n === from[1]) {
            articleLinesList[n] = articleLinesList[n].substr(0, from[0])
          } else if (n === to[1]) {
            articleLinesList[from[1]] += articleLinesList[n].substr(to[0])
            articleLinesList.splice(n, 1)
          } else {
            removeLines.push(n)
          }
        }
        for (var o = 0; o < removeLines.length; o++) {
          articleLinesList.splice(removeLines[o] - o, 1)
        }
      } else {
        var addLines = srcLinesContent.split(GROUP_SEPARATOR)[0],
          removedLines = srcLinesContent.split(GROUP_SEPARATOR)[1]

        if (removedLines === '') {
          articleLinesList[from[1]] = articleLinesList[from[1]].substring(0,
            from[0]) +
            articleLinesList[to[1]].substr(to[0])
          // Note: for b3log editor
          articleLinesList.splice(from[1] + 1, to[1] - from[1])
        }

        if (typeof articleLinesList[from[1]] !== 'undefined') {
          articleLinesList[from[1]] = articleLinesList[from[1]].substring(0,
            from[0]) + addLines
            + articleLinesList[from[1]].substr(from[0])
        } else {
          articleLinesList[from[1]] = ''
        }
      }
      return articleLinesList
    }

    var records = articleContent.split(RECORD_SEPARATOR)
    var STEP = 20 // 间隔速度
    var SPEED = Math.max(records[records.length - 2].split(UNIT_SEPARATOR)[1] /
      30000, 2)

    // 分隔符后的''删除
    if (records[records.length - 1] === '') {
      records.pop()
    }

    for (var i = 0, j = 0; i < records.length; i++) {
      setTimeout(function () {
        if (!$(thoughtContent).data('text')) {
          $(thoughtContent).data('text', '')
        }

        var articleLinesList = genThought(records[j++],
          $(thoughtContent).data('text').split(NEWLINE))

        var articleText = articleLinesList.join(NEWLINE)
        var articleHTML = articleText.replace(/\n/g, '<br>').
          replace(/ /g, '&nbsp;').
          replace(/	/g, '&nbsp;&nbsp;&nbsp;&nbsp;')

        $(thoughtContent).data('text', articleText).html(articleHTML)

      }, parseInt(records[i].split(UNIT_SEPARATOR)[1]) / SPEED)
    }

    // progress
    var currentTime = 0
    var amountTime = parseInt(records[i - 1].split(UNIT_SEPARATOR)[1]) / SPEED
    var interval = setInterval(function () {
      if (currentTime >= amountTime) {
        $thoughtLine.width('100%')
        $thoughtIcon.css('left', '100%')
        clearInterval(interval)
      } else {
        currentTime += STEP
        $thoughtIcon.css('left', (currentTime * 100 / amountTime) + '%')
        $thoughtLine.width((currentTime * 100 / amountTime) + '%')
      }
    }, STEP)

    // preview
    var previewText = ''
    var articleHTML = ''
    var maxHeight = 0
    for (var v = 0, k = 0; v < records.length; v++) {
      var articleLinesList = genThought(records[k++],
        previewText.split(NEWLINE))

      var articleText = articleLinesList.join(NEWLINE)
      articleHTML = articleText.replace(/\n/g, '<br>').
        replace(/ /g, '&nbsp;').
        replace(/	/g, '&nbsp;&nbsp;&nbsp;&nbsp;')

      previewText = articleText
      $(thoughtContent).html(articleHTML)
      maxHeight = Math.max(maxHeight, $(thoughtContent).height())
    }

    $('#thoughtProgressPreview').
      html('<div class="vditor-reset">' + articleHTML + '</div>')

    $('#thoughtProgressPreview').dialog({
      'modal': true,
      'hideFooter': true,
    })
    $thoughtIcon.click(function () {
      $('#thoughtProgressPreview').dialog('open')
    })

    // set default height
    $(thoughtContent).html(articleHTML).
      height(maxHeight).
      css('margin-bottom', '15px').
      html('')
  },
  /**
   * @description 标记消息通知为已读状态.
   */
  makeNotificationRead: function (articleId, commentIds) {
    var requestJSONObject = {
      articleId: articleId,
      commentIds: commentIds,
    }

    $.ajax({
      url: Label.servePath + '/notifications/make-read',
      type: 'POST',
      cache: false,
      data: JSON.stringify(requestJSONObject),
    })
  },
}

Article.init()

$(document).ready(function () {
  Comment.init()

  // Init [Article] channel
  ArticleChannel.init(Label.articleChannel)

  // make nogification read
  if (Label.isLoggedIn) {
    Article.makeNotificationRead(Label.articleOId, Label.notificationCmtIds)

    setTimeout(function () {
      Util.setUnreadNotificationCount()
    }, 1000)
  }
})
