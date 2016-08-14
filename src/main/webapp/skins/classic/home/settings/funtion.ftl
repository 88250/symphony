<#include "macro-home.ftl">
<@home "${type}">

<div class="module">
    <div class="module-header">
        <h2>${miscLabel}</h2>
    </div>
    <div class="module-panel form fn-clear">
        <div class="fn-clear settings-secret">
            <label>${cmtViewModeLabel}</label><br/>
        <select id="userCommentViewMode" name="userCommentViewMode">
            <option value="0"<#if 0 == currentUser.userCommentViewMode> selected</#if>>${traditionLabel}</option>
            <option value="1"<#if 1 == currentUser.userCommentViewMode> selected</#if>>${realTimeLabel}</option>
        </select>
            <div>
                <label>
                    ${displayUALabel}
                    <input id="userUAStatus" <#if 0 == currentUser.userUAStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${useNotifyLabel}
                    <input id="userNotifyStatus" <#if 0 == currentUser.userNotifyStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${userArticleStatusLabel}
                    <input id="userArticleStatus" <#if 0 == currentUser.userArticleStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${userCommentStatusLabel}
                    <input id="userCommentStatus" <#if 0 == currentUser.userCommentStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${userFollowingUserStatusLabel}
                    <input id="userFollowingUserStatus" <#if 0 == currentUser.userFollowingUserStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${userFollowingTagStatusLabel}
                    <input id="userFollowingTagStatus" <#if 0 == currentUser.userFollowingTagStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${userFollowingArticleStatusLabel}
                    <input id="userFollowingArticleStatus" <#if 0 == currentUser.userFollowingArticleStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${userFollowerStatusLabel}
                    <input id="userFollowerStatus" <#if 0 == currentUser.userFollowerStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${userPointStatusLabel}
                    <input id="userPointStatus" <#if 0 == currentUser.userPointStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${userOnlineStatusLabel}
                    <input id="userOnlineStatus" <#if 0 == currentUser.userOnlineStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>

        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${joinBalanceRankLabel}
                    <input id="joinPointRank" <#if 0 == currentUser.userJoinPointRank> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${joinCosumptionRankLabel}
                    <input id="joinUsedPointRank" <#if 0 == currentUser.userJoinUsedPointRank> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
        </div>

        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${userTimelineStatusLabel}
                    <input id="userTimelineStatus" <#if 0 == currentUser.userTimelineStatus> checked="checked"</#if> type="checkbox" /> 
                </label>
            </div>
            <div>
                <label>
                    ${userListPageSizeLabel}
                    <input id="userListPageSize" type="number" style="width:60px" value="${currentUser.userListPageSize}" /> 
                </label>
            </div>
        </div>
        <div id="miscTip" class="tip"></div>
        <div class="fn-hr5"></div>
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

<div class="module">
    <div class="module-header">
        <h2>${dataExportLabel}</h2>
    </div>
    <div class="module-panel form fn-clear">
        ${dataExportTipLabel}
        <button class="green fn-right" onclick="Settings.exportPosts()">${submitLabel}</button>
    </div>
</div>
</@home>
<script type="text/javascript" src="${staticServePath}/js/lib/zeroclipboard/ZeroClipboard.min.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js"></script>
<script>
            Settings.initUploadAvatar({
                id: 'avatarUpload',
                qiniuUploadToken: '${qiniuUploadToken}',
                userId: '${currentUser.oId}',
                maxSize: '${imgMaxSize?c}'
            }, function (data) {
                var qiniuKey = data.result.key;
                $('#avatarURL').css("background-image", 'url(' + qiniuKey + ')').data('imageurl', qiniuKey);
                $('#avatarURLMid').css("background-image", 'url(' + qiniuKey + ')').data('imageurl', qiniuKey);
                $('#avatarURLNor').css("background-image", 'url(' + qiniuKey + ')').data('imageurl', qiniuKey);
                
                Settings.updateAvatar('${csrfToken}');
            }, function (data) {
                var qiniuKey = data.result.key,
                        t = new Date().getTime();
                $('#avatarURL').css("background-image", 'url(${qiniuDomain}/' + qiniuKey + '?' + t + ')').data('imageurl', '${qiniuDomain}/' + qiniuKey);
                $('#avatarURLMid').css("background-image", 'url(${qiniuDomain}/' + qiniuKey + '?' + t + ')').data('imageurl', '${qiniuDomain}/' + qiniuKey);
                $('#avatarURLNor').css("background-image", 'url(${qiniuDomain}/' + qiniuKey + '?' + t + ')').data('imageurl', '${qiniuDomain}/' + qiniuKey);
                
                Settings.updateAvatar('${csrfToken}');
            });
            
            var shareClipboard = new ZeroClipboard(document.getElementById("shareClipboard"));
            shareClipboard.on("ready", function (readyEvent) {
                shareClipboard.on("aftercopy", function (event) {
                    $("#shareClipboard").text('${copiedLabel}');
                });
            });
</script>