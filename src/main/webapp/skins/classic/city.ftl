<#include "macro-head.ftl">
<#include "macro-list.ftl">
<#include "macro-pagination.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${city} - ${symphonyLabel}">
        <meta name="description" content="${symDescriptionLabel}"/>
        </@head>
        <link rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
    </head>
    <body>
        <#include "header.ftl">
        <div class="main">
            <div class="wrapper">
                <div class="content fn-clear">
                    <div class="module">
                        <div class="module-header fn-clear">
                            <span class="fn-right ft-fade">
                                <a class="<#if "" == current>ft-gray</#if>" href="${servePath}/city/my">${cityArticleLabel}</a>
                                /
                                <a class="<#if "/users" == current>ft-gray</#if>" href="${servePath}/city/my/users">${cityUserLabel}</a>
                            </span>
                        </div>
                        <#if "" == current && articles?size gt 0>
                        <div class="fn-clear">
                            <@list listData=articles/>
                            <@pagination url="${servePath}/city/${city?url('utf-8')}"/>
                        </div>
                        <#else>
                        <div class="no-list">
                            <#if !userGeoStatus>
                            ${cityArticlesTipLabel}
                            <#else>
                            <#if !cityFound>
                            ${geoInfoPlaceholderLabel}
                            </#if>
                            </#if>
                        </div>
                        </#if>

                        <#if "/users" == current && isLoggedIn>
                        <div class="follow list">
                            <ul>
                                <#list users as user>
                                <li>
                                    <div class="fn-flex">
                                        <a rel="nofollow ft-gray"  
                                           href="${servePath}/member/${user.userName}">
                                            <div class="avatar fn-left tooltipped tooltipped-se" 
                                                 aria-label="${user.userName} <#if user.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>" 
                                                 style="background-image:url('${user.userAvatarURL}')"></div>
                                        </a>
                                        <div class="fn-flex-1">
                                            <h2 class="fn-inline">
                                                <a rel="nofollow" href="${servePath}/member/${user.userName}" ><#if user.userNickname != ''>${user.userNickname}<#else>${user.userName}</#if></a>
                                            </h2>
                                            <#if user.userNickname != ''>
                                            <a class='ft-fade' rel="nofollow" href="${servePath}/member/${user.userName}" >${user.userName}</a>
                                            </#if>
                                            <#if isLoggedIn && (currentUser.userName != user.userName)> 
                                            <#if user.isFollowing>
                                            <button class="fn-right mid" onclick="Util.unfollow(this, '${user.oId}', 'user')"> 
                                                ${unfollowLabel}
                                            </button>
                                            <#else>
                                            <button class="fn-right mid" onclick="Util.follow(this, '${user.oId}', 'user')"> 
                                                ${followLabel}
                                            </button>
                                            </#if>
                                            </#if>
                                            <div>
                                                <#if user.userArticleCount == 0>
                                                <#if user.userURL != "">
                                                <a class="ft-gray" target="_blank" rel="friend" href="${user.userURL?html}">${user.userURL?html}</a>
                                                <#else>
                                                <span class="ft-gray">${symphonyLabel}</span>
                                                ${user.userNo?c}
                                                <span class="ft-gray">${numVIPLabel}</span>
                                                </#if>
                                                <#else>
                                                <span class="ft-gray">${articleLabel}</span> ${user.userArticleCount?c} &nbsp;
                                                <span class="ft-gray">${tagLabel}</span> ${user.userTagCount?c}
                                                </#if>
                                            </div>
                                        </div>
                                    </div>
                                </li>
                                </#list>
                            </ul>
                        </div>
                        <@pagination url="${servePath}/city/${city?url('utf-8')}/users"/>
                        </#if>
                    </div>
                    <#include "common/domains.ftl">
                </div>

                <div class="side">
                    <#include "side.ftl">
                </div>
            </div>
        </div>
        <#include "footer.ftl">
        <@listScript/>
    </body>
</html>
