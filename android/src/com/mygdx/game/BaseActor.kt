package com.mygdx.game

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.Intersector
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.math.Polygon
import com.badlogic.gdx.scenes.scene2d.Group
import java.util.*


open class BaseActor : Group() {
    var region: TextureRegion
    var _boundingPolygon: Polygon? = null
    var _parentList: ArrayList<out BaseActor>? = null

    init {
        region = TextureRegion()
        _boundingPolygon = null
        _parentList = null
    }

    fun setParentList(pl: ArrayList<out BaseActor>) {
        _parentList = pl
    }

    open fun destroy() {
        remove() // removes self from Stage

        if (_parentList != null)
            _parentList!!.remove(this)
    }

    fun setOriginCenter() {
        if (width == 0f)
            System.err.println("error: actor size not set")
        setOrigin(width / 2, height / 2)
    }

    fun moveToOrigin(target: BaseActor) {
        this.setPosition(
                target.x + target.originX - this.originX,
                target.y + target.originY - this.originY)
    }

    fun setTexture(t: Texture) {
        val w = t.width
        val h = t.height
        width = w.toFloat()
        height = h.toFloat()
        region.setRegion(t)
    }

    fun setRectangleBoundary() {
        val w = width
        val h = height
        val vertices = floatArrayOf(0f, 0f, w, 0f, w, h, 0f, h)
        _boundingPolygon = Polygon(vertices)
        _boundingPolygon!!.setOrigin(originX, originY)
    }

    fun setEllipseBoundary() {
        val n = 12 // number of vertices
        val w = width
        val h = height
        val vertices = FloatArray(2 * n)
        for (i in 0 until n) {
            val t = i * 6.28f / n
            // x-coordinate
            vertices[2 * i] = w / 2 * MathUtils.cos(t) + w / 2
            // y-coordinate
            vertices[2 * i + 1] = h / 2 * MathUtils.sin(t) + h / 2
        }
        _boundingPolygon = Polygon(vertices)
        _boundingPolygon!!.setOrigin(originX, originY)
    }

    fun getBoundingPolygon(): Polygon {
        _boundingPolygon!!.setPosition(x, y)
        _boundingPolygon!!.rotation = rotation
        return _boundingPolygon!!
    }

    /**
     * Determine if the collision polygons of two BaseActor objects overlap.
     * If (resolve == true), then when there is overlap, move this BaseActor
     * along minimum translation vector until there is no overlap.
     */
    fun overlaps(other: BaseActor, resolve: Boolean): Boolean {
        val poly1 = this.getBoundingPolygon()
        val poly2 = other.getBoundingPolygon()

        if (!poly1.boundingRectangle.overlaps(poly2.boundingRectangle))
            return false

        val mtv = Intersector.MinimumTranslationVector()
        val polyOverlap = Intersector.overlapConvexPolygons(poly1, poly2, mtv)
        if (polyOverlap && resolve) {
            this.moveBy(mtv.normal.x * mtv.depth, mtv.normal.y * mtv.depth)
        }
        val significant = 0.5f
        return polyOverlap && mtv.depth > significant
    }

    override fun act(dt: Float) {
        super.act(dt)
    }

    override fun draw(batch: Batch?, parentAlpha: Float) {
        val c = color
        batch!!.setColor(c.r, c.g, c.b, c.a)
        if (isVisible)
            batch.draw(region, x, y, originX, originY,
                    width, height, scaleX, scaleY, rotation)

        super.draw(batch, parentAlpha)
    }

    fun copy(original: BaseActor) {
        if (original.region.texture != null)
            this.region = TextureRegion(original.region)
        if (original._boundingPolygon != null) {
            this._boundingPolygon = Polygon(original._boundingPolygon!!.vertices)
            this._boundingPolygon!!.setOrigin(original.originX, original.originY)
        }
        this.setPosition(original.x, original.y)
        this.originX = original.originX
        this.originY = original.originY
        this.width = original.width
        this.height = original.height
        this.color = original.color
        this.isVisible = original.isVisible
    }

    open fun clone(): BaseActor {
        val newbie = BaseActor()
        newbie.copy(this)
        return newbie
    }
}

