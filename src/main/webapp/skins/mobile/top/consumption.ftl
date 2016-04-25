<#include "macro-top.ftl">
<@top "consumption">
<h2 class="ranking-title"><span class="ft-green">♥</span> ${consumptionLabel}${rankingLabel}</h2>
<div class="list top">
    <ul>
        <#list topConsumptionUsers as user>
        <li>
            <div class="fn-flex">
                <a rel="nofollow"
                   href="/member/${user.userName}" 
                   title="${user.userName}"><div class="avatar" style="background-image:url('${user.userAvatarURL}-64.jpg?${user.userUpdateTime}')"></div></a>
                <div class="fn-flex-1">
                    <div class="fn-clear">
                        <h2 class="fn-left">
                            ${user_index + 1}.
                            <a rel="bookmark" href="/member/${user.userName}">${user.userName}</a>
                        </h2>
                        <div class="fn-right">
                            <a href="/member/${user.userName}/points">
                                ${user.userUsedPoint?c}
                            </a>
                            ~ ${yuanLabel}${user.money}
                        </div>
                    </div>

                    <div class="ft-gray">
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
                            ${symphonyLabel} ${user.userNo?c} ${numVIPLabel},
                            <#if 0 == user.userAppRole>${hackerLabel}<#else>${painterLabel}</#if>
                        </div>
                    </div>
                </div>
            </div>
        </li>
        </#list>
    </ul>
    <br/>
</div>
</@top>