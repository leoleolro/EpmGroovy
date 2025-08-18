<HBRRepo>
    <variables/>
    <rulesets/>
    <rules>
        <rule id="1" name="ProjRep - Budget Data Export Check Data Mismatch" product="Planning">
            <property name="application">RPEPM</property>
            <property name="plantype">ProjRep</property>
            <script type="groovy">
/**
 * Rule: ProjRep - Budget Data Export Check Data Mismatch
 * Purpose: This validation rule runs before a data export to ensure data integrity.
 * It verifies that the value for each account at the intersection ('BegBalance', 'No Year')
 * perfectly matches the value at ('All Years', 'YearTotal'). If any mismatches
 * are found, the operation is stopped and an error message is shown to the user.
 */

// --- Step 1: Set up a custom error message ---
// This message will be shown to the user if the validation fails.
// Using a message bundle is a best practice for managing user-facing text.
def mbUs = messageBundle(["validation.Mismatch": "Data mismatch found. The 'BegBalance' and 'All Years' totals do not match. Please correct the data before exporting."])
def mbl = messageBundleLoader(["en": mbUs])

// --- Step 2: Collect data from the grid using Maps ---
// Using Maps is crucial here. It allows us to store and retrieve values using the
// account name as a unique key, preventing errors from data being in a different order.
Map<String, BigDecimal> begBalanceData = [:]
Map<String, BigDecimal> allYearsData = [:]

// Populate the first map with data from the 'BegBalance'/'No Year' intersection.
// The key is the account name, and the value is the cell's numeric data.
operation.grid.dataCellIterator("BegBalance", "No Year").each { cell ->
    begBalanceData[cell.getMemberName("Account")] = cell.data
}

// Populate the second map with data from the 'All Years'/'YearTotal' intersection.
operation.grid.dataCellIterator("All Years", "YearTotal").each { cell ->
    allYearsData[cell.getMemberName("Account")] = cell.data
}

// --- Step 3: Compare the data in the two maps ---
// Iterate through each account captured from the 'BegBalance' data.
begBalanceData.each { account, begBalanceValue ->
    // For the current account, get the corresponding value from the 'All Years' map.
    def allYearsValue = allYearsData[account]

    // Check for a mismatch. A mismatch occurs if:
    // 1. The account doesn't exist in the 'All Years' data (allYearsValue is null).
    // 2. The values for the same account in both maps are not equal.
    // Using BigDecimal for comparison is more reliable than comparing strings.
    if (allYearsValue == null || begBalanceValue != allYearsValue) {
        
        // If a mismatch is found, stop the process immediately.
        // The throwVetoException function halts the script and displays our custom error message.
        throwVetoException(mbl, "validation.Mismatch")
    }
}

// If the script completes without throwing an exception, it means all data matched.
println("Data validation successful. No mismatches found.")

            </script>
        </rule>
    </rules>
    <components/>
    <deployobjects>
        <deployobject product="2" application="rpepm" plantype="projrep" obj_id="1" obj_type="1" name="PROJREP - BUDGET DATA EXPORT CHECK DATA MISMATCH"/>
    </deployobjects>
</HBRRepo>