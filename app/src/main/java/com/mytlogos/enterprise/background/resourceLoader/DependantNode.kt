package com.mytlogos.enterprise.background.resourceLoader

import java.util.*

internal class DependantNode {
    private var root: Boolean
    val value: DependantValue
    private val children: MutableSet<DependantNode> = HashSet()
    private val optionalChildren: MutableSet<DependantNode> = HashSet()
    private val parents: MutableSet<DependantNode> = HashSet()

    constructor(value: DependantValue) {
        Objects.requireNonNull(value)
        Objects.requireNonNull(value.value)
        this.root = false
        this.value = value
    }

    constructor(intId: Int) {
        Objects.requireNonNull(intId)
        require(intId > 0) { "invalid int id: not greater than zero" }
        this.root = true
        value = DependantValue(intId)
    }

    constructor(stringId: String?) {
        require(!(stringId == null || stringId.isEmpty())) { "invalid string id: empty" }
        this.root = true
        value = DependantValue(stringId)
    }

    fun getChildren(): Set<DependantNode> {
        return Collections.unmodifiableSet(children)
    }

    fun getOptionalChildren(): Set<DependantNode> {
        return Collections.unmodifiableSet(optionalChildren)
    }

    fun addChild(node: DependantNode, optional: Boolean): Boolean {
        Objects.requireNonNull(node)
        val nodes = if (optional) optionalChildren else children
        if (nodes.add(node)) {
            node.parents.add(this)
            return true
        }
        return false
    }

    fun createNewNode(value: DependantValue): DependantNode {
        val node = DependantNode(value)
        for (child in children) {
            child.removeParent(this)
            node.addChild(child, false)
        }
        for (child in optionalChildren) {
            child.removeParent(this)
            node.addChild(child, true)
        }
        return node
    }

    fun removeAsParent(): Collection<DependantNode> {
        for (child in children) {
            if (!child.parents.remove(this)) {
                println("children does not have this as parent")
            }
        }
        for (child in optionalChildren) {
            if (!child.parents.remove(this)) {
                println("children does not have this as parent")
            }
        }
        val nodes: MutableCollection<DependantNode> = ArrayList(
            children.size + optionalChildren.size
        )
        nodes.addAll(children)
        nodes.addAll(optionalChildren)
        return nodes
    }

    fun rejectNode() {
        val iterator = parents.iterator()
        while (iterator.hasNext()) {
            val parent = iterator.next()
            parent.children.remove(this)
            parent.optionalChildren.remove(this)
            iterator.remove()
        }
        for (optionalChild in optionalChildren) {
            optionalChild.rejectNode()
        }
        for (child in children) {
            child.rejectNode()
        }
    }

    fun removeChild(node: DependantNode, optional: Boolean): Boolean {
        Objects.requireNonNull(node)
        return if (optional) optionalChildren.remove(node) else children.remove(node)
    }

    fun addParent(node: DependantNode): Boolean {
        Objects.requireNonNull(node)
        return parents.add(node)
    }

    fun removeParent(node: DependantNode): Boolean {
        Objects.requireNonNull(node)
        return parents.remove(node)
    }

    fun isRoot(): Boolean {
        return root && parents.isEmpty()
    }

    val isFree: Boolean
        get() {
            if (parents.isEmpty()) {
                return true
            }
            for (parent in parents) {
                if (parent.children.contains(this)) {
                    return false
                }
            }
            return true
        }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val that = o as DependantNode
        return value == that.value
    }

    override fun hashCode(): Int {
        return value.hashCode()
    }
}