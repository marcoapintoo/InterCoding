package org.pinto.intercoding.intermediate;

import groovy.lang.Closure;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class OwnerArrayList<T extends BaseModel, TP extends BaseModel> extends ArrayList<T> implements Serializable {
    public abstract T createDefault();

    public abstract void setParenthood(T object, TP parent);

    public OwnerArrayList(TP owner, List<? extends T> baselist) {
        this(owner);
        if (baselist != null) this.addAll(baselist);
    }

    public OwnerArrayList(TP owner) {
        super();
        this.owner = owner;
        this.ownerType = owner == null ? null : owner.getClass();
    }

    private T assignParent(T element) {
        element = DefaultGroovyMethods.asBoolean(element) ? element : createDefault();
        try {
            setParenthood(element, owner);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return element;
    }

    private Collection<? extends T> transform(Collection<? extends T> c) {
        return DefaultGroovyMethods.collect(c, new Closure<T>(this, this) {
            public T doCall(T it) {
                return assignParent(it);
            }

            public T doCall() {
                return doCall(null);
            }

        });
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return super.addAll(transform(c));
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return super.addAll(index, transform(c));
    }

    @Override
    public boolean add(T element) {
        return super.add(assignParent(element));
    }

    @Override
    public void add(int index, T element) {
        super.add(index, assignParent(element));
    }

    protected TP owner;
    protected java.lang.Class<?> ownerType;
}
