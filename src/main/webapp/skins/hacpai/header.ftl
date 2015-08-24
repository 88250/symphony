<div class="nav">
    <div class="logo-wrap">
        <img src="${staticServePath}/skins/hacpai/static/images/logo.png"/>
        <span class="fn-none">程序员和设计师的天堂</span>
    </div>
    <ul class="fn-clear fn-list">
        <li class="current">
            <h1><a rel="nofollow" href="/">首页</a></h1>
        </li>
        <li>
            <h1><a rel="nofollow" href="/">我是画家</a></h1>
        </li>
        <li>
            <h1><a rel="nofollow" href="/">我是黑客</a></h1>
        </li>
        <li>
            <h1><a rel="nofollow" href="/">此刻</a></h1>
        </li>
        <li>
            <h1><a rel="nofollow" href="/">活动</a></h1>
        </li>
    </ul>
    <div class="fn-right info fn-clear">
        <form target="_blank" action="http://search.b3log.org/cse/search" class="fn-clear">
            <input type="text" name="q" placeholder="随便搜点什么...">
            <input type="hidden" value="10365148342193520062" name="s">
            <input type="hidden" name="cc" value="symphony.b3log.org">
            <input type="submit" class="fn-none" value="">
            <span class="icon-search"></span>
        </form>

        <a class="icon-info" href="/notifications" title="${messageLabel}"></a>

        <#if unreadNotificationCount != 0>
        <span class="count">${unreadNotificationCount}</span>
        </#if>
        <a href="/settings">
            <img class="avatar" title="${userName}" src="${currentUser.userAvatarURL}" />
        </a>

        <ul class="fn-list">
            <li>
                <a href="${logoutURL}}">我的文章</a>
            </li>
            <li>
                <a href="${logoutURL}}">我的评价</a>
            </li>
            <li>
                <a href="${logoutURL}}">我的积分</a>
            </li>
            <li>
                <a href="${logoutURL}}">设置</a>
            </li>
            <#if "adminRole" == userRole>
            <li>
                <a href="/admin">用户管理</a>
            </li>
            </#if>
            <li>
                <button class="btn" onclick="window.location.href = '${logoutURL}}'">用户退出</button>
            </li>
        </ul>
    </div>

</div>