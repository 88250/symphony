<div class="nav">
    <div class="logo-wrap">
        <img src="${staticServePath}/skins/hacpai/static/images/logo.png"/>
        <span class="fn-none">程序员和设计师的天堂</span>
    </div>
    <ul class="fn-clear fn-list">
        <li class="current">
            <h1><a rel="nofollow" href="/">${indexLabel}</a></h1>
        </li>
        <li>
            <h1><a rel="nofollow" href="/">${iamPainterLabel}</a></h1>
        </li>
        <li>
            <h1><a rel="nofollow" href="/">${iamHackerLabel}</a></h1>
        </li>
        <li>
            <h1><a rel="nofollow" href="/timeline">${timelineLabel}</a></h1>
        </li>
        <li>
            <h1><a rel="nofollow" href="/activities">${activityLabel}</a></h1>
        </li>
    </ul>
    <div class="fn-right info fn-clear">
        <form target="_blank" action="https://search.hacpai.com/cse/search" class="fn-clear">
            <input type="text" name="q" placeholder="${searchSthLabel}">
            <input type="hidden" value="140632643792102269" name="s">
            <input type="hidden" name="cc" value="hacpai.com">
            <input type="submit" class="fn-none" value="">
            <span class="icon-search"></span>
        </form>

        <a class="icon-info" href="/notifications" title="${messageLabel}"></a>

        <#if unreadNotificationCount != 0>
        <span class="count">${unreadNotificationCount}</span>
        </#if>
        <a href="/settings">
            <img class="avatar" title="${currentUser.userName}" src="${currentUser.userAvatarURL}" />
        </a>
        <span class="ico-down" onclick="$(this).next().toggle()"></span>
        <div class="fn-none">
            <ul class="fn-list">
                <li>
                    <a href="/member/${currentUser.userName}">${myArticleLabel}</a>
                </li>
                <li>
                    <a href="/member/${currentUser.userName}/comments">${myCommentLabel}</a>
                </li>
                <li>
                    <a href="/member/${currentUser.userName}/points">${myPointLabel}</a>
                </li>
                <li>
                    <a href="/settings">${settingsLabel}</a>
                </li>
                <#if "adminRole" == userRole>
                <li>
                    <a href="/admin">${adminLabel}</a>
                </li>
                </#if>
                <li>
                    <button class="btn" onclick="window.location.href = '${logoutURL}'">${logoutLabel}</button>
                </li>
            </ul>
            <span class="list-up"></span>
            <span class="list-up-wrap"></span>
        </div>
    </div>
</div>