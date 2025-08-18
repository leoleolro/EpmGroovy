/**
 * Rule Name: ProjRep - Calculate Monthly Claimed Revenue
 * Purpose  : Calculate Monthly Claimed Revenue based on Bill Quantity and Pay Item Sell Rate.
 * Created  : 30/01/2025
 * Updated  : Refined for maintainability and AI training
 * Author   : Sneha Singh
 */

// ----------------------------
// Step 1: Initialize Cube and Custom Calculation Parameters
// ----------------------------
Cube cube = operation.getApplication().getCube('ProjRep')
CustomCalcParameters calcParameters = new CustomCalcParameters()

// List of accounts to monitor for edits
def payitemBillQuantityAccount = ["Bill Quantity"]

// Get the Period and Year from user variables
String strPeriod = operation.application.getUserVariable("UV_Period").value.name
String strYear = operation.application.getUserVariable("UV_Year").value.name

// ----------------------------
// Step 2: Capture Edited Bill Quantity Cells
// ----------------------------
Set<List<String>> editedTotalLineItems = []

operation.grid.dataCellIterator({ DataCell cell ->
    payitemBillQuantityAccount.contains(cell.getAccountName()) && cell.edited
}).each { DataCell cell ->
    editedTotalLineItems << [
        cell.getMemberName("Line Item"),                    // Pay Item
        cell.getMemberName("Account"),                      // Account (Bill Quantity)
        cell.crossDimCell("Pay Item Escalation Category").formattedValue, // Escalation Category
        (cell.crossDimCell("Pay Item Escalation Category").data).toString() // Raw value
    ]
}

editedTotalLineItems = editedTotalLineItems.toUnique()

// ----------------------------
// Step 3: Exit if no cells were edited
// ----------------------------
if (editedTotalLineItems.isEmpty()) {
    println("No cells were edited - V3")
} else {

    // ----------------------------
    // Step 4: Loop through each edited line item
    // ----------------------------
    editedTotalLineItems.each { tuple ->
        String payItem = tuple[0]
        String billQuantity = tuple[1]
        String escalationCat = tuple[2]
        String isEscalationCatBlank = tuple[3]

        // ----------------------------
        // Step 4a: Build POV (Point of View) cross-join
        // ----------------------------
        String povData = getCrossJoins([
            ['[Plan]'],
            ['[Working]'],
            ['[Load]'],
            ['[Month]'],
            [payItem],
            [mdxParams(rtps.Var_Dim_Entity.member)],
            [mdxParams(rtps.Var_Dim_Project.member)],
            [strPeriod],
            [strYear]
        ])
        calcParameters.pov = povData
        println("POV: ${calcParameters.pov}")

        // ----------------------------
        // Step 4b: Determine calculation logic based on Escalation Category
        // ----------------------------
        String crossData
        String script

        if (isEscalationCatBlank == "0.0") {
            // No escalation category
            crossData = getCrossJoins([
                ['[Bill Quantity]', '[Pay Item Sell Rate]'],
                ['[BegBalance]'],
                ['[No Year]'],
                ['[No View]'],
                ['[Direct Input]'],
                ['[No Line Item]']
            ])
            script = """([Claimed Cost]) := ([Bill Quantity]) * ([Pay Item Sell Rate],[No Year],[BegBalance],[No View]) ;"""
        } else {
            // Escalation category present
            crossData = getCrossJoins([
                ['[Bill Quantity]', escalationCat, '[Pay Item Sell Rate]'],
                ['[BegBalance]'],
                ['[No Year]'],
                ['[No View]'],
                ['[Direct Input]'],
                ['[No Line Item]']
            ])
            script = """([Claimed Cost]) := ([Bill Quantity]) * (([Pay Item Sell Rate],[No Year],[BegBalance],[No View]) * (1 + ([${escalationCat}],[Direct Input],[No Line Item]))) ;"""
        }

        // ----------------------------
        // Step 4c: Execute calculation
        // ----------------------------
        calcParameters.sourceRegion = crossData
        calcParameters.script = script
        calcParameters.roundDigits = 4

        cube.executeAsoCustomCalculation(calcParameters)
    }
}

// ----------------------------
// Step 5: Helper Function to Build MDX Cross-Joins
// ----------------------------
def getCrossJoins(List<List<String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
        crossJoinString = essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members ->
            "CrossJoin(${concat},{${members.join(',')}})"
        }
    }
    return crossJoinString
}
