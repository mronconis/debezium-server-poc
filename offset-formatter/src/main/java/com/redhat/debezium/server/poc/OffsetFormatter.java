package com.redhat.debezium.server.poc;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;
import org.json.JSONArray;

public class OffsetFormatter {
  
    private File file;

    public OffsetFormatter(String offsetFilePath) {
        file = new File(offsetFilePath); 
    }

    public void format() {
        for (Map.Entry<JSONArray, JSONObject> mapEntry : read().entrySet()) {
            System.out.printf("key:\n%s\nvalue:\n%s", 
                mapEntry.getKey().toString(4), mapEntry.getValue().toString(4));
        }
    }

    private Map<JSONArray, JSONObject> read() {
        try (ObjectInputStream is = new ObjectInputStream(Files.newInputStream(file.toPath()))) {
            Object obj = is.readObject();
            if (!(obj instanceof HashMap))
                throw new RuntimeException("Expected HashMap but found " + obj.getClass());
            Map<byte[], byte[]> raw = (Map<byte[], byte[]>) obj;
            Map<JSONArray, JSONObject> data = new HashMap<>();
            for (Map.Entry<byte[], byte[]> mapEntry : raw.entrySet()) {
                JSONArray key = decodeKey(mapEntry.getKey(), StandardCharsets.UTF_8.name());
                JSONObject value = decodeValue(mapEntry.getValue(), StandardCharsets.UTF_8.name());
                data.put(key, value);
            }
            return data;
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private JSONArray decodeKey(byte[] data, String charset) throws UnsupportedEncodingException {
        if (data == null)
            return new JSONArray();
        return new JSONArray(decode(data, charset));
    }

    private JSONObject decodeValue(byte[] data, String charset) throws UnsupportedEncodingException {
        if (data == null)
            return new JSONObject();
        return new JSONObject(decode(data, charset));
    }

    private String decode(byte[] data, String charset) throws UnsupportedEncodingException {
        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        return new String(bytes, charset);
    }
}
