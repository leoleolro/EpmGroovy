/**
 * Rule Name : ProjRep - Calculate Total Tendered Cost
 * Purpose   : Calculate Total Tendered Cost based on Total Line Item
 * Created   : 03/12/2024
 * Author    : Sneha Singh
 */

// ----------------------------
// Step 1: Initialize Cube and Calculation Parameters
// ----------------------------
Cube cube = operation.getApplication().getCube('ProjRep')
CustomCalcParameters calcParameters = new CustomCalcParameters()

// ----------------------------
// Step 2: Build POV (Point of View) across relevant dimensions
// ----------------------------
calcParameters.pov = getCrossJoins([
    ['Descendants([Project Cost Code Budget]', '[Project Cost Code Budget].dimension.Levels(0))'],
    ['Descendants([All Entities]', '[All Entities].dimension.Levels(0))'],
    ['Descendants([YearTotal]', '[YearTotal].dimension.Levels(0))'],
    ['[Load]'],
    ['Descendants([All Projects]', '[All Projects].dimension.Levels(0))'],
    ['[Plan]'],
    ['[Working]'],
    ['[Month]'],
    ['Descendants([All Years]', '[All Years].dimension.Levels(0))']
])

// ----------------------------
// Step 3: Define Source Region
// ----------------------------
calcParameters.sourceRegion = "{[Total Line Item]}"

// ----------------------------
// Step 4: Define Calculation Script
// ----------------------------
String script = """
([Total Tendered Cost]) := ([Total Line Item]);
"""

calcParameters.script = script
calcParameters.roundDigits = 4

// ----------------------------
// Step 5: Execute ASO Custom Calculation
// ----------------------------
cube.executeAsoCustomCalculation(calcParameters)
println("Total Tendered Cost calculation executed successfully.")

// ----------------------------
// Helper Function: Create MDX Cross-Joins
// ----------------------------
def getCrossJoins(List<List<String>> essIntersection) {
    if (essIntersection.size() > 1) {
        return essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members ->
            "Crossjoin(${concat},{${members.join(',')}})"
        }
    }
    return null
}
