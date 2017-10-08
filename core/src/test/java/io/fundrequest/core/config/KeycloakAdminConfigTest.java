package io.fundrequest.core.config;


import org.junit.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;

public class KeycloakAdminConfigTest {

    @Test
    public void fsdfsqd() throws Exception {
        Keycloak keycloak = Keycloak.getInstance(
                "https://dev-key.fundrequest.io/auth",
                "fundrequest",
                "fundrequest-query-users",
                "zkFGYvkwAFyanrf3utAWDzfD5QyPsQ9tsmP7JTj88yhqmNSqPY",
                "fundrequest_dev",
                "cb4fa397-24ed-4198-9a25-28e097e74e9d");
        RealmResource realm = keycloak.realm("fundrequest");
        realm.users().count();
    }
}