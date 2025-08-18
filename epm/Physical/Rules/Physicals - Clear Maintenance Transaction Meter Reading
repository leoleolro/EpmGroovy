/*Rule Name : Physicals - Clear Maintenance Transaction Meter Reading
Purpose : Clear Project Maintenance Meter Readings for All Active Projects
*/

/* RTPS: {Var_Dim_Year},{Var_Dim_Period}  */

def startTime = currentTimeMillis()  
// Context: Capture the script start time to measure execution duration.

Cube cube = operation.getApplication().getCube('ProjRep')  
// Context: Get the reporting cube (ProjRep) where project information is stored.
// Use Case: Used to identify active projects before clearing their meter readings.

FlexibleDataGridDefinitionBuilder dataGrid = cube.flexibleDataGridDefinitionBuilder()  
// Context: Initialize a flexible grid builder for extracting cube data.

dataGrid.setPov('BegBalance','No Scenario','No View','No Year','No Line Item','Load','Working')  
// Context: Define the point of view (POV) for the cube data pull.
// Use Case: Ensures only data in this slice of cube dimensions is evaluated.

dataGrid.addColumn('Project Status')  
// Context: Add "Project Status" as a column in the grid.
// Use Case: This is used to filter active projects.

dataGrid.addRow('ILvl0Descendants(All Entities)','ILvl0Descendants(All Projects)')  
// Context: Add row members - entities and projects at level 0 (leaf level).
// Use Case: Iterates through all projects to determine which ones are active.

dataGrid.setSuppressMissingRows(true)  
dataGrid.setSuppressMissingBlocks(true)  
dataGrid.setSuppressMissingSuppressesZero(true)  
// Context: Suppression rules to reduce unnecessary empty/missing data in the grid.

List<String> ProjList = []  
List<String> ProjListString = []  
// Context: Containers for active project members.

cube.loadGrid(dataGrid.build(), false).withCloseable { grid ->  
    grid.dataCellIterator.each { cell ->  
        if (cell.data == 2)  
            ProjList.add(cell.getMemberName("Project"))  
        // Context: If project status = 2 (active), capture the project name.  
        // Use Case: Only active projects will be cleared in the Physical cube.  
    }  
}

if(ProjList.size()==0){  
    println "Rule : Physicals - Clear Maintenance Meter Readings | No Projects are Active. Exiting script. | Total Time Elapsed : ${(currentTimeMillis() - startTime) / 1000} Secs."  
    // Context: Exit condition if no projects are active.  
    // Use Case: Avoids unnecessary cube operations.  
}  
else{  
    List<String> ProjListFormatted = new ArrayList<>(ProjList)  
    // Context: Convert active project list into a formatted list for MDX region.

    Cube ClearCube = operation.getApplication().getCube('Physical')  
    // Context: Get the "Physical" cube where transactional data resides.
    // Use Case: Clearing data only happens in this cube.

    String mdxRegion = getCrossJoins([  
        ['[Actual]'],  
        ['[Load]'],  
        ['[Month]'],  
        ['[Final]'],  
        ['Descendants([All Days], [All Days].dimension.Levels(0))'],  
        ProjListFormatted,  
        ['Descendants([Maintenance Meters], [Maintenance Meters].dimension.Levels(0))'],  
        ['Descendants([All Entities], [All Entities].dimension.Levels(0))'],  
        ['Descendants([Mining_Equipment], [Mining_Equipment].dimension.Levels(0))'],  
        [mdxParams(rtps.Var_Dim_Year.member)],  
        [mdxParams(rtps.Var_Dim_Period.member)]  
    ])  
    // Context: Build MDX cross-join string for multidimensional selection.  
    // Use Case: Specifies exact data region to be cleared (by time, project, equipment, etc.).  

    Boolean physical = false  

    try {  
        ClearCube.clearPartialData(mdxRegion, physical)  
        // Context: Clears partial data in the Physical cube based on mdxRegion.  
        // Use Case: Removes transactional meter readings for active projects only.  
    } catch (e) {  
        println e.message  
        // Context: Handle errors if clear operation fails.  
    }  

    println "Rule : ProjRep - Clear Project_Actuals | Total Time Elapsed : ${(currentTimeMillis() - startTime) / 1000} Secs."  
    // Context: Print total runtime of the process.  
}  

String getCrossJoins(List<List<String>> essIntersection) {  
    String crossJoinString  
    if (essIntersection.size() > 1) {  
        crossJoinString = essIntersection[1..-1].inject('{' + essIntersection[0].join(',') + '}') { concat, members ->  
            "CrossJoin(" + concat + ',{' + members.join(',') + '})'  
        }  
    }  
    return crossJoinString  
}  
// Context: Utility function to generate MDX cross-joins dynamically.  
// Use Case: Allows flexible targeting of multiple cube dimensions in a single operation.  
