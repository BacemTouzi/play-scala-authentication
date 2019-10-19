package models.enums;

import models.enums.Permission;
import scala.Enumeration;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public enum UserRole {
    admin(Permission.values()),
    intern(),
    employee(Permission.ReadUser) ,
    chief(Permission.ReadUser, Permission.AddUser );

    public final Set<Permission> permissions;

    UserRole(Permission ...permissions) {
        this.permissions = new HashSet<>(Arrays.asList(permissions));
    }
}
