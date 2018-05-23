<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<div class="tabs-sub fn-clear">
    <a pjax-title="${watchingArticlesLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/watching/articles"<#if type == "watchingUsers"> class="current"</#if>>${watchingArticlesLabel}</a>
    <a pjax-title="${followingUsersLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/users"<#if type == "followingUsers"> class="current"</#if>>${followingUsersLabel}</a>
    <a pjax-title="${followingTagsLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/tags"<#if type == "followingTags"> class="current"</#if>>${followingTagsLabel}</a>
    <a pjax-title="${followingArticlesLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/following/articles"<#if type == "followingArticles"> class="current"</#if>>${followingArticlesLabel}</a>
    <a pjax-title="${followersLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/followers"<#if type == "followers"> class="current"</#if>>${followersLabel}</a>
    <a pjax-title="${breezemoonLabel} - ${user.userName} - ${symphonyLabel}"  href="${servePath}/member/${user.userName}/breezemoons"<#if type == "breezemoons"> class="current"</#if>>${breezemoonLabel} &nbsp;<span class="count">${paginationRecordCount}</span></a>
</div>
<#if 0 == user.userFollowerStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
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
                <#if permissions["commonAddBreezemoon"].permissionGrant>
                <div class="list">
                    <ul class="form">
                        <li>
                            <input id="breezemoonInput" type="text">
                            <button onclick="Breezemoon.add()" id="breezemoonBtn"
                                    class="absolute">${breezemoonLabel}</button>
                        </li>
                    </ul>
                </div>
                </#if>
                <div class="list">
                    <ul id="breezemoonList">
                        <#list watchingBreezemoons as item>
                            <li class="fn-flex" id="${item.oId}">
                                <a class="tooltipped tooltipped-n avatar"
                                   style="background-image:url('${item.breezemoonAuthorThumbnailURL48}')"
                                   rel="nofollow" href="${servePath}/member/${item.breezemoonAuthorName}"
                                   aria-label="Vanessa">
                                </a>
                                <div class="fn-flex-1">
                                    <div class="ft-fade">
                                        <a href="${servePath}/member/${item.breezemoonAuthorName}">${item.breezemoonAuthorName}</a>
                                        •
                                        <span class="ft-smaller">
                                            ${item.timeAgo}
                                        </span>
                                        <span class="ft-smaller"
                                              data-ua="${item.breezemoonUA}">via Android</span>

                                        <div class="fn-right">
                                             <#if isLoggedIn && permissions["commonAddBreezemoon"].permissionGrant &&
                                             item.breezemoonAuthorName == currentUser.userName>
                                            <span class="tooltipped tooltipped-n ft-red rm" aria-label="${removeLabel}">
                                                <svg><use xlink:href="#remove"></use></svg>
                                            </span>
                                            &nbsp;&nbsp;
                                            <span class="tooltipped tooltipped-n ft-a-title edit"
                                                  aria-label="${editLabel}">
                                                <svg><use xlink:href="#edit"></use></svg>
                                            </span>
                                            &nbsp;&nbsp;
                                             </#if>
                                            <span class="tooltipped tooltipped-n ft-a-title copy"
                                                  aria-label="${copyLabel}">
                                                <svg><use xlink:href="#articles"></use></svg>
                                            </span>
                                            <textarea style="position: fixed;left: -10000px;">${servePath}/watch/breezemoons#${item.oId}</textarea>
                                        </div>
                                    </div>
                                    <div class="content-reset">${item.breezemoonContent}</div>
                                </div>
                            </li>
                        </#list>
                    </ul>
                </div>
            </div><#if pjax><!---- pjax {#watch-pjax-container} end ----></#if>
        </div>
    </div>
</div>
<@pagination url="${servePath}/member/${user.userName}/followers" pjaxTitle="${followersLabel} - ${user.userName} - ${symphonyLabel}"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>