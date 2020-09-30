package com.del.delcontainer.storage;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class FileStorage {
    private static final String TAG = "FileStorage";
    Context context;
    public FileStorage(Context context){
        this.context = context;
    }

    private boolean isFilePresent(String fileName) {
        String path = context.getFilesDir().getAbsolutePath() + "/" + fileName;
        File file = new File(path);
        return file.exists();
    }

    public String ReadFile(String appId) {
        try(FileInputStream fis = context.openFileInput(appId)){
            InputStreamReader inputStreamReader =
                    new InputStreamReader(fis, StandardCharsets.UTF_8);
            StringBuilder stringBuilder = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                String line = reader.readLine();
                while (line != null) {
                    stringBuilder.append(line).append('\n');
                    line = reader.readLine();
                }
            } catch (IOException e) {
                Log.d(TAG, "Error while opening file for "+appId);
            } finally {
                return stringBuilder.toString();
            }
        } catch(FileNotFoundException e) {
            Log.d(TAG, "Creating empty file storage for "+appId);
            WriteFile(appId, "{}");
            return "{}";
        }
        catch (Exception e) {
            Log.d(TAG, "Error while reading file for "+appId);
        }
        return null;
    }

    public void WriteFile(String appId, String content){
        try (FileOutputStream fos = context.openFileOutput(appId, Context.MODE_PRIVATE)) {
            fos.write(content.getBytes());
        }catch(Exception e) {
            Log.d(TAG, "Error while writing file for "+appId);
        }
    }

}
