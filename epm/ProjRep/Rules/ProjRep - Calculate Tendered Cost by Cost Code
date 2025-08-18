/**
 * Rule Name: ProjRep - Calculate Tendered Cost by Cost Code
 * Purpose  : Calculate Monthly Tendered Cost by Cost Code based on Bill Quantity and Project Cost Code Rates
 * Created  : 30/01/2025
 * Updated  : 30/02/2025 (Updated escalation category)
 * Author   : Sneha Singh
 */

// ----------------------------
// Step 1: Initialize Cube and Calculation Parameters
// ----------------------------
Cube cube = operation.getApplication().getCube('ProjRep')
CustomCalcParameters calcParameters = new CustomCalcParameters()

// ----------------------------
// Step 2: Define constants and fetch user variables
// ----------------------------
String[] payItemAccounts = ["Bill Quantity"]
String strPeriod = operation.application.getUserVariable("UV_Period").value.name
String strYear   = operation.application.getUserVariable("UV_Year").value.name

println("Period: ${strPeriod}, Year: ${strYear}")

// ----------------------------
// Step 3: Capture edited Pay Items (Bill Quantity) from the grid
// ----------------------------
Set<List<String>> editedTotalLineItems = []
operation.grid.dataCellIterator({ DataCell cell ->
    payItemAccounts.contains(cell.getAccountName()) && cell.edited
}).each { DataCell cell ->
    editedTotalLineItems << ([cell.getMemberName("Line Item"),
                              cell.getMemberName("Account"),
                              cell.getFormattedValue()]).toUnique()
}

// Exit if no edits found
if (editedTotalLineItems.isEmpty()) {
    println("No cells were edited - exiting.")
    return
} else {
    println("Edited Line Items: ${editedTotalLineItems}")
}

// ----------------------------
// Step 4: Fetch Project Cost Code Rate Members
// ----------------------------
Dimension accountDim = operation.application.getDimension("Account", cube)
List<Member> rateMembers = accountDim.getEvaluatedMembers("@RELATIVE(\"Project Cost Code Rates\",0)", cube)
println("Project Cost Code Rate Members: ${rateMembers.collect { it.name }}")

// ----------------------------
// Step 5: Loop through each edited line item and calculate tendered cost
// ----------------------------
editedTotalLineItems.each { tuple ->
    String lineItem = tuple[0]
    String billQty  = tuple[2]

    println("Processing Line Item: ${lineItem}, Bill Quantity: ${billQty}")

    rateMembers.each { Member rateMember ->
        String rateAccount = rateMember.name
        println("Rate Account: ${rateAccount}")

        // Build POV (Point of View) including Line Item, Entity, Project, Period, and Year
        String povData = getCrossJoins([
            ['[Plan]'],
            ['[Working]'],
            ['[Load]'],
            ['[Month]'],
            [lineItem],
            [mdxParams(rtps.Var_Dim_Entity.member)],
            [mdxParams(rtps.Var_Dim_Project.member)],
            [strPeriod],
            [strYear]
        ])
        calcParameters.pov = povData
        println("POV Data: ${calcParameters.pov}")

        // Build Source Region (Bill Quantity x Rate Account)
        String sourceRegion = getCrossJoins([
            ['[Bill Quantity]', rateAccount],
            ['[BegBalance]'],
            ['[No Year]'],
            ['[No View]']
        ])
        calcParameters.sourceRegion = sourceRegion

        // Calculation Script: Tendered Cost = Bill Quantity * Rate
        String script = """([${rateAccount.replace(" Rate", " Budget")}]) := ([Bill Quantity] * ([${rateAccount}],[No Year],[BegBalance],[No View]));"""
        println("Calculation Script: ${script}")

        calcParameters.script = script
        calcParameters.roundDigits = 4

        // Execute ASO Custom Calculation
        cube.executeAsoCustomCalculation(calcParameters)
    }
}

// ----------------------------
// Step 6: Helper Function - Create MDX Cross-Joins
// ----------------------------
def getCrossJoins(List<List<String>> essIntersection) {
    if (essIntersection.size() > 1) {
        return essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members ->
            "CrossJoin(${concat},{${members.join(',')}})"
        }
    }
    return null
}
