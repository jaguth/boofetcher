package com.jaguth.datastore;

import com.jaguth.models.Message;

import java.util.List;

public interface iDataStore
{
    public abstract void InitDataStore();
    public abstract void InsertBulkData(List<Message> messages);
    public abstract void Dispose();
}
