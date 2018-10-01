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
 * @fileoverview settings.
 *
 * @author <a href="http://vanessa.b3log.org">Liyuan Li</a>
 * @author <a href="http://88250.b3log.org">Liang Ding</a>
 * @author <a href="http://zephyr.b3log.org">Zephyr</a>
 * @version 1.26.0.5, Aug 29, 2018
 */

/**
 * @description Settings function
 * @static
 */
var Settings = {
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
        reportDataType: 2,
        reportType: $('input[name=report]:checked').val(),
        reportMemo: $('#reportTextarea').val()
      }),
      complete: function (result) {
        $btn.removeAttr('disabled').css('opacity', '1')
        if (result.responseJSON.sc === 0) {
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
   * 获取邮箱验证码
   * @param csrfToken
   */
  getEmailCaptcha: function (csrfToken) {
    $('#emailGetBtn').attr('disabled', 'disabled').css('opacity', '0.3')
    $.ajax({
      url: Label.servePath + '/settings/email/vc',
      type: 'POST',
      headers: {'csrfToken': csrfToken},
      data: JSON.stringify({
        userEmail: $('#emailInput').val(),
        captcha: $('#emailVerify').val(),
      }),
      success: function (result) {
        if (result.sc) {
          $('#emailInput').prop('disabled', true)
          $('.home-account__captch').hide()
          $('#emailCodePanel').show()
          $('#emailCode').show().focus()
          $('#emailSubmitBtn').show()
          $('#emailGetBtn').hide()
        }
        Util.alert(result.msg)
        $('#emailGetBtn').removeAttr('disabled').css('opacity', '1')
      },
    })
  },
  /**
   * 更新邮箱
   */
  updateEmail: function (csrfToken) {
    $('#emailSubmitBtn').attr('disabled', 'disabled').css('opacity', '0.3')
    $.ajax({
      url: Label.servePath + '/settings/email',
      type: 'POST',
      headers: {'csrfToken': csrfToken},
      data: JSON.stringify({
        userEmail: $('#emailInput').val(),
        captcha: $('#emailCode').val(),
      }),
      success: function (result) {
        if (result.sc) {
          $('.home-account__captch').show()
          $('#emailVerify').val('')
          $('#emailCodePanel').hide()
          $('#emailCode').val('')
          $('#emailSubmitBtn').hide()
          $('#emailGetBtn').show()
          $('#emailInput').prop('disabled', false)
          $('.home-account__captch img').click()
          Util.alert(Label.updateSuccLabel)
        } else {
          if (result.code === 1) {
            $('.home-account__captch').show()
            $('#emailVerify').val('')
            $('#emailCodePanel').hide()
            $('#emailCodePanel').hide()
            $('#emailSubmitBtn').hide()
            $('#emailGetBtn').show()
            $('#emailInput').prop('disabled', false)
            $('.home-account__captch img').click()
          }
          Util.alert(result.msg)
        }
        $('#emailSubmitBtn').removeAttr('disabled').css('opacity', '1')
      },
    })
  },
  /**
   * 个人主页滚动固定
   */
  homeScroll: function () {
    $('.nav-tabs').html($('.home-menu').html());
    $('.nav').css({
      'position': 'fixed',
      'box-shadow': '0 1px 2px rgba(0,0,0,.2)'
    });
    $('.main').css('paddingTop', '68px');
  },
  /**
   * 通知页面侧边栏滚动固定
   */
  notiScroll: function () {
    var $side = $('#side'),
      width = $side.width(),
      maxScroll = $('.small-tips').closest('.module').length === 1 ? 109 + $('.small-tips').closest('.module').height() : 89;
    $('.side.fn-none').height($side.height());
    $(window).scroll(function () {
      if ($(window).scrollTop() > maxScroll) {
        $side.css({
          position: 'fixed',
          width: width + 'px',
          top: 0,
          right: $('.wrapper').css('margin-right')
        });

        $('.side.fn-none').show();
        $('.small-tips').closest('.module').hide();
      } else {
        $side.removeAttr('style');

        $('.side.fn-none').hide();
        $('.small-tips').closest('.module').show();
      }
    });
  },
  /**
   * 有代码片段时，需要进行高亮
   * @returns {Boolean}
   */
  initHljs: function () {
    if ($('pre code').length === 0 || Label.markedAvailable) {
      return false;
    }
    $.ajax({
      method: "GET",
      url: Label.servePath + "/js/lib/highlight/highlight.pack.js",
      dataType: "script"
    }).done(function () {
      $('pre code').each(function (i, block) {
        hljs.highlightBlock(block);
        $(this).css('max-height', $(window).height() - 68);
      });
    });
  },
  /**
   * 个人设置预览
   */
  preview: function (it) {
    if ($('#homeSidePanel').css('display') === 'block') {
      $('#homeSidePanel').hide();
      $(it).text(Label.previewLabel);
    } else {
      $('#homeSidePanel').show();
      $('#userNicknameDom').text($('#userNickname').val());
      $('#userTagsDom').text($('#userTags').val());
      $('#userURLDom').text($('#userURL').val()).attr('href', $('#userURL').val());
      $('#userIntroDom').text($('#userIntro').val());

      $(it).text(Label.unPreviewLabel);
    }
  },
  /**
   * 初始化个人设置中的头像图片上传.
   *
   * @returns {Boolean}
   */
  initUploadAvatar: function (params, succCB, succCBQN) {
    var ext = "";
    if ("" === params.qiniuUploadToken) { // 说明没有使用七牛，而是使用本地
      $('#' + params.id).fileupload({
        acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
        maxFileSize: parseInt(params.maxSize),
        multipart: true,
        pasteZone: null,
        dropZone: null,
        url: Label.servePath + "/upload",
        add: function (e, data) {
          ext = data.files[0].type.split("/")[1];

          if (window.File && window.FileReader && window.FileList && window.Blob) {
            var reader = new FileReader();
            reader.readAsArrayBuffer(data.files[0]);
            reader.onload = function (evt) {
              var fileBuf = new Uint8Array(evt.target.result.slice(0, 11));
              var isImg = isImage(fileBuf);

              if (!isImg) {
                Util.alert("Image only~");

                return;
              }

              if (evt.target.result.byteLength > 1024 * 1024) {
                Util.alert("This image is too large (max 1M)");

                return;
              }

              data.submit();
            };
          } else {
            data.submit();
          }
        },
        formData: function (form) {
          var data = form.serializeArray();
          return data;
        },
        submit: function (e, data) {
        },
        done: function (e, data) {
          var qiniuKey = data.result.key;
          if (!qiniuKey) {
            Util.alert("Upload error");
            return;
          }

          succCB(data);
        },
        fail: function (e, data) {
          Util.alert("Upload error: " + data.errorThrown);
        }
      }).on('fileuploadprocessalways', function (e, data) {
        var currentFile = data.files[data.index];
        if (data.files.error && currentFile.error) {
          Util.alert(currentFile.error);
        }
      });
    } else {
      $('#' + params.id).fileupload({
        acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
        maxFileSize: parseInt(params.maxSize),
        multipart: true,
        pasteZone: null,
        dropZone: null,
        url: "https://up.qbox.me/",
        add: function (e, data) {
          ext = data.files[0].type.split("/")[1];

          if (window.File && window.FileReader && window.FileList && window.Blob) {
            var reader = new FileReader();
            reader.readAsArrayBuffer(data.files[0]);
            reader.onload = function (evt) {
              var fileBuf = new Uint8Array(evt.target.result.slice(0, 11));
              var isImg = isImage(fileBuf);

              if (!isImg) {
                Util.alert("Image only~");

                return;
              }

              if (evt.target.result.byteLength > 1024 * 1024) {
                Util.alert("This image is too large (max 1M)");

                return;
              }

              data.submit();
            };
          } else {
            data.submit();
          }
        },
        formData: function (form) {
          var data = form.serializeArray();
          data.push({name: 'token', value: params.qiniuUploadToken});
          data.push({name: 'key', value: 'avatar/' + params.userId + "_" + new Date().getTime() + "." + ext});

          console.log(data);

          return data;
        },
        submit: function (e, data) {
        },
        done: function (e, data) {
          var qiniuKey = data.result.key;
          if (!qiniuKey) {
            Util.alert("Upload error");
            return;
          }

          succCBQN(data);
        },
        fail: function (e, data) {
          Util.alert("Upload error: " + data.errorThrown);
        }
      }).on('fileuploadprocessalways', function (e, data) {
        var currentFile = data.files[data.index];
        if (data.files.error && currentFile.error) {
          Util.alert(currentFile.error);
        }
      });
    }
  },
  /**
   * 数据导出.
   */
  exportPosts: function () {
    $.ajax({
      url: Label.servePath + "/export/posts",
      type: "POST",
      cache: false,
      success: function (result, textStatus) {
        if (!result.sc) {
          Util.alert("TBD: V, tip display it....");

          return;
        }

        window.open(result.url);
      }
    });
  },
  /**
   * @description 修改地理位置状态
   * @param {type} csrfToken CSRF token
   */
  changeGeoStatus: function (csrfToken) {
    var requestJSONObject = {
      "userGeoStatus": $('#geoStatus').val()
    };

    $.ajax({
      url: Label.servePath + "/settings/geo/status",
      type: "POST",
      headers: {"csrfToken": csrfToken},
      cache: false,
      data: JSON.stringify(requestJSONObject),
      success: function (result, textStatus) {
        console.log(result);
      }
    });
  },
  /**
   * @description 积分转账
   * @argument {String} csrfToken CSRF token
   */
  pointTransfer: function (csrfToken) {
    if (Validate.goValidate({
      target: $('#pointTransferTip'),
      data: [{
        "target": $("#pointTransferUserName"),
        "type": "string",
        "max": 256,
        "msg": Label.invalidUserNameLabel
      }, {
        "target": $("#pointTransferAmount"),
        "type": "string",
        'max': 50,
        "msg": Label.amountNotEmpty
      }]
    })) {
      var requestJSONObject = {
        "userName": $("#pointTransferUserName").val(),
        "amount": $("#pointTransferAmount").val(),
        "memo": $("#pointTransferMemo").val()
      };

      $.ajax({
        url: Label.servePath + "/point/transfer",
        type: "POST",
        headers: {"csrfToken": csrfToken},
        cache: false,
        data: JSON.stringify(requestJSONObject),
        beforeSend: function () {
          $("#pointTransferTip").removeClass("succ").removeClass("error").html("");
        },
        error: function (jqXHR, textStatus, errorThrown) {
          Util.alert(errorThrown);
        },
        success: function (result, textStatus) {
          if (result.sc) {
            $("#pointTransferTip").addClass("succ").removeClass("error").html('<ul><li>' + Label.transferSuccLabel + '</li></ul>');
            $("#pointTransferUserName").val('');
            $("#pointTransferAmount").val('');
            $("#pointTransferMemo").val('');
          } else {
            $("#pointTransferTip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');
          }

          $("#pointTransferTip").show();

          setTimeout(function () {
            $("#pointTransferTip").hide();
          }, 2000);
        }
      });
    }
  },
  /**
   * @description 积分兑换邀请码
   * @argument {String} csrfToken CSRF token
   */
  pointBuyInvitecode: function (csrfToken) {
    var requestJSONObject = {};

    $.ajax({
      url: Label.servePath + "/point/buy-invitecode",
      type: "POST",
      headers: {"csrfToken": csrfToken},
      cache: false,
      data: JSON.stringify(requestJSONObject),
      beforeSend: function () {
        $("#pointBuyInvitecodeTip").removeClass("succ").removeClass("error").html("");
      },
      error: function (jqXHR, textStatus, errorThrown) {
        Util.alert(errorThrown);
      },
      success: function (result, textStatus) {
        if (result.sc) {
          $(".list ul").prepend('<li class="content-reset"><code>' + result.msg.split(' ')[0] + '</code>' + result.msg.substr(16) + '</li>');
        } else {
          $("#pointBuyInvitecodeTip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');
        }
        $("#pointBuyInvitecodeTip").show();
      }
    });
  },
  /**
   * @description 查询邀请码状态
   * @param {String} csrfToken CSRF token
   * @returns {undefined}
   */
  queryInvitecode: function (csrfToken) {
    var requestJSONObject = {
      invitecode: $('#invitecode').val()
    };

    $.ajax({
      url: Label.servePath + "/invitecode/state",
      type: "POST",
      headers: {"csrfToken": csrfToken},
      cache: false,
      data: JSON.stringify(requestJSONObject),
      beforeSend: function () {
        $("#invitecodeStateTip").removeClass("succ").removeClass("error").html("");
      },
      error: function (jqXHR, textStatus, errorThrown) {
        Util.alert(errorThrown);
      },
      success: function (result, textStatus) {
        switch (result.sc) {
          case -1:
          case 0:
          case 2:
            $("#invitecodeStateTip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');

            break;
          case 1:
            $("#invitecodeStateTip").addClass("succ").removeClass("error").html('<ul><li>' + result.msg + '</li></ul>');

            break;
          default:
            $("#invitecodeStateTip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');
        }
        S
        $("#invitecodeStateTip").show();
      }
    });
  },
  /**
   * @description 更新 settings 页面数据.
   * @argument {String} csrfToken CSRF token
   */
  update: function (type, csrfToken) {
    var requestJSONObject = {};

    switch (type) {
      case "profiles":
        requestJSONObject = this._validateProfiles();

        break;
      case "password":
        requestJSONObject = this._validatePassword();

        break;
      case "privacy":
        requestJSONObject = {
          userArticleStatus: $("#userArticleStatus").prop("checked"),
          userCommentStatus: $("#userCommentStatus").prop("checked"),
          userFollowingUserStatus: $("#userFollowingUserStatus").prop("checked"),
          userFollowingTagStatus: $("#userFollowingTagStatus").prop("checked"),
          userFollowingArticleStatus: $("#userFollowingArticleStatus").prop("checked"),
          userWatchingArticleStatus: $("#userWatchingArticleStatus").prop("checked"),
          userFollowerStatus: $("#userFollowerStatus").prop("checked"),
          userBreezemoonStatus: $("#userBreezemoonStatus").prop("checked"),
          userPointStatus: $("#userPointStatus").prop("checked"),
          userOnlineStatus: $("#userOnlineStatus").prop("checked"),
          userJoinPointRank: $("#joinPointRank").prop("checked"),
          userJoinUsedPointRank: $("#joinUsedPointRank").prop("checked"),
          userUAStatus: $("#userUAStatus").prop("checked")
        };

        break;
      case "function":
        requestJSONObject = {
          userListPageSize: $("#userListPageSize").val(),
          userCommentViewMode: $("#userCommentViewMode").val(),
          userAvatarViewMode: $("#userAvatarViewMode").val(),
          userListViewMode: $("#userListViewMode").val(),
          userNotifyStatus: $('#userNotifyStatus').prop("checked"),
          userSubMailStatus: $('#userSubMailStatus').prop("checked"),
          userKeyboardShortcutsStatus: $('#userKeyboardShortcutsStatus').prop("checked"),
          userReplyWatchArticleStatus: $('#userReplyWatchArticleStatus').prop("checked"),
          userForwardPageStatus: $('#userForwardPageStatus').prop("checked")
        };

        break;
      case "emotionList":
        requestJSONObject = this._validateEmotionList();

        break;
      case "i18n":
        requestJSONObject = {
          userLanguage: $("#userLanguage").val(),
          userTimezone: $("#userTimezone").val()
        };

        break;
      case "username":
        requestJSONObject = {
          userName: $("#newUsername").val()
        };

        break;
      case "deactivate":
        break;
      default:
        console.log("update settings has no type");
    }

    if (!requestJSONObject) {
      return false;
    }

    $.ajax({
      url: Label.servePath + "/settings/" + type,
      type: "POST",
      headers: {"csrfToken": csrfToken},
      cache: false,
      data: JSON.stringify(requestJSONObject),
      beforeSend: function () {
        $("#" + type.replace(/\//g, "") + "Tip").removeClass("succ").removeClass("error").html("");
      },
      error: function (jqXHR, textStatus, errorThrown) {
        Util.alert(errorThrown);
      },
      success: function (result, textStatus) {
        if (result.sc) {
          $("#" + type.replace(/\//g, "") + "Tip").addClass("succ").removeClass("error")
            .html('<ul><li>' + Label.updateSuccLabel + '</li></ul>').show();
          if (type === 'profiles') {
            $('#userNicknameDom').text(requestJSONObject.userNickname);
            $('#userTagsDom').text(requestJSONObject.userTags);
            $('#userURLDom').text(requestJSONObject.userURL).attr('href', requestJSONObject.userURL);
            $('#userIntroDom').text(requestJSONObject.userIntro);

            return;
          }
        } else {
          $("#" + type.replace(/\//g, "") + "Tip").addClass("error").removeClass("succ").html('<ul><li>' + result.msg + '</li></ul>');
        }

        $("#" + type.replace(/\//g, "") + "Tip").show();

        setTimeout(function () {
          $("#" + type.replace(/\//g, "") + "Tip").hide();

          if (type === 'i18n') {
            window.location.reload();
          }

          if (type === 'deactivate') {
            window.location.href = Label.servePath;
          }
        }, 5000);
      }
    });
  },
  /**
   * @description 需要在上传完成后调用该函数来更新用户头像数据.
   * @argument {String} csrfToken CSRF token
   */
  updateAvatar: function (csrfToken) {
    var requestJSONObject = {
      userAvatarURL: $("#avatarURL").data("imageurl"),
    };

    $.ajax({
      url: Label.servePath + "/settings/avatar",
      type: "POST",
      headers: {"csrfToken": csrfToken},
      cache: false,
      data: JSON.stringify(requestJSONObject),
      beforeSend: function () {
      },
      error: function (jqXHR, textStatus, errorThrown) {
        Util.alert(errorThrown);
      },
      success: function (result, textStatus) {
        if (result.sc) {
          $('#avatarURLDom, .user-nav .avatar-small').attr('style', 'background-image:url(' + requestJSONObject.userAvatarURL + ')');
        }
      }
    });
  },
  /**
   * @description settings 页面 profiles 数据校验
   * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
   */
  _validateProfiles: function () {
    if (Validate.goValidate({
      target: $('#profilesTip'),
      data: [{
        "target": $("#userNickname"),
        "type": "string",
        "min": 0,
        "max": 20,
        "msg": Label.invalidUserNicknameLabel
      }, {
        "target": $("#userTags"),
        "type": "string",
        "min": 0,
        "max": 255,
        "msg": Label.tagsErrorLabel
      }, {
        "target": $("#userURL"),
        "type": "string",
        "min": 0,
        "max": 255,
        "msg": Label.invalidUserURLLabel
      }, {
        "target": $("#userIntro"),
        "type": "string",
        "min": 0,
        "max": 255,
        "msg": Label.invalidUserIntroLabel
      }]
    })) {
      return {
        userNickname: $("#userNickname").val().replace(/(^\s*)|(\s*$)/g, ""),
        userTags: $("#userTags").val().replace(/(^\s*)|(\s*$)/g, ""),
        userURL: $("#userURL").val().replace(/(^\s*)|(\s*$)/g, ""),
        userIntro: $("#userIntro").val().replace(/(^\s*)|(\s*$)/g, "")
      };
    } else {
      return false;
    }
  },
  /**
   * @description settings 页面密码校验
   * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
   */
  _validatePassword: function () {
    var pwdVal = $("#pwdOld").val(),
      newPwdVal = $("#pwdNew").val();
    if (Validate.goValidate({
      target: $('#passwordTip'),
      data: [{
        "target": $("#pwdNew"),
        "type": "password",
        "msg": Label.invalidPasswordLabel
      }, {
        "target": $("#pwdRepeat"),
        "type": "password",
        "oranginal": $('#pwdNew'),
        "msg": Label.confirmPwdErrorLabel
      }]
    })) {
      if (newPwdVal !== $("#pwdRepeat").val()) {
        return false;
      }
      var data = {};
      data.userPassword = calcMD5(pwdVal);
      data.userNewPassword = calcMD5(newPwdVal);
      return data;
    }
    return false;
  },
  /**
   * @description settings 页面表情校验（不知道有啥可校验的，暂不做校验）
   * @returns {boolean/obj} 当校验不通过时返回 false，否则返回校验数据值。
   */
  _validateEmotionList: function () {
    return {
      emotions: $("#emotionList").val()
    };
  },
  /**
   * @description 标记所有消息通知为已读状态.
   */
  makeAllNotificationsRead: function () {
    $.ajax({
      url: Label.servePath + "/notifications/all-read",
      type: "GET",
      cache: false,
      success: function (result, textStatus) {
        if (result.sc) {
          window.location.reload();
        }
      }
    });
  },
  /**
   * @description 删除消息.
   */
  removeNotifications: function (type) {
    $.ajax({
      url: Label.servePath + '/notifications/remove/' + type,
      type: 'GET',
      cache: false,
      success: function (result) {
        if (result.sc) {
          location.reload()
        }
      }
    })
    return false;
  },
  /**
   * @description 设置常用表情点击事件绑定.
   */
  initFunction: function () {
    $("#emojiGrid img").click(function () {
      var emoji = $(this).attr('alt');
      if ($("#emotionList").val().indexOf(emoji) !== -1) {
        return;
      }
      if ($("#emotionList").val() !== "") {
        $("#emotionList").val($("#emotionList").val() + "," + emoji);
      } else {
        $("#emotionList").val(emoji);
      }
    });
  },
  /**
   * 个人主页初始化
   */
  initHome: function () {
    if (Label.type === 'commentsAnonymous' || 'comments' === Label.type) {
      Settings.initHljs();
    }

    $('#reportDialog').dialog({
      'width': $(window).width() > 500 ? 500 : $(window).width() - 50,
      'height': 365,
      'modal': true,
      'hideFooter': true,
    })

    if ($.ua.device.type !== 'mobile') {
      Settings.homeScroll();
    } else {
      return
    }

    $.pjax({
      selector: 'a',
      container: '#home-pjax-container',
      show: '',
      cache: false,
      storage: true,
      titleSuffix: '',
      filter: function (href) {
        return 0 > href.indexOf(Label.servePath + '/member/' + Label.userName);
      },
      callback: function (status) {
        switch (status.type) {
          case 'success':
          case 'cache':
            $('.home-menu a').removeClass('current');
            switch (location.pathname) {
              case '/member/' + Label.userName:
              case '/member/' + Label.userName + '/comments':
                Settings.initHljs();
              case '/member/' + Label.userName + '/articles/anonymous':
              case '/member/' + Label.userName + '/comments/anonymous':
                Settings.initHljs();
                $('.home-menu a:eq(0)').addClass('current');
                break;
              case '/member/' + Label.userName + '/watching/articles':
              case '/member/' + Label.userName + '/following/users':
              case '/member/' + Label.userName + '/following/tags':
              case '/member/' + Label.userName + '/following/articles':
              case '/member/' + Label.userName + '/followers':
                $('.home-menu a:eq(1)').addClass('current');
                break;
              case '/member/' + Label.userName + '/breezemoons':
                $('.home-menu a:eq(1)').addClass('current');
                Breezemoon.init();
                break;
              case '/member/' + Label.userName + '/points':
                $('.home-menu a:eq(2)').addClass('current');
                break;
            }
          case 'error':
            break;
          case 'hash':
            break;
        }
        $('.nav-tabs').html($('.home-menu').html());
        Util.parseMarkdown();
      }
    });
    NProgress.configure({showSpinner: false});
    $('#home-pjax-container').bind('pjax.start', function () {
      NProgress.start();
    });
    $('#home-pjax-container').bind('pjax.end', function () {
      NProgress.done();
    });
  }
};