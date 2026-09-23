package com.trae.ams.common.context;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UserContextTest {

    @Test
    public void testUserContext() {
        // Initially empty
        Assertions.assertNull(UserContext.getUserId());
        Assertions.assertNull(UserContext.getStoreId());
        Assertions.assertEquals(1L, UserContext.getStoreIdOrDefault());

        // Set values
        UserContext.setUserId(100L);
        UserContext.setStoreId(2L);

        Assertions.assertEquals(100L, UserContext.getUserId());
        Assertions.assertEquals(2L, UserContext.getStoreId());
        Assertions.assertEquals(2L, UserContext.getStoreIdOrDefault());

        // Clear
        UserContext.clear();
        Assertions.assertNull(UserContext.getUserId());
        Assertions.assertNull(UserContext.getStoreId());
        Assertions.assertEquals(1L, UserContext.getStoreIdOrDefault());
    }
}
