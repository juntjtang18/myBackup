package com.myBackup.security;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findByUsername(String username) throws IOException;
    void save(User user) throws IOException;
    // In UserRepository interface
    List<User> loadAllUsers() throws IOException;
	void updateUser(User updatedUser) throws IOException;

}
