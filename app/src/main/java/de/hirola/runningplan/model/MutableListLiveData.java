package de.hirola.runningplan.model;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;

import java.util.ArrayList;
import java.util.List;

/**
 * Copyright 2021 by Michael Schmidt, Hirola Consulting
 * This software us licensed under the AGPL-3.0 or later.
 *
 * Notify Observer when item is added to List of LiveData, a workaround to using live data.
 * Found at <a href="https://stackoverflow.com/a/68946223/15577485">
 * Thanks to <a href="https://stackoverflow.com/users/2038544/razz">Razz</a>
 *
 * @author Michael Schmidt (Hirola)
 * @since 1.1.1
 */
public class MutableListLiveData<T> extends MutableLiveData<List<T>> {
    private final MutableLiveData<Long> lastModified = new MutableLiveData<>();
    private List<T> items;
    private ListObserver<List<T>> callback;

    public MutableListLiveData(List<T> list) {
        this.items = list;
    }

    public MutableListLiveData() {
        this.items = new ArrayList<>();
    }

    public void addItem(T item) {
        items.add(item);
        onListModified();
    }

    public void removeItem(int position) {
        items.remove(position);
        onListModified();
    }

    public void removeItem(T item) {
        items.remove(item);
        onListModified();
    }

    public void updateItem(T item) {
        int position = items.indexOf(item);
        if (position > -1) {
            items.set(position, item);
            onListModified();
        }
    }

    public T getItem(int position) {
        return items.get(position);
    }

    private void onListModified() {
        lastModified.setValue(System.currentTimeMillis());
    }

    @Override
    public List<T> getValue() {
        return items;
    }

    @Override
    public void setValue(List<T> items) {
        this.items = items;
        onListModified();
    }

    public void observe(@NonNull LifecycleOwner owner, ListObserver<List<T>> callback) {
        this.callback = callback;
        lastModified.observe(owner, this::onListItemsChanged);
    }

    private void onListItemsChanged(long time) {
        if (callback != null) callback.onListItemsChanged(items, items.size());
    }

    public interface ListObserver<T> {
        void onListItemsChanged(T items, int size);
    }
}