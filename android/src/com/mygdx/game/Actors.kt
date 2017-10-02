package com.mygdx.game


import com.badlogic.gdx.math.*
import com.badlogic.gdx.scenes.scene2d.actions.Actions


class Paddle() : BaseActor() {
    val rectangle
        get() = Rectangle(x, y, width, height)
}

class Brick : BaseActor() {

    val rectangle: Rectangle
        get() = Rectangle(x, y, width, height)

    override fun clone(): Brick {
        val newbie = Brick()
        newbie.copy(this)
        return newbie
    }

    override fun destroy() {
        addAction(Actions.sequence(Actions.fadeOut(0.5f), Actions.removeActor()))

        if (_parentList != null)
            _parentList!!.remove(this)
    }
}

class Ball : PhysicsActor() {

    val circle: Circle
        get() = Circle(getX() + getWidth() / 2, getY() + getHeight() / 2, (getWidth() / 2).toFloat())

    private var prevCircle: Circle? = null
    private var currCircle: Circle? = null

    fun overlaps(paddle: Paddle, bounceOff: Boolean): Boolean {
        if (!Intersector.overlaps(this.circle, paddle.rectangle))
            return false

        if (bounceOff) {
            val ballCenterX = this.getX() + this.getWidth() / 2
            val percent = (ballCenterX - paddle.x) / paddle.width
            val bounceAngle = 150 - percent * 120 // 150 to 30
            this.setVelocityAS(bounceAngle, this.speed)
        }
        return true
    }

    fun multVelocityX(m: Float) {
        velocity.x *= m
    }

    fun multVelocityY(m: Float) {
        velocity.y *= m
    }

    override fun act(dt: Float) {
        // store previous position before and after updating...
        prevCircle = circle
        super.act(dt)
        currCircle = circle
    }

    fun getTop(c: Circle?): Vector2 {
        return Vector2(c!!.x, c.y + c.radius)
    }

    fun getBottom(c: Circle?): Vector2 {
        return Vector2(c!!.x, c.y - c.radius)
    }

    fun getLeft(c: Circle?): Vector2 {
        return Vector2(c!!.x - c.radius, c.y)
    }

    fun getRight(c: Circle?): Vector2 {
        return Vector2(c!!.x + c.radius, c.y)
    }

    fun getBottomLeft(r: Rectangle): Vector2 {
        return Vector2(r.getX(), r.getY())
    }

    fun getBottomRight(r: Rectangle): Vector2 {
        return Vector2(r.getX() + r.getWidth(), r.getY())
    }

    fun getTopLeft(r: Rectangle): Vector2 {
        return Vector2(r.getX(), r.getY() + r.getHeight())
    }

    fun getTopRight(r: Rectangle): Vector2 {
        return Vector2(r.getX() + r.getWidth(), r.getY() + r.getHeight())
    }

    // returns true is a signal to add to remove list, get points, etc.
    // usually bounceOff true, but might be false for "thru-ball"-like effects
    fun overlaps(brick: Brick, bounceOff: Boolean): Boolean {
        if (!Intersector.overlaps(this.circle, brick.rectangle))
            return false

        if (bounceOff) {
            val rect = brick.rectangle
            var sideHit = false

            if (velocity.x > 0 && Intersector.intersectSegments(
                    getRight(prevCircle), getRight(currCircle),
                    getTopLeft(rect), getBottomLeft(rect), null)) {
                multVelocityX(-1f)
                sideHit = true
            } else if (velocity.x < 0 && Intersector.intersectSegments(
                    getLeft(prevCircle), getLeft(currCircle),
                    getTopRight(rect), getBottomRight(rect), null)) {
                multVelocityX(-1f)
                sideHit = true
            }

            if (velocity.y > 0 && Intersector.intersectSegments(
                    getTop(prevCircle), getTop(currCircle),
                    getBottomLeft(rect), getBottomRight(rect), null)) {
                multVelocityY(-1f)
                sideHit = true
            } else if (velocity.y < 0 && Intersector.intersectSegments(
                    getBottom(prevCircle), getBottom(currCircle),
                    getTopLeft(rect), getTopRight(rect), null)) {
                multVelocityY(-1f)
                sideHit = true
            }

            if (!sideHit)
            // well, something hit. then, corner hit! change both dir.
            {
                multVelocityX(-1f)
                multVelocityY(-1f)
            }
        }

        return true
    }
}

class Powerup : PhysicsActor() {

    val rectangle: Rectangle
        get() = Rectangle(x, y, width, height)

    override fun clone(): Powerup {
        val newbie = Powerup()
        newbie.copy(this)
        return newbie
    }

    fun overlaps(other: Paddle): Boolean {
        return Intersector.overlaps(this.rectangle, other.rectangle)
    }

    // randomly select one of stored animations
    fun randomize() {
        val names = ArrayList(animationStorage.keys)
        val n = MathUtils.random(names.size - 1)
        setActiveAnimation(names[n])
    }
}

