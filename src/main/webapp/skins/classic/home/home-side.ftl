<div>
    <img class="avatar-big" src="https://secure.gravatar.com/avatar/22ae6b52ee5c2d024b68531bd250be5b?s=140" />
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