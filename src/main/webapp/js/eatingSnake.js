/*
 * Copyright (c) 2012-2016, b3log.org & hacpai.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 *Zephyr：贪吃蛇常量，暂时放在这里
 *严重BUG：
 *如下可以轻易复现：蛇身向右走的时候，迅速按下下（S）和左（A），即弹出失败
 *可能原因：下这个键事件未来得及捕捉就进行了左，于是与蛇身相撞，判定为失败，如果修改了碰撞逻辑，如蛇身和苹果用不同的值
 *可能会出现直接倒退前进的现象。
 *暂时不管
 */
var dir = null;
var lastDir = null
var map = null;
var food = null;
var R = 10; //圆半径或者外接正方形尺寸的一半
var size = 30
var snake = null;
var oMark = document.getElementById('mark_con'); //分数显示框
var isPause = false; //是否暂停
var snakeCanvas = document.getElementById('snakeCanvas').getContext('2d');
var interval = null;
var currTime = 200;
var stepTime = 5;
var baseLen=6;
var startTime=null;
var endTime=null;
var countTime=null;

function init() {
    dir = {
        x: 0,
        y: 1
    };
    lastDir = {
        x: 0,
        y: 0
    };
    map = new Array();
    food = {
        x: 0,
        y: 0
    };
    snake = new Array();
    setupMap();
    setupSnake();
    drawSnake(1);
    newFood();
    clearInterval(interval);
}

function start() {
    init();
    countTime=0;
    interval = setInterval(gameRun, currTime);
    startTime=new Date().getTime();
}

function gameRun() {
    countTime+=currTime;
    updateSnake();
    drawMap();
    oMark.innerHtml=snake.length-baseLen;
}

//1:snake
//0:nothing
//2:apple
//3:block
function setupMap() {
    for (var x = 1; x <= size; x++) {
        map[x] = new Array();
        for (var y = 1; y <= size; y++) {
            if (x == 1 || x == size || y == 1 || y == size)
                map[x][y] = 3
            else
                map[x][y] = 0
        }
    }
}

function initMap() {
    map = new Array();
    setupMap();
    snakeCanvas.clearRect(0, 0, (size - 1) * 2 * R, (size - 1) * 2 * R);
    for (var x = 1; x <= size; x++) {
        for (var y = 1; y <= size; y++) {
            switch(map[x][y]){
                case 0:
                    snakeCanvas.strokeStyle = "gray";
                    snakeCanvas.strokeRect((x - 1) * 2 * R, (y - 1) * 2 * R, 2 * R, 2 * R); 
                    break;
                case 1:
                    snakeCanvas.fillStyle = "black";
                    snakeCanvas.fillRect((x - 1) * 2 * R, (y - 1) * 2 * R, 2 * R, 2 * R);
                    break;
                case 2:
                    snakeCanvas.fillStyle = "red";
                    snakeCanvas.fillRect((x - 1) * 2 * R, (y - 1) * 2 * R, 2 * R, 2 * R);
                    break;
                case 3:
                    snakeCanvas.fillStyle = "gray";
                    snakeCanvas.fillRect((x - 1) * 2 * R, (y - 1) * 2 * R, 2 * R, 2 * R);
                    break;
            }
        }
    }
}

function drawMap() {
    snakeCanvas.clearRect(0, 0, (size - 1) * 2 * R, (size - 1) * 2 * R);
    for (var x = 1; x <= size; x++) {
        for (var y = 1; y <= size; y++) {
            switch(map[x][y]){
                case 0:
                    snakeCanvas.strokeStyle = "gray";
                    snakeCanvas.strokeRect((x - 1) * 2 * R, (y - 1) * 2 * R, 2 * R, 2 * R); 
                    break;
                case 1:
                    snakeCanvas.fillStyle = "black";
                    snakeCanvas.fillRect((x - 1) * 2 * R, (y - 1) * 2 * R, 2 * R, 2 * R);
                    break;
                case 2:
                    snakeCanvas.fillStyle = "red";
                    snakeCanvas.fillRect((x - 1) * 2 * R, (y - 1) * 2 * R, 2 * R, 2 * R);
                    break;
                case 3:
                    snakeCanvas.fillStyle = "gray";
                    snakeCanvas.fillRect((x - 1) * 2 * R, (y - 1) * 2 * R, 2 * R, 2 * R);
                    break;
            }
        }
    }
}


function check(x, y) {
    if(map[x][y]!=0)
        return true;//true代表此处有填充p
    else
        return false;
}

function setupSnake() {
    for (var i = 1; i <= 5; i++) {
        snake[i] = {
            x: i + 5,
            y: 7
        };
    }
}

function drawSnake(toggle) {
    for (var i = 1; i < snake.length; i++) {
        map[snake[i].x][snake[i].y] = toggle;
    }
}

function newFood() {
    do {
        food.x = Math.floor(Math.random() * (size-1) + 1);
        food.y = Math.floor(Math.random() * (size-1) + 1);
    } while (check(food.x, food.y) == true)
    map[food.x][food.y] = 2;
}

function gameover() {
    endTime=new Date().getTime();
    console.log(endTime-startTime);
    console.log(countTime);
    clearInterval(interval);
    //可以考虑不同分数不同提示
    alert("Game Over! 您的分数是：" + (snake.length - baseLen) + "！哇哦好厉害哟！");
    var requestJSONObject = {
            score: (snake.length - baseLen)
        };
    $.ajax({
        url: Label.servePath + "/activity/eatingSnake/gameOver",
        type: "POST",
        cache: false,
        data: JSON.stringify(requestJSONObject),
        beforeSend: function() {
            var $btn = $("button.green");
            $btn.attr("disabled", "disabled").css("opacity", "0.3").text($btn.text() + 'ing');
        },
        success: function(result, textStatus) {
            alert(result.msg);

            if (result.sc) {
                window.location.reload();
            }
        },
        complete: function() {
            var $btn = $("button.green");
            $btn.removeAttr("disabled").css("opacity", "1").text($btn.text().substr(0, $btn.text().length - 3));
        }
    });
}

function eat() {
    snake[snake.length] = {
        x: snake[1].x,
        y: snake[1].y
    };
    newFood();
    clearInterval(interval);
    if (currTime >= 50)
        currTime = currTime - stepTime;
    interval = setInterval(gameRun, currTime);
}

function updateSnake() {
    lastDir.x = dir.x
    lastDir.y = dir.y
    var targetX = snake[1].x + dir.x,
        targetY = snake[1].y + dir.y;
    if (check(targetX, targetY)) {
        if (targetX == food.x && targetY == food.y) { //eat
            eat();
        } else { //hit
            gameover();
            return;
        }
    }
    drawSnake(0)
    for (var i = snake.length - 1; i >= 2; i--) {
        snake[i].x = snake[i - 1].x
        snake[i].y = snake[i - 1].y
    }
    snake[1].x = targetX
    snake[1].y = targetY

    drawSnake(1)
}

function input(keyCode) {
    switch (keyCode) {
        case 65: //左边
            if (lastDir.x == 0) {
                dir.x = -1;
                dir.y = 0;
            }
            break;
        case 87: //上边
            if (lastDir.y == 0) {
                dir.x = 0;
                dir.y = -1;
            }
            break;
        case 68: //右边
            if (lastDir.x == 0) {
                dir.x = 1;
                dir.y = 0;
            }
            break;
        case 83: //下的
            if (lastDir.y == 0) {
                dir.x = 0;
                dir.y = 1;
            }
            break;
        case 80: //开始/暂停
            if (isPause) {
                interval = setInterval(gameRun, currTime);
                isPause = false;
            } else {
                clearInterval(interval);
                isPause = true;
            }
            break;
    }
}