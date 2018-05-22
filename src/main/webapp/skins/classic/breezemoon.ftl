<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "common/title-icon.ftl">
<!DOCTYPE html>
<html lang="zh-cmn-Hans">
<head>
<@head title="${breezemoonLabel} - ${symphonyLabel}">
    <meta name="description" content="只与清风、明月为伴。清凉的风，明朗的月。"/>
</@head>
    <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}"/>
</head>
<body>
<#include "header.ftl">
<div class="main">
    <div class="wrapper">
        <div class="content fn-clear" id="watch-pjax-container">
            <#if pjax><!---- pjax {#watch-pjax-container} start ----></#if>
            <div class="module">
                <div class="module-header fn-clear">
                    <span class="fn-right ft-fade">
                        <a pjax-title="${followingTagsLabel} - ${symphonyLabel}"
                           class="<#if "" == current>ft-gray</#if>" href="${servePath}/watch">${followingTagsLabel}</a>
                        /
                        <a pjax-title="${followingUsersLabel} - ${symphonyLabel}"
                           class="<#if "/users" == current>ft-gray</#if>"
                           href="${servePath}/watch/users">${followingUsersLabel}</a>
                        /
                        <a pjax-title="${followingUsersLabel} - ${symphonyLabel}"
                           class="<#if "/breezemoons" == current>ft-gray</#if>"
                           href="${servePath}/watch/breezemoons">${breezemoonLabel}</a>
                    </span>
                </div>
                <br>
                <div class="form">
                    <input id="breezemoonInput" type="text">
                    <button onclick="Breezemoon.add()" id="breezemoonBtn" class="absolute">${breezemoonLabel}</button>
                </div>
                <br>
                <div class="list">
                    <ul id="breezemoonList">
                        <#list watchingBreezemoons as item>
                            ${item}
                        </#list>
                        <li class="fn-flex" id="id">
                            <a class="tooltipped tooltipped-n avatar"
                               style="background-image:url('https://img.hacpai.com/avatar/1353745196544_1501644090048.png')"
                               rel="nofollow" href="http://localhost:8080/member/Vanessa" aria-label="Vanessa">
                            </a>
                            <div class="fn-flex-1">
                                <div class="ft-fade">
                                    <a href="">Vanessa</a>
                                    •
                                    <span class="ft-smaller">
                                        1分钟钱
                                    </span>
                                    <span class="ft-smaller"
                                          data-ua="Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Mobile Safari/537.36">via Android</span>

                                    <div class="fn-right">
                                        <span class="tooltipped tooltipped-n ft-red rm" aria-label="${removeLabel}">
                                            <svg><use xlink:href="#remove"></use></svg>
                                        </span>
                                        &nbsp;&nbsp;
                                        <span class="tooltipped tooltipped-n ft-a-title edit" aria-label="${editLabel}">
                                            <svg><use xlink:href="#edit"></use></svg>
                                        </span>
                                    </div>
                                </div>
                                <div class="content-reset">af</div>
                            </div>
                        </li>
                        <li class="fn-flex" id="id">
                            <a class="tooltipped tooltipped-n avatar"
                               style="background-image:url('https://img.hacpai.com/avatar/1353745196544_1501644090048.png')"
                               rel="nofollow" href="http://localhost:8080/member/Vanessa" aria-label="Vanessa">
                            </a>
                            <div class="fn-flex-1">
                                <div class="ft-fade">
                                    <a href="">Vanessa</a>
                                    •
                                    <span class="ft-smaller">
                                        1分钟钱
                                    </span>
                                    <span class="ft-smaller"
                                          data-ua="Mozilla/5.0 (Linux; Android 5.0; SM-G900P Build/LRX21T) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/66.0.3359.181 Mobile Safari/537.36">via Android</span>

                                    <div class="fn-right">
                                        <span class="tooltipped tooltipped-n ft-red rm" aria-label="${removeLabel}">
                                            <svg><use xlink:href="#remove"></use></svg>
                                        </span>
                                        &nbsp;&nbsp;
                                        <span class="tooltipped tooltipped-n ft-a-title edit" aria-label="${editLabel}">
                                            <svg><use xlink:href="#edit"></use></svg>
                                        </span>
                                    </div>
                                </div>
                                <div class="content-reset">af</div>
                            </div>
                        </li>
                    </ul>
                </div>
            </div><#if pjax><!---- pjax {#watch-pjax-container} end ----></#if>
        </div>
        <div class="side">
        <#include "side.ftl">
        </div>
    </div>
</div>
<#include "common/domains.ftl">
<#include "footer.ftl">
<script src="${staticServePath}/js/breezemoon${miniPostfix}.js?${staticResourceVersion}"></script>
<@listScript/>
<script>
    $.pjax({
        selector: 'a',
        container: '#watch-pjax-container',
        show: '',
        cache: false,
        storage: true,
        titleSuffix: '',
        filter: function (href) {
            return 0 > href.indexOf('${servePath}/watch')
        },
        callback: function () {
            Util.lazyLoadCSSImage()
        },
    })
    NProgress.configure({showSpinner: false})
    $('#watch-pjax-container').bind('pjax.start', function () {
        NProgress.start()
    })
    $('#watch-pjax-container').bind('pjax.end', function () {
        NProgress.done()
    })
</script>
</body>
</html>
