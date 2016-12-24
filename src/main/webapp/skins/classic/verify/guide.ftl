<#include "../macro-head.ftl">
<!DOCTYPE html>
<html>
    <head>
        <@head title="${newbieGuideLabel} - ${symphonyLabel}">
        <meta name="description" content="${newbieGuideLabel} ${symphonyLabel}"/>
        </@head>
        <link type="text/css" rel="stylesheet" href="${staticServePath}/css/index.css?${staticResourceVersion}" />
        <link rel="canonical" href="${servePath}/register">
    </head>
    <body>
        <#include "../header.ftl">
        <div class="main">
            <div class="wrapper verify guide">
                <div class="verify-wrap">
                    <div class="step-btn fn-clear">
                        <button class="red fn-right">${nextStepLabel}</button>
                        <span class="fn-right"> &nbsp; &nbsp;</span>
                        <button class="fn-none green fn-right">${preStepLabel}</button>
                    </div>
                    <div class="guide-tab">
                        <div class="fn-clear fn-none">
                            <div class="avatar-big" id="avatarURL"
                                 style="background-image:url('${currentUser.userAvatarURL20}')"></div> &nbsp; &nbsp;
                            <div class="avatar" id="avatarURLMid"
                                 style="background-image:url('${currentUser.userAvatarURL20}')"></div> &nbsp; &nbsp;
                            <div class="avatar-small" id="avatarURLNor"
                                 style="background-image:url('${currentUser.userAvatarURL20}')"></div>
                            <form class="fn-right form" id="avatarUpload" method="POST" enctype="multipart/form-data">
                                <label class="btn">
                                    ${uploadLabel}<input type="file" name="file">
                                </label>
                            </form>
                        </div>
                        <div class="fn-none fn-clear">
                            <ul class="tag-desc">
                                <#list tags as tag>
                                    <li data-id="${tag.oId}">
                                        <a rel="nofollow" href="javascript:void(0)">
                                            <#if tag.tagIconPath!="">
                                                <img src="${staticServePath}/images/tags/${tag.tagIconPath}" alt="${tag.tagTitle}" /></#if>
                                            ${tag.tagTitle}
                                        </a>
                                        <div<#if tag.tagDescription == ''> style="width:auto"</#if>>
                                            <div>${tag.tagDescription}</div>
                                            <span class="fn-right">
                                                <span class="ft-gray">${referenceLabel}</span>
                                                ${tag.tagReferenceCount} &nbsp;
                                                <span class="ft-gray">${cmtLabel}</span>
                                                ${tag.tagCommentCount}&nbsp;
                                            </span>
                                        </div>
                                    </li>
                                </#list>
                            </ul>
                        </div>
                        <div class="fn-none list">
                            <ul class="fn-clear">
                                <#list users as follower>
                                    <li>
                                        <div class="fn-flex">
                                            <a rel="nofollow" class="tooltipped tooltipped-se fn-left" aria-label="${follower.userName} <#if follower.userOnlineFlag>${onlineLabel}<#else>${offlineLabel}</#if>"
                                               href="${servePath}/member/${follower.userName}" >
                                                <div class="avatar" style="background-image:url('${follower.userAvatarURL}')"></div>
                                            </a>
                                            <div class="fn-flex-1">
                                                <h2 class="fn-inline">
                                                    <a rel="nofollow" href="${servePath}/member/${follower.userName}" ><#if follower.userNickname != ''>${follower.userNickname}<#else>${follower.userName}</#if></a>
                                                </h2>
                                                <#if follower.userNickname != ''>
                                                    <a class='ft-fade' rel="nofollow" href="${servePath}/member/${follower.userName}" >${follower.userName}</a>
                                                </#if>
                                                <button class="fn-right mid" onclick="Util.follow(this, '${follower.oId}', 'user')">
                                                    ${followLabel}
                                                </button>
                                                <div>
                                                    <#if follower.userArticleCount == 0>
                                                        <#if follower.userURL != "">
                                                            <a class="ft-gray" target="_blank" rel="friend" href="${follower.userURL?html}">${follower.userURL?html}</a>
                                                            <#else>
                                                                <span class="ft-gray">${symphonyLabel}</span>
                                                                ${follower.userNo?c}
                                                                <span class="ft-gray">${numVIPLabel}</span>
                                                        </#if>
                                                        <#else>
                                                            <span class="ft-gray">${articleLabel}</span> ${follower.userArticleCount?c} &nbsp;
                                                            <span class="ft-gray">${tagLabel}</span> ${follower.userTagCount?c}
                                                    </#if>
                                                </div>
                                            </div>
                                        </div>
                                    </li>
                                </#list>
                            </ul>
                        </div>
                        <div class="fn-none list">
                            <br/><br/>
                            <div class="ft-center">${logoIcon2}</div> <br/>
                            <ul>
                                <li>
                                    <a href="${servePath}/about">${getStartLabel}</a>
                                    <span class="ft-gray">${getStartTipLabel}</span>
                                </li>
                                <li>
                                    <a href="${servePath}/tag/%E4%BD%BF%E7%94%A8%E6%8C%87%E5%8D%97">${basicLabel}</a>
                                    <span class="ft-gray">${basicTipLabel}</span>
                                </li>
                                <li>
                                    <a href="https://hacpai.com/article/1474030007391">${hotKeyLabel}</a>
                                    <span class="ft-gray">${hotKeyTipLabel}</span>
                                </li>
                            </ul>
                            <br/>
                        </div>
                    </div>
                </div>
                <div class="intro content-reset">
                    <dl>
                        <dt class="current"><span class="index">1</span>上传个性头像</dt>
                        <dd class="ft-gray ft-smaller">数据显示，设置头像后欢迎度是默认头像的 8.7 倍！</dd>
                        <dt><span class="index">2</span>上传个性头像</dt>
                        <dd class="ft-gray ft-smaller">数据显示，设置头像后欢迎度是默认头像的 8.7 倍！</dd>
                        <dt><span class="index">3</span>上传个性头像</dt>
                        <dd class="ft-gray ft-smaller">数据显示，设置头像后欢迎度是默认头像的 8.7 倍！</dd>
                    </dl>
                    <div class="fn-none">
                        ${introLabel}
                    </div>
                </div>
            </div>
        </div>
        <#include "../footer.ftl">
        <script type="text/javascript" src="${staticServePath}/js/verify${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/settings${miniPostfix}.js?${staticResourceVersion}"></script>
        <script type="text/javascript" src="${staticServePath}/js/lib/jquery/file-upload-9.10.1/jquery.fileupload.min.js"></script>
        <script>
            Label.finshLabel = '${finshLabel}';
            Label.nextStepLabel = '${nextStepLabel}';
            Label.unfollowLabel = '${unfollowLabel}';
            Label.followLabel = '${followLabel}';
            Verify.initGuide(${currentUser.userGuideStep?c});

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
    </body>
</html>
