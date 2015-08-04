<#include "macro-top.ftl">
<@top "balance">
<div class="list top">
    <ul>
        <#list topBalanceUsers as user>
        <li>
            <div class="fn-flex">
                <a rel="nofollow" class="responsive-hide"
                   href="/member/${user.userName}" 
                   title="${user.userName}"><img class="avatar" src="${user.userAvatarURL}-64" /></a>
                <div class="has-view fn-flex-1">
                    <h2>
                        ${user_index + 1}.
                        <a rel="bookmark" href="/member/${user.userName}">${user.userName}</a>
                    </h2>
                    <div class="ft-small">
                        <#if user.userIntro!="">
                        <div>
                            ${user.userIntro}
                        </div>
                        </#if>
                        <#if user.userURL!="">
                        <div>
                            <a target="_blank" rel="friend" href="${user.userURL?html}">${user.userURL?html}</a>
                        </div>
                        </#if>
                        <div>
                            Symphony ${user.userNo} ${numVIPLabel}, 
                            <#if 0 == user.userAppRole>${hackerLabel}<#else>${painterLabel}</#if>
                        </div>
                    </div>
                    <div class="cmts" title="${cmtLabel}">
                        <a href="/member/${user.userName}/points" title="${user.userPoint?c}">
                            <#if 0 == user.userAppRole>
                            0x${user.userPointHex}
                            <#else>
                            <div class="painter-point" style="background-color: #${user.userPointCC}"></div>
                            </#if>
                        </a>
                    </div>
                </div>
            </div>
        </li>
        </#list>
    </ul>
    &nbsp;
</div>
</@top>