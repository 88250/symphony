<#include "macro-home.ftl">
<#include "../macro-pagination.ftl">
<@home "${type}">
<div class="tabs-sub fn-clear">
    <a href="${servePath}/member/${user.userName}/watching/articles"<#if type == "watchingUsers"> class="current"</#if>>${watchingArticlesLabel}</a>
    <a href="${servePath}/member/${user.userName}/following/users"<#if type == "followingUsers"> class="current"</#if>>${followingUsersLabel}</a>
    <a href="${servePath}/member/${user.userName}/following/tags"<#if type == "followingTags"> class="current"</#if>>${followingTagsLabel}</a>
    <a href="${servePath}/member/${user.userName}/following/articles"<#if type == "followingArticles"> class="current"</#if>>${followingArticlesLabel}</a>
    <a href="${servePath}/member/${user.userName}/followers"<#if type == "followers"> class="current"</#if>>${followersLabel} &nbsp;<span class="count">${paginationRecordCount}</span></a>
</div>
<#if 0 == user.userFollowerStatus || (isLoggedIn && ("adminRole" == currentUser.userRole || currentUser.userName == user.userName))>
<div class="list follow">
    <#if userHomeFollowerUsers?size == 0>
        <p class="ft-center ft-gray home-invisible">${chickenEggLabel}</p>
    </#if>
    <ul>
        <#list userHomeFollowerUsers as follower>
        <li<#if !(paginationPageCount?? && paginationPageCount!=0 && paginationPageCount!=1) && follower_index == userHomeFollowerUsers?size - 1>
            class="last"</#if>>
            <div class="fn-flex">
                <a rel="nofollow" class="tooltipped tooltipped-se fn-left" aria-label="${follower.userName} <#if follower.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>" 
                   href="${servePath}/member/${follower.userName}" >
                    <div class="avatar" style="background-image:url('${follower.userAvatarURL}')"></div>
                </a>
                <div class="fn-flex-1">
                    <h2 class="fn-inline">
                        <a rel="nofollow" href="${servePath}/member/${follower.userName}" ><#if follower.userNickname != ''>${follower.userNickname}<#else>${follower.userName}</#if></a>
                    </h2>
                    <#if follower.userNickname != ''>
                    <a class='ft-fade' rel="nofollow" href="${servePath}/member/${follower.userName}" >${follower.userName}</a>
                    </#if>
                    <#if isLoggedIn && (userName != follower.userName)> 
                    <#if follower.isFollowing>
                    <button class="fn-right mid" onclick="Util.unfollow(this, '${follower.oId}', 'user')"> 
                        ${unfollowLabel}
                    </button>
                    <#else>
                    <button class="fn-right mid" onclick="Util.follow(this, '${follower.oId}', 'user')"> 
                        ${followLabel}
                    </button>
                    </#if>
                    </#if>
                    <div>
                        <#if follower.userArticleCount == 0>
                        <#if follower.userURL != "">
                        <a class="ft-gray" target="_blank" rel="friend" href="${follower.userURL?html}">${follower.userURL?html}</a>
                        <#else>
                        <span class="ft-gray">${symphonyLabel}</span>
                        ${follower.userNo?c}
                        <span class="ft-gray">${numVIPLabel}</span>
                        </#if>
                        <#else>
                        <span class="ft-gray">${articleLabel}</span> ${follower.userArticleCount?c} &nbsp;
                        <span class="ft-gray">${tagLabel}</span> ${follower.userTagCount?c}
                        </#if>
                    </div>
                </div>
            </div>
        </li>
        </#list>
    </ul>
</div>
<@pagination url="${servePath}/member/${user.userName}/followers"/>
<#else>
<p class="ft-center ft-gray home-invisible">${setinvisibleLabel}</p>
</#if>
</@home>