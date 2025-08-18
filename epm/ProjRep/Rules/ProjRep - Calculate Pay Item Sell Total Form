/**
 * Rule Name: ProjRep - Calculate Pay Item Sell Total Form
 * Purpose  : Calculate Monthly Claimed Costs and Revenue based on Bill Quantity, Sell Rate, and Estimated Rate
 * Created  : 10/12/2024
 * Updated  : 20/04/2025 (Replaced PayItem by Total Line Item)
 * Author   : Sneha Singh
 */

// ----------------------------
// Step 1: Initialize Cube and Custom Calculation Parameters
// ----------------------------
Cube cube = operation.getApplication().getCube('ProjRep')
CustomCalcParameters calcParameters = new CustomCalcParameters()

// ----------------------------
// Step 2: Build POV (Point of View) cross-join including Total Line Items
// ----------------------------
String povData = getCrossJoins([
    ['[Plan]'],
    ['[Working]'],
    ['[Load]'],
    ['[No View]'],
    ['Descendants([Total Line Item]', '[Total Line Item].dimension.Levels(0))'], // All line items
    [mdxParams(rtps.Var_Dim_Entity.member)],
    [mdxParams(rtps.Var_Dim_Project.member)],
    ['[No Year]'],
    ['[BegBalance]']
])
calcParameters.pov = povData

// ----------------------------
// Step 3: Calculate Pay Item Sell Total
// ----------------------------
calcParameters.sourceRegion = getCrossJoins([
    ['[Pay Item Qty]', '[Pay Item Sell Rate]'],
    ['[BegBalance]']
])
calcParameters.script = "([Pay Item Sell Total]) := ([Pay Item Qty]) * ([Pay Item Sell Rate]);"
calcParameters.roundDigits = 4
cube.executeAsoCustomCalculation(calcParameters)

// ----------------------------
// Step 4: Calculate Pay Item Estimated Total
// ----------------------------
calcParameters.sourceRegion = getCrossJoins([
    ['[Pay Item Qty]', '[Pay Item Estimated Rate]'],
    ['[BegBalance]']
])
calcParameters.script = "([Pay Item Estimated Total]) := ([Pay Item Qty]) * ([Pay Item Estimated Rate]);"
calcParameters.roundDigits = 4
cube.executeAsoCustomCalculation(calcParameters)

// ----------------------------
// Step 5: Refresh Pay Item Sell Rate
// ----------------------------
calcParameters.sourceRegion = getCrossJoins([['[Pay Item Sell Rate]'], ['[BegBalance]']])
calcParameters.script = "([Pay Item Sell Rate]) := ([Pay Item Sell Rate]);"
calcParameters.roundDigits = 4
cube.executeAsoCustomCalculation(calcParameters)

// ----------------------------
// Step 6: Map Pay Item Sell Total to Project Billed Revenue
// ----------------------------
calcParameters.sourceRegion = getCrossJoins([
    ['[Pay Item Sell Total]'],
    ['[Month]'],
    ['[Calculated]']
])
calcParameters.script = "([Project Billed Revenue],[Calculated],[Month]) := ([Pay Item Sell Total],[Load],[No View]);"
calcParameters.roundDigits = 4
cube.executeAsoCustomCalculation(calcParameters)

// ----------------------------
// Helper Function: Build MDX Cross-Joins
// ----------------------------
def getCrossJoins(List<List<String>> essIntersection) {
    if (essIntersection.size() > 1) {
        return essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members ->
            "Crossjoin(${concat},{${members.join(',')}})"
        }
    }
    return null
}
