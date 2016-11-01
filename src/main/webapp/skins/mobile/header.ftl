<div class="nav"> 
    <div class="wrapper fn-clear">
        <div class="head-fn fn-left">
            <h1>
                ${logoIcon}
            </h1>
        </div>

        <div class="fn-right user-nav">
            <#if isLoggedIn>
            <#if "adminRole" == userRole>
            <a href="${servePath}/admin" title="${adminLabel}" class="last icon-userrole"></a>
            </#if>
            <a href="${servePath}/member/${currentUser.userName}" title="Home" class="<#if 'adminRole' != userRole>last </#if>nav-avatar">
                <span class="avatar-small" style="background-image:url('${currentUser.userAvatarURL20}')"></span>
            </a>
            <a href="${servePath}/activities" title="${activityLabel}" class="icon-flag"></a>
            <a href="${servePath}/pre-post" title="${addArticleLabel}" 
               class="icon-addfile"></a>
            <a id="aNotifications" class="<#if unreadNotificationCount == 0>no-msg<#else>msg</#if>" href="${servePath}/notifications" title="${messageLabel}">${unreadNotificationCount}</a>
            <a href="${servePath}/recent" class="icon-refresh"></a>
            <#else>
            <a id="aRegister" href="javascript:Util.goRegister()" class="last ft-blue unlogin" 
               title="${registerLabel}">${registerLabel}</a>
            <a href="javascript: Util.showLogin();" title="${loginLabel}" class="unlogin">${loginLabel}</a>
            <div class="form fn-none">
                <table cellspacing="0" cellpadding="0">
                    <tr>
                        <td width="80">
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
                        <td align="right">
                            <label>${rememberLoginStatusLabel}</label>
                        </td>
                        <td>
                            <input type="checkbox" id="rememberLogin" checked />
                        </td>
                    </tr>
                    <tr class="fn-none">
                        <td>
                            <img id="captcha" class="fn-pointer" />
                        </td>
                        <td>
                            <input type="text" id="captchaLogin" />
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
