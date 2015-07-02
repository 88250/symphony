<#if isLoggedIn>
<div class="module">
    <div class="module-header nopanel">
        <div class="person-info">
            <div class="fn-clear">
                <div class="fn-right">
                    <a href="/settings">
                        <img class="avatar" 
                             title="${userName}" src="${currentUser.userAvatarURL}" />
                    </a>
                </div>
                <div class="fn-left">
                    <a href="/member/${currentUser.userName}">${userName}</a> 
                    <#if !isDailyCheckin> &nbsp;
                    <a class="btn small" href="/activity/daily-checkin">${activityDailyCheckinLabel}</a>
                    </#if>
                    <div class="fn-hr5"></div>
                    <a href="/add-article" title="${addArticleLabel}${articleLabel}" 
                       class="ft-red"><span class="icon icon-addfile"> ${addArticleLabel}${articleLabel}</span></a>
                    <a href="/member/${currentUser.userName}/points" class="ft-small" title="${pointLabel} ${currentUser.userPoint?c}">
                        <#if 0 == currentUser.userAppRole>
                        0x${currentUser.userPointHex}
                        <#else>
                        <div class="painter-point" style="background-color: #${currentUser.userPointCC}"></div>
                        </#if>
                    </a>
                </div>
            </div>
            <div>
                ${currentUser.userIntro}
            </div>
        </div>


        <ul class="status fn-clear">
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/following/tags'">
                <strong>${currentUser.followingTagCnt}</strong>
                <span class="ft-small">${followingTagsLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/following/users'">
                <strong>${currentUser.followingUserCnt}</strong>
                <span class="ft-small">${followingUsersLabel}</span>
            </li>
            <li class="fn-pointer" onclick="window.location.href = '/member/${currentUser.userName}/following/articles'">
                <strong>${currentUser.followingArticleCnt}</strong>
                <span class="ft-small">${followingArticlesLabel}</span>
            </li>
        </ul>
    </div> 
</div>
</#if>