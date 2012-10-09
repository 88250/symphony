<div>
    <img class="avatar-big" title="${user.userName}" src="${user.userThumbnailURL}" />
    <div>
        online 
        <br/>
        <a href="/${user.userName}">${user.userName}</a> 
        Symphony 第 ${user.userNo} 号会员
        <#if user.userURL!="">
        <br/>
        <span class="ft-small">URL: </span><a href="">${user.userURL}</a>
        </#if>
        <br/>
        <span class="ft-small">${joinTimeLabel}: </span>${user.created}
    </div>
    <div>
        ${user.userIntro}
    </div>
</div>