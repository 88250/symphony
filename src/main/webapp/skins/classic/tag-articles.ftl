<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${tag.tagTitle} - ${tagLabel} - ${symphonyLabel}">
        <meta name="keywords" content="${tag.tagSeoKeywords}"/>
        <meta name="description" content="${tag.tagSeoDesc}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main tag-articles">
            <div class="wrapper">
                <div class="content" id="tag-pjax-container">
                    <#if pjax><!---- pjax {#tag-pjax-container} start ----></#if><div class="module">
                        ${tag.tagCSS}
                        <div class="article-info fn-flex">
                            <#if tag.tagIconPath != "">
                            <div class="avatar" style="background-image:url('${staticServePath}/images/tags/${tag.tagIconPath}')" alt="${tag.tagTitle}"></div>
                            </#if>

                            <div class="fn-flex-1">
                                <span class="ft-gray ft-smaller">
                                    <a pjax-title="${tag.tagTitle} - ${tagLabel} - ${symphonyLabel}" rel="tag" class="ft-gray" href="${servePath}/tag/${tag.tagURI}"><strong>${tag.tagTitle}</strong></a> &nbsp;•&nbsp;
                                    <b class="article-level<#if tag.tagReferenceCount lt 40>${(tag.tagReferenceCount/1000)?int}<#else>4</#if>">${tag.tagReferenceCount?c}</b> ${referenceLabel}  &nbsp;•&nbsp;
                                    <b class="article-level<#if tag.tagCommentCount lt 400>${(tag.tagCommentCount/100)?int}<#else>4</#if>">${tag.tagCommentCount?c}</b> ${cmtLabel}
                                </span>
                                <br/>
                                <#list tag.tagDomains as domain>
                                    <a class="tag" href="${servePath}/domain/${domain.domainURI}">${domain.domainTitle}</a>
                                </#list>
                            </div>

                            <span class="article-actions action-btns">
                                <#if isLoggedIn && isFollowing>
                                <span class="tooltipped tooltipped-n ft-red" aria-label="${unfollowLabel} ${tag.tagFollowerCount}" onclick="Util.unfollow(this, '${tag.oId}', 'tag', ${tag.tagFollowerCount})"><span class="icon-star"></span> ${tag.tagFollowerCount}</span>
                                <#else>
                                <span class="tooltipped tooltipped-n" aria-label="${followLabel} ${tag.tagFollowerCount}" onclick="Util.follow(this, '${tag.oId}', 'tag', ${tag.tagFollowerCount})"><span class="icon-star"></span> ${tag.tagFollowerCount}</span>
                                </#if>
                                <#if permissions["tagUpdateTagBasic"].permissionGrant> &nbsp;
                                <a class="tooltipped tooltipped-n" href="${servePath}/admin/tag/${tag.oId}" aria-label="${adminLabel}"><span class="icon-setting"></span></a>
                                </#if>
                            </span>
                        </div>
                        <#if tag.tagIconPath != "">
                        <div class="content-reset desc">
                            ${tag.tagDescription}
                        </div>
                        </#if>
                        <ul class="tag-desc fn-clear tag-articles-tag-desc">
                            <#list tag.tagRelatedTags as relatedTag>
                            <li>
                                <a pjax-title="${relatedTag.tagTitle} - ${tagLabel} - ${symphonyLabel}" rel="tag" href="${servePath}/tag/${relatedTag.tagURI}">
                                    <#if relatedTag.tagIconPath != "">
                                    <img src="${staticServePath}/images/tags/${relatedTag.tagIconPath}" alt="${relatedTag.tagTitle}" /></#if>
                                    ${relatedTag.tagTitle}</a>
                                <div<#if relatedTag.tagDescription == ''> style="width:auto"</#if>>
                                    <div>${relatedTag.tagDescription}</div>
                                    <span class="fn-right">
                                        <span class="ft-gray">${referenceLabel}</span> 
                                        ${relatedTag.tagReferenceCount?c} &nbsp;
                                        <span class="ft-gray">${cmtLabel}</span>
                                        ${relatedTag.tagCommentCount?c}&nbsp;
                                    </span>
                                </div>
                            </li>
                            </#list>
                        </ul>
                    </div>
                    <#if articles?size != 0>
                    <div class="module">
                        <div class="module-header fn-clear">
                            <span class="fn-right ft-fade">
                                <a pjax-Title="${tag.tagTitle} - ${tagLabel} - ${symphonyLabel}" class="<#if "" == current>ft-gray</#if>" href="${servePath}/tag/${tag.tagURI}">${defaultLabel}</a>
                                /
                                <a pjax-Title="${tag.tagTitle} - ${tagLabel} - ${symphonyLabel}" class="<#if "/hot" == current>ft-gray</#if>" href="${servePath}/tag/${tag.tagURI}/hot">${hotArticlesLabel}</a>
                                /
                                <a pjax-Title="${tag.tagTitle} - ${tagLabel} - ${symphonyLabel}" class="<#if "/good" == current>ft-gray</#if>" href="${servePath}/tag/${tag.tagURI}/good">${goodCmtsLabel}</a>
                                /
                                <a pjax-Title="${tag.tagTitle} - ${tagLabel} - ${symphonyLabel}" class="<#if "/perfect" == current>ft-gray</#if>" href="${servePath}/tag/${tag.tagURI}/perfect"><svg height="16" viewBox="3 2 11 12" width="14">${perfectIcon}</svg> ${perfectLabel}</a>
                                /
                                <a pjax-Title="${tag.tagTitle} - ${tagLabel} - ${symphonyLabel}" class="<#if "/reply" == current>ft-gray</#if>" href="${servePath}/tag/${tag.tagURI}/reply">${recentCommentLabel}</a>
                            </span>
                        </div>
                        <@list listData=articles/>
                        <@pagination url="${servePath}/tag/${tag.tagURI}${current}" pjaxTitle="${tag.tagTitle} - ${tagLabel} - ${symphonyLabel}"/>
                    </div>
                    </#if><#if pjax><!---- pjax {#tag-pjax-container} end ----></#if>
                </div> 
                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
        <script>
            <#if (isLoggedIn && !tag.isReserved) || (tag.isReserved && isAdminLoggedIn)>
            $('.person-info .btn.red').attr('onclick', 'window.location = "/post?tags=${tag.tagURI}&type=0"');
            </#if>
            (function() {
                if (!Label.userKeyboardShortcutsStatus || Label.userKeyboardShortcutsStatus === '1') {
                    return false;
                }

                $(document).bind('keyup', 'v', function assets() {
                    // listen jump hotkey h
                    Util.prevKey = 'v';
                    setTimeout(function () {
                        Util.prevKey = undefined;
                    }, 1000);
                    return false;
                }).bind('keyup', 'c', function assets() {
                      // v i 关注标签
                      if (Util.prevKey === 'v') {
                          $('.article-actions .icon-star').parent().click();
                      }
                      return false;
                }).bind('keyup', 'a', function assets() {
                      // v a 管理员编辑标签
                      if (Util.prevKey === 'v' && $('.article-actions .icon-setting').parent().length === 1) {
                          window.location = $('.article-actions .icon-setting').parent().attr('href');
                      }
                      return false;
                });
            })();

            $.pjax({
                selector: 'a',
                container: '#tag-pjax-container',
                show: '',
                cache: false,
                storage: true,
                titleSuffix: '',
                filter: function(href){
                    return 0 > href.indexOf('${servePath}/tag');
                },
                callback: function(){}
            });
            NProgress.configure({ showSpinner: false });
            $('#tag-pjax-container').bind('pjax.start', function(){
                NProgress.start();
            });
            $('#tag-pjax-container').bind('pjax.end', function(){
                NProgress.done();
            });
        </script>
    </body>
</html>
