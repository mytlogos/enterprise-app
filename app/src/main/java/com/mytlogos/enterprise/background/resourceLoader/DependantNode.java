package com.mytlogos.enterprise.background.resourceLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Objects;
import java.util.Set;

class DependantNode {
    private boolean root;
    private final DependantValue value;
    private final Set<DependantNode> children = new HashSet<>();
    private final Set<DependantNode> optionalChildren = new HashSet<>();
    private final Set<DependantNode> parents = new HashSet<>();

    DependantNode(DependantValue value) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(value.getValue());
        this.root = false;
        this.value = value;
    }

    DependantNode(Integer intId) {
        Objects.requireNonNull(intId);
        if (intId <= 0) {
            throw new IllegalArgumentException("invalid int id: not greater than zero");
        }
        this.root = true;
        this.value = new DependantValue(intId.intValue());
    }

    DependantNode(String stringId) {
        if (stringId == null || stringId.isEmpty()) {
            throw new IllegalArgumentException("invalid string id: empty");
        }
        this.root = true;
        this.value = new DependantValue(stringId);
    }

    Set<DependantNode> getChildren() {
        return Collections.unmodifiableSet(this.children);
    }

    Set<DependantNode> getOptionalChildren() {
        return Collections.unmodifiableSet(this.optionalChildren);
    }

    boolean addChild(DependantNode node, boolean optional) {
        Objects.requireNonNull(node);
        Set<DependantNode> nodes = optional ? this.optionalChildren : this.children;

        if (nodes.add(node)) {
            node.parents.add(this);
            return true;
        }
        return false;
    }

    DependantNode createNewNode(DependantValue value) {
        DependantNode node = new DependantNode(value);

        for (DependantNode child : this.children) {
            child.removeParent(this);
            node.addChild(child, false);
        }
        for (DependantNode child : this.optionalChildren) {
            child.removeParent(this);
            node.addChild(child, true);
        }
        return node;
    }

    Collection<DependantNode> removeAsParent() {
        for (DependantNode child : this.children) {
            if (!child.parents.remove(this)) {
                System.out.println("children does not have this as parent");
            }
        }
        for (DependantNode child : this.optionalChildren) {
            if (!child.parents.remove(this)) {
                System.out.println("children does not have this as parent");
            }
        }

        Collection<DependantNode> nodes = new ArrayList<>(this.children.size() + this.optionalChildren.size());
        nodes.addAll(this.children);
        nodes.addAll(this.optionalChildren);
        return nodes;
    }

    void rejectNode() {
        for (Iterator<DependantNode> iterator = this.parents.iterator(); iterator.hasNext(); ) {
            DependantNode parent = iterator.next();
            parent.children.remove(this);
            parent.optionalChildren.remove(this);
            iterator.remove();
        }
        for (DependantNode optionalChild : this.optionalChildren) {
            optionalChild.rejectNode();
        }
        for (DependantNode child : this.children) {
            child.rejectNode();
        }
    }

    boolean removeChild(DependantNode node, boolean optional) {
        Objects.requireNonNull(node);
        return optional ? this.optionalChildren.remove(node) : this.children.remove(node);
    }

    boolean addParent(DependantNode node) {
        Objects.requireNonNull(node);
        return this.parents.add(node);
    }

    boolean removeParent(DependantNode node) {
        Objects.requireNonNull(node);
        return this.parents.remove(node);
    }

    DependantValue getValue() {
        return value;
    }

    boolean isRoot() {
        return root && this.parents.isEmpty();
    }

    boolean isFree() {
        if (this.parents.isEmpty()) {
            return true;
        }
        for (DependantNode parent : this.parents) {
            if (parent.children.contains(this)) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DependantNode that = (DependantNode) o;

        return value.equals(that.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
