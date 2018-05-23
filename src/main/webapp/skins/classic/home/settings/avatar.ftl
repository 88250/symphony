<#--

    Symphony - A modern community (forum/BBS/SNS/blog) platform written in Java.
    Copyright (C) 2012-2018, b3log.org & hacpai.com

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU Affero General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU Affero General Public License for more details.

    You should have received a copy of the GNU Affero General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

-->
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
                <label class="btn">
                    ${uploadLabel}<input type="file" name="file">
                </label>
            </form>
            <label class="btn green" onclick="Settings.preview(this)">${previewLabel}</label>
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