package com.del.delcontainer.database.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.del.delcontainer.database.entities.UserProfile;

@Dao
public interface UserProfileDao {

    // Get user info
    @Query("SELECT * from user_profile")
    LiveData<UserProfile> getUserProfile();

    // Create new entry (Should only work once)
    //@Query("INSERT into UserProfile(userId, first_name, last_name, age, sex) VALUES(:userId, :firstName, :lastName, :age, :sex)")
    @Insert
    void createUser(UserProfile userProfile);

    // Delete user from system
    //@Query("DELETE from UserProfile where userId = :userId")
    @Delete
    void deleteUserProfile(UserProfile userProfile);

    @Update
    void updateUserDetails(UserProfile userProfile);


    // The above does all the functions below
    // Update user first name
    @Query("UPDATE user_profile set first_name = :firstName")
    void updateFirstName(String firstName);

    // Update user last name
    @Query("UPDATE user_profile set last_name = :lastName")
    void updateLastName(String lastName);

    // Update user age
    @Query("UPDATE user_profile set age = :age")
    void updateAge(int age);
}
