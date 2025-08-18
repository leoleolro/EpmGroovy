/**
 * Rule Name: ProjRep - Calculate Cost Codes From PayItems Form
 * Purpose  : Calculate Tendered Costs by Cost Code using PayItems Form.
 * Created  : 03/12/2025
 * Updated  : 14/07/2025 (Updated edited Total Line Item logic)
 * Author   : Sneha Singh (refined for readability and AI training)
 */

// ----------------------------
// Step 1: Initialize Cube and Custom Calculation Parameters
// ----------------------------
Cube cube = operation.getApplication().getCube('ProjRep')
CustomCalcParameters calcParameters = new CustomCalcParameters()

long startTime = currentTimeMillis()

// ----------------------------
// Step 2: Get Project Cost Code Rate Members
// ----------------------------
Dimension accountDim = operation.application.getDimension("Account", cube)

// Retrieve evaluated members under "Project Cost Code Rates" relative hierarchy
List<Member> mbrs = accountDim.getEvaluatedMembers("@RELATIVE(\"Project Cost Code Rates\",0)", cube)

// Convert members to a quoted list string to use in dataCellIterator
String quotedProjectCostCodeRates = "[${mbrs.collect { "\"${it.name}\"" }.join(', ')}]"

// ----------------------------
// Step 3: Capture Edited Pay Items and Accounts
// ----------------------------
Set<List<String>> editedProjectPayItems = []

operation.grid.dataCellIterator({ DataCell cell ->
    quotedProjectCostCodeRates.contains(cell.getAccountName()) && cell.edited
}).each { DataCell cell ->
    editedProjectPayItems << ([cell.getMemberName("Line Item"), cell.getMemberName("Account"), cell.getMemberName("Version")])
}

editedProjectPayItems = editedProjectPayItems.toUnique()

// ----------------------------
// Step 4: Exit if no cells were edited
// ----------------------------
if (editedProjectPayItems.isEmpty()) {
    println("No cells were edited.")
} else {

    // ----------------------------
    // Step 5: Loop through each edited Pay Item
    // ----------------------------
    editedProjectPayItems.each { tuple ->
        String payItem = tuple[0]
        String rateAccount = tuple[1]

        // ----------------------------
        // Step 5a: Build POV (Point of View) cross-join
        // ----------------------------
        String povData = getCrossJoins([
            ['[Plan]'],
            ['[Working]'],
            ['[Calculated]'],
            ['[Month]'],
            [[payItem]],
            [[rateAccount]],
            [mdxParams(rtps.Var_Dim_Entity.member)],
            [mdxParams(rtps.Var_Dim_Project.member)],
            ['[No Year]'],
            ['[BegBalance]']
        ] as List<List<String>>)
        calcParameters.pov = povData

        // ----------------------------
        // Step 5b: Build Source Region cross-join
        // ----------------------------
        String sourceRegion = getCrossJoins([
            ['[Pay Item Qty]', [rateAccount]],
            ['[No View]'],
            ['[Load]']
        ] as List<List<String>>)
        calcParameters.sourceRegion = sourceRegion

        // ----------------------------
        // Step 5c: Build the calculation script
        // ----------------------------
        String costCodeBudgetScript = """
            ([${rateAccount.replace(" Rate", " Budget")}]) := 
            ([${rateAccount}],[No View],[Load]) * ([Pay Item Qty],[No View],[Load]) ;
        """
        calcParameters.script = costCodeBudgetScript
        calcParameters.roundDigits = 2

        // ----------------------------
        // Step 5d: Execute calculation
        // ----------------------------
        cube.executeAsoCustomCalculation(calcParameters)
    }
}

// ----------------------------
// Step 6: Helper Function to Build MDX Cross-Joins
// ----------------------------
// Accepts a List of List of Strings and recursively builds CrossJoin strings
// Example Input: [['A'], ['B'], ['C']] -> "CrossJoin({A},{B},{C})"
def getCrossJoins(List<List<String>> essIntersection) {
    String crossJoinString
    if (essIntersection.size() > 1) {
        crossJoinString = essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members ->
            "CrossJoin(${concat},{${members.join(',')}})"
        }
    }
    return crossJoinString
}
