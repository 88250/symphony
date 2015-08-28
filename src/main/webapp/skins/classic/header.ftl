<div class="nav"> 
    <div class="wrapper">
        <h1><a rel="nofollow" href="/">Sym</a></h1>
        <form target="_blank" action="http://search.b3log.org/cse/search">
            <span class="icon icon-search"></span>
            <input class="search" type="text" name="q">
            <input type="hidden" value="10365148342193520062" name="s">
            <input type="hidden" name="cc" value="symphony.b3log.org">
            <input type="submit" class="fn-none" value="">
        </form>
        <div class="fn-flex-1 tags responsive-hide">
            <#list navTrendTags as trendTag>
            <a rel="tag" href="/tags/${trendTag.tagTitle?url('UTF-8')}">${trendTag.tagTitle}</a>
            </#list>
        </div>
        <div class="fn-clear user-nav">
            <#if isLoggedIn>
            <a id="logout" href="${logoutURL}" title="${logoutLabel}" class="last icon icon-logout"></a>
            <#if "adminRole" == userRole>
            <a href="/admin" title="${adminLabel}" class="icon icon-userrole"></a>
            </#if>
            <a id="aNotifications" class="<#if unreadNotificationCount == 0>no-msg<#else>msg</#if>" href="/notifications" title="${messageLabel}">${unreadNotificationCount}</a>
            <a href="/activities" title="${activityLabel}" class="icon icon-flag"></a>
            <a href="/add-article" title="${addArticleLabel}" 
               class="icon icon-addfile"></a>
            <#else>
            <a id="aRegister" href="javascript:Util.goRegister()" class="last icon icon-register" 
               title="${registerLabel}"></a>
            <a href="javascript: Util.showLogin();" class="icon icon-login" title="${loginLabel}"></a>
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
                            <button class="red" onclick="Util.login()">${loginLabel}</button>
                        </td>
                    </tr>
                </table>
            </div>
            </#if>
        </div>
    </div>
</div>