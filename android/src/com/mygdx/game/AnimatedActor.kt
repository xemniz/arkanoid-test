package com.mygdx.game

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Animation
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.utils.Array

open class AnimatedActor : BaseActor() {
    private var elapsedTime: Float = 0.toFloat()
    private var activeAnim: Animation<TextureRegion>? = null
    var animationName: String? = null
        private set
    protected var animationStorage: HashMap<String, Animation<TextureRegion>>

    init {
        elapsedTime = 0f
        activeAnim = null
        animationName = null
        animationStorage = HashMap<String, Animation<TextureRegion>>()
    }

    fun storeAnimation(name: String, anim: Animation<TextureRegion>) {
        animationStorage.put(name, anim)
        if (animationName == null)
            setActiveAnimation(name)
    }

    fun storeAnimation(name: String, tex: Texture) {
        val reg = TextureRegion(tex)
        val frames: com.badlogic.gdx.utils.Array<TextureRegion> = Array(arrayOf(reg))
        val anim = Animation<TextureRegion>(1.0f, frames)
        storeAnimation(name, anim)
    }

    fun setActiveAnimation(name: String) {
        if (!animationStorage.containsKey(name)) {
            println("No animation: " + name)
            return
        }

        animationName = name
        activeAnim = animationStorage[name]
        elapsedTime = 0f

        val tex = activeAnim!!.getKeyFrame(0f).getTexture()
        width = tex.getWidth().toFloat()
        height = tex.getHeight().toFloat()
    }

    override fun act(dt: Float) {
        super.act(dt)
        elapsedTime += dt
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        region.setRegion(activeAnim!!.getKeyFrame(elapsedTime))
        super.draw(batch, parentAlpha)
    }

    fun copy(original: AnimatedActor) {
        super.copy(original)
        this.elapsedTime = 0f
        this.animationStorage = original.animationStorage // sharing a reference
        this.animationName = original.animationName!!
        this.activeAnim = this.animationStorage.get(this.animationName!!)
    }

    override fun clone(): AnimatedActor {
        val newbie = AnimatedActor()
        newbie.copy(this)
        return newbie
    }
}