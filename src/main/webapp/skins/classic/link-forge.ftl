<#include "macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index${miniPostfix}.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
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
                            <ul class="tags fn-clear">
                                <#list domain.domainTags as tag>
                                <li>
                                    <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                </li>
                                <li>
                                    <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                </li>
                                <li>
                                    <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                </li>
                                <li>
                                    <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
                                </li>
                                <li>
                                    <a class="tag" rel="nofollow" href="${servePath}/tag/${tag.tagTitle?url('utf-8')}">${tag.tagTitle}</a>
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
                        'max-height': '100px',
                        'overflow': 'hidden'
                    });
                    return false;
                }
                $panel.css({
                    'max-height': 'inherit',
                    'overflow': 'inherit'
                });
            };
        </script>
    </body>
</html>
