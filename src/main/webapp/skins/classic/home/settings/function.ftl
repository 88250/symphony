<#include "macro-settings.ftl">
<@home "function">
<div class="module">
    <div class="module-header">${functionTipLabel}</div>
    <div class="module-panel form fn-clear">
        <label>${userListPageSizeLabel}</label>
        <input id="userListPageSize" type="number" value="${currentUser.userListPageSize}" /> 
        <label>${cmtViewModeLabel}</label>
        <select id="userCommentViewMode" name="userCommentViewMode">
            <option value="0"<#if 0 == currentUser.userCommentViewMode> selected</#if>>${traditionLabel}</option>
            <option value="1"<#if 1 == currentUser.userCommentViewMode> selected</#if>>${realTimeLabel}</option>
        </select>
        <label>${avatarViewModeLabel}</label>
        <select id="userAvatarViewMode" name="userAvatarViewMode">
            <option value="0"<#if 0 == currentUser.userAvatarViewMode> selected</#if>>${orgImgLabel}</option>
            <option value="1"<#if 1 == currentUser.userAvatarViewMode> selected</#if>>${staticImgLabel}</option>
        </select>
        <div class="fn-clear settings-secret">
            <div>
                <label>
                    ${useNotifyLabel} 
                    <input id="userNotifyStatus" <#if 0 == currentUser.userNotifyStatus> checked="checked"</#if> type="checkbox" />
                </label>
            </div>
            <div>
                <label>
                    ${subMailLabel} 
                    <input id="userSubMailStatus" <#if 0 == currentUser.userSubMailStatus> checked="checked"</#if> type="checkbox" />
            </div>
        </div>
        <div class="fn-clear"></div>
        <div id="functionTip" class="tip"></div>
        <div class="fn-hr5"></div>
        <button class="green fn-right" onclick="Settings.update('function', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>

<div class="module">
    <div class="module-header">
        <h2>${setEmotionLabel}</h2>
    </div>
    <div class="module-panel form fn-clear">
        <br>
        <textarea id="emotionList" rows="3" placeholder="${setEmotionTipLabel}" >${emotions}</textarea>
        <table id="emojiGrid">
            <tr>
                <td><img alt="smile" src="${servePath}/emoji/graphics/emojis/smile.png"></td>
                <td><img alt="laughing" src="${servePath}/emoji/graphics/emojis/laughing.png"></td>
                <td><img alt="smirk" src="${servePath}/emoji/graphics/emojis/smirk.png"></td>
                <td><img alt="heart_eyes" src="${servePath}/emoji/graphics/emojis/heart_eyes.png"></td>
                <td><img alt="kissing_heart" src="${servePath}/emoji/graphics/emojis/kissing_heart.png"></td>
                <td><img alt="flushed" src="${servePath}/emoji/graphics/emojis/flushed.png"></td>
                <td><img alt="grin" src="${servePath}/emoji/graphics/emojis/grin.png"></td>
                <td><img alt="stuck_out_tongue_closed_eyes" src="${servePath}/emoji/graphics/emojis/stuck_out_tongue_closed_eyes.png"></td>
                <td><img alt="kissing" src="${servePath}/emoji/graphics/emojis/kissing.png"></td>
                <td><img alt="sleeping" src="${servePath}/emoji/graphics/emojis/sleeping.png"></td>
                <td><img alt="anguished" src="${servePath}/emoji/graphics/emojis/anguished.png"></td>
                <td><img alt="open_mouth" src="${servePath}/emoji/graphics/emojis/open_mouth.png"></td>
                <td><img alt="expressionless" src="${servePath}/emoji/graphics/emojis/expressionless.png"></td>
                <td><img alt="unamused" src="${servePath}/emoji/graphics/emojis/unamused.png"></td>
                <td><img alt="sweat_smile" src="${servePath}/emoji/graphics/emojis/sweat_smile.png"></td>
                <td><img alt="weary" src="${servePath}/emoji/graphics/emojis/weary.png"></td>
                <td><img alt="sob" src="${servePath}/emoji/graphics/emojis/sob.png"></td>
                <td><img alt="joy" src="${servePath}/emoji/graphics/emojis/joy.png"></td>
                <td><img alt="astonished" src="${servePath}/emoji/graphics/emojis/astonished.png"></td>
                <td><img alt="scream" src="${servePath}/emoji/graphics/emojis/scream.png"></td>
            </tr>
            <tr>
                <td><img alt="tired_face" src="${servePath}/emoji/graphics/emojis/tired_face.png"></td>
                <td><img alt="rage" src="${servePath}/emoji/graphics/emojis/rage.png"></td>
                <td><img alt="triumph" src="${servePath}/emoji/graphics/emojis/triumph.png"></td>
                <td><img alt="yum" src="${servePath}/emoji/graphics/emojis/yum.png"></td>
                <td><img alt="mask" src="${servePath}/emoji/graphics/emojis/mask.png"></td>
                <td><img alt="sunglasses" src="${servePath}/emoji/graphics/emojis/sunglasses.png"></td>
                <td><img alt="dizzy_face" src="${servePath}/emoji/graphics/emojis/dizzy_face.png"></td>
                <td><img alt="imp" src="${servePath}/emoji/graphics/emojis/imp.png"></td>
                <td><img alt="smiling_imp" src="${servePath}/emoji/graphics/emojis/smiling_imp.png"></td>
                <td><img alt="innocent" src="${servePath}/emoji/graphics/emojis/innocent.png"></td>
                <td><img alt="alien" src="${servePath}/emoji/graphics/emojis/alien.png"></td>
                <td><img alt="yellow_heart" src="${servePath}/emoji/graphics/emojis/yellow_heart.png"></td>
                <td><img alt="blue_heart" src="${servePath}/emoji/graphics/emojis/blue_heart.png"></td>
                <td><img alt="purple_heart" src="${servePath}/emoji/graphics/emojis/purple_heart.png"></td>
                <td><img alt="heart" src="${servePath}/emoji/graphics/emojis/heart.png"></td>
                <td><img alt="green_heart" src="${servePath}/emoji/graphics/emojis/green_heart.png"></td>
                <td><img alt="broken_heart" src="${servePath}/emoji/graphics/emojis/broken_heart.png"></td>
                <td><img alt="dizzy" src="${servePath}/emoji/graphics/emojis/dizzy.png"></td>
                <td><img alt="anger" src="${servePath}/emoji/graphics/emojis/anger.png"></td>
                <td><img alt="exclamation" src="${servePath}/emoji/graphics/emojis/exclamation.png"></td>
            </tr>
            <tr>
                <td><img alt="question" src="${servePath}/emoji/graphics/emojis/question.png"></td>
                <td><img alt="zzz" src="${servePath}/emoji/graphics/emojis/zzz.png"></td>
                <td><img alt="notes" src="${servePath}/emoji/graphics/emojis/notes.png"></td>
                <td><img alt="shit" src="${servePath}/emoji/graphics/emojis/shit.png"></td>
                <td><img alt="+1" src="${servePath}/emoji/graphics/emojis/+1.png"></td>
                <td><img alt="-1" src="${servePath}/emoji/graphics/emojis/-1.png"></td>
                <td><img alt="ok_hand" src="${servePath}/emoji/graphics/emojis/ok_hand.png"></td>
                <td><img alt="punch" src="${servePath}/emoji/graphics/emojis/punch.png"></td>
                <td><img alt="v" src="${servePath}/emoji/graphics/emojis/v.png"></td>
                <td><img alt="hand" src="${servePath}/emoji/graphics/emojis/hand.png"></td>                    
                <td><img alt="point_up" src="${servePath}/emoji/graphics/emojis/point_up.png"></td>
                <td><img alt="point_down" src="${servePath}/emoji/graphics/emojis/point_down.png"></td>
                <td><img alt="pray" src="${servePath}/emoji/graphics/emojis/pray.png"></td>
                <td><img alt="clap" src="${servePath}/emoji/graphics/emojis/clap.png"></td>
                <td><img alt="muscle" src="${servePath}/emoji/graphics/emojis/muscle.png"></td>
                <td><img alt="ok_woman" src="${servePath}/emoji/graphics/emojis/ok_woman.png"></td>
                <td><img alt="no_good" src="${servePath}/emoji/graphics/emojis/no_good.png"></td>
                <td><img alt="raising_hand" src="${servePath}/emoji/graphics/emojis/raising_hand.png"></td>
                <td><img alt="massage" src="${servePath}/emoji/graphics/emojis/massage.png"></td>
                <td><img alt="haircut" src="${servePath}/emoji/graphics/emojis/haircut.png"></td>
            </tr>
            <tr>
                <td><img alt="nail_care" src="${servePath}/emoji/graphics/emojis/nail_care.png"></td>
                <td><img alt="see_no_evil" src="${servePath}/emoji/graphics/emojis/see_no_evil.png"></td>
                <td><img alt="feet" src="${servePath}/emoji/graphics/emojis/feet.png"></td>
                <td><img alt="kiss" src="${servePath}/emoji/graphics/emojis/kiss.png"></td>
                <td><img alt="eyes" src="${servePath}/emoji/graphics/emojis/eyes.png"></td>
                <td><img alt="trollface" src="${servePath}/emoji/graphics/emojis/trollface.png"></td>
                <td><img alt="snowman" src="${servePath}/emoji/graphics/emojis/snowman.png"></td>
                <td><img alt="zap" src="${servePath}/emoji/graphics/emojis/zap.png"></td>
                <td><img alt="cat" src="${servePath}/emoji/graphics/emojis/cat.png"></td>
                <td><img alt="dog" src="${servePath}/emoji/graphics/emojis/dog.png"></td>
                <td><img alt="mouse" src="${servePath}/emoji/graphics/emojis/mouse.png"></td>
                <td><img alt="hamster" src="${servePath}/emoji/graphics/emojis/hamster.png"></td>
                <td><img alt="rabbit" src="${servePath}/emoji/graphics/emojis/rabbit.png"></td>
                <td><img alt="frog" src="${servePath}/emoji/graphics/emojis/frog.png"></td>
                <td><img alt="koala" src="${servePath}/emoji/graphics/emojis/koala.png"></td>
                <td><img alt="pig" src="${servePath}/emoji/graphics/emojis/pig.png"></td>
                <td><img alt="monkey" src="${servePath}/emoji/graphics/emojis/monkey.png"></td>
                <td><img alt="racehorse" src="${servePath}/emoji/graphics/emojis/racehorse.png"></td>
                <td><img alt="camel" src="${servePath}/emoji/graphics/emojis/camel.png"></td>
                <td><img alt="sheep" src="${servePath}/emoji/graphics/emojis/sheep.png"></td>
            </tr>
            <tr>
                <td><img alt="elephant" src="${servePath}/emoji/graphics/emojis/elephant.png"></td>
                <td><img alt="panda_face" src="${servePath}/emoji/graphics/emojis/panda_face.png"></td>
                <td><img alt="snake" src="${servePath}/emoji/graphics/emojis/snake.png"></td>
                <td><img alt="hatched_chick" src="${servePath}/emoji/graphics/emojis/hatched_chick.png"></td>
                <td><img alt="hatching_chick" src="${servePath}/emoji/graphics/emojis/hatching_chick.png"></td>
                <td><img alt="turtle" src="${servePath}/emoji/graphics/emojis/turtle.png"></td>
                <td><img alt="bug" src="${servePath}/emoji/graphics/emojis/bug.png"></td>
                <td><img alt="honeybee" src="${servePath}/emoji/graphics/emojis/honeybee.png"></td>
                <td><img alt="beetle" src="${servePath}/emoji/graphics/emojis/beetle.png"></td>
                <td><img alt="snail" src="${servePath}/emoji/graphics/emojis/snail.png"></td>

                <td><img alt="octopus" src="${servePath}/emoji/graphics/emojis/octopus.png"></td>
                <td><img alt="whale" src="${servePath}/emoji/graphics/emojis/whale.png"></td>
                <td><img alt="dolphin" src="${servePath}/emoji/graphics/emojis/dolphin.png"></td>
                <td><img alt="dragon" src="${servePath}/emoji/graphics/emojis/dragon.png"></td>
                <td><img alt="goat" src="${servePath}/emoji/graphics/emojis/goat.png"></td>
                <td><img alt="paw_prints" src="${servePath}/emoji/graphics/emojis/paw_prints.png"></td>
                <td><img alt="tulip" src="${servePath}/emoji/graphics/emojis/tulip.png"></td>
                <td><img alt="four_leaf_clover" src="${servePath}/emoji/graphics/emojis/four_leaf_clover.png"></td>
                <td><img alt="rose" src="${servePath}/emoji/graphics/emojis/rose.png"></td>
                <td><img alt="mushroom" src="${servePath}/emoji/graphics/emojis/mushroom.png"></td>
            </tr>
            <tr>
                <td><img alt="seedling" src="${servePath}/emoji/graphics/emojis/seedling.png"></td>
                <td><img alt="shell" src="${servePath}/emoji/graphics/emojis/shell.png"></td>
                <td><img alt="crescent_moon" src="${servePath}/emoji/graphics/emojis/crescent_moon.png"></td>
                <td><img alt="partly_sunny" src="${servePath}/emoji/graphics/emojis/partly_sunny.png"></td>
                <td><img alt="octocat" src="${servePath}/emoji/graphics/emojis/octocat.png"></td>
                <td><img alt="jack_o_lantern" src="${servePath}/emoji/graphics/emojis/jack_o_lantern.png"></td>
                <td><img alt="ghost" src="${servePath}/emoji/graphics/emojis/ghost.png"></td>
                <td><img alt="santa" src="${servePath}/emoji/graphics/emojis/santa.png"></td>
                <td><img alt="tada" src="${servePath}/emoji/graphics/emojis/tada.png"></td>
                <td><img alt="camera" src="${servePath}/emoji/graphics/emojis/camera.png"></td>
                <td><img alt="loudspeaker" src="${servePath}/emoji/graphics/emojis/loudspeaker.png"></td>
                <td><img alt="hourglass" src="${servePath}/emoji/graphics/emojis/hourglass.png"></td>
                <td><img alt="lock" src="${servePath}/emoji/graphics/emojis/lock.png"></td>
                <td><img alt="key" src="${servePath}/emoji/graphics/emojis/key.png"></td>
                <td><img alt="bulb" src="${servePath}/emoji/graphics/emojis/bulb.png"></td>
                <td><img alt="hammer" src="${servePath}/emoji/graphics/emojis/hammer.png"></td>
                <td><img alt="moneybag" src="${servePath}/emoji/graphics/emojis/moneybag.png"></td>
                <td><img alt="smoking" src="${servePath}/emoji/graphics/emojis/smoking.png"></td>
                <td><img alt="bomb" src="${servePath}/emoji/graphics/emojis/bomb.png"></td>
                <td><img alt="gun" src="${servePath}/emoji/graphics/emojis/gun.png"></td>
            </tr>
            <tr>
                <td><img alt="hocho" src="${servePath}/emoji/graphics/emojis/hocho.png"></td>
                <td><img alt="pill" src="${servePath}/emoji/graphics/emojis/pill.png"></td>
                <td><img alt="syringe" src="${servePath}/emoji/graphics/emojis/syringe.png"></td>
                <td><img alt="scissors" src="${servePath}/emoji/graphics/emojis/scissors.png"></td>
                <td><img alt="swimmer" src="${servePath}/emoji/graphics/emojis/swimmer.png"></td>
                <td><img alt="black_joker" src="${servePath}/emoji/graphics/emojis/black_joker.png"></td>
                <td><img alt="coffee" src="${servePath}/emoji/graphics/emojis/coffee.png"></td>
                <td><img alt="tea" src="${servePath}/emoji/graphics/emojis/tea.png"></td>
                <td><img alt="sake" src="${servePath}/emoji/graphics/emojis/sake.png"></td>
                <td><img alt="beer" src="${servePath}/emoji/graphics/emojis/beer.png"></td>
                <td><img alt="wine_glass" src="${servePath}/emoji/graphics/emojis/wine_glass.png"></td>
                <td><img alt="pizza" src="${servePath}/emoji/graphics/emojis/pizza.png"></td>
                <td><img alt="hamburger" src="${servePath}/emoji/graphics/emojis/hamburger.png"></td>
                <td><img alt="poultry_leg" src="${servePath}/emoji/graphics/emojis/poultry_leg.png"></td>
                <td><img alt="meat_on_bone" src="${servePath}/emoji/graphics/emojis/meat_on_bone.png"></td>
                <td><img alt="dango" src="${servePath}/emoji/graphics/emojis/dango.png"></td>
                <td><img alt="doughnut" src="${servePath}/emoji/graphics/emojis/doughnut.png"></td>
                <td><img alt="icecream" src="${servePath}/emoji/graphics/emojis/icecream.png"></td>
                <td><img alt="shaved_ice" src="${servePath}/emoji/graphics/emojis/shaved_ice.png"></td>
                <td><img alt="cake" src="${servePath}/emoji/graphics/emojis/cake.png"></td>
            </tr>
            <tr>
                <td><img alt="cookie" src="${servePath}/emoji/graphics/emojis/cookie.png"></td>
                <td><img alt="lollipop" src="${servePath}/emoji/graphics/emojis/lollipop.png"></td>
                <td><img alt="apple" src="${servePath}/emoji/graphics/emojis/apple.png"></td>
                <td><img alt="green_apple" src="${servePath}/emoji/graphics/emojis/green_apple.png"></td>
                <td><img alt="tangerine" src="${servePath}/emoji/graphics/emojis/tangerine.png"></td>
                <td><img alt="lemon" src="${servePath}/emoji/graphics/emojis/lemon.png"></td>
                <td><img alt="cherries" src="${servePath}/emoji/graphics/emojis/cherries.png"></td>
                <td><img alt="grapes" src="${servePath}/emoji/graphics/emojis/grapes.png"></td>
                <td><img alt="watermelon" src="${servePath}/emoji/graphics/emojis/watermelon.png"></td>
                <td><img alt="strawberry" src="${servePath}/emoji/graphics/emojis/strawberry.png"></td>
                <td><img alt="peach" src="${servePath}/emoji/graphics/emojis/peach.png"></td>
                <td><img alt="melon" src="${servePath}/emoji/graphics/emojis/melon.png"></td>
                <td><img alt="banana" src="${servePath}/emoji/graphics/emojis/banana.png"></td>
                <td><img alt="pear" src="${servePath}/emoji/graphics/emojis/pear.png"></td>
                <td><img alt="pineapple" src="${servePath}/emoji/graphics/emojis/pineapple.png"></td>
                <td><img alt="sweet_potato" src="${servePath}/emoji/graphics/emojis/sweet_potato.png"></td>
                <td><img alt="eggplant" src="${servePath}/emoji/graphics/emojis/eggplant.png"></td>
                <td><img alt="tomato" src="${servePath}/emoji/graphics/emojis/tomato.png"></td>
                <td colspan="2"><a target="_blank" href="${servePath}/emoji/index.html">${moreLabel}...</a></td>
            </tr>
        </table>
        <br><br>
        <div class="fn-clear"></div>
        <div id="emotionListTip" class="tip"></div>
        <div class="fn-hr5"></div>
        <button class="green fn-right" onclick="Settings.update('emotionList', '${csrfToken}')">${saveLabel}</button>
    </div>
</div>
</@home>
