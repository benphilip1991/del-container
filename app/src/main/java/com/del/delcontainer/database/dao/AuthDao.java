package com.del.delcontainer.database.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.del.delcontainer.database.entities.Auth;

@Dao
public interface AuthDao {

    @Insert
    void insertAuthInfo(Auth auth);

    @Query("SELECT * from auth ORDER BY id DESC LIMIT 1")
    Auth getAuthInfo();

    @Query("SELECT access_token from auth")
    String getAccessToken();

    @Query("SELECT refresh_token from auth")
    String getRefreshToken();

    @Query("SELECT user_id from auth")
    String getUserId();

    @Query("UPDATE auth set access_token = :token")
    void updateAccessToken(String token);

    @Query("UPDATE auth set refresh_token = :token")
    void updateRefreshToken(String token);

    @Query("DELETE from auth")
    void clearTokens();
}
