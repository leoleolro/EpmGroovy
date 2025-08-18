/**
 * Rule: ProjRep - Budget Data Export Check Data Mismatch
 * Purpose: Validates data integrity before exporting budget data from the ProjRep cube.
 *          Specifically, it ensures that the 'BegBalance' (No Year) matches
 *          'All Years' (YearTotal) for each account.
 * Date Created: 18/08/2025
 * Created By: Refined for AI Training and Maintainability
 */

// ----------------------------
// Step 1: Set up a user-facing error message
// ----------------------------
// Best practice: use message bundles for validation messages.
// This ensures multi-language support and easier maintenance.
def mbUs = messageBundle([
    "validation.Mismatch": "Data mismatch found. The 'BegBalance' and 'All Years' totals do not match. Please correct the data before exporting."
])
def mbl = messageBundleLoader(["en": mbUs])

// ----------------------------
// Step 2: Initialize Maps to hold grid data
// ----------------------------
// Maps are used for efficient lookup by Account Name
Map<String, BigDecimal> begBalanceData = [:]
Map<String, BigDecimal> allYearsData = [:]

// ----------------------------
// Step 3: Collect 'BegBalance' data
// ----------------------------
// Capture all cells for intersection: Account x 'BegBalance'/'No Year'
operation.grid.dataCellIterator("BegBalance", "No Year").each { cell ->
    // Store numeric value using account name as key
    begBalanceData[cell.getMemberName("Account")] = cell.data
}

// ----------------------------
// Step 4: Collect 'All Years' data
// ----------------------------
// Capture all cells for intersection: Account x 'All Years'/'YearTotal'
operation.grid.dataCellIterator("All Years", "YearTotal").each { cell ->
    allYearsData[cell.getMemberName("Account")] = cell.data
}

// ----------------------------
// Step 5: Compare the data between 'BegBalance' and 'All Years'
// ----------------------------
// Iterate over each account from BegBalance
begBalanceData.each { account, begBalanceValue ->

    // Retrieve the corresponding value from 'All Years'
    def allYearsValue = allYearsData[account]

    // Check for mismatch:
    // 1. Account missing in 'All Years'
    // 2. Numeric values do not match
    if (allYearsValue == null || begBalanceValue != allYearsValue) {
        // Stop the operation immediately and display a user-friendly error
        throwVetoException(mbl, "validation.Mismatch")
    }
}

// ----------------------------
// Step 6: If no mismatches found, log success
// ----------------------------
println("Data validation successful. All 'BegBalance' values match 'All Years'.")
