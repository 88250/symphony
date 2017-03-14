/*
 * Symphony - A modern community (forum/SNS/blog) platform written in Java.
 * Copyright (C) 2012-2017,  b3log.org & hacpai.com
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
/**
 * @fileOverview
 *
 * @author Zephyr
 * @version 1.0.0 Mar 14, 2017
 */
var Gobang = {
    dir: null,
    lastDir: null,
    map: null,
    food: null,
    R: 10, // 圆半径或者外接正方形尺寸的一半
    size: 30,
    snake: null,
    oMark: null, // 分数显示框
    isPause: false, // 是否暂停
    snakeCanvas: null,
    interval: null,
    currTime: 200,
    stepTime: 5,
    baseLen: 6,
    startTime: null,
    endTime: null,
    countTime: null,
    snakeColor: 0,
    appleColor: 255,
    // 1: snake
    // 0: nothing
    // 2: apple
    // 3: block
    setupMap: function () {
        for (var x = 1; x <= Gobang.size; x++) {
            Gobang.map[x] = new Array();
            for (var y = 1; y <= Gobang.size; y++) {
                if (x == 1 || x == Gobang.size || y == 1 || y == Gobang.size)
                    Gobang.map[x][y] = 3
                else
                    Gobang.map[x][y] = 0
            }
        }
    },
    initMap: function (oMarkId, snakeCanvasId) {
        // Gobang.oMark = document.getElementById(oMarkId);
        Gobang.snakeCanvas = document.getElementById(snakeCanvasId).getContext('2d');
        Gobang.map = new Array();
        Gobang.setupMap();
        if (Gobang.snakeCanvas != null)
            Gobang.Clear();
        for (var x = 1; x <= Gobang.size; x++) {
            for (var y = 1; y <= Gobang.size; y++) {
                Gobang.DrawMethod(Gobang.map[x][y],x,y);
            }
        }
    },
    drawMap: function () {
        Gobang.Clear();
        for (var x = 1; x <= Gobang.size; x++) {
            for (var y = 1; y <= Gobang.size; y++) {
                Gobang.DrawMethod(Gobang.map[x][y],x,y);
            }
        }
    },
    check: function (x, y) {
        if (Gobang.map[x][y] != 0)
            return true; // true代表此处有填充p
        else
            return false;
    },
    setupSnake: function () {
        for (var i = 1; i <= 5; i++) {
            Gobang.snake[i] = {
                x: i + 5,
                y: 7
            };
        }
    },
    drawSnake: function (toggle) {
        for (var i = 1; i < Gobang.snake.length; i++) {
            Gobang.map[Gobang.snake[i].x][Gobang.snake[i].y] = toggle;
        }
    },
    newFood: function () {
        do {
            Gobang.food.x = Math.floor(Math.random() * (Gobang.size - 1) + 1);
            Gobang.food.y = Math.floor(Math.random() * (Gobang.size - 1) + 1);
        } while (Gobang.check(Gobang.food.x, Gobang.food.y) == true)
        Gobang.map[Gobang.food.x][Gobang.food.y] = 2;
    },
    gameover: function () {
        clearInterval(Gobang.interval);

        var requestJSONObject = {
            score: (Gobang.snake.length - Gobang.baseLen)
        };

        $.ajax({
            url: Label.servePath + "/activity/eating-snake/collect",
            type: "POST",
            cache: false,
            data: JSON.stringify(requestJSONObject),
            beforeSend: function () {
                var $btn = $("button.green");
                $btn.attr("disabled", "disabled").css("opacity", "0.3").text($btn.text() + 'ing');
            },
            success: function (result, textStatus) {
                if (!result.sc) {
                    alert(result.msg);

                    return;
                }

                Gobang.snakeCanvas.fillStyle = "black";
                Gobang.snakeCanvas.fillRect(150, 100, 300, 200);
                Gobang.snakeCanvas.clearRect(155, 105, 290, 190);
                Gobang.snakeCanvas.font = '36px serif';
                var textWidth = Gobang.snakeCanvas.measureText("Game Over!").width;
                Gobang.snakeCanvas.fillText("Game Over!", 155 + (290 - textWidth) / 2, 150);
                Gobang.snakeCanvas.font = '24px serif';
                var score = Gobang.snake.length - Gobang.baseLen;
                textWidth = Gobang.snakeCanvas.measureText("Your Score: " + score).width;
                Gobang.snakeCanvas.fillText("Your Score: " + score, 155 + (290 - textWidth) / 2, 200);
                Gobang.snakeCanvas.fillStyle = "red";
                Gobang.snakeCanvas.font = "18px serif";
//                ctx.measureText(txt).width
                var resultText;
                if (score <= 10) {
                    resultText = "童鞋，换键盘吧，要不行换手";
                } else if (score > 10 && score <= 20) {
                    resultText = "如此平凡的分数恕我无力吐槽";
                } else if (score > 20 && score <= 30) {
                    resultText = "哇哦，好厉害哦！";
                } else if (score > 30 && score <= 40) {
                    resultText = "哎呀我滴老天爷呀";
                } else if (score > 40 && score <= 50) {
                    resultText = "请收下我的膝盖 OTZ";
                } else {
                    resultText = "太假了！(╯‵□′)╯︵┻━┻";
                }
                textWidth = Gobang.snakeCanvas.measureText(resultText).width;
                Gobang.snakeCanvas.fillText(resultText, 155 + (290 - textWidth) / 2, 250);
            },
            complete: function () {
                var $btn = $("button.green");
                $btn.removeAttr("disabled").css("opacity", "1").text($btn.text().substr(0, $btn.text().length - 3));

            }
        });

    },
    eat: function () {
        Gobang.snake[Gobang.snake.length] = {
            x: Gobang.snake[1].x,
            y: Gobang.snake[1].y
        };
        Gobang.newFood();
        Gobang.snakeColor += 5;
        Gobang.appleColor -= 5;
        clearInterval(Gobang.interval);
        if (Gobang.currTime >= 25) {
            Gobang.currTime = Gobang.currTime - Gobang.stepTime;
        }
        Gobang.interval = setInterval(Gobang.gameRun, Gobang.currTime);
    },
    updateSnake: function () {
        Gobang.lastDir.x = Gobang.dir.x
        Gobang.lastDir.y = Gobang.dir.y
        var targetX = Gobang.snake[1].x + Gobang.dir.x,
            targetY = Gobang.snake[1].y + Gobang.dir.y;
        if (Gobang.check(targetX, targetY)) {
            if (targetX == Gobang.food.x && targetY == Gobang.food.y) { // eat
                Gobang.eat();
            } else { // hit
                Gobang.gameover();
                return;
            }
        }
        Gobang.drawSnake(0)
        for (var i = Gobang.snake.length - 1; i >= 2; i--) {
            Gobang.snake[i].x = Gobang.snake[i - 1].x
            Gobang.snake[i].y = Gobang.snake[i - 1].y
        }
        Gobang.snake[1].x = targetX
        Gobang.snake[1].y = targetY

        Gobang.drawSnake(1)
    },
    input: function (keyCode) {
        switch (keyCode) {
            case 65:
            case 37: // 左边
                if (Gobang.lastDir.x == 0) {
                    Gobang.dir.x = -1;
                    Gobang.dir.y = 0;
                }
                break;
            case 87:
            case 38: // 上边
                if (Gobang.lastDir.y == 0) {
                    Gobang.dir.x = 0;
                    Gobang.dir.y = -1;
                }
                break;
            case 68:
            case 39: // 右边
                if (Gobang.lastDir.x == 0) {
                    Gobang.dir.x = 1;
                    Gobang.dir.y = 0;
                }
                break;
            case 83:
            case 40: // 下边
                if (Gobang.lastDir.y == 0) {
                    Gobang.dir.x = 0;
                    Gobang.dir.y = 1;
                }
                break;
            case 80: // 开始/暂停
                if (Gobang.isPause) {
                    Gobang.interval = setInterval(gameRun, currTime);
                    Gobang.isPause = false;
                } else {
                    clearInterval(interval);
                    Gobang.isPause = true;
                }
                break;
        }
    },
    init: function () {
        Gobang.dir = {
            x: 0,
            y: 1
        };
        Gobang.lastDir = {
            x: 0,
            y: 0
        };
        Gobang.map = new Array();
        Gobang.food = {
            x: 0,
            y: 0
        };
        Gobang.currTime = 200;

        Gobang.snake = new Array();
        Gobang.setupMap();
        Gobang.setupSnake();
        Gobang.drawSnake(1);
        Gobang.newFood();
        clearInterval(Gobang.interval);
    },
    gameRun: function () {
        Gobang.updateSnake();
        Gobang.drawMap();
//        Gobang.oMark.innerHtml = Gobang.snake.length - Gobang.baseLen;
    },
    start: function (csrfToken) {

        window.addEventListener('keydown', function (event) {
            // 如果游戏的时候时使用方向键
            if (event.keyCode == 37 || event.keyCode == 38 || event.keyCode == 39 || event.keyCode == 40) {
                // 阻止方向键滚屏
                event.preventDefault();
                return false;
            }
        });
        Gobang.init();
        Gobang.interval = setInterval(Gobang.gameRun, Gobang.currTime);
        // $.ajax({
        //     url: Label.servePath + "/activity/eating-snake/start",
        //     type: "POST",
        //     headers: {"csrfToken": csrfToken},
        //     cache: false,
        //     success: function (result, textStatus) {
        //         if (result.sc) {
        //             Gobang.init();
        //             Gobang.interval = setInterval(Gobang.gameRun, Gobang.currTime);
        //             return;
        //         } else {
        //             $("#tip").addClass("error").removeClass('succ').html('<ul><li>' + result.msg + '</li></ul>');
        //         }

        //         $("#tip").show();

        //         setTimeout(function () {
        //             $("#tip").hide();
        //         }, 3000);
        //     }
        // });
    },
    DrawMethod:function(mapPos,x,y){
        switch (mapPos) {
            case 0:
                Gobang.snakeCanvas.strokeStyle = "gray";
                Gobang.snakeCanvas.strokeRect((x - 1) * 2 * Gobang.R, (y - 1) * 2 * Gobang.R, 2 * Gobang.R, 2 * Gobang.R);
                break;
            case 1:
                Gobang.snakeCanvas.fillStyle = "rgb(" + Gobang.snakeColor + ",0,0)";
                Gobang.snakeCanvas.fillRect((x - 1) * 2 * Gobang.R, (y - 1) * 2 * Gobang.R, 2 * Gobang.R, 2 * Gobang.R);
                break;
            case 2:
                Gobang.snakeCanvas.fillStyle = "rgb(" + Gobang.appleColor + ",0,0)";
                Gobang.snakeCanvas.fillRect((x - 1) * 2 * Gobang.R, (y - 1) * 2 * Gobang.R, 2 * Gobang.R, 2 * Gobang.R);
                break;
            case 3:
                Gobang.snakeCanvas.fillStyle = "gray";
                Gobang.snakeCanvas.fillRect((x - 1) * 2 * Gobang.R, (y - 1) * 2 * Gobang.R, 2 * Gobang.R, 2 * Gobang.R);
                break;
        }
    },
    Clear:function(){
        Gobang.snakeCanvas.clearRect(0, 0, (Gobang.size - 1) * 2 * Gobang.R, (Gobang.size - 1) * 2 * Gobang.R);
    }
};
