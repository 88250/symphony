<#include "macro-home.ftl">
<@home "settings">
<br/>
<div class="module">
    <div class="module-header fn-clear">
        <a rel="nofollow" href="/member/${currentUser.userName}">${currentUser.userName}</a>
        <h2>${profilesLabel}</h2>
        <span>(${currentUser.userEmail})</span>
    </div>
    <div class="module-panel form fn-clear">
        <label>${selfTagLabel}</label><br/>
        <input id="userTags" type="text" value="${currentUser.userTags}" placeholder="${selfDescriptionLabel}"/>
        <span style="top: 50px; right: 24px;"></span>
        
        <label>URL</label><br/>
        <input id="userURL" type="text" value="${currentUser.userURL}"/>
        <span style="top: 118px; right: 24px;"></span>

        <label>QQ</label><br/>
        <input id="userQQ" type="text" value="${currentUser.userQQ}" />
        <span style="top: 186px; right:24px;"></span>

        <label>${userIntroLabel}</label><br/>
        <textarea id="userIntro">${currentUser.userIntro}</textarea>
        <span style="top:255px; right:24px;"></span><br/>

        <label>${avatarLabel}</label><br/>
        <div class="fn-clear"></div>
        <form class="fn-right" id="avatarUpload" method="POST" enctype="multipart/form-data">
            <label class="btn">
                ${uploadLabel}<input type="file" name="file">
            </label>
        </form>
        <div class="fn-clear"></div>
        <div>
            <img class="avatar-big" id="avatarURL" src="${currentUser.userAvatarURL}"> &nbsp; 
            <span style="top: 506px; right: 24px;"></span>
            <img class="avatar-mid" id="avatarURLMid" src="${currentUser.userAvatarURL}"> &nbsp;
            <img class="avatar" id="avatarURLNor" src="${currentUser.userAvatarURL}">
        </div>
        <div class="fn-right">
            ${updateAvatarTipLabel}
        </div>
        <span style="right:140px;top:265px;"></span><br/>
        <br/><br/>
        <span id="profilesTip" style="right: 95px; top: 703px;"></span>
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
        <span style="right:24px;top:49px;"></span><br/><br/>
        <input id="pointTransferAmount" type="text" placeholder="${amountLabel}"/>
        <span style="right:24px;top:49px;"></span><br/><br/>

        <span id="pointTransferTip" style="right: 110px; top: 149px;"></span>
        <button class="red fn-right" onclick="Settings.pointTransfer('${csrfToken}')">${confirmTransferLabel}</button>
    </div>
</div>

<div class="module">
    <div class="module-header">
        <h2>${syncLabel}</h2>
    </div>
    <div class="module-panel form fn-clear">
        <label>B3log Key</label>
        <input id="soloKey" type="text" value="${currentUser.userB3Key}" /> 
        <span style="right:24px;top:49px;"></span><br/>

        <label>${clientArticleLabel}</label>
        <input id="soloPostURL" type="text" value="${currentUser.userB3ClientAddArticleURL}" />
        <span style="right:24px;top:118px;"></span><br/>

        <label>${clientUpdateArticleLabel}</label>
        <input id="soloUpdateURL" type="text" value="${currentUser.userB3ClientUpdateArticleURL}" />
        <span style="right:24px;top:186px;"></span><br/>

        <label>${clientCmtLabel}</label>
        <input id="soloCmtURL" type="text" value="${currentUser.userB3ClientAddCommentURL}" /> 
        <span style="right:24px;top:255px;"></span><br/><br/>

        <span id="syncb3Tip" style="right: 95px; top: 305px;"></span>
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
        <span style="right:24px;top:50px;"></span><br/>

        <label>${newPasswordLabel}</label>
        <input id="pwdNew" type="password" />
        <span style="right:24px;top:118px;"></span><br/>

        <label>${confirmPasswordLabel}</label>
        <input id="pwdRepeat" type="password" /> 
        <span style="right:24px;top:187px;"></span><br/><br/>

        <span id="passwordTip" style="right: 95px; top: 237px;"></span>
        <button class="green fn-right" onclick="Settings.update('password', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>
</@home>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/vendor/jquery.ui.widget.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.iframe-transport.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-process.js"></script>
<script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload-validate.js"></script>
<script>
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
                        console.log(data.result)
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
</script>