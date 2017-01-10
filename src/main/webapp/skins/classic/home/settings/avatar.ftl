<#include "macro-settings.ftl">
<@home "avatar">
<div class="module">
    <div class="module-header">${updateAvatarTipLabel}</div>
    <div class="module-panel form">
        <div class="fn-clear">
            <div class="avatar-big" id="avatarURL" data-imageurl="${currentUser.userAvatarURL}"
                 onclick="$('#avatarUpload input').click()"
                 style="background-image:url('${currentUser.userAvatarURL}?${currentUser.userUpdateTime?c}')"></div> &nbsp; &nbsp; 
            <div class="avatar" id="avatarURLMid" data-imageurl="${currentUser.userAvatarURL}"
                 onclick="$('#avatarUpload input').click()"
                 style="background-image:url('${currentUser.userAvatarURL}?${currentUser.userUpdateTime?c}')"></div> &nbsp; &nbsp; 
            <div class="avatar-small" id="avatarURLNor" data-imageurl="${currentUser.userAvatarURL}"
                 onclick="$('#avatarUpload input').click()"
                 style="background-image:url('${currentUser.userAvatarURL}?${currentUser.userUpdateTime?c}')"></div>
        </div>
        <br/>
        <div class="fn-clear">
            <form class="fn-right" id="avatarUpload" method="POST" enctype="multipart/form-data">
                <label class="btn green">
                    ${uploadLabel}<input type="file" name="file">
                </label>
            </form>
            <label class="btn" onclick="Settings.preview(this)">${previewLabel}</label>
        </div>
    </div>
</div>

</@home>
<script src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js"></script>
<script>
        Settings.initUploadAvatar({
            id: 'avatarUpload',
            qiniuUploadToken: '${qiniuUploadToken}',
            userId: '${currentUser.oId}',
            maxSize: '${imgMaxSize?c}'
        }, function (data) {
            var uploadKey = data.result.key;
            $('#avatarURL').css("background-image", 'url(' + uploadKey + ')').data('imageurl', uploadKey);
            $('#avatarURLMid').css("background-image", 'url(' + uploadKey + ')').data('imageurl', uploadKey);
            $('#avatarURLNor').css("background-image", 'url(' + uploadKey + ')').data('imageurl', uploadKey);

            Settings.updateAvatar('${csrfToken}');
        }, function (data) {
            var qiniuKey = data.result.key,
                    t = new Date().getTime();
            $('#avatarURL').css("background-image", 'url(${qiniuDomain}/' + qiniuKey + '?' + t + ')').data('imageurl', '${qiniuDomain}/' + qiniuKey);
            $('#avatarURLMid').css("background-image", 'url(${qiniuDomain}/' + qiniuKey + '?' + t + ')').data('imageurl', '${qiniuDomain}/' + qiniuKey);
            $('#avatarURLNor').css("background-image", 'url(${qiniuDomain}/' + qiniuKey + '?' + t + ')').data('imageurl', '${qiniuDomain}/' + qiniuKey);

            Settings.updateAvatar('${csrfToken}');
        });
</script>