<div class="nav"> 
    <div class="wrapper fn-clear">
        <div class="head-fn fn-clear">
            <h1 class="fn-left">
                <a href="/" alt="${symphonyLabel}" title="${symphonyLabel}" class="fn-pointer">
                    <svg version="1.0" xmlns="http://www.w3.org/2000/svg" width="48px" height="48px" viewBox="0 0 300.000000 300.000000">
                        <g transform="translate(0.000000,300.000000) scale(0.100000,-0.100000)" fill="#484848" stroke="none">
                            <path d="M1893 2282 c-106 -106 -286 -412 -387 -658 l-37 -91 -76 -11 c-42 -7
                                  -132 -28 -200 -48 -69 -19 -127 -34 -129 -31 -2 2 7 55 20 118 111 512 10 759
                                  -268 656 -67 -25 -87 -47 -66 -72 11 -13 24 -15 67 -9 45 5 57 3 74 -12 54
                                  -49 75 -155 60 -300 -17 -150 -61 -426 -72 -446 -5 -9 -30 -29 -55 -44 -132
                                  -77 -265 -215 -323 -334 -31 -64 -36 -85 -36 -145 0 -55 5 -78 23 -106 25 -41
                                  81 -69 138 -69 49 0 125 42 169 92 54 64 136 235 190 400 l49 145 65 26 c82
                                  31 253 84 303 93 l38 6 -15 -43 c-28 -79 -55 -236 -55 -321 0 -140 41 -238
                                  100 -238 46 0 54 20 29 81 -38 94 -23 274 38 460 23 70 28 77 55 83 38 8 138
                                  8 138 0 0 -5 -14 -107 -26 -179 -2 -16 -9 -136 -15 -265 -12 -267 -9 -290 41
                                  -290 37 0 85 23 94 45 3 9 6 40 6 69 0 66 28 278 38 287 4 4 35 15 70 24 181
                                  46 322 123 438 239 151 152 188 306 112 465 -30 65 -169 199 -246 239 -80 43
                                  -192 81 -259 89 -29 3 -53 8 -53 10 0 2 11 19 25 37 60 79 8 119 -62 48z m161
                                  -250 c140 -46 260 -142 314 -252 36 -73 38 -193 5 -255 -45 -85 -144 -175
                                  -258 -232 -53 -27 -226 -85 -233 -78 -3 3 0 51 6 106 11 89 15 104 37 121 27
                                  21 33 59 11 77 -12 10 -11 24 5 94 10 45 30 123 45 172 42 142 48 183 30 210
                                  -18 27 -56 36 -103 23 -29 -8 -39 -18 -60 -65 -24 -52 -103 -348 -103 -385 0
                                  -16 -10 -18 -76 -18 l-76 0 7 28 c12 53 123 303 176 398 l53 96 71 -7 c38 -4
                                  105 -19 149 -33z m-1213 -855 c-11 -30 -45 -106 -77 -168 -80 -157 -132 -211
                                  -178 -187 -15 8 -17 18 -13 54 7 66 54 149 132 232 65 70 144 137 152 129 2
                                  -2 -6 -29 -16 -60z"/>
                            <path d="M201 1972 c-68 -34 -72 -48 -74 -243 -2 -205 -8 -221 -83 -241 -42
                                  -12 -45 -15 -42 -42 3 -25 8 -31 37 -37 18 -4 45 -19 60 -33 26 -26 26 -28 27
                                  -192 1 -106 6 -176 14 -195 17 -40 75 -69 140 -69 49 0 50 1 50 30 0 26 -4 30
                                  -27 30 -38 0 -91 34 -98 62 -2 13 -6 94 -7 180 l-3 157 -29 30 c-17 17 -37 31
                                  -45 31 -10 0 -4 8 14 20 57 37 65 67 65 245 1 110 4 165 13 176 18 24 52 39
                                  86 39 29 0 31 3 31 35 l0 35 -47 0 c-27 0 -63 -8 -82 -18z"/>
                            <path d="M2680 1956 c0 -32 2 -34 41 -39 78 -10 83 -23 89 -220 5 -181 11
                                  -204 55 -233 l23 -15 -36 -34 -37 -33 -5 -169 c-3 -106 -10 -176 -17 -189 -13
                                  -22 -60 -44 -94 -44 -14 0 -19 -7 -19 -30 0 -30 1 -30 53 -30 34 0 63 7 84 20
                                  59 36 63 50 66 239 l2 173 33 29 c18 16 43 29 57 29 22 0 25 4 25 35 0 31 -3
                                  35 -34 41 -19 3 -45 17 -58 31 -22 24 -23 31 -25 205 -2 204 -6 217 -76 249
                                  -23 10 -61 19 -84 19 -42 0 -43 -1 -43 -34z"/>
                        </g>
                    </svg></a>
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
