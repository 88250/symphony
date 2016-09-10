<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/link-forge">
    </head>
    <body>
        <#include "header.ftl">
        <div class="link-forge-upload">
            <div class="wrapper form">
                <input type="text"/><button class="green" onclick="postLink()">${submitLabel}</button>
                <div id="uploadLinkTip" class="tip"></div>
            </div>
        </div>
        <div class="main link-forge">
            <div class="wrapper">
                <div class="content fn-clear">
                    <#list domains as domain>
                    <div class="module">
                        <div class="module-header">
                            <h2>
                                <span class="avatar-small"  style="background-image:url('http://7xjz0r.com1.z0.glb.clouddn.com/user-thumbnail.png')"></span>
                                ${domain.domainTitle}
                            </h2>
                            <a class="ft-gray fn-right" rel="nofollow" href="javascropt:void(0)" onclick="linkForgeToggle(this)">${domain.domainTags?size} Links</a>
                        </div>
                        <div class="module-panel">
                            <ul class="module-list">
                                <#list domain.domainTags as tag>
                                <li>
                                    <a class="title fn-ellipsis" rel="nofollow" href="${servePath}/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                </li>
                                </#list>
                            </ul>
                        </div>
                    </div>
                    </#list>
                </div>
                <div class="side">
                    <#include "common/person-info.ftl">
                    <div class='domains-count'>
                        Tags: <b>${domainCnt}</b><br/>
                        Links: <b>${tagCnt}</b>
                    </div>
                    <#if ADLabel!="">
                    <div class="module">
                        <div class="module-header">
                            <h2>
                                ${sponsorLabel} 
                                <a href="https://hacpai.com/article/1460083956075" class="fn-right ft-13 ft-gray" target="_blank">${wantPutOnLabel}</a>
                            </h2>
                        </div>
                        <div class="module-panel ad fn-clear">
                            ${ADLabel}
                        </div>
                    </div>
                    </#if>
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <script>
            var linkForgeToggle = function (it) {
                var $panel = $(it).closest('.module').find('.module-panel');
                if ($panel.css('overflow') !== 'hidden') {
                    $panel.css({
                        'max-height': '122px',
                        'overflow': 'hidden'
                    });
                    return false;
                }
                $panel.css({
                    'max-height': 'inherit',
                    'overflow': 'inherit'
                });
            };

            var postLink = function () {
                if (Validate.goValidate({target: $('#uploadLinkTip'),
                    data: [{
                            "target": $('.link-forge-upload input'),
                            "type": "url",
                            "msg": '${invalidUserURLLabel}'
                        }]})) {
                    $.ajax({
                        url: Label.servePath + "/forge/link",
                        type: "POST",
                        cache: false,
                        data: JSON.stringify({
                            url: $('.link-forge-upload input').val()
                        }),
                        error: function (jqXHR, textStatus, errorThrown) {
                            alert(errorThrown);
                        },
                        success: function (result, textStatus) {
                            if (result.sc) {
                                $('#uploadLinkTip').html('<ul><li>${submitSuccLabel}</li></ul>').addClass('succ');
                                 $('.link-forge-upload input').val('');
                                setTimeout(function () {
                                    $('#uploadLinkTip').html('').removeClass('succ');
                                }, 3000);
                            } else {
                                alert(result.msg);
                            }
                        }
                    });
                }
            };

            $(document).ready(function () {
                $('.link-forge-upload input').focus().keyup(function (event) {
                    if (event.which === 13) {
                        postLink();
                        return false;
                    }

                    Validate.goValidate({target: $('#uploadLinkTip'),
                        data: [{
                                "target": $('.link-forge-upload input'),
                                "type": "url",
                                "msg": '${invalidUserURLLabel}'
                            }]})
                });
            });
        </script>
    </body>
</html>
