package org.stubby.database;

/**
 * @author Alexander Zagniotov
 * @since 6/12/12, 5:31 PM
 */
public final class Queries {
   public static final String SELECT_ALL_FROM_PERSON = "SELECT * FROM ENDPOINTS.PERSON";
   public static final String SELECT_FROM_PERSON_WHERE = "SELECT * FROM ENDPOINTS.PERSON WHERE ID='%s' AND FIRSTNAME = '%s' AND LASTNAME='%s'";

   private Queries() {

   }
}
