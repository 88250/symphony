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
 * @fileoverview add-article.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="https://hacpai.com/member/ZephyrJung">Zephyr</a>
 * @author <a href="https://qiankunpingtai.cn">qiankunpingtai</a>
 * @version 2.26.2.1, Apr 30, 2020
 */

/**
 * @description Add article function.
 * @static
 */
var AddArticle = {
  editor: undefined,
  rewardEditor: undefined,
  /**
   * 记录思绪过程
   * @param {string} currentValue 当前内容
   * @param {string} prevValue 输入前的上一次值
   * @param {Object} postData 发贴本地缓存数据
   */
  recordThought: function (currentValue, prevValue, postData) {
    var diff = JsDiff.diffChars(prevValue, currentValue)
    var unitSep = String.fromCharCode(31) // Unit Separator (单元分隔符)
    var change = ''
    var fromCh = 0
    var fromLine = 0
    var toCh = 0
    var toLine = 0
    for (var iMax = diff.length, i = 0; i < iMax; i++) {
      var time = (new Date()).getTime() - postData.thoughtTime
      var valueArr = diff[i].value.split('\n')

      if (!diff[i].removed && !diff[i].added) {
        toLine = fromLine = valueArr.length - 1 + fromLine
        if (valueArr.length === 1) {
          toCh = fromCh = fromCh + valueArr[valueArr.length - 1].length
        } else {
          toCh = fromCh = valueArr[valueArr.length - 1].length
        }
      }

      if (diff[i].removed) {
        toCh += diff[i].count
        toLine += valueArr.length - 1

        change += String.fromCharCode(24) + unitSep + time // cancel
          + unitSep + fromCh + '-' + fromLine
          + unitSep + toCh + '-' + toLine
          + String.fromCharCode(30)  // Record Separator (记录分隔符)
      } else if (diff[i].added) {
        change += diff[i].value + unitSep + time
          + unitSep + fromCh + '-' + fromLine
          + unitSep + toCh + '-' + toLine
          + String.fromCharCode(30)  // Record Separator (记录分隔符)

        if (valueArr[valueArr.length - 1].length === 0) {
          toCh = fromCh = 0
        } else {
          toCh = fromCh = valueArr[valueArr.length - 1].length + fromCh
        }
        toLine = fromLine = valueArr.length - 1 + fromLine
      }
    }

    postData.thoughtContent += change
  },

  /**
   * @description 删除文章
   * @csrfToken [string] CSRF 令牌
   * @it [bom] 调用事件的元素
   */
  remove: function (csrfToken, it) {
    if (!confirm(Label.confirmRemoveLabel)) {
      return
    }

    $.ajax({
      url: Label.servePath + '/article/' + Label.articleOId + '/remove',
      type: 'POST',
      headers: {'csrfToken': csrfToken},
      cache: false,
      beforeSend: function () {
        $(it).attr('disabled', 'disabled').css('opacity', '0.3')
      },
      error: function (jqXHR, textStatus, errorThrown) {
        $('#addArticleTip').
          addClass('error').
          html('<ul><li>' + errorThrown + '</li></ul>')
      },
      success: function (result, textStatus) {
        $(it).removeAttr('disabled').css('opacity', '1')
        if (0 === result.code) {
          window.location.href = Label.servePath + '/member/' + Label.userName
        } else {
          $('#addArticleTip').
            addClass('error').
            html('<ul><li>' + result.msg + '</li></ul>')
        }
      },
      complete: function () {
        $(it).removeAttr('disabled').css('opacity', '1')
      },
    })
  },
  /**
   * @description 发布文章
   * @csrfToken [string] CSRF 令牌
   * @it [Bom] 触发事件的元素
   */
  add: function (csrfToken, it) {
    if (Validate.goValidate({
      target: $('#addArticleTip'),
      data: [
        {
          'type': 'string',
          'max': 256,
          'msg': Label.articleTitleErrorLabel,
          'target': $('#articleTitle'),
        }],
    })) {
      var articleType = parseInt(
        $('input[type=\'radio\'][name=\'articleType\']:checked').val())

      if (articleType !== 5) {
        // 打赏区启用后积分不能为空
        if ($('#articleRewardPoint').data('orval')
          && !/^\+?[1-9][0-9]*$/.test($('#articleRewardPoint').val())) {
          $('#addArticleTip').addClass('error').html('<ul><li>'
            + Label.articleRewardPointErrorLabel + '</li></ul>')
          return false
        }
      }

      var articleTags = ''
      $('.tags-input .tag .text').each(function () {
        articleTags += $(this).text() + ','
      })

      var requestJSONObject = {
        articleTitle: $('#articleTitle').val().replace(/(^\s*)|(\s*$)/g, ''),
        articleContent: this.editor.getValue(),
        articleTags: articleTags,
        articleCommentable: $('#articleCommentable').prop('checked'),
        articleNotifyFollowers: $('#articleNotifyFollowers').prop('checked'),
        articleType: articleType,
        articleShowInList: Boolean($('#articleShowInList').prop('checked'))
          ? 1
          : 0,
      }

      if (articleType !== 5) {
        requestJSONObject.articleRewardContent = this.rewardEditor.getValue()
        requestJSONObject.articleRewardPoint = $('#articleRewardPoint').
          val().
          replace(/(^\s*)|(\s*$)/g, '')
        requestJSONObject.articleAnonymous = $('#articleAnonymous').
          prop('checked')
      } else {
        requestJSONObject.articleQnAOfferPoint = $('#articleAskPoint').
          val().
          replace(/(^\s*)|(\s*$)/g, '')
      }

      var url = Label.servePath + '/article', type = 'POST'

      if (3 === parseInt(requestJSONObject.articleType)) { // 如果是“思绪”
        requestJSONObject.articleContent = JSON.parse(
          window.localStorage.postData).thoughtContent
      }

      if (Label.articleOId) {
        url = url + '/' + Label.articleOId
        type = 'PUT'
      }

      $.ajax({
        url: url,
        type: type,
        headers: {'csrfToken': csrfToken},
        cache: false,
        data: JSON.stringify(requestJSONObject),
        beforeSend: function () {
          $(it).attr('disabled', 'disabled').css('opacity', '0.3')
        },
        error: function (jqXHR, textStatus, errorThrown) {
          $('#addArticleTip').
            addClass('error').
            html('<ul><li>' + errorThrown + '</li></ul>')
        },
        success: function (result, textStatus) {
          $(it).removeAttr('disabled').css('opacity', '1')
          if (0 === result.code) {
            window.location.href = Label.servePath + '/article/' +
              result.articleId
            localStorage.removeItem('postData')
            AddArticle.editor.clearCache()
            AddArticle.rewardEditor.clearCache()
          } else {
            $('#addArticleTip').
              addClass('error').
              html('<ul><li>' + result.msg + '</li></ul>')
          }
        },
        complete: function () {
          $(it).removeAttr('disabled').css('opacity', '1')
        },
      })
    }
  },
  /**
   * @description 初始化发文
   */
  init: function () {
    $.ua.set(navigator.userAgent)

    // local data
    if (location.search.indexOf('?id=') > -1) {
      localStorage.removeItem('postData')
    }

    var postData = undefined
    if (!localStorage.postData) {
      postData = {
        title: '',
        content: '',
        tags: '',
        thoughtContent: '',
        rewardContent: '',
        rewardPoint: '',
        thoughtTime: (new Date()).getTime(),
      }
      localStorage.postData = JSON.stringify(postData)
    } else {
      postData = JSON.parse(localStorage.postData)
    }

    // init content editor
    if ('' !== postData.content) {
      $('#articleContent').val(postData.content)
    }

    var prevValue = postData.content
    // 初始化文章编辑器
    AddArticle.editor = Util.newVditor({
      outline: true,
      typewriterMode: true,
      id: 'articleContent',
      cache: Label.articleOId ? false : true,
      preview: {
        mode: 'both',
      },
      resize: {
        enable: false,
      },
      after: function () {
        if ($('#articleContent').next().val() !== '') {
          AddArticle.editor.setValue($('#articleContent').next().val())
        }
      },
      height: 360,
      counter: 4096,
      placeholder: $('#articleContent').data('placeholder'),
      input: function () {
        if (Label.articleType === 3) {
          var postData = JSON.parse(localStorage.postData)
          prevValue = localStorage.getItem('vditorarticleContent') || ''
          AddArticle.recordThought(AddArticle.editor.getValue(), prevValue,
            postData)
          localStorage.postData = JSON.stringify(postData)
        }
      },
    })

    // 私信 at 默认值
    var atIdx = location.href.indexOf('at=')
    if (-1 !== atIdx) {
      if ('' == postData.content) {
        var at = AddArticle.editor.getValue()
        AddArticle.editor.setValue('\n\n\n' + at)
      }

      if ('' == postData.title) {
        var username = Util.getParameterByName('at')
        $('#articleTitle').val('Hi, ' + username)
      }
      if ('' !== postData.tags) {
        var tagTitles = Label.discussionLabel
        var tags = Util.getParameterByName('tags')
        if ('' !== tags) {
          tagTitles += ',' + tags
        }
        $('#articleTags').val(tagTitles)
      }
    }

    // set url title
    if ('' == postData.title) {
      var title = Util.getParameterByName('title')
      if (title && title.length > 0) {
        $('#articleTitle').val(title)
      }
    }

    // set localStorage
    if ('' !== postData.title) {
      $('#articleTitle').val(postData.title)
    }
    $('#articleTitle').keyup(function () {
      var postData = JSON.parse(localStorage.postData)
      postData.title = $(this).val()
      localStorage.postData = JSON.stringify(postData)
    })

    if ('' !== postData.tags) {
      $('#articleTags').val(postData.tags)
    }

    this._initTag()

    // focus
    if ($('#articleTitle').val().length <= 0) {
      $('#articleTitle').focus()
    }

    // check title is repeat
    $('#articleTitle').blur(function () {
      if ($.trim($(this).val()) === '') {
        return false
      }

      if (1 === Label.articleType) { // 机要不检查
        return
      }

      $.ajax({
        url: Label.servePath + '/article/check-title',
        type: 'POST',
        data: JSON.stringify({
          'articleTitle': $.trim($(this).val()),
          'articleId': Label.articleOId, // 更新时才有值
        }),
        success: function (result, textStatus) {
          if (0 !== result.code) {
            if ($('#articleTitleTip').length === 1) {
              $('#articleTitleTip').html(result.msg)
            } else {
              $('#articleTitle').
                after('<div class="module" id="articleTitleTip">' +
                  result.msg +
                  '</div>')
            }

          } else {
            $('#articleTitleTip').remove()
          }
        },
      })
    })

    // 快捷发文
    $('#articleTags, #articleRewardPoint, #articleAskPoint').
      keypress(function (event) {
        if (event.ctrlKey && 10 === event.charCode) {
          AddArticle.add()
          return false
        }
      })

    if ($('#articleAskPoint').length === 0) {
      // 初始化打赏区编辑器
      if (0 < $('#articleRewardPoint').val().replace(/(^\s*)|(\s*$)/g, '')) {
        $('#showReward').click()
      }

      AddArticle.rewardEditor = Util.newVditor({
        id: 'articleRewardContent',
        cache: Label.articleOId ? false : true,
        preview: {
          mode: 'editor',
        },
        resize: {
          enable: false,
        },
        height: 200,
        counter: 4096,
        placeholder: $('#articleRewardContent').data('placeholder'),
        after: function () {
          if ($('#articleRewardContent').next().val() !== '') {
            $('#showReward').click()
            AddArticle.rewardEditor.setValue(
              $('#articleRewardContent').next().val())
          }
        },
      })
    }

    if ($('#articleAskPoint').length === 0) {
      if ('' !== postData.rewardContent) {
        $('#showReward').click()
        AddArticle.rewardEditor.setValue(postData.rewardContent)
      }

      if ('' !== postData.rewardPoint) {
        $('#showReward').click()
        $('#articleRewardPoint').val(postData.rewardPoint)
      }
      $('#articleRewardPoint').keyup(function () {
        var postData = JSON.parse(localStorage.postData)
        postData.rewardPoint = $(this).val()
        localStorage.postData = JSON.stringify(postData)
      })
    } else {
      $('#articleAskPoint').keyup(function () {
        var postData = JSON.parse(localStorage.postData)
        postData.QnAOfferPoint = $(this).val()
        localStorage.postData = JSON.stringify(postData)
      })
      if ('' !== postData.QnAOfferPoint && $('#articleAskPoint').val() === '') {
        $('#articleAskPoint').val(postData.QnAOfferPoint)
      }
    }
  },
  /**
   * @description 初始化标签编辑器
   * @returns {undefined}
   */
  _initTag: function () {
    $.ua.set(navigator.userAgent)

    // 添加 tag 到输入框
    var addTag = function (text) {
      if (text.replace(/\s/g, '') === '') {
        return false
      }
      var hasTag = false
      text = text.replace(/\s/g, '').replace(/,/g, '')
      $('#articleTags').val('')

      // 重复添加处理
      $('.tags-input .text').each(function () {
        var $it = $(this)
        if (text === $it.text()) {
          $it.parent().addClass('haved')
          setTimeout(function () {
            $it.parent().removeClass('haved')
          }, 900)
          hasTag = true
        }
      })

      if (hasTag) {
        return false
      }

      // 长度处理
      if ($('.tags-input .tag').length >= 4) {
        $('#articleTags').val('').data('val', '')
        return false
      }

      $('.post .tags-selected').append('<span class="tag"><span class="text">'
        + text + '</span><span class="close">x</span></span>')

      // set tags to localStorage
      if (location.search.indexOf('?id=') === -1) {
        var articleTags = ''
        $('.tags-input .tag .text').each(function () {
          articleTags += $(this).text() + ','
        })

        var postData = JSON.parse(localStorage.postData)
        postData.tags = articleTags
        localStorage.postData = JSON.stringify(postData)
      }

      if ($('.tags-input .tag').length >= 4) {
        $('#articleTags').val('').data('val', '')
      }
    }

    // domains 切换
    $('.domains-tags .btn').click(function () {
      $('.domains-tags .btn.current').removeClass('current green')
      $(this).addClass('current').addClass('green')
      $('.domains-tags .domain-tags').hide()
      $('#tags' + $(this).data('id')).show()
    })

    // tag 初始化渲染
    var initTags = $('#articleTags').val().split(',')
    for (var j = 0, jMax = initTags.length; j < jMax; j++) {
      addTag(initTags[j])
    }

    // 领域 tag 选择
    $('.domain-tags .tag').click(function () {
      addTag($(this).text())
    })

    // 移除 tag
    $('.tags-input').on('click', '.tag > span.close', function () {
      $(this).parent().remove()

      // set tags to localStorage
      if (location.search.indexOf('?id=') === -1) {
        var articleTags = ''
        $('.tags-input .tag .text').each(function () {
          articleTags += $(this).text() + ','
        })

        var postData = JSON.parse(localStorage.postData)
        postData.tags = articleTags
        localStorage.postData = JSON.stringify(postData)
      }
    })

    // 展现领域 tag 选择面板
    $('#articleTags').click(function () {
      $('.post .domains-tags').show()
      if ($.ua.device.type !== 'mobile') {
        $('.post .domains-tags').
          css('left', ($('.post .tags-selected').width() + 10) + 'px')
      }
      $('#articleTagsSelectedPanel').hide()
    }).blur(function () {
      if ($('#articleTagsSelectedPanel').css('display') === 'block') {
        // 鼠标点击 completed 面板时避免把输入框的值加入到 tag 中
        return false
      }
      addTag($(this).val())
    })

    // 关闭领域 tag 选择面板
    $('body').click(function (event) {
      if ($(event.target).closest('.tags-input').length === 1 ||
        $(event.target).closest('.domains-tags').length === 1) {
      } else {
        $('.post .domains-tags').hide()
      }
    })

    // 自动补全 tag
    $('#articleTags').completed({
      height: 170,
      onlySelect: true,
      data: [],
      afterSelected: function ($it) {
        addTag($it.text())
      },
      afterKeyup: function (event) {
        $('.post .domains-tags').hide()
        // 遇到分词符号自动添加标签
        if (event.key === ',' || event.key === '，' ||
          event.key === '、' || event.key === '；' || event.key === ';') {
          var text = $('#articleTags').val()
          addTag(text.substr(0, text.length - 1))
          return false
        }

        // 回车，自动添加标签
        if (event.keyCode === 13) {
          addTag($('#articleTags').val())
          return false
        }

        // 上下左右
        if (event.keyCode === 37 || event.keyCode === 39 ||
          event.keyCode === 38 || event.keyCode === 40) {
          return false
        }

        // ECS 隐藏面板
        if (event.keyCode === 27) {
          $('#articleTagsSelectedPanel').hide()
          return false
        }

        // 删除 tag
        if (event.keyCode === 8 && event.data.settings.chinese === 8
          && event.data.settings.keydownVal.replace(/\s/g, '') === '') {
          $('.tags-input .tag .close:last').click()
          return false
        }

        if ($('#articleTags').val().replace(/\s/g, '') === '') {
          return false
        }

        $.ajax({
          url: Label.servePath + '/tags/query?title=' + $('#articleTags').val(),
          error: function (jqXHR, textStatus, errorThrown) {
            $('#addArticleTip').
              addClass('error').
              html('<ul><li>' + errorThrown + '</li></ul>')
          },
          success: function (result, textStatus) {
            if (0 === result.code) {
              if ($.ua.device.type !== 'mobile') {
                $('#articleTagsSelectedPanel').
                  css('left', ($('.post .tags-selected').width() + 10) + 'px')
              }
              $('#articleTags').completed('updateData', result.tags)
            } else {
              console.log(result)
            }
          },
        })
      },
    })
  },
}

AddArticle.init()
