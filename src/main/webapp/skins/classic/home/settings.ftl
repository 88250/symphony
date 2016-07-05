<#include "macro-home.ftl">
<@home "settings">
<br/>
<div class="module">
    <div class="module-header fn-clear">
        <a rel="nofollow" href="/member/${currentUser.userName}" target="_blank">${currentUser.userName}</a>
        <h2>${profilesLabel}</h2>
        <span>(${currentUser.userEmail})</span>
        <a class="ft-red fn-right" href="javascript:Util.logout()">${logoutLabel}</a>
    </div>
    <div class="module-panel form fn-clear">
        <label>${nicknameLabel}</label><br/>
        <input id="userNickname" type="text" value="${currentUser.userNickname}" placeholder="${selfNicknameLabel}"/>

        <label>${selfTagLabel}</label><br/>
        <input id="userTags" type="text" value="${currentUser.userTags}" placeholder="${selfDescriptionLabel}"/>

        <label>URL</label><br/>
        <input id="userURL" type="text" value="${currentUser.userURL}" placeholder="${selfURLLabel}"/>

        <#--
        <label>QQ</label><br/>
        <input id="userQQ" type="text" value="${currentUser.userQQ}" />
        -->

        <label>${userIntroLabel}</label><br/>
        <textarea id="userIntro" placeholder="${selfIntroLabel}">${currentUser.userIntro}</textarea>

        <label>${commentViewModeLabel}</label><br/>
        <select id="userCommentViewMode" name="userCommentViewMode">
            <option value="0"<#if 0 == currentUser.userCommentViewMode> selected</#if>>${traditionLabel}</option>
            <option value="1"<#if 1 == currentUser.userCommentViewMode> selected</#if>>${realTimeLabel}</option>
        </select>

        <div class="fn-clear"></div>
        <label>
            ${joinBalanceRankLabel}
            <input id="joinPointRank" <#if 0 == currentUser.userJoinPointRank> checked="checked"</#if> type="checkbox" /> 
        </label>

        <label> &nbsp; &nbsp;
            ${joinCosumptionRankLabel}
            <input id="joinUsedPointRank" <#if 0 == currentUser.userJoinUsedPointRank> checked="checked"</#if> type="checkbox" /> 
        </label>
        <div class="fn-clear"></div>

        <label>${avatarLabel}</label><br/>
        <div class="fn-clear"></div>
        <form class="fn-right" id="avatarUpload" method="POST" enctype="multipart/form-data">
            <label class="btn">
                ${uploadLabel}<input type="file" name="file">
            </label>
        </form>
        <div class="fn-clear">
            <div>
                <div class="avatar-big" id="avatarURL" data-imageurl="${currentUser.userAvatarURL}"
                     style="background-image:url('${currentUser.userAvatarURL}?${currentUser.userUpdateTime?c}')"></div> &nbsp; 
                <div class="responsive-show fn-hr5"></div>
                <div class="avatar-mid" id="avatarURLMid" data-imageurl="${currentUser.userAvatarURL}"
                     style="background-image:url('${currentUser.userAvatarURL}?${currentUser.userUpdateTime?c}')"></div> &nbsp;
                <div class="responsive-show fn-hr5"></div>
                <div class="avatar" id="avatarURLNor" data-imageurl="${currentUser.userAvatarURL}"
                     style="background-image:url('${currentUser.userAvatarURL}?${currentUser.userUpdateTime?c}')"></div>
            </div>
            <div class="fn-hr5"></div>
            <div class="fn-right">
                ${updateAvatarTipLabel}
            </div>
        </div>
        <br/>
        <div class="tip" id="profilesTip"></div>
        <br/>
        <button class="green fn-right" onclick="Settings.update('profiles', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>

<div class="module">
    <div class="module-header">
        <h2>${inviteLabel}</h2>
    </div>

    <div class="module-panel form fn-clear">
        ${inviteTipLabel}<br><br>
        <input type="text" value="${serverScheme}://${serverHost}/register?r=${currentUser.userName}" onclick="this.select()"/>
        <button class="red" id="shareClipboard"
                data-clipboard-text="${serverScheme}://${serverHost}/register?r=${currentUser.userName}">${copyLabel}</button>
    </div>
</div>

<div class="module">
    <div class="module-header">
        <h2>${pointTransferLabel}</h2>
    </div>

    <div class="module-panel form fn-clear">
        ${pointTransferTipLabel}<br><br>
        <input id="pointTransferUserName" type="text" placeholder="${userNameLabel}"/>
        <br/> <br/>
        <input id="pointTransferAmount" type="number" placeholder="${amountLabel}"/> <br/><br/>
        <div id="pointTransferTip" class="tip"></div> <br/>
        <button class="red fn-right" onclick="Settings.pointTransfer('${csrfToken}')">${confirmTransferLabel}</button>
    </div>
</div>

<div class="module">
    <div class="module-header">
        <h2>${geoLable}</h2>
    </div>

    <div class="module-panel form fn-clear">
        ${geoInfoTipLabel}<br><br>
        <input id="cityName" type="text" placeholder="${geoInfoPlaceholderLabel}" value="${user.userCity}" 
               readonly="readonly"/>

        <br/><br/>

        <select id="geoStatus" onchange="Settings.changeGeoStatus('${csrfToken}')">
            <option name="public" value="0" <#if 0 == user.userGeoStatus>selected</#if>>${publicLabel}</option>
            <option name="private" value="1" <#if 1 == user.userGeoStatus>selected</#if>>${privateLabel}</option>
        </select>
    </div>
</div>

<div class="module">
    <div class="module-header">
        <h2>${syncLabel} <a href="https://hacpai.com/article/1440820551723" target="_blank">(?)</a></h2>
    </div>
    <div class="module-panel form fn-clear">
        <label>B3log Key</label>
        <input id="soloKey" type="text" value="${currentUser.userB3Key}" /> 

        <label>${clientArticleLabel}</label>
        <input id="soloPostURL" type="text" value="${currentUser.userB3ClientAddArticleURL}" />

        <label>${clientUpdateArticleLabel}</label>
        <input id="soloUpdateURL" type="text" value="${currentUser.userB3ClientUpdateArticleURL}" />

        <label>${clientCmtLabel}</label>
        <input id="soloCmtURL" type="text" value="${currentUser.userB3ClientAddCommentURL}" />

        <label>
            ${syncWithSymphonyClientLabel}
            <input id="syncWithSymphonyClient" <#if currentUser.syncWithSymphonyClient> checked="checked"</#if> type="checkbox" /> 
        </label>

        <br/><br/>
        <div class="fn-clear"></div>
        <div id="syncb3Tip" class="tip"></div><br/>
        <button class="green fn-right" onclick="Settings.update('sync/b3', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>

<div class="module">
    <div class="module-header">
        <h2>${miscLabel}</h2>
    </div>
    <div class="module-panel form fn-clear">
        <label>
            ${displayUALabel}
            <input id="userUAStatus" <#if 0 == currentUser.userUAStatus> checked="checked"</#if> type="checkbox" /> 
        </label>

        <label>&nbsp; &nbsp;
            ${useNotifyLabel}
            <input id="userNotifyStatus" <#if 0 == currentUser.userNotifyStatus> checked="checked"</#if> type="checkbox" /> 
        </label>

        <br/><br/>
        <div class="fn-clear"></div>
        <div id="miscTip" class="tip"></div><br/>
        <button class="green fn-right" onclick="Settings.update('misc', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>

<div class="module">
    <div class="module-header">
        <h2>${passwordLabel}</h2>
    </div>
    <div class="module-panel form fn-clear">
        <label>${oldPasswordLabel}</label>
        <input id="pwdOld" type="password" />

        <label>${newPasswordLabel}</label>
        <input id="pwdNew" type="password" />

        <label>${confirmPasswordLabel}</label>
        <input id="pwdRepeat" type="password" /> <br/><br/>
        <div id="passwordTip" class="tip"></div><br/>
        <button class="green fn-right" onclick="Settings.update('password', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>
</@home>
<script type="text/javascript" src="${staticServePath}/js/lib/zeroclipboard/ZeroClipboard.min.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js"></script>
<script>
            Util.initUpload({
            id: 'avatarUpload',
                    qiniuUploadToken: '${qiniuUploadToken}',
                    userId: '${currentUser.oId}',
                    maxSize: ${imgMaxSize?c}
            }, function (data) {
            var qiniuKey = data.result.key;
            $('#avatarURL').css("background-image", 'url(' + qiniuKey + ')').data('imageurl', qiniuKey);
            $('#avatarURLMid').css("background-image", 'url(' + qiniuKey + ')').data('imageurl', qiniuKey);
            $('#avatarURLNor').css("background-image", 'url(' + qiniuKey + ')').data('imageurl', qiniuKey);
            }, function (data) {
            var qiniuKey = data.result.key,
                    t = new Date().getTime();
            $('#avatarURL').css("background-image", 'url(${qiniuDomain}/' + qiniuKey + '?' + t + ')').data('imageurl', '${qiniuDomain}/' + qiniuKey);
            $('#avatarURLMid').css("background-image", 'url(${qiniuDomain}/' + qiniuKey + '?' + t + ')').data('imageurl', '${qiniuDomain}/' + qiniuKey);
            $('#avatarURLNor').css("background-image", 'url(${qiniuDomain}/' + qiniuKey + '?' + t + ')').data('imageurl', '${qiniuDomain}/' + qiniuKey);
            });
            var shareClipboard = new ZeroClipboard(document.getElementById("shareClipboard"));
            shareClipboard.on("ready", function (readyEvent) {
            shareClipboard.on("aftercopy", function (event) {
            $("#shareClipboard").text('${copiedLabel}');
            });
            });
</script>