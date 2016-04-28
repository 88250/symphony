<div class="nav"> 
    <div class="wrapper fn-clear">
        <div class="head-fn fn-clear">
            <h1 class="fn-left">
                <a href="/" style="background-image: url('${staticServePath}/images/hacpai.png')" alt="${symphonyLabel}" 
                   title="${symphonyLabel}" width="42" class="fn-pointer"></a>
            </h1>
            <#if esEnabled || algoliaEnabled>
            <form class="responsive-hide fn-left" target="_blank" action="/search">
                <input class="search" placeholder="Search HacPai" type="text" name="key" id="search" value="<#if key??>${key}</#if>" >
                <input type="submit" class="fn-none" value="">
            </form>
            </#if>
            <#--        
            <div class="fn-right">
                <a href="/timeline" class="icon-clock last" title="${timelineLabel}"></a>
                <#if isLoggedIn>
                <a href="/city/my" class="icon-compass" title="${sameCityLabel}"></a>
                </#if>
                <a href="/hot" class="icon-refresh" title="${recentArticleLabel}"></a>
            </div> -->
        </div>

        <div class="fn-clear user-nav">
            <#if isLoggedIn>
            <#if "adminRole" == userRole>
            <a href="/admin" title="${adminLabel}" class="last icon-userrole"></a>
            </#if>
            <a href="/member/${currentUser.userName}" title="Home" class="<#if 'adminRole' != userRole>last </#if>nav-avatar">
                <span class="avatar-small" style="background-image:url('${currentUser.userAvatarURL}-64.jpg?${currentUser.userUpdateTime?c}')"></span>
            </a>
            <a href="/activities" title="${activityLabel}" class="icon-flag"></a>
            <a href="/pre-post" title="${addArticleLabel}" 
               class="icon-addfile responsive-show"></a>
            <a id="aNotifications" class="<#if unreadNotificationCount == 0>no-msg<#else>msg</#if>" href="/notifications" title="${messageLabel}">${unreadNotificationCount}</a>
            <#else>
            <a id="aRegister" href="javascript:Util.goRegister()" class="last ft-blue unlogin" 
               title="${registerLabel}">${registerLabel}</a>
            <a href="javascript: Util.showLogin();" title="${loginLabel}" class="unlogin">${loginLabel}</a>
            <div class="form fn-none">
                <table cellspacing="0" cellpadding="0">
                    <tr>
                        <td width="40">
                            <label for="nameOrEmail">${accountLabel}</label>
                        </td>
                        <td>
                            <input id="nameOrEmail" type="text" placeholder="${nameOrEmailLabel}" />
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <label for="loginPassword">${passwordLabel}</label>
                        </td>
                        <td>
                            <input type="password" id="loginPassword" />
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2" align="right">
                            <div id="loginTip" class="tip"></div><br/>
                            <button class="info" onclick="window.location.href = '${servePath}/forget-pwd'">${forgetPwdLabel}</button>
                            <button class="red" onclick="Util.login()">${loginLabel}</button>
                        </td>
                    </tr>
                </table>
            </div>
            </#if>
        </div>
    </div>
</div>
