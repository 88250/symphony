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
 *eating-snake.js
 *Author:Zephyr,Alexar
 *Alexar wrote it in love2d, Zephyr translate it into javascript
 *TODO：进行包装，否则作用域是全局的可能导致覆盖其他函数或变量
 */
var EatingSnake = {
    dir: null,
    lastDir: null,
    map: null,
    food: null,
    R: 10, //圆半径或者外接正方形尺寸的一半
    size: 30,
    snake: null,
    oMark: null, //分数显示框
    isPause: false, //是否暂停
    snakeCanvas: null,
    interval: null,
    currTime: 200,
    stepTime: 5,
    baseLen: 6,
    startTime: null,
    endTime: null,
    countTime: null,

    //1:snake
    //0:nothing
    //2:apple
    //3:block
    setupMap: function() {
        for (var x = 1; x <= this.size; x++) {
            this.map[x] = new Array();
            for (var y = 1; y <= this.size; y++) {
                if (x == 1 || x == this.size || y == 1 || y == this.size)
                    this.map[x][y] = 3
                else
                    this.map[x][y] = 0
            }
        }
    },

    initMap: function(oMarkId,snakeCanvasId) {
        this.oMark = document.getElementById(oMarkId);
        this.snakeCanvas = document.getElementById(snakeCanvasId).getContext('2d');
        this.map = new Array();
        this.setupMap();
        if(this.snakeCanvas!=null)
            this.snakeCanvas.clearRect(0, 0, (this.size - 1) * 2 * this.R, (this.size - 1) * 2 * this.R);
        for (var x = 1; x <= this.size; x++) {
            for (var y = 1; y <= this.size; y++) {
                switch (this.map[x][y]) {
                    case 0:
                        this.snakeCanvas.strokeStyle = "gray";
                        this.snakeCanvas.strokeRect((x - 1) * 2 * this.R, (y - 1) * 2 * this.R, 2 * this.R, 2 * this.R);
                        break;
                    case 1:
                        this.snakeCanvas.fillStyle = "black";
                        this.snakeCanvas.fillRect((x - 1) * 2 * this.R, (y - 1) * 2 * this.R, 2 * this.R, 2 * this.R);
                        break;
                    case 2:
                        this.snakeCanvas.fillStyle = "red";
                        this.snakeCanvas.fillRect((x - 1) * 2 * this.R, (y - 1) * 2 * this.R, 2 * this.R, 2 * this.R);
                        break;
                    case 3:
                        this.snakeCanvas.fillStyle = "gray";
                        this.snakeCanvas.fillRect((x - 1) * 2 * this.R, (y - 1) * 2 * this.R, 2 * this.R, 2 * this.R);
                        break;
                }
            }
        }
    },

    drawMap: function() {
        this.snakeCanvas.clearRect(0, 0, (this.size - 1) * 2 * this.R, (this.size - 1) * 2 * this.R);
        for (var x = 1; x <= this.size; x++) {
            for (var y = 1; y <= this.size; y++) {
                switch (this.map[x][y]) {
                    case 0:
                        this.snakeCanvas.strokeStyle = "gray";
                        this.snakeCanvas.strokeRect((x - 1) * 2 * this.R, (y - 1) * 2 * this.R, 2 * this.R, 2 * this.R);
                        break;
                    case 1:
                        this.snakeCanvas.fillStyle = "black";
                        this.snakeCanvas.fillRect((x - 1) * 2 * this.R, (y - 1) * 2 * this.R, 2 * this.R, 2 * this.R);
                        break;
                    case 2:
                        this.snakeCanvas.fillStyle = "red";
                        this.snakeCanvas.fillRect((x - 1) * 2 * this.R, (y - 1) * 2 * this.R, 2 * this.R, 2 * this.R);
                        break;
                    case 3:
                        this.snakeCanvas.fillStyle = "gray";
                        this.snakeCanvas.fillRect((x - 1) * 2 * this.R, (y - 1) * 2 * this.R, 2 * this.R, 2 * this.R);
                        break;
                }
            }
        }
    },


    check: function(x, y) {
        if (this.map[x][y] != 0)
            return true; //true代表此处有填充p
        else
            return false;
    },

    setupSnake: function() {
        for (var i = 1; i <= 5; i++) {
            this.snake[i] = {
                x: i + 5,
                y: 7
            };
        }
    },

    drawSnake: function(toggle) {
        for (var i = 1; i < this.snake.length; i++) {
            this.map[this.snake[i].x][this.snake[i].y] = toggle;
        }
    },

    newFood: function() {
        do {
            this.food.x = Math.floor(Math.random() * (this.size - 1) + 1);
            this.food.y = Math.floor(Math.random() * (this.size - 1) + 1);
        } while (this.check(this.food.x, this.food.y) == true)
        this.map[this.food.x][this.food.y] = 2;
    },

    gameover: function() {
        clearInterval(this.interval);
        //可以考虑不同分数不同提示
        alert("Game Over! 您的分数是：" + (this.snake.length - this.baseLen) + "！哇哦好厉害哟！");
        var requestJSONObject = {
            score: (this.snake.length - this.baseLen)
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
    },

    eat: function() {
        this.snake[this.snake.length] = {
            x: this.snake[1].x,
            y: this.snake[1].y
        };
        this.newFood();
        clearInterval(interval);
        if (this.currTime >= 50)
            this.currTime = this.currTime - this.stepTime;
        this.interval = setInterval(this.gameRun, this.currTime);
    },

    updateSnake: function() {
        this.lastDir.x = this.dir.x
        this.lastDir.y = this.dir.y
        var targetX = this.snake[1].x + this.dir.x,
            targetY = this.snake[1].y + this.dir.y;
        if (this.check(targetX, targetY)) {
            if (targetX == this.food.x && targetY == this.food.y) { //eat
                this.eat();
            } else { //hit
                this.gameover();
                return;
            }
        }
        this.drawSnake(0)
        for (var i = this.snake.length - 1; i >= 2; i--) {
            this.snake[i].x = this.snake[i - 1].x
            this.snake[i].y = this.snake[i - 1].y
        }
        this.snake[1].x = targetX
        this.snake[1].y = targetY

        this.drawSnake(1)
    },

    input: function(keyCode) {
        switch (keyCode) {
            case 65: //左边
                if (this.lastDir.x == 0) {
                    this.dir.x = -1;
                    this.dir.y = 0;
                }
                break;
            case 87: //上边
                if (this.lastDir.y == 0) {
                    this.dir.x = 0;
                    this.dir.y = -1;
                }
                break;
            case 68: //右边
                if (this.lastDir.x == 0) {
                    this.dir.x = 1;
                    this.dir.y = 0;
                }
                break;
            case 83: //下的
                if (this.lastDir.y == 0) {
                    this.dir.x = 0;
                    this.dir.y = 1;
                }
                break;
            case 80: //开始/暂停
                if (this.isPause) {
                    this.interval = setInterval(gameRun, currTime);
                    this.isPause = false;
                } else {
                    clearInterval(interval);
                    this.isPause = true;
                }
                break;
        }
    },
    init: function() {
        this.dir = {
            x: 0,
            y: 1
        };
        this.lastDir = {
            x: 0,
            y: 0
        };
        this.map = new Array();
        this.food = {
            x: 0,
            y: 0
        };
        this.currTime = 200;
        
        this.snake = new Array();
        this.setupMap();
        this.setupSnake();
        this.drawSnake(1);
        this.newFood();
        clearInterval(this.interval);
    },
    gameRun: function() {
        // countTime += currTime;
        this.updateSnake();
        this.drawMap();
        this.oMark.innerHtml = this.snake.length - this.baseLen;
    },
    start: function() {
        this.init();
        // countTime = 0;
        this.interval = setInterval(this.gameRun, this.currTime);
        // startTime = new Date().getTime();
    },
    
}