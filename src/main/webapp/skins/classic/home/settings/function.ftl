<style>
img{
	width:25px;
	height:25px;
}
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
        <label>${useNotifyLabel} 
            <input id="userNotifyStatus" <#if 0 == currentUser.userNotifyStatus> checked="checked"</#if> type="checkbox" /> 
        </label>   
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
        <td><img alt="smile" src="/emoji/graphics/emojis/smile.png"></td>
        <td><img alt="laughing" src="/emoji/graphics/emojis/laughing.png"></td>
        <td><img alt="smirk" src="/emoji/graphics/emojis/smirk.png"></td>
        <td><img alt="heart_eyes" src="/emoji/graphics/emojis/heart_eyes.png"></td>
        <td><img alt="kissing_heart" src="/emoji/graphics/emojis/kissing_heart.png"></td>
        <td><img alt="flushed" src="/emoji/graphics/emojis/flushed.png"></td>
        <td><img alt="grin" src="/emoji/graphics/emojis/grin.png"></td>
        <td><img alt="stuck_out_tongue_closed_eyes" src="/emoji/graphics/emojis/stuck_out_tongue_closed_eyes.png"></td>
        <td><img alt="kissing" src="/emoji/graphics/emojis/kissing.png"></td>
        <td><img alt="sleeping" src="/emoji/graphics/emojis/sleeping.png"></td>
        <td><img alt="anguished" src="/emoji/graphics/emojis/anguished.png"></td>
        <td><img alt="open_mouth" src="/emoji/graphics/emojis/open_mouth.png"></td>
        <td><img alt="expressionless" src="/emoji/graphics/emojis/expressionless.png"></td>
        <td><img alt="unamused" src="/emoji/graphics/emojis/unamused.png"></td>
        <td><img alt="sweat_smile" src="/emoji/graphics/emojis/sweat_smile.png"></td>
        <td><img alt="weary" src="/emoji/graphics/emojis/weary.png"></td>
        <td><img alt="sob" src="/emoji/graphics/emojis/sob.png"></td>
        <td><img alt="joy" src="/emoji/graphics/emojis/joy.png"></td>
        <td><img alt="astonished" src="/emoji/graphics/emojis/astonished.png"></td>
        <td><img alt="scream" src="/emoji/graphics/emojis/scream.png"></td>
      </tr>
      <tr>
        <td><img alt="tired_face" src="/emoji/graphics/emojis/tired_face.png"></td>
        <td><img alt="rage" src="/emoji/graphics/emojis/rage.png"></td>
        <td><img alt="triumph" src="/emoji/graphics/emojis/triumph.png"></td>
        <td><img alt="yum" src="/emoji/graphics/emojis/yum.png"></td>
        <td><img alt="mask" src="/emoji/graphics/emojis/mask.png"></td>
        <td><img alt="sunglasses" src="/emoji/graphics/emojis/sunglasses.png"></td>
        <td><img alt="dizzy_face" src="/emoji/graphics/emojis/dizzy_face.png"></td>
        <td><img alt="imp" src="/emoji/graphics/emojis/imp.png"></td>
        <td><img alt="smiling_imp" src="/emoji/graphics/emojis/smiling_imp.png"></td>
        <td><img alt="innocent" src="/emoji/graphics/emojis/innocent.png"></td>
        <td><img alt="alien" src="/emoji/graphics/emojis/alien.png"></td>
        <td><img alt="yellow_heart" src="/emoji/graphics/emojis/yellow_heart.png"></td>
        <td><img alt="blue_heart" src="/emoji/graphics/emojis/blue_heart.png"></td>
        <td><img alt="purple_heart" src="/emoji/graphics/emojis/purple_heart.png"></td>
        <td><img alt="heart" src="/emoji/graphics/emojis/heart.png"></td>
        <td><img alt="green_heart" src="/emoji/graphics/emojis/green_heart.png"></td>
        <td><img alt="broken_heart" src="/emoji/graphics/emojis/broken_heart.png"></td>
        <td><img alt="dizzy" src="/emoji/graphics/emojis/dizzy.png"></td>
        <td><img alt="anger" src="/emoji/graphics/emojis/anger.png"></td>
        <td><img alt="exclamation" src="/emoji/graphics/emojis/exclamation.png"></td>
      </tr>
      <tr>
        <td><img alt="question" src="/emoji/graphics/emojis/question.png"></td>
        <td><img alt="zzz" src="/emoji/graphics/emojis/zzz.png"></td>
        <td><img alt="notes" src="/emoji/graphics/emojis/notes.png"></td>
        <td><img alt="shit" src="/emoji/graphics/emojis/shit.png"></td>
        <td><img alt="+1" src="/emoji/graphics/emojis/+1.png"></td>
        <td><img alt="-1" src="/emoji/graphics/emojis/-1.png"></td>
        <td><img alt="ok_hand" src="/emoji/graphics/emojis/ok_hand.png"></td>
        <td><img alt="punch" src="/emoji/graphics/emojis/punch.png"></td>
        <td><img alt="v" src="/emoji/graphics/emojis/v.png"></td>
        <td><img alt="hand" src="/emoji/graphics/emojis/hand.png"></td>                    
        <td><img alt="point_up" src="/emoji/graphics/emojis/point_up.png"></td>
        <td><img alt="point_down" src="/emoji/graphics/emojis/point_down.png"></td>
        <td><img alt="pray" src="/emoji/graphics/emojis/pray.png"></td>
        <td><img alt="clap" src="/emoji/graphics/emojis/clap.png"></td>
        <td><img alt="muscle" src="/emoji/graphics/emojis/muscle.png"></td>
        <td><img alt="ok_woman" src="/emoji/graphics/emojis/ok_woman.png"></td>
        <td><img alt="no_good" src="/emoji/graphics/emojis/no_good.png"></td>
        <td><img alt="raising_hand" src="/emoji/graphics/emojis/raising_hand.png"></td>
        <td><img alt="massage" src="/emoji/graphics/emojis/massage.png"></td>
        <td><img alt="haircut" src="/emoji/graphics/emojis/haircut.png"></td>
      </tr>
      <tr>
        <td><img alt="nail_care" src="/emoji/graphics/emojis/nail_care.png"></td>
        <td><img alt="see_no_evil" src="/emoji/graphics/emojis/see_no_evil.png"></td>
        <td><img alt="feet" src="/emoji/graphics/emojis/feet.png"></td>
        <td><img alt="kiss" src="/emoji/graphics/emojis/kiss.png"></td>
        <td><img alt="eyes" src="/emoji/graphics/emojis/eyes.png"></td>
        <td><img alt="trollface" src="/emoji/graphics/emojis/trollface.png"></td>
        <td><img alt="snowman" src="/emoji/graphics/emojis/snowman.png"></td>
        <td><img alt="zap" src="/emoji/graphics/emojis/zap.png"></td>
        <td><img alt="cat" src="/emoji/graphics/emojis/cat.png"></td>
        <td><img alt="dog" src="/emoji/graphics/emojis/dog.png"></td>
        <td><img alt="mouse" src="/emoji/graphics/emojis/mouse.png"></td>
        <td><img alt="hamster" src="/emoji/graphics/emojis/hamster.png"></td>
        <td><img alt="rabbit" src="/emoji/graphics/emojis/rabbit.png"></td>
        <td><img alt="frog" src="/emoji/graphics/emojis/frog.png"></td>
        <td><img alt="koala" src="/emoji/graphics/emojis/koala.png"></td>
        <td><img alt="pig" src="/emoji/graphics/emojis/pig.png"></td>
        <td><img alt="monkey" src="/emoji/graphics/emojis/monkey.png"></td>
        <td><img alt="racehorse" src="/emoji/graphics/emojis/racehorse.png"></td>
        <td><img alt="camel" src="/emoji/graphics/emojis/camel.png"></td>
        <td><img alt="sheep" src="/emoji/graphics/emojis/sheep.png"></td>
      </tr>
      <tr>
        <td><img alt="elephant" src="/emoji/graphics/emojis/elephant.png"></td>
        <td><img alt="panda_face" src="/emoji/graphics/emojis/panda_face.png"></td>
        <td><img alt="snake" src="/emoji/graphics/emojis/snake.png"></td>
        <td><img alt="hatched_chick" src="/emoji/graphics/emojis/hatched_chick.png"></td>
        <td><img alt="hatching_chick" src="/emoji/graphics/emojis/hatching_chick.png"></td>
        <td><img alt="turtle" src="/emoji/graphics/emojis/turtle.png"></td>
        <td><img alt="bug" src="/emoji/graphics/emojis/bug.png"></td>
        <td><img alt="honeybee" src="/emoji/graphics/emojis/honeybee.png"></td>
        <td><img alt="beetle" src="/emoji/graphics/emojis/beetle.png"></td>
        <td><img alt="snail" src="/emoji/graphics/emojis/snail.png"></td>
        
        <td><img alt="octopus" src="/emoji/graphics/emojis/octopus.png"></td>
        <td><img alt="whale" src="/emoji/graphics/emojis/whale.png"></td>
        <td><img alt="dolphin" src="/emoji/graphics/emojis/dolphin.png"></td>
        <td><img alt="dragon" src="/emoji/graphics/emojis/dragon.png"></td>
        <td><img alt="goat" src="/emoji/graphics/emojis/goat.png"></td>
        <td><img alt="paw_prints" src="/emoji/graphics/emojis/paw_prints.png"></td>
        <td><img alt="tulip" src="/emoji/graphics/emojis/tulip.png"></td>
        <td><img alt="four_leaf_clover" src="/emoji/graphics/emojis/four_leaf_clover.png"></td>
        <td><img alt="rose" src="/emoji/graphics/emojis/rose.png"></td>
        <td><img alt="mushroom" src="/emoji/graphics/emojis/mushroom.png"></td>
      </tr>
      <tr>
        <td><img alt="seedling" src="/emoji/graphics/emojis/seedling.png"></td>
        <td><img alt="shell" src="/emoji/graphics/emojis/shell.png"></td>
        <td><img alt="crescent_moon" src="/emoji/graphics/emojis/crescent_moon.png"></td>
        <td><img alt="partly_sunny" src="/emoji/graphics/emojis/partly_sunny.png"></td>
        <td><img alt="octocat" src="/emoji/graphics/emojis/octocat.png"></td>
        <td><img alt="jack_o_lantern" src="/emoji/graphics/emojis/jack_o_lantern.png"></td>
        <td><img alt="ghost" src="/emoji/graphics/emojis/ghost.png"></td>
        <td><img alt="santa" src="/emoji/graphics/emojis/santa.png"></td>
        <td><img alt="tada" src="/emoji/graphics/emojis/tada.png"></td>
        <td><img alt="camera" src="/emoji/graphics/emojis/camera.png"></td>
        <td><img alt="loudspeaker" src="/emoji/graphics/emojis/loudspeaker.png"></td>
        <td><img alt="hourglass" src="/emoji/graphics/emojis/hourglass.png"></td>
        <td><img alt="lock" src="/emoji/graphics/emojis/lock.png"></td>
        <td><img alt="key" src="/emoji/graphics/emojis/key.png"></td>
        <td><img alt="bulb" src="/emoji/graphics/emojis/bulb.png"></td>
        <td><img alt="hammer" src="/emoji/graphics/emojis/hammer.png"></td>
        <td><img alt="moneybag" src="/emoji/graphics/emojis/moneybag.png"></td>
        <td><img alt="smoking" src="/emoji/graphics/emojis/smoking.png"></td>
        <td><img alt="bomb" src="/emoji/graphics/emojis/bomb.png"></td>
        <td><img alt="gun" src="/emoji/graphics/emojis/gun.png"></td>
      </tr>
      <tr>
        <td><img alt="hocho" src="/emoji/graphics/emojis/hocho.png"></td>
        <td><img alt="pill" src="/emoji/graphics/emojis/pill.png"></td>
        <td><img alt="syringe" src="/emoji/graphics/emojis/syringe.png"></td>
        <td><img alt="scissors" src="/emoji/graphics/emojis/scissors.png"></td>
        <td><img alt="swimmer" src="/emoji/graphics/emojis/swimmer.png"></td>
        <td><img alt="black_joker" src="/emoji/graphics/emojis/black_joker.png"></td>
        <td><img alt="coffee" src="/emoji/graphics/emojis/coffee.png"></td>
        <td><img alt="tea" src="/emoji/graphics/emojis/tea.png"></td>
        <td><img alt="sake" src="/emoji/graphics/emojis/sake.png"></td>
        <td><img alt="beer" src="/emoji/graphics/emojis/beer.png"></td>
        <td><img alt="wine_glass" src="/emoji/graphics/emojis/wine_glass.png"></td>
        <td><img alt="pizza" src="/emoji/graphics/emojis/pizza.png"></td>
        <td><img alt="hamburger" src="/emoji/graphics/emojis/hamburger.png"></td>
        <td><img alt="poultry_leg" src="/emoji/graphics/emojis/poultry_leg.png"></td>
        <td><img alt="meat_on_bone" src="/emoji/graphics/emojis/meat_on_bone.png"></td>
        <td><img alt="dango" src="/emoji/graphics/emojis/dango.png"></td>
        <td><img alt="doughnut" src="/emoji/graphics/emojis/doughnut.png"></td>
        <td><img alt="icecream" src="/emoji/graphics/emojis/icecream.png"></td>
        <td><img alt="shaved_ice" src="/emoji/graphics/emojis/shaved_ice.png"></td>
        <td><img alt="cake" src="/emoji/graphics/emojis/cake.png"></td>
      </tr>
      <tr>
        <td><img alt="cookie" src="/emoji/graphics/emojis/cookie.png"></td>
        <td><img alt="lollipop" src="/emoji/graphics/emojis/lollipop.png"></td>
        <td><img alt="apple" src="/emoji/graphics/emojis/apple.png"></td>
        <td><img alt="green_apple" src="/emoji/graphics/emojis/green_apple.png"></td>
        <td><img alt="tangerine" src="/emoji/graphics/emojis/tangerine.png"></td>
        <td><img alt="lemon" src="/emoji/graphics/emojis/lemon.png"></td>
        <td><img alt="cherries" src="/emoji/graphics/emojis/cherries.png"></td>
        <td><img alt="grapes" src="/emoji/graphics/emojis/grapes.png"></td>
        <td><img alt="watermelon" src="/emoji/graphics/emojis/watermelon.png"></td>
        <td><img alt="strawberry" src="/emoji/graphics/emojis/strawberry.png"></td>
        <td><img alt="peach" src="/emoji/graphics/emojis/peach.png"></td>
        <td><img alt="melon" src="/emoji/graphics/emojis/melon.png"></td>
        <td><img alt="banana" src="/emoji/graphics/emojis/banana.png"></td>
        <td><img alt="pear" src="/emoji/graphics/emojis/pear.png"></td>
        <td><img alt="pineapple" src="/emoji/graphics/emojis/pineapple.png"></td>
        <td><img alt="sweet_potato" src="/emoji/graphics/emojis/sweet_potato.png"></td>
        <td><img alt="eggplant" src="/emoji/graphics/emojis/eggplant.png"></td>
        <td><img alt="tomato" src="/emoji/graphics/emojis/tomato.png"></td>
        <td colspan="2"><a target="_blank" href="${servePath}/emoji/index.html">更多...</a></td>
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
