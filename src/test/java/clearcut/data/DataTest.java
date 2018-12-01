package clearcut.data;

import java.util.Map;
import java.util.List;
import java.util.HashMap;
import java.sql.SQLException;

import org.junit.Test;
import junit.framework.TestCase;
import static org.junit.Assert.assertTrue;

import clearcut.Logger;
import clearcut.InjectionException;
import static clearcut.Injector.INJECTOR;

/* You need junit-4.1.jar and mysql.jar, mssql.jar or similar on your CLASSPATH
   and table 'test' in the clearcut_test database - run dat/build.mysql etc. to create it
 */

public class DataTest extends TestCase {

private Dataset dataset;
private String clinic;
private String[] columnNames = { "id", "drug_name", "clinic_name",
                                 "member_id", "external_id", "source", "created_date",
                                 "deleted_date", "accepted_date", "last_touched_date", "optlock" };

public void setUp() throws Exception {
        this.clinic = "Portland Clinic";
        if (!m$()) {
                String jdbc_driver = INJECTOR.property("database", "jdbc_driver");
                Class.forName(jdbc_driver);
                this.dataset = (Dataset) INJECTOR.implement("database", "dataset");
        }
}

public void tearDown() throws Exception {
}

public DataTest() {
}

// MySQL is a bit more broad-minded than SQL Server
private void m$(DataException d) throws Exception {
        Exception x = d.innerException();
        String name = x.getClass().getName().toLowerCase();
        String msg = x.getMessage().toLowerCase();
        if (name.indexOf("microsoft") < 0 || name.indexOf("sqlserver") < 0
            || msg.indexOf("cannot") < 0 || msg.indexOf("identity") < 0)
                throw x;
}

// This method follows Microsoft's instructions: load the 2005 JDBC driver first:
// Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver"); 2005 version
// Class.forName("com.microsoft.jdbc.sqlserver.SQLServerDriver"); 2000 version
// This attempts to load both drivers, and returns false if both attempts fail
private boolean m$() {
        try {
                String m$_url = INJECTOR.property("database", "m$_url");
                if (m$_url == null)
                        return false;
                String m$_driver_2005 = INJECTOR.property("database",
                                                          "m$_driver_2005");
                String m$_driver_2000 = INJECTOR.property("database",
                                                          "m$_driver_2000");
                if (m$_driver_2005 == null && m$_driver_2000 == null)
                        return false;

                String m$_user_name = INJECTOR.property("database", "m$_user_name");
                String m$_password_encrypted = INJECTOR.property("database",
                                                                 "m$_password_encrypted");
                if (m$_user_name == null)
                        return false;
                if (m$_password_encrypted == null)
                        m$_password_encrypted = "";

                String m$_db = INJECTOR.property("database", "m$_db");
                if (m$_db == null)
                        return false;

                int num_drivers = 0;
                if (m$_driver_2005 != null)
                        try {
                                Class.forName(m$_driver_2005);
                                num_drivers++;
                        } catch (ClassNotFoundException c) {
                        }
                if (m$_driver_2000 != null)
                        try {
                                Class.forName(m$_driver_2000);
                                num_drivers++;
                        } catch (ClassNotFoundException c) {
                        }
                if (num_drivers < 1)
                        return false;
                this.dataset = new Dataset(m$_url + ";databaseName=" + m$_db,
                                           m$_user_name, m$_password_encrypted);

        } catch (InjectionException j) {
                return false;
        }

        return true;
}

public void testGetDataByColumnNumbersWorks() throws Exception {
        doInsert("Amoxil");
        List<String[]> rows = this.dataset.rows("test", this.columnNames, null);
        String[] row = rows.get(0);
        assertTrue("No data found in dataset", row.length > 0);
}

public void testGetDataByColumnNamesWorks() throws Exception {
        doInsert("Ibuprofen");
        List<Map<String, String> > table = this.dataset.table("test",
                                                              this.columnNames, null);
        Map<String, String> row = table.get(table.size() - 1);
        assertTrue("Error getting data", row.get("id") != null);
}

public void testIfYouCanInsertIntoAnAutoIncrementColumnIfItsNotThereAlready()
throws Exception {
        String[] columns = new String[1];
        columns[0] = "id";
        int id = -42;
        List<String[]> rows = this.dataset.rows("test", columns, null);
        for (String[] row : rows) {
                assertTrue("Rows should be 1 in size but are " + row.length,
                           row.length == 1);
                int num = Integer.parseInt(row[0]);
                if (num > id)
                        id = num;
        }
        assertTrue("ID did not get set", id != -42);
        Map<String, String> changes = new HashMap<String, String>();
        changes.put("id", "" + (id + 42)); // Insert an ID greater than the ones already there
        changes.put("accepted_date", "2007-01-02");
        changes.put("last_touched_date", "2008-01-20");
        changes.put("created_date", "2007-01-29");
        changes.put("deleted_date", "2007-09-29");
        changes.put("drug_name", "Agenerase");
        changes.put("clinic_name", "Denver Hospital");
        try {
                this.dataset.insert("test", changes);
        } catch (DataException d) {
                m$(d); // See if it's an expected exception from SQL Server - swallow it - else throw it
        }
}

public void testIfYouCannotUpdateUniqueAutoIncrementColumnIfItsThereAlready()
throws Exception {
        String[] columns = new String[1];
        columns[0] = "id";
        boolean ex = false;
        int id = -42;
        int max = -42;
        String num = null;
        List<String[]> rows = this.dataset.rows("test", columns, null);
        for (String[] row : rows) {
                num = row[0];
                int tmp = Integer.parseInt(num);
                if (tmp > max)
                        max = tmp;
                if (id == -42)
                        id = Integer.parseInt(num);
                // Find id and max, different unique ids for failed update and successful update - see below
        }
        assertTrue("Two different ids not found - needed for test",
                   !num.equals("" + id));
        assertTrue("ID did not get set", id != -42);
        assertTrue("Max did not get set", max != -42);
        Map<String, String> changes = new HashMap<String, String>();
        changes.put("id", num);
        changes.put("created_date", "2004-04-29");
        changes.put("deleted_date", "2007-10-29");
        changes.put("drug_name", "Thalidomide"); // This drug is banned so it's
        // a good thing this fails
        changes.put("clinic_name", "Lancaster Hospital");
        try { // You cannnot update from one existing id to another existing id
                this.dataset.update("test", changes, "Id = '" + id + "'");
        } catch (DataException x) {
                ex = true;
        }
        if (!ex)
                throw new Exception(
                              "Attempt to change an id to an id which was already there did not throw exception");
        // You can update an existing id to a completely original brand new id in MySQL
        changes.put("id", "" + (max + 1));
        changes.put("drug_name", "Tylenol");
        try {
                this.dataset.update("test", changes, "Id = '" + id + "'");
        } catch (DataException d) {
                m$(d); // See if it's an expected exception from SQL Server - swallow it - else throw it
        }
}

public void testYouCannotInsertAnInvalidDate() throws Exception {
        try {
                Map<String, String> columns = new HashMap<String, String>();
                columns.put("created_date", "2007-02-29");
                columns.put("deleted_date", "2008-02-29");
                columns.put("drug_name", "Herceptin");
                columns.put("clinic_name", "Beaverton Clinic");
                this.dataset.insert("test", columns);
        } catch (DataException x) {
                if (x.innerException() instanceof SQLException)
                        return;
                else
                        throw x.innerException();
        }
        throw new Exception(
                      "Attempt to insert invalid date did not throw exception");
}

public void testYouCannotInsertNullIntoANonNullColumn() throws Exception {
        try {
                Map<String, String> columns = new HashMap<String, String>();
                columns.put("created_date", "2007-01-29");
                columns.put("deleted_date", "2008-01-29");
                columns.put("drug_name", null);
                columns.put("clinic_name", "Chinese Clinic");
                this.dataset.insert("test", columns);
        } catch (DataException x) {
                if (x.innerException() instanceof SQLException)
                        return;
                else
                        throw x.innerException();
        }
        throw new Exception(
                      "Attempt to insert null into non-null column did not throw exception");
}

public void testYouCannotForgetToInsertIntoANonNullColumn()
throws Exception {
        try {
                Map<String, String> columns = new HashMap<String, String>();
                columns.put("created_date", "2007-04-29");
                columns.put("deleted_date", "2007-06-29");
                // Forget drug_name
                columns.put("clinic_name", "Manchester Hospital");
                this.dataset.insert("test", columns);
        } catch (DataException x) {
                if (x.innerException() instanceof SQLException)
                        return;
                else
                        throw x.innerException();
        }
        throw new Exception(
                      "Attempt to not insert anything into non-null column did not throw exception");
}

public void testYouCanInsertColumnWithQuoteIn() throws Exception {
        Map<String, String> columns = new HashMap<String, String>();
        columns.put("clinic_name", "The O'Reilly Special Clinic");
        columns.put("drug_name", "Penicillin");
        this.dataset.insert("test", columns);
}

public void testYouCanInsertColumnWithQuotesIn() throws Exception {
        Map<String, String> columns = new HashMap<String, String>();
        columns.put("clinic_name", "The O'Reilly 'Special' Clinic");
        columns.put("drug_name", "Penicillin");
        this.dataset.insert("test", columns);
}

public void testYouCanInsertColumnWithDoubleQuotesIn() throws Exception {
        Map<String, String> columns = new HashMap<String, String>();
        columns.put("clinic_name", "The McTaggart \"Special\" Clinic");
        columns.put("drug_name", "Penicillin");
        this.dataset.insert("test", columns);
}

public void testYouCanInsertColumnWithFunnyCharsIn() throws Exception {
        Map<String, String> columns = new HashMap<String, String>();
        columns.put("clinic_name", "\"The O`Reilly \"#$\\%^&*\" Clinic\"");
        columns.put("drug_name", "Penicillin");
        this.dataset.insert("test", columns);
}

public void testYouCanInsertColumnWithQuotesAndDoubleQuotesIn()
throws Exception {
        Map<String, String> columns = new HashMap<String, String>();
        columns.put("clinic_name", "'The O'Reilly \"Special\" Clinic'");
        columns.put("drug_name", "Penicillin");
        this.dataset.insert("test", columns);
}

public void testYouCanInsertDeleteAndUpdate() throws Exception {
        int id = -42;
        boolean found = false;
        String drug = "Cymbalta";
        String[] columns = new String[2];
        columns[0] = "id";
        columns[1] = "drug_name";
        Map<String, String> updateCols = new HashMap<String, String>();
        updateCols.put("drug_name", drug);

        doInsert("Valium");
        doDelete("Valium");
        doInsert("Viagra");
        String whereClause = "  where clinic_name = '" + this.clinic
                             + "' and drug_name = 'Viagra'";
        List<String[]> rows = this.dataset.rows("test", columns, whereClause);
        for (String[] row : rows)
                id = Integer.parseInt(row[0]);
        assertTrue("Did not find a row for Viagra in " + this.clinic
                   + " after we tried to insert it", id != -42);

        whereClause = "Id = " + id;
        this.dataset.update("test", updateCols, whereClause);
        List<Map<String, String> > table = this.dataset.table("test", columns,
                                                              whereClause);
        assertTrue("Didn't find any rows for " + drug + " in " + this.clinic,
                   table.size() > 0);
        for (Map<String, String> row : table)
                if (row.get("drug_name").equals(drug))
                        found = true;
        // Where clause should guarantee if there are any rows they are right, but just in case...
        assertTrue("Didn't find a row for " + drug + " in " + this.clinic,
                   found);
}

public void testYouCanChange() throws Exception {
        doDelete("Sudafed");
        doDelete("Amoxil");

        String randomclinic_name = "Clinic no. "
                                   + Math.abs(new java.util.Random().nextInt());
        Map<String, String> insertOrUpdateValues = new HashMap<String, String>();
        insertOrUpdateValues.put("clinic_name", randomclinic_name);
        insertOrUpdateValues.put("drug_name", "Sudafed");
        String whereClause = "where clinic_name = '" + randomclinic_name + "'";
        this.dataset.change("test", insertOrUpdateValues, whereClause);
        List<String[]> rows = this.dataset.rows("test", Change.STAR,
                                                whereClause);
        assertTrue("Change should have inserted one row", 1 == rows.size());
        insertOrUpdateValues.put("drug_name", "Amoxil");
        this.dataset.change("test", insertOrUpdateValues, whereClause);
        rows = this.dataset.rows("test", Change.STAR, whereClause);
        assertTrue("Change should have updated one row", 1 == rows.size());
}

public void testYouCanCallProcedures() throws Exception {
        String id = null;
        String drugName = null;
        List<String[]> results = this.dataset.procedure("clinic_names", null);
        if (results.size() > 0)
                assertTrue("Results from clinic_names should have one column",
                           results.get(0).length == 1);
        String clinicName = null;
        for (String[] result : results)
                if (result[0] != null)
                        clinicName = result[0];
        assertTrue("Did not find non-null Clinic Name", clinicName != null);

        boolean found = false;
        String[] clinicNames = new String[2];
        clinicNames[0] = clinicName; // Change clinic name to clinic name with a
        // '!' on the end
        clinicNames[1] = clinicName + "!";
        results = this.dataset.procedure("change_clinic_name", clinicNames);
        assertTrue("Procedure change_clinic_name should have no results",
                   results.size() == 0);

        results = this.dataset.procedure("clinic_names", null);
        for (String[] name : results) {
                String newName = name[0];
                if (clinicName.equals(newName))
                        throw new Exception(
                                      "There should be no clinics named "
                                      + clinicName
                                      + " because we just changed them with a stored procedure");
                if ((clinicName + "!").equals(newName))
                        found = true;
        }
        assertTrue(clinicName
                   + " plus exclamation mark should be in results but isn't",
                   found);

        String[] columns = new String[2];
        columns[0] = "id";
        columns[1] = "drug_name";
        results = this.dataset.rows("test", columns, null);
        for (String[] row : results) {
                id = row[0];
                drugName = row[1];
        }

        columns = new String[1];
        columns[0] = "" + id;
        List<Map<String, String> > table = this.dataset.method("drug_name",
                                                               columns);
        assertTrue(
                "Procedure drug_name with id " + id + " should return 1 row",
                table.size() == 1);
        String result = table.get(0).get("drug_name");
        assertTrue("Procedure drug_name with id " + id + " should return "
                   + drugName + ", not " + result, drugName.equals(result));

}

public void doInsert(String drug) throws Exception {
        boolean found = false;
        Map<String, String> columns = new HashMap<String, String>();
        columns.put("clinic_name", this.clinic);
        columns.put("drug_name", drug);
        this.dataset.insert("test", columns);
        String whereClause = "where clinic_name = '" + this.clinic + "'";
        String[] twoCols = new String[2];
        twoCols[0] = "id";
        twoCols[1] = "drug_name";
        List<String[]> rows = this.dataset.rows("test", twoCols, whereClause);
        for (String[] row : rows)
                if (row[1].equals(drug))
                        found = true;
        if (!found)
                throw new Exception("Didn't find a row for " + drug + " in "
                                    + this.clinic);
}

public void doDelete(String drug) throws Exception {
        boolean found = false;
        String[] columns = new String[2];
        columns[0] = "id";
        columns[1] = "drug_name";
        String whereClause = "clinic_name = '" + this.clinic
                             + "' and drug_name = '" + drug + "'";
        this.dataset.delete("test", whereClause);
        List<String[]> rows = this.dataset.rows("test", columns, whereClause);
        for (String[] row : rows)
                if (row[1].equals(drug))
                        found = true;
        if (found)
                throw new Exception("Found a row for " + drug + " in "
                                    + this.clinic + " after we tried to delete it");
}

}
