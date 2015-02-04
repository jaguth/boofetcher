package com.jaguth.datastore;

import com.jaguth.models.Message;
import org.json.JSONObject;

import java.util.List;

public interface iDataStore
{
    public abstract void Initialize(JSONObject obj);
    public abstract void InsertBulkData(List<Message> messages);
    public abstract void Dispose();
}
