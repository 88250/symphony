<#include "macro-home.ftl">
<@home "settings">
<br/>
<div class="module">
    <div class="module-header fn-clear">
        <a rel="nofollow" href="/member/${currentUser.userName}" target="_blank">${currentUser.userName}</a>
        <h2>${profilesLabel}</h2>
        <span>(${currentUser.userEmail})</span>
    </div>
    <div class="module-panel form fn-clear">
        <label>${selfTagLabel}</label><br/>
        <input id="userTags" type="text" value="${currentUser.userTags}" placeholder="${selfDescriptionLabel}"/>

        <label>URL</label><br/>
        <input id="userURL" type="text" value="${currentUser.userURL}"/>

        <!--
        <label>QQ</label><br/>
        <input id="userQQ" type="text" value="${currentUser.userQQ}" />
        -->

        <label>${userIntroLabel}</label><br/>
        <textarea id="userIntro">${currentUser.userIntro}</textarea>

        <label>${avatarLabel}</label><br/>
        <div class="fn-clear"></div>
        <form class="fn-right" id="avatarUpload" method="POST" enctype="multipart/form-data">
            <label class="btn">
                ${uploadLabel}<input type="file" name="file">
            </label>
        </form>
        <div class="fn-clear">
            <div>
                <img class="avatar-big" id="avatarURL" src="${currentUser.userAvatarURL}?${currentUser.userUpdateTime?c}"> &nbsp; 
                <div class="responsive-show fn-hr5"></div>
                <img class="avatar-mid" id="avatarURLMid" src="${currentUser.userAvatarURL}?${currentUser.userUpdateTime?c}"> &nbsp;
                <div class="responsive-show fn-hr5"></div>
                <img class="avatar" id="avatarURLNor" src="${currentUser.userAvatarURL}?${currentUser.userUpdateTime?c}">
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
               readonly="readonly"/><!--<br/><br/>

        <select id="geoStatus" onchange="Settings.changeGeoStatus('${csrfToken}')">
            <option name="public" value="0" <#if 0 == user.userGeoStatus>selected</#if>>${publicLabel}</option>
            <option name="private" value="1" <#if 1 == user.userGeoStatus>selected</#if>>${privateLabel}</option>
        </select>
        ${geoInfoLabel}
        -->
    </div>
</div>

<div class="module">
    <div class="module-header">
        <h2>${syncLabel} <a href="http://hacpai.com/article/1440820551723" target="_blank">(?)</a></h2>
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
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js"></script>
<script>

            if ("" === '${qiniuUploadToken}') { // 说明没有使用七牛，而是使用本地
                $('#avatarUpload').fileupload({
                    acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
                    maxFileSize: 1024 * 1024, // 1M
                    multipart: true,
                    pasteZone: null,
                    dropZone: null,
                    url: "/upload",
                    formData: function (form) {
                        var data = form.serializeArray();
                        return data;
                    },
                    submit: function (e, data) {
                    },
                    done: function (e, data) {
                        // console.log(data.result)
                        var qiniuKey = data.result.key;
                        if (!qiniuKey) {
                            alert("Upload error");
                            return;
                        }

                        $('#avatarURL').attr("src", qiniuKey);
                        $('#avatarURLMid').attr("src", qiniuKey);
                        $('#avatarURLNor').attr("src", qiniuKey);
                    },
                    fail: function (e, data) {
                        alert("Upload error: " + data.errorThrown);
                    }
                }).on('fileuploadprocessalways', function (e, data) {
                    var currentFile = data.files[data.index];
                    if (data.files.error && currentFile.error) {
                        alert(currentFile.error);
                    }
                });
            } else {
                $('#avatarUpload').fileupload({
                    acceptFileTypes: /(\.|\/)(gif|jpe?g|png)$/i,
                    maxFileSize: 1024 * 1024, // 1M
                    multipart: true,
                    pasteZone: null,
                    dropZone: null,
                    url: "http://upload.qiniu.com/",
                    formData: function (form) {
                        var data = form.serializeArray();
                        data.push({name: 'token', value: '${qiniuUploadToken}'});
                        data.push({name: 'key', value: 'avatar/${currentUser.oId}'});
                        return data;
                    },
                    submit: function (e, data) {
                    },
                    done: function (e, data) {
                        // console.log(data.result)
                        var qiniuKey = data.result.key;
                        if (!qiniuKey) {
                            alert("Upload error");
                            return;
                        }

                        var t = new Date().getTime();
                        $('#avatarURL').attr("src", '${qiniuDomain}/' + qiniuKey + '?' + t);
                        $('#avatarURLMid').attr("src", '${qiniuDomain}/' + qiniuKey + '?' + t);
                        $('#avatarURLNor').attr("src", '${qiniuDomain}/' + qiniuKey + '?' + t);
                    },
                    fail: function (e, data) {
                        alert("Upload error: " + data.errorThrown);
                    }
                }).on('fileuploadprocessalways', function (e, data) {
                    var currentFile = data.files[data.index];
                    if (data.files.error && currentFile.error) {
                        alert(currentFile.error);
                    }
                });
            }
</script>