package com.mygdx.game

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.utils.ScissorStack.getViewport
import android.provider.SyncStateContract.Helpers.update
import com.badlogic.gdx.*
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.GL20
import com.badlogic.gdx.scenes.scene2d.ui.Table
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Skin


abstract class BaseGame : Game() {
    // used to store resources common to multiple screens
    internal var skin: Skin

    init {
        skin = Skin()
    }

    abstract override fun create()

    override fun dispose() {
        skin.dispose()
    }
}

abstract class BaseScreen(protected var game: BaseGame) : Screen, InputProcessor {

    protected var mainStage: Stage
    protected var uiStage: Stage

    protected var uiTable: Table

    val viewWidth = 800
    val viewHeight = 600

    // pause methods
    var isPaused: Boolean = false

    init {

        mainStage = Stage(FitViewport(viewWidth.toFloat(), viewHeight.toFloat()))
        uiStage = Stage(FitViewport(viewWidth.toFloat(), viewHeight.toFloat()))

        uiTable = Table()
        uiTable.setFillParent(true)
        uiStage.addActor(uiTable)

        isPaused = false

        val im = InputMultiplexer(this, uiStage, mainStage)
        Gdx.input.inputProcessor = im

        create()
    }

    abstract fun create()

    abstract fun update(dt: Float)

    // this is the gameloop. update, then render.
    override fun render(dt: Float) {
        uiStage.act(dt)

        // only pause gameplay events, not UI events
        if (!isPaused) {
            mainStage.act(dt)
            update(dt)
        }

        // render
        Gdx.gl.glClearColor(0f, 0f, 0f, 1f)
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT)

        mainStage.draw()
        uiStage.draw()
    }

    fun togglePaused() {
        isPaused = !isPaused
    }

    // methods required by Screen interface
    override fun resize(width: Int, height: Int) {
        mainStage.viewport.update(width, height, true)
        uiStage.viewport.update(width, height, true)
    }

    override fun pause() {}

    override fun resume() {}

    override fun dispose() {}

    override fun show() {}

    override fun hide() {}

    // methods required by InputProcessor interface
    override fun keyDown(keycode: Int): Boolean {
        return false
    }

    override fun keyUp(keycode: Int): Boolean {
        return false
    }

    override fun keyTyped(c: Char): Boolean {
        return false
    }

    override fun mouseMoved(screenX: Int, screenY: Int): Boolean {
        return false
    }

    override fun scrolled(amount: Int): Boolean {
        return false
    }

    override fun touchDown(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }

    override fun touchDragged(screenX: Int, screenY: Int, pointer: Int): Boolean {
        return false
    }

    override fun touchUp(screenX: Int, screenY: Int, pointer: Int, button: Int): Boolean {
        return false
    }
}

class GameScreen(g: BaseGame) : BaseScreen(g) {
    private var paddle: Paddle? = null
    private var ball: Ball? = null

    private var baseBrick: Brick? = null
    private var brickList: ArrayList<Brick>? = null

    private var basePowerup: Powerup? = null
    private var powerupList: ArrayList<Powerup>? = null

    private var removeList: ArrayList<BaseActor>? = null

    // game world dimensions
    internal val mapWidth = 800
    internal val mapHeight = 600

    override fun create() {
        paddle = Paddle()
        val paddleTex = Texture(Gdx.files.internal("paddle.png"))
        paddleTex.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        paddle!!.setTexture(paddleTex)
        paddle!!.setRectangleBoundary()
        mainStage.addActor(paddle)

        baseBrick = Brick()
        val brickTex = Texture(Gdx.files.internal("brick-gray.png"))
        baseBrick!!.setTexture(brickTex)
        baseBrick!!.setOriginCenter()

        brickList = ArrayList()

        ball = Ball()
        val ballTex = Texture(Gdx.files.internal("ball.png"))
        ball!!.storeAnimation("default", ballTex)
        ball!!.setPosition(400f, 200f)
        ball!!.setVelocityAS(30f, 300f)
        ball!!.setAccelerationXY(0f, -10f)
        ball!!.setEllipseBoundary()
        mainStage.addActor(ball)

        basePowerup = Powerup()
        basePowerup!!.setVelocityXY(0f, -100f)
        basePowerup!!.storeAnimation("paddle-expand",
                Texture(Gdx.files.internal("paddle-expand.png")))
        basePowerup!!.storeAnimation("paddle-shrink",
                Texture(Gdx.files.internal("paddle-shrink.png")))
        basePowerup!!.setOriginCenter()

        powerupList = ArrayList()

        val colorArray = arrayOf<Color>(Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE, Color.PURPLE)

        for (j in 0..5) {
            for (i in 0..9) {
                val brick = baseBrick!!.clone()
                brick.setPosition((8 + 80 * i).toFloat(), (500 - (24 + 16) * j).toFloat())
                brick.color = colorArray[j]
                brickList!!.add(brick)
                brick.setParentList(brickList!!)
                brick.setRectangleBoundary()
                mainStage.addActor(brick)
            }
        }

        removeList = ArrayList()
    }

    override fun update(dt: Float) {
        // adjust paddle position to horizontal mouse coordinate

        paddle!!.setPosition(Gdx.input.x - paddle!!.width / 2, 32f)

        // bound paddle to screen

        if (paddle!!.x < 0)
            paddle!!.x = 0f

        if (paddle!!.x + paddle!!.width > mapWidth)
            paddle!!.x = mapWidth - paddle!!.width

        // bounce ball off screen edges

        if (ball!!.x < 0) {
            ball!!.x = 0f
            ball!!.multVelocityX(-1f)
        }

        if (ball!!.x + ball!!.width > mapWidth) {
            ball!!.x = mapWidth - ball!!.width
            ball!!.multVelocityX(-1f)
        }

        if (ball!!.y < 0) {
            ball!!.y = 0f
            ball!!.multVelocityY(-1f)
        }

        if (ball!!.y + ball!!.height > mapHeight) {
            ball!!.y = mapHeight - ball!!.height
            ball!!.multVelocityY(-1f)
        }

        // bounce ball off paddle

        if (ball!!.overlaps(paddle!!, true)) {
            // ball.overlaps(paddle, true);
            // play boing sound
        }

        removeList!!.clear()

        for (br in brickList!!) {
            if (ball!!.overlaps(br, true))
            // bounces off bricks
            {
                removeList!!.add(br)
                if (Math.random() < 0.20) {
                    val pow = basePowerup!!.clone()
                    pow.randomize()
                    pow.moveToOrigin(br)

                    pow.setScale(0f, 0f)
                    pow.addAction(Actions.scaleTo(1f, 1f, 0.5f))

                    powerupList!!.add(pow)
                    pow.setParentList(powerupList!!)
                    mainStage.addActor(pow)
                }
            }
        }

        for (pow in powerupList!!) {
            if (pow.overlaps(paddle!!)) {
                val powName = pow.animationName
                if (powName == "paddle-expand" && paddle!!.width < 256) {
                    paddle!!.addAction(Actions.sizeBy(32f, 0f, 0.5f))
                } else if (powName == "paddle-shrink" && paddle!!.width > 64) {
                    paddle!!.addAction(Actions.sizeBy(-32f, 0f, 0.5f))
                }

                removeList!!.add(pow)
            }
        }

        for (b in removeList!!) {
            b.destroy()
        }
    }

    // InputProcessor methods for handling discrete input
    override fun keyDown(keycode: Int): Boolean {
        if (keycode == Input.Keys.P)
            togglePaused()

        if (keycode == Input.Keys.R)
            game.setScreen(GameScreen(game))

        return false
    }
}

class RectangleDestroyerGame : BaseGame() {
    override fun create() {
        // initialize resources common to multiple screens

        // load game screen
        val gs = GameScreen(this)
        setScreen(gs)
    }
}