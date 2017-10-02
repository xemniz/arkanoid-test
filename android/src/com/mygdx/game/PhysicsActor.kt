package com.mygdx.game

import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Vector2

open class PhysicsActor : AnimatedActor() {
    protected var velocity: Vector2
    private var acceleration: Vector2? = null

    // maximum speed
    private var maxSpeed: Float = 0.toFloat()

    // speed reduction, in pixels/second, when not accelerating
    private var deceleration: Float = 0.toFloat()

    // should image rotate to match velocity?
    private var autoAngle: Boolean = false

    var speed: Float
        get() = velocity.len()
        set(s) {
            velocity.setLength(s)
        }

    val motionAngle: Float
        get() = MathUtils.atan2(velocity.y, velocity.x) * MathUtils.radiansToDegrees

    init {
        velocity = Vector2()
        acceleration = Vector2()
        maxSpeed = 9999f
        deceleration = 0f
        autoAngle = false
    }

    // velocity methods

    fun setVelocityXY(vx: Float, vy: Float) {
        velocity.set(vx, vy)
    }

    fun addVelocityXY(vx: Float, vy: Float) {
        velocity.add(vx, vy)
    }

    // set velocity from angle and speed
    fun setVelocityAS(angleDeg: Float, speed: Float) {
        velocity.x = speed * MathUtils.cosDeg(angleDeg)
        velocity.y = speed * MathUtils.sinDeg(angleDeg)
    }

    fun setMaxSpeed(ms: Float) {
        maxSpeed = ms
    }

    fun setAutoAngle(b: Boolean) {
        autoAngle = b
    }

    // acceleration methods

    fun setAccelerationXY(ax: Float, ay: Float) {
        acceleration!!.set(ax, ay)
    }

    fun addAccelerationXY(ax: Float, ay: Float) {
        acceleration!!.add(ax, ay)
    }

    // set acceleration from angle and speed
    fun setAccelerationAS(angleDeg: Float, speed: Float) {
        acceleration!!.x = speed * MathUtils.cosDeg(angleDeg)
        acceleration!!.y = speed * MathUtils.sinDeg(angleDeg)
    }

    // add acceleration from angle and speed
    fun addAccelerationAS(angleDeg: Float, speed: Float) {
        acceleration!!.add(
                speed * MathUtils.cosDeg(angleDeg),
                speed * MathUtils.sinDeg(angleDeg))
    }

    fun accelerateForward(speed: Float) {
        setAccelerationAS(getRotation(), speed)
    }

    fun setDeceleration(d: Float) {
        deceleration = d
    }

    override fun act(dt: Float) {
        super.act(dt)

        // apply acceleration
        velocity.add(acceleration!!.x * dt, acceleration!!.y * dt)

        // decrease velocity when not accelerating
        if (acceleration!!.len() < 0.01) {
            val decelerateAmount = deceleration * dt
            if (speed < decelerateAmount)
                speed = 0f
            else
                speed = speed - decelerateAmount
        }

        // cap at max speed
        if (speed > maxSpeed)
            speed = maxSpeed

        // apply velocity
        moveBy(velocity.x * dt, velocity.y * dt)

        // rotate image when moving
        if (autoAngle && speed > 0.1)
            setRotation(motionAngle)
    }

    fun copy(original: PhysicsActor) {
        super.copy(original)
        this.velocity = Vector2(original.velocity)
        this.acceleration = Vector2(original.acceleration)
        this.maxSpeed = original.maxSpeed
        this.deceleration = original.deceleration
        this.autoAngle = original.autoAngle
    }

    override fun clone(): PhysicsActor {
        val newbie = PhysicsActor()
        newbie.copy(this)
        return newbie
    }
}